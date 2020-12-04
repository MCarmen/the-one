package routing.control;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.Properties;
import java.util.stream.Stream;

import core.Settings;
import core.SimClock;
import core.control.ControlMessage;
import core.control.DirectiveMessage;
import core.control.MetricMessage;
import report.control.directive.DirectiveDetails;
import report.control.directive.LRDirectiveDetails;
import report.control.metric.MetricDetails;
import routing.control.metric.DoubleWeightedAverageCongestionMetricAggregator;
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
	private static final String NROF_LRCONGESTION_INPUTS_S = "nrofLRCongestionInputs"; 
	
	/** Default value for the property {@link #nrofLRCongestionInputs}. */
	private static final int NROF_LRCONGESTION_INPUTS_DEF = 20;
	
	/** {@value} setting id in the LinearRegressionEngine name space. */
	private static final String MAX_NROF_LRCONGESTION_INPUTS_S = "maxNrofLRCongestionInputs";
	
	/** {@value} -setting id in the LinearRegressionEngine name space. {@see #predictionTimeFactor} */	
	private static final String PREDICTIONTIME_FACTOR_S = "predictionTimeFactor";
	
	/** Default value for the property {@link #predictionTimeFactorDef}. */
	private static final int PREDICTIONTIME_FACTOR_DEF = 3;

	/** An impossible value for any property. */
	protected static final double NOT_SET_VALUE = -1;
	
	/** {@value} -setting id in the LREngine name space. {@see #aggregationInterval} */
	private static final String AGGREGATION_INTERVAL_S = "interval";
	
	/** Default value for the property {@link #aggregationInterval}*/
	private static final int AGGREGATION_INTERVAL_DEF = 90;
	
	/**
	 * ({@value}) setting indicating the name space used to aggregate metrics.
	 */
	public static final String METRIC_AGGR_NS_S = "metricAggregationNS";
	
	/** Default namespace for the metrics aggregation. */
	public static final String METRIC_AGGR_NS_DEF = "metricDoubleWeightedAvg";
	
	/** The number of congestion readings needed to calculate 
	 * the linear regression. If this number is not achieved the congestion 
	 * estimation is performed using the accumulated mobile average.*/
	private int nrofLRCongestionInputs; 
	
	/**
	 * The max number of congestion readings to be used to calculate the linear
	 * regression. By default is set to NOT_SET_VALUE indicating that there is no limit.
	 * {@see #maxNrofLRCongestionInputs}.
	 */
	private int maxNrofLRCongestionInputs; 
	
	/** Multiplicative factor applied to the metric generation interval unit, 
	 * to calculate the time in the future when we want to foresee the congestion. */
	private int predictionTimeFactor;
	
	private double congestionPrediction = NOT_SET_VALUE;
	
	/** Measures how well the regression predictions approximate the real data points (R2). */
	private double coeficientOfDetermination;
	
	/** The slope of the calculated line: line <em>y</em> = &alpha; + &beta; <em>x</em>. */
    private double slope;
	
    /** The calculated congestion is the one predicted for this time */
    private double predictedFor;
	/** 
	 * Window with congestion readings. The size of this window is 
	 * {@link #nrofLRCongestionInputs}*/
	private LinkedList<Double> lrCongestionInputs = new LinkedList<Double>();
	
	/** Timestamp of the congestion readings. */
	private LinkedList<Double> lrTimeInputs = new LinkedList<Double>();
	 	
	/** Accumulated soften congestion per windowTime.  */
	//private EWMAProperty sCongestionAverage;
	
	/** Double weighted aggregator used to aggregate the received metrics.*/
	private DoubleWeightedAverageCongestionMetricAggregator metricAggregator;
	
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
	private int aggrIntervalCounter;
	
	/** Property that encapsulates the data used to generate a directive */
	private LRDirectiveDetails directiveDetails;
	
	/** Flag that indicates if a directive has been ever generated through 
	 * a prediction using a LR. */
	private boolean hasDirectiveBeenPredicted;
	
	/**
	 * Controller that reads from the settings, which is set to the value of the
	 * setting control.engine, all the engine specific parameters.
	 * 
	 * @param engineSettings the settings object set to the value of the setting
	 *                 control.engine and which has as a subnamespace the 
	 *                 'DirectiveEngine' namespace.
	 * @param router, the router who has initialized this directiveEngine.                 
	 */
	public LREngine(Settings engineSettings, SprayAndWaitControlRouter router) {
		super(engineSettings, router);
		
		this.directiveDetails = (LRDirectiveDetails)this.newEmptyDirectiveDetails();
		this.predictionTimeFactor = (engineSettings.contains(PREDICTIONTIME_FACTOR_S)) ? engineSettings.getInt(PREDICTIONTIME_FACTOR_S) : LREngine.PREDICTIONTIME_FACTOR_DEF; 
		this.nrofLRCongestionInputs = (engineSettings.contains(NROF_LRCONGESTION_INPUTS_S)) ? engineSettings.getInt(NROF_LRCONGESTION_INPUTS_S) : LREngine.NROF_LRCONGESTION_INPUTS_DEF;
		this.maxNrofLRCongestionInputs = (engineSettings.contains(MAX_NROF_LRCONGESTION_INPUTS_S)) ? engineSettings.getInt(MAX_NROF_LRCONGESTION_INPUTS_S) : (int)LREngine.NOT_SET_VALUE; 
		this.aggregationInterval = (engineSettings.contains(AGGREGATION_INTERVAL_S)) ? engineSettings.getInt(AGGREGATION_INTERVAL_S) : AGGREGATION_INTERVAL_DEF;
		String aggregationNs = engineSettings.contains(METRIC_AGGR_NS_S) ? engineSettings.getSetting(METRIC_AGGR_NS_S) : METRIC_AGGR_NS_DEF;
		Settings aggregationSettings = new Settings(aggregationNs);
		this.metricAggregator = 
			new DoubleWeightedAverageCongestionMetricAggregator(aggregationSettings);
		this.hasDirectiveBeenPredicted = false;
		this.aggrIntervalCounter = 0;
		
		this.nextAggregationInterval();		
	}

	/**
	 * The metric is added using a double weighted average. After intervalTime, 
	 * we get the the value of the aggregation. This value is considered as a 
	 * point of the linear regression.
	 * @param metric The metric to be aggregated.
	 */
	public void addMetric(MetricMessage metric) {
		double currentTime = SimClock.getTime();//DEBUG
		this.metricAggregator.addMetric(metric);
		this.receivedCtrlMsgInDirectiveCycle = true;
				
		if(currentTime >= this.aggregationIntervalEndTime) {
			MetricDetails aggrMetricDetails = new MetricDetails();
			double congestionAvg = this.metricAggregator.getDoubleWeightedAverageForMetric(this.router.getBufferOccupancy(), aggrMetricDetails).getCongestionValue();
			this.directiveDetails.addMetricUsed(aggrMetricDetails, this.aggrIntervalCounter);
			this.lrCongestionInputs.add(BigDecimal.valueOf(congestionAvg)
					.setScale(2, RoundingMode.HALF_UP).doubleValue());
			this.lrTimeInputs
					.add(BigDecimal.valueOf(currentTime).setScale(2, RoundingMode.HALF_UP).doubleValue());
			if (this.lrCongestionInputs.size() >= this.nrofLRCongestionInputs) {
				// this.predictionTimeFactor times the cicle of generating a prediction.
				this.predictedFor = currentTime + this.aggregationInterval * this.predictionTimeFactor * this.nrofLRCongestionInputs;
				this.calculateCongestionPredictionAt(this.predictedFor);
				//sliding the window.
				LREngine.popNIfNecessary(this.lrCongestionInputs, this.maxNrofLRCongestionInputs);
				LREngine.popNIfNecessary(this.lrTimeInputs, this.maxNrofLRCongestionInputs);
				// The createNewDirective method calls the engine.generateDirective.
				this.hasDirectiveBeenPredicted = ((SprayAndWaitControlRouter) this.router)
						.createNewDirectiveMessage(new DirectiveMessage(this.router.getHost()), false);
				this.resetPredictedDirectiveSettings();

			}
			this.nextAggregationInterval();			
		}
	}
	
	/**
	 * Support method that checks whether the list passed as a parameter has 
	 * more than <code>maxN</code> elements. If this is the case we delete the 
	 * first leftover elements from the head of the list.
	 * @param n The number of elements to be popped from the list.
	 * @return a View of the list containing up to the lasts maxN elements. 
	 */
	public static void popNIfNecessary(LinkedList<Double>llist, int maxN){
		int n = llist.size() - maxN;
		
		for (int i=0; i < n; i++) {
			llist.removeFirst();
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
	private void calculateCongestionPredictionAt(double time) {		
		double[] congestionReadingsArr = listToPrimitiveTypeArray(this.lrCongestionInputs);
		double[] timesArr = listToPrimitiveTypeArray(this.lrTimeInputs);
		
		LinearRegression lr = new LinearRegression(timesArr, congestionReadingsArr);
		double congestionPrediction = lr.predict(time);
		this.slope = lr.slope();
		this.coeficientOfDetermination = lr.R2();
		
		this.congestionPrediction = (congestionPrediction < 0 ? 0 : congestionPrediction);
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
	 * Method that generates a directive (@see DirectiveEngine.generateDirective). 
	 * In case we generate a directive by timeout and we have never predicted one, 
	 * if we have received congestion metrics, the calculated congestion is the 
	 * double weighted average of the congestion readings received including
	 * our own reading.
	 * @param message The message the message to be filled with the fields of the 
	 * directive.
	 * @param isTimeOut Flag indicating if the directive has to be generated by
	 * timeout(synchronously) or asynchronously.
	 * @return In case the are no metrics or directives to be considered to 
	 * generate a new directive, the message is not fulfilled and the method 
	 * returns null, otherwise, the message is fulfilled with the directive 
	 * fields and the method returns the detailed information about the data used
	 * to generate the directive.
	 */
	public DirectiveDetails generateDirective(ControlMessage message, boolean isTimeOut) {
		double currentTime = SimClock.getTime();//DEBUG
		if(isTimeOut && !this.hasDirectiveBeenPredicted && this.receivedCtrlMsgInDirectiveCycle){
			this.congestionPrediction = this.metricAggregator.getDoubleWeightedAverageForMetric(this.router.getBufferOccupancy(), new MetricDetails()).getCongestionValue();
		}
		return super.generateDirective(message, isTimeOut);
	}
	
	@Override
	protected boolean isSetCongestionMeasure() {
		return (this.congestionPrediction != NOT_SET_VALUE);
	}

	@Override
	protected void initDirectiveDetails(ControlMessage message, int lasCtrlCycleNrofCopies) {
		this.directiveDetails.init(message, lasCtrlCycleNrofCopies, 
				this.congestionPrediction, this.congestionState,
				LREngine.listToPrimitiveTypeArray(this.lrCongestionInputs),
				LREngine.listToPrimitiveTypeArray(this.lrTimeInputs),				
				this.coeficientOfDetermination, this.slope, this.predictedFor);		
	}

	@Override
	protected DirectiveDetails copyDirectiveDetails() {
		return new LRDirectiveDetails(this.directiveDetails); 
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
		this.metricAggregator.reset();
	}
	
	private void resetPredictedDirectiveSettings() {
		this.congestionPrediction = NOT_SET_VALUE;
		this.slope = 0;
		this.coeficientOfDetermination = 0;
		this.predictedFor = 0;
		this.aggrIntervalCounter = 1;
	}

	@Override
	protected void resetDirectiveCycleSettings() {
		this.receivedCtrlMsgInDirectiveCycle = false;
		this.directiveDetails.reset();
	}

	@Override
	protected boolean shouldUpdateCongestionState(boolean sync) {
		return ((!sync) || (!this.hasDirectiveBeenPredicted && this.receivedCtrlMsgInDirectiveCycle)) 
				? true : false;
	}
	
}
