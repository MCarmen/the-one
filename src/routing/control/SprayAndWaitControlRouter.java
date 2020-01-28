/**
 * 
 */
package routing.control;

import java.util.ArrayList;
import java.util.List;

import core.Connection;
import core.DTNHost;
import core.Message;
import core.Settings;
import core.control.DirectiveCode;
import routing.SprayAndWaitRouter;

/**
 * Implementation of a routing algorithm that for data messages acts as the 
 * Spray and wait router depicted in <I>Spray and Wait: An Efficient Routing 
 * Scheme for Intermittently Connected Mobile Networks</I> by 
 * Thrasyvoulos Spyropoulus et al. and as an epidemic one for control messages. 
 *
 */
public class SprayAndWaitControlRouter extends SprayAndWaitRouter {

	/** SprayAndWaitControl router's settings name space ({@value})*/
	public static final String SPRAYANDWAITCONTROL_NS = "SprayAndWaitControlRouter";
	
	public SprayAndWaitControlRouter(Settings s) {
		super(s);
	}
	
	/**
	 * Copy constructor.
	 * @param r The router prototype where setting values are copied from
	 */
	protected SprayAndWaitControlRouter(SprayAndWaitControlRouter r) {
		super(r);		
	}	
	
	@Override
	protected void setSettings() {
		this.snwSettings = new Settings(SPRAYANDWAITCONTROL_NS);
	}
	
	@Override
	/**
	 * If the msg transferred is a control one it is transferred configured with 
	 * one copy of the message in the node being transferred.
	 */
	public Message messageTransferred(String id, DTNHost from) {
		Message msg = super.messageTransferred(id, from);
		Integer nrofCopies = (Integer)msg.getProperty(MSG_COUNT_PROPERTY);

		assert nrofCopies != null : "Not a SnW message: " + msg;

		/* If it is a control msg the receiving node gets only single copy */
		if (msg.isControlMsg()) {
			nrofCopies = 1;
		} else if (isBinary) {
			/* in binary S'n'W the receiving node gets floor(n/2) copies */
			nrofCopies = (int) Math.floor(nrofCopies / 2.0);
		} else {
			/* in standard S'n'W the receiving node gets only single copy */
			nrofCopies = 1;
		}

		msg.updateProperty(MSG_COUNT_PROPERTY, nrofCopies);
		return msg;
	}

	@Override
	/**
	 * Control messages are configured with one msg copy.
	 */
	public boolean createNewMessage(Message msg) {
		boolean messageCreated = super.createNewMessage(msg);
		int msgCountPropertyValue = (msg.isControlMsg()) ? 1 :
				this.routingProperties.get(SprayAndWaitRoutingPropertyMap.MSG_COUNT_PROPERTY);						
		msg.addProperty(MSG_COUNT_PROPERTY, msgCountPropertyValue);
		return messageCreated;
	}
	
	@Override
	/**
	 * Control messages aren't limited by number of copies. They always
	 * have just one copy.
	 */
	protected List<Message> getMessagesWithCopiesLeft() {
		List<Message> list = new ArrayList<Message>();

		for (Message m : getMessageCollection()) {
			Integer nrofCopies = (Integer)m.getProperty(MSG_COUNT_PROPERTY);
			assert nrofCopies != null : "SnW message " + m + " didn't have " +
				"nrof copies property!";
			if ((nrofCopies > 1) || (m.isControlMsg()) )  {
				list.add(m);
			}
		}

		return list;
	}
	
	@Override
	/**
	 * After a control msg is transfered it keeps always a copy of the msg.
	 */
	protected void transferDone(Connection con) {
		Integer nrofCopies;
		String msgId = con.getMessage().getId();
		/* get this router's copy of the message */
		Message msg = getMessage(msgId);

		if (msg == null) { // message has been dropped from the buffer after..
			return; // ..start of transfer -> no need to reduce amount of copies
		}

		/* reduce the amount of copies left if the msg is not a control one */
		if (!msg.isControlMsg()) {

			nrofCopies = (Integer) msg.getProperty(MSG_COUNT_PROPERTY);
			if (isBinary) {
				/* in binary S'n'W the sending node keeps ceil(n/2) copies */
				nrofCopies = (int) Math.ceil(nrofCopies / 2.0);
			} else {
				nrofCopies--;
			}
			msg.updateProperty(MSG_COUNT_PROPERTY, nrofCopies);
		}
	}

	@Override
	/**
	 * Directives just apply to data messages. 
	 */
	protected void applyDirective(Message message) {
		if (message.containsProperty​(DirectiveCode.NROF_COPIES_CODE.toString()) && 
			!message.isControlMsg()) {
			super.applyDirective(message);
			this.routingProperties.put(SprayAndWaitRoutingPropertyMap.MSG_COUNT_PROPERTY, (Integer)(message.getProperty(DirectiveCode.NROF_COPIES_CODE.toString())));
		}
	}

}
