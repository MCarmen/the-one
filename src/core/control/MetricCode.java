package core.control;

/**
 * Enum class with all the metrics code.
 *
 */
public enum MetricCode{
	CONGESTION_CODE("congestion");
	
	/** Identifier of the metric */
	private final String code;
	private MetricCode(String code) {
		this.code = code;
	}
	
	public String toString() {
		return this.code;
	}	
}
