package routing.control;

public enum CongestionState{
	CONGESTION, OPTIMAL, INITIAL, UNDER_USE;
	
	public String toString() {
		String congestionStateStr;
		switch(this) {
		case CONGESTION: 
			congestionStateStr="congestion";
			break;
		case OPTIMAL:	
			congestionStateStr="optimal";
			break;
		case UNDER_USE:	
			congestionStateStr="under_use";
			break;								
		default: 
			congestionStateStr="initial";
		}
		return congestionStateStr;
	}
}
