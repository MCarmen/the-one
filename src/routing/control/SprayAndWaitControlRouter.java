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
import core.control.ControlMessage;
import core.control.DirectiveCode;
import core.control.DirectiveMessage;
import core.control.MetricMessage;
import report.control.directive.BufferedMessageUpdate;
import report.control.directive.DirectiveDetails;
import report.control.directive.ReceivedDirective;
import report.control.metric.MetricDetails;
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
	
	private DirectiveMessage lastAppliedDirective = null;
	
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
	public void applyDirective(Message message) {
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
	
	/**
	 * Method that checks whether the router has applied any directive during the 
	 * simulation.
	 * @return <code>True</code> if a directive has been aplied during the simulation.
	 * <code>false</code> otherwise.
	 */
	private boolean hasADirectiveBeenApplied() {
		return (this.lastAppliedDirective != null);
	}
	
	/**
	 * Public method that checks if a directive has been applied, which means 
	 * that the router is carrying a directive. 
	 * @return <code>True</code> if the router is carrying a directive.
	 * <code>false</code> otherwise.
	 */
	public boolean doesCarryADirective(){
		return this.hasADirectiveBeenApplied();
	}
	
	/**
	 * A directive should be applied if no directive has been applied before, or
	 * if the new directive is newer than the last one being applied and if the new 
	 * directive value is different from the last applied value. If the 
	 * directive is to be applied, its timestamp is stored.
	 * @param message The directive message
	 * @return true if the directive has to be applied or false otherwise.
	 */
	private boolean directiveShouldBeApplied(Message message) {
		boolean hasToBeApplied = false;
		int directiveMsgCountValue = (Integer) (message.getProperty(DirectiveCode.NROF_COPIES_CODE.toString())); // L
		int currentDirectiveMsgCountValue = this.routingProperties.get(SprayAndWaitRoutingPropertyMap.MSG_COUNT_PROPERTY);
		
		if(!this.hasADirectiveBeenApplied() || 
				(message.getCreationTime() > this.lastAppliedDirective.getCreationTime() && 
				directiveMsgCountValue != currentDirectiveMsgCountValue) ){
			hasToBeApplied = true;
			this.lastAppliedDirective = (DirectiveMessage)message;			
		}
		return hasToBeApplied;	
	}
	
	
	@Override
	/**
	 * This method is called synchronously through an event.
	 * @param the message to be fulfilled. 
	 */
	public boolean createNewMessage(Message m) {
		boolean msgHasBeenCreated = false;
		
		msgHasBeenCreated = (m.getType() == Message.MessageType.DIRECTIVE)?
				this.createNewDirectiveMessage((DirectiveMessage)m) : (m.getType() == Message.MessageType.METRIC) ?
						this.createNewMetricMessage((MetricMessage)m) :
							super.createNewMessage(m);
				
		return msgHasBeenCreated;
	}
	
	/**
	 * The router delegates the the fulfillment of the message with the
	 * directive to {@link Controller#fillMessageWithDirective(ControlMessage)}.
	 * 
	 * @param msg The directive message to be fulfilled
	 * @return if the controller has a directive to be used to fulfill the 
	 * message being created.
	 */
	public boolean createNewDirectiveMessage(DirectiveMessage msg) {
		DirectiveDetails directiveDetails;
		directiveDetails = this.controller.fillMessageWithDirective(msg);
		boolean msgHasBeenCreated = (directiveDetails != null) ? true : false;
		if (msgHasBeenCreated) {
			this.lastAppliedDirective = msg;
			this.reportDirectiveCreated(directiveDetails);
		}

		return msgHasBeenCreated;
	}

	
	/**
	 * The router delegates the fulfillment of the message, with the metric information, to 
	 * {@link MetricsSensed#fillMessageWithMetric(Message)}. If there is no 
	 * metric available this method returns false.	 
	 * @param msg The message to be filled in.	 
	 * @return A boolean indicating whether the the message has been created or not. 
	 */
	public boolean createNewMetricMessage(MetricMessage msg) {
		return this.createNewMetricMessage(msg, null);
	}
	
	
	/**
	 * The router delegates the fulfillment of the message, with the metric information, to 
	 * {@link MetricsSensed#fillMessageWithMetric(Message)}. If there is no 
	 * metric available this method returns false. 	 
	 * @param msg The message to be filled in.
	 * @param exclude HostId to be excluded from the aggregation.      	
	 * @return A boolean indicating whether the the message has been created or not.
	 */
	public boolean createNewMetricMessage(MetricMessage msg, String exclude) {
		MetricDetails metricDetails = this.metricsSensed.fillMessageWithMetric(msg, this.getFreeBufferSize(), exclude);
		if (this.isControlMsgGeneratedByMeAsAController(msg)) {
			this.controller.addMetric(msg);
		}	
		boolean msgHasBeenCreated = (metricDetails != null) ? true : false;
		if(msgHasBeenCreated) {
			this.reportNewMetric(metricDetails);
		}
		
		return msgHasBeenCreated;
	}
	
	/**
	 * The router creates a MetricMessage with the local congestion reading at this
	 * moment. 
	 * @param msg The message to be fulfilled.
	 */
	public MetricMessage createLocalCongestionMessage(){
		MetricMessage metric = new MetricMessage(this.getHost());
		this.metricsSensed.fillMessageWithLocalCongestion(metric, this.getFreeBufferSize());
		return metric;
	}
	
	
	/**
	 * This method should be called (on the receiving host) after a metric message
	 * was successfully transferred. 
	 * @param The message that the from host receives.
	 */
	public void metricMessageTransferred(MetricMessage metric) {
		if(this.isAController()) {
			this.controller.addMetric(metric);
		}else {
			this.metricsSensed.addReceivedMetric(metric);
		}
	}
	
	/**
	 * This method should be called (on the receiving host) after a directive message
	 * was successfully transferred. 
	 * @param The message that the from host receives.
	 */
	public void directiveMessageTransferred(DirectiveMessage directive) {
		this.reportReceivedDirective(directive);
		if(!this.isAController()) {
			this.applyDirective(directive);	
		}else {
			this.controller.addDirective(directive);
		}
	}
	
	@Override
	public void changedConnection(Connection con) {
		super.changedConnection(con);		

		if (con.isUp()) {
			DTNHost otherHost = con.getOtherNode(getHost());
						
			if (this.doesCarryADirective()) {				
				((SprayAndWaitControlRouter)otherHost.getRouter()).directiveMessageTransferred(this.lastAppliedDirective);
			}
			MetricMessage metric = new MetricMessage(this.getHost()); 
			boolean metricCreated = this.createNewMetricMessage(metric, otherHost.toString());
			if(metricCreated) {
				((SprayAndWaitControlRouter)otherHost.getRouter()).metricMessageTransferred(metric);				
			}
			
		}
	}
	


}
