package core.control;

/**
 * Enum class with all the directives code.
 *
 */
public enum DirectiveCode{
	NROF_COPIES_CODE("L");
	 
	private final String code;
	private DirectiveCode(String code) {
		this.code = code;
	}
	
	public String toString() {
		return this.code;
	}
	
} //end enum
