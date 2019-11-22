package report.control;

import core.DTNHost;
import core.Message;
import core.Message.MessageType;
import core.control.ControlMessage;
import report.TMessageStatsReport;

public class ControlStatsReport extends TMessageStatsReport{
	private long nrofBytesOfDataMsgStarted;
	private long nrofBytesOfDataMsgCreated;
	private long nrofBytesOfControlMsgStarted;
	private long nrofBytesOfControlMsgCreated;	

	public ControlStatsReport(){
		this.init(ControlMessage.class);
	}

	@Override
	public void newMessage(Message m) {
		if (isWarmupID(m.getId()) || isWarmDownID(m.getId())) {
			return;
		}			
		if(m.getType() == MessageType.MESSAGE) {
			this.nrofBytesOfDataMsgCreated += m.getSize();	
		}else {
			this.nrofBytesOfControlMsgCreated += m.getSize();
		}	
		super.newMessage(m);
	}
	
	@Override
	public void messageTransferStarted(Message m, DTNHost from, DTNHost to) {
		if (isWarmupID(m.getId()) || isWarmDownID(m.getId())) {
			return;
		}	
		if(m.getType() == MessageType.MESSAGE) {
			this.nrofBytesOfDataMsgStarted += m.getSize();	
		}else {
			this.nrofBytesOfControlMsgStarted += m.getSize();
		}	
		super.messageTransferStarted(m, from, to);
	}
	
	@Override
	/**
	 * Calculates the overHead, in bytes, of the control messages created and 
	 * started over the total messages created and started.
	 * @return The fraction of the control messages created and started over 
	 * the total amount of messages created and started, in bytes.
	 */
	protected double getOverHead() {
		double congestionOverHead = Double.NaN;	// overhead ratio
		long dataMsgCreatedAndStarted = this.nrofBytesOfDataMsgCreated + this.nrofBytesOfDataMsgStarted;				
		long controlMsgCreatedAndStarted = this.nrofBytesOfControlMsgCreated + this.nrofBytesOfControlMsgStarted;
		long totalMsgCreatedAndStarted = dataMsgCreatedAndStarted + controlMsgCreatedAndStarted;
		if (totalMsgCreatedAndStarted > 0) {
			congestionOverHead = (1.0 * controlMsgCreatedAndStarted) / totalMsgCreatedAndStarted;
		}
		return congestionOverHead;
	}
	
	
}

