package routing.control.metric;

import java.util.List;

import core.Settings;
import core.SimClock;
import core.control.MetricCode;
import core.control.MetricMessage;
import report.control.metric.MetricDetails;
import report.control.metric.MetricDetails.AggregatedMetricDetails;
import routing.control.util.Decay;
import routing.control.util.DecayFactory;

/**
 * Encapsulates the methods to calculate a double weighted average of a 
 * congestion metric.
 * @author mc
 *
 */
public class DoubleWeightedAverageCongestionMetricCalculator {

	/**
	 * The decay function to be applied to the metrics in case it is set in the
	 * settings
	 */
	private static Decay decay = DecayFactory.getDecay();
		
	/** The weight of one of the average factors. */
	private static double alpha;
	
	/** Name space for the metricDoubleWeightedAvg settings*/
	private static final String METRIC_DOUBLE_WEIGHTED_NS = "metricDoubleWeightedAvg";
	
	/** {@value} setting. */
	private static final String ALPHA_S = "alpha";
	
	/** Default value for the setting {@link #ALPHA_S} */
	private static final double DEF_ALPHA_S = 0.5;
	
	static {
		Settings doubleWeightedSettings = new Settings(METRIC_DOUBLE_WEIGHTED_NS);
		alpha =  (doubleWeightedSettings.contains(ALPHA_S)) 
			? doubleWeightedSettings.getDouble(ALPHA_S) : DEF_ALPHA_S; 					
		}
	
		
		
	/**
	 * Method that calculates the congestion weighted metric built out of the
	 * current bufferOccupancy reading aggregated with metrics received from other
	 * nodes during a window time following the formula: sum(i=1,k)(
	 * node_i_congestion *
	 * (alpha*(node_i_aggregations/sum(j=1,k)node_j_aggregations) +
	 * (1-alpha)*(node_i_decay/sum(l=1,k)node_l_decay)) )
	 * 
	 * @param congestionReading The congestion metric reading.
	 * @param windowTime        Time while the congestion information is being
	 *                          gathered.
	 * @param metrics           The list of received metrics during a window Time.
	 * @param metricDetails     Creation details of the metric. To be filled by this
	 *                          method.
	 */
	public static CongestionMetricPerWT getDoubleWeightedAverageForMetric(double congestionReading, double windowTime,
			List<MetricMessage> metrics, MetricDetails metricDetails) {
		/** Current simulation time used to calculate the metric's decay. */
		double currentTime = SimClock.getTime();
		/**
		 * Array of the decay weight for each one of the metric in the metrics property.
		 * In the first position we place the node's decay weight which is 1 (no decay)
		 */
		double[] metricDecayWeights = new double[metrics.size() + 1];
		CongestionMetricPerWT congestionMetric;
		int sumOfAllTheMetricsAggregations = getSumOfAllAggregations(metrics);
		double sumOfAllDecayWeights = getSumOfAllDecayWeights(currentTime, metrics, metricDecayWeights);
		double doubleWeightedAverageForCongestion = 0;
		int i = 0;

		// aggregating the current's node congestion reading
		doubleWeightedAverageForCongestion += getDoubleWeightedAverageForAMeasure(congestionReading, 1,
				sumOfAllTheMetricsAggregations, metricDecayWeights[i++], sumOfAllDecayWeights);
		for (MetricMessage metric : metrics) {
			if (metric.containsProperty​(MetricCode.CONGESTION_CODE)) {
				congestionMetric = (CongestionMetricPerWT) metric.getProperty(MetricCode.CONGESTION_CODE);
				doubleWeightedAverageForCongestion += getDoubleWeightedAverageForAMeasure(
						congestionMetric.congestionValue, congestionMetric.nrofAggregatedMetrics,
						sumOfAllTheMetricsAggregations, metricDecayWeights[i++], sumOfAllDecayWeights);
				
				metricDetails.aggregateMetric(new AggregatedMetricDetails(metric.getId(), metric.getFrom().toString(),
						metric.getCreationTime(), congestionMetric.congestionValue, congestionMetric.nrofAggregatedMetrics,
						decay.getDecayWeightAt(currentTime - metric.getCreationTime())));
			}
		}

		metricDetails.init(congestionReading, doubleWeightedAverageForCongestion, metricDecayWeights[0],
				sumOfAllTheMetricsAggregations, sumOfAllDecayWeights);

		CongestionMetricPerWT doubleWeightedCongestionMetric = new BufferOccupancyPerWT(
				doubleWeightedAverageForCongestion, windowTime, metricDecayWeights.length);

		return doubleWeightedCongestionMetric;
	}
	
	/**
	 * Method that calculates the double weighted average for the value 
	 * congestionReading following the formula:
	 * node_congestion *
	 * (alpha*(node_aggregations/sum(j=1,k)node_j_aggregations) +
	 * (1-alpha)*(node_decay/sum(l=1,k)node_l_decay))
	 * 
	 * @param congestionReading the congestion reading to be double weighted.
	 * @param readingAggregations The nrof metrics used to generate the congestion reading. 
	 * @param sumOfAggregations The aggregations used to generate all the congestion readings.
	 * @param readingDecay The congestion reading decay
	 * @param sumOfDecays
	 * @return
	 */
	private static double getDoubleWeightedAverageForAMeasure(double congestionReading, 
			int readingAggregations, double sumOfAggregations, double readingDecay, 
			double sumOfDecays) {
		return (congestionReading 
				* (alpha*(readingAggregations/sumOfAggregations)
				+(1-alpha)*(readingDecay/sumOfDecays)));
	}
	
	/**
	 * Returns the sum of all decay weights of the metrics in the metrics list. It stores
	 * in an array all the decay weights. In the first position of the array is the 
	 * current node's congestion reading decay weight which is 1 (no decay).
	 * @param currentTime the current simulation time.
	 * @param metrics The list of received metrics during a window Time.
	 * @param metricDecayWeights Array of the decay weight for each one of the 
	 * metric in the metrics list. In the first position we place the node's 
	 * decay weight which is 1 (no decay).
	 * @return The sum of the decays.
	 */
	private static double getSumOfAllDecayWeights(double currentTime, List<MetricMessage> metrics, double[] metricDecayWeights) {		
		int i = 0;
		double decayWeight;
		
		metricDecayWeights[i] = 1;
		double sumOfAllDecays = metricDecayWeights[i++];
		for(MetricMessage metric : metrics) {
			if(metric.containsProperty​(MetricCode.CONGESTION_CODE)) {
				decayWeight = decay.getDecayWeightAt(currentTime - metric.getCreationTime());
				metricDecayWeights[i++] = decayWeight;
				sumOfAllDecays += decayWeight;  
			}
		}		
		return sumOfAllDecays;
	}
	
	/**
	 * Method that sums the number of aggregations used to generate each one of 
	 * the metrics passed as a parameter. 
	 * We include our reading, which has been produced by the current node.
	 * @param metrics The list of received metrics during a window Time.   
	 * @return the sum of the number of aggregations of each of the metrics including 
	 * the aggregations performed by the current node. 
	 */
	private static int getSumOfAllAggregations(List<MetricMessage> metrics) {
		int sumOfAllAggregations = 1;  
		CongestionMetricPerWT congestionMetric;
		
		for(MetricMessage metric : metrics) {
			if(metric.containsProperty​(MetricCode.CONGESTION_CODE)) {
				congestionMetric = (CongestionMetricPerWT)metric.getProperty(MetricCode.CONGESTION_CODE);
				sumOfAllAggregations += congestionMetric.getNrofAggregatedMetrics();  
			}
		}
		return sumOfAllAggregations;
	}
}
