package com.ruisi.vdop.web.webreport;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import org.apache.commons.io.FileUtils;

import com.ruisi.ext.engine.cross.CrossOutData;
import com.ruisi.ext.engine.init.ExtEnvirContext;
import com.ruisi.ext.engine.view.context.ExtContext;
import com.ruisi.ext.engine.view.context.MVContext;
import com.ruisi.ext.engine.view.context.chart.ChartContext;
import com.ruisi.ext.engine.view.emitter.ContextEmitter;
import com.ruisi.ext.engine.view.emitter.JavaContextEmitterImpl;
import com.ruisi.ext.engine.view.emitter.UserJavaEmitter;
import com.ruisi.ext.engine.view.emitter.excel.ExcelLayoutEnginer;
import com.ruisi.ext.engine.view.emitter.highcharts.ShowHighCharts;
import com.ruisi.ext.engine.view.emitter.highcharts.chart.LineChart;
import com.ruisi.ext.engine.wrapper.ExtRequest;
import com.ruisi.ext.engine.wrapper.ExtResponse;
import com.ruisi.ext.engine.wrapper.ExtWriter;
import com.ruisi.ext.engine.wrapper.StringBufferWriterImpl;
import com.ruisi.vdop.ser.olap.CompPreviewService;
import com.ruisi.vdop.ser.webreport.ChartService;
import com.ruisi.vdop.ser.webreport.JSONNullProcessor;
import com.ruisi.vdop.ser.webreport.PageService;
import com.ruisi.vdop.util.VDOPUtils;

public class ChartAction {
	
	public String kpiJson;
	private String chartJson;
	private String datasource;
	private String dataset;
	private String cube;// 当前图形使用数据立方体
	private String params;//当前参数
	
	private File image; //上传的文件
	private String imageFileName; //文件名称
	private String imageContentType; //文件类型
	
	private String mvid;
	
	public String chartType(){
		return "chartType";
	}
	
	public String view() throws Exception{
		JsonConfig jsonConfig = new JsonConfig();
		jsonConfig.registerJsonValueProcessor(JSONNull.class,new JSONNullProcessor());
		ExtContext.getInstance().removeMV(PageService.deftMvId);
		JSONObject chartj = JSONObject.fromObject(chartJson, jsonConfig);
		JSONArray kpij = JSONArray.fromObject(kpiJson, jsonConfig);
		JSONObject dsource = JSONObject.fromObject(datasource, jsonConfig);
		JSONObject dset = JSONObject.fromObject(dataset, jsonConfig);
		JSONObject jcube = JSONObject.fromObject(this.cube, jsonConfig);
		JSONArray jparams = JSONArray.fromObject(this.params, jsonConfig);
		
		LineChart.maxsercnt = 2;
		
		ChartService cs = new ChartService(null, false);
		
		MVContext mv = cs.json2MV(chartj, kpij, dsource, dset, jcube, jparams, true);
		
		CompPreviewService ser = new CompPreviewService();
		ser.setParams(cs.getMvParams());
		ser.initPreview();
		
		String ret = ser.buildMV(mv);
		
		HttpServletResponse resp = VDOPUtils.getResponse();
		resp.setContentType("text/html; charset=UTF-8");
		resp.getWriter().print(ret);
		
		LineChart.maxsercnt = LineChart.maxsercntDef;
		return null;
	}
	
	public String showPic() throws IOException{
		byte[] chartbt = (byte[])VDOPUtils.getSession().getAttribute(mvid);
		HttpServletResponse resp = VDOPUtils.getResponse();
		resp.setContentType("image/jpeg");
				
		resp.getOutputStream().write(chartbt);
		VDOPUtils.getSession().removeAttribute(mvid);
		return null;
	}
	
	public String listPics(){
		String path = VDOPUtils.getServletContext().getRealPath("/");
		path = path + "pic/resource";
		File p = new File(path);
		if(!p.exists()){
			p.mkdirs();
		}
		File[] fs = p.listFiles(new FileFilter(){

			public boolean accept(File arg0) {
				String name = arg0.getName();
				if(name.endsWith("gif") || name.endsWith("jpg") || name.endsWith("png")){
					return true;
				}else{
					return false;
				}
			}
			
		});
		List ret = new ArrayList();
		for(int i=0; fs != null && i<fs.length; i++){
			Map m = new HashMap();
			String name = fs[i].getName();
			m.put("name", name);
			m.put("path", "pic/resource/");
			ret.add(m);
		}
		VDOPUtils.getRequest().setAttribute("ls", ret);
		return "listpic";
	}
	
	public String uploadPic() throws IOException{
		String path = VDOPUtils.getServletContext().getRealPath("/");
		path = path + "pic";
		File p = new File(path);
		if(!p.exists()){
			p.mkdirs();
		}
		String name = UUID.randomUUID().toString().replaceAll("-", "");
		String extName = imageFileName.substring(imageFileName.lastIndexOf('.'));
		File target = new File(path + "/" + name + extName);
		
		FileUtils.copyFile(this.image, target);
		
		VDOPUtils.getRequest().setAttribute("fileName", target.getName());
		
		return "upload";
	}
	
	public String toPic() throws Exception{
		final String mid = "mv.pic." + String.valueOf(new Random().nextInt(5000));
		JsonConfig jsonConfig = new JsonConfig();
		jsonConfig.registerJsonValueProcessor(JSONNull.class,new JSONNullProcessor());
		JSONObject chartj = JSONObject.fromObject(chartJson, jsonConfig);
		JSONArray kpij = JSONArray.fromObject(kpiJson, jsonConfig);
		JSONObject dsource = JSONObject.fromObject(datasource, jsonConfig);
		JSONObject dset = JSONObject.fromObject(dataset, jsonConfig);
		JSONObject jcube = JSONObject.fromObject(this.cube, jsonConfig);
		JSONArray jparams = JSONArray.fromObject(this.params, jsonConfig);
		
		LineChart.maxsercnt = 2;
		
		ChartService cs = new ChartService(mid, null);
		
		MVContext mv = cs.json2MV(chartj, kpij, dsource, dset, jcube, jparams, false);
		
		CompPreviewService ser = new CompPreviewService();
		ser.setParams(cs.getMvParams());
		ser.initPreview();
		final HttpSession session = VDOPUtils.getSession();
		
		ContextEmitter emitter = new JavaContextEmitterImpl(new UserJavaEmitter(){
			
			private ExtWriter out;
			private ExtRequest request;
			private ExtResponse response;
			private ExtEnvirContext ctx;

			public void initialize(ExtWriter out, ExtRequest request,
					ExtResponse response, ExtEnvirContext ctx) {
				this.out = out;
				this.request = request;
				this.response = response;
				this.ctx = ctx;
			}

			public void startChart(ChartContext ctx) {
				ExtWriter out = new StringBufferWriterImpl();
				ShowHighCharts chart = new ShowHighCharts(out, ctx, this.request, this.ctx, true);
				chart.show();
				String chartjs = out.toString();
				byte[] chartbt = ExcelLayoutEnginer.loadChart(chartjs, Integer.parseInt(ctx.getWidth()), Integer.parseInt(ctx.getHeight()));
				session.setAttribute(mid, chartbt);
			}

			public void startCrossReport(CrossOutData data) {
				
			}
			
		});
		ser.buildMV(mv, emitter);
		
		LineChart.maxsercnt = LineChart.maxsercntDef;
		ExtContext.getInstance().removeMV(mid);
		VDOPUtils.getResponse().getWriter().print(mid);
		return null;
	}

	public String getKpiJson() {
		return kpiJson;
	}

	public String getChartJson() {
		return chartJson;
	}

	public String getDatasource() {
		return datasource;
	}

	public String getDataset() {
		return dataset;
	}

	public void setKpiJson(String kpiJson) {
		this.kpiJson = kpiJson;
	}

	public void setChartJson(String chartJson) {
		this.chartJson = chartJson;
	}

	public void setDatasource(String datasource) {
		this.datasource = datasource;
	}

	public void setDataset(String dataset) {
		this.dataset = dataset;
	}

	public File getImage() {
		return image;
	}

	public void setImage(File image) {
		this.image = image;
	}

	public String getImageContentType() {
		return imageContentType;
	}

	public void setImageContentType(String imageContentType) {
		this.imageContentType = imageContentType;
	}

	public String getImageFileName() {
		return imageFileName;
	}

	public void setImageFileName(String imageFileName) {
		this.imageFileName = imageFileName;
	}

	public String getCube() {
		return cube;
	}

	public void setCube(String cube) {
		this.cube = cube;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	public String getMvid() {
		return mvid;
	}

	public void setMvid(String mvid) {
		this.mvid = mvid;
	}
}
