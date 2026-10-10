package routing.anycast;

import routing.MessageRouter;
import core.DTNHost;
import core.Message;
import core.Settings;


public abstract class MessageAnycastRouter extends MessageRouter{

    /** Anycast groups identifiers -setting id ({@value}). Array of strings.*/
    public static final String ANYCAST_ID_S = "anycastIdentifiers";

    /** Strings for identifying the Anycast groups (none if not set) */
    private String[] anycastIDs = new String[0];

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
        String groupID = node.getGroupId();
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
                node1.getGroupId().equals(node2.getGroupId()));
    }

    /**
     * Returns true if this host is the final recipient of the message: it is
     * the destination of the message or it is in the same Anycast group.
     * @param m The message to check
     * @return True if this host is the final recipient of the message
     */
    @Override
    protected boolean isFinalRecipient(Message m) {
        return super.isFinalRecipient(m) ||
                this.anycastCompare(m.getTo(), getHost());
    }
}
