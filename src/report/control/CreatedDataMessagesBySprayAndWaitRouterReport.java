package report.control;

import core.Message;
import report.CreatedMessagesBySprayAndWaitRouterReport;

/**
 * See {@link report.CreatedDataMessagesBySprayAndWaitRouterReport}. This 
 * class reports just the data messages that have been created, not the control ones. 
 */
public class CreatedDataMessagesBySprayAndWaitRouterReport extends CreatedMessagesBySprayAndWaitRouterReport {
	@Override
	public void newMessage(Message m) {
		if(!m.isControlMsg()) {
			super.newMessage(m);
		}
	}

}
