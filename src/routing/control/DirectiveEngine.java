package routing.control;

import core.Settings;
import core.control.ControlMessage;
import report.control.directive.DirectiveDetails;
import routing.MessageRouter;

/**
 * Abstract class for the Engines that generate directives
 *
 */
public abstract class DirectiveEngine {
	

	protected MessageRouter router;
		
	/** 
	 * Settings of the namespace: control.engine or null if the engine to 
	 * be used is the default one.  
	 */
	protected Settings settings;
	
	/** Container for the details of the directive */ 
	protected DirectiveDetails directiveDetails;
	
	/**
	 * Initializes the property settings.
	 * @param settings settings corresponding to the namespace core.engine.
	 * @param router, the router who has initialized this directiveEngine.
	 */
	public DirectiveEngine(Settings settings, MessageRouter router) {
		this.settings = settings;
		this.router = router;
		this.directiveDetails = new DirectiveDetails();
	}
	
	/**
	 * Method called when a new metric has to be considered for the 
	 * directive to be generated.
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
