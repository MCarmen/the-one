package routing.control;

import core.Settings;

public class EWMAProperty{
	
	private static final double NOT_SET_VALUE = System.currentTimeMillis();
    
	/** Average of the propety value smoothed by the application of the EWMA 
	 * function after a call to the method aggregateValue(newValue) */
	private double sProperty; 

	/** Alfa constant used in the EWMA function. */
    private double alpha;
        	
	/**
	 * Constructor that sets the initial value of the property to be smoothed 
	 * with the EWMA function. It also sets the alpha constant used in the EWMA
	 * function with the settings value or with a default value. 
	 * @param sProperty the property to be smoothed.
	 * @param alpha the alpha used in the EWMA function.
	 */
	public EWMAProperty(double sProperty, double alpha) {
		this.sProperty = sProperty;
		this.alpha = alpha;		
	}
	
	/**
	 * Constructor that sets the sProperty to a certain value used to indicate 
	 * that the sProperty has not been set yet.
	 * @param alpha the alpha used in the EWMA function. 
	 */
	public EWMAProperty(double alpha) {
		this(EWMAProperty.NOT_SET_VALUE, alpha);
	}	
	
	public double getValue() {
		return sProperty;
	}
	
	public boolean isSet() {
		return  !(this.sProperty == EWMAProperty.NOT_SET_VALUE);
	}

	/**
	 * Function that applies an EWMA function to a property.
	 * The new value is smoothed with the already smoothed previous value 
	 * (sProperty).
	 * The EWUMA function for smoothing the property is:
	 * sPoperty = (1-alpha) * sPoperty + alpha * property_messured.
	 * @param newValue new reading of the drops.
	 */
	public void aggregateValue(double newValue) {
		this.sProperty = (this.sProperty == EWMAProperty.NOT_SET_VALUE) ? newValue
				: (1 - this.alpha) * this.sProperty + this.alpha * newValue;
	}
}
