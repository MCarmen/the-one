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
	private double bytesDroppedPerWT;
	
	/** Counter of the drops sensed during an amount of time. */
	private int dropsPerWT;	
			
	/** Amount of time while the sensing has been done. */
	private double sensingWindowTime;
	
	/** The size of the buffer */
	private double bufferSize;
	
	/** History of the metrics sensed for a windowTime  */
	private List<DropsPerTime> history;
	
	/**
	 * The constructor initializes the drops to 0 and the sensing time to the 
	 * current simulation time. It initializes the size of the buffer.
	 */	
	public MetricsSensed(long bufferSize) {
		this.history = new ArrayList<>();
		this.bufferSize = bufferSize;
		this.reset();
	}
	
	public String getHistoryAsString() {
		String historyStr = "";
		for(DropsPerTime dropsPerT : this.history) {
			historyStr += String.format("%d, ", dropsPerT.getPercentageOfStorageDropped());
		}
		
		return historyStr;
	}
		
	/**
	 * Method that resets the drops and the sensing time. The drops are set to 0
	 * and the sensing time to the current simulation time.
	 */
	private void reset() {
		this.dropsPerWT = 0;		
		this.bytesDroppedPerWT = 0;
		this.sensingWindowTime = SimClock.getTime();
	}
	
	/**
	 * Method that increments in one unit the drops counter and calculates the 
	 * percentage of bytes of the message respect the buffer size.
	 * @param the dropped message
	 */
	public void addDrop(Message message) {
		this.dropsPerWT++;
		this.bytesDroppedPerWT += message.getSize();		
	}
	

	/**
	 * Method that fills the message with the percentage of the bytes that have been
	 * dropped at the point of calling this method. The window sensing time is reset
	 * to the current simulation time.
	 * 
	 * @param message the message to be filled with the percentage of the bytes
	 *                dropped until now.
	 * @return true if the message has been modified with the percentage of the
	 *         bytes dropped..
	 */
	public boolean fillMessageWithMetric(Message message) {
		double percentageOfBytesDropped = this.bytesDroppedPerWT / this.bufferSize;
		DropsPerTime dropsPerTime = new DropsPerTime(this.dropsPerWT, percentageOfBytesDropped,
				SimClock.getTime() - this.sensingWindowTime);
		message.addProperty(MetricCode.DROPS_CODE.toString(), dropsPerTime);
		this.history.add(dropsPerTime);

		this.reset();
		return true;
	}
	
	/**
	 * Method that fills the message with the drops sensed and the percentage of the bytes that have been
	 * dropped at the point of calling this method. The window sensing time is reset
	 * to the current simulation time.
	 * 
	 * @param message the message to be filled with the percentage of the bytes
	 *                dropped until now.
	 * @return true if the message has been modified with the percentage of the
	 *         bytes dropped..
	 */
	public boolean fillMessageWithMetric(Message message, double bufferFreeSpace) {
		double bufferOccupancy = (bufferFreeSpace < 0) ? 1 : (this.bufferSize - bufferFreeSpace)/this.bufferSize;	
		return true;
	}
	
	/**
	 * Returns an string representation
	 */
	public String toString() {
		return String.format("%d %.3f %.1f", this.dropsPerWT, this.bytesDroppedPerWT,  
					(SimClock.getTime() - this.sensingWindowTime));		
	}
	
	/**
	 * Inner class that encapsulates the percentage of drops sensed during certain time.
	 */
	public static class DropsPerTime{
		/** Drops sensed. */
		private int nrofDrops;		
		/** Percentage of drops sensed. */
		private double percentageOfStorageDropped;
		/** Time while we have been sensing. */
		private double time;

		/**
		 * Constructor that initializes the object with the drops sensed during 
		 * a window time and the percentage that represents those bytes dropped 
		 * respect the buffer size. 
		 * @param drops drops sensed
		 * @param percentageOfDrops bytes dropped respect the buffer size
		 * @param time sensing time.
		 */
		public DropsPerTime(int drops, double percentageOfDrops, double time) {
			this.nrofDrops = drops;
			this.percentageOfStorageDropped = percentageOfDrops;
			this.time = time;
		}
		
		public int getNrofDrops() {
			return nrofDrops;
		}

		public double getPercentageOfStorageDropped() {
			return percentageOfStorageDropped;
		}

		public double getTime() {
			return time;
		}
		
		public String toString() {
			return String.format("%d %.3f %.1f", this.nrofDrops, this.percentageOfStorageDropped,  this.time);
		}
				
	}
	
}
