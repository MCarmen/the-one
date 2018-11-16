package input.control;

import java.util.List;

import core.Settings;
import input.ExternalEvent;
import input.MessageCreateEvent;
import input.MessageEventGenerator;

/**
 * Metric Message creation -external events generator. Creates uniformly 
 * distributed message creation patterns whose message size and 
 * inter-message intervals can be configured.
 */
public class MetricMessageEventGenerator extends ControlMessageEventGenerator {

	public MetricMessageEventGenerator(Settings s) {
		super(s);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected ExternalEvent getEvent(int from, int to, String id, double time) {
		// TODO Auto-generated method stub
		return new MetricMessageCreateEvent(from, to, id, time);
	}	
	
}
