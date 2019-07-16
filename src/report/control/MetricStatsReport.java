package report.control;

import core.control.MetricMessage;
import report.TMessageStatsReport;

public class MetricStatsReport extends TMessageStatsReport {

	public MetricStatsReport(){
		this.init(MetricMessage.class);
	}
}
