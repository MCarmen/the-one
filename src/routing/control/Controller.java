package routing.control;

import java.util.HashMap;
import java.util.Map;

import core.Message;
import core.Settings;
import core.control.ControlMessage;
import core.control.DirectiveCode;
import routing.MessageRouter;
import routing.SprayAndWaitRouter;

/**
 * This class implements a controller able to collect network metric messages
 * and generate directives with that information. The generated directives are
 * applied by the routers.
 * 
 */
public class Controller {
	/** namespace of the controller settings ({@value}) */
	public static final String CONTROL_NS = "control";
	/** package where to look for controller engines */
	private static final String CONTROL_PACKAGE = "routing.control";

	/** engine -setting id ({@value}) in the controller name space */
	public static final String ENGINE_S = "engine";
	/** engine -setting id ({@value}) in the controller name space */ 
	public static final String NROF_CONTROLLERS_S = "nrofControllers";	
	/** Default setting value for the engine of a controller */
	public static final String AGGREGATION_ENGINE = "AggregationEngine";
	/** Engine to be used to generate directives. */
    protected DirectiveEngine directiveEngine;
    /** If there is just one controller in the simulation it is set to true. 
     * True is the default value.*/
    private boolean isACentralizedController = true;

	/**
	 * Constructor that initializes the engine used to generate directives, 
	 * based on the settings configuration and on the router configuration.
	 * It also sets if the controller is a centralized one (case where there is
	 * just one controller). 
	 * @param settings Settings of the name space: GROUP_NS and GROUP_NS+i
	 */
	public Controller(Settings settings, MessageRouter router) {
		// TODO Auto-generated constructor stub
		Settings s = new Settings(CONTROL_NS);
		int nrofControllers;
		this.initializeDirectiveEngine(s, router);
		
		if(s.contains(NROF_CONTROLLERS_S)){
			nrofControllers = s.getInt(NROF_CONTROLLERS_S);
			s.ensurePositiveValue(nrofControllers, NROF_CONTROLLERS_S);
			this.isACentralizedController = (nrofControllers == 1) ? true : false;
		}
		
	}
	
	/**
	 * Factory method that builds a map with information from the router, to be 
	 * used by the engine to generate a directive. For instance, 
	 * an SprayAndWaitRouter contains the nrof_copies of the message. 
	 * The directive could be nrof_copies/2.
	 * @param router the router.
	 * @return A map with the router settings to be used to generate a 
	 * directive. If there is no setting to be adjusted by a directive the map
	 * will be empty.  
	 */
	protected static Map<DirectiveCode, Double> getControlConfiguration(MessageRouter router){
		Map<DirectiveCode, Double> controlConfiguration = 
				new HashMap<DirectiveCode, Double>();
		
		if (router instanceof SprayAndWaitRouter) { 
			controlConfiguration.put(DirectiveCode.NROF_COPIES_CODE, 
					Double.valueOf(((SprayAndWaitRouter)router).getInitialNrofCopies()));
		}
		
		return controlConfiguration;
	}
	
	/**
	 * Method that initializes the engine used to generate directives, 
	 * based on the settings configuration and on the router configuration. 
	 * @param settings Settings of the name space: controller
	 * @param router
	 */
    protected void initializeDirectiveEngine(Settings s, MessageRouter router) {
		String directiveEngine_str = CONTROL_PACKAGE + ".";
		
		directiveEngine_str += (s.contains(ENGINE_S)) ? s.getSetting(ENGINE_S) : AGGREGATION_ENGINE;
		String[] argsName = {"java.util.Map"};
		Object[] args = {Controller.getControlConfiguration(router)};
		
		this.directiveEngine= (DirectiveEngine)s.createInitializedObject(
				directiveEngine_str, argsName, args);
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
	 * @return True if the message has been updated with the fields of a new 
	 * generated directive. False otherwise.
	 *  
	 */
	public boolean fillMessageWithDirective(Message message) {
		return this.directiveEngine.generateDirective((ControlMessage)message);
    }

}
