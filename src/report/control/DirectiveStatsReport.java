package report.control;

import core.control.DirectiveMessage;

public class DirectiveStatsReport extends TControlMessageStatsReport{

	public DirectiveStatsReport(){
		this.init(DirectiveMessage.class);
	}

}
