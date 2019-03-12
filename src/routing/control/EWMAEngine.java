package routing.control;

import core.Settings;
import core.SimScenario;
import core.control.ControlMessage;
import core.control.DirectiveCode;
import core.control.DirectiveMessage;
import core.control.MetricCode;
import report.control.directive.DirectiveDetails;
import routing.MessageRouter;
import routing.SprayAndWaitRouter;




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
	private int dropsThreshold;
	
	/** Total number of hosts in the scenario */
	private int totalNrofHostsInTheScenario = SimScenario.getNumberOfHostsConfiguredInTheSettings();

	/**
	 * Controller that reads from the settings, which is set to the value of the
	 * setting control.engine, all the alphas to be used to smooth, using an EWMA
	 * function, the drops received in metrics and the number of copies of the 
	 * message got from directives. It also gets from the settings the drops 
	 * threshold to be considered to generate a directive or not.
	 * 
	 * @param settings the settings object set to the value of the setting
	 *                 control.engine.
	 * @param router, the router who has initialized this directiveEngine.                 
	 */
	public EWMAEngine(Settings settings, MessageRouter router) {
		super(settings, router);
		this.dropsAlpha = (settings.contains(DROPS_ALPHA_S)) ? settings.getDouble(DROPS_ALPHA_S) : EWMAEngine.DEF_ALPHA;
		this.nrofCopiesAlpha = (settings.contains(NROFCOPIES_ALPHA_S)) ? settings.getDouble(NROFCOPIES_ALPHA_S)
				: EWMAEngine.DEF_ALPHA;
		this.directivesAlpha = (settings.contains(DIRECTIVES_ALPHA_S)) ? settings.getDouble(DIRECTIVES_ALPHA_S)
				: EWMAEngine.DEF_DIRECTIVES_ALPHA;		
		this.dropsThreshold = (settings.contains(DROPS_THRESHOLD_S)) ? settings.getInt(DROPS_THRESHOLD_S)
				: EWMAEngine.DEF_DROPS_THRESHOLD;
		this.sDropsAverage = new EWMAProperty(this.dropsAlpha);
		this.sNrofMsgCopiesAverage = new EWMAProperty(this.directivesAlpha);
	}

	@Override
	public void addMetric(ControlMessage metric) {
		MetricsSensed.DropsPerTime nextDropsReading;
		
		if (metric.containsProperty​(MetricCode.DROPS_CODE.toString())){
			nextDropsReading = (MetricsSensed.DropsPerTime)metric.getProperty(MetricCode.DROPS_CODE.toString());
			this.sDropsAverage.aggregateValue(nextDropsReading.getNrofDrops());
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
			this.sNrofMsgCopiesAverage.aggregateValue(nextNrofCopiesReading);
		}
		this.directiveDetails.addDirectiveUsed(directive.getId());
	}

	@Override
	/**
	 * Method that generates a directive message with an 'L' field. It  
	 * generates a directive, just in case the calculated number of copies is 
	 * different from the initial configuration or if the number of copies is the 
	 * same as the initial configuration, but we have received directives from 
	 * other controllers.
	 * @param message the message directive to be generated.
	 * @return DirectiveDetails details of the directive: ID, Host ID, Aggregated
	 * directives used to infer the directive, or null if no directive has been 
	 * generated.
	 * 
	 */
	public DirectiveDetails generateDirective(ControlMessage message) { 
		double newNrofCopies = this.router.getRoutingProperties().get(SprayAndWaitRoutingPropertyMap.MSG_COUNT_PROPERTY);
		DirectiveDetails currentDirectiveDetails = null;
		boolean generateDirective = false;
 		
		if (!this.sDropsAverage.isSet() || (this.sDropsAverage.getValue() <= this.dropsThreshold)) {
			newNrofCopies = newNrofCopies + (newNrofCopies/4);
		}else {
			newNrofCopies = (newNrofCopies)*(3/4);
		}
		if (this.sNrofMsgCopiesAverage.isSet()) {
			newNrofCopies = EWMAProperty.aggregateValue(newNrofCopies, this.sNrofMsgCopiesAverage.getValue(), this.nrofCopiesAlpha);
		}
		
		int newNrofCopiesIntValue = Math.min((int)Math.ceil(newNrofCopies), SimScenario.getNumberOfHostsConfiguredInTheSettings());
		generateDirective = (
			(newNrofCopiesIntValue != this.router.getRoutingProperties().get(SprayAndWaitRoutingPropertyMap.MSG_COUNT_PROPERTY)) ||
			(
			  (newNrofCopiesIntValue == this.router.getRoutingProperties().get(SprayAndWaitRoutingPropertyMap.MSG_COUNT_PROPERTY)) &&
			  (this.directiveDetails.getDirectivesUsed().size()>0))
		);			
			
		if (generateDirective){		
		//Adding the 'L' property in the Directive message.
		((DirectiveMessage) message).addProperty(DirectiveCode.NROF_COPIES_CODE.toString(), newNrofCopiesIntValue);
		//modifying the copies left of the message in the routingConfiguration map.
		this.router.getRoutingProperties().put(SprayAndWaitRoutingPropertyMap.MSG_COUNT_PROPERTY, 
			Math.min(newNrofCopiesIntValue, ((SprayAndWaitRouter)this.router).getCtrlMsgCountPropertyThreshold()));
			
		this.directiveDetails.init(message);
		currentDirectiveDetails = new DirectiveDetails(this.directiveDetails);
		}
		
		this.directiveDetails.reset();		
		return currentDirectiveDetails;
	}

}
