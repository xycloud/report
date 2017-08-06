package com.ruisi.vdop.web.report;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import com.ruisi.ext.engine.ExtConstants;
import com.ruisi.ext.engine.dao.DaoHelper;
import com.ruisi.ext.engine.view.context.ExtContext;
import com.ruisi.ext.engine.view.context.MVContext;
import com.ruisi.ext.engine.view.context.form.InputField;
import com.ruisi.ext.engine.view.emitter.ContextEmitter;
import com.ruisi.ext.engine.view.emitter.excel.ExcelEmitter;
import com.ruisi.ext.engine.view.emitter.pdf.PdfEmitter;
import com.ruisi.ext.engine.view.emitter.text.TextEmitter;
import com.ruisi.vdop.ser.bireport.ReportService;
import com.ruisi.vdop.ser.olap.CompPreviewService;
import com.ruisi.vdop.util.VDOPUtils;
import com.ruisi.vdop.util.VdopConstant;

public class ReportViewAction {
	
	private String mvid;
	private String type; //导出方式
	private Integer rid; 
	private String userid;
	
	public String execute(){
		//进行鉴权
		DaoHelper dao = VDOPUtils.getDaoHelper();
		this.userid = VDOPUtils.getLoginedUser().getUserId();
		List ls = dao.getSqlMapClientTemplate().queryForList("bi.ext.report.authReport", this); 
		if(ls.size() == 0){
			return null;
		}
		Map m = (Map)ls.get(0);
		VDOPUtils.getRequest().setAttribute("data", m);
		return "success";
	}
	
	/**
	 * 在公有报表管理的时候查看
	 * @return
	 */
	public String authView(){
		return "auth";
	}
	
	public String export() throws Exception  {
		MVContext mv = ExtContext.getInstance().getMVContext(this.mvid);
		Map<String, InputField> params = ExtContext.getInstance().getParams(this.mvid);
		ReportService tser = new ReportService();
		CompPreviewService ser = new CompPreviewService();
		ser.setParams(params);
		ser.initPreview();
		
		String fileName = "file.";
		if("html".equals(this.type)){
			fileName += "html";
		}else
		if("excel".equals(this.type)){
			fileName += "xls";
		}else
		if("csv".equals(this.type)){
			fileName += "csv";
		}else
		if("pdf".equals(this.type)){
			fileName += "pdf";
		}
		
		HttpServletResponse resp = VDOPUtils.getResponse();
		resp.setContentType("application/x-msdownload");
		String contentDisposition = "attachment; filename=\""+fileName+"\"";
		resp.setHeader("Content-Disposition", contentDisposition);
		
		if("html".equals(this.type)){
			String ret = ser.buildMV(mv);
			String html = tser.htmlPage(ret, VDOPUtils.getConstant("resPath"));
			InputStream is = IOUtils.toInputStream(html, "utf-8");
			IOUtils.copy(is, resp.getOutputStream());
			is.close();
		}else
		if("excel".equals(this.type)){
			ContextEmitter emitter = new ExcelEmitter();
			ser.buildMV(mv, emitter);
		}else
		if("csv".equals(this.type)){
			ContextEmitter emitter = new TextEmitter();
			String ret = ser.buildMV(mv, emitter);
			InputStream is = IOUtils.toInputStream(ret, "gb2312");
			IOUtils.copy(is, resp.getOutputStream());
			is.close();
		}else 
		if("pdf".equals(this.type)){
			ContextEmitter emitter = new PdfEmitter();
			ser.buildMV(mv, emitter);
		}
		
		return null;
	}
	
	public String print() throws Exception {
		MVContext mv = ExtContext.getInstance().getMVContext(this.mvid);
		Map<String, InputField> params = ExtContext.getInstance().getParams(this.mvid);
		
		CompPreviewService ser = new CompPreviewService();
		ser.setParams(params);
		ser.initPreview();
		
		String ret = ser.buildMV(mv);
		VDOPUtils.getRequest().setAttribute("data", ret);
		
		return "print";
	}

	public String getMvid() {
		return mvid;
	}

	public void setMvid(String mvid) {
		this.mvid = mvid;
	}

	public String getType() {
		return type;
	}

	public Integer getRid() {
		return rid;
	}

	public void setRid(Integer rid) {
		this.rid = rid;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}
}
