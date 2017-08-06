package com.ruisi.vdop.ser.report;

import java.util.Map;

public interface TreeInterface {
	
	public void processMap(Map m);
	
	public void processEnd(Map m, boolean hasChild);
}
