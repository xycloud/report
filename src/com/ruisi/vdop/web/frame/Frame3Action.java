package com.ruisi.vdop.web.frame;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;

import com.ruisi.ext.engine.dao.DaoHelper;
import com.ruisi.vdop.bean.User;
import com.ruisi.vdop.util.SysUserBase;
import com.ruisi.vdop.util.VDOPUtils;

public class Frame3Action extends SysUserBase {
	
	private DaoHelper daoHelper;
	private String userId;
	private String id; //tree id
	
	private String defaultId = "1"; // 默认的 tree id ,只有在 id 为 null的时候起作用

	public String execute() {
		User u = VDOPUtils.getLoginedUser();
		VDOPUtils.getRequest().setAttribute("uinfo", u);
		userId = u.getUserId();
		//查询一级菜单
		List menuList = daoHelper.getSqlMapClientTemplate().queryForList("vdop.frame.frame.frametop", this);
		VDOPUtils.getRequest().setAttribute("menu", menuList);
		
		return "success";
	}
	
	public String tree() throws IOException{

		User u = VDOPUtils.getLoginedUser();
		this.userId = u.getUserId();
		if(id == null || id.length() == 0){
			this.id = defaultId;
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
	
	public String syspage(){
		return "syspage";
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

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDefaultId() {
		return defaultId;
	}

	public void setDefaultId(String defaultId) {
		this.defaultId = defaultId;
	}
	
}
