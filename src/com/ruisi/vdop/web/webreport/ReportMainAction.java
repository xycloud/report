package com.ruisi.vdop.web.webreport;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONNull;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.ruisi.ext.engine.ExtConstants;
import com.ruisi.ext.engine.dao.DaoHelper;
import com.ruisi.ext.engine.view.context.ExtContext;
import com.ruisi.ext.engine.view.context.MVContext;
import com.ruisi.vdop.ser.bireport.ReportService;
import com.ruisi.vdop.ser.olap.CompPreviewService;
import com.ruisi.vdop.ser.report.MvPageService;
import com.ruisi.vdop.ser.webreport.JSONNullProcessor;
import com.ruisi.vdop.ser.webreport.PageService;
import com.ruisi.vdop.util.VDOPUtils;
import com.ruisi.vdop.util.VdopConstant;

/**
 * 在线报表
 * @author hq
 * @date 2014-1-20
 */
public class ReportMainAction {
	
	private DaoHelper daoHelper;
	
	private String pageId;
	private String pageInfo;
	private String userid;
	private String pageName; //页面名称
	private String pageNote; //页面描述
	private String dbName = VDOPUtils.getConstant(ExtConstants.dbName);
	private String purl;// 新生成的URL
	private int rid; //唯一标识
	private String cataId; //报表目录ID
	private String fileName; //发布时生成的文件名
	private String menus; //控制菜单是否显示，默认显示，如果open=0,表示菜单不显示
	
	public String execute() throws IOException, SQLException{
		this.userid = VDOPUtils.getLoginedUser().getUserId();
		if(pageId != null && pageId.length() > 0){
			Map m = (Map)this.daoHelper.getSqlMapClientTemplate().queryForObject("web.report.querypageinfo", this);
			if(m == null){
				return "success";
			}
			Object pctx = m.get("pageinfo");
			if(pctx instanceof String){
				this.pageInfo = (String)m.get("pageinfo");
			}else if(pctx instanceof oracle.sql.CLOB){
				oracle.sql.CLOB clob = (oracle.sql.CLOB)pctx;
				Reader is = clob.getCharacterStream();
				this.pageInfo = IOUtils.toString(is);
				is.close();
			}else if(pctx instanceof net.sourceforge.jtds.jdbc.ClobImpl){
				net.sourceforge.jtds.jdbc.ClobImpl clob = (net.sourceforge.jtds.jdbc.ClobImpl)pctx;
				Reader is = clob.getCharacterStream();
				this.pageInfo = IOUtils.toString(is);
				is.close();
			}
			this.pageName = (String)m.get("pagename");
		}
		
		if(menus != null && menus.length() > 0){
			JSONObject obj = JSONObject.fromObject(menus);
			VDOPUtils.getRequest().setAttribute("menuDisp", obj);
		}
		
		return "success";
	}
	
	/**
	 * 报表发布
	 * @return
	 * @throws Exception 
	 */
	public String release() throws Exception{
		this.userid = VDOPUtils.getLoginedUser().getUserId();
		if("oracle".equalsIgnoreCase(dbName)){
			this.pageId = String.valueOf(VDOPUtils.getSEQ());
		}
		//如果名称重复，直接更新，名字不存在直接插入
		List files = this.daoHelper.getSqlMapClientTemplate().queryForList("bi.ext.report.releaseExist", this);
		if(files.size() > 0){
			Map m = (Map)files.get(0);
			fileName = (String)m.get("file");
			//更新xml文件
			JsonConfig jsonConfig = new JsonConfig();
			jsonConfig.registerJsonValueProcessor(JSONNull.class,new JSONNullProcessor());
			JSONObject json = JSONObject.fromObject(pageInfo, jsonConfig);
			ReportService tser = new ReportService(VdopConstant.pushPath + "." + fileName);
			PageService pser = new PageService(json, VdopConstant.pushPath + "." + fileName);
			MVContext mv = pser.json2MV(false, false, null, false);
			String ret = tser.mv2XML2(mv);
			MvPageService.update(VdopConstant.pushPath + "." + fileName, ret);
			ExtContext.getInstance().removeMV(mv.getMvid());
		}else{
			//确认文件名
			fileName = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
			//生成XML文件
			JsonConfig jsonConfig = new JsonConfig();
			jsonConfig.registerJsonValueProcessor(JSONNull.class,new JSONNullProcessor());
			JSONObject json = JSONObject.fromObject(pageInfo, jsonConfig);
			ReportService tser = new ReportService(VdopConstant.pushPath + "." + fileName);
			PageService pser = new PageService(json, VdopConstant.pushPath + "." + fileName);
			MVContext mv = pser.json2MV(false, false, null, false);
			String ret = tser.mv2XML2(mv);
			String filePath = VdopConstant.pushPath + "." +  fileName;
			MvPageService.save(filePath, ret);
			this.daoHelper.getSqlMapClientTemplate().insert("web.report.release", this);
		}
		return null;
	}
	
	public String print() throws Exception{
		ExtContext.getInstance().removeMV(PageService.deftMvId);
		JsonConfig jsonConfig = new JsonConfig();
		jsonConfig.registerJsonValueProcessor(JSONNull.class,new JSONNullProcessor());
		JSONObject pageJson = JSONObject.fromObject(pageInfo, jsonConfig);
		PageService tser = new PageService(pageJson);
		MVContext mv = tser.json2MV(false, true, null,false);
		CompPreviewService ser = new CompPreviewService();
		ser.setParams(tser.getMvParams());
		ser.initPreview();
		String ret = ser.buildMV(mv);
		VDOPUtils.getRequest().setAttribute("data", ret);
		
		return "print";
	}
	
	public String view() throws Exception{
		JsonConfig jsonConfig = new JsonConfig();
		jsonConfig.registerJsonValueProcessor(JSONNull.class,new JSONNullProcessor());
		JSONObject pageJson = JSONObject.fromObject(pageInfo, jsonConfig);
		PageService tser = new PageService(pageJson);
		ExtContext.getInstance().removeMV(PageService.deftMvId);
		MVContext mv = tser.json2MV();
		CompPreviewService ser = new CompPreviewService();
		ser.setParams(tser.getMvParams());
		ser.initPreview();
		String ret = ser.buildMV(mv);
		VDOPUtils.getRequest().setAttribute("data", ret);
		return "view";
	}
	
	/**
	 * 根据JSON测试 报表
	 * @return
	 */
	public String test(){
		return "test";
	}
	
	/**
	 * 页面推送
	 * @return
	 * @throws Exception 
	 */
	public String push() throws Exception{
		ExtContext.getInstance().removeMV(PageService.deftMvId);
		//如果名称重复，直接更新，名字不存在直接插入
		List files = this.daoHelper.getSqlMapClientTemplate().queryForList("bi.portal.3gexist", this);
		if(files.size() == 0){  //不存在，直接插入
			this.userid = VDOPUtils.getLoginedUser().getUserId();
			this.fileName = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
			JsonConfig jsonConfig = new JsonConfig();
			jsonConfig.registerJsonValueProcessor(JSONNull.class,new JSONNullProcessor());
			JSONObject json = JSONObject.fromObject(pageInfo, jsonConfig);
			ReportService tser = new ReportService(VdopConstant.pushPath + "." + fileName);
			PageService pser = new PageService(json,VdopConstant.pushPath + "." + this.fileName);
			MVContext mv = pser.json2MV(false, false, null, true);
			String ret = tser.mv2XML2(mv);
			String fileName = VdopConstant.pushPath + "." + this.fileName;
			//把生成的xml内容存入表中
			MvPageService.save(fileName, ret);
			//写门户表
			this.purl = "control/extView?mvid="+fileName.replaceAll("\\.xml", "").replaceAll("/", ".")+"&returnJsp=false";
			Integer maxId = (Integer)daoHelper.getSqlMapClientTemplate().queryForObject("bi.portal.maxid", this);
			if(maxId == null){
				this.rid = 1;
			}else{
				this.rid = maxId + 1;
			}
			daoHelper.getSqlMapClientTemplate().insert("bi.portal.insertPortal", this);
		}else{
			//存在，更新文件
			Map dt = (Map)files.get(0);
			String mvid = (String)dt.get("mvid");
			ExtContext.getInstance().removeMV(VdopConstant.pushPath + "." + mvid);
			JsonConfig jsonConfig = new JsonConfig();
			jsonConfig.registerJsonValueProcessor(JSONNull.class,new JSONNullProcessor());
			JSONObject json = JSONObject.fromObject(pageInfo, jsonConfig);
			ReportService tser = new ReportService(VdopConstant.pushPath + "." + mvid);
			PageService pser = new PageService(json,VdopConstant.pushPath + "." + mvid);
			MVContext mv = pser.json2MV(false, false, null, true);
			String ret = tser.mv2XML2(mv);
			String fileName = VdopConstant.pushPath + "." + mvid;
			MvPageService.update(fileName, ret);
		}
		return null;
	}
	
	public String save() throws IOException {
		HttpServletResponse resp = VDOPUtils.getResponse();
		resp.setContentType("text/html; charset=UTF-8");
		this.userid = VDOPUtils.getLoginedUser().getUserId();
		if(pageId == null || pageId.length() == 0){
			//判断名字是否重复
			int cnt = (Integer)this.daoHelper.getSqlMapClientTemplate().queryForObject("bi.ext.report.reportExist", this);
			if(cnt > 0){
				resp.getWriter().print("no");
				return null;
			}
			this.pageId = daoHelper.getSqlMapClientTemplate().queryForObject("web.report.querypid", this).toString();
			JsonConfig jsonConfig = new JsonConfig();
			jsonConfig.registerJsonValueProcessor(JSONNull.class,new JSONNullProcessor());
			JSONObject page = JSONObject.fromObject(pageInfo, jsonConfig);
			page.put("id", Integer.parseInt(this.pageId));
			this.pageInfo = page.toString();
			daoHelper.getSqlMapClientTemplate().insert("web.report.insertUserSave", this);
		}else{
			daoHelper.getSqlMapClientTemplate().update("web.report.updateUserSave" , this);
		}
		
		resp.getWriter().print(pageId);
		
		return null;
	}

	public String getPageId() {
		return pageId;
	}

	public void setPageId(String pageId) {
		this.pageId = pageId;
	}

	public DaoHelper getDaoHelper() {
		return daoHelper;
	}

	public void setDaoHelper(DaoHelper daoHelper) {
		this.daoHelper = daoHelper;
	}

	public String getPageInfo() {
		return pageInfo;
	}

	public void setPageInfo(String pageInfo) {
		this.pageInfo = pageInfo;
	}


	public String getUserid() {
		return userid;
	}

	public int getRid() {
		return rid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	public void setRid(int rid) {
		this.rid = rid;
	}

	public String getPageName() {
		return pageName;
	}

	public void setPageName(String pageName) {
		this.pageName = pageName;
	}

	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	
	public String getPageNote() {
		return pageNote;
	}

	public void setPageNote(String pageNote) {
		this.pageNote = pageNote;
	}

	public String getCataId() {
		return cataId;
	}

	public void setCataId(String cataId) {
		this.cataId = cataId;
	}

	public String getPurl() {
		return purl;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public void setPurl(String purl) {
		this.purl = purl;
	}

	public String getMenus() {
		return menus;
	}

	public void setMenus(String menus) {
		this.menus = menus;
	}
	
}
