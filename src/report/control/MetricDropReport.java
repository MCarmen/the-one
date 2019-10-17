package report.control;

import core.Message;
import core.Message.MessageType;
import report.DropByMsgTypeReport;

public class MetricDropReport extends DropByMsgTypeReport{
	@Override
	protected void init() {
		// TODO Auto-generated method stub
		super.init(MessageType.METRIC);
	}
}
