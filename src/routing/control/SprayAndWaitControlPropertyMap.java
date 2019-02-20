/**
 * 
 */
package routing.control;

import core.control.DirectiveCode;
import routing.SprayAndWaitRouter;

/**
 * Class that has a map that can be fed with the SprayAndWait router properties, 
 * to be used to generate a directive.  
 *
 */
public class SprayAndWaitControlPropertyMap extends ControlPropertyMap {

	/**
	 * Initializes the properties field with the SprayAndWait router properties.
	 */
	public SprayAndWaitControlPropertyMap(SprayAndWaitRouter router) {
		this.put(DirectiveCode.NROF_COPIES_CODE, Double.valueOf(router.getInitialNrofCopies()));
	}

}
