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
import core.SimClock;
import core.control.DirectiveCode;
import core.control.DirectiveMessage;
import report.control.directive.BufferedMessageUpdate;
import report.control.directive.ReceivedDirective;
import routing.MessageRouter;
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
	
	private static final int NO_APPLIED_DIRECTIVES = -1;
	
	private double creationTimeOfTheAppliedDirective = NO_APPLIED_DIRECTIVES;
	
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
	
	/**
	 * If the msg transferred is a control one it is transferred configured with 
	 * one copy of the message in the node being transferred.
	 */
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

	
	/**
	 * See {@link router.MessageRouter.applyDirective}. When a directive arrives, 
	 * if it is newer than the previous one being applied, the
	 * router modifies the routingProperties map, changing the entry
	 * MSG_COUNT_PROPERTY with the new L encapsulated in the received directive.
	 * This affects the nrofcopies of the new created data messages. It also affects
	 * the buffered data messages. The nrofcopies (L) of all the data messages in
	 * the queue will be reviewed following this algorithm: We take into account the
	 * times the message has had an encounter and has decremented the message
	 * property nrofCopies by a half. We calculate which would be the newL if the
	 * initial L would have been L_directive and taking into account all the
	 * decrease iterations been done over the message: newL =
	 * L_directive/2^(decrease_iterations).
	 * 
	 * @param message The received directive.
	 */
	protected void applyDirective(Message message) {
		if (message.containsProperty​(DirectiveCode.NROF_COPIES_CODE)) {
			int directiveMsgCountValue = (Integer) (message.getProperty(DirectiveCode.NROF_COPIES_CODE.toString())); // L
			int decreaseIterations;
			int pow;
			boolean isAlive;
			if (directiveShouldBeApplied(message)) {			
				this.routingProperties.put(SprayAndWaitRoutingPropertyMap.MSG_COUNT_PROPERTY, directiveMsgCountValue);
				BufferedMessageUpdate messagesUpdates = new BufferedMessageUpdate(
						new ReceivedDirective(message.getId(), this.getHost().toString(), directiveMsgCountValue));
				int newMsgCountValue;

				for (Message msg : this.getMessageCollection()) {
					if (!msg.isControlMsg() && msg.containsProperty​(SprayAndWaitControlRouter.MSG_COUNT_PROPERTY)
							&& msg.containsProperty​(MSG_PROP_DECREASE_ITERATIONS)) {
						int previousMsgCountValue = (int) msg.getProperty(SprayAndWaitControlRouter.MSG_COUNT_PROPERTY);
						decreaseIterations = (int) msg.getProperty(MSG_PROP_DECREASE_ITERATIONS);
						pow = (int) Math.pow(2, decreaseIterations);
						newMsgCountValue = (int) (directiveMsgCountValue / pow);
						msg.updateProperty(MSG_COUNT_PROPERTY, newMsgCountValue);
						isAlive = (newMsgCountValue < 1) ? false : true;
						msg.updateProperty(MessageRouter.MSG_PROP_ALIVE, isAlive);
						;
						// System.out.println(String.format(
						// "T: %.1f Msg: %s, directive: %d, hist: %s, iter: %d, pow: %d, newL: %d,
						// alive: %b",
						// msg.getCreationTime(), msg.getId(), directiveMsgCountValue,
						// msg.getProperty(MSG_PROP_INIT_L_HISTORY), decreaseIterations, pow,
						// newMsgCountValue,
						// (newMsgCountValue < 1) ? false : true));

						messagesUpdates.addUpdate(msg.getId(), previousMsgCountValue, newMsgCountValue,
								decreaseIterations, isAlive); // DEBUG
					}
				}
				this.reportAppliedDirectiveToBufferedMessages(messagesUpdates);
			}
		}
	}
	
	private boolean hasADirectiveBeenApplied() {
		return (this.creationTimeOfTheAppliedDirective != NO_APPLIED_DIRECTIVES);
	}
	
	/**
	 * A directive should be applied if no directive has been applied before, or
	 * if the new directive is newer than the last one being applied. If the 
	 * directive is to be applied, its timestamp is stored.
	 * @param message The directive message
	 * @return true if the directive has to be applied or false otherwise.
	 */
	private boolean directiveShouldBeApplied(Message message) {
		boolean hasToBeApplied = false;
		
		if(!this.hasADirectiveBeenApplied() || message.getCreationTime() > this.creationTimeOfTheAppliedDirective) {
			hasToBeApplied = true;
			this.creationTimeOfTheAppliedDirective = message.getCreationTime();			
		}
		return hasToBeApplied;	
	}

}
