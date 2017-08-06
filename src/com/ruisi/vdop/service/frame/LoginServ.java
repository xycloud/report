package com.ruisi.vdop.service.frame;

import java.util.List;

import com.ruisi.ext.engine.dao.DaoHelper;
import com.ruisi.vdop.util.VDOPUtils;

public class LoginServ {
	public static List getUserInfo(Object parameter){
		DaoHelper daoHelper=VDOPUtils.getDaoHelper();
		if(daoHelper!=null){
			return daoHelper.getSqlMapClientTemplate().queryForList("vdop.frame.login.login",parameter);
		}
		return null;
	}

}
