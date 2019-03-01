/**
 * 
 */
package routing.control;

import routing.SprayAndWaitRouter;

/**
 * Class that has a map that can be fed with the SprayAndWait router properties, 
 * to be used to generate a directive.  
 *
 */
public class SprayAndWaitRoutingPropertyMap extends RoutingPropertyMap {
	public final static String MSG_COUNT_PROPERTY = SprayAndWaitRouter.MSG_COUNT_PROPERTY;
	/**
	 * Puts the properties to be used by the controller in a Map.
	 */
	public SprayAndWaitRoutingPropertyMap(SprayAndWaitRouter router) {
		this.put(MSG_COUNT_PROPERTY, Integer.valueOf(router.getInitialNrofCopies()));
	}

}
