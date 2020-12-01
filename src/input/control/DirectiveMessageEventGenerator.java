package input.control;

import core.Settings;
import core.control.DirectiveMessage;
import input.ExternalEvent;

/**
 * Directive Message creation -external events generator. Creates uniformly 
 * distributed message creation patterns whose message size and 
 * inter-message intervals can be configured.
 */
public class DirectiveMessageEventGenerator extends ControlMessageEventGenerator {

	public DirectiveMessageEventGenerator(Settings s) {
		super(s);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected ExternalEvent getEvent(int from, int to, String id, int size, double time) {
		// TODO Auto-generated method stub
		return new DirectiveMessageCreateEvent(from, to, id, size, time);
	}	
	
	@Override
	protected String getID() {
		return DirectiveMessage.nextId();
	}
	
}

