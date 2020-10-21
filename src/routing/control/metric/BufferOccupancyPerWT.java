/**
 * 
 */
package routing.control.metric;

/**
 * class that encapsulates the fraction of buffer occupancy sensed 
 * during a window time
 * @param occupancy the fraction of buffer occupancy + bytes dropped in a 
 * window time.
 * @param time sensing time.
 */
public class BufferOccupancyPerWT extends CongestionMetricPerWT {
	public BufferOccupancyPerWT(double bufferOccupancy, double time) {
		super(bufferOccupancy, time);
	}
	
	
	public BufferOccupancyPerWT(double congestionValue, double time, int nrofAggregatedMetrics) {
		super(congestionValue, time, nrofAggregatedMetrics);
	}


	public String toString() {
		return String.format("%.3f %.1f", this.congestionValue, this.time);
	}		
	
}
