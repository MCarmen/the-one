package report.control.directive;

import java.text.DecimalFormat;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import core.Message;
import core.control.ControlMessage;
import core.control.DirectiveCode;
import core.control.MetricCode;
import routing.control.CongestionState;
import routing.control.metric.CongestionMetricPerWT;

/**
 * Class with all the information used by a directive engine to generate a directive.
 * @author mc
 *
 */
public abstract class DirectiveDetails {
	
	/** Directive Identifier. */
	protected String directiveID;
	
	/** Identifier of the node that generated the directive.  */
	protected String generatedByNode;
		
	/** Value of the field containing the number of copies.  */
	protected int newNrofCopies;

	/** Value of the field containing the number of copies in the previous control
	 * cycle.  */
	protected int lastCtrlCycleNrofCopies;
	
	/** List of the identifiers of the aggregated directives used to 
	 * generate  this one.*/
	protected List<Properties> directivesUsed;
	
	/** A list with the metrics used  */
	protected List<Properties> metricsUsed;
	
	/** When the directive was created */
	protected int creationTime;
	
	/** The congestion state under which this directive was generated. */
	protected CongestionState congestionState;
		
	/** The congestion calculated by a directive engine  */
	protected double calculatedCongestion; 
	
	protected static final String HEADER_STR = "dirID | creation Time | Generated By | "+
			"prev_nrofCopies | new_nrofCopies | calculatedCongestion | CongestionState | ";
	
	/**
	 * Constructor that initializes the list of the ids of the directives used
	 * to generate this directive.
	 */
	public DirectiveDetails() {
		this.directivesUsed = new ArrayList<>();
		this.metricsUsed = new ArrayList<>();
	}
	
	/**
	 * Copy constructor.
	 * @param directiveDetails the directiveDetails object to be copied.
	 */
	public DirectiveDetails(DirectiveDetails directiveDetails) {
		this.directiveID = directiveDetails.directiveID;
		this.generatedByNode = directiveDetails.generatedByNode;	
		this.newNrofCopies = directiveDetails.newNrofCopies;
		this.lastCtrlCycleNrofCopies = directiveDetails.lastCtrlCycleNrofCopies;
		this.directivesUsed = new ArrayList<>(directiveDetails.directivesUsed);
		this.metricsUsed = new ArrayList<>(directiveDetails.metricsUsed);
		this.creationTime = directiveDetails.creationTime;
		this.calculatedCongestion = directiveDetails.calculatedCongestion;
		this.congestionState = directiveDetails.congestionState;
	}
	

	
	/** 
	 * Method to be invoked when the directive has been created and therefore
	 * the message has been set with the directive fields.
	 * These fields are used to initialize this object.   
	 * @param m the message filled with the directive fields.
	 * @param lastCtrlCycleNrofCopies the number of copies calculated in the 
	 * previous control cycle. If this method is called in the first control 
	 * cycle this parameter should be set to -1.
	 * @param calculatedCongestion The current calculated congestion value.. 
	 * @param congestionState the congestionState after generating the directive. 
	 */	
	public void init(Message m, int lastCtrlCycleNrofCopies, 
		double calculatedCongestion, CongestionState congestionState) {
		this.directiveID = m.getId();
		this.generatedByNode = m.getFrom().toString();
		this.creationTime = (int)m.getCreationTime();
		if (m.containsProperty​(DirectiveCode.NROF_COPIES_CODE)) {
			this.newNrofCopies = (int)m.getProperty(DirectiveCode.NROF_COPIES_CODE);
		}
		this.calculatedCongestion = calculatedCongestion;
		this.congestionState = congestionState;
		this.lastCtrlCycleNrofCopies = lastCtrlCycleNrofCopies;
	}
		
	/**
	 * Method that registers the metric that has been aggregated in the controller
	 * affecting the current directive to be generated.
	 * @param directiveProperties The directive information.
	 */
	public void addDirectiveUsed(ControlMessage directive, Properties directiveProperties) {
		this.directivesUsed.add(directiveProperties);
	
		directiveProperties.put("id", directive.getId());
		directiveProperties.put("from", directive.getFrom());
		directiveProperties.put("dirS", new DecimalFormat("#0.00").format(directive.getProperty(DirectiveCode.NROF_COPIES_CODE)));
	}
	
	/**
	 * Method that registers the metric that has been aggregated in the controller
	 * affecting the current directive to be generated.
	 * @param metric The received metric about to be aggregated
	 * @param properties the properties of the metric. This object is to be filled
	 * with the basic details of the metric.   
	 */
	public void addMetricUsed(ControlMessage metric, Properties metricProperties) {
		CongestionMetricPerWT congestionMetric = (CongestionMetricPerWT)metric.getProperty(MetricCode.CONGESTION_CODE);
		this.metricsUsed.add(metricProperties);
			
		metricProperties.put("id", metric.getId());
		metricProperties.put("creationT", new DecimalFormat("#0").format(metric.getCreationTime()));
		metricProperties.put("from", metric.getFrom());
		metricProperties.put("aggregations",congestionMetric.getNrofAggregatedMetrics());
		metricProperties.put("CongS", new DecimalFormat("#0.00").format(congestionMetric.getCongestionValue()));		
	}
	
	/**
	 * Method that associates an empty list of directivesUsed to the 
	 * directiveDetails.
	 */
	public void reset() {
		this.directivesUsed.clear();
		this.metricsUsed.clear();
	}
	
	public String toString() {
		return String.format("%s %s %s", this.basicFieldsToString(), this.metricsUsedToString(), this.directivesUsed);
	}
	
	protected String basicFieldsToString() {
		return String.format("%s %d %s %d %d %.2f %s", this.directiveID, this.creationTime, 
				this.generatedByNode, this.lastCtrlCycleNrofCopies, this.newNrofCopies,  
				this.calculatedCongestion, this.congestionState);		
	}
	
	protected String metricsUsedToString() {
		return String.format("%d: %s", this.metricsUsed.size(), this.metricsUsed);
	}
	
	/**
	 * Method to print each one of the aggregatedMetricDetails in a different row 
	 * @return An string with all the metrics separated by a \n.
	 */
	protected String aggregatedMetricsToStringln() {
		String str = "";
		for(Properties aggregatedMetric: this.metricsUsed) {
			str += String.format("%s\n",aggregatedMetric);
		}
		return str;
	}
	
	/**
	 * Returns the name of the properties used in the toString method. It can be used by a report using 
	 * the {@ link #toString()} method as a header.
	 * @return
	 */
	public abstract String getHeaderString();

}
