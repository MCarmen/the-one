package report.control;

import core.DTNHost;
import core.Message;
import core.MessageListener;
import report.SnapshotReport;
import routing.control.SprayAndWaitControlRouter;

public class NodeSnapshotReport extends SnapshotReport{

	protected boolean isFirstWrite = true;
	
	@Override
	protected void writeSnapshot(DTNHost host) {
		if (isFirstWrite) {
			write("Node_ID | metricsSensedHistory");
			this.isFirstWrite = false;
		}
		// TODO Auto-generated method stub
		write(String.format("%s %s\n\n", host, ((SprayAndWaitControlRouter)host.getRouter()).getMetricsSensed().getHistoryAsString()));

	}
	
}
