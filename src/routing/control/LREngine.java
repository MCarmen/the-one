package routing.control;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.Properties;
import java.util.stream.Stream;

import core.Settings;
import core.SimClock;
import core.control.ControlMessage;
import core.control.MetricCode;
import report.control.directive.DirectiveDetails;
import report.control.directive.LRDirectiveDetails;
import routing.MessageRouter;
import routing.control.metric.CongestionMetricPerWT;
import routing.control.util.EWMAProperty;
import routing.control.util.EWMAPropertyIterative;
import routing.control.util.LinearRegression;

/**
 * Implementation of a Directive Engine {@link  routing.control.DirectiveEngine}.
 * In this implementation we receive congestion metrics during a window time. 
 * We aggregate those readings using an EWMA. After the windowTime, the resulting
 * aggregated average will be one of the inputs for a linear regression along with
 * the time of the interval. Those inputs along with the time they were generated
 * fed the linear regression. We will use this linear regression to estimate 
 * the congestion in the future.
 */
public class LREngine extends DirectiveEngine {
	
	/** {@value} -setting id in the LinearRegressionEngine name space. {@see #nrofPredictors} */
	private static final String NROF_LRCONGESTION_INPUTS_S = "nrofCongestionReadings"; 
	
	/** Default value for the property {@link #nrofLRCongestionInputs}. */
	private static final int NROF_LRCONGESTION_INPUTS_DEF = 20;
	
	/** {@value} -setting id in the LinearRegressionEngine name space. {@see #predictionTimeFactor} */	
	private static final String PREDICTIONTIME_FACTOR_S = "predictionTimeFactor";
	
	/** Default value for the property {@link #predictionTimeFactorDef}. */
	private static final int PREDICTIONTIME_FACTOR_DEF = 3;

	/** An impossible value for any property. */
	protected static final double NOT_SET_VALUE = System.currentTimeMillis();
	
	/** Alpha -setting id ({@value}) in the EWMAEngine name space for the drops */
	private static final String CONGESTION_ALPHA_S = "congestionAlpha";
	
	/** alpha-setting's default value if it is not specified in the settings 
	 ({@value}) */
	private static final double DEF_ALPHA = 0.2; 
	
	/** {@value} -setting id in the LREngine name space. {@see #aggregationInterval} */
	private static final String AGGREGATION_INTERVAL_S = "aggregationInterval";
	
	/** The number of congestion readings needed to calculate 
	 * the linear regression. If this number is not achieved the congestion 
	 * estimation is performed using the acumulated mobile average.*/
	private int nrofLRCongestionInputs; 
	
	/** Multiplicative factor applied to the metric generation interval unit, 
	 * to calculate the time in the future when we want to foresee the congestion. */
	private int predictionTimeFactor;
	
	private double congestionPrediction = NOT_SET_VALUE;
	
	/** Measures how well the regression predictions approximate the real data points (R2). */
	private double coeficientOfDetermination;
	
	/** The slope of the calculated line: line <em>y</em> = &alpha; + &beta; <em>x</em>. */
    private double slope;
	
	/** 
	 * Window with congestion readings. The size of this window is 
	 * {@link #nrofLRCongestionInputs}*/
	private LinkedList<Double> lrCongestionInputs = new LinkedList<Double>();
	
	/** Timestamp of the congestion readings. */
	private LinkedList<Double> lrTimeInputs = new LinkedList<Double>();
	 
	/** Flag that indicates if a directive has been generated during the control
	 * cicle. */
	private boolean hasDirectiveBeenGenerated = false;
	
	/** Accumulated soften congestion per windowTime.  */
	private EWMAProperty sCongestionAverage;
	
	/** 
	 * Alpha to be used to calculate sCongestionAvg with the EWMA:
	 * sCongestionAvg = (1-alpha) * sPoperty + alpha * property_messured.
	 */
	private double congestionAlpha;
	
	/** 
	 * Window time while the received congestion readings are aggregated using 
	 * an EWMA.
	 */
	private int aggregationInterval;
	
	/** 
	 * The controller receives congestion readings and aggregates them using 
	 * an EWMA during a time interval. At the end of one interval the aggregated
	 * value is stored as an input for the regression. This property indicates 
	 * when an aggregationIntervalEnds.
	 */
	private double aggregationIntervalEndTime;
	
	/** Index indicating which is the current aggregationInterval */
	private int aggrIntervalCounter = 0;
			
	/**
	 * Controller that reads from the settings, which is set to the value of the
	 * setting control.engine, all the engine specific parameters.
	 * 
	 * @param engineSettings the settings object set to the value of the setting
	 *                 control.engine and which has as a subnamespace the 
	 *                 'DirectiveEngine' namespace.
	 * @param router, the router who has initialized this directiveEngine.                 
	 */
	public LREngine(Settings engineSettings, MessageRouter router) {
		super(engineSettings, router);
		
		this.predictionTimeFactor = (engineSettings.contains(PREDICTIONTIME_FACTOR_S)) ? engineSettings.getInt(PREDICTIONTIME_FACTOR_S) : LREngine.PREDICTIONTIME_FACTOR_DEF; 
		this.nrofLRCongestionInputs = (engineSettings.contains(NROF_LRCONGESTION_INPUTS_S)) ? engineSettings.getInt(NROF_LRCONGESTION_INPUTS_S) : LREngine.NROF_LRCONGESTION_INPUTS_DEF;
		this.congestionAlpha = (engineSettings.contains(CONGESTION_ALPHA_S)) ? engineSettings.getDouble(CONGESTION_ALPHA_S) : LREngine.DEF_ALPHA;
		this.aggregationInterval = (engineSettings.contains(AGGREGATION_INTERVAL_S)) ? engineSettings.getInt(AGGREGATION_INTERVAL_S) : this.metricGenerationInterval;
		
		this.nextAggregationInterval();		
	}

	@Override
	protected void addMetricStraightForward(ControlMessage metric) {
		double congestionReading = ((CongestionMetricPerWT) metric
				.getProperty(MetricCode.CONGESTION_CODE)).getCongestionValue();
		double lastCongestionAverage = this.sCongestionAverage.getValue(); 
		this.sCongestionAverage.aggregateValue(congestionReading);
		((LRDirectiveDetails)this.directiveDetails).addMetricUsed(metric, lastCongestionAverage, this.sCongestionAverage.getValue(), this.aggrIntervalCounter);
		
		
		if(SimClock.getTime() >= this.aggregationIntervalEndTime) {			
			this.lrCongestionInputs.add(this.sCongestionAverage.getValue());
			this.lrTimeInputs.add(SimClock.getTime());			
			if (this.lrCongestionInputs.size() == this.nrofLRCongestionInputs) {
				double predictionTime = BigDecimal.valueOf(SimClock.getTime() + this.metricGenerationInterval * this.predictionTimeFactor)
					    .setScale(2, RoundingMode.HALF_UP)
					    .doubleValue();
				this.congestionPrediction = this.calculateCongestionPredictionAt(predictionTime); 
				//TODO generar de forma asíncrona la directiva.
				//this.router.createNewMessage(new DirectiveMessage(from, to, id, size))
				//this.generateDirective(message, false)
				//TODO descomentar aquesta línia quan el TODO de dalt estigui fet. 
				//this.hasDirectiveBeenGenerated = true;
				
				//sliding the window.
				this.lrCongestionInputs.pop();
				this.lrTimeInputs.pop();
			}
			this.nextAggregationInterval();			
		}
	}
	
	/**
	 * Method that uses a linear regression to predict the congestion in time 
	 * {@code time}. This calculation is set in the property 
	 * {@link #congestionPrediction}.
	 * 
	 * @param time We want to calculate the congestion prediction at that time.
  	 *	
	 */
	private double calculateCongestionPredictionAt(double time) {		
		double[] congestionReadingsArr = listToPrimitiveTypeArray(this.lrCongestionInputs);
		double[] timesArr = listToPrimitiveTypeArray(this.lrTimeInputs);
		
		LinearRegression lr = new LinearRegression(timesArr, congestionReadingsArr);
		double congestionPrediction = lr.predict(time);
		this.slope = lr.slope();
		this.coeficientOfDetermination = lr.R2();
		
		return (congestionPrediction < 0 ? 0 : congestionPrediction);
	}
	
	/**
	 * Support method that converts a LinkedList to a primitive array.
	 * @param list the list to be converted.
	 * @return an array with the elements of the list converted to its primitive type.
	 */
	private static double[] listToPrimitiveTypeArray(LinkedList<Double> list) {
		Double[] listToDouble = new Double[list.size()];
		return Stream.of(list.toArray(listToDouble)).mapToDouble(Double::doubleValue).toArray();		
	}
	

	@Override
	public void addDirective(ControlMessage directive) {		
		this.directiveDetails.addDirectiveUsed(directive, new Properties());
	}

	@Override
	/**
	 * This method is called after a timeout. @see DirectiveEngine.generateDirective
	 * for the full documentation of the method.
	 */
	public DirectiveDetails generateDirective(ControlMessage message) {		
		return this.generateDirective(message, true);
	}
	
	/**
	 * Method that generates a directive (@see DirectiveEngine.generateDirective) 
	 * just if no directive has been generated during this windowTime. 
	 * In case we get to the point to generate a directive by a timeout and 
	 * we haven't received enough data to predict the congestion, the prediction
	 * is the average of the congestion readings received.
	 * @param message The message the message to be filled with the fields of the 
	 * directive.
	 * @param isTimeOut Flag indicating if the method is called after finishing the 
	 * control cycle.
	 * @return In case the are no metrics or directives to be considered to 
	 * generate a new directive, the message is not fulfilled and the method 
	 * returns null, otherwise, the message is fulfilled with the directive 
	 * fields and the method returns the detailed information about the data used
	 * to generate the directive.
	 */
	private DirectiveDetails generateDirective(ControlMessage message, boolean isTimeOut) {
		if (!this.isSetCongestionMeasure() && (this.sCongestionAverage.isSet())) {
			this.congestionPrediction = this.sCongestionAverage.getValue();
		}
		return (!isTimeOut) || (!this.hasDirectiveBeenGenerated) ? 
				super.generateDirective(message) : null;

	}
	
	@Override
	protected boolean isSetCongestionMeasure() {
		return (this.congestionPrediction != NOT_SET_VALUE);
	}

	@Override
	protected void initDirectiveDetails(ControlMessage message, int lasCtrlCycleNrofCopies) {
		((LRDirectiveDetails)this.directiveDetails).init(message, lasCtrlCycleNrofCopies, 
				this.congestionPrediction, this.congestionState,
				LREngine.listToPrimitiveTypeArray(this.lrCongestionInputs),
				LREngine.listToPrimitiveTypeArray(this.lrTimeInputs),				
				this.coeficientOfDetermination, this.slope);		
	}

	@Override
	protected DirectiveDetails copyDirectiveDetails() {
		return new LRDirectiveDetails((LRDirectiveDetails)this.directiveDetails); 
	}

	@Override
	protected DirectiveDetails newEmptyDirectiveDetails() {
		return new LRDirectiveDetails();
	}

	@Override
	protected double getCalculatedCongestion() {
		return this.congestionPrediction;
	}

	/**
	 * Method to be called to move to the next aggregation interval. It sets all 
	 * the attributes involved in the calculations during the interval.
	 */
	private void nextAggregationInterval() {		
		this.aggregationIntervalEndTime = SimClock.getTime() + 
				this.aggregationInterval;
		this.aggrIntervalCounter++;
		this.sCongestionAverage = new EWMAPropertyIterative(this.congestionAlpha);	
	}

}
