package report.control;

import core.DTNHost;
import core.Message;
import core.MessageListener;
import report.SnapshotReport;

public class NodeSnapshotReport extends SnapshotReport{

	protected boolean isFirstWrite = true;
	
	@Override
	protected void writeSnapshot(DTNHost host) {
		if (isFirstWrite) {
			write("Node_ID | metricsSensed: dropsSensed WindowTime");
			this.isFirstWrite = false;
		}
		// TODO Auto-generated method stub
		write(String.format("%s %s", host, host.getRouter().getMetricsSensed()));

	}
	
}
