/**
 * 
 */
package core.control.listener;

import core.DTNHost;
import core.Message;
import core.MessageListener;
import report.control.directive.BufferedMessageUpdate;
import report.control.directive.DirectiveDetails;

/**
 * Interface for classes that want to be informed about when 
 * directives are created or applied in a host
 *
 */
public interface DirectiveListener extends MessageListener {
	public void directiveReceived(Message m, DTNHost to);
	public void directiveCreated(DirectiveDetails directiveDetails);
	public void directiveAppliedToBufferedMessages(BufferedMessageUpdate msgsUpdates);
}
