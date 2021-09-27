package report.control;

import core.DTNHost;
import report.SnapshotReport;
import routing.SprayAndWaitRouter;
import routing.control.SprayAndWaitRoutingPropertyMap;
/**
 * Node that reports the number of copies configured when a message is created 
 * every configurable-amount-of seconds (see {@link #GRANULARITY}).
 */
public class NodeNrofCopiesSnapshotReport extends SnapshotReport {

	@Override
	protected void writeSnapshot(DTNHost host) {
		//format nodeID nrofcopies
		this.write(String.format("%s %d", host, 
				((SprayAndWaitRouter)host.getRouter()).getNrofCopies()));
	}

}
