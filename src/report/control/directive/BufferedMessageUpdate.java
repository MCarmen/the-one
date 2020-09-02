package report.control.directive;

import java.util.ArrayList;
import java.util.List;

import core.Message;

/**
 * Class that stores in a list the new value of the msg property MSG_COUNT_PROPERTY 
 * (L) applied to the messages in the buffer after applying a received directive. 
 *
 */
public class BufferedMessageUpdate {
	/* List with all the updates applied to the buffered messages. */
	private List<MessageUpdate> messageUpdate = new ArrayList<MessageUpdate>();
	private ReceivedDirective receivedDirective;
	

	public BufferedMessageUpdate(ReceivedDirective receivedDirective) {
		this.receivedDirective = receivedDirective;
	}

	/**
	 * Method that adds in the modifications list a msg modification.
	 * @param msg the buffered Message
	 * @param newNrofCopies the new value of the msg field MSG_COUNT_PROPERTY
	 * after applying a directive.
	 */
	public void addUpdate(Message msg, int currentNrofCopies, int newNrofCopies) {
		MessageUpdate msgUpdate = new MessageUpdate(msg, currentNrofCopies, newNrofCopies);
		this.messageUpdate.add(msgUpdate);
	}
	
	public String toString() {
		String msgsUpdatesStr = "";
		for(MessageUpdate msgUpdate : this.messageUpdate) {
			msgsUpdatesStr += String.format("%s\n", msgUpdate);
		}
		return String.format("%s [%s]", this.receivedDirective.toString(), msgsUpdatesStr);		
	}
	
	/**
	 * Class that encapsulates a change of the value of the message field MSG_COUNT_PROPERTY
	 */
	private static class MessageUpdate {
		private Message msg;
		private int currentNrofCopies;
		private int newNrofCopies;
		
		public MessageUpdate(Message msg, int currentNrofCopies, int newNrofCopies) {
			this.msg = msg;
			this.currentNrofCopies = currentNrofCopies;
			this.newNrofCopies = newNrofCopies;
		}
		
		public String toString() {
			return String.format("{msgID: %s, hops: %d (%s), L: %d, newL: %d}", this.msg.getId(), this.msg.getHopCount(), this.msg.getHops(),
					this.currentNrofCopies, this.newNrofCopies);
		}
	}
	
}
