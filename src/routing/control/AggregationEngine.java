package routing.control;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import core.control.ControlMessage;
import core.control.DirectiveCode;
import core.control.DirectiveMessage;
import core.control.MetricCode;
import core.control.MetricMessage;

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
	private List<MetricMessage> workingOnMetrics;
	
	/** List where to add the received directives to be aggregated */
	private List<DirectiveMessage> workingOnDirectives;

	/** Control configuration the directive will be based on. For instance, 
	 * the control configuration could contain the nrof_copies of the routing 
	 * algorithm. The directive could be nrof_copies/2. */
	private Map<DirectiveCode, Double> controlConfiguration;
	/**
	 * Constructor that creates the lists of workingOnMetrics and 
	 * workingOnDirectives and sets the control configuration to be 
	 * used to generate directives. 
	 */
	public AggregationEngine(Map<DirectiveCode, Double> controlConfiguration) {
		// TODO Auto-generated constructor stub
		this.workingOnMetrics = new ArrayList<MetricMessage>();
		this.workingOnDirectives = new ArrayList<DirectiveMessage>();	
		this.controlConfiguration = controlConfiguration;
	}

	/* (non-Javadoc)
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
	public boolean generateDirective(ControlMessage message) {
		boolean generatedDirective = false;
		Double newNrofCopies = this.controlConfiguration.get(DirectiveCode.NROF_COPIES_CODE);
		int nrofConsideredMetrics = 0;
		int totalDrops = 0;
		double dropsAverage = 0;
		double nextDropsReading = 0;
		int nrofConsideredDirectives = 0;
		int totalNrofCopies = 0;
		double nrofCopiesAverage = 0;
		double nextNrofCopiesReading = 0;
		
	
		if(!this.workingOnMetrics.isEmpty()) {
			for (MetricMessage metric : this.workingOnMetrics) {
				if (metric.containsProperty​(MetricCode.DROPS_CODE.toString())) {
					nextDropsReading = ((Double)metric.getProperty(MetricCode.DROPS_CODE.toString())).doubleValue();
					if (nextDropsReading > 0) {
						totalDrops += nextDropsReading;
						nrofConsideredMetrics ++;
					}					  
				}
			}
			if(nrofConsideredMetrics > 0) {
				dropsAverage = totalDrops / nrofConsideredMetrics;
				newNrofCopies = this.controlConfiguration.get(DirectiveCode.NROF_COPIES_CODE)/2;
			}
			this.workingOnMetrics.clear();
		} //end working with the workingOnMetrics list
		
		if(!this.workingOnDirectives.isEmpty()) {			
			for (DirectiveMessage directive : workingOnDirectives) {
				if(directive.containsProperty​(DirectiveCode.NROF_COPIES_CODE.toString())) {
					nextNrofCopiesReading = ((Double)directive.getProperty(DirectiveCode.NROF_COPIES_CODE.toString())).doubleValue();
					totalNrofCopies += nextNrofCopiesReading;
					nrofConsideredDirectives ++;
				}
			}
			if (nrofConsideredDirectives > 0) {
				nrofCopiesAverage = totalNrofCopies / nrofConsideredDirectives;
				newNrofCopies = (newNrofCopies + nrofCopiesAverage) / 2;
			}
			this.workingOnDirectives.clear();
		} //end working with the workingOnDirectives list
		
		if (newNrofCopies != ((Double)this.controlConfiguration.get(DirectiveCode.NROF_COPIES_CODE)).doubleValue()) {
			((DirectiveMessage)message).addProperty(DirectiveCode.NROF_COPIES_CODE.toString(), newNrofCopies);
			generatedDirective = true;
		}
				
		return generatedDirective;
	}
}
