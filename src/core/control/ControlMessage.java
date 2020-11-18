package core.control;

import core.DTNHost;
import core.Message;

/**
 * Interface used as a super class for the control messages
 *
 */
public abstract class ControlMessage extends Message{
	
	/**
	 * Constructor used to build control messages. Those messages will not be 
	 * treated as data messages. These messages cannot be added to the message buffer. 
	 * @param from The host that created the message.
	 * @param id The identifier of the message.
	 */
	public ControlMessage(DTNHost from, String id) {
		this(from, null, id, 0);
	}
	
	/**
	 * Constructor that will create a control message that can be treated like
	 * a data message.
	 * @param from The host that created the message
	 * @param to The destination of the message.
	 * @param id The identifier of the message.
	 * @param size The size of the messge.
	 */
	public ControlMessage(DTNHost from, DTNHost to, String id, int size) {
		super(from, to, id, size);
		// TODO Auto-generated constructor stub
	}
	
	
	@Override
	public abstract Message replicate();
	
}
