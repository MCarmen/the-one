package report.control.directive;

import core.SimClock;

public class ReceivedDirective{
	/** Directive Identifier */
	private String directiveID; 
	
	/** Node ID this directive has been applied to*/
	private String receivedTo;
	
	/** nrofCopies field in the directive, regarding to the max number of  
	 * message's copies that can be in the network.*/
	protected double nrofCopies;
	
	/**
	 * Simulation time when the directive was applied.
	 */
	private double simTime;

	/**
	 * Creates an ReceivedDirective object specifying the directive ID, the  
	 * node this directive is applied to, and the max copies of the message 
	 * that can be in the network.
	 * @param directiveID
	 * @param appliedToNodeID
	 * @param nrofCopies
	 */
	public ReceivedDirective(String directiveID, String receivedTo, double nrofCopies) {		
		this.directiveID = directiveID;
		this.receivedTo = receivedTo;
		this.nrofCopies = nrofCopies;
		this.simTime = SimClock.getTime();			
	}
	
	public String toString() {
		return String.format("%.2f %s %s %.2f", this.simTime, this.receivedTo, 
				this.directiveID, this.nrofCopies);
	}
}