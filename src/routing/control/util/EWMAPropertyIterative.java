package routing.control.util;

/**
 * Support class encapsulating an smoothed property calculated through an 
 * EWMA function that needs to be applied iterations times. 
 * @author mc
 *
 */
public class EWMAPropertyIterative extends EWMAProperty{
	
	public EWMAPropertyIterative(double alpha) {
		super(alpha);
	}

	public EWMAPropertyIterative(double sProperty, double alpha) {
		super(sProperty, alpha);
	}
	
	
	/**
	 * Function that applies an EWMA function to a property several times.
	 * The new value is smoothed with the already smoothed previous value 
	 * (sProperty).
	 * The EWMA function for smoothing the property is:
	 * sPoperty = alpha^iterations * sPoperty - alpha^iterations * property_messured 
	 * + property_messured.
	 * This function is equivalent to have applied iterations times the classical
	 * EWMA function:
	 * sPoperty = (1-alpha) * sPoperty + alpha * property_messured.
	 * @param newValue new measure to be smoothed.
	 * @param iterations The number of times the EWMA function needs to be applied.
	 */
	public void aggregateValue(double newValue, int iterations) {
		double alphaPoweredToIterations = Math.pow(this.alpha, iterations);
		this.sProperty = this.isSet() ? alphaPoweredToIterations * this.sProperty 
				- alphaPoweredToIterations * newValue + newValue 
				: newValue;	
	}
	
	/**
	 * Method that aggregates the secondaryValue to the mainValue using an EWMA
	 * iterations times.
	 * @param mainValue The value with most weight. It is applied the 
	 * multiplying factor of (1-alpha).
	 * @param secondaryValue the value to be aggregated to the mainValue
	 * @param alpha the alpha to use for the EWMA aggregation.
	 * @param iterations The number of times the EWMA function needs to be applied.
	 * @return The result of applying the formula:
	 * alpha^iterations * mainValue - alpha^iterations * secondaryValue 
	 * + secondaryValue.
	 * This function is equivalent to have applied iterations times the classical
	 * EWMA function:
	 * (1-alpha) * mainValue + alpha * secondaryValue.
	 */
	public static double aggregateValue(double mainValue, double secondaryValue, double alpha, int iterations) {
		double alphaPoweredToIterations = Math.pow(alpha, iterations);		
		return (alphaPoweredToIterations * mainValue 
				- alphaPoweredToIterations * secondaryValue + secondaryValue );
	}
	

}
