package com.ruisi.vdop.util;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.ruisi.ext.engine.dao.DaoHelper;

public class MyServletContextListener implements ServletContextListener {

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		VDOPUtils.setMyServletContext(sce.getServletContext());
		
		//如果是oracle,自动创建ORACLE的一个序列
		String db = VDOPUtils.getConstant("dbName");
		if("oracle".equalsIgnoreCase(db)){
			DaoHelper dao = VDOPUtils.getDaoHelper(sce.getServletContext());
			String sql = "SELECT count(*) cnt FROM All_Sequences where SEQUENCE_NAME='SEQ_SYS_ID'";
			int ret = dao.queryForInt(sql);
			if(ret == 0){
				dao.execute("create sequence SEQ_SYS_ID start with 10900 increment by 1 cache 20");
			}
		}
	}

}
