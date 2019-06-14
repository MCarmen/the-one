/**
 * 
 */
package routing.control;

import java.util.ArrayList;
import java.util.List;

import core.Message;
import core.SimClock;
import core.control.MetricCode;
import routing.MessageRouter;

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
	
	/** History of the metrics sensed for a windowTime  */
	private List<DropsPerTime> history;
	/**
	 * The constructor initializes the drops to 0 and the sensing time to the 
	 * current simulation time. 
	 */

	
	public MetricsSensed() {
		this.history = new ArrayList<>();
		this.reset();
	}
	
	public String getHistoryAsString() {
		String historyStr = "";
		for(DropsPerTime dropsPerT : this.history) {
			historyStr += String.format("%d, ", dropsPerT.getNrofDrops());
		}
		
		return historyStr;
	}
	
	//DEBUG
	public double getSensingWindowTime() {
		return sensingWindowTime;
	}
	//DEBUG
	
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
	 * @param message the message to be filled with the drops sensed until now.
	 * @return true if the message has been modified with the drops sensed. If
	 * there have been no drops it returns false.
	 */
	public boolean fillMessageWithMetric(Message message, MessageRouter router) {
		boolean messageFilled = (this.dropsPerWT > 0);
		DropsPerTime dropsPerTime;
		if (this.dropsPerWT > 0) {
			dropsPerTime = new DropsPerTime(this.dropsPerWT, SimClock.getTime()-this.sensingWindowTime);
			System.out.println(String.format("%s", dropsPerTime)); //DEBUG
			message.addProperty(MetricCode.DROPS_CODE.toString(), 
					dropsPerTime);
			this.history.add(dropsPerTime);
		}
		this.reset();
		System.out.println(String.format("Reset for: %s  new startSensing WT: %.1f", router.toString(), this.sensingWindowTime));//DEBUG
		
		return messageFilled;
	}
	
	/**
	 * Returns an string representation
	 */
	public String toString() {
//		return String.format("%d %.1f", this.dropsPerWT,  
//					(SimClock.getTime() - this.sensingWindowTime));
		return String.format("drops: %d simTime: %.1f startSensingTime: %.1f", this.dropsPerWT,  
				SimClock.getTime(), this.sensingWindowTime);		
		
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
		
		public String toString() {
			return String.format("%d %.1f", this.nrofDrops,  this.time);
		}
				
	}
	
}
