package input.control;

import core.DTNHost;
import core.Message;
import core.control.MetricMessage;

/**
 * class for external event for creating a metric message.
 */
public class MetricMessageCreateEvent extends ControlMessageCreateEvent {

	/**
	 * Creates a metric message creation event without a response request
	 * @param from The creator of the message
	 * @param to Where the message is destined to
	 * @param id ID of the message
	 * @param size Size of the message  
	 * @param time Time, when the message is created
	 */
	public MetricMessageCreateEvent(int from, int to, String id, int size, double time) {
		super(from, to, id, size, time);
		// TODO Auto-generated constructor stub
	}
	
	
	@Override
	public String toString() {
		return super.toString() + " [" + fromAddr + "->" + toAddr + "] " +
		 " CREATE_METRIC";
	}
	
	@Override
	protected Message getMessage(DTNHost from, DTNHost to, String id, int size) {
		// TODO Auto-generated method stub
		return new MetricMessage(from, to, id, size);
	}

}
