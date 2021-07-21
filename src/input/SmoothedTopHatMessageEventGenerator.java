package input;

import core.Settings;
import core.SimClock;
import input.util.SmoothedTopHatFunction;
import util.Range;

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
	
	/** [min,max] values for l1. In case just one value is needed min and max 
	 * will be the same. L1 is the top limit in the top hat distribution function.
	 * {@link input.util.SmoothedTopHatFunction smoothed top hat function}. */	 	
	private int[] l1;
	
	/**
	 * Upper limit of the 
	 * {@link input.util.SmoothedTopHatFunction smoothedTopHatFunction}. 
	 */
	private int l2;
	
	public SmoothedTopHatMessageEventGenerator(Settings s) {
		super(s);
		Settings sthmegSettings = new Settings(this.getClass().getSimpleName());
			
		if(sthmegSettings.contains(REDUCTION_FREQUENCY_S)) {
			this.reductionFrequency = sthmegSettings.getInt(REDUCTION_FREQUENCY_S);
		}
		if(sthmegSettings.contains(PHYSICAL_REGION_S)) {
			this.pr = sthmegSettings.getCsvInts(PHYSICAL_REGION_S);
		}
		this.msgDistributionFunc = new SmoothedTopHatFunction(this.l2, this.l1[0], this.reductionFrequency, this.pr[0], this.pr[1]); 
	}
	
	/**
	 * The format for the message interval setting is a [range] for L1, and 
	 * a an int for L2. It does not make sense to have a range for L2 as it 
	 * decreases with the time up to L1. 
	 * @param s the generator settings.
	 */
	protected void setMsgInterval(Settings s) {
		Range[] topHatMsgInterval = s.getCsvRanges(MESSAGE_INTERVAL_S);
		//System.out.println("Contingut del messageInterval[0]" + topHatMsgInterval[0]);
		//System.out.println("Contingut del messageInterval[1]" + topHatMsgInterval[1]);		
		this.l1 = new int[]{(int)topHatMsgInterval[0].getMin(), (int)topHatMsgInterval[0].getMax()}; 
		//System.out.println("l1[0] " + this.l1[0]);
		//System.out.println("l1[1] " + this.l1[1]);
		this.l2 = (int)topHatMsgInterval[1].getMin();
		this.msgInterval = this.l1;
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
		if (timeDiff == this.l1[0]) {
			timeDiff = super.drawNextEventTimeDiff();
		}
		//System.out.println(String.format("Sim time %.2f in hours %.2f time diff %d", SimClock.getTime(), oneDayMod, timeDiff)); //DEBUG
		return timeDiff;		
	}

}
