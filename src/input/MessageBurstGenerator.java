/*
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details.
 */
package input;

import java.util.ArrayList;

import core.Settings;

/**
 * Message creation -external events generator. Creates bursts of messages where
 * every source node (defined with {@link MessageEventGenerator#HOST_RANGE_S})
 * creates a new message to every destination node (defined with
 * {@link MessageEventGenerator#TO_HOST_RANGE_S})on every interval.
 * The message size, burst times, and inter-burst intervals can be configured
 * like with {@link MessageEventGenerator}.
 * @see MessageEventGenerator
 */
public class MessageBurstGenerator extends MessageEventGenerator {
	/** next index to use from the "from" range */
	private int nextFromOffset;
	private int nextToOffset;

	public MessageBurstGenerator(Settings s) {
		super(s);
		this.nextFromOffset = 0;
		this.nextToOffset = 0;

		if (this.toHostRange == null) {
			this.toHostRange = this.hostRange;
		}
	}

	/**
	 * Returns the next message creation event
	 * @see input.EventQueue#nextEvent()
	 */
	public ExternalEvent nextEvent() {
		int responseSize = 0; /* no responses requested */
		int msgSize;
		int interval;
		int from;
		int to;
		boolean nextBurst = false;

		ArrayList<Integer> allfromHostsAddresses = this.getAllHostsAddresses(this.hostRange);
		ArrayList<Integer> allToHostsAddresses = this.getAllHostsAddresses(this.toHostRange);

		from = allfromHostsAddresses.get(nextFromOffset);
		to = allToHostsAddresses.get(nextToOffset);

		if (to == from) { /* skip self */
			to = allToHostsAddresses.get(++nextToOffset);
		}

		msgSize = drawMessageSize();
		MessageCreateEvent mce = new MessageCreateEvent(from, to, getID(), msgSize, responseSize, this.nextEventsTime);
		if (this.nextToOffset < allToHostsAddresses.size() - 1) {
			this.nextToOffset++;
		} else if (this.nextFromOffset < allfromHostsAddresses.size() - 1) {
			this.nextFromOffset++;
			this.nextToOffset = 0;
		} else {
			/* TODO: doesn't work correctly with non-aligned ranges */			
			nextBurst = true;
		}

		if (nextBurst) {
			interval = drawNextEventTimeDiff();
			this.nextEventsTime += interval;
			this.nextFromOffset = 0;
			this.nextToOffset = 0;
		}

		if (this.msgTime != null && this.nextEventsTime > this.msgTime[1]) {
			/* next event would be later than the end time */
			this.nextEventsTime = Double.MAX_VALUE;
		}

		return mce;
	}

}
