package routing.control;

import core.Settings;
import core.control.ControlMessage;




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
	
	/** Alpha -setting id ({@value}) in the controller name space for the drops */
	private static final String DROPS_ALPHA_S = "dropsAlpha";

	/** Alpha -setting id ({@value}) in the controller name space for the drops */
	private static final String NROFCOPIES_ALPHA_S = "nrofCopiesAlpha";
	
	/** alpha-setting's default value if it is not specified in the settings 
	 ({@value}) */
	private static final double DEF_ALPHA = 0.2; 
	
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
	 * Controller that reads from the settings, which is set to the value of the
	 * setting control.engine, the alpha to be used to calculate sDropsAverage and
	 * sNrofMsgCopiesAverage using an EWMA function. Those alpha values are used to
	 * create the EWMA properties for the sdropsAverage and for the
	 * sNrofCopiesAverage.
	 * 
	 * @param settings the settings object set to the value of the setting
	 *                 control.engine.
	 */
	public EWMAEngine(Settings settings) {
		super(settings);
		this.dropsAlpha = (settings.contains(DROPS_ALPHA_S)) ? settings.getDouble(DROPS_ALPHA_S) : EWMAEngine.DEF_ALPHA;
		this.nrofCopiesAlpha = (settings.contains(NROFCOPIES_ALPHA_S)) ? settings.getDouble(NROFCOPIES_ALPHA_S)
				: EWMAEngine.DEF_ALPHA;
		this.sDropsAverage = new EWMAProperty(this.dropsAlpha);
		this.sNrofMsgCopiesAverage = new EWMAProperty(this.nrofCopiesAlpha);
	}

	@Override
	public void addMetric(ControlMessage metric) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addDirective(ControlMessage directive) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean generateDirective(ControlMessage message) {
		// TODO Auto-generated method stub
		return false;
	}

}
