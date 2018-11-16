package input.control;

import core.World;

/**
 * class for external event for creating a metric message.
 */
public class MetricMessageCreateEvent extends ControlMessageCreateEvent {

	/**
	 * Creates a metric message creation event without a response request
	 * @param from The creator of the message
	 * @param to Where the message is destined to
	 * @param id ID of the message
	 * @param time Time, when the message is created
	 */
	public MetricMessageCreateEvent(int from, int to, String id, double time) {
		super(from, to, id, time);
		// TODO Auto-generated constructor stub
	}
	
	
	@Override
	public String toString() {
		return super.toString() + " [" + fromAddr + "->" + toAddr + "] " +
		 " CREATE_METRIC";
	}

}
