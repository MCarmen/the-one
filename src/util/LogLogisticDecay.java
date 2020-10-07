package util;

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
public class LogLogisticDecay {
	/** Default value for the reductionFrequency. */
	private static final int DEF_REDUCTION_FREQ = 1;
	/** The frequency in we apply the reductionFactor. */
	private int reductionFrequency = DEF_REDUCTION_FREQ;
	
	/** The reduction applied to the current value of the function parameter. If not specified
	 * by default is -1 to indicate that it has not been specified. You cannot operate with 
	 * this value */
	private double reductionFactor = -1;

	public LogLogisticDecay(int reductionFrequency, double reductionFactor) {	
		this.reductionFrequency = reductionFrequency;
		this.reductionFactor = reductionFactor;
	}

	/**
	 * In this case the reductionFrequency is set to the default value.
	 * @param reductionFactor
	 */
	public LogLogisticDecay(double reductionFactor) {
		this.reductionFactor = reductionFactor;
	}
	
	/**
	 * This constructor is used to generate the LogLogistic decay function
	 * without knowing the decay factor but the desired decay factor at 
	 * the abscissa asymptote. 
	 * @param abscissaAsymptote A point in the abscissa asymptote of the function.
	 * @param decayFactorAtTheAbscissaAsymptote Desired decay factor at the abscissa 
	 * asymptote. 
	 */
	public LogLogisticDecay(double abscissaAsymptote, double decayFactorAtTheAbscissaAsymptote) {
		this(DEF_REDUCTION_FREQ, abscissaAsymptote, decayFactorAtTheAbscissaAsymptote);
	}
	
	/**
	 * This constructor is used to generate the LogLogistic decay function
	 * without knowing the decay factor but the desired decay factor at 
	 * the abscissa asymptote. 
	 * @param reductionFrequency
	 * @param abscissaAsymptote A point in the abscissa asymptote of the function.
	 * @param decayFactorAtTheAbscissaAsymptote Desired decay factor at the abscissa 
	 * asymptote. 
	 */
	public LogLogisticDecay(int reductionFrequency, double abscissaAsymptote, double decayFactorAtTheAbscissaAsymptote) {
		this.reductionFrequency = reductionFrequency;
		this.reductionFactor = Math.pow(
				(1 - decayFactorAtTheAbscissaAsymptote) / (abscissaAsymptote * decayFactorAtTheAbscissaAsymptote),
				(1/this.reductionFrequency));
	}	
	
	protected boolean isSetReductionFactor() {
		return (this.reductionFactor == -1) ? false : true;
	}
	
	/**
	 * Calculates which is the decay weight corresponding to an abscissa value
	 * following a log-logistic function:
	 * f(t) = if 0<=t<=startingReductionPoint: 1
	 * else
	 *  1/(1+(t-startingReductionTime)*(reductionFactor^reductionFreq)) 
	 * @param at the abscissa value from where to get the corresponding decay weight.
	 * @param startingReductionPoint When, in the abscissas coordinates, to start
	 * to apply the decay.
	 * @return The decay weight for an abscissa value.
	 * @throws ReductionFactorException. Exception thrown in case the reduction 
	 * factor is not specified through a constructor. 
	 */
	public double getDecayWeightAt(double at, double startingReductionPoint) throws ReductionFactorException{
		if (!this.isSetReductionFactor()) {
			throw new ReductionFactorException();
		} 
		
		double decayWeight = (at >= 0 && at <= startingReductionPoint) ? 1 : 
			1/(1+(at-startingReductionPoint)*(Math.pow(this.reductionFactor, this.reductionFrequency)));
		return decayWeight;
	}
	
	class LogLogisticDecayException extends Exception{
		private static final long serialVersionUID = 1L;

		public LogLogisticDecayException(String message) {
			super(message);
			// TODO Auto-generated constructor stub
		}		
	}
	
	class ReductionFactorException extends LogLogisticDecayException{
		private static final long serialVersionUID = 1L;

		public ReductionFactorException() {
			super("The reduction factor has not been specified");
			// TODO Auto-generated constructor stub
		}
	}
}


