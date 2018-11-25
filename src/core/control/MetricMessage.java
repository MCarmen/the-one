/**
 * 
 */
package core.control;

import core.DTNHost;

/**
 * A metric message that is created at a node or passed between nodes.
 */
public class MetricMessage extends ControlMessage {
	/**
	 * Creates a new MetricMessage.
	 * @param from Who the message is (originally) from
	 * @param to Who the message is (originally) to
	 * @param id Message identifier (must be unique for message but
	 * 	will be the same for all replicates of the message)
	 * @param size Size of the message (in bytes)
	 */
	public MetricMessage(DTNHost from, DTNHost to, String id, int size) {
		super(from, to, id, size);
		// TODO Auto-generated constructor stub
		this.type = MessageType.METRIC;		
	}

}
