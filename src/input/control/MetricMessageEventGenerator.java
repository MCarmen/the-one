package input.control;

import core.Settings;
import input.ExternalEvent;

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
	protected ExternalEvent getEvent(int from, int to, String id, int size, double time) {
		// TODO Auto-generated method stub
		return new MetricMessageCreateEvent(from, to, id, size, time);
	}	
	
}
