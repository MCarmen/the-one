package report.control;

import core.DTNHost;
import core.Message;
import core.MessageListener;
import core.control.DirectiveMessage;

public class DirectiveStatsReport extends ControlStatsReport implements MessageListener {

//	public ControlStatsReport() {
//		// TODO Auto-generated constructor stub
//	}

	@Override
	public void newMessage(Message m) {
		if(m instanceof DirectiveMessage) {
			super.newMessage(m);
		}

	}

	@Override
	public void messageTransferStarted(Message m, DTNHost from, DTNHost to) {
		if(m instanceof DirectiveMessage) {
			super.messageTransferStarted(m, from, to);
		}
	}

	@Override
	public void messageDeleted(Message m, DTNHost where, boolean dropped) {
		if(m instanceof DirectiveMessage) {
			super.messageDeleted(m, where, dropped);
		}
	}

	@Override
	public void messageTransferAborted(Message m, DTNHost from, DTNHost to) {
		if(m instanceof DirectiveMessage) {
			super.messageTransferAborted(m, from, to);
		}
	}

	@Override
	public void messageTransferred(Message m, DTNHost from, DTNHost to, boolean firstDelivery) {
		if(m instanceof DirectiveMessage) {
			super.messageTransferred(m, from, to, firstDelivery);
		}

	}

}
