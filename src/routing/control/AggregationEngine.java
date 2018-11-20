package routing.control;

import java.util.ArrayList;
import java.util.List;

import core.control.ControlMessage;

/**
 * Implementation of a Directive Engine {@link  routing.control.DirectiveEngine}.
 * In this implementation, all the metrics added to the list of workingOnMetrics 
 * will be aggregated by calculating the average. After the average is calculated
 * the working list is emptied, ready for adding new metrics. The same applies
 * to the received directives.
 *
 */
public class AggregationEngine implements DirectiveEngine {
	/** List where to add the received metrics to be aggregated */
	private List<ControlMessage> workingOnMetrics;
	
	/** List where to add the received directives to be aggregated */
	private List<ControlMessage> workingOnDirectives;

	/**
	 * Constructor that creates the lists of workingOnMetrics and 
	 * workingOnDirectives.
	 */
	public AggregationEngine() {
		// TODO Auto-generated constructor stub
		this.workingOnMetrics = new ArrayList<ControlMessage>();
		this.workingOnDirectives = new ArrayList<ControlMessage>();
		
	}

	/* (non-Javadoc)
	 * @see routing.control.DirectiveEngine#addMetric(core.control.ControlMessage)
	 */
	@Override
	public void addMetric(ControlMessage metric) {
		this.workingOnMetrics.add(metric);
	}

	/* (non-Javadoc)
	 * @see routing.control.DirectiveEngine#addDirective(core.control.ControlMessage)
	 */
	@Override
	public void addDirective(ControlMessage directive) {
		this.workingOnDirectives.add(directive);
	}

	/** 
	 * If there is at least one metric indicating that there is congestion 
	 * in the network, the engine generates a directive indicating to reduce
	 * the copies of a message in the network by half of the current configuration.
	 * If there is no metric, which means there is no congestion, nothing has 
	 * to be changed, so a directive is generated indicating that nothing has to 
	 * be changed.
	 * @see routing.control.DirectiveEngine#generateDirective(core.control.ControlMessage)
	 */
	@Override
	public boolean generateDirective(ControlMessage message) {
		// TODO Auto-generated method stub
		this.workingOnDirectives.clear();
		this.workingOnDirectives.clear();
		return false;
	}
	


}
