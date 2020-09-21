package report.control;

import core.Message.MessageType;

/**
 * Report for generating different kind of total statistics about 
 * broadcast messages relaying performance. Messages that were created during 
 * the warm up period are ignored.
 * <P><strong>Note:</strong> if some statistics could not be created (e.g.
 * overhead ratio if no messages were delivered) "NaN" is reported for
 * double values and zero for integer median(s).
 */
public class BroadcastMessageStatsReport extends StandardMessageStatsReport {

	public BroadcastMessageStatsReport() {
		super(MessageType.MESSAGE_BROADCAST);
		// TODO Auto-generated constructor stub
	}
	

}
