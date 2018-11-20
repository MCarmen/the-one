/**
 * 
 */
package routing.control;

import core.Message;
import core.SimClock;
import core.control.MetricCode;

/*
  class MetricsSensed{

    + fillMessageWithMetric(Message) : boolean
  }

 */

/**
 * Class that encapsulates the metrics sensed by the router. At the moment
 * just the drops are sensed.
 */
public class MetricsSensed {
	/** Counter of the drops sensed during an amount of time. */
	private int dropsPerWT;
	
	/** Amount of time while the sensing has been done. */
	private double sensingWindowTime;
	
	/**
	 * The constructor initializes the drops to 0 and the sensing time to the 
	 * current simulation time. 
	 */
	public MetricsSensed() {
		this.reset();
	}
	
	/**
	 * Method that resets the drops and the sensing time. The drops are set to 0
	 * and the sensing time to the current simulation time.
	 */
	private void reset() {
		this.dropsPerWT = 0;
		this.sensingWindowTime = SimClock.getTime();
	}
	
	/**
	 * Method that increments in one unit the drops counter.
	 */
	public void addDrop() {
		this.dropsPerWT++;
	}

	/**
	 * Method that fills the message with the drops sensed at the point of 
	 * calling this method. If there is no drop, the message is not modified, 
	 * and the window sensing time is reset to the current simulation time.
	 * @param message the message to be filled with the drops sensed untill now.
	 * @return true if the message has been modified with the drops sensed. If
	 * there have been no drops it returns false.
	 */
	public boolean fillMessageWithMetric(Message message) {
		boolean messageFilled = (this.dropsPerWT > 0);
		if (this.dropsPerWT > 0) {
			message.addProperty(MetricCode.DROPS_CODE.toString(), 
					new DropsPerTime(this.dropsPerWT, SimClock.getTime()-this.sensingWindowTime));
		}
		this.reset();
		
		return messageFilled;
	}
	
	
	/**
	 * Inner class that encapsulates the number of drops sensed during
	 * certain time.
	 */
	public static class DropsPerTime{
		/** Drops sensed. */
		private int nrofDrops;
		/** Time while we have been sensing. */
		private double time;

		/**
		 * Constructor that initalizes the object with the drops sensed during 
		 * time.
		 * @param drops drops sensed
		 * @param time sensing time.
		 */
		public DropsPerTime(int drops, double time) {
			this.nrofDrops = drops;
			this.time = time;
		}

		public int getNrofDrops() {
			return nrofDrops;
		}

		public double getTime() {
			return time;
		}
				
	}
	
}
