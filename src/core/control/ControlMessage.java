package core.control;

import core.DTNHost;
import core.Message;

/**
 * Interface used as a super class for the control messages
 *
 */
public abstract class ControlMessage extends Message{

	public ControlMessage(DTNHost from, DTNHost to, String id, int size) {
		super(from, to, id, size);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public abstract Message replicate();

}
