/**
 * 
 */
package core.control;

import core.DTNHost;
import core.Message;
import core.Message.MessageType;

/**
 * A directive message that is created at a node or passed between nodes.
 */
public class DirectiveMessage extends Message implements ControlMessage {
	public static final String NROF_COPIES_CODE = "copies";
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

}
