/**
 * 
 */
package report.control.directive;

import java.text.DecimalFormat;
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
			" | R2 | slope | Using Metrics | Directived received";

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
	 * @param coeficientOfDetermination a measure of how good 
	 * the regression predictions approximate the real data points (R2). It's 
	 * value is between 0 and 1.
	 * @param slope the regression line slope. 
	 */	
	public void init(Message m, int lastCtrlCycleNrofCopies, 
			double calculatedCongestion, CongestionState congestionState, 
			double coeficientOfDetermination, double slope) {
		super.init(m, lastCtrlCycleNrofCopies, calculatedCongestion, congestionState);
		this.coeficientOfDetermination = coeficientOfDetermination;
		this.slope = slope;
	}
	
	
	/* (non-Javadoc)
	 * @see report.control.directive.DirectiveDetails#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s %.2f %.2 %s %s", this.basicFieldsToString(),
				this.coeficientOfDetermination, this.slope,	
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
