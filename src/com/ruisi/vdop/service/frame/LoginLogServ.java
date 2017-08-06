package com.ruisi.vdop.service.frame;

import java.util.UUID;

import com.ruisi.ext.engine.ExtConstants;
import com.ruisi.ext.engine.dao.DaoHelper;
import com.ruisi.ext.engine.view.context.ExtContext;
import com.ruisi.vdop.bean.User;
import com.ruisi.vdop.util.VDOPUtils;
import com.ruisi.vdop.util.VdopConstant;

public class LoginLogServ {
	
	public static void insertLoginLog(User u){
		DaoHelper daoHelper=VDOPUtils.getDaoHelper();
		
		String rid=UUID.randomUUID().toString();
		if(rid!=null){
			rid=rid.replace("-","");
			u.setRid(rid);
			daoHelper.getSqlMapClientTemplate().insert("vdop.frame.login.writeLoginLog", u);
			daoHelper.getSqlMapClientTemplate().insert("vdop.frame.login.writeOnlineLog", u);
		}
		
	}
	public static void updateLogInfo(User u){
		DaoHelper daoHelper=VDOPUtils.getDaoHelper();
		u.setDbName(ExtContext.getInstance().getConstant(ExtConstants.dbName));
		daoHelper.getSqlMapClientTemplate().update("vdop.frame.login.updateLogActive", u);
	}

}
