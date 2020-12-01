/**
 * 
 */
package core.control;

import core.DTNHost;
import core.Message;

/**
 * A directive message that is created at a node or passed between nodes.
 */
public class DirectiveMessage extends ControlMessage {
	
	/** Counter to generate a unique identifier for each directive msg. */
	private static long idCounter = 0;
	
	/** Prefix to identify a directive message */
	private static final String PREFIX = "D"; 

	/**
	 * Constructor used to build control messages. Those messages will not be 
	 * treated as data messages. These messages cannot be added to the message buffer. 
	 * @param from The host that created the message.
	 * @param id The identifier of the message.
	 */
	public DirectiveMessage(DTNHost from) {
		this(from, null, DirectiveMessage.nextId(), 0);
	}
	
	/**
	 * Creates a new directive message.
	 * @param from Who the message is (originally) from
	 * @param to Who the message is (originally) to
	 * @param id Message identifier (must be unique for message but
	 * 	will be the same for all replicates of the message)
	 * @param size Size of the message (in bytes)
	 */
	public DirectiveMessage(DTNHost from, DTNHost to, String id, int size) {
		super(from, to, id, size);
		// TODO Auto-generated constructor stub
		this.type = MessageType.DIRECTIVE;
	}

	@Override
	public Message replicate() {
		// TODO Auto-generated method stub
		DirectiveMessage m = new DirectiveMessage(this.getFrom(), this.getTo(), this.getId(), this.getSize());
		m.copyFrom(this);
		return m;
	}

	/**
	 * Generates a new id combining a unique identifier with a prefix.
	 * @return A new ID combining a unique identifier 
	 */
	public static String nextId() {
		return String.format("%s%d", DirectiveMessage.PREFIX, ++DirectiveMessage.idCounter);		
	}
	

}
