package report.control.metric;

import java.util.ArrayList;
import java.util.List;

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
	private List<AggregatedMetricDetails> aggregatedMetrics = new ArrayList<>();

	
	public MetricDetails(String metricID, String generatedByNode, double creationTime) {
		this.metricID = metricID;
		this.generatedByNode = generatedByNode;
		this.creationTime = creationTime;
	}

	public MetricDetails(String metricID, String generatedByNode, int creationTime, double originalValue,
			double aggregatedValue, double delay, int sumOfMetricsAggregations, double sumOfDelays) {
		this(metricID, generatedByNode, creationTime);
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

	public void aggregateMetric(AggregatedMetricDetails metricDetails) {
		this.aggregatedMetrics.add(metricDetails);
	}

	/**
	 * Returns the name of the properties used in the toString method. 
	 * It can be used by a report using 
	 * the {@ link #toString()} method to print the header.
	 * @return
	 */
	public static String getHeaderString() {
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append("id | CreationT | Gen By | ");
		strBuilder.append("originalVal | aggregatedVal | numAggr | delay | sumAggr | sumDelay");
		strBuilder.append("Foreach metric: ID | Node | val | aggregations | decayWeight ");
		
		return strBuilder.toString();
	}
	
	public String toString() {
		String str = String.format(
				"ID:%s Created:%.2f Node:%s Val:%.2f AggrVal:%.2f nrofAggr:%d decayWeight:%.2f sumAggr:%d sumDelay:%.2f aggregated metrics:\n%s", this.metricID, this.creationTime, this.generatedByNode, this.originalValue, this.aggregatedValue, this.aggregatedMetrics.size() + 1, this.decay, this.sumOfMetricsAggregations, this.sumOfDelays, this.aggregatedMetrics);
			
//				this.metricID, this.creationTime, this.generatedByNode, this.originalValue, this.aggregatedValue,
//				this.aggregatedMetrics.size() + 1, this.delay, this.sumOfMetricsAggregations, this.sumOfDelays,
//				this.aggregatedMetrics);
		return str;
	}
	
	
	/**
	 * Inner class that encapsulates the basic information of an aggregated 
	 * metric.
	 * @author mc
	 *
	 */
	public static class AggregatedMetricDetails{
		/** Metric Identifier. */
		private String metricID;
		
		/** Identifier of the node that generated the metric.  */
		private String generatedByNode;
		
		/** When the metric was created */
		private double creationTime;		
				
		/** The value of the metric. */
		private double value;
		
		/** The number of metrics aggregated to produce this metric */
		private int nrofAggregations;
		
		/** The current metric decay*/
		private double decayWeight;

		public AggregatedMetricDetails(String metricID, String generatedByNode, double creationTime, double value, int nrofAggregations,
				double decayWeight) {
			this.metricID = metricID;
			this.generatedByNode = generatedByNode;
			this.creationTime = creationTime;
			this.value = value;
			this.nrofAggregations = nrofAggregations;
			this.decayWeight = decayWeight;
		}
		
		public String toString() {
			return String.format("ID:%s Node:%s Created:%.2f Val:%.2f nrofAggr:%d delay:%.2f\n", this.metricID, this.generatedByNode,
					this.creationTime, this.value, this.nrofAggregations, this.decayWeight);
		}
	}
}
