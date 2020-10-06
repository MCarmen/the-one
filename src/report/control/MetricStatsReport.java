package report.control;

import core.control.MetricMessage;

public class MetricStatsReport extends TControlMessageStatsReport {

	public MetricStatsReport(){
		this.init(MetricMessage.class);
	}
}
