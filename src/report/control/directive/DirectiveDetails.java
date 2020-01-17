package report.control.directive;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import core.Message;
import core.control.ControlMessage;
import core.control.DirectiveCode;
import core.control.DirectiveMessage;
import core.control.MetricCode;
import routing.control.EWMAEngine;
import routing.control.EWMAEngine.CongestionState;
import routing.control.metric.CongestionMetricPerWT;

public class DirectiveDetails {
	
	/** Directive Identifier. */
	private String directiveID;
	
	/** Identifier of the node that generated the directive.  */
	private String generatedByNode;
		
	/** Value of the field containing the number of copies.  */
	private int newNrofCopies;
	
	/** List of the identifiers of the aggregated directives used to 
	 * generate  this one.*/
	private List<Properties> directivesUsed;
	
	/** A list with the metrics used  */
	private List<Properties> metricsUsed;
	
	/** When the directive was created */
	private int creationTime;
	
	private EWMAEngine.CongestionState congestionState;

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
		this.directiveID = directiveDetails.getDirectiveID();
		this.generatedByNode = directiveDetails.getGeneratedByNode();	
		this.newNrofCopies = directiveDetails.getNewNrofCopies();
		this.directivesUsed = new ArrayList<>(directiveDetails.directivesUsed);
		this.metricsUsed = new ArrayList<>(directiveDetails.metricsUsed);
		this.creationTime = directiveDetails.getCreationTime();
		this.congestionState = directiveDetails.getCongestionState();
	}
	
	public String getDirectiveID() {
		return directiveID;
	}

	public String getGeneratedByNode() {
		return generatedByNode;
	}

	public int getNewNrofCopies() {
		return newNrofCopies;
	}

	public int getCreationTime() {
		return creationTime;
	}
	
	public EWMAEngine.CongestionState getCongestionState() {
		return congestionState;
	}
	
	
	/** 
	 * Method to be invoked when the directive has been created and therefore
	 * the message has been set with the directive fields.
	 * These fields are used to initialize this object.   
	 * @param m the message filled with the directive fields.
	 */	
	public void init(Message m) {
		this.directiveID = m.getId();
		this.generatedByNode = m.getFrom().toString();
		this.creationTime = (int)m.getCreationTime();
		if (m.containsProperty​(DirectiveCode.NROF_COPIES_CODE.toString())) {
			this.newNrofCopies = (int)m.getProperty(DirectiveCode.NROF_COPIES_CODE.toString());
		}		
	}
	
	/** 
	 * Method to be invoked when the directive has been created and therefore
	 * the message has been set with the directive fields.
	 * These fields are used to initialize this object.   
	 * @param m the message filled with the directive fields.
	 * @param congestionState the congestionState after generating the directive.
	 */	
	public void init(Message m, CongestionState congestionState) {
		this.init(m);
		this.congestionState = congestionState;
	}	
			
	public void addDirectiveUsed(ControlMessage directive, double currentAggregatedDirectivesAvg,
			double directiveSensed) {
		Properties directiveProperties = new Properties();
		
		directiveProperties.put("id", directive.getId());
		directiveProperties.put("from", directive.getFrom());
		directiveProperties.put("dirS", directive.getProperty(DirectiveCode.NROF_COPIES_CODE.toString()));
		directiveProperties.put("dirAvg", currentAggregatedDirectivesAvg);
		
		this.directivesUsed.add(directiveProperties);
	}
	
	/**
	 * Method that registers the metric that has been aggregated in the controller
	 * affecting the current directive to be generated.
	 * @param metric The received metric about to be aggregated
	 * @param currentCongestionAverage The drops measurement aggregated until now.
	 * @param newCongestionAverage The drops measurement after the metric passed as 
	 * a parameter is aggregated.
	 * @param currentMeanDeviationAverage the current meanDeviation of the measure.
	 * @param newMeanDeviationAverage the new meanDeviation after aggregating the new 
	 * deviation of the metric measure's to the mean. 
	 *  
	 */
	public void addMetricUsed(ControlMessage metric,
			double currentCongestionAverage, double newCongestionAverage, 
			double currentMeanDeviationAverage, double newMeanDeviationAverage) {
		Properties metricProperties = new Properties();
		CongestionMetricPerWT congestionMetric = (CongestionMetricPerWT)metric.getProperty(MetricCode.CONGESTION_CODE.toString());
		double congestionSensed = congestionMetric.getCongestionMetric();
		
		metricProperties.put("id", metric.getId());
		metricProperties.put("from", metric.getFrom());
		metricProperties.put("CongS", new DecimalFormat("#0.00").format(congestionSensed));
		metricProperties.put("CongAvg" , new DecimalFormat("#0.00").format(currentCongestionAverage));
		//metricProperties.put("DrpMeanDeviationAvg", new DecimalFormat("#0.00").format(currentMeanDeviationAverage));
		//metricProperties.put("NDrpMeanDeviationAvg", new DecimalFormat("#0.00").format(newMeanDeviationAverage));
		
		this.metricsUsed.add(metricProperties);
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
		return String.format("%s %s %d %d %s %s %s", this.directiveID, 
				this.generatedByNode, this.newNrofCopies, this.creationTime, 
				this.directivesUsed, this.metricsUsed, this.congestionState);
	}
}
