package routing.control.util;

import core.Settings;

/**
 * Factory that builds Decay objects shaped by the decay namespace in the
 * settings
 * @author mc
 *
 */
public class DecayFactory {

	
	/** Base namespace for the decay settings ({@value})*/
	private static final String BASE_DECAY_NS = "decay";
	
	/** function setting in the Controller#DECAY_S nameSpace */
	private static final String DECAY_FUNCTION_S = "function";
	
	/** package where to look for routing classes */
	private static final String ROUTING_PACKAGE = "routing.";
	
	
	
	/**
	 * Method that builds a decay object that encapsulates the decay function
	 * specified in the settings. If no decay function is specified in the settings
	 * the decay object is null.
	 * 
	 * @param decaySettings The settings object corresponding to the decay
	 *                      namespace.
	 * @return An object encapsulating a decay function or null if no decay function
	 *         is specified in the settings.
	 */
	public static Decay getDecay(String decayNamspace) {
		Decay decay = null;

		Settings decaySettings = new Settings(decayNamspace);
		if (decaySettings != null) {
			String decayFuncClassName = (decaySettings.contains(DECAY_FUNCTION_S))
					? decaySettings.getSetting(DECAY_FUNCTION_S)
					: null;							
			if (decayFuncClassName != null) {
				decay = (Decay) decaySettings.createIntializedObject(ROUTING_PACKAGE+decayFuncClassName);
			}
		}
		return decay;
	}

	/**
	 * {@code decayNamspace} defaults to {@link DecayFactory#BASE_DECAY_NS}.
	 *
	 * @see DecayFactory#getDecay(String)
	 */
	public static Decay getDecay() {
		return getDecay(BASE_DECAY_NS);
	}
}
