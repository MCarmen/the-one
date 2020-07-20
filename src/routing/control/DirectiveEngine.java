package routing.control;

import java.util.HashMap;

import core.Message;
import core.Settings;
import core.SimClock;
import core.control.ControlMessage;
import core.control.DirectiveMessage;
import core.control.MetricMessage;
import report.control.directive.DirectiveDetails;
import routing.MessageRouter;

/**
 * Abstract class for the Engines that generate directives
 *
 */
public abstract class DirectiveEngine {
	
	/** incrementCopiesRatio -setting id ({@value}) in the EWMAEngine name space. 
	 * It is used to increment by a ratio the number of copies of the msg(L). */
	protected static final String ADDITIVE_INCREASE_S =  "additiveIncrease";
	
	/** decrementCopiesRatio -setting id ({@value}) in the EWMAEngine name space. 
	 * It is used to decrement by a ratio the number of copies of the msg(L). */	
	protected static final String MULTIPLICATIVE_DECREASE_S =  "multiplicativeDecrease";
	

	/** metricGenerationInterval -setting id ({@value}) in the control name space. */
	protected static final String METRICS_GENERATION_INTERVAL_S = "metricGenerationInterval";
	
	/** directiveGenerationInterval -setting id ({@value}) in the control name space. */
	protected static final String DIRECTIVE_GENERATION_INTERVAL_S = "directiveGenerationInterval";
	
	/** metric time to live -setting id ({@value}) in the control name space */
	protected static final String METRIC_TTL_S = "metricTtl";

	/** directive time to live -setting id ({@value}) in the control name space */
	protected static final String DIRECTIVE_TTL_S = "directiveTtl";
		
	/** 
	 * maxCopies -setting id ({@value}) in the control name space
	 * Upper limit for the replication level (L). 
	 */
	protected static final String MAXCOPIES_S = "maxCopies";
	
	/** 
	 * minCopies -setting id ({@value}) in the control name space
	 * Low limit allowed for the replication level (L).  
	 */	
	protected static final String MINCOPIES_S = "minCopies";
	
	/** congestionThrMax -setting id ({@value}) in the control name space*/
	private static final String CONGESTION_THRMAX_S = "congestionThrMax";

	/** congestionThrMin -setting id ({@value}) in the control name space*/
	private static final String CONGESTION_THRMIN_S = "congestionThrMin";
	
	/** additive increase default value if it is not specified in the settings 
	 ({@value}) */
	protected static final double DEF_ADDITIVE_INCREASE = 5;
	
	/** decrementCopiesRatio's default value if it is not specified in the settings 
	 ({@value}) */
	protected static final double DEF_MULTIPLICATIVE_DECREASE = 0.25;
	
	/** meanDeviation's default value if it is not specified in the settings ({@value}) */
	protected static final int DEF_MEAN_DEVIATION_FACTOR = 2;
	
	/** metricTTL's default value if it is not specified in the settings ({@value}) */
	protected static final int DEF_METRIC_TTL = 0;
	
	/** directiveTTL's default value if it is not specified in the settings ({@value}) */
	protected static final int DEF_DIRECTIVE_TTL = 0;
	
	/** maxCopies default value if it is not specified in the settings ({@value})  */
	/** This default value implies the setting is not considered. */
	protected static final int DEF_MAXCOPIES = -1;
	
	/** minCopies default value if it is not specified in the settings ({@value})  */
	protected static final int DEF_MINCOPIES = 1;
	
	/** dropsThrMax-setting's default value if it is not specified in the settings 
	 ({@value}) */
	private static final double DEF_CONGESTION_THRMAX = 0.8;
	
	/** dropsThrMin-setting's default value if it is not specified in the settings 
	 ({@value}) */
	private static final double DEF_CONGESTION_THRMIN = 0.5;	
	
	/**
	 * Interval for the metrics generation. If it is not set, it is assumed 
	 * the interval for the directives generation and this value is set to -1.
	 */
	protected int metricGenerationInterval;

	/**
	 * Interval for the directives generation. If it is not set 
	 * this value is set to -1.
	 */
	protected int directiveGenerationInterval;
	
	/**
	 * Metric time to live. After this time the metric is discarded. 
	 */
	protected int metricTTL;
	
	/**
	 * Directive time to live. After this time the directive is discarded. 
	 */
	protected int directiveTTL;	
	
	
	/**
	 * Ratio to increment the number of  copies of a message by.
	 */
	protected double additiveIncrease;
	
	/**
	 * Ratio to decrement the number of  copies of a message by. 
	 */
	protected double multiplicativeDecrease;
	
	/** Property where to load the meanDeviationFactor from the settings */
	protected double meanDeviationFactor;	
	
	/**
	 * Maximum congestion threshold of the congestion window.
	 */
	protected double congestionThrMax;
	
	/**
	 * Minimum congestion threshold of the congestion window.
	 */
	protected double congestionThrMin;	
	
	
	protected MessageRouter router;
		
	/** 
	 * Settings of the namespace: control.engine or null if the engine to 
	 * be used is the default one.  
	 */
	protected Settings settings;
	
	/** Container for the details of the directive */ 
	protected DirectiveDetails directiveDetails;

	/** The allowed maximum number of copies of the message in the network. */
	protected int maxCopies;
	
	/** The minimum number of copies of the message in the network. */
	protected int minCopies;	
	
	/** 
	 * Flag set to true if the engine has received a control message in  a
	 * directive cycle. False otherwise. This flag is reset each directive cycle.
	 */
	protected boolean receivedCtrlMsgInDirectiveCycle = false;
	
	/** 
	 * Dictionary indexed by the id of the host that has generated the message (key).
	 * This table contains the metrics received during a control cycle.  
	 */
	HashMap<Integer, ControlMessage> ctrlCycleMetricHistory = new HashMap<>();
	
	/** 
	 * Dictionary indexed by the id of the host that has generated the message (key).
	 * This table contains the directives received during a control cycle.  
	 */
	HashMap<Integer, ControlMessage> ctrlCycleDirectiveHistory = new HashMap<>();
	
	/**
	 * Initializes the property settings.
	 * @param engineSettings settings corresponding to the namespace control.engine.
	 * which has as a subnamespace the 'DirectiveEngine' namespace.
	 * @param router, the router who has initialized this directiveEngine.
	 */
	public DirectiveEngine(Settings engineSettings, MessageRouter router) {
		this.settings = engineSettings;
		this.router = router;
		this.directiveDetails = new DirectiveDetails();
		
		this.directiveGenerationInterval = (engineSettings.contains(DIRECTIVE_GENERATION_INTERVAL_S)) ? 
				engineSettings.getInt(DIRECTIVE_GENERATION_INTERVAL_S) : 1; 
		this.metricGenerationInterval =  
				(engineSettings.contains(METRICS_GENERATION_INTERVAL_S))
				? engineSettings.getInt(METRICS_GENERATION_INTERVAL_S)
				: this.directiveGenerationInterval;
		this.additiveIncrease = (engineSettings.contains(ADDITIVE_INCREASE_S)) ? 
				engineSettings.getDouble(ADDITIVE_INCREASE_S) : DEF_ADDITIVE_INCREASE;
		this.multiplicativeDecrease = (engineSettings.contains(MULTIPLICATIVE_DECREASE_S)) ? 
				engineSettings.getDouble(MULTIPLICATIVE_DECREASE_S) : DEF_MULTIPLICATIVE_DECREASE;
		this.metricTTL = engineSettings.contains(METRIC_TTL_S) ?
				engineSettings.getInt(METRIC_TTL_S) : DEF_METRIC_TTL;
		this.directiveTTL = engineSettings.contains(DIRECTIVE_TTL_S) ?
				engineSettings.getInt(DIRECTIVE_TTL_S) : DEF_DIRECTIVE_TTL;				
		this.maxCopies = engineSettings.contains(MAXCOPIES_S) ? engineSettings.getInt(MAXCOPIES_S) : 
					DirectiveEngine.DEF_MAXCOPIES;
		this.minCopies = engineSettings.contains(MINCOPIES_S) ? engineSettings.getInt(MINCOPIES_S) : 
			DirectiveEngine.DEF_MINCOPIES;		
		this.congestionThrMax = engineSettings.contains(CONGESTION_THRMAX_S) ? 
				engineSettings.getDouble(CONGESTION_THRMAX_S) : DEF_CONGESTION_THRMAX;
		this.congestionThrMin = engineSettings.contains(CONGESTION_THRMIN_S) ? 
				engineSettings.getDouble(CONGESTION_THRMIN_S) : DEF_CONGESTION_THRMIN;						
	}
	
	/**
	 * Method that resets all the settings that have information related to a 
	 * directive cycle. This method might be overriden by subclasses.
	 */
	protected void resetDirectiveCycleSettings() {
		this.receivedCtrlMsgInDirectiveCycle = false;
		this.ctrlCycleDirectiveHistory.clear();
		this.ctrlCycleMetricHistory.clear();
	}
		
	/**
	 * Method called when a new metric has to be considered for the 
	 * directive to be generated. The metric is aggregated if it has not expired. 
	 * @param metric metric to be considered
	 */
	public abstract void addMetric(ControlMessage metric);
	
	/**
	 * Method called when a directive from another controller can be taken
	 * into account for the generation of the new directive. This directive is 
	 * aggregated if it has not expired.
	 * @param directive other controller directive.
	 */
	public abstract void addDirective(ControlMessage directive);
	
	/**
	 * Method that produces a directive based on the metrics and other 
	 * directives collected. After producing the directive, the method fills the 
	 * empty message, passed as a parameter, with 
	 * the fields of the directive.
	 * @param message the message to be filled with the fields of the 
	 * directive.
	 * @return In case the are no metrics or directives to be considered to 
	 * generate a new directive, the message is not fulfilled and the method 
	 * returns false, otherwise, the message is fulfilled with the directive 
	 * fields and the method returns true.
	 */
	public abstract DirectiveDetails generateDirective(ControlMessage message);
	
	/**
	 * Method that checks the source of the parameter ctrlMsg.
	 * @param ctrlMsg
	 * @return true if the ctrlMsg has been generated by the router 
	 * that has instantiated the controller that has instantiated this engine. 
	 * It returns false otherwise.
	 */
	protected boolean isASelfGeneratedCtrlMsg(ControlMessage ctrlMsg) {
		boolean haveIGeneratedThisMsg = (this.router.relatedWithThisHost(ctrlMsg.getFrom()) == 0); //DEBUG
		//return (this.router.relatedWithThisHost(ctrlMsg.getFrom()) == 0);
		return haveIGeneratedThisMsg;
	}
			
}
