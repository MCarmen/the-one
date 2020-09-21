package report.control;

import core.DTNHost;
import core.Message;
import core.Message.MessageType;
import core.MessageListener;
import report.MessageStatsReport;

/**
 * Report for generating different kind of total statistics about 
 * concrete message types relaying performance. Messages that were created during 
 * the warm up period are ignored.
 * <P><strong>Note:</strong> if some statistics could not be created (e.g.
 * overhead ratio if no messages were delivered) "NaN" is reported for
 * double values and zero for integer median(s).
 */
public abstract class StandardMessageStatsReport extends MessageStatsReport implements MessageListener {

//	public ControlStatsReport() {
//		// TODO Auto-generated constructor stub
//	}
	
	protected MessageType messageType;
	
	public StandardMessageStatsReport(MessageType messageType) {
		this.messageType = messageType;
	}

	@Override
	public void newMessage(Message m) {
		if (m.getType() == this.messageType) {
			super.newMessage(m);
		}

	}

	@Override
	public void messageTransferStarted(Message m, DTNHost from, DTNHost to) {
		if (m.getType() == this.messageType) {
			super.messageTransferStarted(m, from, to);
		}
	}

	@Override
	public void messageDeleted(Message m, DTNHost where, boolean dropped) {
		if (m.getType() == this.messageType) {
			super.messageDeleted(m, where, dropped);
		}
	}

	@Override
	public void messageTransferAborted(Message m, DTNHost from, DTNHost to) {
		if (m.getType() == this.messageType) {
			super.messageTransferAborted(m, from, to);
		}
	}

	@Override
	public void messageTransferred(Message m, DTNHost from, DTNHost to, boolean firstDelivery) {
		if(m.getType() == this.messageType) {
			super.messageTransferred(m, from, to, firstDelivery);
		}

	}

}
