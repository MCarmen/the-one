/**
 * 
 */
package report.control.directive;

import java.text.DecimalFormat;
import java.util.Properties;

import core.Message;
import core.control.ControlMessage;
import core.control.DirectiveCode;
import core.control.MetricCode;
import routing.control.CongestionState;
import routing.control.metric.CongestionMetric;

/**
 * Class with all the information used by an EWMADirective engine to generate a directive.
 * @author mc
 *
 */
public class EWMADirectiveDetails extends DirectiveDetails {
	/** Accumulated soften nrofMsgCopies average from the received directives. */
	private double nrofMsgCopiesAverage;
	
	private static final String HEADER_STR = DirectiveDetails.HEADER_STR + "directivesAvg | " + 
		"Using Metrics | Using Directives | ";
	
	public EWMADirectiveDetails() {
		super();
	}
	
	public EWMADirectiveDetails(EWMADirectiveDetails directiveDetails) {
		super(directiveDetails);
		this.nrofMsgCopiesAverage = directiveDetails.nrofMsgCopiesAverage;
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
	 * @param nrofMsgCopiesAverage the average of the number of copies encapsulated in the 
	 * received directives aggregated to the number of copies calculated by the controller. 
	 * @param congestionState the congestionState after generating the directive. 
	 */	
	public void init(Message m, int lastCtrlCycleNrofCopies, 
			double calculatedCongestion, CongestionState congestionState, double nrofMsgCopiesAverage) {
		super.init(m, lastCtrlCycleNrofCopies, calculatedCongestion, congestionState);
		this.nrofMsgCopiesAverage = nrofMsgCopiesAverage;

	}
	
	/**
	 * Adds the details of the considered directive.
	 * @param directive The received directive.
	 * @param currentAggregatedDirectivesAvg. The mobile average of the already received directives.
	 * @param directiveSensed. The field nrOfCopies of the directive message.
	 * @param newDirecivesAvg. The mobile average of the received directives including 
	 * the one passed as a parameter. 
	 */
	public void addDirectiveUsed(ControlMessage directive, double currentAggregatedDirectivesAvg,
			double directiveSensed, double newDirecivesAvg) {
		Properties directiveProperties = new Properties();
		
		super.addDirectiveUsed(directive, directiveProperties);
		
		directiveProperties.put("dirAvg", new DecimalFormat("#0.00").format(currentAggregatedDirectivesAvg));
		directiveProperties.put("newDirAvg", new DecimalFormat("#0.00").format(newDirecivesAvg));
		
	}
	
	/**
	 * Method that registers the metric that has been aggregated in the controller
	 * affecting the current directive to be generated.
	 * @param metric The received metric about to be aggregated
	 * @param currentCongestionAverage The congestion measurement aggregated until now.
	 * @param newCongestionAverage The congestion measurement after the metric passed as 
	 * a parameter is aggregated.  
	 */
	public void addMetricUsed(ControlMessage metric,
			double currentCongestionAverage, double newCongestionAverage 
			) {
		Properties metricProperties = new Properties();
		super.addMetricUsed(metric, metricProperties);

		metricProperties.put("CongAvg" , new DecimalFormat("#0.00").format(currentCongestionAverage));
		metricProperties.put("NewCongAvg" , new DecimalFormat("#0.00").format(newCongestionAverage));
	}
	
	@Override
	public String toString() {
		return String.format("%s %.2f %s %s", this.basicFieldsToString(), this.nrofMsgCopiesAverage, 				 
				this.metricsUsedToString(), this.directivesUsed);
	}
	
	/**
	 * Returns the name of the properties used in the toString method. It can 
	 * be used by a report using the {@ link #toString()} method as a header.
	 * @return
	 */
	public String getHeaderString() {
		return EWMADirectiveDetails.HEADER_STR; 
	}	
}
