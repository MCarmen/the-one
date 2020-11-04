package routing.control;

import java.util.LinkedList;
import java.util.Properties;
import java.util.stream.Stream;

import core.Settings;
import core.control.ControlMessage;
import core.control.MetricCode;
import report.control.directive.DirectiveDetails;
import report.control.directive.LRDirectiveDetails;
import routing.MessageRouter;
import routing.control.metric.CongestionMetricPerWT;
import routing.control.util.LinearRegression;

/**
 * Implementation of a Directive Engine {@link  routing.control.DirectiveEngine}.
 * In this implementation, a window of n readings of the congestion metrics are  
 * used, along with the creation time of these metrics, to estimate the congestion 
 * in the future. 
 */
public class LREngine extends DirectiveEngine {
	
	/** {@value} -setting id in the LinearRegressionEngine name space. {@see #nrofPredictors} */
	private static final String NROF_CONGESTION_READINGS_S = "nrofCongestionReadings"; 
	
	/** Default value for the property {@link #nrofCongestionReadings}. */
	private static final int NROF_CONGESTION_READINGS_DEF = 20;
	
	/** {@value} -setting id in the LinearRegressionEngine name space. {@see #predictionTimeFactor} */	
	private static final String PREDICTIONTIME_FACTOR_S = "predictionTimeFactor";
	
	/** Default value for the property {@link #predictionTimeFactorDef}. */
	private static final int PREDICTIONTIME_FACTOR_DEF = 3;

	/** An impossible value for any property. */
	protected static final double NOT_SET_VALUE = System.currentTimeMillis();
	
		
	/** The number of congestion readings needed to calculate 
	 * the linear regression. If this number is not achieved the congestion 
	 * estimation is performed using an average over the readings.*/
	private int nrofCongestionReadings; 
	
	/** Multiplicative factor applied to the metric generation interval unit, 
	 * to calculate the time in the future when we want to foresee the congestion. */
	private int predictionTimeFactor;
	
	private double congestionPrediction = NOT_SET_VALUE;
	
	/** Measures how well the regression predictions approximate the real data points (R2). */
	private double coeficientOfDetermination;
	
	/** The slope of the calculated line: line <em>y</em> = &alpha; + &beta; <em>x</em>. */
    private double slope;
	
	/** 
	 * window with congestion readings. The size of this window is 
	 * {@link #nrofCongestionReadings}*/
	private LinkedList<Double> congestionReadings = new LinkedList<Double>();
	
	/** Timestamp of the congestion readings. */
	private LinkedList<Double> congestionReadingsTimes = new LinkedList<Double>();
	 
	/** Flag that indicates if a directive has been generated during the control
	 * cicle. */
	private boolean hasDirectiveBeenGenerated = false;
			
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
		this.nrofCongestionReadings = (engineSettings.contains(NROF_CONGESTION_READINGS_S)) ? engineSettings.getInt(NROF_CONGESTION_READINGS_S) : LREngine.NROF_CONGESTION_READINGS_DEF; 	
	}

	@Override
	protected void addMetricStraightForward(ControlMessage metric) {
		double congestionReading = ((CongestionMetricPerWT) metric
				.getProperty(MetricCode.CONGESTION_CODE)).getCongestionValue();		
		if(this.congestionReadings.size() == this.nrofCongestionReadings) {
			//sliding the window.
			this.congestionReadings.pop();
			this.congestionReadingsTimes.pop();
		}
		this.congestionReadings.add(congestionReading);
		this.congestionReadingsTimes.add(metric.getCreationTime());
		this.directiveDetails.addMetricUsed(metric, new Properties());
		if (this.congestionReadings.size() == this.nrofCongestionReadings) {
			this.congestionPrediction = this.calculateCongestionPredictionAt(this.metricGenerationInterval * this.predictionTimeFactor);
			//TODO generar de forma asíncrona la directiva.
			//this.router.createNewMessage(new DirectiveMessage(from, to, id, size))
			//this.generateDirective(message, false)
			//TODO descomentar aquesta línia quan el TODO de dalt estigui fet. 
			//this.hasDirectiveBeenGenerated = true;
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
		Double[] congestionReadingsDoubleArr = new Double[this.congestionReadings.size()];
		Double[] timesDoubleArr = new Double[congestionReadingsDoubleArr.length];		
		this.congestionReadings.toArray(congestionReadingsDoubleArr);
		this.congestionReadingsTimes.toArray(timesDoubleArr);
		double[] congestionReadingsArr = Stream.of(timesDoubleArr).mapToDouble(Double::doubleValue).toArray();
		double[] timesArr = Stream.of(timesDoubleArr).mapToDouble(Double::doubleValue).toArray();
		
		LinearRegression lr = new LinearRegression(timesArr, congestionReadingsArr);
		double congestionPrediction = lr.predict(time);
		this.slope = lr.slope();
		this.coeficientOfDetermination = lr.R2();
		
		return congestionPrediction;
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
				this.congestionPrediction, this.congestionState, this.coeficientOfDetermination, this.slope);
		
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



}
