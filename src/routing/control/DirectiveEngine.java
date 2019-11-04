package routing.control;

import core.Settings;
import core.SimClock;
import core.control.ControlMessage;
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
	protected static final String INCREMENT_COPIES_RATIO_S =  "incrementCopiesRatio";
	
	/** decrementCopiesRatio -setting id ({@value}) in the EWMAEngine name space. 
	 * It is used to decrement by a ratio the number of copies of the msg(L). */	
	protected static final String DECREMENT_COPIES_RATIO_S =  "decrementCopiesRatio";
	
	/** meanDeviationFactor -setting id ({@value}) in the control name space. */
	protected static final String MEAN_DEVIATION_FACTOR_S = "meanDeviationFactor";	

	/** metricGenerationInterval -setting id ({@value}) in the control name space. */
	protected static final String METRICS_GENERATION_INTERVAL_S = "metricGenerationInterval";
	
	/** directiveGenerationInterval -setting id ({@value}) in the control name space. */
	protected static final String DIRECTIVE_GENERATION_INTERVAL_S = "directiveGenerationInterval";
	
	/** directiveGenerationInterval -setting id ({@value}) in the control name space */
	protected static final String METRIC_EXPIRED_WINDOW_FACTOR_S = "metricExpiredWindowFactor";
		
	/** maxCopies -setting id ({@value}) in the control name space */
	protected static final String MAXCOPIES_S = "maxCopies";		
	
	/** incrementCopiesRatio's default value if it is not specified in the settings 
	 ({@value}) */
	protected static final double DEF_INCREMENT_COPIES_RATIO = 0.25;
	
	/** decrementCopiesRatio's default value if it is not specified in the settings 
	 ({@value}) */
	protected static final double DEF_DECREMENT_COPIES_RATIO = 0.25;
	
	/** meanDeviation's default value if it is not specified in the settings ({@value}) */
	protected static final int DEF_MEAN_DEVIATION_FACTOR = 2;
	
	/** metricExpiredWindowFactor's default value if it is not specified in the settings ({@value}) */
	protected static final int DEF_METRIC_EXPIRED_WINDOW_FACTOR = 0;

	
	/** maxCopies default value if it is not specified in the settings ({@value})  */
	/** This default value implies the setting is not considered. */
	protected static final int DEF_MAXCOPIES = -1;	
	
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
	 * Factor (n_times) of the directiveGenerationInterval seconds while the 
	 * received metric is accepted. 
	 */
	protected int metricExpiredWindowFactor;
	
	/**
	 * Ratio to increment the number of  copies of a message by.
	 */
	protected double incrementCopiesRatio;
	
	/**
	 * Ratio to decrement the number of  copies of a message by. 
	 */
	protected double decrementCopiesRatio;
	
	/** Property where to load the meanDeviationFactor from the settings */
	protected double meanDeviationFactor;	
	
	
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
	
	/**
	 * Initializes the property settings.
	 * @param engineSettings settings corresponding to the namespace control.engine.
	 * which has as a subnamespace the control namespace.
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
		this.incrementCopiesRatio = (engineSettings.contains(INCREMENT_COPIES_RATIO_S)) ? 
				engineSettings.getDouble(INCREMENT_COPIES_RATIO_S) : DEF_INCREMENT_COPIES_RATIO;
		this.decrementCopiesRatio = (engineSettings.contains(DECREMENT_COPIES_RATIO_S)) ? 
				engineSettings.getDouble(DECREMENT_COPIES_RATIO_S) : DEF_DECREMENT_COPIES_RATIO;
		this.meanDeviationFactor = (engineSettings.contains(MEAN_DEVIATION_FACTOR_S)) ?
				engineSettings.getDouble(MEAN_DEVIATION_FACTOR_S) : DEF_MEAN_DEVIATION_FACTOR;
		this.metricExpiredWindowFactor = engineSettings.contains(METRIC_EXPIRED_WINDOW_FACTOR_S) ?
				engineSettings.getInt(METRIC_EXPIRED_WINDOW_FACTOR_S) : DEF_METRIC_EXPIRED_WINDOW_FACTOR;
		this.maxCopies = engineSettings.contains(MAXCOPIES_S) ? engineSettings.getInt(MAXCOPIES_S) : 
					DirectiveEngine.DEF_MAXCOPIES;				
	}
	
	/**
	 * Checks wether the metric has expired. A metric has expired when 
	 * the simulation current time - metric's creation time > n_times the
	 * directiveGenerationInterval (where n_times = 
	 * control.metricExpiredWindowFactor)
	 * @param metric
	 * @return
	 */
	protected boolean hasMetricExpired(MetricMessage metric) {
		boolean hasMetricExpired = ((this.metricExpiredWindowFactor!=0) && 
				(SimClock.getTime() - metric.getCreationTime() > 
				this.metricExpiredWindowFactor * this.directiveGenerationInterval) ) ?
				true : false;		
		return  hasMetricExpired; 
	}
	
	/**
	 * Method called when a new metric has to be considered for the 
	 * directive to be generated. The metric is added if it is not expired. 
	 * @param metric metric to be considered
	 */
	public abstract void addMetric(ControlMessage metric);
	
	/**
	 * Method called when a directive from another controller can be taken
	 * into account for the generation of the new directive
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
			
}
