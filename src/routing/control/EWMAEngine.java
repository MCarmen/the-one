package routing.control;

import core.Settings;
import core.SimClock;
import core.control.ControlMessage;
import core.control.DirectiveCode;
import core.control.DirectiveMessage;
import core.control.MetricCode;
import core.control.MetricMessage;
import report.control.directive.DirectiveDetails;
import routing.MessageRouter;
import routing.control.util.EWMAProperty;
import routing.control.util.MeanDeviationEWMAProperty;;




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
	private static final String DROPS_ALPHA_S = "dropsAlpha";

	/** Alpha -setting id ({@value}) in the EWMAEngine name space for the 
	 * number of copies of the message */
	private static final String NROFCOPIES_ALPHA_S = "nrofCopiesAlpha";
	
	/** Alpha -setting id ({@value}) in the EWMAEngine name space for the 
	 * number of copies of the message got from other directives. */	
	private static final String DIRECTIVES_ALPHA_S = "directivesAlpha";
	
	/** dropsThreshold -setting id ({@value}) in the EWMAEngine name space for the drops */
	private static final String DROPS_THRESHOLD_S = "dropsThreshold";
				
	/** congestionWeight -setting id ({@value}) in the control name space */
	protected static final String CONGESTION_WEIGHT_S = "congestionWeight";		
	
	/** Default value for the congestionWeight. */
	protected static final int DEF_CONGESTION_WEIGHT = 1;	
	
	/** alpha-setting's default value if it is not specified in the settings 
	 ({@value}) */
	private static final double DEF_ALPHA = 0.2; 
	
	/** directivesAlpha-setting's default value if it is not specified in the settings 
	 ({@value}) */
	private static final double DEF_DIRECTIVES_ALPHA = 0.2;
	
	/** dropsThreshold-setting's default value if it is not specified in the settings 
	 ({@value}) */
	private static final int DEF_DROPS_THRESHOLD = 0;	
			
	/** Accumulated soften drops average  */
	private EWMAProperty sDropsAverage;
	
	/** Accumulated soften nrofMsgCopies average from the received directives. */
	private EWMAProperty sNrofMsgCopiesAverage;
	
	/** 
	 * Alpha to be used to calculate sDropsAverage with the EWMA:
	 * sdropsAverage = (1-dropsAlpha) * sdropsAverage + dropsAlpha * drops_messured.
	 */
	private double dropsAlpha;
	
	/** 
	 * Alpha to be used to calculate sNrofCopiesAverage, from the received 
	 * directives, with the EWMA:
	 * sNrofCopiesAverage = (1-directivesAlpha) * sNrofCopiesAverage + directivesAlpha * nrofCopies_messured.
	 */	
	private double directivesAlpha;

	/** 
	 * Alpha to be used to combine with an EWMA the newNrofCopies calculated 
	 * in the process of generate a directive, with the sNrofCopiesAverage 
	 * aggregated from the received directives.
	 */	
	private double nrofCopiesAlpha;	
	
	/**
	 * Drops threshold from which a directive is generated to decrease the 
	 * number of message copies. 
	 */
	private double dropsThreshold;
	
		
	/** The aggregated drops after the last metric was generated */
	private double lastSDropsAverage = -1;
	
	/**
	 * Support class to calculate the mean deviation of the measured drops during 
	 * all the simulation.
	 */
	private MeanDeviationEWMAProperty sDropsMeanDeviation; 
	
	/**
	 * Support class to calculate the mean deviation of the received directives during 
	 * all the simulation.
	 */
	private MeanDeviationEWMAProperty sDirectivesMeanDeviation;

	/**
	 *  weight applied to the congestion average. Used in the control function (default = 1)
	 */
	private double congestionWeight;

	/**
	 * Controller that reads from the settings, which is set to the value of the
	 * setting control.engine, all the alphas to be used to smooth, using an EWMA
	 * function, the drops received in metrics and the number of copies of the 
	 * message got from directives. It also gets from the settings the drops 
	 * threshold to be considered to generate a directive or not.
	 * 
	 * @param engineSettings the settings object set to the value of the setting
	 *                 control.engine.
	 * @param router, the router who has initialized this directiveEngine.                 
	 */
	public EWMAEngine(Settings engineSettings, MessageRouter router) {
		super(engineSettings, router);
		this.dropsAlpha = (engineSettings.contains(DROPS_ALPHA_S)) ? engineSettings.getDouble(DROPS_ALPHA_S) : EWMAEngine.DEF_ALPHA;
		this.nrofCopiesAlpha = (engineSettings.contains(NROFCOPIES_ALPHA_S)) ? engineSettings.getDouble(NROFCOPIES_ALPHA_S)
				: EWMAEngine.DEF_ALPHA;
		this.directivesAlpha = (engineSettings.contains(DIRECTIVES_ALPHA_S)) ? engineSettings.getDouble(DIRECTIVES_ALPHA_S)
				: EWMAEngine.DEF_DIRECTIVES_ALPHA;
		this.dropsThreshold = (engineSettings.contains(DROPS_THRESHOLD_S)) ? engineSettings.getDouble(DROPS_THRESHOLD_S)
				: EWMAEngine.DEF_DROPS_THRESHOLD;			
		this.congestionWeight = engineSettings.contains(CONGESTION_WEIGHT_S) ?
				engineSettings.getDouble(CONGESTION_WEIGHT_S) : DEF_CONGESTION_WEIGHT;
	
		this.sDropsAverage = new EWMAProperty(this.dropsAlpha);
		this.sNrofMsgCopiesAverage = new EWMAProperty(this.directivesAlpha);	
		this.sDropsMeanDeviation = new MeanDeviationEWMAProperty(this.meanDeviationFactor);
		this.sDirectivesMeanDeviation = new MeanDeviationEWMAProperty(this.meanDeviationFactor);
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
	private boolean adjustSDropsAverage() {
		boolean adjusted = false;
		int metricsCycles;

		if (this.sDropsAverage.isSet() && (this.lastSDropsAverage == this.sDropsAverage.getValue())) {
			metricsCycles = (int) Math.ceil(this.directiveGenerationInterval / this.metricGenerationInterval);
			adjusted = true;
			for (int i = 0; i < metricsCycles; i++) {
				this.sDropsAverage.aggregateValue(0);
				this.sDropsMeanDeviation.aggregateValue(0, this.sDropsAverage);
			}
		}

		return adjusted;
	}

	@Override
	public void addMetric(ControlMessage metric) {
		MetricsSensed.DropsPerTime nextDropsReading;
		double dropsAvg;
		double dropsMeanDeviationAvg;

		if ((!this.hasMetricExpired((MetricMessage)metric)) && 
		(metric.containsProperty​(MetricCode.DROPS_CODE.toString())) ){
			nextDropsReading = (MetricsSensed.DropsPerTime) metric.getProperty(MetricCode.DROPS_CODE.toString());
			dropsMeanDeviationAvg = this.sDropsMeanDeviation.getValue();
			this.sDropsMeanDeviation.aggregateValue(nextDropsReading.getNrofDrops(), this.sDropsAverage);
			dropsAvg = this.sDropsAverage.getValue();
			this.sDropsAverage.aggregateValue(nextDropsReading.getNrofDrops());
			this.directiveDetails.addMetricUsed(metric, dropsAvg, this.sDropsAverage.getValue(), 
					dropsMeanDeviationAvg, this.sDropsMeanDeviation.getValue());
		}
	}

	@Override
	/**
	 * When a directive from another controller is received it is aggregated using
	 * an EWMA. it is never reset.
	 */
	public void addDirective(ControlMessage directive) {
		double nextNrofCopiesReading;
		
		if(directive.containsProperty​(DirectiveCode.NROF_COPIES_CODE.toString())) {
			nextNrofCopiesReading = (double)directive.getProperty(DirectiveCode.NROF_COPIES_CODE.toString());
			this.sDirectivesMeanDeviation.aggregateValue(nextNrofCopiesReading, this.sNrofMsgCopiesAverage);			
			this.sNrofMsgCopiesAverage.aggregateValue(nextNrofCopiesReading);
		}
		this.directiveDetails.addDirectiveUsed(directive.getId());
	}
	

	@Override
	/**
	 * Method that generates a directive message with an 'L' field. It always 
	 * generates a directive, even if the number of copies is the same as the 
	 * initial configuration.
	 * @param message the message directive to be generated.
	 * @return DirectiveDetails details of the directive: ID, Host ID, Aggregated
	 * directives used to infer the directive, or null if no directive has been 
	 * generated.
	 * 
	 */
	public DirectiveDetails generateDirective(ControlMessage message) { 
//		double currentTime = SimClock.getTime(); //DEBUG
		double newNrofCopies = this.router.getRoutingProperties().get(SprayAndWaitRoutingPropertyMap.MSG_COUNT_PROPERTY);
		DirectiveDetails currentDirectiveDetails = null;
		 		
		//this.adjustSDropsAverage();
		//Additive increase
		if (!this.sDropsAverage.isSet() || (this.sDropsAverage.getValue()  <= this.dropsThreshold)) {
			newNrofCopies = Math.ceil(newNrofCopies + (newNrofCopies* this.incrementCopiesRatio));
		//multiplicative decrease	
		}else {
			newNrofCopies = Math.ceil(newNrofCopies * this.sDropsAverage.getValue() * this.congestionWeight);
			if (newNrofCopies <= 0) newNrofCopies = 1;
		}
		
		//number of copies aggregated from received directives.
		if (this.sNrofMsgCopiesAverage.isSet()) {
			newNrofCopies = Math.floor(EWMAProperty.aggregateValue(newNrofCopies, this.sNrofMsgCopiesAverage.getValue(), this.nrofCopiesAlpha));
		}
		
		//int newNrofCopiesIntValue = Math.min(((int)newNrofCopies),SimScenario.getNumberOfHostsConfiguredInTheSettings());
		int newNrofCopiesIntValue = Math.min((int)newNrofCopies, this.maxCopies);
							
		//Adding the 'L' property in the Directive message.
		((DirectiveMessage) message).addProperty(DirectiveCode.NROF_COPIES_CODE.toString(), newNrofCopiesIntValue);
		//modifying, in the routingConfiguration map, the initial number of copies for new messages.
		this.router.getRoutingProperties().put(SprayAndWaitRoutingPropertyMap.MSG_COUNT_PROPERTY, 
			newNrofCopiesIntValue);
			
		this.directiveDetails.init(message);
		currentDirectiveDetails = new DirectiveDetails(this.directiveDetails);

		
		this.directiveDetails.reset();
		this.lastSDropsAverage = this.sDropsAverage.getValue();
		return currentDirectiveDetails;
	}

}
