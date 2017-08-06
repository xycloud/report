package com.ruisi.vdop.web.frame;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;

import com.ruisi.ext.engine.dao.DaoHelper;
import com.ruisi.ext.engine.init.XmlParser;
import com.ruisi.ext.engine.view.exception.AuthException;
import com.ruisi.vdop.bean.User;
import com.ruisi.vdop.util.SysUserBase;
import com.ruisi.vdop.util.VDOPUtils;

public class NFrameAction extends SysUserBase {
	
	private DaoHelper daoHelper;
	
	private String userId;
	private String id;

	public String execute() {
		User u = VDOPUtils.getLoginedUser();
		VDOPUtils.getRequest().setAttribute("uinfo", u);
		
		//查询用户菜单，如果用户菜单没有或只有一个，直接隐藏菜单
		
		return "success";
	}
	
	public String welcome() throws Exception{
		//查询有效天数
		return "welcome";
	}
	
	public String tree() throws IOException{

		User u = VDOPUtils.getLoginedUser();
		this.userId = u.getUserId();
		if(id == null || id.length() == 0){
			this.id = "0";
		}
		List menuList = daoHelper.getSqlMapClientTemplate().queryForList("vdop.frame.frame.frametop2", this);
		
		for(int i=0; i<menuList.size(); i++){
			Map m = (Map)menuList.get(i);
			Map attr = new HashMap();
			m.put("attributes", attr);
			attr.put("url", m.get("url"));
			
			if("open".equals(m.get("state"))){
				m.put("iconCls", "icon-gears");
			}
		}
		
		HttpServletResponse resp = VDOPUtils.getResponse();
		resp.setContentType("text/xml; charset=UTF-8");
		String ctx = JSONArray.fromObject(menuList).toString();
		resp.getWriter().println(ctx);
		return null;
	}

	public DaoHelper getDaoHelper() {
		return daoHelper;
	}

	public void setDaoHelper(DaoHelper daoHelper) {
		this.daoHelper = daoHelper;
	}

	public String getUserId() {
		return userId;
	}

	public String getId() {
		return id;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public void setId(String id) {
		this.id = id;
	}
}
