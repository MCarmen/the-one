package routing.control.util;

import core.Settings;

/**
 * Class that encapsulates the functionality of a log-logistic decay function:
 * f(t) = if 0<=t<=startingReductionPoint: 1
 * else
 *  1/(1+(t-startingReductionPoint)*(reductionFactor^reductionFreq))
 *  For more information about decay functions visit: 
 *  https://www.researchgate.net/figure/a-Comparison-of-the-three-different-decay-functions-Gaussian-logistic-and_fig1_305074253
 * @author mc
 *
 */
public class LogLogisticDecay implements Decay{
	/** The reduction to be applied each step. */
	private static final String DECAY_REDUCTION_FACTOR_S = "reductionFactor";
	
	/** Default value for the {@link #DECAY_REDUCTION_FACTOR_S} setting. */
	private static final double DEF_DECAY_REDUCTION_FACTOR_S = 0;

	/** How often the reduction should be applied.  */
	private static final String DECAY_REDUCTION_FREQ_S = "reductionFrequency"; 
	
	/** Default value for the reductionFrequency setting. {@link #DECAY_REDUCTION_FREQ_S}*/
	private static final double DEF_DECAY_REDUCTION_FREQ = 1;
	/**
	 * At which time the decay will start -setting id ({@value}). 
	 * Value is in seconds and must be a double.
	 */
	private static final String START_DECAY_AT_S = "startDecayAt";
		
	/** Default value for the startDecayAt setting*/
	private static final double DEF_START_DECAY_AT = 0;

	/** The frequency we apply the decayFactor. */
	private double reductionFrequency = DEF_DECAY_REDUCTION_FREQ;
		
	/** The reduction applied to the current value of the function parameter. If not specified
	 * by default is {@link #DEF_DECAY_REDUCTION_FACTOR_S} to indicate that it has not been specified. You cannot operate with 
	 * this value */
	private double reductionFactor = DEF_DECAY_REDUCTION_FACTOR_S;
	
	/**The coordinate in the x-axys from where to start applying the decay.*/ 
	private double startDecayAt = DEF_START_DECAY_AT;
		
	/**
	 * In this case the reductionFrequency is set to the default value.
	 * @param reductionFactor
	 */
	public LogLogisticDecay(double reductionFactor) {
		this.reductionFactor = reductionFactor;
	}
	
	public LogLogisticDecay(double reductionFrequency, double reductionFactor) {
		this(reductionFactor);
		this.reductionFrequency = reductionFrequency;
	}
	
	public LogLogisticDecay(double reductionFrequency, double reductionFactor,
			double startDecayAt) {
		this(reductionFrequency, reductionFactor);
		this.startDecayAt = startDecayAt;
	}

	/**
	 * Constructor that builds a LogLogisticDecay object out of all the settings
	 * in the settings name space passed as a parameter.
	 * @param settings the settings name space from where to get parameters to 
	 * build a LogLogisticDecay object.
	 */
	public LogLogisticDecay(Settings decaySettings) {
		this.reductionFrequency = (decaySettings.contains(DECAY_REDUCTION_FREQ_S))
				? decaySettings.getDouble(DECAY_REDUCTION_FREQ_S)
				: DEF_DECAY_REDUCTION_FREQ;
		this.reductionFactor = (decaySettings.contains(DECAY_REDUCTION_FACTOR_S)) 
				? decaySettings.getDouble(DECAY_REDUCTION_FACTOR_S)
				: DEF_DECAY_REDUCTION_FACTOR_S;				
		this.startDecayAt = (decaySettings.contains(START_DECAY_AT_S))
				? decaySettings.getDouble(START_DECAY_AT_S)
				: DEF_START_DECAY_AT;		
	}
	 
	
	/**
	 * This support method is used to generate the LogLogistic decay function
	 * without knowing the decay factor but the desired decay factor at 
	 * the abscissa asymptote. 
	 * @param abscissaAsymptote A point in the abscissa asymptote of the function.
	 * @param decayWeightAtTheAbscissaAsymptote Desired decay factor at the abscissa 
	 * asymptote. 
	 * @return a LogLogisticDecay object initialized with the default reductionFrequency,
	 * and with a reductionFactor calculated out of the method parameters.
	 */
	public static LogLogisticDecay getLogLogisticDecay(double abscissaAsymptote, double decayWeightAtTheAbscissaAsymptote) {
		
		return getLogLogisticDecay(DEF_DECAY_REDUCTION_FREQ, abscissaAsymptote, decayWeightAtTheAbscissaAsymptote);
	}
	
	/**
	 * This support method is used to generate the LogLogistic decay function
	 * without knowing the decay factor but the desired decay factor at 
	 * the abscissa asymptote. 
	 * @param reductionFrequency
	 * @param abscissaAsymptote A point in the abscissa asymptote of the function.
	 * @param decayWeightAtTheAbscissaAsymptote Desired decay factor at the abscissa 
	 * asymptote. 
	 * @return a LogLogisticDecay object initialized with the default reductionFrequency,
	 * and with a reductionFactor calculated out of the method parameters.
	 */
	public static LogLogisticDecay getLogLogisticDecay(double reductionFrequency, double abscissaAsymptote, double decayWeightAtTheAbscissaAsymptote) {
		double reductionFactor = Math.pow(
				(1 - decayWeightAtTheAbscissaAsymptote) / (abscissaAsymptote * decayWeightAtTheAbscissaAsymptote),
				(1/reductionFrequency));
		return new LogLogisticDecay(reductionFrequency, reductionFactor);
	}	
	
	/**
	 * Calculates which is the decay weight corresponding to an abscissa value
	 * following a log-logistic function:
	 * f(t) = if 0<=t<=startingReductionPoint: 1
	 * else
	 *  1/(1+(t-startingReductionTime)*(reductionFactor^reductionFreq)) 
	 * @param at the abscissa value from where to get the corresponding decay weight.
	 * @param startDecay When, in the abscissas coordinates, to start
	 * to apply the decay.
	 * @return The decay weight for an abscissa value.
	 * @throws ReductionFactorException. Exception thrown in case the reduction 
	 * factor is not specified through a constructor. 
	 */
	public double getDecayWeightAt(double at, double startDecay) {		
		double decayWeight = (at >= 0 && at <= startDecay) ? 1 : 
			1/(1+(at-startDecay)*(Math.pow(this.reductionFactor, this.reductionFrequency)));
		return decayWeight;
	}
	
	/**
	 * {@code startDecay} defaults to {@link LogLogisticDecay#startDecayAt}.
	 *
	 * @see LogLogisticDecay#getDecayWeightAt(double, double)
	 */
	public double getDecayWeightAt(double at) {
		return this.getDecayWeightAt(at, this.startDecayAt);		
	}
	
	public double getStartAt() {
		return startDecayAt;
	}

	public void setStartAt(double startAt) {
		this.startDecayAt = startAt;
	}


	class LogLogisticDecayException extends DecayException{
		private static final long serialVersionUID = 1L;

		public LogLogisticDecayException(String message) {
			super(message);
			// TODO Auto-generated constructor stub
		}		
	}

}


