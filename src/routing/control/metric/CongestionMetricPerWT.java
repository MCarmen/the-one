package routing.control.metric;

/**
 * Interface for all the congestion metrics. 
 */
public abstract class CongestionMetricPerWT {	
	/***
	 * Congestion metric measured per window time.
	 */
	protected double congestionValue = 0.0;
	
	/** Time while we have been sensing. */
	protected double time;	
	
	/** The number of metrics, including the current reading, we have aggregated 
	 * to generate this congestion metric. By default we consider the current
	 * reading. */
	protected int nrofAggregatedMetrics = 1;
		
	public CongestionMetricPerWT(double congestionValue, double time) {
		this.congestionValue = congestionValue;
		this.time = time;
	}
	
	public CongestionMetricPerWT(double congestionValue, double time, int nrofAggregatedMetrics) {
		this(congestionValue, time);
		this.nrofAggregatedMetrics = nrofAggregatedMetrics;
	}



	protected CongestionMetricPerWT() {}
	
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
