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
import report.control.directive.BufferedMessageUpdate;
import report.control.directive.ReceivedDirective;
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
	public SprayAndWaitControlRouter replicate() {
		return new SprayAndWaitControlRouter(this);
	}
	
	@Override
	/**
	 * If the msg transferred is a control one it is transferred configured with 
	 * one copy of the message in the node being transferred.
	 */
	public Message messageTransferred(String id, DTNHost from) {
		Message msg = super.messageTransferred(id, from);
		this.updateNrOfCopies(msg);
		
		return msg;
	}

	@Override
	protected void updateNrOfCopies(Message msg) {
		/* If it is a control msg the receiving node gets only single copy */
		if (msg.isControlMsg()) {
			msg.updateProperty(MSG_COUNT_PROPERTY, 1);
		} else {
			super.updateNrOfCopies(msg);
		}
	}
	
	@Override
	/**
	 * Control messages are configured with one msg copy.
	 */
	protected void setMsgCountProperty(Message msg) {
		int msgCountPropertyValue = (msg.isControlMsg()) ? 1 :
			this.routingProperties.get(SprayAndWaitRoutingPropertyMap.MSG_COUNT_PROPERTY);						
		msg.addProperty(MSG_COUNT_PROPERTY, msgCountPropertyValue);
	}
	
	@Override
	/**
	 * Control messages aren't limited by number of copies. They always
	 * have just one copy.
	 */
	protected List<Message> getMessagesWithCopiesLeft() {
		List<Message> list = new ArrayList<Message>();

		for (Message m : getMessageCollection()) {
			Integer nrofCopies = (Integer)(m.getProperty(MSG_COUNT_PROPERTY));
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
		String msgId = con.getMessage().getId();
		/* get this router's copy of the message */
		Message msg = getMessage(msgId);

		if (msg == null) { // message has been dropped from the buffer after..
			return; // ..start of transfer -> no need to reduce amount of copies
		}

		/* reduce the amount of copies left if the msg is not a control one */
		if (!msg.isControlMsg()) {
			super.transferDone(con);
		}
	}
	
	@Override
	/**
	 * If the message is of type message, it is direct delivery if the 
	 * m.to == connectionHostPeer.
	 * If the message is of type directive, it has always to be direct delivered.
	 * If the message is of type metric, if the connectionHostPeer is a controller, 
	 * it has always to be delivered.
	 * 
	 * m.getTo = con.getOtherNode
	 * @param m The message to be direct delivered.
	 * @param connectionHostPeer the connection host peer.
	 * @return true if the message can be delivered directly of false otherwise.
	 */
	protected boolean isADirectDeliveryMessageForConnection(Message m, DTNHost connectionHostPeer) {
		boolean isADirectDelivery = false;
		
		switch(m.getType()) {
		case DIRECTIVE:
			isADirectDelivery = true;
			break;
		case METRIC:
			if (connectionHostPeer.getRouter().isAController()) {
				isADirectDelivery = true;
			}
			break;
		default:
			isADirectDelivery = super.isADirectDeliveryMessageForConnection(m, connectionHostPeer);						
		}
		return isADirectDelivery;
	}

	@Override
	/**
	 * See {@link router.MessageRouter.applyDirective}. When a directive arrives the router modifies the
	 * routingProperties map, changing the entry MSG_COUNT_PROPERTY with the new L encapsulated in the received
	 * directive. This affects the nrofcopies of the new created data messages. It also affects the buffered 
	 * data messages. The nrofcopies (L) of all the data messages in the queue will be reviewed following
	 * this algorithm:
	 * If L_msg > L_directive -> L_message = L_directive
	 * If L_msg < L_directive:
	 * We take into account the number of nodes the message has been through except the current one: n_nodes.
	 * We calculate which would be the current L if the L_directive would be been applied when this
	 * msg was created:
	 * L_current = L_directive/2^(n_nodes).
	 * @param message The received directive.
	 */
	protected void applyDirective(Message message) {
		if (message.containsProperty​(DirectiveCode.NROF_COPIES_CODE.toString())) {
			int directiveMsgCountValue = (Integer)(message.getProperty(DirectiveCode.NROF_COPIES_CODE.toString())); //L
			this.routingProperties.put(SprayAndWaitRoutingPropertyMap.MSG_COUNT_PROPERTY, directiveMsgCountValue);
			
			BufferedMessageUpdate messagesUpdates = new BufferedMessageUpdate(
					new ReceivedDirective(message.getId(), this.getHost().toString(), directiveMsgCountValue));
			int newMsgCountValue;
			
			for (Message msg : this.getMessageCollection()) {
				if (!msg.isControlMsg() && msg.containsProperty​(SprayAndWaitControlRouter.MSG_COUNT_PROPERTY)) {
					int currentNrofCopies = (int) msg.getProperty(SprayAndWaitControlRouter.MSG_COUNT_PROPERTY);
					/*
					newMsgCountValue = (currentMsgCount > directiveMsgCountValue) ? directiveMsgCountValue
							: (currentMsgCount < directiveMsgCountValue)
									? (int) Math.round(directiveMsgCountValue / Math.pow(2, msg.getHopCount()))
									: directiveMsgCountValue;
					newMsgCountValue = Math.max(newMsgCountValue, 1);				
					messagesUpdates.addUpdate(msg, currentMsgCount, newMsgCountValue);									
					*/
					/*
					if (currentMsgCount > directiveMsgCountValue) {
						newMsgCountValue = directiveMsgCountValue;
						messagesUpdates.addUpdate(msg, currentMsgCount, newMsgCountValue);
						msg.updateProperty(SprayAndWaitControlRouter.MSG_COUNT_PROPERTY, newMsgCountValue);						
					}
					*/
					messagesUpdates.addUpdate(msg, currentNrofCopies, -1); //DEBUG
				}
			}
			this.reportAppliedDirectiveToBufferedMessages(messagesUpdates);
				
		}
	}

}
