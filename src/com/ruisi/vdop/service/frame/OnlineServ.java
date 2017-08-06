package com.ruisi.vdop.service.frame;

import java.util.List;

import com.ruisi.ext.engine.dao.DaoHelper;
import com.ruisi.vdop.bean.User;
import com.ruisi.vdop.util.VDOPUtils;
import com.ruisi.vdop.util.VdopConstant;

public class OnlineServ {
	public static List getUserBySessionId(String sessionId){
		DaoHelper daoHelper=VDOPUtils.getDaoHelper();
		if(daoHelper!=null){
			User p=VDOPUtils.getNewUser();
			p.setSessionId(sessionId);
			return (List)daoHelper.getSqlMapClientTemplate().queryForList("vdop.frame.online.selectBySessionId",p);
		}
		return null;
	}
}
