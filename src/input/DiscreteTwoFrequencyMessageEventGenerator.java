package input;

import core.Settings;
import core.SettingsError;
import core.SimClock;

/**
 * Message creation -external events generator. Creates a discrete message creation
 * pattern consisting on a two possible inter-message generation time values: 
 * high frequency msg generation and low frequency msg generation. 
 * Those two values are specified through the rang defined with the "interval" 
 * setting. Instead of using this rang to generate a uniform distribution to regulate
 * the frequency of the message generation, we use the lower rang to specify the 
 * lowest difference between messages generation and the higher rang value to 
 * specify the highest difference between messages generation.
 * Each of the frequency msg generation interval(high or low) lasts a period 
 * time (intervalPeriod). The messages during one intervalPeriod are generated 
 * at a constant high rate or at a constant low rate. 
 * The whole simulation time is slotted in intervalPeriods.   
 */
public class DiscreteTwoFrequencyMessageEventGenerator extends MessageEventGenerator {
	/**
	 * Interval period -setting id {@value}. It must be a single value. This 
	 * value defines how long lasts each message generation interval. 
	 */
	protected static final String INTERVAL_PERIOD_S = "intervalPeriod";
	/** High  constant rate for the message generation.*/ 
	protected int highFrequency = -1;	
	/** Low constant rate for the message generation.*/	
	protected int lowFrequency = -1;
	/** Period to apply the same generation rate (high or low). */
	protected int intervalPeriod;
	/** The current rate mode for the message generation: high or low */
	protected MessageGenerationRateMode currentMessageGenerationRateMode = MessageGenerationRateMode.LOW_FREQ;
  
	public DiscreteTwoFrequencyMessageEventGenerator(Settings s) {
		super(s);
		
		if(s.contains(INTERVAL_PERIOD_S)) {
			this.intervalPeriod = s.getInt(INTERVAL_PERIOD_S);
		}else {
			throw new SettingsError("This message event generator needs the " + 
					"the setting " + INTERVAL_PERIOD_S);
		}
		this.highFrequency = this.msgInterval[0];
		this.lowFrequency = this.msgInterval[1];
		
		//calculate the first event's time considering starting with a slow 
		//start.
		this.nextEventsTime = (this.msgTime != null ? this.msgTime[0] : 0)
			+ this.lowFrequency;
	}

	/**
	 * Method that checks the current simulation time. Calculates in which
	 * trafficGenerationRate mode we are running. It checks if the number of consumed
	 * intervalPeriods regarding the current simulation time is even or odd. If 
	 * it is even the generationRateMode is low frequency otherwise is high 
	 * frequency. It updates the property {@link #currentMessageGenerationRateMode}.
	 */
	private void updateCurrentGenerationRateMode() {
		int simElapsedPeriods = SimClock.getIntTime() / this.intervalPeriod;
		this.currentMessageGenerationRateMode = ((simElapsedPeriods % 2) == 0) ? MessageGenerationRateMode.LOW_FREQ
				: MessageGenerationRateMode.HIGH_FREQ;
	}
	
	/**
	 * Generates a time difference between two msg creation events depending on
	 * the current generation rate. 
	 * @return the time difference for the next message generation.
	 */
	protected int drawNextEventTimeDiff() {		
		int timeDiff = (this.currentMessageGenerationRateMode == MessageGenerationRateMode.HIGH_FREQ) ?
				this.highFrequency : this.lowFrequency;
		return timeDiff;
	}
	
	/**
	 * Enum class to indicate whether we are in the state of generating low 
	 * traffic, of high traffic. Each mode lasts the time specified in the setting 
	 * {@link #INTERVAL_PERIOD}
	 *
	 */
	enum MessageGenerationRateMode{
		LOW_FREQ, HIGH_FREQ;
	}
}


