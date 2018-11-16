/**
 * 
 */
package input.control;

import java.util.ArrayList;
import java.util.List;

import core.Settings;
import input.ExternalEvent;
import input.MessageCreateEvent;
import input.MessageEventGenerator;

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
	 * @param time Time, when the message is created
	 * @return
	 */
	protected abstract ExternalEvent getEvent(int from, int to, String id, 
			double time) ;
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
	public List<ExternalEvent> nextEvents(int[] fromHostRange, int[] toHostRange) {
		int responseSize = 0; /* zero stands for one way messages */
		int msgSize = 0;
		int interval = drawNextEventTimeDiff();

		int to;
		List<ExternalEvent> nextEvents = new ArrayList<ExternalEvent>();

		for(int from = fromHostRange[0]; from < fromHostRange[1]; from++) {
			/* Get two *different* nodes randomly from the toHostRange range */		
			to = drawToAddress(toHostRange, from); 
			nextEvents.add(this.getEvent(from, to, this.getID(), 
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
}
