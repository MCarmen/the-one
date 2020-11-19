package routing.control.metric;

import java.util.Map;

import core.Settings;
import core.control.MetricMessage;
import routing.control.util.Decay;
import routing.control.util.DecayFactory;

/**
 * Class used to calculate a double weighted average of a congestion metric.
 * @author mc
 *
 */
public class DoubleWeightedAverageForCongestion {
	/** 
	 * A map with the metrics received. The map is refreshed before being used
	 * so that the old metrics are removed.*/
	private Map<String, MetricMessage> metrics;	

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
	
	
	
	public DoubleWeightedAverageForCongestion(Settings doubleWeightedAvgSettings) {
		
	}



	/**
	 * Method that adds a metric to the map of metrics.
	 * 
	 * @param metric The received metric.
	 */
	public void addMetric(MetricMessage metric) {
		this.metrics.put(metric.getFrom().toString(), metric);
	}
	
	
	
}


