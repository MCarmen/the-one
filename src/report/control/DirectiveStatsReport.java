package report.control;

import core.control.DirectiveMessage;
import report.TMessageStatsReport;

public class DirectiveStatsReport extends TMessageStatsReport{

	public DirectiveStatsReport(){
		this.init(DirectiveMessage.class);
	}

}
