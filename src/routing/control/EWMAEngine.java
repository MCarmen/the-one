package routing.control;

import java.util.Properties;

import core.Settings;
import core.control.ControlMessage;
import core.control.DirectiveCode;
import core.control.MetricCode;
import core.control.MetricMessage;
import report.control.directive.DirectiveDetails;
import report.control.directive.EWMADirectiveDetails;
import report.control.directive.LRDirectiveDetails;
import routing.control.metric.CongestionMetric;
import routing.control.util.EWMAProperty;
import routing.control.util.EWMAPropertyIterative;


/**
 * Implementation of a Directive Engine {@link  routing.control.DirectiveEngine}.
 * In this implementation, all the metrics of the drops readings are  aggregated 
 * by calculating an EWMA.The accumulated smoothed value is kept along the whole
 * simulation. 
 * An EWMA is also applied to accumulate an average of the directives received 
 * announcing the number of copies of the messages in the network they have 
 * calculated. The accumulated smoothed value is kept along the whole
 * simulation. 
 *
 */
public class EWMAEngine extends DirectiveEngine {
	
	/** Alpha -setting id ({@value}) in the EWMAEngine name space for the drops */
	private static final String CONGESTION_ALPHA_S = "congestionAlpha";

	/** Alpha -setting id ({@value}) in the EWMAEngine name space for the 
	 * number of copies of the message */
	private static final String NROFCOPIES_ALPHA_S = "nrofCopiesAlpha";
	
	/** Alpha -setting id ({@value}) in the EWMAEngine name space for the 
	 * number of copies of the message got from other directives. */	
	private static final String DIRECTIVES_ALPHA_S = "directivesAlpha";

	
	/** alpha-setting's default value if it is not specified in the settings 
	 ({@value}) */
	private static final double DEF_ALPHA = 0.2; 
	
	/** directivesAlpha-setting's default value if it is not specified in the settings 
	 ({@value}) */
	private static final double DEF_DIRECTIVES_ALPHA = 0.2;
	
	/** The property name to index  the congestion mobile average previous to aggregate a new value. */
	private static final String PROP_PREV_CONGESTION_METRIC_AVG = "previousCongestionMetricAvg";
			
	/** Accumulated soften drops average  */
	private EWMAPropertyIterative sCongestionAverage;
	
	/** Accumulated soften nrofMsgCopies average from the received directives. */
	private EWMAProperty sNrofMsgCopiesAverage;
	
	/** 
	 * Alpha to be used to calculate sCongestionAverage with the EWMA:
	 * sCongestionAverage = (1-congestionAlpha) * sCongestionAverage + 
	 * congestionAlpha * CongestionAverage_messured.
	 */
	private double congestionAlpha;
	
	/** 
	 * Alpha to be used to aggregate to the sNrofCopiesAverage soft average the new  
	 * nrofCopies_messured from the received directive, with the EWMA:
	 * sNrofCopiesAverage = (1-directivesAlpha) * sNrofCopiesAverage + directivesAlpha * nrofCopies_messured.
	 */	
	private double directivesAlpha;

	/** 
	 * Alpha to be used to combine with an EWMA the newNrofCopies calculated 
	 * in the process of generate a directive, with the sNrofCopiesAverage 
	 * aggregated from the received directives.
	 */	
	private double nrofCopiesAlpha;	
	
	protected EWMADirectiveDetails directiveDetails;
	
	/**
	 * Controller that reads from the settings, which is set to the value of the
	 * setting control.engine, all the alphas to be used to smooth, using an EWMA
	 * function, the congestion measure received in metrics and the number of copies of the 
	 * message got from directives. It also gets from the settings the congestion 
	 * threshold to be considered to generate a directive or not.
	 * 
	 * @param engineSettings the settings object set to the value of the setting
	 *                 control.engine and which has as a subnamespace the 
	 *                 'DirectiveEngine' namespace.
	 * @param router, the router who has initialized this directiveEngine.                 
	 */
	public EWMAEngine(Settings engineSettings, SprayAndWaitControlRouter router) {
		super(engineSettings, router);
		this.directiveDetails = (EWMADirectiveDetails)this.newEmptyDirectiveDetails();
		this.congestionAlpha = (engineSettings.contains(CONGESTION_ALPHA_S)) ? engineSettings.getDouble(CONGESTION_ALPHA_S) : EWMAEngine.DEF_ALPHA;
		this.nrofCopiesAlpha = (engineSettings.contains(NROFCOPIES_ALPHA_S)) ? engineSettings.getDouble(NROFCOPIES_ALPHA_S)
				: EWMAEngine.DEF_ALPHA;
		this.directivesAlpha = (engineSettings.contains(DIRECTIVES_ALPHA_S)) ? engineSettings.getDouble(DIRECTIVES_ALPHA_S)
				: EWMAEngine.DEF_DIRECTIVES_ALPHA;	
		
		this.sCongestionAverage = new EWMAPropertyIterative(this.congestionAlpha);
		this.sNrofMsgCopiesAverage = new EWMAProperty(this.directivesAlpha);	
	}
		
	/**
	 * Method that checks if the property lastDropsAverage has the same value as the
	 * current value of the property sDropsAverage. In that case, it means that the
	 * controller has not received any metric in this cycle. In this case, 
	 * it aggregates to the property sDropsAverage as many 0(drops) as metric's 
	 * cycles has a directive generation cycle. It also modifies the 
	 * dropsMeanDeviation adding as many 0 likewise the previous case.
	 * 
	 * @return true if the sDropsAverage has been updated or not.
	 */
//	private boolean adjustSDropsAverage() {
//		boolean adjusted = false;
//		int metricsCycles;
//
//		if (this.sCongestionAverage.isSet() && (this.lastSCongestionAvg == this.sCongestionAverage.getValue())) {
//			metricsCycles = (int) Math.ceil(this.directiveGenerationInterval / this.metricGenerationInterval);
//			adjusted = true;
//			for (int i = 0; i < metricsCycles; i++) {
//				this.sCongestionAverage.aggregateValue(0);
//				this.sCongestionMeanDeviation.aggregateValue(0, this.sCongestionAverage);
//			}
//		}
//
//		return adjusted;
//	}
	
	/**
	 * Method called when a new metric has to be considered for the 
	 * directive to be generated.  
	 * @param metric metric to be considered
	 */
	public void addMetric(MetricMessage metric) {
		if (this.isASelfGeneratedCtrlMsg(metric)) {
			this.localMetric = metric;
		} else {
			this.receivedCtrlMsgInDirectiveCycle = true;
			this.addMetricStraightForward(metric);
		}
		if((this.receivedCtrlMsgInDirectiveCycle) && (this.localMetric!=null)) {
			this.addMetricStraightForward(this.localMetric);
			this.localMetric = null;
		}
	}

	/**
	 * Method that aggregates the metric passed as a parameter to the historical
	 * aggregated value.
	 * @param metric The metric to be aggregated.
	 */
	protected void addMetricStraightForward(MetricMessage metric) {
		CongestionMetric nextCongestionReading = (CongestionMetric) metric
				.getProperty(MetricCode.CONGESTION_CODE);
		double congestionMetricAvg = this.sCongestionAverage.getValue();
		this.sCongestionAverage.aggregateValue(nextCongestionReading.getCongestionValue(), nextCongestionReading.getNrofAggregatedMetrics());
		this.directiveDetails.addMetricUsed(metric, congestionMetricAvg, this.sCongestionAverage.getValue());		
	}
		

	@Override
	/**
	 * When a directive from another controller is received it is aggregated using
	 * an EWMA. The EWMA moving average is never reset.
	 * It is not possible to receive your own generated directive as it is sent
	 * both the the messages list and to the delivered list.
	 * 
	 * @param directive the directive to be aggregated.
	 */
	public void addDirective(ControlMessage directive) {
		// TODO To be decided if the directives will be aggregated.
		double nextNrofCopiesReading = 0;
		double directiveAvg = 0;

		nextNrofCopiesReading = (int) directive.getProperty(DirectiveCode.NROF_COPIES_CODE);
		directiveAvg = this.sNrofMsgCopiesAverage.getValue();
		this.sNrofMsgCopiesAverage.aggregateValue(nextNrofCopiesReading);
		this.directiveDetails.addDirectiveUsed(directive, directiveAvg, nextNrofCopiesReading,
				this.sNrofMsgCopiesAverage.getValue());
	}
		
	
	@Override
	protected boolean isSetCongestionMeasure() {
		return this.sCongestionAverage.isSet();		
	}
	
	@Override
	protected void initDirectiveDetails(ControlMessage message, int lasCtrlCycleNrofCopies) {		
		this.directiveDetails.init(message, lasCtrlCycleNrofCopies, this.sCongestionAverage.getValue(),
				this.congestionState, this.sNrofMsgCopiesAverage.getValue());	
	}
	
	@Override
	protected DirectiveDetails copyDirectiveDetails() {
		return new EWMADirectiveDetails(this.directiveDetails);
	}
	
	@Override
	protected DirectiveDetails newEmptyDirectiveDetails() {
		return new EWMADirectiveDetails();
	}
	
	//
	/**
	 * Method that includes in the directiveDetails object the details of the 
	 * computed metric.
	 * @param metric The computed metric. 
	 * @param details Specific details of the aggregated metric.
	 */
	protected void addMetricUsedToDirectiveDetails(ControlMessage metric, Properties details) {
		double previousCongestionMetricAvg = (double)details.get(PROP_PREV_CONGESTION_METRIC_AVG);
		((EWMADirectiveDetails)this.directiveDetails).addMetricUsed(metric, previousCongestionMetricAvg, this.sCongestionAverage.getValue());
	}

	@Override
	protected double getCalculatedCongestion() {
		return this.sCongestionAverage.getValue();
	}
	
	@Override
	protected DirectiveDetails getDirectiveDetails() {
		return this.directiveDetails;
	}

}
