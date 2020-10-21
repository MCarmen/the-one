package routing.control.util;

/**
 * Interface for all the decay functions.
 * @author mc
 *
 */
public interface Decay {
	
	/**
	 * Calculates which is the decay weight corresponding to an abscissa value.
	 * @param at the abscissa value from where to get the corresponding decay weight.
	 * @param startDecay When, in the abscissas coordinates, to start
	 * to apply the decay.
	 * @return The decay weight for an abscissa value.
	 * @throws DecayException in case . 
	 */
	public double getDecayWeightAt(double at, double startDecay);

	/**
	 * Calculates which is the decay weight corresponding to an abscissa value.
	 * @param at the abscissa value from where to get the corresponding decay weight.
	 * @param startDecay When, in the abscissas coordinates, to start
	 * to apply the decay.
	 * @return The decay weight for an abscissa value.
	 * @throws DecayException in case . 
	 */
	public double getDecayWeightAt(double at);
	
	class DecayException extends Exception {
		/**
		 * For serialization.
		 */
		private static final long serialVersionUID = System.currentTimeMillis();

		public DecayException(String msg) {
			super(msg);
		}
	}

}
