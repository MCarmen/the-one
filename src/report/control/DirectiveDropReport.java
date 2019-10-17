package report.control;

import core.Message.MessageType;
import report.DropByMsgTypeReport;

public class DirectiveDropReport extends DropByMsgTypeReport {
	@Override
	protected void init() {
		// TODO Auto-generated method stub
		super.init(MessageType.DIRECTIVE);
	}
}
