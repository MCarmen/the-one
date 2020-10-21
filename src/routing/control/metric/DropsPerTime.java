/**
 * 
 */
package routing.control.metric;

/**
 * Encapsulates the number of drops sensed during certain time.
 */
public class DropsPerTime extends CongestionMetricPerWT {
	/**
	 * Constructor that initializes the object with the drops sensed during 
	 * a window time.
	 * @param drops drops sensed
	 * @param time sensing time.
	 */
	public DropsPerTime(double drops, double time) {
		super(drops, time);
	}
	
	public String toString() {
		return String.format("%.0f %.1f", this.congestionValue, this.time);
	}	
}
