package report.control.metric;

import java.util.ArrayList;
import java.util.List;

import core.DTNHost;
import core.Message;
import core.control.listener.MetricListener;
import report.Report;

public class NewMetricReport extends Report implements MetricListener {
	
	/** Details of the creation and the content of a metric. */
	List<MetricDetails> newMetricDetails = new ArrayList<>();

	@Override
	public void newMessage(Message m) {
		// TODO Auto-generated method stub

	}

	@Override
	public void messageTransferStarted(Message m, DTNHost from, DTNHost to) {
		// TODO Auto-generated method stub

	}

	@Override
	public void messageDeleted(Message m, DTNHost where, boolean dropped) {
		// TODO Auto-generated method stub

	}

	@Override
	public void messageTransferAborted(Message m, DTNHost from, DTNHost to) {
		// TODO Auto-generated method stub

	}

	@Override
	public void messageTransferred(Message m, DTNHost from, DTNHost to, boolean firstDelivery) {
		// TODO Auto-generated method stub

	}

	@Override
	public void newMetric(MetricDetails metricDetails) {
		this.newMetricDetails.add(metricDetails);
	}
	
	@Override
	public void done() {
		this.write("new metric for scenario " + getScenarioName() +
				"\nsim_time: " + format(getSimTime()));
		this.write(MetricDetails.getHeaderString());	
		for(MetricDetails metricDetails : this.newMetricDetails) {
			this.write(metricDetails.toString("\n"));
		}
		super.done();
	}

}
