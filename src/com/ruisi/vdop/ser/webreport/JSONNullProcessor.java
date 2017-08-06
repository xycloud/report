package com.ruisi.vdop.ser.webreport;

import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonValueProcessor;

/**
 * 把 null 转换成 ""
 * @author hq
 * @date 2015-4-15
 */
public class JSONNullProcessor implements JsonValueProcessor {

	public Object processArrayValue(Object arg0, JsonConfig arg1) {
		return null;
	}

	public Object processObjectValue(String arg0, Object arg1,
			JsonConfig arg2) {
		return "";
	}
}
