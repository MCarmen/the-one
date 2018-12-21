package routing.control;

import java.util.Hashtable;
import java.util.Map;

import core.control.DirectiveCode;

/**
 * Class that has a map that can be fed with the router properties to be 
 * used to generate a directive.  
 *
 */
public class ControlPropertyMap {

	/** Map holding the router properties used to generate a directive.*/ 
	private Map<DirectiveCode, Double> properties;
	
	/**
	 * Creates an empty Map for the router properties.
	 */
	public ControlPropertyMap() {
		this.properties = new Hashtable<>();
	}
	
	/**
	 * Method that puts a property in the properties map.
	 * @param property the property to be added as a key
	 * @param value the value of the property.
	 */
	public void putProperty(DirectiveCode property, Double value) {
		this.properties.put(property, value);
	}
	
	/**
	 * Method that adds the properties in the ControlProperties map passed as a 
	 * parameter to the class map.
	 * @param properties the properties to be added to the class map.
	 */
	public void putProperties(ControlPropertyMap properties) {
		this.properties.putAll(properties.properties);
	}
	
	/**
	 * Method that returns the value of an entry in the map.
	 * @param code the key of the map entry.
	 * @return the value of the key. 
	 */
	public Double getProperty(DirectiveCode code) {
		return this.properties.get(code);
	}

}
