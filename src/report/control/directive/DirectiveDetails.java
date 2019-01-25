package report.control.directive;

import java.util.ArrayList;
import java.util.List;

import core.Message;
import core.control.DirectiveCode;

public class DirectiveDetails {
	
	/** Directive Identifier. */
	private String directiveID;
	
	/** Identifier of the node that generated the directive.  */
	private String generatedByNode;
		
	/** Value of the field containing the number of copies.  */
	private double newNrofCopies;
	
	/** List of the identifiers of the aggregated directives used to 
	 * generate  this one.*/
	private List<String> directivesUsed;


	/**
	 * Constructor that initializes the list of the ids of the directives used
	 * to generate this directive.
	 */
	public DirectiveDetails() {
		this.directivesUsed = new ArrayList<String>();
	}
	
	/**
	 * Copy constructor.
	 * @param directiveDetails the directiveDetails object to be copied.
	 */
	public DirectiveDetails(DirectiveDetails directiveDetails) {
		this.directiveID = directiveDetails.getDirectiveID();
		this.generatedByNode = directiveDetails.getGeneratedByNode();	
		this.newNrofCopies = directiveDetails.getNewNrofCopies();
		this.directivesUsed = new ArrayList<String>(directiveDetails.getDirectivesUsed());
	}
	
	
	
	public String getDirectiveID() {
		return directiveID;
	}

	public String getGeneratedByNode() {
		return generatedByNode;
	}

	public double getNewNrofCopies() {
		return newNrofCopies;
	}

	public List<String> getDirectivesUsed() {
		return directivesUsed;
	}
	
	/** 
	 * Method to be invoked when the directive has been created and therefore
	 * the message has been set with the directive fields.
	 * These fields are used to initialize this object.   
	 * @param m the message filled with the directive fields.
	 */	
	public void init(Message m) {
		this.directiveID = m.getId();
		this.generatedByNode = m.getFrom().toString();
		if (m.containsProperty​(DirectiveCode.NROF_COPIES_CODE.toString())) {
			this.newNrofCopies = (double)m.getProperty(DirectiveCode.NROF_COPIES_CODE.toString());
		}		
	}
			
	public void addDirectiveUsed(String directiveUsed) {
		this.directivesUsed.add(directiveUsed);
	}
	
	/**
	 * Method that associates an empty list of directivesUsed to the 
	 * directiveDetails.
	 */
	public void reset() {
		this.directivesUsed.clear();
	}
	
	public String toString() {
		return String.format("%s %s %f %s", this.directiveID, 
				this.generatedByNode, this.newNrofCopies, this.directivesUsed);
	}
}
