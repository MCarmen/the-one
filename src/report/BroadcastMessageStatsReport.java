package report;

import core.BroadcastMessage;
import report.control.TControlMessageStatsReport;

/**
 * Report for generating different kind of total statistics relaying performance 
 * regarding broadcast messages. 
 * Messages that were created during the warm up period
 * are ignored.
 * <P><strong>Note:</strong> if some statistics could not be created (e.g.
 * overhead ratio if no messages were delivered) "NaN" is reported for
 * double values and zero for integer median(s).
 */
public class BroadcastMessageStatsReport extends TControlMessageStatsReport {

	public BroadcastMessageStatsReport(){
		this.init(BroadcastMessage.class);
	}
}
