/**
 * 
 */
package input.control;

import java.util.ArrayList;
import java.util.List;

import core.Settings;
import input.ExternalEvent;
import input.MessageEventGenerator;
import util.Range;

/**
 * Super class of Metric and Directive Event Generators
 *
 */
public abstract class ControlMessageEventGenerator extends MessageEventGenerator {
	public ControlMessageEventGenerator(Settings s) {
		super(s);
		// TODO Auto-generated constructor stub
	}
	
	
	/**
	 * Factory method to build an instance of any of the subclasses of 
	 * ControlMessageCreateEvent class.
	 * @param from Who the message is (originally) from
	 * @param to Who the message is (originally) to
	 * @param id Message identifier (must be unique for message but
	 * 	will be the same for all replicates of the message)
	 * @param size Size of the message 
	 * @param time Time, when the message is created
	 * @return
	 */
	protected abstract ExternalEvent getEvent(int from, int to, String id, 
			int size, double time) ;
	/**
	 * Returns a collection with the next control message creation events.
	 * For each one of the hosts in fromHostRange, an event is created.
	 * The destination of each event is picked randomly from the toHostsRange.
	 * @param fromHostRange The first and the last: [first .. last) hosts id's 
	 * that will be the event's from field. The last host specified is not 
	 * considered.
	 * @param toHostRange The first and the last: [fist .. last) hosts id's 
	 * from where to choose to be the event's to field.  
	 * @see input.EventQueue#nextEvent()
	 */
	private List<ExternalEvent> nextEvents(Range[] fromHostRange, Range[] toHostRange) {
		ArrayList<Integer> allFromHostsAddr = this.getAllHostsAddresses(fromHostRange);
		int msgSize = drawMessageSize();
		int interval = drawNextEventTimeDiff();

		int to;
		List<ExternalEvent> nextEvents = new ArrayList<ExternalEvent>();
		for(Integer from : allFromHostsAddr) {			
			/* Get two *different* nodes randomly from the toHostRange range */		
			to = drawToAddress(toHostRange, from); 
			//System.out.println(String.format("From: %d To: %d", from, to) );//DEBUG
			nextEvents.add(this.getEvent(from, to, this.getID(), msgSize,
					this.nextEventsTime));
		}
		/* Advance to the next event  */
		this.nextEventsTime += interval;

		if (this.msgTime != null && this.nextEventsTime > this.msgTime[1]) {
			/* next event would be later than the end time */
			this.nextEventsTime = Double.MAX_VALUE;
		}

		return nextEvents;
	}
	
	/**
	 * Returns a collection with the next control message 
	 * creation events.
	 * @return The next events collection.
	 * @see input.EventQueue#nextEvent() 
	 */
	public List<ExternalEvent> nextEvents(){
		return this.nextEvents(this.hostRange, this.toHostRange);
	}
	
	@Override
	/**
	 * This method needs to be overridden in order to consider when all nodes,
	 * including the controllers, can generate a metric. If the toHosts rang is 
	 * just one node i.e.: [0,1), when the generator generates a ctrl msg 
	 * the configuration could be: from: 0 to 0 as both nodes are in the hosts, 
	 * and toHosts settings. This configuration means the controller generates a 
	 * ctrl msg and himself is the destination. In this situation we need to 
	 * allow the 'from' and 'to' variables have the same value.   
	 */
	protected int drawToAddress(Range[] hostRange, int from) {
		//int to = this.drawHostAddress(hostRange);
		int to;
		if ((hostRange.length == 1) && (hostRange[0].isOneElementRange())) {
			to = (int)hostRange[0].getMin();
		}else {
			to = super.drawToAddress(hostRange, from);
		}
		return to;
	}
}
