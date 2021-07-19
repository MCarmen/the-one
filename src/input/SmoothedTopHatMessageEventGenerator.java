package input;

import core.Settings;
import core.SimClock;
import input.util.SmoothedTopHatFunction;

/**
 * Message creation external events generator that creates messages 
 * at an interval defined by a reversed smoothed top hat function.  
 *    
 */
public class SmoothedTopHatMessageEventGenerator extends MessageEventGenerator {	
	/**
	 * Reduction frequency -setting id {@value}. It must be a single value. 
	 * Specifies how often a reduction is applied. It affects the slope of the 
	 * function.
	 */
	private static final String REDUCTION_FREQUENCY_S = "reductionFrequency";
	
	
	/**
	 * Physical region -setting id {@value}. It is defined as a range 
	 * (begin,end) and specifies the length of the physical 
	 * region (the constant flat line of the top hat function).   
	 */
	private static final String PHYSICAL_REGION_S = "pr";
	
	/** How often we apply a reduction in the function slope. */
	private int reductionFrequency;
	
	/** The limits of the physical region in the top hat function. */
	private int[] pr;
	
	/** The distribution function we are going to use for the message 
	 * generation */
	private SmoothedTopHatFunction msgDistributionFunc;
	
	
	public SmoothedTopHatMessageEventGenerator(Settings s) {
		super(s);
		Settings sthmegSettings = new Settings(this.getClass().getSimpleName());
			
		if(sthmegSettings.contains(REDUCTION_FREQUENCY_S)) {
			this.reductionFrequency = sthmegSettings.getInt(REDUCTION_FREQUENCY_S);
		}
		if(sthmegSettings.contains(PHYSICAL_REGION_S)) {
			this.pr = sthmegSettings.getCsvInts(PHYSICAL_REGION_S);
		}
		this.msgDistributionFunc = new SmoothedTopHatFunction(this.msgInterval[1], this.msgInterval[0], this.reductionFrequency, this.pr[0], this.pr[1]); 
	}
	
	/**
	 * Generates a time difference between two msg creation events using an 
	 * {@link input.util.SmoothedTopHatFunction smoothed top hat function}.
	 * The x axis correspond to one day in hours [0, 24). The y axis correspond 
	 * to the frequency for the message generation.       
	 * @return the time difference for the next message generation.
	 */
	protected int drawNextEventTimeDiff() {	
		int oneHourInSec = 3600;
		//(int)Math.round(clockTime) 
		double oneDayMod = (Math.round(SimClock.getTime()/oneHourInSec))%24;
		int timeDiff = (int)Math.round(this.msgDistributionFunc.get(oneDayMod));	
		System.out.println(String.format("Sim time %.2f in hours %.2f time diff %d", SimClock.getTime(), oneDayMod, timeDiff)); //DEBUG
		return timeDiff;		
	}

}
