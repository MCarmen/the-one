/**
 * 
 */
package routing.control;

import java.util.ArrayList;
import java.util.List;

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
		
	/** Messages created by a router during a window time. */
	private int createdMsgsPerWT;
	
	/** Messages received by a  router during a window time. */
	private int receivedMsgsPerWT;
	
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
		
	/**
	 * Method that resets the drops and the sensing time. The drops are set to 0
	 * and the sensing time to the current simulation time.
	 */
	private void reset() {
		this.dropsPerWT = 0;
		this.createdMsgsPerWT = 0;
		this.receivedMsgsPerWT = 0;
		this.sensingWindowTime = SimClock.getTime();
	}
	
	/**
	 * Method that increments in one unit the drops counter.
	 */
	public void addDrop() {
		this.dropsPerWT++;
	}
	
	/**
	 * Method that increments in one unit the createdMsgs counter.
	 */
	public void addCreatedMsg() {
		this.createdMsgsPerWT++;
	}

	/**
	 * Method that increments in one unit the receivedMsgs counter.
	 */
	public void addReceivedMsg() {
		this.receivedMsgsPerWT++;
	}

	/**
	 * Method that fills the message with the percentage of drops sensed at the point of 
	 * calling this method.  
	 * The window sensing time is reset to the current 
	 * simulation time 
	 * @param message the message to be filled with the drops sensed until now.
	 * @return true if the message has been modified with the drops sensed.
	 * false if the message has not been modified because 
	 * this.createdMsgsPerWT + this.receivedMsgsPerWT = 0. 
	 */
	public boolean fillMessageWithMetric(Message message) {
		boolean messageFilled = 
				(this.createdMsgsPerWT + this.receivedMsgsPerWT) != 0;
		if (messageFilled) {
			double percentageOfDrops = 
				this.dropsPerWT/(this.createdMsgsPerWT + this.receivedMsgsPerWT);
			DropsPerTime dropsPerTime;
			dropsPerTime = new DropsPerTime(percentageOfDrops, SimClock.getTime()-this.sensingWindowTime);
			message.addProperty(MetricCode.DROPS_CODE.toString(), 
					dropsPerTime);
			this.history.add(dropsPerTime);
		}	
		this.reset();		
		return messageFilled;
	}
	
	/**
	 * Returns an string representation
	 */
	public String toString() {
		return String.format("%.3f %.1f", this.dropsPerWT,  
					(SimClock.getTime() - this.sensingWindowTime));		
	}
	
	/**
	 * Inner class that encapsulates the percentage of drops sensed during certain time.
	 */
	public static class DropsPerTime{
		/** Percentage of drops sensed. */
		private double percentageOfDrops;
		/** Time while we have been sensing. */
		private double time;

		/**
		 * Constructor that initializes the object with the drops sensed during 
		 * time. 
		 * @param drops drops sensed
		 * @param time sensing time.
		 */
		public DropsPerTime(double percentageOfDrops, double time) {
			this.percentageOfDrops = percentageOfDrops;
			this.time = time;
		}

		public double getNrofDrops() {
			return percentageOfDrops;
		}

		public double getTime() {
			return time;
		}
		
		public String toString() {
			return String.format("%.3f %.1f", this.percentageOfDrops,  this.time);
		}
				
	}
	
}
