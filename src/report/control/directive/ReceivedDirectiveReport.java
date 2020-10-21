package report.control.directive;

import java.util.ArrayList;
import java.util.List;

import core.DTNHost;
import core.Message;
import core.Message.MessageType;
import core.control.DirectiveCode;
import core.control.listener.DirectiveListener;
import report.Report;

/**
 * Class that reports when a directive has been received.
 * @author mc
 *
 */
public class ReceivedDirectiveReport extends Report implements DirectiveListener {
	/** List with all the received directives reported. */
	private List<ReceivedDirective> receivedDirectives = new ArrayList<ReceivedDirective>();
	
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
	public void directiveCreated(DirectiveDetails directiveDetails) {
		// TODO Auto-generated method stub

	}

	@Override
	public void directiveAppliedToBufferedMessages(BufferedMessageUpdate msgsUpdates) {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void directiveReceived(Message m, DTNHost to) {
		// TODO Auto-generated method stub
		if(m.getType() == MessageType.DIRECTIVE) {
			int nrofCopies = (int)m.getProperty(DirectiveCode.NROF_COPIES_CODE);
			ReceivedDirective receivedDirective = 
					new ReceivedDirective(m.getId(), to.toString(), nrofCopies); 
			this.receivedDirectives.add(receivedDirective);
		}
	}
		
	@Override
	public void done() {
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append(("Received directives for scenario " + getScenarioName() +
				"\nsim_time: " + format(getSimTime())));
		strBuilder.append("\nSimTime | Received by | Directive ID | nrofCopies \n");
		for(ReceivedDirective receivedDirective : this.receivedDirectives) {
			strBuilder.append(receivedDirective + "\n");
		}
		this.write(strBuilder.toString());
		super.done();
	}

}
