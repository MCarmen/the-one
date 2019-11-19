package routing.control.metric;

/**
 * Interface for all the congestion metrics. 
 */
public abstract class CongestionMetricPerWT {	
	/***
	 * Congestion metric measured per window time.
	 */
	protected double congestionMetric = 0.0;
	
	/** Time while we have been sensing. */
	protected double time;	
		
	public CongestionMetricPerWT(double congestionMetric, double time) {
		this.congestionMetric = congestionMetric;
		this.time = time;
	}
	
	public double getCongestionMetric() {
		return this.congestionMetric;
	}
	/**
	 * @return a String representation of the metric.
	 */
	public abstract String toString();
}
