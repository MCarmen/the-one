package routing.control;

import java.util.Collection;
import java.util.HashMap;

import core.Message;
import core.Settings;
import core.SimClock;
import core.control.ControlMessage;
import core.control.DirectiveCode;
import core.control.DirectiveMessage;
import core.control.MetricCode;
import core.control.MetricMessage;
import report.control.directive.DirectiveDetails;
import routing.MessageRouter;
import routing.control.metric.CongestionMetricPerWT;
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
	
			
	/** Accumulated soften drops average  */
	private EWMAProperty sCongestionAverage;
	
	/** Accumulated soften nrofMsgCopies average from the received directives. */
	private EWMAProperty sNrofMsgCopiesAverage;
	
	/** 
	 * Alpha to be used to calculate sDropsAverage with the EWMA:
	 * sdropsAverage = (1-dropsAlpha) * sdropsAverage + dropsAlpha * drops_messured.
	 */
	private double congestionAlpha;
	
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
		
		
	/** The aggregated drops after the last metric was generated */
	//private double lastSCongestionAvg = -1;
	
	/**
	 * Support class to calculate the mean deviation of the measured drops during 
	 * all the simulation.
	 */
	private MeanDeviationEWMAProperty sCongestionMeanDeviation; 
	
	/**
	 * Support class to calculate the mean deviation of the received directives during 
	 * all the simulation.
	 */
	private MeanDeviationEWMAProperty sDirectivesMeanDeviation;
	
	/**
	 * State of congestion of the network.
	 */
	private CongestionState congestionState;
	

	/**
	 * Controller that reads from the settings, which is set to the value of the
	 * setting control.engine, all the alphas to be used to smooth, using an EWMA
	 * function, the drops received in metrics and the number of copies of the 
	 * message got from directives. It also gets from the settings the drops 
	 * threshold to be considered to generate a directive or not.
	 * 
	 * @param engineSettings the settings object set to the value of the setting
	 *                 control.engine and which has as a subnamespace the 
	 *                 'DirectiveEngine' namespace.
	 * @param router, the router who has initialized this directiveEngine.                 
	 */
	public EWMAEngine(Settings engineSettings, MessageRouter router) {
		super(engineSettings, router);
		this.congestionState = CongestionState.INITIAL;
		this.congestionAlpha = (engineSettings.contains(CONGESTION_ALPHA_S)) ? engineSettings.getDouble(CONGESTION_ALPHA_S) : EWMAEngine.DEF_ALPHA;
		this.nrofCopiesAlpha = (engineSettings.contains(NROFCOPIES_ALPHA_S)) ? engineSettings.getDouble(NROFCOPIES_ALPHA_S)
				: EWMAEngine.DEF_ALPHA;
		this.directivesAlpha = (engineSettings.contains(DIRECTIVES_ALPHA_S)) ? engineSettings.getDouble(DIRECTIVES_ALPHA_S)
				: EWMAEngine.DEF_DIRECTIVES_ALPHA;	
		
		this.sCongestionAverage = new EWMAProperty(this.congestionAlpha);
		this.sNrofMsgCopiesAverage = new EWMAProperty(this.directivesAlpha);	
		this.sCongestionMeanDeviation = new MeanDeviationEWMAProperty(this.meanDeviationFactor);
		this.sDirectivesMeanDeviation = new MeanDeviationEWMAProperty(this.meanDeviationFactor);
	}
	
	@Override
	protected void resetDirectiveCycleSettings() {		
		super.resetDirectiveCycleSettings();
		this.directiveDetails.reset();
		//this.lastSCongestionAvg = this.sCongestionAverage.getValue();
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
	 * Generic method that first checks whether the ctrlMessage contains a  
	 * field with the messageCode. If so, it checks if ctrlCycleControlHistory
	 * contains an entry with the same host address as the ctrlMessage.from property. 
	 * In that case, if ctrlMessage is older than the one already indexed, 
	 * ctrlMessage is not added to the history. If the history does not have 
	 * any entry ctrlMessage.from, ctrlMessage is added.
	 * @param ctrlMessage the message to be added to the history.
	 * @param ctrlCycleControlHistory the history dictionary.
	 * @param messageCode the control message code field.
	 */
	//MetricCode.CONGESTION_CODE.toString()
	public void addCtrlMsg(ControlMessage ctrlMessage, 
			HashMap<Integer, ControlMessage> ctrlCycleControlHistory, 
			String messageCode) {
		boolean putMetric = true;
		
		if ( ctrlMessage.containsProperty​(messageCode)) {
			if(!this.isASelfGeneratedCtrlMsg(ctrlMessage)) {
				this.receivedCtrlMsgInDirectiveCycle = true;
			}			
			if (ctrlCycleControlHistory.containsKey(ctrlMessage.getFrom().getAddress())){
				ControlMessage indexedCtrlMessage = ctrlCycleControlHistory.get(ctrlMessage.getFrom().getAddress());
				if (indexedCtrlMessage.getCreationTime() >= ctrlMessage.getCreationTime()) {
					putMetric = false;
				}
			}
			if(putMetric) {
				ctrlCycleControlHistory.put(ctrlMessage.getFrom().getAddress(), ctrlMessage);
			}			
		}	
		
	}
	
	@Override
	public void addMetric(ControlMessage metric) {
		this.addCtrlMsg(metric, this.ctrlCycleMetricHistory, MetricCode.CONGESTION_CODE.toString());		
	}

	@Override
	/**
	 * When a directive from another controller is received it is aggregated using
	 * an EWMA. The EWMA moving average is never reset.
	 * @param directive the directive to be aggregated.
	 */
	public void addDirective(ControlMessage directive) {
		this.addCtrlMsg(directive, this.ctrlCycleDirectiveHistory, DirectiveCode.NROF_COPIES_CODE.toString());		
	}
	
	/**
	 * Method that goes over the the ctrlCycleMetricHistory aggregating through
	 * an EWMA each entry into the property "sCongestionAverage".
	 */
	private void resumeCtrlCycleMetricHistory() {
		Collection<ControlMessage> metricHistory = this.ctrlCycleMetricHistory.values();
		CongestionMetricPerWT nextCongestionReading;
		double congestionMetricAvg;
		double congestionMetricMeanDeviationAvg = 0;
		
		for(Message msg : metricHistory) {
			MetricMessage metric = (MetricMessage)msg;
			nextCongestionReading = (CongestionMetricPerWT)metric.getProperty(MetricCode.CONGESTION_CODE.toString());
			congestionMetricAvg = this.sCongestionAverage.getValue();
			this.sCongestionAverage.aggregateValue(nextCongestionReading.getCongestionMetric());
			this.directiveDetails.addMetricUsed(metric, congestionMetricAvg, this.sCongestionAverage.getValue(), 
					congestionMetricMeanDeviationAvg, this.sCongestionMeanDeviation.getValue());	
		}		
	}
	
	/**
	 * Method that goes over the the ctrlCycleDirectiveHistory aggregating through
	 * an EWMA each entry into the property "sNrofMsgCopiesAverage".
	 */
	private void resumeCtrlCycleDirectiveHistory() {
		Collection<ControlMessage> directiveHistory = this.ctrlCycleDirectiveHistory.values();		
		double nextNrofCopiesReading = 0;
		double directiveAvg = 0;
		
		for(Message msg : directiveHistory) {
			DirectiveMessage directive = (DirectiveMessage)msg; 
			nextNrofCopiesReading = (int) directive.getProperty(DirectiveCode.NROF_COPIES_CODE.toString());
			directiveAvg = this.sNrofMsgCopiesAverage.getValue();
			this.sNrofMsgCopiesAverage.aggregateValue(nextNrofCopiesReading);
			this.directiveDetails.addDirectiveUsed(directive, directiveAvg, nextNrofCopiesReading);
		}
	}
	
	/**
	 * Method that updates the current congestion state of the node depending on the
	 * current congestion reading.
	 */
	private void updateCongestionSate() {
		switch (this.congestionState) {
		case INITIAL:
			this.congestionState = (this.sCongestionAverage.getValue() >= this.congestionThrMax)
					? CongestionState.CONGESTION
					: CongestionState.NO_CONGESTION;
			break;
		case NO_CONGESTION:
			if (this.sCongestionAverage.getValue() >= this.congestionThrMax) {
				this.congestionState = CongestionState.CONGESTION;
			}
			break;
		case CONGESTION:
			if (this.sCongestionAverage.getValue() < this.congestionThrMin) {
				this.congestionState = CongestionState.NO_CONGESTION;
			}
		}
	}

	@Override
	/**
	 * Method that generates a directive message with an 'L' field. It always
	 * generates a directive, even if the number of copies is the same as the
	 * initial configuration.
	 * 
	 * @param message the message directive to be generated.
	 * @return DirectiveDetails details of the directive: ID, Host ID, Aggregated
	 *         directives used to infer the directive, or null if no directive has
	 *         been generated.
	 * 
	 */
	public DirectiveDetails generateDirective(ControlMessage message) {
		double currentTime = SimClock.getTime(); //DEBUG
		double newNrofCopies = this.router.getRoutingProperties()
				.get(SprayAndWaitRoutingPropertyMap.MSG_COUNT_PROPERTY);
		DirectiveDetails currentDirectiveDetails = null;
		
		this.resumeCtrlCycleMetricHistory();
		this.resumeCtrlCycleDirectiveHistory();
		
		// if we have received metrics and no silence from other nodes != from ourselves.
		if (this.sCongestionAverage.isSet() && this.receivedCtrlMsgInDirectiveCycle) {
			this.updateCongestionSate();
			if (this.congestionState == CongestionState.NO_CONGESTION) {
				// applying additive increase
				newNrofCopies = Math.ceil(newNrofCopies + this.additiveIncrease);
			} else if (this.congestionState == CongestionState.CONGESTION) {
				// multiplicative decrease
				newNrofCopies = Math.ceil(newNrofCopies * this.multiplicativeDecrease);
				if (newNrofCopies <= 0)
					newNrofCopies = 1;
			}
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
			
		this.directiveDetails.init(message, this.congestionState);
		currentDirectiveDetails = new DirectiveDetails(this.directiveDetails);

		this.resetDirectiveCycleSettings();
		return currentDirectiveDetails;
	}

	public static enum CongestionState{
		CONGESTION, NO_CONGESTION, INITIAL;
		
		public String toString() {
			String congestionStateStr;
			switch(this) {
			case CONGESTION: 
				congestionStateStr="congestion";
				break;
			case NO_CONGESTION:	
				congestionStateStr="no_congestion";
				break;
			default: 
				congestionStateStr="initial";
			}
			return congestionStateStr;
		}
	}
}
