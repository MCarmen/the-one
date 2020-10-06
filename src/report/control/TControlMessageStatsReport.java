package report.control;

import core.DTNHost;
import core.Message;
import core.MessageListener;
import report.MessageStatsReport;

/**
 * Report for generating different kind of total statistics 
 * relaying performance about any type of message. Messages that were created 
 * during the warm up period are ignored.
 * <P><strong>Note:</strong> if some statistics could not be created (e.g.
 * overhead ratio if no messages were delivered) "NaN" is reported for
 * double values and zero for integer median(s).
 */
public abstract class TControlMessageStatsReport extends MessageStatsReport implements MessageListener {
	Class<? extends Message> messageClass;
	
	/**
	 * This method has to be called by the constructors of the subclasses of 
	 * this class to specify which is the type of the message we will be 
	 * generating the statistics about. 
	 * @param messageClass The Class<? extends Message> specifying the type of 
	 * the Message we are generating the statistics about.
	 */
	protected void init(Class<? extends Message> messageClass) {
		this.messageClass = messageClass;
	}
	
	@Override
	public void newMessage(Message m) {
		if(messageClass.isInstance(m)) {
			super.newMessage(m);
		}
	}

	@Override
	public void messageTransferStarted(Message m, DTNHost from, DTNHost to) {
		if(messageClass.isInstance(m)) {
			super.messageTransferStarted(m, from, to);
		}
	}

	@Override
	public void messageDeleted(Message m, DTNHost where, boolean dropped) {
		if(messageClass.isInstance(m)) {
			super.messageDeleted(m, where, dropped);
		}
	}

	@Override
	public void messageTransferAborted(Message m, DTNHost from, DTNHost to) {
		if(messageClass.isInstance(m)) {
			super.messageTransferAborted(m, from, to);
		}
	}

	@Override
	public void messageTransferred(Message m, DTNHost from, DTNHost to, boolean firstDelivery) {
		if(messageClass.isInstance(m)) {
			super.messageTransferred(m, from, to, firstDelivery);
		}

	}
}
