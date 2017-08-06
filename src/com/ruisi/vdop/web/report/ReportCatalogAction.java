package com.ruisi.vdop.web.report;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.ruisi.ext.engine.ExtConstants;
import com.ruisi.ext.engine.dao.DaoHelper;
import com.ruisi.vdop.ser.report.TreeInterface;
import com.ruisi.vdop.ser.report.TreeService;
import com.ruisi.vdop.util.VDOPUtils;
import com.ruisi.vdop.util.VdopConstant;

/**
 * 报表目录管理
 * @author hq
 * @date 2014-6-16
 */
public class ReportCatalogAction {
	
	private String id;
	private String type; // -1公有。1私有
	private DaoHelper daoHelper;
	private String userId;
	
	private String name;
	private String note;
	private String ord;
	private String pid;
	private String dbName = VDOPUtils.getConstant(ExtConstants.dbName);
	
	private Boolean cataControl;  //控制目录的显示，只显示有报表的目录
	
	public String execute(){
		return "success";
	}
	
	/**
	 * 管理公有目录
	 * @return
	 */
	public String manager(){
		return "manager";
	}
	
	public String delete() throws Exception{
		this.userId = VDOPUtils.getLoginedUser().getUserId();
		//判断节点下是否有报表
		int c = 0;
		if("-1".equals(type)){
			c = (Integer)this.daoHelper.getSqlMapClientTemplate().queryForObject("bi.ext.report.hasPubReport", this);
		}else{
			c = (Integer)this.daoHelper.getSqlMapClientTemplate().queryForObject("bi.ext.report.hasPrivateReport", this);
		}
		if(c > 0){
			throw new Exception("有报表，不能删除。");
		}
		this.daoHelper.getSqlMapClientTemplate().delete("bi.ext.report.delCata", this);
		return null;
	}
	
	public String tree() throws IOException{
		if(this.id == null || this.id.length() == 0){
			id = "0";
		}
		if("-1".equals(type)){
			this.userId = null;  //在查询公有报表目录时，不需要userid做限制条件
		}else{
			this.userId = VDOPUtils.getLoginedUser().getUserId();
		}
		VDOPUtils.getResponse().setContentType("text/html; charset=UTF-8");
		List ls = daoHelper.getSqlMapClientTemplate().queryForList("bi.ext.report.selectPubTypes-all", this);
		TreeService ser = new TreeService();
		List ret = ser.createTreeData(ls, new TreeInterface(){

			@Override
			public void processMap(Map m) {
				
			}

			@Override
			public void processEnd(Map m, boolean hasChild) {
				
			}
			
		});
		//判断是否控制权限
		if(cataControl != null && cataControl){
			Map param = new HashMap();
			param.put("userid", VDOPUtils.getLoginedUser().getUserId());
			List reports = daoHelper.getSqlMapClientTemplate().queryForList("bi.ext.report.listAuthReport", param);
			ret = ser.controlCata(ret, reports);
		}
		
		String str = JSONArray.fromObject(ret).toString();
		VDOPUtils.getResponse().getWriter().print(str);
		return null;
	}
	
	public String save() throws IOException{
		VDOPUtils.getResponse().setContentType("text/html; charset=UTF-8");
		this.userId = VDOPUtils.getLoginedUser().getUserId();
		if("oracle".equals(dbName)){
			this.id = String.valueOf(VDOPUtils.getSEQ());
		}
		daoHelper.getSqlMapClientTemplate().insert("bi.ext.report.insertCata", this);
		Integer maxId = (Integer)daoHelper.getSqlMapClientTemplate().queryForObject("bi.ext.report.maxId");
		VDOPUtils.getResponse().getWriter().print(maxId);
		return null;
	}
	
	public String update(){
		daoHelper.getSqlMapClientTemplate().update("bi.ext.report.updateCata", this);
		return null;
	}
	
	public String get() throws IOException{
		VDOPUtils.getResponse().setContentType("text/html; charset=UTF-8");
		Map r = (Map)daoHelper.getSqlMapClientTemplate().queryForObject("bi.ext.report.getOne", this);
		String str = JSONObject.fromObject(r).toString();
		VDOPUtils.getResponse().getWriter().print(str);
		return null;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public DaoHelper getDaoHelper() {
		return daoHelper;
	}

	public void setDaoHelper(DaoHelper daoHelper) {
		this.daoHelper = daoHelper;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getName() {
		return name;
	}

	public String getNote() {
		return note;
	}

	public String getOrd() {
		return ord;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public void setOrd(String ord) {
		this.ord = ord;
	}

	public String getPid() {
		return pid;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public Boolean getCataControl() {
		return cataControl;
	}

	public void setCataControl(Boolean cataControl) {
		this.cataControl = cataControl;
	}
}
