package com.ruisi.vdop.web.report;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.ruisi.ext.engine.ExtConstants;
import com.ruisi.ext.engine.dao.DaoHelper;
import com.ruisi.ext.engine.view.context.ExtContext;
import com.ruisi.vdop.ser.report.ReportService;
import com.ruisi.vdop.util.VDOPUtils;
import com.ruisi.vdop.util.VdopConstant;

public class ReportAction {
	
	private String userid;
	private String pageId;
	private String pageInfo;
	private String pageName;
	private String cataId;
	
	private String dbName = VDOPUtils.getConstant(ExtConstants.dbName);
	
	private DaoHelper daoHelper;
	
	private String income; //来源，olap/report
	
	private Integer page; //当前第几页，从1开始
	private Integer rows; //每页的记录数
	
	private String mvid; //在删除文件时，需要从内存中删除整个MV
	
	private String sort = "crtdate";
	private String order = "desc";
	
	/**
	 * 新的保存，按树形结构
	 * @return
	 * @throws IOException 
	 */
	public String newSave() throws IOException{
		HttpServletResponse resp = VDOPUtils.getResponse();
		resp.setContentType("text/html; charset=UTF-8");
		this.userid = VDOPUtils.getLoginedUser().getUserId();
		//判断名字是否重复
		int cnt = (Integer)this.daoHelper.getSqlMapClientTemplate().queryForObject("bi.ext.report.olapExist", this);
		if(cnt > 0){
			resp.getWriter().print("no");
			return null;
		}
		
		if(pageId == null || pageId.length() == 0){
			this.pageId = daoHelper.getSqlMapClientTemplate().queryForObject("bi.report.querypid").toString();
			JSONObject page = JSONObject.fromObject(this.pageInfo);
			page.put("id", Integer.parseInt(this.pageId));
			this.pageInfo = page.toString();
			daoHelper.getSqlMapClientTemplate().insert("bi.ext.report.insertReport", this);
		}
		
		resp.getWriter().print(pageId);
		return null;
	}
	
	/**
	 * 列出所有授权的报表，用在报表目录中
	 * @return
	 * @throws IOException 
	 */
	public String listAuthGy() throws IOException{
		this.userid = VDOPUtils.getLoginedUser().getUserId();
		if(cataId == null || cataId.equals("0") || cataId.length() == 0){
			cataId = null;
		}
		if(this.cataId != null){
			//查询目录的子目录
			ReportService rs = new ReportService(this.daoHelper);
			List ls = this.daoHelper.queryForList("select id, pid from report_catalog where type = -1");
			List<Integer> ids = rs.queryChildTypes(Integer.parseInt(this.cataId), ls);
			StringBuffer sb = new StringBuffer();
			for(int i=0; i<ids.size(); i++){
				sb.append(ids.get(i));
				if(i != ids.size() - 1){
					sb.append(",");
				}
			}
			this.cataId = sb.toString();
		}
		List ls = daoHelper.getSqlMapClientTemplate().queryForList("bi.ext.report.listAuthReport", this, (this.page - 1) * this.rows,this.rows);
		//取总记录数
		int count = (Integer)daoHelper.getSqlMapClientTemplate().queryForObject("bi.ext.report.countAuthReport", this);
		
		Map map = new HashMap();
		map.put("total", count);
		map.put("rows", ls);
		
		/**
		 * 格式化日期
		 */
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		for(int i=0; i<ls.size(); i++){
			Map m = (Map)ls.get(i);
			//if(!dbName.equals("sqlser")){   //sqlserver 不用转换
				Object dt = m.get("crtdate");
				m.put("crtdate", sdf.format(dt));
			//}
			String income = (String)m.get("income");
			if("olap".equalsIgnoreCase(income)){
				m.put("incomeName", "多维分析");
			}else if("report".equalsIgnoreCase(income)){
				m.put("incomeName", "报表");
			}else if("ybp".equalsIgnoreCase(income)){
				m.put("incomeName", "仪表盘");
			}
		}
		HttpServletResponse resp = VDOPUtils.getResponse();
		resp.setContentType("text/html; charset=UTF-8");
		resp.getWriter().print(JSONObject.fromObject(map));
		return null;
	}
	/**
	 * 在用户发布的地方列出公有报表
	 * @return
	 * @throws IOException 
	 */
	public String listRelease() throws IOException{
		if(cataId == null || cataId.equals("0") || cataId.length() == 0){
			cataId = null;
		}
		List ls = daoHelper.getSqlMapClientTemplate().queryForList("bi.ext.report.listRelease", this);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		for(int i=0; i<ls.size(); i++){
			Map m = (Map)ls.get(i);
			if(!dbName.equals("sqlser")){   //sqlserver 不用转换
				Object dt = m.get("crtdate");
				m.put("crtdate", sdf.format(dt));
			}
		}
		HttpServletResponse resp = VDOPUtils.getResponse();
		resp.setContentType("text/html; charset=UTF-8");
		resp.getWriter().print(JSONArray.fromObject(ls));
		return null;
	}
	
	/**
	 * 列出所有公有报表，用在公有报表管理中
	 * @return
	 * @throws IOException
	 */
	public String listgy() throws IOException{
		if(cataId == null || cataId.equals("0") || cataId.length() == 0){
			cataId = null;
		}
		if(this.cataId != null){
			ReportService rs = new ReportService(this.daoHelper);
			List ls = this.daoHelper.queryForList("select id, pid from report_catalog where type = -1");
			List<Integer> ids = rs.queryChildTypes(Integer.parseInt(this.cataId), ls);
			StringBuffer sb = new StringBuffer();
			for(int i=0; i<ids.size(); i++){
				sb.append(ids.get(i));
				if(i != ids.size() - 1){
					sb.append(",");
				}
			}
			this.cataId = sb.toString();
		}
		List ls = daoHelper.getSqlMapClientTemplate().queryForList("bi.ext.report.listgyReport", this, (this.page - 1) * this.rows,this.rows);
		//取总记录数
		int count = (Integer)daoHelper.getSqlMapClientTemplate().queryForObject("bi.ext.report.countgyReport", this);
		
		Map map = new HashMap();
		map.put("total", count);
		map.put("rows", ls);
		
		/**
		 * 格式化日期
		 */
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		for(int i=0; i<ls.size(); i++){
			Map m = (Map)ls.get(i);
			//if(!dbName.equals("sqlser")){   //sqlserver 不用转换
				Object dt = m.get("crtdate");
				m.put("crtdate", sdf.format(dt));
			//}
			String income = (String)m.get("income");
			if("olap".equalsIgnoreCase(income)){
				m.put("incomeName", "多维分析");
			}else if("report".equalsIgnoreCase(income)){
				m.put("incomeName", "报表");
			}else if("ybp".equalsIgnoreCase(income)){
				m.put("incomeName", "仪表盘");
			}
		}
		HttpServletResponse resp = VDOPUtils.getResponse();
		resp.setContentType("text/html; charset=UTF-8");
		resp.getWriter().print(JSONObject.fromObject(map));
		return null;
	}
	
	public String list() throws IOException{
		if(cataId == null || cataId.equals("0") || cataId.length() == 0){
			cataId = null;
		}
		if(this.cataId != null){
			ReportService rs = new ReportService(this.daoHelper);
			List ls = this.daoHelper.queryForList("select id, pid from report_catalog where type = 1");
			List<Integer> ids = rs.queryChildTypes(Integer.parseInt(this.cataId), ls);
			StringBuffer sb = new StringBuffer();
			for(int i=0; i<ids.size(); i++){
				sb.append(ids.get(i));
				if(i != ids.size() - 1){
					sb.append(",");
				}
			}
			this.cataId = sb.toString();
		}
		this.userid = VDOPUtils.getLoginedUser().getUserId();
		List ls = daoHelper.getSqlMapClientTemplate().queryForList("bi.ext.report.listsyReport", this, (this.page - 1) * this.rows,this.rows);
		
		//取总记录数
		int count = (Integer)daoHelper.getSqlMapClientTemplate().queryForObject("bi.ext.report.countSyReport", this);
		
		Map map = new HashMap();
		map.put("total", count);
		map.put("rows", ls);
		/**
		 * 格式化日期
		 */
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		for(int i=0; i<ls.size(); i++){
			Map m = (Map)ls.get(i);
			//if(!dbName.equals("sqlser")){   //sqlserver 不用转换
				Object dt = m.get("crtdate");
				m.put("crtdate", sdf.format(dt));
				Object dt2 = m.get("updatedate");
				m.put("updatedate", sdf.format(dt2));
			//}
			String income = (String)m.get("income") == null ? (String)m.get("income".toUpperCase()) : (String)m.get("income");
			m.put("income", income); //处理 oracle 查询把 字段变成大写
			if("olap".equalsIgnoreCase(income)){
				m.put("incomeName", "多维分析");
			}else if("report".equalsIgnoreCase(income)){
				m.put("incomeName", "报表");
			}
			m.remove("ord");
			m.remove("ORD");
		}
		HttpServletResponse resp = VDOPUtils.getResponse();
		resp.setContentType("text/html; charset=UTF-8");
		resp.getWriter().print(JSONObject.fromObject(map));
		return null;
	}
	
	/**
	 * 删除公有报表
	 * @return
	 */
	public String delPubReport(){
		this.daoHelper.getSqlMapClientTemplate().delete("bi.ext.report.delRelease", this);
		//删除MV
		if(mvid != null && mvid.length() > 0){
			ExtContext.getInstance().removeMV("usave."+this.mvid);
			//删除文件
			com.ruisi.vdop.ser.bireport.ReportService tser = new com.ruisi.vdop.ser.bireport.ReportService();
			String path = tser.getFilePath(VDOPUtils.getServletContext());
			String filePath = path + VdopConstant.pushPath + "/" +  this.mvid + ".xml";
			File f = new File(filePath);
			f.delete();
		}
		return null;
	}
	/**
	 * 编辑公有报表的名称、分类信息
	 * @return
	 */
	public String updatePubReport(){
		this.daoHelper.getSqlMapClientTemplate().update("bi.ext.report.updateRelease", this);
		return null;
	}

	public String getPageInfo() {
		return pageInfo;
	}

	public String getPageName() {
		return pageName;
	}

	public String getCataId() {
		return cataId;
	}


	public void setPageInfo(String pageInfo) {
		this.pageInfo = pageInfo;
	}

	public void setPageName(String pageName) {
		this.pageName = pageName;
	}

	public void setCataId(String cataId) {
		this.cataId = cataId;
	}

	public String getUserid() {
		return userid;
	}

	public String getPageId() {
		return pageId;
	}

	public DaoHelper getDaoHelper() {
		return daoHelper;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	public void setPageId(String pageId) {
		this.pageId = pageId;
	}

	public void setDaoHelper(DaoHelper daoHelper) {
		this.daoHelper = daoHelper;
	}

	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public String getIncome() {
		return income;
	}

	public void setIncome(String income) {
		this.income = income;
	}

	public Integer getPage() {
		return page;
	}

	public Integer getRows() {
		return rows;
	}

	public void setPage(Integer page) {
		this.page = page;
	}

	public void setRows(Integer rows) {
		this.rows = rows;
	}

	public String getMvid() {
		return mvid;
	}

	public void setMvid(String mvid) {
		this.mvid = mvid;
	}

	public String getSort() {
		return sort;
	}

	public String getOrder() {
		return order;
	}

	public void setSort(String sort) {
		this.sort = sort;
	}

	public void setOrder(String order) {
		this.order = order;
	}
	
}
