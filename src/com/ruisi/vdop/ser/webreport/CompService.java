package com.ruisi.vdop.ser.webreport;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.ruisi.ext.engine.ConstantsEngine;
import com.ruisi.ext.engine.ExtConstants;
import com.ruisi.ext.engine.init.TemplateManager;
import com.ruisi.ext.engine.util.IdCreater;
import com.ruisi.ext.engine.view.context.Element;
import com.ruisi.ext.engine.view.context.ExtContext;
import com.ruisi.ext.engine.view.context.MVContext;
import com.ruisi.ext.engine.view.context.chart.ChartContext;
import com.ruisi.ext.engine.view.context.cross.CrossReportContext;
import com.ruisi.ext.engine.view.context.cross.RowDimContext;
import com.ruisi.ext.engine.view.context.dc.grid.GridDataCenterContext;
import com.ruisi.ext.engine.view.context.form.InputField;
import com.ruisi.ext.engine.view.context.form.TextFieldContext;
import com.ruisi.ext.engine.view.context.form.TextFieldContextImpl;
import com.ruisi.ext.engine.view.context.gridreport.GridReportContext;
import com.ruisi.ext.engine.view.context.html.DataContext;
import com.ruisi.ext.engine.view.context.html.DataContextImpl;
import com.ruisi.ext.engine.view.context.html.DivContext;
import com.ruisi.ext.engine.view.context.html.DivContextImpl;
import com.ruisi.ext.engine.view.context.html.TextContext;
import com.ruisi.ext.engine.view.context.html.TextContextImpl;
import com.ruisi.ext.engine.view.context.html.TextProperty;
import com.ruisi.ext.engine.view.context.html.table.TdContext;
import com.ruisi.ext.engine.view.exception.ExtConfigException;
import com.ruisi.vdop.ser.bireport.TableSqlJsonVO;
import com.ruisi.vdop.ser.bireport.TableSqlJsonVO.KpiInfo;

public class CompService {
	
	private MVContext mv;
	private PageService pageSer;
	private ChartService chartSer;
	private DataService dataService = new DataService();
	private CrossReportService crsService;
	private TableService tabService;
	private StaticCrossService scrossService;
	
	public CompService(MVContext mv, PageService pageSer, boolean isexport, String outType){
		this.mv = mv;
		this.pageSer = pageSer;
		this.tabService = new TableService(pageSer.getCss(), pageSer.getScripts());
		this.crsService = new CrossReportService(pageSer.getCss(), pageSer.getScripts());
		this.scrossService = new StaticCrossService(pageSer.getCss(), pageSer.getScripts());
		this.chartSer = new ChartService(outType, isexport);
	}
	
	public void createChart(JSONObject json, TdContext td, boolean insertParam) throws IOException, ExtConfigException{
		//给chart生成div边框, 用来控制样式
		StringBuffer css = this.pageSer.getCss();
		DivContext ctx = new DivContextImpl();
		ctx.setId("C"+IdCreater.create());
		td.getChildren().add(ctx);
		ctx.setParent(td);
		ctx.setChildren(new ArrayList<Element>());
		JSONObject style = (JSONObject)json.get("style");
		css.append("#"+ctx.getId()+"{");
		this.pageSer.dealCompBorder(style, css);
		css.append("}");
		
		JSONObject chartJson = json.getJSONObject("chartJson");
		JSONArray kpiJson = json.getJSONArray("kpiJson");
		TableSqlJsonVO sqlVO = chartSer.json2ChartSql(chartJson, kpiJson);
		boolean useCube = false;
		String income = (String)json.get("income");
		if("cube".equals(income)){
			JSONObject cube = this.pageSer.findCubeById(json.getString("cubeid"));
			if("olap".equals(cube.get("from"))){
				useCube = true;
			}
		}
		ChartContext cr = chartSer.json2Chart(chartJson, sqlVO, false, useCube, json.getString("id"), true);
		String dsourceId = null;
		String sql = "";
		//useCube == true 表示当前图形使用立方体数据，并且立方体数据是从多维分析建模而来
		if(useCube){
			sql = this.chartSer.crtSqlByOlapCube(chartJson, kpiJson, this.pageSer.findCubeById(json.getString("cubeid")));
		}else{
			//income = "cube" 表示当前图形使用立方体的数据，不过立方体是从数据集创建而来。
			if("cube".equalsIgnoreCase(json.getString("income"))){
				JSONObject cube = this.pageSer.findCubeById(json.getString("cubeid"));
				JSONObject dset = pageSer.findDataSetById(cube.getString("datasetid"));
				dsourceId = dset.getString("dsid");
				sql = dataService.createDatasetSql(dset);
			}else{
				JSONObject dset = pageSer.findDataSetById(json.getString("datasetid"));
				dsourceId = dset.getString("dsid");
				sql = dataService.createDatasetSql(dset);
			}
		}
		
		//判断是否有事件，是否需要添加参数
		JSONObject linkAccept = (JSONObject)chartJson.get("linkAccept");
		if(linkAccept != null && !linkAccept.isNullObject() && !linkAccept.isEmpty()){
			//判断参数是否存在，
			if(!this.isParamExist((String)linkAccept.get("col"))){
				//创建参数
				TextFieldContext linkText = new TextFieldContextImpl();
				linkText.setType("hidden");
				linkText.setShow(true);
				linkText.setDefaultValue((String)linkAccept.get("dftval"));
				linkText.setId((String)linkAccept.get("col"));
				mv.getChildren().add(0, linkText);
				linkText.setParent(mv);
				if(insertParam){
					this.pageSer.getMvParams().put(linkText.getId(), linkText);
					ExtContext.getInstance().putServiceParam(mv.getMvid(), linkText.getId(), linkText);
					mv.setShowForm(true);
				}	
			}
		}
		
		//把chart放入mv
		if(mv.getCharts() == null){
			mv.setCharts(new HashMap<String, ChartContext>());
		}
		mv.getCharts().put(cr.getId(), cr);
		
		GridDataCenterContext dc = chartSer.createDataCenter(sqlVO, sql, cr, dsourceId, useCube, linkAccept);
		cr.setRefDataCenter(dc.getId());
		if(mv.getGridDataCenters() == null){
			mv.setGridDataCenters(new HashMap<String, GridDataCenterContext>());
		}
		mv.getGridDataCenters().put(dc.getId(), dc);
		
		ctx.getChildren().add(cr);
		cr.setParent(ctx);
	}
	
	public void createPic(TdContext td, JSONObject compJson){
		StringBuffer css = this.pageSer.getCss();
		JSONObject style = (JSONObject)compJson.get("style");
		TextContext text = new TextContextImpl();
		String url = compJson.getString("url");
		String income = compJson.getString("income");
		String width = (String)style.get("width");
		String height = (String)style.get("height");
		String align = (String)style.get("align");
		String id = "C" + IdCreater.create();
		StringBuffer sb = new StringBuffer();
		sb.append("<div id=\""+id+"\"><img src='");
		if("file".equals(income)){
			sb.append("../pic/" + url);
		}else{
			sb.append(url);
		}
		sb.append("'");
		if(width != null && width.length() > 0){
			sb.append(" width='"+width+"'");
		}
		if(height != null && height.length() > 0){
			sb.append(" height='"+height+"'");
		}
		sb.append("></div>");
		
		css.append("#"+id+"{");
		if(align != null && align.length() > 0){
			css.append("text-align:"+align+";");
		}
		this.pageSer.dealCompBorder(style, css);
		css.append("}");
		
		text.setText(sb.toString());
		text.setParent(td);
		td.getChildren().add(text);
	}
	
	public void createLabel(String type, TdContext td, JSONObject compJson) throws IOException{
		StringBuffer css = this.pageSer.getCss();
		TextContext text = new TextContextImpl();
		JSONObject style = (JSONObject)compJson.get("style");
		if(style != null && !style.isNullObject() && !style.isEmpty()){
			TextProperty tp = new TextProperty();
			tp.setAlign((String)style.get("align"));
			tp.setHeight((String)style.get("fontheight"));
			tp.setSize((String)style.get("fontsize"));
			tp.setStyleClass((String)style.get("cstyle"));
			String fontweight = (String)style.get("fontweight");
			if("true".equals(fontweight)){
				tp.setWeight("bold");
			}
			tp.setColor((String)style.get("fontcolor"));
			tp.setId("C"+IdCreater.create());
			text.setTextProperty(tp);
			
			css.append("#"+tp.getId()+"{");
			String italic = (String)style.get("italic");
			String underscore = (String)style.get("underscore");
			String lineheight = (String)style.get("lineheight");
			if("true".equals(italic)){
				css.append("font-style:italic;");
			}
			if("true".equals(underscore)){
				css.append("text-decoration: underline;");
			}
			if(lineheight != null && lineheight.length() > 0){
				css.append("line-height:"+lineheight+"px;");
			}
			PageService.dealCompBorder(style, css);
			
			css.append("}");
		}
		if(type.equals("label")){
			text.setText(compJson.getString("desc"));
		}else{
			text.setFormatHtml(true); // 格式化 html 到 导出所需对象 (word/pdf)
			String desc = compJson.getString("desc");
			String dsetId = (String)compJson.get("datasetId");
			if(dsetId == null || dsetId.length() == 0){
				text.setText(desc);
			}else{
				String key = "key" + IdCreater.create();
				desc = desc.replaceAll("\\[\\$(\\w+)\\]", "\\${"+key+".$1}"); //替换为表达式
				String name = TemplateManager.getInstance().createTemplate(desc);
				text.setTemplateName(name);
				
				//创建数据集
				DataContext data = this.crtDataSet(key, dsetId);
				data.setParent(td);
				td.getChildren().add(data); //放入第一行
			}
		}
		text.setParent(td);
		text.setFormatEnter(true);
		td.getChildren().add(text);
		
	}
	
	public DataContext crtDataSet(String key, String datasetId) throws IOException{
		JSONObject dset = this.pageSer.findDataSetById(datasetId);
		DataContext ctx = new DataContextImpl();
		ctx.setRefDsource(dset.getString("dsid"));
		ctx.setKey(key);
		ctx.setMulti(false);
		String sql = dataService.createDatasetSql(dset);
		String tname = TemplateManager.getInstance().createTemplate(sql);
		ctx.setTemplateName(tname);
		return ctx;
	}
	
	public void crtTable(JSONObject json, TdContext td, boolean insertParam) throws IOException, ExtConfigException{
		StringBuffer css = this.pageSer.getCss();
		this.tabService.init();
		GridReportContext grid = this.tabService.json2Table(json);
		
		//处理表格样式
		JSONObject style = (JSONObject)json.get("style");
		if(style != null && !style.isNullObject() && !style.isEmpty()){
			String lock = (String)json.get("lockhead");
			css.append("#"+grid.getId()+"{");
			String margin = (String)style.get("margin");  //margin用来控制组件和其他组件的位置 ,而不是th/td
			if(margin != null && margin.length() > 0){
				String[] p = margin.split(",");
				if(p.length == 1){
					css.append("margin:" + p[0]+"px;");
				}else{
					css.append("margin:" + (p.length>0&&p[0].length()>0?p[0]:"0")+"px "+(p.length>1&&p[1].length()>0?p[1]:"0")+"px "+(p.length>2&&p[2].length()>0?p[2]:"0")+"px "+(p.length>3&&p[3].length()>0?p[3]:"0")+"px;");
				}
			}
			//宽度，高度
			String compwidth = (String)style.get("compwidth");
			String compheight = (String)style.get("compheight");
			grid.setWidth(compwidth);
			grid.setHeight(compheight);
			
			css.append("}");
			if("true".equals(lock)){
				css.append("#"+grid.getId()+" #head-"+grid.getId()+" th, #"+grid.getId()+" #body-"+grid.getId()+" td {");
			}else{
				css.append("div.crossReport #T_"+grid.getId()+" td, div.crossReport #T_"+grid.getId()+" th{");
			}
			PageService.dealCompBorder(style, css);
			css.append("}");
		}
		JSONObject dset = null;
		String dsourceId = null;
		JSONObject linkAccept = (JSONObject)json.get("linkAccept");
		dset = this.pageSer.findDataSetById(json.getString("datasetid"));
		dsourceId = dset.getString("dsid");
		
		//income表示数据是从立方体(cube)来，还是数据集(dataset)来, //现在都是从数据集来了。
		GridDataCenterContext dc = this.tabService.createDataCenter(dset, dsourceId, linkAccept);
		grid.setRefDataCenter(dc.getId());
		
		//判断是否有事件，是否需要添加参数
		if(linkAccept != null && !linkAccept.isNullObject() && !linkAccept.isEmpty()){
			//判断参数是否存在，
			if(!this.isParamExist((String)linkAccept.get("col"))){
				//创建参数
				TextFieldContext linkText = new TextFieldContextImpl();
				linkText.setType("hidden");
				linkText.setDefaultValue((String)linkAccept.get("dftval"));
				linkText.setId((String)linkAccept.get("col"));
				linkText.setShow(true);
				mv.getChildren().add(0, linkText);
				linkText.setParent(mv);
				if(insertParam){
					this.pageSer.getMvParams().put(linkText.getId(), linkText);
					ExtContext.getInstance().putServiceParam(mv.getMvid(), linkText.getId(), linkText);
					mv.setShowForm(true);
				}
			}
		}
		
		//把DataCenter放入mv
		if(mv.getGridDataCenters() == null){
			mv.setGridDataCenters(new HashMap<String, GridDataCenterContext>());
		}
		mv.getGridDataCenters().put(dc.getId(), dc);
		
		td.getChildren().add(grid);
		grid.setParent(td);
		
		//把grid回写到mv
		if(mv.getGridReports() == null || mv.getGridReports().size() == 0){
			Map<String, GridReportContext> grids = new HashMap<String, GridReportContext>();
			mv.setGridReports(grids);
		}
		mv.getGridReports().put(grid.getId(), grid);
	}
	
	public void crtStaticCross(JSONObject json, TdContext td, boolean insertParam) throws IOException {
		CrossReportContext crsreport = scrossService.json2Table(json);
		JSONObject cube = this.pageSer.findCubeById(json.getString("cubeId"));
		TableSqlJsonVO sqlVO = scrossService.json2TalbeSqlVO(json);
		
		//处理表格样式
		StringBuffer css = this.pageSer.getCss();
		JSONObject style = (JSONObject)json.get("style");
		if(style != null && !style.isNullObject() && !style.isEmpty()){
			css.append("div.crossReport #T_"+crsreport.getId()+"{border-collapse:collapse;table-layout:fixed;");
			String margin = (String)style.get("margin");  //margin用来控制table ,而不是th/td
			if(margin != null && margin.length() > 0){
				String[] p = margin.split(",");
				if(p.length == 1){
					css.append("margin:" + p[0]+"px;");
				}else{
					css.append("margin:" + (p.length>0&&p[0].length()>0?p[0]:"0")+"px "+(p.length>1&&p[1].length()>0?p[1]:"0")+"px "+(p.length>2&&p[2].length()>0?p[2]:"0")+"px "+(p.length>3&&p[3].length()>0?p[3]:"0")+"px;");
				}
			}
			css.append("}");
			css.append("div.crossReport #T_"+crsreport.getId()+" td, div.crossReport #T_"+crsreport.getId()+" th{");
			PageService.dealCompBorder(style, css);
			css.append("}");
		}
		
		GridDataCenterContext dc = null;
		if("olap".equals((String)json.get("from"))){  //从多维立方体来
			JSONObject tableJson = json.getJSONObject("tableJson");  //需要把baseKpi 的 指标加入 sqlVO 的指标中，用来生成SQL
			JSONObject baseKpi = (JSONObject)tableJson.get("baseKpi");
			if(baseKpi != null && !baseKpi.isNullObject() && !baseKpi.isEmpty()){
				if(sqlVO.getKpiByCol(baseKpi.getString("colname")) == null){ 
					KpiInfo kpi = new KpiInfo();
					kpi.setColName(baseKpi.getString("colname"));
					kpi.setAlias(baseKpi.getString("kpi"));
					kpi.setAggre(baseKpi.getString("aggre"));
					kpi.setCalc(baseKpi.getInt("calc"));
					sqlVO.getKpis().add(kpi);
				}
			}
			//处理cube定义的参数，只在立方体中有效
			Object param = cube.get("param");
			String tname = json.getString("tname");
			dc = scrossService.createDataCenterByOlap(sqlVO, (JSONArray)param, tname);
		}else{
			// 从自定义立方体来
			JSONObject dset = this.pageSer.findDataSetById(cube.getString("datasetid"));
			dc = scrossService.createDataCenter(dset, (String)dset.get("dsid"), sqlVO);
			
		}
		crsreport.setRefDataCetner(dc.getId());
		if(mv.getGridDataCenters() == null){
			mv.setGridDataCenters(new HashMap<String, GridDataCenterContext>());
		}
		mv.getGridDataCenters().put(dc.getId(), dc);
		
		td.getChildren().add(crsreport);
		crsreport.setParent(td);
		if(mv.getCrossReports() == null){
			mv.setCrossReports(new HashMap<String, CrossReportContext>());
		}
		mv.getCrossReports().put(crsreport.getId(), crsreport);
	}
	
	public void crtCrossReport(JSONObject json, TdContext td, boolean insertParam) throws ParseException, IOException, ExtConfigException{
		JSONObject tableJson = json.getJSONObject("tableJson");
		JSONArray kpiJson = json.getJSONArray("kpiJson");
		JSONObject linkAccept = (JSONObject)json.get("linkAccept");
		TableSqlJsonVO sqlVO = crsService.json2TableSql(tableJson, kpiJson);
		CrossReportContext crsreport = crsService.json2Table(json, sqlVO);
		JSONObject cube = this.pageSer.findCubeById(json.getString("cubeId"));
		
		//处理表格样式
		StringBuffer css = this.pageSer.getCss();
		JSONObject style = (JSONObject)json.get("style");
		if(style != null && !style.isNullObject() && !style.isEmpty()){
			String id = crsreport.getId();
			String lockhead = (String)json.get("lockhead");  //是否锁定表头，锁定表头和未锁定表头控制样式不一样
			String margin = (String)style.get("margin");  //margin用来控制组件和其他组件的位置 ,而不是th/td
			css.append("#"+id+"{");
			if(margin != null && margin.length() > 0){
				String[] p = margin.split(",");
				if(p.length == 1){
					css.append("margin:" + p[0]+"px;");
				}else{
					css.append("margin:" + (p.length>0&&p[0].length()>0?p[0]:"0")+"px "+(p.length>1&&p[1].length()>0?p[1]:"0")+"px "+(p.length>2&&p[2].length()>0?p[2]:"0")+"px "+(p.length>3&&p[3].length()>0?p[3]:"0")+"px;");
				}
			}
			css.append("} \n");
			css.append("#"+id+("true".equals(lockhead)?" #head-"+id : " #T_"+id)+" th, #"+id+" "+("true".equals(lockhead)?" #body-"+id:" #T_"+id)+" td{");
			this.pageSer.dealCompBorder(style, css);
			css.append("}");
		}
		
		GridDataCenterContext dc = null;
		if("olap".equals((String)json.get("from"))){
			//处理cube定义的参数，只在立方体中有效
			Object param = cube.get("param");
			dc = crsService.createDataCenterByOlap(json, (JSONArray)param, linkAccept, 0);
			//判断是否有钻取维
			List<RowDimContext> drillDims = crsreport.getDims();
			for(int i=0; drillDims!=null&&i<drillDims.size(); i++){
				RowDimContext drillDim = drillDims.get(i);
				//生成钻取维的DataCenter
				GridDataCenterContext drillDc = crsService.createDataCenterByOlap(json, (JSONArray)param, null, i+1);
				drillDim.setRefDataCenter(drillDc.getId());
				if(mv.getGridDataCenters() == null){
					mv.setGridDataCenters(new HashMap<String, GridDataCenterContext>());
				}
				mv.getGridDataCenters().put(drillDc.getId(), drillDc);
			}
		}else{
			JSONObject dset = this.pageSer.findDataSetById(cube.getString("datasetid"));
			dc = crsService.createDataCenter(tableJson, kpiJson, dset, dset.getString("dsid"), sqlVO, linkAccept);
			//判断是否有钻取维
			List<RowDimContext> drillDims = crsreport.getDims();
			for(int i=0; drillDims!=null&&i<drillDims.size(); i++){
				RowDimContext drillDim = drillDims.get(i);
				//生成钻取维的DataCenter
				GridDataCenterContext drillDc = crsService.createDataCenter(tableJson, kpiJson, dset, dset.getString("dsid"), sqlVO, linkAccept);
				drillDim.setRefDataCenter(drillDc.getId());
				if(mv.getGridDataCenters() == null){
					mv.setGridDataCenters(new HashMap<String, GridDataCenterContext>());
				}
				mv.getGridDataCenters().put(drillDc.getId(), drillDc);
			}
		}
		crsreport.setRefDataCetner(dc.getId());
		if(mv.getGridDataCenters() == null){
			mv.setGridDataCenters(new HashMap<String, GridDataCenterContext>());
		}
		mv.getGridDataCenters().put(dc.getId(), dc);
		
		//判断是否有事件，是否需要添加参数
		if(linkAccept != null && !linkAccept.isNullObject() && !linkAccept.isEmpty()){
			//判断参数是否存在，
			if(!this.isParamExist((String)linkAccept.get("col"))){
				//创建参数
				TextFieldContext linkText = new TextFieldContextImpl();
				linkText.setType("hidden");
				linkText.setShow(true);
				linkText.setDefaultValue((String)linkAccept.get("dftval"));
				linkText.setId((String)linkAccept.get("col"));
				mv.getChildren().add(0, linkText);
				linkText.setParent(mv);
				if(insertParam){
					this.pageSer.getMvParams().put(linkText.getId(), linkText);
					ExtContext.getInstance().putServiceParam(mv.getMvid(), linkText.getId(), linkText);
					mv.setShowForm(true);
				}
			}
		}
		
		td.getChildren().add(crsreport);
		crsreport.setParent(td);
		if(mv.getCrossReports() == null){
			mv.setCrossReports(new HashMap<String, CrossReportContext>());
		}
		mv.getCrossReports().put(crsreport.getId(), crsreport);
		
		//判断是否有斜线表头，生成斜线表头代码
		JSONObject lineHead = (JSONObject)json.get("lineHead");
		if(lineHead != null && !lineHead.isNullObject()){
			this.createXXBT(lineHead, mv, crsreport.getId());
		}
	}
	
	/**
	 * 创建斜线表头的JS代码
	 * @param lineHead
	 * @param mv
	 */
	private void createXXBT(JSONObject lineHead, MVContext mv, String compId){
		String str = "<script>$(function(){ \n";
		str += "var setHeadFunc = function(){ \n";
		str += "var json = "+lineHead + " \n";
		str += "var obj = $(\"#"+compId+" #xxhead\"); \n";
		//str += "obj.css(\"padding\", \"0px\"); \n";
		str += "json.width = obj.width();json.height = obj.height(); \n";
		str += "obj.load(\"../webreport/CrossHead.action\", {\"json\" : JSON.stringify(json)}); \n";
		str += "} \n";
		str += "setHeadFunc();$(window).resize(setHeadFunc); \n";
		str += "});</script>";
		TextContext text = new TextContextImpl();
		text.setText(str);
		
		text.setParent(mv);
		mv.getChildren().add(text);
	}
	
	/**
	 * 判断用来联动的参数是否存在
	 * @return
	 */
	public boolean isParamExist(String id){
		boolean ret = false;
		List<Element> eles = mv.getChildren();
		for(Element ele : eles){
			if(ele instanceof InputField){
				InputField input = (InputField)ele;
				if(input.getId().equals(id)){
					ret = true;
					break;
				}
			}
		}
		return ret;
	}
}
