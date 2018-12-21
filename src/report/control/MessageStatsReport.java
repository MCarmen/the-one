package report.control;

import core.DTNHost;
import core.Message;
import core.Message.MessageType;
import core.MessageListener;

public class MessageStatsReport extends ControlStatsReport implements MessageListener {

//	public ControlStatsReport() {
//		// TODO Auto-generated constructor stub
//	}

	@Override
	public void newMessage(Message m) {
		if (m.getType() == MessageType.MESSAGE) {
			super.newMessage(m);
		}

	}

	@Override
	public void messageTransferStarted(Message m, DTNHost from, DTNHost to) {
		if (m.getType() == MessageType.MESSAGE) {
			super.messageTransferStarted(m, from, to);
		}
	}

	@Override
	public void messageDeleted(Message m, DTNHost where, boolean dropped) {
		if (m.getType() == MessageType.MESSAGE) {
			super.messageDeleted(m, where, dropped);
		}
	}

	@Override
	public void messageTransferAborted(Message m, DTNHost from, DTNHost to) {
		if (m.getType() == MessageType.MESSAGE) {
			super.messageTransferAborted(m, from, to);
		}
	}

	@Override
	public void messageTransferred(Message m, DTNHost from, DTNHost to, boolean firstDelivery) {
		if(m.getType() == MessageType.MESSAGE) {
			super.messageTransferred(m, from, to, firstDelivery);
		}

	}

}
