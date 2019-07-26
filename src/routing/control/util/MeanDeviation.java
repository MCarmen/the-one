package routing.control.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to calculate the mean deviation of a collection of values from a mean. 
 * @author mc
 *
 */
public class MeanDeviation {
	/**
	 * List of values used to calculate their deviation regarding to a mean value.
	 */
	private List<Double> measures = new ArrayList<>();
	
	
	public void addMeasure(double measure) {
		this.measures.add(measure);
	}
	
	public void addMeasures(List<Double> measures) {
		this.measures.addAll(measures);
	}
	
	/**
	 * Calculates the mean deviation of the measures to the mean value passed 
	 * as a parameter.
	 * @param ewmaProperty the mean value which has been calculated through an
	 * ewma.
	 * @return the mean deviation.
	 */
	public double getMeanDeviation(EWMAProperty ewmaProperty) {
		double mean = (ewmaProperty.isSet()) ? ewmaProperty.getValue() : 0;
		return this.getMeanDeviation(mean);
	}
	
	/**
	 * Calculates the mean deviation of the measures to the mean value passed 
	 * as a parameter.
	 * @param mean the mean value
	 * @return the mean deviation.
	 */
	public double getMeanDeviation(double mean) {
		double accumulatedDeviation = 0;
		
		for(double measure : this.measures) {
			accumulatedDeviation += Math.abs(measure - mean);
		}
		return (accumulatedDeviation/this.measures.size());
	}
}
