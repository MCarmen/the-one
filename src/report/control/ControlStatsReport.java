package report.control;

import core.control.ControlMessage;
import report.TMessageStatsReport;

public class ControlStatsReport extends TMessageStatsReport{

	public ControlStatsReport(){
		this.init(ControlMessage.class);
	}
}
