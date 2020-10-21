/**
 * 
 */
package routing.control;

import java.util.ArrayList;
import java.util.List;

import core.Message;
import core.SimClock;
import core.control.MetricCode;
import core.control.MetricMessage;
import report.control.metric.MetricDetails;
import routing.control.metric.BufferOccupancyPerWT;
import routing.control.metric.CongestionMetricPerWT;
import routing.control.metric.DoubleWeightedAverageCongestionMetricCalculator;

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
	
	/** Number of bytes dropped during an amount of time. */
	private int bytesDroppedPerWT;
	
	/** Congestion metric calculated for a windowTime */
	private CongestionMetricPerWT congestionMetricPerWT;
			
	/** Amount of time while the sensing has been done. */
	private double sensingWindowTime;
	
	/** The size of the buffer */
	private double bufferSize;
	
	/** A list with the metrics received for a window time */
	private List<MetricMessage> rececivedMetricsPerWT;	
	
	/** History of the metrics sensed for a windowTime  */
	private List<CongestionMetricPerWT> history;
	
	/**
	 * The constructor initializes the drops to 0, the sensing time to the 
	 * current simulation time. It initializes the size of the buffer.
	 */	
	public MetricsSensed(long bufferSize) {
		this.history = new ArrayList<>();
		this.bufferSize = bufferSize;
		this.reset();
	}
	
	public String getHistoryAsString() {
		String historyStr = "";
		for(CongestionMetricPerWT congestionMetricPerWT : this.history) {
			historyStr += String.format("%d, ", congestionMetricPerWT.getCongestionValue());
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
		this.rececivedMetricsPerWT = new ArrayList<MetricMessage>();		
	}
	
	/**
	 * Method that increments in one unit the drops counter and increments the number 
	 * of bytes dropped.
	 * @param the dropped message
	 */
	public void addDrop(Message message) {
		this.dropsPerWT++;
		this.bytesDroppedPerWT += message.getSize();
	}
	
	/**
	 * Method that adds in the list of the receivedMetrics the received one.
	 * @param metric The received metric.
	 */
	public void addReceivedMetric(MetricMessage metric) {
		this.rececivedMetricsPerWT.add(metric);
	}
	
	/**
	 * Method that fills the message with the percentage of the (buffer occupancy
	 * including the bytes that have been dropped) at the point of calling this
	 * method. The window sensing time is reset to the current simulation time.
	 * 
	 * @param message         the message to be filled with the fraction of the
	 *                        occupancy of the buffer + drops in bytes
	 * @param bufferFreeSpace The free buffer space in bytes. May return a negative
	 *                        value if there are more messages in the buffer than
	 *                        should fit there (because of creating new
	 *                        messages) @see routing.ActiveRouter#makeRoomForMessage
	 * 
	 * @return true if the message has been modified with the fraction of the
	 *         occupancy of the buffer + drops in bytes
	 */
	public MetricDetails fillMessageWithMetric(Message message, double bufferFreeSpace) {
		double occupancy = ((this.bufferSize - bufferFreeSpace) + this.bytesDroppedPerWT) / this.bufferSize;
		MetricDetails metricDetails = new MetricDetails(message.getId(), message.getFrom().toString(), message.getCreationTime());
		this.congestionMetricPerWT = DoubleWeightedAverageCongestionMetricCalculator.getDoubleWeightedAverageForMetric(
				occupancy, SimClock.getTime() - this.sensingWindowTime, this.rececivedMetricsPerWT, metricDetails);
		message.addProperty(MetricCode.CONGESTION_CODE, this.congestionMetricPerWT);
		this.history.add(this.congestionMetricPerWT);

		this.reset();
		return metricDetails;
	}
	

		
	/**
	 * Returns an string representation
	 */
	public String toString() {
		return String.format("%.3f %.1f", this.congestionMetricPerWT.getCongestionValue(),  
					(SimClock.getTime() - this.sensingWindowTime));		
	}
		
}
