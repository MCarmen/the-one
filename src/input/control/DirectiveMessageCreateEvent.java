package input.control;

/**
 * class for external event for creating a directive message.
 */
public class DirectiveMessageCreateEvent extends ControlMessageCreateEvent {

	/**
	 * Creates a directive message creation event without a response request
	 * @param from The creator of the message
	 * @param to Where the message is destined to
	 * @param id ID of the message
	 * @param size Size of the message
	 * @param time Time, when the message is created
	 */
	public DirectiveMessageCreateEvent(int from, int to, String id, double time) {
		super(from, to, id, time);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public String toString() {
		return super.toString() + " [" + fromAddr + "->" + toAddr + "] " +
		"size:" + this.size + " CREATE_DIRECTIVE";
	}

}
