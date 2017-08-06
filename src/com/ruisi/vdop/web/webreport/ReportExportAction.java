package com.ruisi.vdop.web.webreport;

import java.io.InputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.io.IOUtils;

import net.sf.json.JSONNull;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import com.ruisi.ext.engine.view.context.ExtContext;
import com.ruisi.ext.engine.view.context.MVContext;
import com.ruisi.ext.engine.view.emitter.ContextEmitter;
import com.ruisi.ext.engine.view.emitter.excel.ExcelEmitter;
import com.ruisi.ext.engine.view.emitter.pdf.PdfEmitter;
import com.ruisi.ext.engine.view.emitter.text.TextEmitter;
import com.ruisi.ext.engine.view.emitter.word.WordEmitter;
import com.ruisi.vdop.ser.olap.CompPreviewService;
import com.ruisi.vdop.ser.webreport.JSONNullProcessor;
import com.ruisi.vdop.ser.webreport.PageService;
import com.ruisi.vdop.util.VDOPUtils;

public class ReportExportAction {
	
	private String type; //导出方式 
	private String pageInfo; //报表JSON
	
	public String execute() throws Exception{
		JsonConfig jsonConfig = new JsonConfig();
		jsonConfig.registerJsonValueProcessor(JSONNull.class,new JSONNullProcessor());
		JSONObject rjson = JSONObject.fromObject(pageInfo, jsonConfig);
		PageService tser = new PageService(rjson);
		ExtContext.getInstance().removeMV(PageService.deftMvId);
		MVContext mv = tser.json2MV(true, true, this.type, false);
		CompPreviewService ser = new CompPreviewService();
		ser.setParams(tser.getMvParams());
		ser.initPreview();
		//String ret = ser.buildMV(mv);
		
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
		}else 
		if("word".equals(this.type)){
			fileName += "docx";
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
		}else 
		if("word".equals(this.type)){
			ContextEmitter emitter = new WordEmitter();
			ser.buildMV(mv, emitter);
		}
		
		return null;
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getPageInfo() {
		return pageInfo;
	}

	public void setPageInfo(String pageInfo) {
		this.pageInfo = pageInfo;
	}
}
