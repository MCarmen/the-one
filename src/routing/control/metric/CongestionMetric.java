package routing.control.metric;

/**
 * Interface for all the congestion metrics. 
 */
public abstract class CongestionMetric {	
	/***
	 * Congestion metric measured per window time.
	 */
	protected double congestionValue = 0.0;
	
	/** The number of metrics, including the current reading, we have aggregated 
	 * to generate this congestion metric. By default we consider the current
	 * reading. */
	protected int nrofAggregatedMetrics = 1;
		
	public CongestionMetric(double congestionValue) {
		this.congestionValue = congestionValue;
	}
	
	public CongestionMetric(double congestionValue, int nrofAggregatedMetrics) {
		this(congestionValue);
		this.nrofAggregatedMetrics = nrofAggregatedMetrics;
	}



	protected CongestionMetric() {}
	
	public double getCongestionValue() {
		return this.congestionValue;
	}
	
	
	public int getNrofAggregatedMetrics() {
		return nrofAggregatedMetrics;
	}

	/**
	 * @return a String representation of the metric.
	 */
	public abstract String toString();
}
