package input;

import core.Settings;

/**
 * Broadcast message creation -external events generator. Creates uniformly distributed
 * message creation patterns whose message size and inter-message intervals can
 * be configured.
 */
public class BroadcastMessageEventGenerator extends MessageEventGenerator {

	public BroadcastMessageEventGenerator(Settings s) {
		super(s);
	}

	/**
	 * Returns the next message creation event
	 * @see input.EventQueue#nextEvent()
	 */
	public ExternalEvent nextEvent() {
		int responseSize = 0; /* zero stands for one way messages */
		int msgSize;
		int interval;
		int from;
		int to;

		/* Get a from node randomly from the fromHost range */
		from = drawHostAddress(this.hostRange);
		/* Get whatever host from the toHost range, as it is not going to be 
		 * used, as it is a broadcast message.*/
		to = this.drawToAddress(this.toHostRange, from);

		msgSize = drawMessageSize();
		interval = drawNextEventTimeDiff();

		/* Create event and advance to next event */
		MessageCreateEvent mce = new BroadcastMessageCreateEvent(from, to, this.getID(),
				msgSize, responseSize, this.nextEventsTime);
		this.nextEventsTime += interval;

		if (this.msgTime != null && this.nextEventsTime > this.msgTime[1]) {
			/* next event would be later than the end time */
			this.nextEventsTime = Double.MAX_VALUE;
		}

		return mce;
	}
}
