package report.control;

import core.DTNHost;
import core.Message;
import core.control.ControlMessage;
import report.DropReport;

public class ControlDropReport extends DropReport{
	
	@Override
	public void messageDeleted(Message m, DTNHost where, boolean dropped) {		
		if(m instanceof ControlMessage) {
			super.messageDeleted(m,  where,  dropped);
		}	
	}
}
