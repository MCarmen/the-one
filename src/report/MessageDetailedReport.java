package report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import core.DTNHost;
import core.Message;
import core.MessageListener;

/**
 * Report for generating different kind of statistics per message about message
 * relaying performance. Messages that were created during the warm up period
 * are ignored.
 * <P><strong>Note:</strong> if some statistics could not be created (e.g.
 * overhead ratio if no messages were delivered) "NaN" is reported for
 * double values and zero for integer median(s).
 */
public class MessageDetailedReport extends Report implements MessageListener{
	private HashMap<String, MessageStatistics> messageStats = new HashMap<>();
	public MessageDetailedReport() {
		init();
	}
	
	@Override
	protected void init() {
		super.init();
	}

	@Override
	public void newMessage(Message m) {
		if (isWarmup()) {
			addWarmupID(m.getId());
			return;
		}
		if(this.isWarmDown()) {
			this.addWarmDownID(m.getId());
			return;
		}
		
		MessageStatistics msgStats = new MessageStatistics(m.getFrom().getAddress(), 
				m.getTo().getAddress(), this.getSimTime());
		this.messageStats.put(m.getId(), msgStats);
						
	}

	@Override
	public void messageTransferStarted(Message m, DTNHost from, DTNHost to) {
		
	}

	@Override
	public void messageDeleted(Message m, DTNHost where, boolean dropped) {
		if (isWarmupID(m.getId()) || isWarmDownID(m.getId())) {
			return;
		}
		
		MessageStatistics msgStats = this.messageStats.get(m.getId());
		if (dropped) {			
			msgStats.hasBeenDropped = true;
		}
		else {
			msgStats.hasBeenRemoved = true;
		}

		double bufferTime = Double.parseDouble(String.format(".2f", getSimTime() - m.getReceiveTime()));
		msgStats.msgBufferTime.add(bufferTime);
		
	}

	@Override
	public void messageTransferAborted(Message m, DTNHost from, DTNHost to) {
		// TODO Auto-generated method stub
		
		if (isWarmupID(m.getId()) || isWarmDownID(m.getId())) {
			return;
		}

		this.messageStats.get(m.getId()).hasBeenAborted = true;		
	}

	@Override
	public void messageTransferred(Message m, DTNHost from, DTNHost to, boolean finalTarget) {
		if (isWarmupID(m.getId()) || isWarmDownID(m.getId())) {
			return;
		}
		MessageStatistics msgStats = this.messageStats.get(m.getId());
		List<Integer> originDestTuple = new ArrayList<>(2);
		originDestTuple.add(0, from.getAddress());
		originDestTuple.add(1, to.getAddress());
		msgStats.hops.add(originDestTuple);

		if (finalTarget) {
			msgStats.latency = this.getSimTime() - msgStats.creationTime;
			msgStats.hasBeenDelivered = true;
			if (m.isResponse()) {
				msgStats.isAResponse = true;
				msgStats.rtt = getSimTime() -	m.getRequest().getCreationTime();
			}
		}
	}
	
	@Override
	public void done() {
		this.write(String.format("Message stats for scenario  %s: id | creationTime | origin | destination | "+
		" latency | delivered | dropped | removed | aborted | isResponse | rtt | hops | bufferTime", 
				this.getScenarioName()));
		
		for(Entry<String, MessageStatistics>msgStatsEntry : this.messageStats.entrySet()) {
			this.write(String.format("%s %s", msgStatsEntry.getKey(), msgStatsEntry.getValue()));
		}
		super.done();
	}
	
	/**
	 * Class that wraps all the statistics measured per message.
	 *
	 */
	public class MessageStatistics {
		private int origin;
		private int destination;		
		private double creationTime;		
		private double latency;
		private List<List<Integer>> hops = new ArrayList<>();
		private List<Double> msgBufferTime = new ArrayList<>();
		private double rtt; // round trip time
		private boolean hasBeenDropped;
		private boolean hasBeenRemoved;
		private boolean hasBeenAborted;
		private boolean hasBeenDelivered;
		private boolean isAResponse;

		public MessageStatistics() {
		}
		
		

		public MessageStatistics(int origin, int destination, double creationTime) {
			this.origin = origin;
			this.destination = destination;
			this.creationTime = creationTime;
		}



		public String toString() {

			return String.format("%.1f %d %d %.1f %b %b %b %b %b %.1f %s %s", this.creationTime, this.origin, this.destination, this.latency,
					this.hasBeenDelivered, this.hasBeenDropped, this.hasBeenRemoved, this.hasBeenAborted,
					this.isAResponse, this.rtt, this.hops, this.msgBufferTime);

		}
	}

}
