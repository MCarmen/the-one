package report.control.directive;

import java.util.ArrayList;
import java.util.List;

import core.DTNHost;
import core.Message;
import core.SimClock;
import core.Message.MessageType;
import core.control.DirectiveCode;
import core.control.listener.DirectiveListener;
import report.Report;

public class AppliedDirectiveReport extends Report implements DirectiveListener {
	/** List with all the applied directives reported. */
	private List<AppliedDirective> appliedDirectives;
	
	public AppliedDirectiveReport() {
		this.init();
	}
	
	@Override
	protected void init() {
		super.init();
		this.appliedDirectives = new ArrayList<AppliedDirective>();
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
	public void directiveCreated(DirectiveDetails directiveDetails) {
		// TODO Auto-generated method stub
		
	}	

	@Override
	public void directiveApplied(Message m, DTNHost to) {
		// TODO Auto-generated method stub
		if(m.getType() == MessageType.DIRECTIVE) {
			int nrofCopies = (int)m.getProperty(DirectiveCode.NROF_COPIES_CODE.toString());
			AppliedDirective appliedDirective = 
					new AppliedDirective(m.getId(), to.toString(), nrofCopies); 
			this.appliedDirectives.add(appliedDirective);
		}
	}
		
	@Override
	public void done() {
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append(("Applied directives for scenario " + getScenarioName() +
				"\nsim_time: " + format(getSimTime())));
		strBuilder.append("\nSimTime | Applied to | Directive ID | nrofCopies \n");
		for(AppliedDirective appliedDirective : this.appliedDirectives) {
			strBuilder.append(appliedDirective + "\n");
		}
		this.write(strBuilder.toString());
		super.done();
	}
	
	private static class AppliedDirective{
		/** Directive Identifier */
		private String directiveID; 
		
		/** Node ID this directive has been applied to*/
		private String appliedToNodeID;
		
		/** nrofCopies field in the directive, regarding to the max number of  
		 * message's copies that can be in the network.*/
		private double nrofCopies;
		
		/**
		 * Simulation time when the directive was applied.
		 */
		private double simTime;

		/**
		 * Creates an AppliedDirective object specifying the directive ID, the  
		 * node this directive is applied to, and the max copies of the message 
		 * that can be in the network.
		 * @param directiveID
		 * @param appliedToNodeID
		 * @param nrofCopies
		 */
		public AppliedDirective(String directiveID, String appliedToNodeID, double nrofCopies) {		
			this.directiveID = directiveID;
			this.appliedToNodeID = appliedToNodeID;
			this.nrofCopies = nrofCopies;
			this.simTime = SimClock.getTime();			
		}
		
		public String toString() {
			return String.format("%.2f %s %s %.2f", this.simTime, this.appliedToNodeID, 
					this.directiveID, this.nrofCopies);
		}
	}


}
