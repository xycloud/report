package com.ruisi.vdop.web.frame;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;

import com.ruisi.ext.engine.dao.DaoHelper;
import com.ruisi.vdop.bean.TreeNode;
import com.ruisi.vdop.bean.User;
import com.ruisi.vdop.ser.report.TreeInterface;
import com.ruisi.vdop.ser.report.TreeService;
import com.ruisi.vdop.util.SysUserBase;
import com.ruisi.vdop.util.VDOPUtils;
import com.ruisi.vdop.util.VdopConstant;

public class FrameAction extends SysUserBase {

	private DaoHelper daoHelper;
	
	private String mid; //一级菜单ID
	private Map secMenu; //二级菜单id

	public String execute() throws IOException {
		VDOPUtils.getResponse().sendRedirect("NFrame.action");
		return null;
	}
	
	/**
	 * 决策者视图
	 * @return
	 */
	public String guest(){
		User u = VDOPUtils.getLoginedUser();
		String user_id = u.getUserId();
		
		//把用户数据存入session
		VDOPUtils.getRequest().setAttribute("uinfo", u);
		
		//查询一级菜单
		Map p = new HashMap();
		p.put("type", "-1");
		p.put("id", "0");
		List ls = daoHelper.getSqlMapClientTemplate().queryForList("bi.ext.report.selectPubTypes", p);
		VDOPUtils.getRequest().setAttribute("menu", ls);
		
		String firstId = ((Map)ls.get(0)).get("id").toString();
		if(mid == null || mid.length() == 0){
			mid = firstId;
		}
		//查询二级菜单
		p.put("id", mid);
		List ls2 = daoHelper.getSqlMapClientTemplate().queryForList("bi.ext.report.selectPubTypes", p);
		//添加自己, 为了获取自己分类下的报表，得添加自己
		for(int i=0; i<ls.size(); i++){
			Map t = (Map)ls.get(i);
			if(t.get("id").toString().equals(mid)){
				ls2.add(0, t);
				break;
			}
		}
		TreeService ser = new TreeService();
		List ret = ser.createTreeDataById(ls2, new TreeInterface(){

			@Override
			public void processMap(Map m) {
				
			}

			@Override
			public void processEnd(Map m, boolean hasChild) {
				
			}
			
		}, 0);
		
		Map param = new HashMap();
		param.put("userid", VDOPUtils.getLoginedUser().getUserId());
		List reports = daoHelper.getSqlMapClientTemplate().queryForList("bi.ext.report.listAuthReport", param);
		ser.addReport2Cata(ret, reports);
		//移除自己
		ret = (List)((Map)ret.get(0)).get("children");
		VDOPUtils.getRequest().setAttribute("subMenu", JSONArray.fromObject(ret).toString());
		
		return "guest";
	}

	public String onlineUser() throws IOException{
		int ret = VDOPUtils.getOnlineUser(VDOPUtils.getServletContext());
		VDOPUtils.getResponse().getWriter().print(ret);
		return null;
	}
	
	public DaoHelper getDaoHelper() 
	{
		return daoHelper;
	}

	public void setDaoHelper(DaoHelper daoHelper) 
	{
		this.daoHelper = daoHelper;
	}


	public String getMid() {
		return mid;
	}


	public void setMid(String mid) {
		this.mid = mid;
	}


	public Map getSecMenu() {
		return secMenu;
	}


	public void setSecMenu(Map secMenu) {
		this.secMenu = secMenu;
	}
}
