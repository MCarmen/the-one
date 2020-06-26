/*
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details.
 */
package report;

import java.util.ArrayList;
import java.util.List;

import core.DTNHost;
import core.Message;
import core.MessageListener;

/**
 * Reports delivered messages' delays (one line per delivered message)
 * and cumulative delivery probability sorted by message delays.
 * Ignores the messages that were created during the warm up period.
 */
public class MessageDelayReport extends Report implements MessageListener {
	public static final String HEADER =
	    "# messageDelay  cumulativeDelayAverage cumulativeProbability";
	/** all message delays */
	protected List<Double> delays;
	protected int nrofCreated;

	/**
	 * Constructor.
	 */
	public MessageDelayReport() {
		init();
	}

	@Override
	public void init() {
		super.init();
		write(HEADER);
		this.delays = new ArrayList<Double>();
		this.nrofCreated = 0;
	}

	public void newMessage(Message m) {
		if (isWarmup()) {
			addWarmupID(m.getId());
		}
		else {
			this.nrofCreated++;
		}
	}

	public void messageTransferred(Message m, DTNHost from, DTNHost to,
			boolean firstDelivery) {
		if (firstDelivery && !isWarmupID(m.getId())) {
			this.delays.add(getSimTime() - m.getCreationTime());
		}

	}

	@Override
	public void done() {
		if (delays.size() == 0) {
			write("# no messages delivered in sim time "+format(getSimTime()));
			super.done();
			return;
		}
		double currentDelay = 0;
		double cumProb = 0; // cumulative probability
		double cumDelay = 0; //cumulative delay

		java.util.Collections.sort(delays);

		for (int i=0; i < delays.size(); i++) {
			cumProb += 1.0/nrofCreated;
			currentDelay = delays.get(i);
			cumDelay += currentDelay;		
			write(String.format("%.2f %.2f %.2f", currentDelay, cumDelay/(i+1), cumProb));
		}
		super.done();
	}

	// nothing to implement for the rest
	public void messageDeleted(Message m, DTNHost where, boolean dropped) {}
	public void messageTransferAborted(Message m, DTNHost from, DTNHost to) {}
	public void messageTransferStarted(Message m, DTNHost from, DTNHost to) {}

}
