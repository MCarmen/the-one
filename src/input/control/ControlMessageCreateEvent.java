/**
 * 
 */
package input.control;

import core.World;
import input.MessageCreateEvent;

/**
 * Super class for external event for creating a control message.
 */
public abstract class ControlMessageCreateEvent extends MessageCreateEvent {

	/**
	 * Creates a control message creation event without a response request
	 * @param from The creator of the message
	 * @param to Where the message is destined to
	 * @param id ID of the message
	 * @param time Time, when the message is created
	 */
	public ControlMessageCreateEvent(int from, int to, String id, double time) {
		super(from, to, id, 0, 0, time);
		// TODO Auto-generated constructor stub
	}
	
}
