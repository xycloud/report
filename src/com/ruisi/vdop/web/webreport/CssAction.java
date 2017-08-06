package com.ruisi.vdop.web.webreport;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import net.sf.json.JSONArray;

import com.ruisi.vdop.ser.webreport.PageService;
import com.ruisi.vdop.util.VDOPUtils;

/**
 * 控制报表样式
 * @author hq
 * @date 2015-7-25
 */
public class CssAction {
	
	private String fileName;
	
	private String style;
	
	public String savecss() throws IOException{
		//解析Css
		JSONArray json = JSONArray.fromObject(style);
		String cssctx = PageService.style2file(json);
		
		String path = VDOPUtils.getServletContext().getRealPath("/") + "/pic/css/";
		path = path + fileName+".css";
		
		FileUtils.writeStringToFile(new File(path), cssctx, "utf-8");
		return null;
	}
	
	public String listcss() throws IOException{
		String path = VDOPUtils.getServletContext().getRealPath("/") + "/pic/css";
		File f = new File(path);
		if(!f.exists()){
			f.mkdirs();
		}
		File[] ls = f.listFiles(new FileFilter(){

			public boolean accept(File arg0) {
				String name = arg0.getName();
				if(name.endsWith("css")){
					return true;
				}else{
					return false;
				}
			}
			
		});
		List ret = new ArrayList();
		for(int i=0; ls != null && i<ls.length; i++){
			String name = ls[i].getName();
			Date dt = new Date(ls[i].lastModified());
			Map m = new HashMap();
			m.put("name", name);
			m.put("dt", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(dt));
			ret.add(m);
		}
		
		VDOPUtils.getResponse().setContentType("text/html; charset=UTF-8");
		VDOPUtils.getResponse().getWriter().print(JSONArray.fromObject(ret));
		
		return null;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}
	
	
}
