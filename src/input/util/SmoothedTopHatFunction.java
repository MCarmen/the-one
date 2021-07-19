package input.util;

/**
 * Class that implements an smoothed top hat function with the shape:
 *  --,            ,--      L2
 *     |		  | 	
 *      | 		 |
 *       ________           L1
 *       
 *  The function is a combination between two logistic functions and a linear one.
 *  --,            
 *     |		  	
 *      | 		
 *       _____         
 *       
 *  f(x) = L2/(1+ae^((x-x0)*k)) with k > 0
 *  where x-x0 is the flexible horizontal translation. We consider x0=0
 *  where a=(L2-L1)/(L1*e^((x-x0)*k)) Where (x-x0) is where the physical region
 *  starts. 
 *  ============================================================================
 *              ,--      L2
 *  	       | 	
 *            |
 *       _____        
 *       
 *  g(x)= L2/(1+a1*e^((x-x0)k)) with k < 0
 *  where a1=(L2-L1)/(L1*e^((x-x0)*(-k)) Where (x-x0) is where the physical 
 *  region ends.
 *  ============================================================================
 *  Linear function of the physical region. 
 *  acronyms: start physical region (spr); end physical region (epr); 
 *  last x-axis value (last_x)
 *  h(x) = {0<=x<spr:f(x),spr<=x<epr:L1, last_x<=x<eof:g(x)}
 *  
 *       
 * @author mc
 *
 */
public class SmoothedTopHatFunction {
	/** Upper limit*/
	private int l2;
	
	/** Lower  limit*/
	private int l1;
		
	/** Reduction frequency */
	private int k;
	
	/** X-axis value where the physical region starts in hours*/
	private int startPR_x;
	
	/** X-axis value where the physical region ends in hours*/
	private int endPR_x;

	public SmoothedTopHatFunction(int l2, int l1, int k, int startPR_x, int endPR_x) {
		super();
		this.l2 = l2;
		this.l1 = l1;
		this.k = k;
		this.startPR_x = startPR_x;
		this.endPR_x = endPR_x;
	}
	
	
	/**
	 * Returns the value of the y-axis for a x-axis value (f(x)).
	 * 
	 * @param x: the x-axis value in seconds.
	 * @return f(x)
	 */
	public double get(double x) {
		double h = ((x >= 0) && (x < this.startPR_x)) ?
				f(x) : ((x >= this.startPR_x) && (x < this.endPR_x)) ? this.l1 :
					g(x);
				
		return h;		
	}
	
	/**
	 * Returns the value of the y-axis for a x-axis value (f(x)). 
	 * @param x The x-axis value must be in the range of [0, startPR]
	 * @return f(x)
	 */
	private double f(double x) {
		double a = (this.l2 - this.l1) / (this.l1 * Math.exp(this.startPR_x * this.k));
		double result = this.l2 / (1 + (a * Math.exp(x * this.k)));
		return result;		
	}

	/**
	 * Returns the value of the y-axis for a x-axis value (g(x)). 
	 * @param x The x-axis value must be > endPR
	 * @return g(x)
	 */
	private double g(double x) {
		double a1 = (this.l2 - this.l1) / (this.l1 * Math.exp(this.endPR_x * (Math.negateExact(this.k))));
		double result = this.l2 / (1 + (a1 * Math.exp(x * Math.negateExact(this.k))));
		return result;
	}
	
	
}
