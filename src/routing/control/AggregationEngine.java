package routing.control;

import java.util.ArrayList;
import java.util.List;

import core.Settings;
import core.control.ControlMessage;
import core.control.DirectiveCode;
import core.control.DirectiveMessage;
import core.control.MetricCode;
import core.control.MetricMessage;
import report.control.directive.DirectiveDetails;

/**
 * Implementation of a Directive Engine {@link  routing.control.DirectiveEngine}.
 * In this implementation, all the metrics added to the list of workingOnMetrics 
 * will be aggregated by calculating the average. After the average is calculated
 * the working list is emptied, ready for adding new metrics. The same applies
 * to the received directives.
 *
 */
public class AggregationEngine extends DirectiveEngine {
	/** List where to add the received metrics to be aggregated */
	private List<MetricMessage> workingOnMetrics;
	
	/** List where to add the received directives to be aggregated */
	private List<DirectiveMessage> workingOnDirectives;

	/**
	 * Constructor that creates the lists of workingOnMetrics and 
	 * workingOnDirectives.
	 */
	public AggregationEngine(Settings settings) {
		super(settings);
		this.workingOnMetrics = new ArrayList<MetricMessage>();
		this.workingOnDirectives = new ArrayList<DirectiveMessage>();
	}
	
	
	/** 
	 * @see routing.control.DirectiveEngine#addMetric(core.control.ControlMessage)
	 */
	@Override
	public void addMetric(ControlMessage metric) {
		this.workingOnMetrics.add((MetricMessage)metric);
	}

	/* (non-Javadoc)
	 * @see routing.control.DirectiveEngine#addDirective(core.control.ControlMessage)
	 */
	@Override
	public void addDirective(ControlMessage directive) {
		this.workingOnDirectives.add((DirectiveMessage)directive);
		this.directiveDetails.addDirectiveUsed(directive.getId());
	}

	/** 
	 * @see routing.control.DirectiveEngine#generateDirective(core.control.ControlMessage) 
	 * If there is at least one metric indicating that there is congestion 
	 * in the network, the engine generates a directive reducing the copies of 
	 * a message in the network by half of the current configuration.
	 * The method calculates the average of the number of drops mesured by the 
	 * metrics.
	 * The method also checks for directives received from other controllers. 
	 * If there are new directives, it calculates the average between the new 
	 * freshly generated directive and the received directives.
	 * If the controller has not generated any new fresh directive and has received
	 * other directives, calculates the average between its initial configuration
	 * and the received directives.  
	 * If there is no metric, which means there is no congestion, and no other
	 * directive arrived from other controllers, nothing has to be changed, so 
	 * no directive is generated.
	 * @param message the message to be filled with the fields of the 
	 * directive.
	 */
	@Override
	public DirectiveDetails generateDirective(ControlMessage message) {
		DirectiveDetails currentDirectiveDetails = null;
		double newNrofCopies = this.getCurrentControlPropertyValue(DirectiveCode.NROF_COPIES_CODE).doubleValue();
		int nrofConsideredMetrics = 0;
		int totalDrops = 0;
		double dropsAverage;
		MetricsSensed.DropsPerTime nextDropsReading;
		int nrofConsideredDirectives = 0;
		int totalNrofCopies = 0;
		double nrofCopiesAverage = 0;
		double nextNrofCopiesReading = 0;
		
	
		if(!this.workingOnMetrics.isEmpty()) {
			for (MetricMessage metric : this.workingOnMetrics) {
				if (metric.containsProperty​(MetricCode.DROPS_CODE.toString())) {
					nextDropsReading = (MetricsSensed.DropsPerTime)metric.getProperty(MetricCode.DROPS_CODE.toString());
					if (nextDropsReading != null) {
						totalDrops += nextDropsReading.getNrofDrops();
						nrofConsideredMetrics ++;
					}					  
				}
			}
			if(nrofConsideredMetrics > 0) {
				dropsAverage = totalDrops / nrofConsideredMetrics;
				newNrofCopies = (int)Math.ceil(newNrofCopies / 2);
			}
			this.workingOnMetrics.clear();
		} //end working with the workingOnMetrics list
		
		if(!this.workingOnDirectives.isEmpty()) {			
			for (DirectiveMessage directive : workingOnDirectives) {
				if(directive.containsProperty​(DirectiveCode.NROF_COPIES_CODE.toString())) {
					nextNrofCopiesReading = (Double)(directive.getProperty(DirectiveCode.NROF_COPIES_CODE.toString()));
					totalNrofCopies += nextNrofCopiesReading;
					nrofConsideredDirectives++;
				}
			}
			if (nrofConsideredDirectives > 0) {
				nrofCopiesAverage = totalNrofCopies / nrofConsideredDirectives;
				newNrofCopies = (int)Math.ceil((newNrofCopies + nrofCopiesAverage) / 2);
			}
			this.workingOnDirectives.clear();
		} //end working with the workingOnDirectives list
		
		if (newNrofCopies != this.currentValueForControlProperties.get(DirectiveCode.NROF_COPIES_CODE)) {
			this.currentValueForControlProperties.put(DirectiveCode.NROF_COPIES_CODE, newNrofCopies);
			((DirectiveMessage)message).addProperty(DirectiveCode.NROF_COPIES_CODE.toString(), newNrofCopies);
			this.directiveDetails.init(message);
			currentDirectiveDetails = new DirectiveDetails(this.directiveDetails);
		}
				
		this.directiveDetails.reset();

		return currentDirectiveDetails;
	}
}
