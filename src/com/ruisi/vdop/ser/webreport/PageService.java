package com.ruisi.vdop.ser.webreport;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.ruisi.ext.engine.ExtConstants;
import com.ruisi.ext.engine.init.TemplateManager;
import com.ruisi.ext.engine.util.IdCreater;
import com.ruisi.ext.engine.view.context.Element;
import com.ruisi.ext.engine.view.context.ExtContext;
import com.ruisi.ext.engine.view.context.MVContext;
import com.ruisi.ext.engine.view.context.MVContextImpl;
import com.ruisi.ext.engine.view.context.cross.CrossReportContext;
import com.ruisi.ext.engine.view.context.dsource.DataSourceContext;
import com.ruisi.ext.engine.view.context.face.OptionsLoader;
import com.ruisi.ext.engine.view.context.face.Template;
import com.ruisi.ext.engine.view.context.form.ButtonContext;
import com.ruisi.ext.engine.view.context.form.ButtonContextImpl;
import com.ruisi.ext.engine.view.context.form.CheckBoxContextImpl;
import com.ruisi.ext.engine.view.context.form.DateSelectContextImpl;
import com.ruisi.ext.engine.view.context.form.InputField;
import com.ruisi.ext.engine.view.context.form.RadioContextImpl;
import com.ruisi.ext.engine.view.context.form.SelectContextImpl;
import com.ruisi.ext.engine.view.context.form.TextFieldContext;
import com.ruisi.ext.engine.view.context.form.TextFieldContextImpl;
import com.ruisi.ext.engine.view.context.form.TreeContextImpl;
import com.ruisi.ext.engine.view.context.html.BRContext;
import com.ruisi.ext.engine.view.context.html.BRContextImpl;
import com.ruisi.ext.engine.view.context.html.DataContext;
import com.ruisi.ext.engine.view.context.html.DivContext;
import com.ruisi.ext.engine.view.context.html.DivContextImpl;
import com.ruisi.ext.engine.view.context.html.IncludeContext;
import com.ruisi.ext.engine.view.context.html.IncludeContextImpl;
import com.ruisi.ext.engine.view.context.html.TextContext;
import com.ruisi.ext.engine.view.context.html.TextContextImpl;
import com.ruisi.ext.engine.view.context.html.table.TableContext;
import com.ruisi.ext.engine.view.context.html.table.TableContextImpl;
import com.ruisi.ext.engine.view.context.html.table.TdContext;
import com.ruisi.ext.engine.view.context.html.table.TdContextImpl;
import com.ruisi.ext.engine.view.context.html.table.TrContext;
import com.ruisi.ext.engine.view.context.html.table.TrContextImpl;
import com.ruisi.ext.engine.view.exception.ExtConfigException;
import com.ruisi.vdop.ser.bireport.TableSqlJsonVO;
import com.ruisi.vdop.ser.olap.TableJsonService;

public class PageService {
	
	public final static String deftMvId = "mv.report.tmp";
	private String mvid; //如果存在使用它，如果不存在，使用 deftMvid
	private Map<String, InputField> mvParams = new HashMap(); //mv的参数
	private StringBuffer css = new StringBuffer(); //在创建页面过程中生成所需要的组件样式文件
	private StringBuffer scripts = new StringBuffer();
	
	private JSONObject tableJson;  //pageInfo, 对应页面JSON
	private DataService dataService = new DataService();
	
	private static Map<String, String> reportObjs; //报表对象样式和css的对应对象。
	
	public PageService(JSONObject tableJson){
		this.tableJson = tableJson;
	}
	
	public PageService(JSONObject tableJson, String mvid){
		this.tableJson = tableJson;
		this.mvid = mvid;
	}
	
	public MVContext json2MV() throws Exception{
		return json2MV(false, true, null, false);
	}
	
	public MVContext json2MV(boolean isexport, boolean insertParam, String exportType, boolean push3G) throws Exception{
		//创建MV
		MVContext mv = new MVContextImpl();
		mv.setChildren(new ArrayList());
		String formId = ExtConstants.formIdPrefix + IdCreater.create();
		mv.setFormId(formId);
		mv.setMvid(mvid == null ? deftMvId : mvid);
		
		//获取输出类型
		String outType = null;
		JSONObject style = (JSONObject)this.tableJson.get("style");
		if(style != null){
			outType = (String)style.get("type"); 
		}
		CompService compSer = new CompService(mv, this, isexport, outType);
		//解析变量
		parserVariable((JSONArray)tableJson.get("vars"), mv, compSer);
		
		//解析页面样式 userstyle
		this.dealuserStyle(mv);
		
		//解析参数
		Object param = tableJson.get("param");
		if(param != null && ((JSONArray)param).size()>0){
			if(isexport){
				this.parserHiddenParam((JSONArray)param, mv, insertParam);
			}else{
				this.parserParam((JSONArray)param, mv, insertParam);
			}
		}else{
			//生成form，让分页不报错。
			mv.setShowForm(true);
		}
		
		//创建datasource
		if(tableJson.get("datasource") != null){
			JSONArray dsources = tableJson.getJSONArray("datasource");
			for(int i=0; i<dsources.size(); i++){
				DataSourceContext ds = new DataSourceContext();
				JSONObject dsource = dsources.getJSONObject(i);
				String use = (String)dsource.get("use");
				if(use == null || use.equals("jdbc")){
					ds.putProperty("linktype", dsource.getString("linktype"));
					ds.putProperty("linkname", dsource.getString("linkname"));
					ds.putProperty("linkpwd", dsource.getString("linkpwd"));
					ds.putProperty("linkurl", dsource.getString("linkurl"));
				}else{
					ds.putProperty("jndiname", dsource.getString("jndiname"));
				}
				ds.putProperty("id", dsource.getString("dsid"));
				ds.putProperty("use", use);
				
				if(mv.getDsources() == null){
					mv.setDsources(new HashMap<String, DataSourceContext>());
				}
				mv.getDsources().put(ds.getId(), ds);
			}
		}
		JSONObject body = tableJson.getJSONObject("body");

		//解析布局器样式
		JSONObject pageStyle = (JSONObject)tableJson.get("style");
		if(this.isParserCss(isexport, exportType) && pageStyle != null && !pageStyle.isNullObject() && !pageStyle.isEmpty()){
			TextContext text = new TextContextImpl();
			StringBuffer str = new StringBuffer("<style>");
			str.append(".mv_main2{");
			String width =(String)pageStyle.get("width");
			String height = (String)pageStyle.get("height");
			String type = (String)pageStyle.get("type");  //type表示页面类型
			if(push3G){
				//如果是推送到3G页面，在设计和预览时设计的宽度不起作用
			}else{
				if("PHONE".equals(type)){
					str.append("width:400px;margin:auto;");
				}else if("PAD".equals(type)){
					str.append("width:800px;margin:auto;");
				}else{
					if(width != null && width.length() > 0){
						str.append("width:"+width+"px;margin:auto;");
					}	
				}
			}
			if(height != null && height.length() > 0){
				str.append("height:"+height+"px;");
			}
			str.append("}");
			//布局器样式
			str.append("table.mylayout { border-collapse:collapse;table-layout:fixed; }");
			str.append("table.mylayout td.layouttd {");
			dealCompBorder(pageStyle, str);
			str.append("}");
			str.append("</style>");
			text.setText(str.toString());
			mv.getChildren().add(text);
			text.setParent(mv);
		}
		parserBody(body, mv, compSer, insertParam);
		String cssstr = css.toString();
		if(this.isParserCss(isexport, exportType) && cssstr.length() > 0){
			TextContext text = new TextContextImpl();
			text.setText( "<style>" + cssstr +"</style>");
			mv.getChildren().add(text);
			text.setParent(mv);
		}
		//创建scripts， 在服务器后台执行的代码
		String script = scripts.toString();
		if(script != null && script.length() > 0){
			mv.setScripts(script);
		}
		
		//创建 脚本中的 script 代码，在页面浏览器中执行的而不是在服务器后台执行的
		Object scripts = this.tableJson.get("scripts");
		if(scripts != null){
			this.createScripts(mv, (JSONArray)scripts);
		}
		
		return mv;
	}
	
	/**
	 * 创建 脚本中的 script 代码，在页面浏览器中执行的而不是在服务器后台执行的
	 */
	private void createScripts(MVContext mv, JSONArray scripts){
		TextContext text = new TextContextImpl();
		StringBuffer str = new StringBuffer("<script>\n");
		str.append("$(function(){\n");
		for(int i=0; i<scripts.size(); i++){
			JSONObject s = scripts.getJSONObject(i);
			str.append(s.getString("code"));
			str.append("\n");
		}
		str.append("\n});");
		str.append("</script>");
		text.setText(str.toString());
		mv.getChildren().add(text);
		text.setParent(mv);
	}
	
	private boolean isParserCss(boolean isexport, String exportType){
		//不是导出，需要解析CSS
		if(isexport == false){
			return true;
		}
		//如果是html导出，需要解析css，其他不用
		if(isexport && "html".equals(exportType)){
			return true;
		}else{
			return false;
		}
	}
	
	public JSONObject findDataSourceById(String id){
		JSONObject ret = null;
		JSONArray dsets = this.tableJson.getJSONArray("datasource");
		for(int i=0; i<dsets.size(); i++){
			JSONObject dset = dsets.getJSONObject(i);
			if(dset.getString("dsid").equals(id)){
				ret = dset;
				break;
			}
		}
		return ret;
	}
	
	public JSONObject findDataSetById(String id){
		JSONObject ret = null;
		JSONArray dsets = this.tableJson.getJSONArray("dataset");
		for(int i=0; i<dsets.size(); i++){
			JSONObject dset = dsets.getJSONObject(i);
			if(dset.getString("datasetid").equals(id)){
				ret = dset;
				break;
			}
		}
		return ret;
	}
	
	public JSONObject findCubeById(String id){
		JSONObject ret = null;
		JSONArray dsets = this.tableJson.getJSONArray("cube");
		for(int i=0; i<dsets.size(); i++){
			JSONObject dset = dsets.getJSONObject(i);
			if(dset.getString("id").equals(id)){
				ret = dset;
				break;
			}
		}
		return ret;
	} 
	public static JSONObject findVarById(JSONArray vars, String id){
		JSONObject ret = null;
		for(int i=0; vars!=null && i<vars.size(); i++){
			JSONObject var = vars.getJSONObject(i);
			String tid = var.getString("id");
			if(id.equals(tid)){
				ret = var;
				break;
			}
		}
		return ret;
	}
	//在导出的情况下，把参数都隐藏
	public void parserHiddenParam(JSONArray params, MVContext mv, boolean insertParam) throws ExtConfigException{
		for(int i=0; i<params.size(); i++){
			JSONObject param = params.getJSONObject(i);
			String id = param.getString("id");
			String desc = param.getString("desc");
			String tp = param.getString("type");
			String val = (String)param.get("defvalue");
			TextFieldContext input =  new TextFieldContextImpl();
			input.setType("hidden");
			input.setShow(false);
			input.setDesc(desc);
			input.setId(id);
			input.setDefaultValue(val);
			
			mv.getChildren().add(input);
			input.setParent(mv);
			
			//添加参数,在预览时需要，在发布是不需要
			if(insertParam){
				this.mvParams.put(id, input);
				ExtContext.getInstance().putServiceParam(mv.getMvid(), id, input);
				mv.setShowForm(true);
			}
		}
	}
	
	private void paramOptions(JSONObject param, OptionsLoader option){
		List ls = option.loadOptions();
		if(ls == null){
			ls = new ArrayList();
			option.setOptions(ls);
		}
		Object vals = param.get("values");
		if(vals != null){
			JSONArray values = (JSONArray)vals;
			for(int i=0; i<values.size(); i++){
				JSONObject opt = values.getJSONObject(i);
				Map<String, String> nOption = new HashMap<String, String>();
				nOption.put("text", opt.getString("text"));
				nOption.put("value", opt.getString("value"));
				ls.add(nOption);
			}
		}
	}
	
	/**
	 * 返回dataSourceId
	 * @param param
	 * @param template
	 * @return
	 * @throws IOException
	 */
	private String paramSql(JSONObject param, Template template, String casca) throws IOException{
		JSONObject option = param.getJSONObject("option");
		String datasetid = option.getString("datasetid");
		JSONObject dataSet = this.findDataSetById(datasetid);
		String sql = this.dataService.createDatasetSql(dataSet);
		//直接用SQL封装成 value, text
		sql = "select "+option.getString("value")+" \"value\", "+option.getString("text")+" \"text\" from (" + sql + ") cc";
		//处理级联
		if(casca != null && casca.length() > 0){
			sql += " where "+param.getString("acceptColumn")+" = $" + casca;
		}
		String name = TemplateManager.getInstance().createTemplate(sql);
		template.setTemplateName(name);
		return dataSet.getString("dsid");
	}
	
	/**
	 * 生成tree参数的 SQL
	 * @param param
	 * @param template
	 * @return
	 * @throws IOException
	 */
	private String paramTreeSql(JSONObject param, Template template) throws IOException{
		JSONObject option = param.getJSONObject("option");
		String datasetid = option.getString("datasetid");
		JSONObject dataSet = this.findDataSetById(datasetid);
		String sql = this.dataService.createDatasetSql(dataSet);
		//直接用SQL封装成 value, text
		sql = "select "+option.getString("value")+" \"id\", "+option.getString("text")+" \"text\", "+option.getString("pid")+" \"pid\" from (" + sql + ") cc";
		String name = TemplateManager.getInstance().createTemplate(sql);
		template.setTemplateName(name);
		return dataSet.getString("dsid");
	}
	
	public void parserVariable(JSONArray vars, MVContext mv, CompService compSer) throws IOException{
		for(int i=0; vars!=null&&i<vars.size(); i++){
			JSONObject obj = vars.getJSONObject(i);
			String id = obj.getString("id");
			String val = (String)obj.get("value");
			String valType = obj.getString("valtype");
			String datasetid = (String)obj.get("datasetid");
			String col = (String)obj.get("col");
			if("jtz".equals(valType)){
				this.scripts.append("\n extContext.put(\""+id+"\",\""+val+"\");\n");
			}else{
				//创建 data 标签
				DataContext data = compSer.crtDataSet("D"+IdCreater.create(), datasetid);
				//设置 输出 的col
				data.setOutKey(new String[]{id});
				data.setOutVal(new String[]{col});
				mv.getChildren().add(data);
				data.setParent(mv);
			}
		}
	}
	
	public void parserParam(JSONArray params, MVContext mv, boolean insertParam) throws ExtConfigException, IOException{
		DivContext div = new DivContextImpl();
		div.setStyleClass("rpeortParam");
		div.setChildren(new ArrayList<Element>());
		mv.getChildren().add(div);
		div.setParent(mv);
		for(int i=0; i<params.size(); i++){
			JSONObject param = params.getJSONObject(i);
			String id = param.getString("id");
			String desc = param.getString("desc");
			String tp = param.getString("type");
			String val = (String)param.get("defvalue");
			String vtp = (String)param.get("valtype");
			String defvalType = (String)param.get("defvalType");
			String defvalRef = (String)param.get("defvalRef");
			InputField input = null;
			if("textField".equals(tp)){
				input = new TextFieldContextImpl();
			}else if("select".equals(tp)){
				SelectContextImpl target = new SelectContextImpl();
				if("static".equals(vtp)){
					this.paramOptions(param, target);
				}else if("dynamic".equals(vtp)){
					String dsid = this.paramSql(param, target, null);
					target.setRefDsource(dsid);
				}
				input = target;
			}else if("dateSelect".equals(tp)){
				input = new DateSelectContextImpl();
			}else if("radio".equals(tp)){
				RadioContextImpl rd = new RadioContextImpl();
				if("static".equals(vtp)){
					this.paramOptions(param, rd);
				}else if("dynamic".equals(vtp)){
					String dsid = this.paramSql(param, rd, null);
					rd.setRefDsource(dsid);
				}
				input = rd;
			}else if("checkbox".equals(tp)){
				CheckBoxContextImpl ck = new CheckBoxContextImpl();
				if("static".equals(vtp)){
					this.paramOptions(param, ck);
				}else if("dynamic".equals(vtp)){
					String dsid = this.paramSql(param, ck, null);
					ck.setRefDsource(dsid);
				}
				input = ck;
			}else if("tree".equals(tp)){
				TreeContextImpl tree = new TreeContextImpl();
				String dsid = this.paramTreeSql(param, tree);
				tree.setRefDsource(dsid);
				JSONObject option = param.getJSONObject("option");
				tree.setDefRootId(option.getString("defrootid"));
				tree.setValueId("id");
				tree.setValuePid("pid");
				tree.setValueText("text");
				input = tree;
			}else if("casca".equals(tp)){  //级联参数特殊处理
				String first = null; //后面一个参数自动级联前面一个参数
				JSONArray children = param.getJSONArray("children");
				for(int j=0; j<children.size(); j++){
					JSONObject obj = children.getJSONObject(j);
					String cid = obj.getString("id");
					String cdesc = obj.getString("desc");
					String cval = (String)obj.get("defvalue");
					SelectContextImpl sc = new SelectContextImpl();
					String dsid = this.paramSql(obj, sc, first);
					sc.setId(cid);
					sc.setDesc(cdesc);
					sc.setDefaultValue(cval);
					sc.setRefDsource(dsid);
					//设置级联上级
					if(j>0){
						sc.setCascade(first);
					}
					div.getChildren().add(sc);
					sc.setParent(div);
					//添加参数,在预览时需要，在发布是不需要
					if(insertParam){
						this.mvParams.put(cid, sc);	
						ExtContext.getInstance().putServiceParam(mv.getMvid(), cid, sc);
						mv.setShowForm(true);
					}
					first = cid;
				}
			}
			if(input == null){
				continue;  //对于级联参数，INPUT 为 null
			}
			input.setDesc(desc);
			input.setId(id);
			if("dtz".equals(defvalType)){
				input.setDefaultValue("${"+defvalRef+"}");
			}else{
				input.setDefaultValue(val);
			}
			div.getChildren().add(input);
			input.setParent(div);
			
			//添加参数,在预览时需要，在发布是不需要
			if(insertParam){
				this.mvParams.put(id, input);	
				ExtContext.getInstance().putServiceParam(mv.getMvid(), id, input);
				mv.setShowForm(true);
			}
		}
		if(!params.isEmpty()){
			ButtonContext btn = new ButtonContextImpl();
			btn.setDesc("查询");
			btn.setType("button");
			btn.setMvId(new String[]{PageService.deftMvId});
			div.getChildren().add(btn);
			btn.setParent(div);
		}
	}
	
	//解析布局器
	public void parserBody(JSONObject body, MVContext mv, CompService compSer,  boolean insertParam) throws IOException, ParseException, ExtConfigException{
		TableContext tab = new TableContextImpl();
		tab.setStyleClass("mylayout");
		tab.setChildren(new ArrayList());
		mv.getChildren().add(tab);
		tab.setParent(mv);
		for(int i=1; true; i++){
			Object tmp = body.get("tr" + i);
			if(tmp == null){
				break;
			}
			JSONArray trs = (JSONArray)tmp;
			TrContext tabTr = new TrContextImpl();
			tabTr.setChildren(new ArrayList());
			tab.getChildren().add(tabTr);
			tabTr.setParent(tab);
			for(int j=0; j<trs.size(); j++){
				JSONObject td = trs.getJSONObject(j);
				TdContext tabTd = new TdContextImpl();
				tabTd.setStyleClass("layouttd");
				tabTd.setChildren(new ArrayList());
				tabTd.setParent(tabTr);
				tabTr.getChildren().add(tabTd);
				tabTd.setColspan(String.valueOf(td.getInt("colspan")));
				tabTd.setRowspan(String.valueOf(td.getInt("rowspan")));
				tabTd.setWidth(td.getInt("width") + "%");
				
				Object cldTmp = td.get("children");
				
				if(cldTmp != null){
					JSONArray children = (JSONArray)cldTmp;
					for(int k=0; k<children.size(); k++){
						JSONObject comp = children.getJSONObject(k);
						String tp = comp.getString("type");
						if(tp.equals("label") || tp.equals("text")){
							compSer.createLabel(tp, tabTd, comp);
						}else if(tp.equals("chart")){
							compSer.createChart(comp, tabTd, insertParam);
						}else if(tp.equals("cross")){
							compSer.crtCrossReport(comp, tabTd, insertParam);
						}else if(tp.equals("table")){
							compSer.crtTable(comp, tabTd, insertParam);
						}else if(tp.equals("pic")){
							compSer.createPic(tabTd, comp);
						}else if(tp.equals("staticCross")){
							compSer.crtStaticCross(comp, tabTd, insertParam);
						}
					}
				}
			}
		}
	}
	
	public String htmlPage(String body, String host){
		StringBuffer sb = new StringBuffer();
		
		sb.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
		sb.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
		sb.append("<head>");
		sb.append("<title>WEB报表工具</title>");
		sb.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">");
		sb.append("<script type=\"text/javascript\" src=\""+host+"/ext-res/js/jquery.min.js\"></script>");
		sb.append("<script type=\"text/javascript\" src=\""+host+"/ext-res/js/ext-base.js\"></script>");
		sb.append("<script type=\"text/javascript\" src=\""+host+"/ext-res/highcharts/highcharts.js\"></script>");
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

	public Map<String, InputField> getMvParams() {
		return mvParams;
	}

	public void setMvParams(Map<String, InputField> mvParams) {
		this.mvParams = mvParams;
	}
	
	public static boolean dealCompBorder(JSONObject pageStyle, StringBuffer str){
		boolean use = false;
		if(pageStyle == null || pageStyle.isNullObject() || pageStyle.isEmpty()){
			return use;
		}
		String bordercolor = (String)pageStyle.get("bordercolor");
		String bordersize = (String)pageStyle.get("bordersize");
		String bordertype = (String)pageStyle.get("bordertype");
		String padding = (String)pageStyle.get("padding");
		String margin = (String)pageStyle.get("margin");
		String bgcolor = (String)pageStyle.get("bgcolor");
		if(bordercolor != null && bordercolor.length() > 0){
			str.append("border-color:" + bordercolor+";");
			use = true;
		}
		if(bordersize != null && bordersize.length() > 0){
			str.append("border-width:" + bordersize+"px;");
			use = true;
		}
		if(bordertype != null && bordertype.length() > 0){
			str.append("border-style:" + bordertype+";");
			use = true;
		}
		if(padding != null && padding.length() > 0){
			String[] p = padding.split(",");
			if(p.length == 1){
				str.append("padding:" + p[0]+"px;");
			}else{
				str.append("padding:" + (p.length>0&&p[0].length()>0?p[0]:"0")+"px "+(p.length>1&&p[1].length()>0?p[1]:"0")+"px "+(p.length>2&&p[2].length()>0?p[2]:"0")+"px "+(p.length>3&&p[3].length()>0?p[3]:"0")+"px;");
			}
			use = true;
		}
		if(margin != null && margin.length() > 0){
			String[] p = margin.split(",");
			if(p.length == 1){
				str.append("margin:" + p[0]+"px;");
			}else{
				str.append("margin:" + (p.length>0&&p[0].length()>0?p[0]:"0")+"px "+(p.length>1&&p[1].length()>0?p[1]:"0")+"px "+(p.length>2&&p[2].length()>0?p[2]:"0")+"px "+(p.length>3&&p[3].length()>0?p[3]:"0")+"px;");
			}
			use = true;
		}
		if(bgcolor != null && bgcolor.length() > 0){
			str.append("background-color:" + bgcolor+";");
			use = true;
		}
		return use;
	}
	
	public static String reportObj2Style(String input){
		if(reportObjs == null){
			Map<String, String> m = new HashMap<String, String>();
			m.put("body", "body");
			m.put("params", "div.rpeortParam");
			m.put("layout", "table.mylayout");
			m.put("layout-td", "table.mylayout td.layouttd");
			m.put("table", "table.grid3");
			m.put("table-head", "table.grid3 th.grid3-td, TABLE.lockgrid TH.grid3-td");
			m.put("table-detail", "table.grid3 td.grid3-td, TABLE.lockgrid td.lockgrid-td");
			m.put("table-foot", "table.grid3 td.grid3-foot, TABLE.lockgrid td.grid3-foot");
			m.put("table-tr-row1", "table.grid3 tr.tr-row1,TABLE.lockgrid tr.tr-row1");
			m.put("table-tr-row2", "table.grid3 tr.tr-row2,TABLE.lockgrid tr.tr-row2");
			m.put("mainPage", "div.mv_main");
			reportObjs = m;
		}
		return reportObjs.get(input);
	}
	
	/**
	 * 转换JSON样式定义到样式文件
	 * @param styles
	 * @return
	 */
	public static String style2file(JSONArray styles){
		StringBuffer sb = new StringBuffer("<style><!-- \n");
		for(int i=0; styles != null && i<styles.size(); i++){
			JSONObject style = styles.getJSONObject(i);
			String styletype = style.getString("styletype");  
			if("reportele".equals(styletype)){  //报表对象样式
				String obj = (String)style.get("styleobj");
				sb.append(reportObj2Style(obj));
			}else{
				sb.append("."+style.get("stylename"));
			}
			sb.append("{");
			String family = (String)style.get("family");
			if(family != null && family.length() > 0){
				sb.append("font-family:'"+family+"';");
			}
			String color = (String)style.get("color");
			if(color != null && color.length() > 0){
				sb.append("color:"+color+";");
			}
			String size = (String)style.get("size");
			if(size != null && size.length() > 0){
				sb.append("font-size:"+size+"px;");
			}
			String weight = (String)style.get("weight");
			if(weight != null && weight.length() > 0){
				sb.append("font-weight:"+weight+";");
			}
			String fstyle = (String)style.get("style");
			if(fstyle != null && fstyle.length() > 0){
				sb.append("font-style:"+fstyle+";");
			}
			String decoration = (String)style.get("decoration");
			if(decoration != null && decoration.length() > 0){
				sb.append("text-decoration:" + decoration+";");
			}
			String bgcolor = (String)style.get("bgcolor");
			if(bgcolor != null && bgcolor.length() > 0){
				sb.append("background-color:"+bgcolor+";");
			}
			String bgimg = (String)style.get("bgimg");
			if(bgimg != null && bgimg.length() > 0){
				sb.append("background-image: url(../pic/resource/"+bgimg+");");
			}
			String repeat = (String)style.get("repeat");
			if(repeat != null && repeat.length() > 0){
				sb.append("background-repeat:"+repeat+";");
			}
			String attachment = (String)style.get("attachment");
			if(attachment != null && attachment.length() > 0){
				sb.append("background-attachment:"+attachment+";");
			}
			String positionx = (String)style.get("positionx");
			String positiony = (String)style.get("positiony");
			if(positionx != null && positionx.length() > 0 && positiony != null && positiony.length() > 0){
				sb.append("background-position:" + positionx + " " + positiony + ";");
			}
			String lineheight = (String)style.get("lineheight");
			if(lineheight != null && lineheight.length() > 0){
				sb.append("line-height:"+lineheight+"px;");
			}
			String letterspacing = (String)style.get("letterspacing");
			if(letterspacing != null && letterspacing.length() > 0){
				sb.append("letter-spacing:"+letterspacing+"px;");
			}
			String wordspacing = (String)style.get("wordspacing");
			if(wordspacing != null && wordspacing.length() > 0){
				sb.append("word-spacing:"+wordspacing+"px;");
			}
			String verticalalign = (String)style.get("verticalalign");
			if(verticalalign != null && verticalalign.length() > 0){
				sb.append("vertical-align:"+verticalalign+";");
			}
			String textalign = (String)style.get("textalign");
			if(textalign != null && textalign.length() > 0){
				sb.append("text-align:"+textalign+";");
			}
			String textindent = (String)style.get("textindent");
			if(textindent != null && textindent.length() > 0){
				sb.append("text-indent:"+textindent+"px;");
			}
			String display = (String)style.get("display");
			if(display != null && display.length() > 0){
				sb.append("display:"+display+";");
			}
			String width = (String)style.get("width");
			if(width != null && width.length() > 0){
				sb.append("width:"+width+"px;");
			}
			String height = (String)style.get("height");
			if(height != null && height.length() > 0){
				sb.append("height:"+height+"px;");
			}
			String paddingtop = (String)style.get("paddingtop");
			if(paddingtop != null && paddingtop.length() > 0){
				if("auto".equals(paddingtop)){
					sb.append("padding-top:auto;");
				}else{
					sb.append("padding-top:"+paddingtop+"px;");
				}
			}
			String paddingright = (String)style.get("paddingright");
			if(paddingright != null && paddingright.length() > 0){
				if("auto".equals(paddingright)){
					sb.append("padding-right:auto;");
				}else{
					sb.append("padding-right:"+paddingright+"px;");
				}
			}
			String paddingbottom = (String)style.get("paddingbottom");
			if(paddingbottom != null && paddingbottom.length() > 0){
				if("auto".equals(paddingbottom)){
					sb.append("padding-bottom:auto;");
				}else{
					sb.append("padding-bottom:"+paddingbottom+"px;");
				}
			}
			String paddingleft = (String)style.get("paddingleft");
			if(paddingleft != null && paddingleft.length() > 0){
				if("auto".equals(paddingleft)){
					sb.append("padding-left:auto;");
				}else{
					sb.append("padding-left:"+paddingleft+"px;");
				}
			}
			String margintop = (String)style.get("margintop");
			if(margintop != null && margintop.length() > 0){
				if("auto".equals(margintop)){
					sb.append("margin-top:auto;");
				}else{
					sb.append("margin-top:"+margintop+"px;");
				}
			}
			String marginright = (String)style.get("marginright");
			if(marginright != null && marginright.length() > 0){
				if("auto".equals(marginright)){
					sb.append("margin-right:auto;");
				}else{
					sb.append("margin-right:"+marginright+"px;");
				}
			}
			String marginbottom = (String)style.get("marginbottom");
			if(marginbottom != null && marginbottom.length() > 0){
				if("auto".equals(marginbottom)){
					sb.append("margin-bottom:auto;");
				}else{
					sb.append("margin-bottom:"+marginbottom+"px;");
				}
			}
			String marginleft = (String)style.get("marginleft");
			if(marginleft != null && marginleft.length() > 0){
				if("auto".equals(marginleft)){
					sb.append("margin-left:auto;");
				}else{
					sb.append("margin-left:"+marginleft+"px;");
				}
			}
			String bordertop = (String)style.get("bordertop");
			if(bordertop != null && bordertop.length() > 0){
				sb.append("border-top-style:"+bordertop+";");
			}
			String borderright = (String)style.get("borderright");
			if(borderright != null && borderright.length() > 0){
				sb.append("border-right-style:"+borderright+";");
			}
			String borderbottom = (String)style.get("borderbottom");
			if(borderbottom != null && borderbottom.length() > 0){
				sb.append("border-bottom-style:"+borderbottom+";");
			}
			String borderleft = (String)style.get("borderleft");
			if(borderleft != null && borderleft.length() > 0){
				sb.append("border-left-style:"+borderleft+";");
			}
			String widthtop = (String)style.get("widthtop");
			if(widthtop != null && widthtop.length() > 0){
				sb.append("border-top-width:"+widthtop+"px;");
			}
			String widthright = (String)style.get("widthright");
			if(widthright != null && widthright.length() > 0){
				sb.append("border-right-width:"+widthright+"px;");
			}
			String widthbottom = (String)style.get("widthbottom");
			if(widthbottom != null && widthbottom.length() > 0){
				sb.append("border-bottom-width:"+widthbottom+"px;");
			}
			String widthleft = (String)style.get("widthleft");
			if(widthleft != null && widthleft.length() > 0){
				sb.append("border-left-width:"+widthleft+"px;");
			}
			String colortop = (String)style.get("colortop");
			if(colortop != null && colortop.length() > 0){
				sb.append("border-top-color:"+colortop+";");
			}
			String colorright = (String)style.get("colorright");
			if(colorright != null && colorright.length() > 0){
				sb.append("border-right-color:"+colortop+";");
			}
			String colorbottom = (String)style.get("colorbottom");
			if(colorbottom != null && colorbottom.length() > 0){
				sb.append("border-bottom-color:"+colorbottom+";");
			}
			String colorleft = (String)style.get("colorleft");
			if(colorleft != null && colorleft.length() > 0){
				sb.append("border-left-color:"+colorleft+";");
			}
			sb.append("} \n");
		}
		sb.append("\n--></style>");
		return sb.toString();
	}
	
	public void dealuserStyle(MVContext mv){
		JSONArray styles = (JSONArray)this.tableJson.get("userstyle");
		String sb = style2file(styles);
		TextContext ct = new TextContextImpl();
		ct.setText(sb.toString());
		mv.getChildren().add(ct);
		ct.setParent(mv);
		
		//处理引用的样式
		JSONArray files = (JSONArray)this.tableJson.get("cssfiles");
		for(int i=0; files!=null&&i<files.size(); i++){
			JSONObject file =(JSONObject)files.get(i);
			String name = file.getString("name");
			IncludeContext ctx = new IncludeContextImpl();
			ctx.setPage("/pic/css/" + name);
			
			mv.getChildren().add(ctx);
			ctx.setParent(mv);
		}
	}

	public StringBuffer getCss() {
		return css;
	}

	public StringBuffer getScripts() {
		return scripts;
	}

	public void setScripts(StringBuffer scripts) {
		this.scripts = scripts;
	}
}
