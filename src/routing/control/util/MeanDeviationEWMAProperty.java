package routing.control.util;

public class MeanDeviationEWMAProperty  {
	private EWMAProperty ewmaProperty;
	
	
	/**
	 * Constructor that sets the initial value of the property to be smoothed 
	 * with the EWMA function. It also sets the alpha constant used in the EWMA
	 * function with the settings value or with a default value. 
	 * @param sProperty the property to be smoothed.
	 * @param alpha the alpha used in the EWMA function.
	 */
	public MeanDeviationEWMAProperty(double sProperty, double alpha) {
		this.ewmaProperty = new EWMAProperty(sProperty, alpha);
	}
	
	/**
	 * Constructor that sets the sProperty to a certain value used to indicate 
	 * that the sProperty has not been set yet.
	 * @param alpha the alpha used in the EWMA function. 
	 */
	public MeanDeviationEWMAProperty(double alpha) {
		this.ewmaProperty = new EWMAProperty(alpha);
	}
	
	public double getValue() {
		return this.ewmaProperty.getValue();
	}
	
	public boolean isSet() {
		return  this.ewmaProperty.isSet();
	}
	
	/**
	 * Function that applies an EWMA function to smoothe the deviation of a 
	 * measure to a mean.
	 * The new value is smoothed with the already smoothed previous value 
	 * (sProperty).
	 * The EWUMA function for smoothing the property is:
	 * sPoperty = (1-alpha) * sPoperty + alpha * property_messured.
	 * @param newValue new measure to calculate the deviation to the mean.
	 * @param the smoothed mean.
	 */
	public void aggregateValue(double measure, EWMAProperty sMean) {
		double mean = sMean.isSet() ?  sMean.getValue() : 0;
		this.ewmaProperty.aggregateValue(Math.abs(measure - mean));
	}
	
	
}
