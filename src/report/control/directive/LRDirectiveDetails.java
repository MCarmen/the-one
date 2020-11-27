/**
 * 
 */
package report.control.directive;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import core.Message;
import report.control.metric.MetricDetails;
import routing.control.CongestionState;

/**
 * Class with all the information used by an LinearRegressionEngine to generate 
 * a directive. 
 * @author mc
 *
 */
public class LRDirectiveDetails extends DirectiveDetails {

	private static final String HEADER_STR = DirectiveDetails.HEADER_STR + 
			"predictedFor | R2 | slope | CongestionInputs | timeInputs | Using Metrics | Directived received";

	/** List with the congestion inputs (y values) for the regression.  */
	private double[] lrCongestionInputs;
	
	/** List of the times when the {@link #congetionInputs} were calculated. */
	private double[] lrTimeInputs;
	
	/** Measures how well the regression predictions approximate the real data points (R2). */
	private double coeficientOfDetermination;
	/** The slope of the calculated line: line <em>y</em> = &alpha; + &beta; <em>x</em>. */
    private double slope;
    
    /** The calculated congestion is the one predicted for this time */
    private double predictedFor;
    
    /** Map indexed by the interval id having as value the metric generated in this interval*/
    private Map<Integer, MetricDetails> metricDetailPerInterval = new HashMap<>();

	public LRDirectiveDetails() {
		super();		
	}
    
	public LRDirectiveDetails(LRDirectiveDetails directiveDetails) {
		super(directiveDetails);
		this.coeficientOfDetermination = directiveDetails.coeficientOfDetermination;
		this.slope = directiveDetails.slope;
		this.lrCongestionInputs = directiveDetails.lrCongestionInputs;
		this.lrTimeInputs = directiveDetails.lrTimeInputs;
		this.predictedFor = directiveDetails.predictedFor;		
		this.metricDetailPerInterval = new HashMap<>(directiveDetails.metricDetailPerInterval);
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
			double coeficientOfDetermination, double slope, double predictedFor) {
		super.init(m, lastCtrlCycleNrofCopies, calculatedCongestion, congestionState);
		this.coeficientOfDetermination = coeficientOfDetermination;
		this.slope = slope;
		this.lrCongestionInputs = lrCongestionInputs;
		this.lrTimeInputs = lrTimeInputs;
		this.predictedFor = predictedFor;
	}
	
	/**
	 * Method that registers the metric that has been generated in the controller
	 * in a time interval out of the received metrics during this time interval.
	 * 
	 * @param metric                   The generated metric after a time interval.
	 * @param currentCongestionAverage The congestion measurement aggregated until
	 *                                 now.
	 * @param newCongestionAverage     The congestion measurement after the metric
	 *                                 passed as a parameter is aggregated.
	 * @param aggregationIntervalCounter in Which aggregation interval this metric
	 * has been aggregated.
	 * 
	 */
	public void addMetricUsed(MetricDetails metricDetails, int aggregationIntervalCounter) {		
		this.metricDetailPerInterval.put(aggregationIntervalCounter, metricDetails);
	}
	

	
	/* (non-Javadoc)
	 * @see report.control.directive.DirectiveDetails#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s %.2f %.2f %.2f %s %s %s %s", this.basicFieldsToString(),
				this.predictedFor,
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
	
	@Override
	protected String metricsUsedToString() {
		String metricsDetailsStr = "";
		
		for(Entry<Integer, MetricDetails> entry: this.metricDetailPerInterval.entrySet()) {
			metricsDetailsStr += String.format(" %d: %s", entry.getKey(), entry.getValue());
		}
		
		return metricsDetailsStr;
	}
	
	@Override
	public void reset() {
		super.reset();
		this.metricDetailPerInterval.clear();
	}

}
