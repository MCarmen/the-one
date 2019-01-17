package routing.control;

import core.Message;
import core.Settings;
import core.control.ControlMessage;
import core.control.DirectiveCode;
import core.control.DirectiveMessage;
import core.control.MetricCode;




/**
 * Implementation of a Directive Engine {@link  routing.control.DirectiveEngine}.
 * In this implementation, all the metrics of the drops readings are  aggregated 
 * by calculating an EWMA.The accumulated smoothed value is kept along the whole
 * simulation. 
 * An EWMA is also applied to accumulate an average of the directives received 
 * announcing the number of copies of the messages in the network they have 
 * calculated. The accumulated smoothed value is kept along the whole
 * simulation. 
 *
 */
public class EWMAEngine extends DirectiveEngine {
	
	/** Alpha -setting id ({@value}) in the EWMAEngine name space for the drops */
	private static final String DROPS_ALPHA_S = "dropsAlpha";

	/** Alpha -setting id ({@value}) in the EWMAEngine name space for the 
	 * number of copies of the message */
	private static final String NROFCOPIES_ALPHA_S = "nrofCopiesAlpha";
	
	/** Alpha -setting id ({@value}) in the EWMAEngine name space for the 
	 * number of copies of the message got from other directives. */	
	private static final String DIRECTIVES_ALPHA_S = "directivesAlpha";
	
	/** dropsThreshold -setting id ({@value}) in the EWMAEngine name space for the drops */
	private static final String DROPS_THRESHOLD_S = "dropsThreshold";
	
	/** alpha-setting's default value if it is not specified in the settings 
	 ({@value}) */
	private static final double DEF_ALPHA = 0.2; 
	
	/** directivesAlpha-setting's default value if it is not specified in the settings 
	 ({@value}) */
	private static final double DEF_DIRECTIVES_ALPHA = 0.2;
	
	/** dropsThreshold-setting's default value if it is not specified in the settings 
	 ({@value}) */
	private static final int DEF_DROPS_THRESHOLD = 0;
	
	/** Accumulated soften drops average  */
	private EWMAProperty sDropsAverage;
	
	/** Accumulated soften nrofMsgCopies average */
	private EWMAProperty sNrofMsgCopiesAverage;
	
	/** 
	 * Alpha to be used to calculate sDropsAverage with the EWMA:
	 * sdropsAverage = (1-dropsAlpha) * sdropsAverage + dropsAlpha * drops_messured.
	 */
	private double dropsAlpha;
	
	/** 
	 * Alpha to be used to calculate sNrofCopiesAverage with the EWMA:
	 * sNrofCopiesAverage = (1-sNrofMsgCopiesAverage) * sNrofCopiesAverage + 
	 * 	sNrofMsgCopiesAverage * nrofCopies_messured.
	 */	
	private double nrofCopiesAlpha;

	/** 
	 * Alpha to be used to weight the number of copies of a message got after 
	 * evaluating the drops regarding to the number of copies of a message got 
	 * after evaluating the directives.
	 */	
	private double directivesAlpha;	
	
	/**
	 * Drops threshold from which a directive is generated to decrease the 
	 * number of message copies. 
	 */
	private int dropsThreshold;

	/**
	 * Controller that reads from the settings, which is set to the value of the
	 * setting control.engine, all the alphas to be used to smooth, using an EWMA
	 * function, the drops received in metrics and the number of copies of the 
	 * message got from directives. It also gets from the settings the drops 
	 * threshold to be considered to generate a directive or not.
	 * 
	 * @param settings the settings object set to the value of the setting
	 *                 control.engine.
	 */
	public EWMAEngine(Settings settings) {
		super(settings);
		this.dropsAlpha = (settings.contains(DROPS_ALPHA_S)) ? settings.getDouble(DROPS_ALPHA_S) : EWMAEngine.DEF_ALPHA;
		this.nrofCopiesAlpha = (settings.contains(NROFCOPIES_ALPHA_S)) ? settings.getDouble(NROFCOPIES_ALPHA_S)
				: EWMAEngine.DEF_ALPHA;
		this.directivesAlpha = (settings.contains(DIRECTIVES_ALPHA_S)) ? settings.getDouble(DIRECTIVES_ALPHA_S)
				: EWMAEngine.DEF_DIRECTIVES_ALPHA;		
		this.dropsThreshold = (settings.contains(DROPS_THRESHOLD_S)) ? settings.getInt(DROPS_THRESHOLD_S)
				: EWMAEngine.DEF_DROPS_THRESHOLD;
		this.sDropsAverage = new EWMAProperty(this.dropsAlpha);
		this.sNrofMsgCopiesAverage = new EWMAProperty(this.nrofCopiesAlpha);
	}

	@Override
	public void addMetric(ControlMessage metric) {
		MetricsSensed.DropsPerTime nextDropsReading;
		
		if (metric.containsProperty​(MetricCode.DROPS_CODE.toString())){
			nextDropsReading = (MetricsSensed.DropsPerTime)metric.getProperty(MetricCode.DROPS_CODE.toString());
			this.sDropsAverage.aggregateValue(nextDropsReading.getNrofDrops());
		}
	}

	@Override
	public void addDirective(ControlMessage directive) {
		double nextNrofCopiesReading;
		
		if(directive.containsProperty​(DirectiveCode.NROF_COPIES_CODE.toString())) {
			nextNrofCopiesReading = (double)directive.getProperty(DirectiveCode.NROF_COPIES_CODE.toString());
			this.sNrofMsgCopiesAverage.aggregateValue(nextNrofCopiesReading);
		}
	}

	@Override
	public boolean generateDirective(ControlMessage message) {
		double newNrofCopies = this.controlProperties.getProperty(DirectiveCode.NROF_COPIES_CODE).doubleValue();
		boolean generatedDirective = false;

		if (this.sDropsAverage.isSet() && (this.sDropsAverage.getValue() >= this.dropsThreshold)) {
			if (this.sNrofMsgCopiesAverage.isSet()) { 
				//sDropsAverage is set and sNrofCopies is set
				newNrofCopies =  (newNrofCopies / 2) * this.directivesAlpha + (1 - this.directivesAlpha) * this.sNrofMsgCopiesAverage.getValue();
			}else { 
				//sDropsAverage is set and sNrofCopies is not set
				newNrofCopies = (newNrofCopies / 2);
			}
		}else if(this.sNrofMsgCopiesAverage.isSet()){ 
			//sDropsAverage is not set and sNrofCopies is set
			newNrofCopies = this.sNrofMsgCopiesAverage.getValue();
		}// else: sDropsAverage is not set and sNrofCopies is not set 

		if (newNrofCopies != this.controlProperties.getProperty(DirectiveCode.NROF_COPIES_CODE)) {
			((DirectiveMessage) message).addProperty(DirectiveCode.NROF_COPIES_CODE.toString(), newNrofCopies);
			generatedDirective = true;
		}

		return generatedDirective;
	}

}
