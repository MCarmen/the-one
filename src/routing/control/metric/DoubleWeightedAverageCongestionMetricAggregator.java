package routing.control.metric;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import core.Settings;
import core.SimClock;
import core.control.MetricCode;
import core.control.MetricMessage;
import report.control.metric.MetricDetails;
import routing.control.util.Decay;
import routing.control.util.DecayFactory;

/**
 * Class used to calculate a double weighted average of a congestion metric using
 * the current reading and the historical readings.
 * @author mc
 *
 */
public class DoubleWeightedAverageCongestionMetricAggregator {
	/** 
	 * A map with the metrics received. */
	private Map<String, MetricMessage> metrics;	
	
	/** The decay function to be applied to the metrics.*/
	private Decay decay;
		
	/** The weight of one of the average factors. */
	private double alpha;
	
	/** Name space for the metricDoubleWeightedAvg settings*/
	private static final String METRIC_DOUBLE_WEIGHTED_NS = "metricDoubleWeightedAvg";
	
	/** {@value} setting. */
	private static final String ALPHA_S = "alpha";
	
	/** Default value for the setting {@link #ALPHA_S} */
	private static final double DEF_ALPHA_S = 0.5;
	
	/** 
	 * {@value} setting used to specify the name space of the decay function
	 * to be used to calculat the double weighted average. 
	 */
	private static final String DECAY_NS_S = "decayNS";
	
	/** {@value} setting indicating the maximum number of metrics that can be stored in the metrics table. */
	private static final String METRICS_TABLE_MAX_SIZE_S = "metricsTableMaxSizeValue";
	
	/** Value to indicate no limit for the metrics table.  */
	private static final int NO_METRICS_TABLE_MAX_SIZE_LIMIT = Integer.MAX_VALUE;
		
	private int metricsTableMaxSizeValue;
	
	public DoubleWeightedAverageCongestionMetricAggregator() {
		this(new Settings(METRIC_DOUBLE_WEIGHTED_NS));
	}
	
	public DoubleWeightedAverageCongestionMetricAggregator(Settings doubleWeightedSettings) {
		this.alpha = (doubleWeightedSettings.contains(ALPHA_S)) 
				? doubleWeightedSettings.getDouble(ALPHA_S) : DEF_ALPHA_S;
		String decayNS = doubleWeightedSettings.getSetting(DECAY_NS_S);		
		this.decay = DecayFactory.getDecay(decayNS);
		this.metricsTableMaxSizeValue = (doubleWeightedSettings.contains(METRICS_TABLE_MAX_SIZE_S)) 
				? doubleWeightedSettings.getInt(METRICS_TABLE_MAX_SIZE_S) : NO_METRICS_TABLE_MAX_SIZE_LIMIT;
		this.metrics = new HashMap<>();		
	}
	
	/**
	 * Method used to set the list of the metrics we want to use to calculate 
	 * the average congestion. This list can be populated straight way through
	 * this method or by calling the method {@link #addMetric(MetricMessage)}.   
	 * @param metrics the metrics to be used to calculate the double weighted avg.
	 */
	public void setMetrics(Map<String, MetricMessage> metrics) {
		this.metrics = metrics;
	}

	/**
	 * Method that adds a metric to the map of metrics. If the metrics table is 
	 * full it makes room for the received metric.
	 * 
	 * @param metric The received metric.
	 */
	public void addMetric(MetricMessage metric) {
		this.makeRoomForMetricIfNecessary();
		this.metrics.put(metric.getFrom().toString(), metric);
	}
	
	/**
	 * Method that checks if there is enough room to store the new metric in the 
	 * metrics table. If not, we find the eldest entry.  
	 */
	private void makeRoomForMetricIfNecessary() {
		MetricMessage eldestMetric = null;
		if (this.metrics.size() >= this.metricsTableMaxSizeValue) {
			for(Entry<String, MetricMessage> entry : this.metrics.entrySet()) {
				if(eldestMetric == null) {
					eldestMetric = entry.getValue();
				}else {
					if (entry.getValue().getCreationTime() < eldestMetric.getCreationTime()) {
						eldestMetric = entry.getValue();
					}
				}		
			}
			this.metrics.remove(eldestMetric.getFrom().toString());			
		}		
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
	 * @param metrics           The map of received metrics to be used to calculate 
	 * 							the average.
	 * @param metricDetails     Creation details of the metric. To be filled by this
	 *                          method.
	 */
	public CongestionMetric getDoubleWeightedAverageForMetric(double congestionReading,
			 MetricDetails metricDetails) {
		return this.getDoubleWeightedAverageForMetric(congestionReading, metricDetails, null);
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
	public CongestionMetric getDoubleWeightedAverageForMetric(double congestionReading,
			MetricDetails metricDetails, String exclude) {
		//Current simulation time used to calculate the metric's decay. 
		double currentTime = SimClock.getTime();
		//Array of the decay weight for each one of the metric in the metrics property.
		//In the first position we place the node's decay weight which is 1 (no decay)
		
		Map<String, Double> metricDecayWeights = new HashMap<String, Double>();
		CongestionMetric congestionMetric;
		double doubleWeightedAverageForCongestion = 0;		
		int sumOfAllTheMetricsAggregations = this.getSumOfAllAggregations(exclude);
		int nrofAggregatedMetrics = 1; //our own metric.
		
		//We include our reading.		
		sumOfAllTheMetricsAggregations++;		
		double sumOfAllDecayWeights = this.getSumOfAllDecayWeights(currentTime, metricDecayWeights, exclude);
		//We include the current node's congestion reading decay weight which is 1 (no decay).
		sumOfAllDecayWeights++;

		// aggregating the current's node congestion reading
		doubleWeightedAverageForCongestion += this.getDoubleWeightedAverageForAMeasure(congestionReading, 1,
				sumOfAllTheMetricsAggregations, 1.0, sumOfAllDecayWeights);
		for (Map.Entry<String, MetricMessage> entry : this.metrics.entrySet()) {
			if(exclude == null || !entry.getKey().equals(exclude)) {
				MetricMessage metric = entry.getValue();
				double metricDecayWeight = metricDecayWeights.get(metric.getFrom().toString()); 
				congestionMetric = (CongestionMetric) metric.getProperty(MetricCode.CONGESTION_CODE);
				doubleWeightedAverageForCongestion += getDoubleWeightedAverageForAMeasure(
						congestionMetric.congestionValue, congestionMetric.nrofAggregatedMetrics,
						sumOfAllTheMetricsAggregations, metricDecayWeight, sumOfAllDecayWeights);				
				metricDetails.registerAggregatedMetric(metric, metricDecayWeight);
				nrofAggregatedMetrics++;
			}
		}

		metricDetails.init(congestionReading, doubleWeightedAverageForCongestion, 1.0,
				sumOfAllTheMetricsAggregations, sumOfAllDecayWeights);

		CongestionMetric doubleWeightedCongestionMetric = new BufferOccupancy(
				doubleWeightedAverageForCongestion, nrofAggregatedMetrics);

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
	private double getDoubleWeightedAverageForAMeasure(double congestionReading, 
			int readingAggregations, int sumOfAggregations, double readingDecay, 
			double sumOfDecays) {
		return (congestionReading 
				* (alpha*(readingAggregations/sumOfAggregations)
				+(1-alpha)*(readingDecay/sumOfDecays)));
	}
	
	/**
	 * Method that calculates the congestion weighted metric built out of the
	 * aggregated metrics up to now: sum(i=1,k)(
	 * node_i_congestion *
	 * (alpha*(node_i_aggregations/sum(j=1,k)node_j_aggregations) +
	 * (1-alpha)*(node_i_decay/sum(l=1,k)node_l_decay)) )
	 * 
	 * @return 	The double weighted average of the received metrics.
	 */
	public double getDoubleWeightedAverage() {
		//Current simulation time used to calculate the metric's decay. 
		double currentTime = SimClock.getTime();
		//Array of the decay weight for each one of the metric in the metrics property.
		Map<String, Double> metricDecayWeights = new HashMap<String, Double>();
		CongestionMetric congestionMetric;
		double doubleWeightedAverageForCongestion = 0;
		int sumOfAllTheMetricsAggregations = getSumOfAllAggregations(null);
		double sumOfAllDecayWeights = getSumOfAllDecayWeights(currentTime, metricDecayWeights, null);
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
	 * @param metricDecayWeights Map of the decay weight for each one of the 
	 * metric in the metrics list. 
	 * @param exclude HostId to be excluded from the aggregation.      
	 * @return The sum of the decays.
	 */
	private double getSumOfAllDecayWeights(double currentTime, Map<String, Double> metricDecayWeights, String exclude) {		
		double decayWeight;
		double sumOfAllDecays = 0;
		
		for (Map.Entry<String, MetricMessage> entry : this.metrics.entrySet()) {
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
	 * the metrics in the metrics map. 
	 * @param exclude HostId to be excluded from the aggregation.     
	 * @return the sum of the number of aggregations of each one of the metrics. 
	 */
	private int getSumOfAllAggregations(String exclude) {
		int sumOfAllAggregations = 0;
		CongestionMetric congestionMetric;
		
		for (Map.Entry<String, MetricMessage> entry : this.metrics.entrySet()) {
			if(exclude == null || !entry.getKey().equals(exclude)) {
				congestionMetric = (CongestionMetric)entry.getValue().getProperty(MetricCode.CONGESTION_CODE);
				sumOfAllAggregations += congestionMetric.getNrofAggregatedMetrics();
			}				
		}
			
		return sumOfAllAggregations;
	}
	
	/**
	 * Method that resets the map hoding the metrics used to calculate the 
	 * aggregation.
	 */
	public void reset() {
		this.metrics.clear();
	}
	
}


