package routing.control;

import core.Message;
import core.Settings;
import core.control.ControlMessage;
import core.control.DirectiveCode;
import report.control.directive.DirectiveDetails;
import routing.MessageRouter;

/**
 * This class implements a controller able to collect network metric messages
 * and generate directives with that information. The generated directives are
 * applied by the routers.
 * 
 */
public class Controller {
	/** namespace of the controller settings ({@value}) */
	public static final String CONTROL_NS = "control";
	/** package where to look for the routing classes*/
	private static final String ROUTING_PACKAGE = "routing";
	private static final String MESSAGE_ROUTER_CLASS = "MessageRouter";
	
	/** package where to look for controller engines */
	private static final String CONTROL_PACKAGE = ROUTING_PACKAGE + "."+ CONTROL_NS;

	/** engine -setting id ({@value}) in the controller name space */
	public static final String ENGINE_S = "engine";
	/** engine -setting id ({@value}) in the controller name space */ 
	public static final String NROF_CONTROLLERS_S = "nrofControllers";	
	/** Default setting value for the engine of a controller */
	public static final String AGGREGATION_ENGINE = "AggregationEngine";	
	/** The settings package full name. */
	private static final String SETTINGS_PACKAGE = "core.Settings";
	/** Engine to be used to generate directives. */
    protected DirectiveEngine directiveEngine;
    /** If there is just one controller in the simulation it is set to true. 
     * True is the default value.*/
    private boolean isACentralizedController = true;

	/**
	 * Constructor that initializes the engine used to generate directives, 
	 * based on the settings configuration and on the router configuration. 
	 * It sets if the controller is a centralized one (case where there is
	 * just one controller).
	 * It initializes the routingProperties with an empty map mean to be 
	 * fed by the routers themselves.  
	 * @param router the router that has created this controller.
	 */
	public Controller(MessageRouter router) {
		// TODO Auto-generated constructor stub
		Settings s = new Settings(CONTROL_NS);
		int nrofControllers;
		this.setDirectiveEngine(s, router);
		
		if(s.contains(NROF_CONTROLLERS_S)){
			nrofControllers = s.getInt(NROF_CONTROLLERS_S);
			s.ensurePositiveValue(nrofControllers, NROF_CONTROLLERS_S);
			this.isACentralizedController = (nrofControllers == 1) ? true : false;
		}
		
	}
		

	
	/**
	 * Method that builds the engine used to generate directives, 
	 * based on the settings configuration of the specified engine. 
	 * @param settings Settings of the name space: controller
	 * @param router the router that has created this controller.

	 */
    protected void setDirectiveEngine(Settings s, MessageRouter router) {
		String engineNameSpace = (s.contains(ENGINE_S)) ? s.getSetting(ENGINE_S) : AGGREGATION_ENGINE;
		Settings engineSettings = new Settings(engineNameSpace);
		String directiveEngine_str = CONTROL_PACKAGE + "." + engineNameSpace;
		String[] directiveEngineConstructorArgumentTypes = {SETTINGS_PACKAGE, 
				ROUTING_PACKAGE +"."+MESSAGE_ROUTER_CLASS} ;
		
		this.directiveEngine= (DirectiveEngine)engineSettings.createInitializedObject(
				directiveEngine_str, directiveEngineConstructorArgumentTypes, 
				new Object[]{engineSettings, router});
    }   
    
    
    /**
     * Getter of the attribute isACentralizedController.
     * @return True if there is just one controller in the scenario. False if 
     * there are more than one.
     */
    public boolean isACentralizedController() {
    	return this.isACentralizedController;
    }
    
    /**
     * Method that injects a metric into the directiveEngine to be considered 
     * when generating the next directive. 
     * @param message the message containing metrics.
     */
    public void addMetric(ControlMessage message) {
    	this.directiveEngine.addMetric(message);
    }
        	
    /**
     * Method that injects a metric into the directiveEngine to be considered 
     * when generating the next directive. 
     * @param message the message containing a directive.
     */    
    public void addDirective(ControlMessage message) {
    	this.directiveEngine.addDirective(message);
    }
    
	/**
	 * Method called by the event generated by the eventGenerator when it 
	 * comes the time to generate a new directive. This method delegates to the 
	 * directiveEngine the production of a new directive. The directiveEngine 
	 * produces a new directive in the case the current network situation, 
	 * mesured by the metrics and directives being injected since the last 
	 * directive was generated, demands a new network configuration.   
	 * @param message Message to be filled with the generated directive's fields.
	 * If no directive is generated by calling this method, the message is not
	 * changed. 
	 * @return If a directive has been generated, returns an object with the 
	 * directive details. It returns Null if the directive has not been generated.  
	 */
	public DirectiveDetails fillMessageWithDirective(Message message) {
		return this.directiveEngine.generateDirective((ControlMessage)message);
    }

}
