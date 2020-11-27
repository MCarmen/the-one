package report.control.metric;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import core.control.MetricCode;
import core.control.MetricMessage;
import routing.control.metric.CongestionMetric;

/**
 * Class with the details of the created metric. 
 * @author mc
 *
 */
public class MetricDetails {
	
	/** Metric Identifier. */
	private String metricID;
	
	/** Identifier of the node that generated the metric.  */
	private String generatedByNode;
	
	/** To destination of the metric. */
	private String to;
	
	/** When the metric was created */
	private double creationTime;
	
	/** The original value of the metric. */
	private double originalValue;
	
	/** The final value of the metric after aggregating the metrics rececived. */	
	private double aggregatedValue;
	
	/** The current metric decay*/
	private double decay;
	
	/** The sum of the aggregations made by each one of the aggregated metrics. */
	private int sumOfMetricsAggregations;
	
	/** The sum of the delays of each one of the aggregated metrics. */
	private double sumOfDelays;
	
	/** A list with the aggregated metrics. */
	private List<Properties> aggregatedMetrics = new ArrayList<>();
	
	public MetricDetails() {
		
	}

	/**
	 * 
	 * @param metricID The identifier of the metric.
	 * @param generatedByNode The node that generated the metric.
	 * @param to The node destination of the metric. 
	 * @param creationTime The creation time of the metric.
	 */
	public MetricDetails(String metricID, String generatedByNode, String to, double creationTime) {
		this.metricID = metricID;
		this.generatedByNode = generatedByNode;
		this.to = to;
		this.creationTime = creationTime;
	}

	public MetricDetails(String metricID, String generatedByNode, String to, int creationTime, double originalValue,
			double aggregatedValue, double delay, int sumOfMetricsAggregations, double sumOfDelays) {
		this(metricID, generatedByNode, to, creationTime);
		this.init(originalValue, aggregatedValue, delay, sumOfMetricsAggregations, sumOfDelays);
	}
	

	/**
	 * Method that initializes some of the attributes of the class with the passed parameters.
	 * 
	 */
	public void init(double originalValue,
			double aggregatedValue, double decay, int sumOfMetricsAggregations, double sumOfDelays) {

		this.originalValue = originalValue;
		this.aggregatedValue = aggregatedValue;
		this.decay = decay;
		this.sumOfMetricsAggregations = sumOfMetricsAggregations;
		this.sumOfDelays = sumOfDelays;		
	}

	/**
	 * Method that adds to the list of aggregated metrics the metric passed as 
	 * a parameter. 
	 * @param metric The aggregated metric.
	 * @param decayWeight The decay weight of the aggregated metric.
	 */
	public void registerAggregatedMetric(MetricMessage metric, double decayWeight) {
		Properties metricProperties = new Properties();
		CongestionMetric congestionMetric;
		if (metric.containsProperty​(MetricCode.CONGESTION_CODE)) {
			congestionMetric = (CongestionMetric) metric.getProperty(MetricCode.CONGESTION_CODE);
			metricProperties.put("id", metric.getId());
			metricProperties.put("from", metric.getFrom());
			metricProperties.put("to", metric.getTo());
			metricProperties.put("creationT", new DecimalFormat("#0").format(metric.getCreationTime()));
			metricProperties.put("Value", new DecimalFormat("#0.00").format(congestionMetric.getCongestionValue()));
			metricProperties.put("aggregations",congestionMetric.getNrofAggregatedMetrics());
			metricProperties.put("decayWeight", decayWeight);
			this.aggregatedMetrics.add(metricProperties);
		}
	}

	/**
	 * Returns the name of the properties used in the toString method. 
	 * It can be used by a report using 
	 * the {@ link #toString()} method to print the header.
	 * @return
	 */
	public static String getHeaderString() {
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append("id | CreationT | Gen By | To | ");
		strBuilder.append("originalVal | aggregatedVal | numAggr | delay | sumAggr | sumDelay");
		strBuilder.append("Foreach metric: ID | Node | val | aggregations | decayWeight ");
		
		return strBuilder.toString();
	}
	
	public String toString() {
		return this.toString(" ");
	}
	
	/**
	 * Creates a string representation of the metric. It uses a separator between
	 * the metric details and the aggregated metrics.  
	 * @param separator string separator.
	 * @return The string representation of the metric.
	 */
	public String toString(String separator) {
		String str = String.format(
				"ID:%s Created:%.2f Node:%s To:%s Val:%.2f AggrVal:%.2f nrofAggr:%d decayWeight:%.2f sumAggr:%d sumDelay:%.2f aggregated metrics:\n%s",
				this.metricID, this.creationTime, this.generatedByNode, this.to, this.originalValue, this.aggregatedValue,
				this.aggregatedMetrics.size() + 1, this.decay, this.sumOfMetricsAggregations, this.sumOfDelays,
				this.toStringAggregatedMetrics(separator));

//				this.metricID, this.creationTime, this.generatedByNode, this.originalValue, this.aggregatedValue,
//				this.aggregatedMetrics.size() + 1, this.delay, this.sumOfMetricsAggregations, this.sumOfDelays,
//				this.aggregatedMetrics);
		return str;
	}
	
	/**
	 * Method to print each one of the aggregatedMetricDetails in a different row 
	 * @return An string with all the metrics separated by a \n.
	 */
	private String toStringAggregatedMetrics(String separator) {
		String str = "";
		for(Properties aggregatedMetric: this.aggregatedMetrics) {
			str += String.format("%s\n",aggregatedMetric);
		}
		return str;
	}
}
