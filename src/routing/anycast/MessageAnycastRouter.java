package routing.anycast;

import routing.ActiveRouter;
import routing.MessageRouter;
import core.*;


public abstract class MessageAnycastRouter extends MessageRouter{

    /** Anycast groups identifiers -setting id ({@value}). Array of strings.*/
    public static final String ANYCAST_ID_S = "anycastIdentifiers";

    /** Strings for identifying the Anycast groups  */
    private String[] anycastIDs;

    /**
     * Constructor. Creates a new message anycast router based on the settings in
     * the given Settings object. Size of the message buffer is read from
     * {@link #B_SIZE_S} setting. Default value is Integer.MAX_VALUE.
     * @param s The settings object
     */
    public MessageAnycastRouter(Settings s) {
        super(s);

        if (s.contains(ANYCAST_ID_S)) {
            this.anycastIDs = s.getCsvSetting(ANYCAST_ID_S);
        }
    }

    /**
     * Copy constructor.
     * @param r The router prototype where setting values are copied from
     */
    protected MessageAnycastRouter(MessageAnycastRouter r) {
        super(r);
        this.anycastIDs = r.anycastIDs;
    }

    /**
     * Checks if the node is part of an Anycast group (based on its groupID)
     * @param node The node to evaluate
     * @return True if the host belongs to some Anycast group. False otherwise.
     */
    private boolean isAnycastNode(DTNHost node) {
        boolean isAnycast = false;
        String groupID = node.toString().split("\\d")[0];
        for (String identifier : anycastIDs) {
            if (identifier.equals(groupID)) {
                isAnycast = true;
                break;
            }
        }
        return isAnycast;
    }

    /**
     * Returns <code>true</code> if the nodes are part of the same Anycast group
     * (their name, excluding the network address, is the same).
     * @param node1 The first node of the comparison
     * @param node2 The second node of the comparison
     * @return True if the nodes are part of the same Anycast group (they start with the same prefix), false otherwise.
     */
    protected boolean anycastCompare(DTNHost node1, DTNHost node2) {
        return (this.isAnycastNode(node1) && this.isAnycastNode(node2) &&
                node1.toString().split("\\d")[0].equals(node2.toString().split("\\d")[0]));
    }

    /**
     * This method should be called (on the receiving host) after a message
     * was successfully transferred. The transferred message is put to the
     * message buffer unless this host is the final recipient of the message.
     * @param id Id of the transferred message
     * @param from Host the message was from (previous hop)
     * @return The message that this host received
     */
    public Message messageTransferred(String id, DTNHost from) {
        Message incoming = removeFromIncomingBuffer(id, from);
        boolean isFinalRecipient;
        boolean isFirstDelivery; // is this first delivered instance of the msg


        if (incoming == null) {
            throw new SimError("No message with ID " + id + " in the incoming "+
                    "buffer of " + this.host);
        }

        incoming.setReceiveTime(SimClock.getTime());

        // Pass the message to the application (if any) and get outgoing message
        Message outgoing = incoming;
        for (Application app : getApplications(incoming.getAppID())) {
            // Note that the order of applications is significant
            // since the next one gets the output of the previous.
            outgoing = app.handle(outgoing, this.host);
            if (outgoing == null) break; // Some app wanted to drop the message
        }

        Message aMessage = (outgoing==null)?(incoming):(outgoing);
        // If the application re-targets the message (changes 'to')
        // then the message is not considered as 'delivered' to this host.
        isFinalRecipient = ((aMessage.getTo() == this.host) || this.anycastCompare(aMessage.getTo(), getHost()));
        isFirstDelivery = isFinalRecipient &&
                !isDeliveredMessage(aMessage);

        if (!isFinalRecipient && outgoing!=null) {
            // not the final recipient and app doesn't want to drop the message
            // -> put to buffer
            addToMessages(aMessage, false);
        } else if (isFirstDelivery) {
            this.deliveredMessages.put(id, aMessage);
        } else if (outgoing == null) {
            // Blacklist messages that an app wants to drop.
            // Otherwise the peer will just try to send it back again.
            this.blacklistedMessages.put(id, null);
        }

        for (MessageListener ml : this.mListeners) {
            ml.messageTransferred(aMessage, from, this.host,
                    isFirstDelivery);
        }

        return aMessage;
    }
}
