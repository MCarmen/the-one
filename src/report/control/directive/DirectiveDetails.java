package report.control.directive;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import core.Message;
import core.control.ControlMessage;
import core.control.DirectiveCode;
import core.control.MetricCode;
import routing.control.MetricsSensed;

public class DirectiveDetails {
	
	/** Directive Identifier. */
	private String directiveID;
	
	/** Identifier of the node that generated the directive.  */
	private String generatedByNode;
		
	/** Value of the field containing the number of copies.  */
	private int newNrofCopies;
	
	/** List of the identifiers of the aggregated directives used to 
	 * generate  this one.*/
	private List<String> directivesUsed;
	
	/** A list with the metrics used  */
	private List<Properties> metricsUsed;
	
	/** When the directive was created */
	private int creationTime;

	/**
	 * Constructor that initializes the list of the ids of the directives used
	 * to generate this directive.
	 */
	public DirectiveDetails() {
		this.directivesUsed = new ArrayList<String>();
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
		this.directivesUsed = new ArrayList<String>(directiveDetails.getDirectivesUsed());
		this.metricsUsed = new ArrayList<>(directiveDetails.metricsUsed);
		this.creationTime = directiveDetails.getCreationTime();
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

	public List<String> getDirectivesUsed() {
		return directivesUsed;
	}
	
	public int getCreationTime() {
		return creationTime;
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
			
	public void addDirectiveUsed(String directiveUsed) {
		this.directivesUsed.add(directiveUsed);
	}
	
	/**
	 * Method that registers the metric that has been aggregated in the controller
	 * affecting the current directive to be generated.
	 * @param metric The received metric about to be aggregated
	 * @param currentDropsAverage The drops measurement aggregated until now.
	 * @param newDropsAverage The drops measurement after the metric passed as 
	 * a parameter is aggregated.
	 * @param currentMeanDeviationAverage the current meanDeviation of the measure.
	 * @param newMeanDeviationAverage the new meanDeviation after aggregating the new 
	 * deviation of the metric measure's to the mean. 
	 *  
	 */
	public void addMetricUsed(ControlMessage metric,
			double currentDropsAverage, double newDropsAverage, 
			double currentMeanDeviationAverage, double newMeanDeviationAverage) {
		Properties metricProperties = new Properties();
		double dropsSensed = 
				((MetricsSensed.DropsPerTime)metric.getProperty(MetricCode.DROPS_CODE.toString())).getNrofDrops();
		
		metricProperties.put("id", metric.getId());
		metricProperties.put("from", metric.getFrom());
		metricProperties.put("DrpS", new DecimalFormat("#0.00").format(dropsSensed));
		metricProperties.put("DrpAvg" , new DecimalFormat("#0.00").format(currentDropsAverage));
		metricProperties.put("NDrpAvg", new DecimalFormat("#0.00").format(newDropsAverage));
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
		return String.format("%s %s %d %d %s %s", this.directiveID, 
				this.generatedByNode, this.newNrofCopies, this.creationTime, 
				this.directivesUsed, this.metricsUsed);
	}
}
