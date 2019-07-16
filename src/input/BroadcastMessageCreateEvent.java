package input;

import core.BroadcastMessage;
import core.DTNHost;
import core.Message;
import core.World;

/**
 * External event for creating a broadcast message.
 */
public class BroadcastMessageCreateEvent extends MessageCreateEvent {

	/**
	 * Creates a broadcast message creation event with a optional response request
	 * @param from The creator of the message
	 * @param to Where the message is destined to
	 * @param id ID of the message
	 * @param size Size of the message
	 * @param responseSize Size of the requested response message or 0 if
	 * no response is requested
	 * @param time Time, when the message is created
	 */
	public BroadcastMessageCreateEvent(int from, int to, String id, int size, int responseSize, double time) {
		super(from, to, id, size, responseSize, time);
	}

	/**
	 * Creates the message this event represents.
	 */
	@Override
	public void processEvent(World world) {
		DTNHost to = world.getNodeByAddress(this.toAddr);
		DTNHost from = world.getNodeByAddress(this.fromAddr);

		Message m = new BroadcastMessage(from, to, this.id, this.size);
		m.setResponseSize(this.responseSize);
		from.createNewMessage(m);
	}

}
