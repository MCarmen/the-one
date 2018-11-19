/**
 * 
 */
package input.control;

import core.DTNHost;
import core.Message;
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
	
	@Override
	public void processEvent(World world) {
		DTNHost to = world.getNodeByAddress(this.toAddr);
		DTNHost from = world.getNodeByAddress(this.fromAddr);

		Message m = this.getMessage(from, to, this.id, this.size);
		from.createNewMessage(m);
	}
	
	/**
	 * Factory method to create a control message.
	 * 
	 * @param from The creator of the message
	 * @param to   Where the message is destined to
	 * @param id   ID of the message
	 * @param size the size of the payload of the message.
	 * @return
	 */
	protected abstract Message getMessage(DTNHost from, DTNHost to, String id, int size);
}
