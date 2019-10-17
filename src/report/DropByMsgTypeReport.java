package report;

import core.DTNHost;
import core.Message;
import core.MessageListener;

/**
 * This class is a base class for the ...
 * @author mc
 *
 */
public abstract class DropByMsgTypeReport extends DropReport implements MessageListener{
	
	private Message.MessageType msgType;

	
	protected void init(Message.MessageType msgType) {
		this.msgType = msgType;
		super.init();
	}
	
	@Override
	public void messageDeleted(Message m, DTNHost where, boolean dropped) {
		if (m.getType() == this.msgType) {
			super.messageDeleted(m,  where,  dropped);
		}
	}
		
}
