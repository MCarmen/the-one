package report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
			msgStats.droppedAt.add(where.getAddress());
		}
		else {
			msgStats.removedAt.add(where.getAddress());
		}

		double bufferTime = Double.parseDouble(String.format(".2f", getSimTime() - m.getReceiveTime()));
		msgStats.msgBufferTime.add(Map.entry(where.getAddress(), bufferTime)); 
		
	}

	@Override
	public void messageTransferAborted(Message m, DTNHost from, DTNHost to) {
		// TODO Auto-generated method stub
		
		if (isWarmupID(m.getId()) || isWarmDownID(m.getId())) {
			return;
		}

		this.messageStats.get(m.getId()).abortedAt.add(to.getAddress());
		
	}

	@Override
	/**
	 * This method is called (on the receiving host) after a message
	 * was successfully transferred. 
	 * @param id Id of the transferred message
	 * @param from Host the message was from (previous hop)
	 * @param to the host this message has been transferred to
	 * @param isFirstDelivery if the 'to' host is the final destination of the 
	 * message and it is the first time the 'to' host has received the message.
	 */
	public void messageTransferred(Message m, DTNHost from, DTNHost to, boolean isFirstDelivery) {
		if (isWarmupID(m.getId()) || isWarmDownID(m.getId())) {
			return;
		}
		MessageStatistics msgStats = this.messageStats.get(m.getId());
		List<Integer> originDestTuple = new ArrayList<>(2);
		originDestTuple.add(0, from.getAddress());
		originDestTuple.add(1, to.getAddress());
		msgStats.replicas.put(m.getUniqueId(), m);
		

		if (isFirstDelivery) {
			MessageStatistics.DeliveredDetails deliveredDetails = 
					msgStats.new DeliveredDetails(to.getAddress(), this.getSimTime() - msgStats.creationTime);
			if (m.isResponse()) {
				deliveredDetails.isAResponse = true;
				deliveredDetails.rtt = getSimTime() -	m.getRequest().getCreationTime(); 		
			}
			msgStats.deliveredTo.add(deliveredDetails);
		}
	}
	
	@Override
	public void done() {
		this.write(String.format("Message stats for scenario  %s: id | creationTime | origin | destination | "+
		" deliveredTo | latency | isResponse | rtt | droppedAt | removedAt | abortedAt | hops | bufferTimeAt", 
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
		private List<Map.Entry<Integer, Double>> msgBufferTime = new ArrayList<>();
		private List<Integer>droppedAt = new ArrayList<>();
		private List<Integer>removedAt = new ArrayList<>();
		private List<Integer>abortedAt = new ArrayList<>();
		private List<DeliveredDetails> deliveredTo = new ArrayList<>();
		private HashMap<Integer, Message> replicas = new HashMap<>();

		public MessageStatistics() {}
				

		public MessageStatistics(int origin, int destination, double creationTime) {
			this.origin = origin;
			this.destination = destination;
			this.creationTime = creationTime;
		}


		public String hopsToString() {
			String hopsPaths = "";
			for(Message replica : this.replicas.values()) {
				hopsPaths += String.format("%s ", replica.getHops());
			}
			return String.format("[%s]", hopsPaths);
		}

		public String toString() {

			return String.format("%.1f | %d | %d | %s | %s | %s | %s | %s | %s", 
					this.creationTime, this.origin, this.destination, 
					this.deliveredTo, this.droppedAt, this.removedAt, 
					this.abortedAt, this.hopsToString(), 
					this.msgBufferTime);

		}
		
		public class DeliveredDetails{
			private int deliveredTo;
			private double latency;
			private boolean isAResponse;
			private double rtt; // round trip time

			public DeliveredDetails(int deliveredTo, double latency) {
				this.deliveredTo = deliveredTo;
				this.latency = latency;
				this.isAResponse = false;
				this.rtt = 0.0;
			}
			
			public String toString() {
				return String.format("[%d %.2f %b %.1f]", this.deliveredTo, 
						this.latency, this.isAResponse, this.rtt);
			}
						
						
		}
	} // end MessageStatistics class

}
