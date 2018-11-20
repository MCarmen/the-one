/**
 * 
 */
package routing.control;

import core.SimClock;

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
	
	public boolean fillMessageWithMetric(Message message) {
		
	}
	
	
	/**
	 * Inner class that encapsulates the number of drops sensed during
	 * certain time.
	 */
	public static class DropsPerTime{
		/** Drops sensed. */
		private int nrofDrops;
		/** Time while we have been sensing. */
		private long time;

		/**
		 * Constructor that initalizes the object with the drops sensed during 
		 * time.
		 * @param drops drops sensed
		 * @param time sensing time.
		 */
		public DropsPerTime(int drops, long time) {
			this.nrofDrops = drops;
			this.time = time;
		}

		public int getNrofDrops() {
			return nrofDrops;
		}

		public long getTime() {
			return time;
		}
				
	}
	
}
