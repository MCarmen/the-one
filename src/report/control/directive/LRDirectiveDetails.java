/**
 * 
 */
package report.control.directive;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Properties;

import core.Message;
import core.control.ControlMessage;
import routing.control.CongestionState;

/**
 * Class with all the information used by an LinearRegressionEngine to generate 
 * a directive. 
 * @author mc
 *
 */
public class LRDirectiveDetails extends DirectiveDetails {

	private static final String HEADER_STR = DirectiveDetails.HEADER_STR + 
			"| R2 | slope | CongestionInputs | timeInputs | Using Metrics | Directived received";

	/** List with the congestion inputs (y values) for the regression.  */
	private double[] lrCongestionInputs;
	
	/** List of the times when the {@link #congetionInputs} were calculated. */
	private double[] lrTimeInputs;
	
	/** Measures how well the regression predictions approximate the real data points (R2). */
	private double coeficientOfDetermination;
	/** The slope of the calculated line: line <em>y</em> = &alpha; + &beta; <em>x</em>. */
    private double slope;

	public LRDirectiveDetails() {
		super();
	}
    
	public LRDirectiveDetails(LRDirectiveDetails directiveDetails) {
		super(directiveDetails);
		this.coeficientOfDetermination = directiveDetails.coeficientOfDetermination;
		this.slope = directiveDetails.slope;
		this.lrCongestionInputs = directiveDetails.lrCongestionInputs;
		this.lrTimeInputs = directiveDetails.lrTimeInputs;
		
	}
	
	/** 
	 * Method to be invoked when the directive has been created and therefore
	 * the message has been set with the directive fields.
	 * These fields are used to initialize this object.   
	 * @param m the message filled with the directive fields.
	 * @param lastCtrlCycleNrofCopies the number of copies calculated in the 
	 * previous control cycle. If this method is called in the first control 
	 * cycle this parameter should be set to -1.
	 * @param calculatedCongestion The current calculated congestion value. 
	 * @param congestionState the congestionState used to calculate 
	 * the calculatedCongestion. 
	 * @param lrCongestionInputs An array with the calculated congestion inputs
	 * for the regression.
	 * @param lrTimeInputs An array with the time when each congestionInput was
	 * generated.
	 * @param coeficientOfDetermination a measure of how good 
	 * the regression predictions approximate the real data points (R2). It's 
	 * value is between 0 and 1.
	 * @param slope the regression line slope. 
	 */	
	public void init(Message m, int lastCtrlCycleNrofCopies, 
			double calculatedCongestion, CongestionState congestionState, 
			double[] lrCongestionInputs, double[] lrTimeInputs,
			double coeficientOfDetermination, double slope) {
		super.init(m, lastCtrlCycleNrofCopies, calculatedCongestion, congestionState);
		this.coeficientOfDetermination = coeficientOfDetermination;
		this.slope = slope;
		this.lrCongestionInputs = lrCongestionInputs;
		this.lrTimeInputs = lrTimeInputs;
	}
	
	/**
	 * Method that registers the metric that has been aggregated in the controller
	 * 
	 * @param metric                   The received metric about to be aggregated
	 * @param currentCongestionAverage The congestion measurement aggregated until
	 *                                 now.
	 * @param newCongestionAverage     The congestion measurement after the metric
	 *                                 passed as a parameter is aggregated.
	 * @param aggregationIntervalCounter in Which aggregation interval this metric
	 * has been aggregated.
	 * 
	 */
	public void addMetricUsed(ControlMessage metric, double currentCongestionAverage, double newCongestionAverage,
			int aggregationIntervalCounter) {
		Properties metricProperties = new Properties();
		super.addMetricUsed(metric, metricProperties);

		metricProperties.put("CongAvg", new DecimalFormat("#0.00").format(currentCongestionAverage));
		metricProperties.put("NewCongAvg", new DecimalFormat("#0.00").format(newCongestionAverage));
		metricProperties.put("intervalCount", aggregationIntervalCounter);
	}
	
	/* (non-Javadoc)
	 * @see report.control.directive.DirectiveDetails#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s %.2f %.2f %s %s %s %s", this.basicFieldsToString(),				
				this.coeficientOfDetermination, this.slope,	
				Arrays.toString(this.lrCongestionInputs), Arrays.toString(this.lrTimeInputs),
				this.metricsUsedToString(), this.directivesUsed);
	}

	/* (non-Javadoc)
	 * @see report.control.directive.DirectiveDetails#getHeaderString()
	 */
	@Override
	public String getHeaderString() {
		// TODO Auto-generated method stub
		return LRDirectiveDetails.HEADER_STR;
	}

}
