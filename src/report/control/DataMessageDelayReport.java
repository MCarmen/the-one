package report.control;

import core.DTNHost;
import core.Message;
import report.MessageDelayReport;

/**
 * Reports the data delivered messages' delays (one line per delivered message)
 * and cumulative delivery probability sorted by message delays.
 * Ignores the control messages and the messages that were created during the warm up period
 */
public class DataMessageDelayReport extends MessageDelayReport {
	
	public DataMessageDelayReport() {
		super();
	}
	
	public void newMessage(Message m) {
		if (!m.isControlMsg()) {
			super.newMessage(m);
		}
	}

	public void messageTransferred(Message m, DTNHost from, DTNHost to,
			boolean firstDelivery) {
		if(!m.isControlMsg()) {
			super.messageTransferred(m, from, to, firstDelivery);
		}

	}
}

