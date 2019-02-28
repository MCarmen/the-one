package routing.control;

import core.Settings;
import core.control.ControlMessage;
import core.control.DirectiveCode;
import report.control.directive.DirectiveDetails;

/**
 * Abstract class for the Engines that generate directives
 *
 */
public abstract class DirectiveEngine {
	
	/** Control configuration the directive will be based on. For instance, 
	 * the control configuration could contain the nrof_copies of the routing 
	 * algorithm. The directive could be nrof_copies/2. */
	protected RoutingPropertyMap routingProperties;
	
	/**
	 * Current values for the control properties the directives are modifying.
	 */
	protected RoutingPropertyMap currentValueForRoutingProperties;
	
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
	 */
	public DirectiveEngine(Settings settings) {
		this.settings = settings;
		this.routingProperties = new RoutingPropertyMap();
		this.currentValueForRoutingProperties = new RoutingPropertyMap();
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
		
	/**
	 * Method that adds an entry to the engine routerProperties map. 
	 * This entry corresponds to a router property to be used by the engine to 
	 * generate a directive. For instance, 
	 * an SprayAndWaitRouter contains the nrof_copies of the message. 
	 * @param code the code of the router property.
	 * @param value The value of this property
	 */
    public void putRoutingProperty(DirectiveCode code, Double value) {
    	this.routingProperties.put(code, value);
    }
	/**
	 * Method that adds all the entries in the routerProperties passed as a 
	 * parameter to the engine routerProperties map. 
	 * These entries correspond to router properties, to be used by the engine to 
	 * generate a directive. For instance, 
	 * an SprayAndWaitRouter contains the nrof_copies of the message. 
	 * @param code the code of the router property.
	 * @param value The value of this property
	 */
	public void putRoutingProperties(RoutingPropertyMap properties) {
		this.routingProperties.putAll(properties);		
	}
	
	/**
	 * Returns the value of the routerProperty passed as a parameter from 
	 * the currentValueForRouterProperties map. If there is no entry for this
	 * property, an entry is set in the currentValueForRouterProperty map with 
	 * the initial value of the property got from the routerPropertyMap. 
	 * @param code the property which current value we want to get.
	 * @return the current value for the property or its original value.
	 */
    protected Double getCurrentRouterPropertyValue(DirectiveCode code) {
    	if(!this.currentValueForRoutingProperties.containsKey(code)) {
    		this.currentValueForRoutingProperties.put(code, 
    				this.routingProperties.get(code));
    	}
    	return this.currentValueForRoutingProperties.get(code); 	
    }

}
