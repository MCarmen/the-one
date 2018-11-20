package routing.control;

import core.Settings;
import routing.MessageRouter;

/**
 * This class implements a controller able to collect network metric messages 
 * and generate directives with that information. 
 * The generated directives are applied by the routers.
 * 
 */
public class Controller {
	/** namespace of the controller settings ({@value})*/
	public static final String CONTROLLER_NS = "controller";
	/** host type -setting id ({@value}) in the Group name space*/
	public static final String HOST_TYPE_S = "type";
	/** Default setting value for type specifying the type of a group
	 * (controller or host)*/
	public static final String HOST_TYPE = "host";
	

	/**
	 * Constructor that initializes the engine used to generate directives,
	 * based on the settings configuration and on the router configuration.
	 * @param settings Settings of the name space: GROUP_NS and GROUP_NS+i 
	 */
	public Controller(Settings settings, MessageRouter router) {
		// TODO Auto-generated constructor stub
		
		settings.contains(name)
		
	}

}
