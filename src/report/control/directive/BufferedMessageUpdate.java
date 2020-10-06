package report.control.directive;

import java.util.ArrayList;
import java.util.List;

import core.Message;
import routing.MessageRouter;
import routing.SprayAndWaitRouter;
import routing.control.SprayAndWaitControlRouter;

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
	 * @param msgID the Id of the msg.
	 * @param previousNrofCopies the previous value of the msg field MSG_COUNT_PROPERTY
	 * before applying a directive.
	 * @param newNrofCopies the new nrof copies of the buffered message after
	 * applying the directive retroactively.
	 * @param decreaseIterations the number of times this msg has been splitted.
	 * @param isAlive If after applying retroactively the new directive over 
	 * a buffered msg the msg has no copies left this flag is set to false.
	 */	
	public void addUpdate(String msgID, int previousNrofCopies, int newNrofCopies, int decreaseIterations, boolean isAlive) {
		this.messageUpdate.add(new MessageUpdate(msgID, previousNrofCopies, newNrofCopies, decreaseIterations, isAlive));
	}
	
	public String toString() {
		String msgsUpdatesStr = "";
		for(MessageUpdate msgUpdate : this.messageUpdate) {
			msgsUpdatesStr += String.format("%s\n", msgUpdate);
		}
		return String.format("%s [\n%s]", this.receivedDirective.toString(), msgsUpdatesStr);		
	}
	
	/**
	 * Class that encapsulates a change of the value of the message field MSG_COUNT_PROPERTY
	 */
	private static class MessageUpdate {
		private String msgID;
		private int previousNrofCopies;
		private int newNrofCopies;
		private int decreaseIterations;
		private boolean isAlive;

		
		public MessageUpdate(String msgID, int previousNrofCopies, int newNrofCopies, int decreaseIterations, boolean isAlive) {
			this.msgID = msgID;
			this.previousNrofCopies = previousNrofCopies;
			this.newNrofCopies = newNrofCopies;
			this.decreaseIterations = decreaseIterations;
			this.isAlive = isAlive;
		}



		public String toString() {
			return String.format("{msgID: %s, decreaseIter: %d, Prev_L: %d, new_L: %d, isAlive: %b}",
					this.msgID, 
					this.decreaseIterations, 
					this.previousNrofCopies, 
					this.newNrofCopies,
					this.isAlive);
		}
	}
	
}
