/**
 * 
 */
package routing.control.metric;

/**
 * Encapsulates the number of drops sensed.
 */
public class Drops extends CongestionMetric {
	/**
	 * Constructor that initializes the object with the drops sensed during 
	 * a window time.
	 * @param drops drops sensed
	 */
	public Drops(double drops) {
		super(drops);
	}
	
	public String toString() {
		return String.format("%.0f", this.congestionValue);
	}	
}
