package routing.control.metric;

import java.util.HashMap;
import java.util.Map;

import core.Settings;
import core.SimClock;
import core.control.MetricCode;
import core.control.MetricMessage;
import report.control.metric.MetricDetails;
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
	 * @param metrics           The map of received metrics during a window Time.
	 * @param metricDetails     Creation details of the metric. To be filled by this
	 *                          method.
	 */
	public static CongestionMetric getDoubleWeightedAverageForMetric(double congestionReading,
			Map<String, MetricMessage> metrics, MetricDetails metricDetails) {
	
		return DoubleWeightedAverageCongestionMetricCalculator.
				getDoubleWeightedAverageForMetric(congestionReading, metrics, metricDetails, null);
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
	 * @param metrics           The map of received metrics during a window Time.
	 * @param metricDetails     Creation details of the metric. To be filled by this
	 *                          method.
	 * @param exclude			HostId to be excluded from the aggregation. 
	 */
	public static CongestionMetric getDoubleWeightedAverageForMetric(double congestionReading,
			Map<String, MetricMessage> metrics, MetricDetails metricDetails, String exclude) {
		//Current simulation time used to calculate the metric's decay. 
		double currentTime = SimClock.getTime();
		//Array of the decay weight for each one of the metric in the metrics property.
		//In the first position we place the node's decay weight which is 1 (no decay)
		
		Map<String, Double> metricDecayWeights = new HashMap<String, Double>();
		CongestionMetric congestionMetric;
		double doubleWeightedAverageForCongestion = 0;		
		int sumOfAllTheMetricsAggregations = getSumOfAllAggregations(metrics, exclude);
		
		//We include our reading.		
		sumOfAllTheMetricsAggregations++;		
		double sumOfAllDecayWeights = getSumOfAllDecayWeights(currentTime, metrics, metricDecayWeights, exclude);
		//We include the current node's congestion reading decay weight which is 1 (no decay).
		sumOfAllDecayWeights++;

		// aggregating the current's node congestion reading
		doubleWeightedAverageForCongestion += getDoubleWeightedAverageForAMeasure(congestionReading, 1,
				sumOfAllTheMetricsAggregations, 1.0, sumOfAllDecayWeights);
		for (Map.Entry<String, MetricMessage> entry : metrics.entrySet()) {
			if(exclude == null || !entry.getKey().equals(exclude)) {
				MetricMessage metric = entry.getValue();
				double metricDecayWeight = metricDecayWeights.get(metric.getFrom().toString()); 
				congestionMetric = (CongestionMetric) metric.getProperty(MetricCode.CONGESTION_CODE);
				doubleWeightedAverageForCongestion += getDoubleWeightedAverageForAMeasure(
						congestionMetric.congestionValue, congestionMetric.nrofAggregatedMetrics,
						sumOfAllTheMetricsAggregations, metricDecayWeight, sumOfAllDecayWeights);				
				metricDetails.aggregateMetric(metric, metricDecayWeight);
			}
		}

		metricDetails.init(congestionReading, doubleWeightedAverageForCongestion, 1.0,
				sumOfAllTheMetricsAggregations, sumOfAllDecayWeights);

		CongestionMetric doubleWeightedCongestionMetric = new BufferOccupancy(
				doubleWeightedAverageForCongestion, metrics.size());

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
			int readingAggregations, int sumOfAggregations, double readingDecay, 
			double sumOfDecays) {
		return (congestionReading 
				* (alpha*(readingAggregations/sumOfAggregations)
				+(1-alpha)*(readingDecay/sumOfDecays)));
	}
	
	/**
	 * Method that calculates the congestion weighted metric built out of the
	 * aggregation of the metrics in the metrics table: sum(i=1,k)(
	 * node_i_congestion *
	 * (alpha*(node_i_aggregations/sum(j=1,k)node_j_aggregations) +
	 * (1-alpha)*(node_i_decay/sum(l=1,k)node_l_decay)) )
	 * 
	 * 
	 * @param metrics       The map of received metrics during a window Time.
	 * @param metricDetails Creation details of the metric. To be filled by this
	 *                      method.
	 */
	public static double getDoubleWeightedAverage(Map<String, MetricMessage> metrics) {
		//Current simulation time used to calculate the metric's decay. 
		double currentTime = SimClock.getTime();
		//Array of the decay weight for each one of the metric in the metrics property.
		Map<String, Double> metricDecayWeights = new HashMap<String, Double>();
		CongestionMetric congestionMetric;
		double doubleWeightedAverageForCongestion = 0;
		int sumOfAllTheMetricsAggregations = getSumOfAllAggregations(metrics, null);
		double sumOfAllDecayWeights = getSumOfAllDecayWeights(currentTime, metrics, metricDecayWeights, null);
		for (Map.Entry<String, MetricMessage> entry : metrics.entrySet()) {
			MetricMessage metric = entry.getValue();
			double metricDecayWeight = metricDecayWeights.get(metric.getFrom().toString()); 
			congestionMetric = (CongestionMetric) metric.getProperty(MetricCode.CONGESTION_CODE);
			doubleWeightedAverageForCongestion += getDoubleWeightedAverageForAMeasure(
					congestionMetric.congestionValue, congestionMetric.nrofAggregatedMetrics,
					sumOfAllTheMetricsAggregations, metricDecayWeight, sumOfAllDecayWeights);				
		}

		return doubleWeightedAverageForCongestion;				
	}
	
	/**
	 * Returns the sum of all decay weights of the metrics in the metrics map. It stores
	 * in a map indexed by the host id all the decay weights. 
	 * @param currentTime the current simulation time.
	 * @param metrics The list of received metrics during a window Time.
	 * @param metricDecayWeights Map of the decay weight for each one of the 
	 * metric in the metrics list. 
	 * @param exclude HostId to be excluded from the aggregation.      
	 * @return The sum of the decays.
	 */
	private static double getSumOfAllDecayWeights(double currentTime, Map<String, MetricMessage> metrics, Map<String, Double> metricDecayWeights, String exclude) {		
		double decayWeight;
		double sumOfAllDecays = 0;
		
		for (Map.Entry<String, MetricMessage> entry : metrics.entrySet()) {
			if(exclude == null || !entry.getKey().equals(exclude)) {
				decayWeight = decay.getDecayWeightAt(currentTime - entry.getValue().getCreationTime());
				metricDecayWeights.put(entry.getValue().getFrom().toString(), decayWeight);
				sumOfAllDecays += decayWeight; 
			}
		}
		
		return sumOfAllDecays;
	}
	
	/**
	 * Method that sums the number of aggregations used to generate each one of 
	 * the metrics passed as a parameter. 
	 * @param metrics The map of received metrics.
	 * @param exclude HostId to be excluded from the aggregation.     
	 * @return the sum of the number of aggregations of each one of the metrics. 
	 */
	private static int getSumOfAllAggregations(Map<String, MetricMessage> metrics, String exclude) {
		int sumOfAllAggregations = 0;
		CongestionMetric congestionMetric;
		
		for (Map.Entry<String, MetricMessage> entry : metrics.entrySet()) {
			if(exclude == null || !entry.getKey().equals(exclude)) {
				congestionMetric = (CongestionMetric)entry.getValue().getProperty(MetricCode.CONGESTION_CODE);
				sumOfAllAggregations += congestionMetric.getNrofAggregatedMetrics();
			}				
		}
			
		return sumOfAllAggregations;
	}
}
