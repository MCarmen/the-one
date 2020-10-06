package report.control.directive;

import java.util.ArrayList;
import java.util.List;

import core.DTNHost;
import core.Message;
import core.control.listener.DirectiveListener;
import report.Report;

/**
 * Class that reports the the updates made over the buffered messages after applying the received 
 * directive. 
 * @author mc
 *
 */
public class AppliedDirectiveToBufferedMessagesReport extends Report implements DirectiveListener {
	private List<BufferedMessageUpdate> updatedBufferedMessagesPerNode = new ArrayList<BufferedMessageUpdate>();

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
	public void directiveCreated(DirectiveDetails directiveDetails) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void directiveAppliedToBufferedMessages(BufferedMessageUpdate msgsUpdates) {
		// TODO Auto-generated method stub
		this.updatedBufferedMessagesPerNode.add(msgsUpdates);		
	}
	
	@Override
	public void done() {
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append(("Applied directives for scenario " + getScenarioName() +
				"\nsim_time: " + format(getSimTime())));
		strBuilder.append("\nSimTime | Applied to | Directive ID | nrofCopies | msg | decreaseIter | L | newL \n");
		for(BufferedMessageUpdate msgUpdate : this.updatedBufferedMessagesPerNode) {
			strBuilder.append(String.format("%s\n", msgUpdate));
		}		
		this.write(strBuilder.toString());
		super.done();
	}
}
