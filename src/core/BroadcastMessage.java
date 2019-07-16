package core;

import core.control.DirectiveMessage;

/**
 * A broadcast message that is created at a node or passed between nodes.
 * The destination of a broadcast message can be a random host from the toHost 
 * list, as the broadcast messages is addressed to all the hosts in toHost list. 
 */
public class BroadcastMessage extends Message{

	/**
	 * Creates a new Broadcast Message.
	 * @param from Who the message is (originally) from
	 * @param to Who the message is (originally) to. This parameter can be an 
	 * random host from the toHost list, as the broadcast messages is addressed
	 * to all the hosts in toHost list.
	 * @param id Message identifier (must be unique for message but
	 * 	will be the same for all replicates of the message)
	 * @param size Size of the message (in bytes)
	 */
	public BroadcastMessage(DTNHost from, DTNHost to, String id, int size) {
		super(from, to, id, size);
		this.type = MessageType.MESSAGE_BROADCAST;
	}
	

	@Override
	public Message replicate() {
		// TODO Auto-generated method stub
		BroadcastMessage m = new BroadcastMessage(this.getFrom(), this.getTo(), this.getId(), this.getSize());
		m.copyFrom(this);
		return m;
	}

}
