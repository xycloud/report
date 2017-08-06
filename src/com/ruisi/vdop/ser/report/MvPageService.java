package com.ruisi.vdop.ser.report;

import java.util.HashMap;
import java.util.Map;

import com.ruisi.ext.engine.dao.DaoHelper;
import com.ruisi.vdop.util.VDOPUtils;
import com.ruisi.vdop.util.VdopConstant;

/**
 * 存取mv文件的 service
 * @author hq
 * @date 2015-5-14
 */
public class MvPageService {
	
	public static void save(String id, String ctx){
		Map m = new HashMap();
		m.put("id", id);
		m.put("content", ctx);
		DaoHelper dao = VDOPUtils.getDaoHelper();
		dao.getSqlMapClientTemplate().insert("web.report.savemv", m);
	}
	
	public static void update(String id, String ctx){
		Map m = new HashMap();
		m.put("id", id);
		m.put("content", ctx);
		DaoHelper dao = VDOPUtils.getDaoHelper();
		dao.getSqlMapClientTemplate().update("web.report.updatemv", m);
	}
	
	public static void delete(String id){
		Map m = new HashMap();
		m.put("id", id);
		DaoHelper dao = VDOPUtils.getDaoHelper();
		dao.getSqlMapClientTemplate().delete("web.report.deletemv", m);
	}
}
