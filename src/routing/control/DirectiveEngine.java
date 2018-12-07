package routing.control;

import core.control.ControlMessage;
import core.control.DirectiveCode;

/**
 * Interface for the Engines that generate directives
 *
 */
public interface DirectiveEngine {
	/**
	 * Method called when a new metric has to be considered for the 
	 * directive to be generated.
	 * @param metric metric to be considered
	 */
	public void addMetric(ControlMessage metric);
	
	/**
	 * Method called when a directive from another controller can be taken
	 * into account for the generation of the new directive
	 * @param directive other controller directive.
	 */
	public void addDirective(ControlMessage directive);
	
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
	public boolean generateDirective(ControlMessage message);
		
	/**
	 * Method that adds an entry to the engine controlProperties map. 
	 * This entry corresponds to a router property to be used by the engine to 
	 * generate a directive. For instance, 
	 * an SprayAndWaitRouter contains the nrof_copies of the message. 
	 * @param code the code of the router property.
	 * @param value The value of this property
	 */
    public void putControlProperty(DirectiveCode code, Double value);
    
	/**
	 * Method that adds all the entries in the ControlProperties passed as a 
	 * parameter to the engine controlProperties map. 
	 * These entries correspond to router properties, to be used by the engine to 
	 * generate a directive. For instance, 
	 * an SprayAndWaitRouter contains the nrof_copies of the message. 
	 * @param code the code of the router property.
	 * @param value The value of this property
	 */
    public void putControlProperties(ControlPropertyMap properties);    

}
