package report.control;

import core.Message;
import core.control.MetricCode;
import core.control.MetricMessage;
import report.CreatedMessagesReport;
import routing.SprayAndWaitRouter;
import routing.control.MetricsSensed;
import routing.control.metric.CongestionMetricPerWT;

/**
 * Reports information about created metrics. Metrics created during
 * the warm up period are ignored.
 * For output syntax, see {@link #HEADER}.
 */
public class CreatedMetricMessagesReport extends CreatedMessagesReport  {
	@Override
	public void init() {
		CreatedMessagesReport.HEADER = "# time  ID  fromHost  toHost " + 
				"drops windowTime copiesLeft";
		super.init();
	}

	public void newMessage(Message m) {
		if (m instanceof MetricMessage) {
			String report = "";
			if (!isWarmup()) {
				/*
				write(String.format("%.1f %s %d %s %s %s", this.format(this.getSimTime()), m.getId(), m.getSize(),
						m.getFrom(), m.getTo(),  
						((MetricsSensed.DropsPerTime)m.getProperty(MetricCode.DROPS_CODE.toString()) )));
				//(Integer) m.getProperty(SprayAndWaitRouter.MSG_COUNT_PROPERTY),
				  
				 */
				report += String.format("%.1f ", this.getSimTime());
				report += String.format("%s ", m.getId()); 
				report += String.format("%s ", m.getFrom());
				report += String.format("%s ", m.getTo());
				report += String.format("%s ", ((CongestionMetricPerWT)m.getProperty(MetricCode.CONGESTION_CODE.toString())) );
				report += String.format("%d ", (Integer)m.getProperty(SprayAndWaitRouter.MSG_COUNT_PROPERTY));
				write(report);
			}
		}
	}
}
