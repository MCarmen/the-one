/**
 * 
 */
package routing.control;

import java.util.ArrayList;
import java.util.List;

import core.Message;
import core.control.MetricCode;
import core.control.MetricMessage;
import report.control.metric.MetricDetails;
import routing.control.metric.BufferOccupancy;
import routing.control.metric.CongestionMetric;
import routing.control.metric.DoubleWeightedAverageCongestionMetricAggregator;

/*
  class MetricsSensed{

    + fillMessageWithMetric(Message) : boolean
  }

 */

/**
 * Class that encapsulates the metrics sensed by the router. At the moment
 * just the buffer occupancy is sensed.
 */
public class MetricsSensed {	
	/** The size of the buffer */
	private double bufferSize;
		
	/** History of the metrics sensed for a windowTime  */
	private List<CongestionMetric> history;
	
	/** Number of bytes dropped during an amount of time. */
	private int bytesDroppedPerWT;
	
	/** Aggregator for the received metrics. */
	private DoubleWeightedAverageCongestionMetricAggregator metricAggregator;
	
	/**
	 * The constructor initializes the drops to 0, the sensing time to the 
	 * current simulation time. It initializes the size of the buffer.
	 */	
	public MetricsSensed(long bufferSize, DoubleWeightedAverageCongestionMetricAggregator aggregator){
		this.history = new ArrayList<>();
		this.bufferSize = bufferSize;
		this.metricAggregator = aggregator;
		this.reset();
	}
	
	public String getHistoryAsString() {
		String historyStr = "";
		for(CongestionMetric congestionMetric : this.history) {
			historyStr += String.format("%.2f, ", congestionMetric.getCongestionValue());
		}
		
		return historyStr;
	}
		
	/**
	 * Method that resets the drops and the sensing time. The drops are set to 0
	 * and the sensing time to the current simulation time.
	 */
	private void reset() {
		this.metricAggregator.reset();
		this.bytesDroppedPerWT = 0;
	}

//	/**
//	 * Method that increments in one unit the drops counter and increments the number 
//	 * of bytes dropped.
//	 * @param the dropped message
//	 */
//	public void addDrop(Message message) {
//		this.bytesDroppedPerWT += message.getSize();
//	}
	
	/**
	 * Method that adds in the map of the receivedMetrics the received one.
	 * 
	 * @param metric The received metric.
	 */
	public void addReceivedMetric(MetricMessage metric) {
		this.metricAggregator.addMetric(metric);
	}
	
	/**
	 * Method that fills the message with the percentage of the (buffer occupancy
	 * including the bytes that have been dropped) at the point of calling this
	 * method. To calculate the buffer occupancy this method aggregates all the metrics 
	 * received up to this moment to the buffer occupancy reading. The received
	 * metrics expire based on their creation time. 
	 * 
	 * @param message         the message to be filled with the fraction of the
	 *                        occupancy of the buffer + drops in bytes
	 * @param bufferFreeSpace The free buffer space in bytes. May return a negative
	 *                        value if there are more messages in the buffer than
	 *                        should fit there (because of creating new
	 *                        messages) @see routing.ActiveRouter#makeRoomForMessage
	 *                        
	 * 
	 * @return true if the message has been modified with the fraction of the
	 *         occupancy of the buffer + drops in bytes
	 */
	public MetricDetails fillMessageWithMetric(Message message, double bufferFreeSpace) {
		return this.fillMessageWithMetric(message, bufferFreeSpace, null);
	}
	

	/**
	 * Method that fills the message with the percentage of the (buffer occupancy
	 * including the bytes that have been dropped) at the point of calling this
	 * method. To calculate the buffer occupancy this method aggregates all the metrics 
	 * received up to this moment to the buffer occupancy reading. The received
	 * metrics expire based on their creation time. 
	 * 
	 * @param message         the message to be filled with the fraction of the
	 *                        occupancy of the buffer + drops in bytes
	 * @param bufferFreeSpace The free buffer space in bytes. May return a negative
	 *                        value if there are more messages in the buffer than
	 *                        should fit there (because of creating new
	 *                        messages) @see routing.ActiveRouter#makeRoomForMessage
	 * @param exclude 		  Identifier of the node its metric we do not want to 
	 * aggregate in the process of the calculation of the buffer occupancy.                        
	 * 
	 * @return true if the message has been modified with the fraction of the
	 *         occupancy of the buffer + drops in bytes
	 */
	public MetricDetails fillMessageWithMetric(Message message, double bufferFreeSpace, String exclude) {
		double occupancy = this.getBufferOccupancy(bufferFreeSpace);
		MetricDetails metricDetails = new MetricDetails(message.getId(), message.getFrom().toString(), message.getCreationTime());
		//TODO: removed the old stored metrics (decay < threshold)
		CongestionMetric congestionMetric = this.metricAggregator.getDoubleWeightedAverageForMetric(
				occupancy, metricDetails, exclude);
		message.addProperty(MetricCode.CONGESTION_CODE, congestionMetric);
		this.history.add(congestionMetric);

		this.reset();
		return metricDetails;
	}

	/**
	 * Fills a Message with the occupancy of the buffer. 
	 * @param bufferFreeSpace The current buffer free space.
	 */
	public void fillMessageWithLocalCongestion(Message message, double bufferFreeSpace) {
		double occupancy = this.getBufferOccupancy(bufferFreeSpace);
		CongestionMetric congestion = new BufferOccupancy(occupancy, 1);
		message.addProperty(MetricCode.CONGESTION_CODE, congestion);		
	}
	
	/**
	 * Support method that calculates the buffer occupancy.  
	 * 
	 * @return The buffer occupancy.
	 */
	private double getBufferOccupancy(double bufferFreeSpace) {
		//return ((this.bufferSize - bufferFreeSpace) + this.bytesDroppedPerWT) / this.bufferSize;	
		return (this.bufferSize - bufferFreeSpace) / this.bufferSize;
	}
		
}
