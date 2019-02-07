package report;

import core.Message;
import routing.SprayAndWaitRouter;

/**
 * See {@link report.CreatedMessagesReport CreatedMessagesReport}. This 
 * class also reports some of the message fields content. 
 *
 */
public class CreatedMessagesBySprayAndWaitRouterReport extends CreatedMessagesReport {
	
	@Override
	public void init() {
		CreatedMessagesReport.HEADER += " initialNrofCopies";
		super.init();
	}
	
	@Override
	public void newMessage(Message m) {
		if (isWarmup()) {
			return;
		}

		int ttl = m.getTtl();
		write(format(getSimTime()) + " " + m.getId() + " " +
				m.getSize() + " " + m.getFrom() + " " + m.getTo() + " " +
				(ttl != Integer.MAX_VALUE ? ttl : "n/a") +
				(m.isResponse() ? " Y " : " N ") + 
				m.getProperty(SprayAndWaitRouter.MSG_COUNT_PROPERTY));
	}
	
}
