/**
 * 
 */
package routing.control.metric;

/**
 * Class that encapsulates the sensed fraction of buffer occupancy.
 */
public class BufferOccupancy extends CongestionMetric {
	public BufferOccupancy(double bufferOccupancy) {
		super(bufferOccupancy);
	}
	
	
	public BufferOccupancy(double congestionValue, int nrofAggregatedMetrics) {
		super(congestionValue, nrofAggregatedMetrics);
	}


	public String toString() {
		return String.format("%.3f", this.congestionValue);
	}		
	
}
