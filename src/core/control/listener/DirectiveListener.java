/**
 * 
 */
package core.control.listener;

import core.DTNHost;
import core.Message;
import core.MessageListener;
import report.control.directive.DirectiveDetails;

/**
 * Interface for classes that want to be informed about when 
 * directives are created or applied in a host
 *
 */
public interface DirectiveListener extends MessageListener {
	public void directiveApplied(Message m, DTNHost to);
	public void directiveCreated(DirectiveDetails directiveDetails);
}
