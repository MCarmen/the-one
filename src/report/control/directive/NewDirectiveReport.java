package report.control.directive;

import java.util.ArrayList;
import java.util.List;

import core.DTNHost;
import core.Message;
import core.control.DirectiveCode;
import core.control.listener.DirectiveListener;
import report.Report;

public class NewDirectiveReport extends Report implements DirectiveListener {
	
	/** List of the details of the created directives. */
	private List<DirectiveDetails> newDirectives;
	
	public NewDirectiveReport() {
		this.init();
	}
	
	@Override
	protected void init() {
		super.init();
		this.newDirectives = new ArrayList<DirectiveDetails>();
	}

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
	public void directiveReceived(Message m, DTNHost to) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void directiveAppliedToBufferedMessages(BufferedMessageUpdate msgsUpdates) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void directiveCreated(DirectiveDetails directiveDetails) {
		// TODO Auto-generated method stub
		this.newDirectives.add(directiveDetails);
	}
	
	@Override
	public void done() {
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append("new directives for scenario " + getScenarioName() +
				"\nsim_time: " + format(getSimTime())+"\n");
		strBuilder.append(DirectiveDetails.getHeaderString()+"\n");		
		for(DirectiveDetails directiveDetails : this.newDirectives) {
			strBuilder.append(directiveDetails + "\n");
		}
		this.write(strBuilder.toString());
		super.done();
	}

}
