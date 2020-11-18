/*
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details.
 */
package routing;

import java.util.ArrayList;
import java.util.List;

import core.Connection;
import core.DTNHost;
import core.Message;
import core.MessageListener;
import core.Settings;
import routing.control.RoutingPropertyMap;
import routing.control.SprayAndWaitRoutingPropertyMap;

/**
 * Implementation of Spray and wait router as depicted in
 * <I>Spray and Wait: An Efficient Routing Scheme for Intermittently
 * Connected Mobile Networks</I> by Thrasyvoulos Spyropoulus et al.
 *
 */
public class SprayAndWaitRouter extends ActiveRouter {
	/** identifier for the initial number of copies setting ({@value})*/
	public static final String NROF_COPIES = "nrofCopies";
	/** identifier for the binary-mode setting ({@value})*/
	public static final String BINARY_MODE = "binaryMode";
	/** SprayAndWait router's settings name space ({@value})*/
	public static final String SPRAYANDWAIT_NS = "SprayAndWaitRouter";
	/** Message property key */
	public static final String MSG_COUNT_PROPERTY = SPRAYANDWAIT_NS + "." +
		"copies";
	
	/** 
	 * Msg property for the number of decreases by nrofcopies/2 applied to 
	 * the message after every contact since the creation of the message.
	 */
	public static final String MSG_PROP_DECREASE_ITERATIONS = "decreaseIterations";

	protected int initialNrofCopies;
	protected boolean isBinary;
	protected Settings snwSettings;
	
	/** Map to be filled by the specific routers with specific routing information*/
	protected RoutingPropertyMap routingProperties;

	public SprayAndWaitRouter(Settings s) {
		super(s);
		this.setSettings();
		initialNrofCopies = this.snwSettings.getInt(NROF_COPIES);		
		isBinary = this.snwSettings.getBoolean( BINARY_MODE);		
	}

	/**
	 * Copy constructor.
	 * @param r The router prototype where setting values are copied from
	 */
	protected SprayAndWaitRouter(SprayAndWaitRouter r) {
		super(r);
		this.initialNrofCopies = r.initialNrofCopies;
		this.isBinary = r.isBinary;
		this.routingProperties = r.routingProperties;		
	}
	
	/**
	 * Method that sets the settings depending on the specific Spray and Wait name 
	 * space. Subclasses of this class might override this method. 
	 */
	protected void setSettings() {
		this.snwSettings = new Settings(SPRAYANDWAIT_NS);
	}
	
	@Override
	public void init(DTNHost host, List<MessageListener> mListeners) {
		super.init(host, mListeners);
		this.routingProperties = new SprayAndWaitRoutingPropertyMap(this);
	}

	@Override
	public int receiveMessage(Message m, DTNHost from) {
		return super.receiveMessage(m, from);
	}

	@Override
	public Message messageTransferred(String id, DTNHost from) {
		Message msg = super.messageTransferred(id, from);
		this.updateNrOfCopies(msg);
		return msg;
	}
	
	/**
	 * Method that updates the number of copies of the message transfered to us. 
	 * Subclasses might need to override this class. 
	 * @param msg the message to be modified with a new number of copies.
	 */
	protected void updateNrOfCopies(Message msg) {
		Integer nrofCopies = (Integer)msg.getProperty(MSG_COUNT_PROPERTY);

		assert nrofCopies != null : "Not a SnW message: " + msg;

		if (isBinary) {
			if (nrofCopies > 1) {
				/* in binary S'n'W the receiving node gets floor(n/2) copies */
				nrofCopies = (int)Math.floor(nrofCopies/2.0);
				this.updateDecreaseIterations(msg);
			}
		}
		else {
			/* in standard S'n'W the receiving node gets only single copy */
			nrofCopies = 1;
		}

		msg.updateProperty(MSG_COUNT_PROPERTY, nrofCopies);
	}

	@Override
	public boolean createNewMessage(Message msg) {
		this.setMsgCountProperty(msg);
		msg.addProperty("Init_L_History", String.format("%d", this.routingProperties.get(SprayAndWaitRoutingPropertyMap.MSG_COUNT_PROPERTY))); //DEBUG
		msg.addProperty(MSG_PROP_DECREASE_ITERATIONS, 0);
		boolean messageCreated = super.createNewMessage(msg);
		return messageCreated;
	}
	
	/**
	 * Method that sets the value of the property MSG_COUNT_PROPERTY in a message. Subclasses should
	 * override this method.
	 * @param msg the message which MSG_COUNT_PROPERTY is to be set
	 */
	protected void setMsgCountProperty(Message msg) {
		int msgCountPropertyValue = 
				this.routingProperties.get(SprayAndWaitRoutingPropertyMap.MSG_COUNT_PROPERTY);						
		msg.addProperty(MSG_COUNT_PROPERTY, msgCountPropertyValue);
	}

	@Override
	public void update() {
		super.update();
		if (!canStartTransfer() || isTransferring()) {
			return; // nothing to transfer or is currently transferring
		}

		/* try messages that could be delivered to final recipient */
		if (exchangeDeliverableMessages() != null) {
			return;
		}

		/* create a list of SAWMessages that have copies left to distribute */
		@SuppressWarnings(value = "unchecked")
		List<Message> copiesLeft = sortByQueueMode(getMessagesWithCopiesLeft());

		if (copiesLeft.size() > 0) {
			/* try to send those messages */
			this.tryMessagesToConnections(copiesLeft, getConnections());
		}
	}

	/**
	 * Creates and returns a list of messages this router is currently
	 * carrying and still has copies left to distribute (nrof copies > 1).
	 * This method might be overriden by subclasses.
	 * @return A list of messages that have copies left
	 */
	protected List<Message> getMessagesWithCopiesLeft() {
		List<Message> list = new ArrayList<Message>();

		for (Message m : getMessageCollection()) {
			Integer nrofCopies = (Integer)m.getProperty(MSG_COUNT_PROPERTY);
			assert nrofCopies != null : "SnW message " + m + " didn't have " +
				"nrof copies property!";
			if (nrofCopies > 1) {
				list.add(m);
			}
		}

		return list;
	}

	/**
	 * Called just before a transfer is finalized (by
	 * {@link ActiveRouter#update()}).
	 * Reduces the number of copies we have left for a message.
	 * In binary Spray and Wait, sending host is left with floor(n/2) copies,
	 * but in standard mode, nrof copies left is reduced by one.
	 * This method might be overriden by subclasses.
	 */
	@Override
	protected void transferDone(Connection con) {
		Integer nrofCopies;
		String msgId = con.getMessage().getId();
		/* get this router's copy of the message */
		Message msg = getMessage(msgId);

		if (msg == null) { // message has been dropped from the buffer after..
			return; // ..start of transfer -> no need to reduce amount of copies
		}

		/* reduce the amount of copies left */ 
		nrofCopies = (Integer)msg.getProperty(MSG_COUNT_PROPERTY);
		if (isBinary) {
			/* in binary S'n'W the sending node keeps floor(n/2) copies */
			if (nrofCopies > 1) {
				nrofCopies = (int)Math.floor(nrofCopies/2.0);	
				this.updateDecreaseIterations(msg);
			}
		}
		else {
			nrofCopies--;
		}
		msg.updateProperty(MSG_COUNT_PROPERTY, nrofCopies);
	}

	@Override
	public SprayAndWaitRouter replicate() {
		return new SprayAndWaitRouter(this);
	}

	public int getInitialNrofCopies() {
		return initialNrofCopies;
	}
	
	protected void updateDecreaseIterations(Message msg){
		if(msg.containsProperty​(MSG_PROP_DECREASE_ITERATIONS)) {
			msg.updateProperty(MSG_PROP_DECREASE_ITERATIONS, (int)msg.getProperty(MSG_PROP_DECREASE_ITERATIONS) + 1);
		}
	}
	
}
