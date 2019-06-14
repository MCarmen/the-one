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
	
	private static final String METRICS_GENERATION_INTERVAL_S = "metricGenerationInterval"; 
	private static final String DIRECTIVE_GENERATION_INTERVAL_S = "directiveGenerationInterval";
	
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
	
	/**
	 * Interval for the metrics generation. If it is not set, it is assumed 
	 * the interval for the directives generation and this value is set to -1.
	 */
	private int metricGenerationInterval;

	/**
	 * Interval for the directives generation. If it is not set 
	 * this value is set to -1.
	 */
	private int directiveGenerationInterval;
	
	
	/** Total number of hosts in the scenario */
	private int totalNrofHostsInTheScenario = SimScenario.getNumberOfHostsConfiguredInTheSettings();
	
	/** The aggregated drops after the last metric was generated */
	private double lastSDropsAverage = -1;
	

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
		this.directiveGenerationInterval = (settings.contains(DIRECTIVE_GENERATION_INTERVAL_S)) ? 
				settings.getInt(DIRECTIVE_GENERATION_INTERVAL_S) : 1; 
		this.metricGenerationInterval =  
				(settings.contains(METRICS_GENERATION_INTERVAL_S))
				? settings.getInt(METRICS_GENERATION_INTERVAL_S)
				: this.directiveGenerationInterval;
		this.sDropsAverage = new EWMAProperty(this.dropsAlpha);
		this.sNrofMsgCopiesAverage = new EWMAProperty(this.directivesAlpha);						
	}
	
	/**
	 * Method that checks if the property lastDropsAverage has the same value as the
	 * current value of the property sDropsAverage. In that case, it means that the
	 * controller has not received any metric in this cycle. This fact aggregates 
	 * to the property sDropsAverage as many 0(drops) as metric's cycles has a 
	 * directive cycle in the property sDropsAverage.
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
			}
		}

		return adjusted;
	}

	@Override
	public void addMetric(ControlMessage metric) {
		MetricsSensed.DropsPerTime nextDropsReading;
		double dropsAvg;
		
		if (metric.containsProperty​(MetricCode.DROPS_CODE.toString())){
			nextDropsReading = (MetricsSensed.DropsPerTime)metric.getProperty(MetricCode.DROPS_CODE.toString());
			dropsAvg = this.sDropsAverage.getValue();
			this.sDropsAverage.aggregateValue(nextDropsReading.getNrofDrops());
			this.directiveDetails.addMetricUsed(metric, dropsAvg, this.sDropsAverage.getValue());
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
		double newNrofCopies = this.router.getRoutingProperties().get(SprayAndWaitRoutingPropertyMap.MSG_COUNT_PROPERTY);
		int initialNrofCopies = ((SprayAndWaitRouter)this.router).getInitialNrofCopies();
		DirectiveDetails currentDirectiveDetails = null;
		boolean generateDirective = false;
 		
		this.adjustSDropsAverage();
		
		if (!this.sDropsAverage.isSet() || (this.sDropsAverage.getValue() <= this.dropsThreshold)) {
			newNrofCopies = newNrofCopies + (newNrofCopies/4);
			
		}else {
			newNrofCopies = (newNrofCopies)*(0.25);
			if ((int)newNrofCopies <= 0) newNrofCopies = 1;
		}
		//number of copies aggregated from received directives.
		if (this.sNrofMsgCopiesAverage.isSet()) {
			newNrofCopies = EWMAProperty.aggregateValue(newNrofCopies, this.sNrofMsgCopiesAverage.getValue(), this.nrofCopiesAlpha);
		}
		
		//int newNrofCopiesIntValue = Math.min((int)Math.ceil(newNrofCopies),SimScenario.getNumberOfHostsConfiguredInTheSettings());
		int newNrofCopiesIntValue = (int)Math.ceil(newNrofCopies);
							
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
