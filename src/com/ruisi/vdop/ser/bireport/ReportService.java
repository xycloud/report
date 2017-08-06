package com.ruisi.vdop.ser.bireport;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.ruisi.ext.engine.ExtConstants;
import com.ruisi.ext.engine.init.TemplateManager;
import com.ruisi.ext.engine.util.IdCreater;
import com.ruisi.ext.engine.view.context.Element;
import com.ruisi.ext.engine.view.context.MVContext;
import com.ruisi.ext.engine.view.context.MVContextImpl;
import com.ruisi.ext.engine.view.context.chart.ChartContext;
import com.ruisi.ext.engine.view.context.cross.CrossReportContext;
import com.ruisi.ext.engine.view.context.dc.grid.GridDataCenterContext;
import com.ruisi.ext.engine.view.context.dsource.DataSourceContext;
import com.ruisi.ext.engine.view.context.form.ButtonContext;
import com.ruisi.ext.engine.view.context.form.ButtonContextImpl;
import com.ruisi.ext.engine.view.context.form.CheckBoxContext;
import com.ruisi.ext.engine.view.context.form.DateSelectContext;
import com.ruisi.ext.engine.view.context.form.DateSelectContextImpl;
import com.ruisi.ext.engine.view.context.form.InputField;
import com.ruisi.ext.engine.view.context.form.MultiSelectContext;
import com.ruisi.ext.engine.view.context.form.MultiSelectContextImpl;
import com.ruisi.ext.engine.view.context.form.RadioContext;
import com.ruisi.ext.engine.view.context.form.SelectContext;
import com.ruisi.ext.engine.view.context.form.SelectContextImpl;
import com.ruisi.ext.engine.view.context.form.TextFieldContext;
import com.ruisi.ext.engine.view.context.form.TextFieldContextImpl;
import com.ruisi.ext.engine.view.context.gridreport.GridReportContext;
import com.ruisi.ext.engine.view.context.html.CustomContext;
import com.ruisi.ext.engine.view.context.html.CustomContextImpl;
import com.ruisi.ext.engine.view.context.html.DataContext;
import com.ruisi.ext.engine.view.context.html.DivContext;
import com.ruisi.ext.engine.view.context.html.DivContextImpl;
import com.ruisi.ext.engine.view.context.html.IncludeContext;
import com.ruisi.ext.engine.view.context.html.TextContext;
import com.ruisi.ext.engine.view.context.html.TextContextImpl;
import com.ruisi.ext.engine.view.context.html.table.TableContext;
import com.ruisi.ext.engine.view.context.html.table.TdContext;
import com.ruisi.ext.engine.view.context.html.table.TrContext;
import com.ruisi.vdop.ser.bireport.TableSqlJsonVO.DimInfo;
import com.ruisi.vdop.ser.olap.TableJsonService;
import com.ruisi.vdop.ser.webreport.PageService;
import com.ruisi.vdop.util.VDOPUtils;
import com.ruisi.vdop.util.VdopConstant;

public class ReportService {
	
	public final static String deftMvId = "mv.export.tmp";
	private TableJsonService jsonService = new TableJsonService();
	
	ReportXMLService xmlSer ;
	private String mvid;
	
	public ReportService(){
		xmlSer = new ReportXMLService(this);
	}
	
	public ReportService(String mvid){
		this.mvid = mvid;
		xmlSer = new ReportXMLService(this);
	}
	
	public CustomContext createDataming(MVContext mv , JSONObject obj){
		CustomContext ctx = new CustomContextImpl();
		ctx.setJson(obj.toString());
		ctx.setParent(mv);
		mv.getChildren().add(ctx);
		return ctx;
	}
	
	public TextContext createText(MVContext mv, String txt){
		TextContext text = new TextContextImpl();
		text.setText(txt);
		text.setParent(mv);
		mv.getChildren().add(text);
		return text;
	}
	
	/**
	 * 
	 * @param mv
	 * @param tableJson
	 * @param kpiJson
	 * @param params
	 * @param release  判断当前是否为发布状态, 0 表示不是发布，1表示发布到多维分析，2表示发布到仪表盘
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 */		
	public String getFilePath(ServletContext ctx){
		String path = "";
		path = ctx.getRealPath("/") + VDOPUtils.getConstant(ExtConstants.xmlResource);
		return path;
	}
	
	/**
	 * 报表/仪表盘 mv 生成 xml
	 * @param mv
	 * @return
	 * @throws IOException 
	 */
	public String mv2XML2(MVContext mv) throws IOException{
		StringBuffer sb = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?><ext-config>");
		//生成scripts;
		if(mv.getScripts() != null && mv.getScripts().length() > 0){
			sb.append("<script><![CDATA[ "+mv.getScripts()+" ]]></script>");
		}
		List<Element> children = mv.getChildren();
		for(int i=0; i<children.size(); i++){
			Element comp = children.get(i);
			if(comp instanceof TableContext){
				TableContext table = (TableContext)comp;
				sb.append("<table");
				if(table.getStyleClass() != null && table.getStyleClass().length() > 0){
					sb.append(" class=\""+table.getStyleClass()+"\"");
				}
				sb.append(">");
				List<Element> trs = table.getChildren();
				for(int j=0; j<trs.size(); j++){
					sb.append("<tr>");
					TrContext tr = (TrContext)trs.get(j);
					List<Element> tds = tr.getChildren();
					for(int k=0; k<tds.size(); k++){
						TdContext td = (TdContext)tds.get(k);
						sb.append("<td colspan=\""+td.getColspan()+"\" width=\""+td.getWidth()+"\" rowspan=\""+td.getRowspan()+"\"");
						if(td.getStyleClass() != null && td.getStyleClass().length() > 0){
							sb.append(" styleClass=\""+td.getStyleClass()+"\"");
						}
						sb.append(">");
						
						List<Element> tdcld = td.getChildren();
						for(int l=0; l<tdcld.size(); l++){
							Element tdcldo = tdcld.get(l);
							if(tdcldo instanceof TextContext){
								xmlSer.createText(sb, (TextContext)tdcldo);
							}else if(tdcldo instanceof CrossReportContext){
								xmlSer.createCrossReport(sb, (CrossReportContext)tdcldo);
							}else if(tdcldo instanceof ChartContext){
								xmlSer.createChart(sb, (ChartContext)tdcldo);
							}else if(tdcldo instanceof GridReportContext){
								xmlSer.createGridReport(sb, (GridReportContext)tdcldo);
							}else if(tdcldo instanceof DivContext){
								xmlSer.createDiv(sb, (DivContext)tdcldo);
							}else if(tdcldo instanceof DataContext){
								DataContext data = (DataContext)tdcldo;
								xmlSer.createData(sb, data);
							}else if(tdcldo instanceof InputField){
								this.processParam(sb, tdcldo);
							}
							//对每个组件之间启用换行
							/**
							if(l != tdcld.size() - 1){
								sb.append("<br/>");
							}
							**/
						}
						
						sb.append("</td>");
					}
					sb.append("</tr>");
				}
				sb.append("</table>");
			}else if(comp instanceof DataContext){
				DataContext data = (DataContext)comp;
				xmlSer.createData(sb, data);
			}else if(comp instanceof IncludeContext){
				IncludeContext include = (IncludeContext)comp;
				xmlSer.createInclude(sb, include);
				
			}else if(comp instanceof DivContext){
				DivContext div = (DivContext)comp;
				sb.append("<div styleClass=\""+div.getStyleClass()+"\">");
				if(div.getChildren() != null){
					List ls = div.getChildren();
					for(int j=0; j<ls.size(); j++){
						Element ele = (Element)ls.get(j);
						processParam(sb, ele);
					}
				}
				sb.append("</div>");
			}else if(comp instanceof TextFieldContext){
				TextFieldContext input = (TextFieldContext)comp;
				sb.append("<textField type=\"hidden\" id=\""+input.getId()+"\" desc=\""+(input.getDesc() == null ? "":input.getDesc())+"\"");
				if(input.getDefaultValue() != null){
					sb.append(" defaultValue=\""+input.getDefaultValue()+"\"");
				}
				if(input.isShow()){
					sb.append(" show=\"true\"");
				}
				sb.append("/>");
			}else if(comp instanceof TextContext){
				xmlSer.createText(sb, (TextContext)comp);
			}
		}
		//生成dataCenter
		Map<String, GridDataCenterContext> dcs = mv.getGridDataCenters();
		xmlSer.createDataCenter(sb, dcs);
		
		//生成dataSource
		Map<String, DataSourceContext> dsources = mv.getDsources();
		xmlSer.createDataSource(sb, dsources);
		sb.append("</ext-config>");
		return sb.toString();
	}
	
	public void processParam(StringBuffer sb, Element comp) throws IOException{
		if(comp instanceof TextFieldContext){
			TextFieldContext input = (TextFieldContext)comp;
			sb.append("<textField type=\""+(input.getType() == null ? input.getType() : "")+"\" id=\""+input.getId()+"\" desc=\""+(input.getDesc() == null ? "":input.getDesc())+"\"");
			if(input.getDefaultValue() != null){
				sb.append(" defaultValue=\""+input.getDefaultValue()+"\"");
			}
			if(input.isShow()){
				sb.append(" show=\"true\"");
			}
			sb.append("/>");
		}else if(comp instanceof DateSelectContext){
			DateSelectContext input = (DateSelectContext)comp;
			sb.append("<dateSelect id=\""+input.getId()+"\" desc=\""+(input.getDesc() == null ? "":input.getDesc())+"\" ");
			if(input.getDefaultValue() != null){
				sb.append(" defaultValue=\""+input.getDefaultValue()+"\"");
			}
			if(input.getShowCalendar() != null && input.getShowCalendar()){
				sb.append("	showCalendar=\"true\"");
			}
			if(input.getTarget() != null){
				sb.append("	target=\""+ReportXMLService.array2String(input.getTarget())+"\"");
			}
			sb.append("/>");
		}else if(comp instanceof SelectContext){
			SelectContext input = (SelectContext)comp;
			sb.append("<select id=\""+input.getId()+"\" desc=\""+input.getDesc()+"\" multiple=\""+(input instanceof MultiSelectContext ? "true":"")+"\" ");
			if(input.getDefaultValue() != null){
				sb.append(" defaultValue=\""+input.getDefaultValue()+"\"");
			}
			if(input.getAddEmptyValue() != null){
				sb.append(" addEmptyValue=\""+input.getAddEmptyValue()+"\"");
			}
			sb.append(" refDsource=\""+(input.getRefDsource()==null?"":input.getRefDsource())+"\" >");
			//判断是sql还是直接配置的 option
			if(input.getTemplateName() != null && input.getTemplateName().length() > 0){
				String sql = TemplateManager.getInstance().getTemplate(input.getTemplateName());
				sb.append("<![CDATA[");
				sb.append(sql);
				sb.append("]]>");
			}else{
				List ls = input.loadOptions();
				for(int j=0; j<ls.size(); j++){
					Map<String, String> m = (Map<String, String>)ls.get(j);
					sb.append("<option value=\""+m.get("value")+"\">"+m.get("text")+"</option>");
				}
			}
			sb.append("</select>");
		}else if(comp instanceof CheckBoxContext){	
			CheckBoxContext input = (CheckBoxContext)comp;
			sb.append("<checkBox id=\""+input.getId()+"\" desc=\""+(input.getDesc() == null ? "":input.getDesc())+"\" ");
			if(input.getDefaultValue() != null){
				sb.append(" defaultValue=\""+input.getDefaultValue()+"\"");
			}
			if(input.getShowSpan() != null && input.getShowSpan()){
				sb.append("	showSpan=\"true\"");
			}
			if(input.getCheckboxStyle() != null && input.getCheckboxStyle().length() > 0){
				sb.append("	checkboxStyle=\""+input.getCheckboxStyle()+"\"");
			}
			if(input.getTarget() != null){
				sb.append("	target=\""+ReportXMLService.array2String(input.getTarget())+"\"");
			}
			sb.append(" refDsource=\""+(input.getRefDsource()==null?"":input.getRefDsource())+"\" >");
			//判断是sql还是直接配置的 option
			if(input.getTemplateName() != null && input.getTemplateName().length() > 0){
				String sql = TemplateManager.getInstance().getTemplate(input.getTemplateName());
				sb.append("<![CDATA[");
				sb.append(sql);
				sb.append("]]>");
			}else{
				List ls = input.loadOptions();
				for(int j=0; j<ls.size(); j++){
					Map<String, String> m = (Map<String, String>)ls.get(j);
					sb.append("<option value=\""+m.get("value")+"\">"+m.get("text")+"</option>");
				}
			}
			sb.append("</checkBox>");
		}else if(comp instanceof ButtonContext){
			ButtonContext btn = (ButtonContext)comp;
			sb.append("<button type=\""+btn.getType()+"\" desc=\""+btn.getDesc()+"\" mvId=\""+(this.mvid == null ? deftMvId : this.mvid)+"\"/>");
		}else if(comp instanceof RadioContext){
			RadioContext input = (RadioContext)comp;
			sb.append("<radio id=\""+input.getId()+"\" desc=\""+(input.getDesc() == null ? "":input.getDesc())+"\" ");
			if(input.getDefaultValue() != null){
				sb.append(" defaultValue=\""+input.getDefaultValue()+"\"");
			}
			if(input.getShowSpan() != null && input.getShowSpan()){
				sb.append("	showSpan=\"true\"");
			}
			if(input.getRadioStyle() != null && input.getRadioStyle().length() > 0){
				sb.append(" radioStyle=\""+input.getRadioStyle()+"\"");
			}
			if(input.getTarget() != null){
				sb.append("	target=\""+ReportXMLService.array2String(input.getTarget())+"\"");
			}
			sb.append(" refDsource=\""+(input.getRefDsource()==null?"":input.getRefDsource())+"\" >");
			//判断是sql还是直接配置的 option
			if(input.getTemplateName() != null && input.getTemplateName().length() > 0){
				String sql = TemplateManager.getInstance().getTemplate(input.getTemplateName());
				sb.append("<![CDATA[");
				sb.append(sql);
				sb.append("]]>");
			}else{
				List ls = input.loadOptions();
				for(int j=0; j<ls.size(); j++){
					Map<String, String> m = (Map<String, String>)ls.get(j);
					sb.append("<option value=\""+m.get("value")+"\">"+m.get("text")+"</option>");
				}
			}
			sb.append("</radio>");
		}else if(comp instanceof TextContext){
			TextContext input = (TextContext)comp;
			sb.append("<text><![CDATA["+input.getText()+"]]></text>");
		}
	}
	
	/**
	 * 多维分析mv 生成 xml
	 * @param mv
	 * @return
	 * @throws IOException
	 */
	public String mv2XML(MVContext mv) throws IOException{
		StringBuffer sb = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?><ext-config>");
		//先生成scripts
		if(mv.getScripts() != null){
			sb.append("<script><![CDATA["+mv.getScripts()+"]]></script>");
		}
		List<Element> children = mv.getChildren();
		for(int i=0; i<children.size(); i++){
			Element comp = children.get(i);
			if(comp instanceof TextContext){
				xmlSer.createText(sb, (TextContext)comp);
			}else if(comp instanceof TextFieldContext){
				//处理隐藏参数
				TextFieldContext input = (TextFieldContext)comp;
				sb.append("<textField type=\"hidden\" id=\""+input.getId()+"\"");
				if(input.getDefaultValue() != null){
					sb.append(" defaultValue=\""+input.getDefaultValue()+"\"");
				}
				sb.append("/>");
			}else if(comp instanceof CrossReportContext){
				xmlSer.createCrossReport(sb, (CrossReportContext)comp);
			}else if(comp instanceof ChartContext){
				xmlSer.createChart(sb, (ChartContext)comp);
			}else if(comp instanceof DivContext){
				DivContext div = (DivContext)comp;
				sb.append("<div styleClass=\""+div.getStyleClass()+"\">");
				if(div.getChildren() != null){
					List ls = div.getChildren();
					for(int j=0; j<ls.size(); j++){
						Element ele = (Element)ls.get(j);
						processParam(sb, ele);
					}
				}
				sb.append("</div>");
			}
			//对每个组件之间启用换行
			/**
			if(i != children.size() - 1){
				sb.append("<br/>");
			}
			**/
		}
		
		//生成dataCenter
		Map<String, GridDataCenterContext> dcs = mv.getGridDataCenters();
		xmlSer.createDataCenter(sb, dcs);
		
		//生成dataSource
		Map<String, DataSourceContext> dsources = mv.getDsources();
		xmlSer.createDataSource(sb, dsources);
		sb.append("</ext-config>");
		return sb.toString();
	}
	
	public String createDimSql(JSONObject dim){
		String tname = (String)dim.get("tableName");
		if(tname == null || tname.length() == 0){  //维度未关联码表,直接从数据中查询。
			String sql = "select distinct "+(String)dim.get("colname")+" \"value\", "+(String)dim.get("colname")+" \"text\" from " + dim.get("tname");
			sql += " order by "+dim.get("colname")+" "  + dim.get("dimord");
			return sql;
		}else{
			String sql = "select "+(String)dim.get("tableColKey")+" \"value\", "+(String)dim.get("tableColName")+" \"text\" from " + tname;
			sql += " order by "+dim.get("tableColKey")+" "  + dim.get("dimord");
			return sql;
		}
	}
	
	public String createMonthSql(){
		String sql = "select mid \"value\", mname \"text\" from code_month order by mid desc";
		return sql;
	}
	
	public String htmlPage(String body, String host){
		StringBuffer sb = new StringBuffer();
		
		sb.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
		sb.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
		sb.append("<head>");
		sb.append("<title>多维分析工具</title>");
		sb.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">");
		sb.append("<script type=\"text/javascript\" src=\""+host+"/ext-res/js/jquery.min.js\"></script>");
		sb.append("<script type=\"text/javascript\" src=\""+host+"/ext-res/js/ext-base.js\"></script>");
		sb.append("<script type=\"text/javascript\" src=\""+host+"/ext-res/highcharts/highcharts.js\"></script>");
		sb.append("<script type=\"text/javascript\" src=\""+host+"/ext-res/highcharts/highcharts-more.js\"></script>");
		sb.append("<script type=\"text/javascript\" src=\""+host+"/ext-res/highcharts/modules/map.js\"></script>");
		sb.append("<script type=\"text/javascript\" src=\""+host+"/ext-res/js/sortabletable.js\"></script>");
		sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\""+host+"/ext-res/css/fonts-min.css\" />");
		sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\""+host+"/ext-res/css/boncbase.css\" />");
		sb.append("</head>");
		sb.append("<body class=\"yui-skin-sam\">");
		sb.append(body);
		sb.append("</body>");
		sb.append("</html>");
		
		return sb.toString();
	}

	public ReportXMLService getXmlSer() {
		return xmlSer;
	}
}
