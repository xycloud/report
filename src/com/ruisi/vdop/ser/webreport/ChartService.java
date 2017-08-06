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
import com.ruisi.ext.engine.dao.DaoHelper;
import com.ruisi.ext.engine.init.TemplateManager;
import com.ruisi.ext.engine.util.IdCreater;
import com.ruisi.ext.engine.view.context.ExtContext;
import com.ruisi.ext.engine.view.context.MVContext;
import com.ruisi.ext.engine.view.context.MVContextImpl;
import com.ruisi.ext.engine.view.context.chart.ChartContext;
import com.ruisi.ext.engine.view.context.chart.ChartContextImpl;
import com.ruisi.ext.engine.view.context.chart.ChartKeyContext;
import com.ruisi.ext.engine.view.context.chart.ChartLinkContext;
import com.ruisi.ext.engine.view.context.chart.ChartTitleContext;
import com.ruisi.ext.engine.view.context.cross.CrossReportContext;
import com.ruisi.ext.engine.view.context.dc.grid.AggreVO;
import com.ruisi.ext.engine.view.context.dc.grid.GridAggregationContext;
import com.ruisi.ext.engine.view.context.dc.grid.GridDataCenterContext;
import com.ruisi.ext.engine.view.context.dc.grid.GridDataCenterContextImpl;
import com.ruisi.ext.engine.view.context.dc.grid.GridFilterContext;
import com.ruisi.ext.engine.view.context.dc.grid.GridSetConfContext;
import com.ruisi.ext.engine.view.context.dc.grid.GridSortContext;
import com.ruisi.ext.engine.view.context.dsource.DataSourceContext;
import com.ruisi.ext.engine.view.context.form.InputField;
import com.ruisi.ext.engine.view.context.form.TextFieldContext;
import com.ruisi.ext.engine.view.context.form.TextFieldContextImpl;
import com.ruisi.ext.engine.view.context.html.TextContext;
import com.ruisi.ext.engine.view.context.html.TextContextImpl;
import com.ruisi.ext.engine.view.context.html.TextProperty;
import com.ruisi.ext.engine.view.exception.ExtConfigException;
import com.ruisi.ispire.dc.grid.GridFilter;
import com.ruisi.vdop.ser.bireport.TableSqlJsonVO;
import com.ruisi.vdop.ser.bireport.TableSqlJsonVO.DimInfo;
import com.ruisi.vdop.ser.bireport.TableSqlJsonVO.KpiInfo;
import com.ruisi.vdop.ser.olap.TableJsonService;
import com.ruisi.vdop.util.VDOPUtils;

public class ChartService {
	
	private Map<String, InputField> mvParams = new HashMap(); //mv的参数
	
	private DataService dataService = new DataService();
	
	private String defMvid;
	
	private String outType; //输出类型， PC/PHONE/PAD
	
	private boolean isexport = false; //是否导出
	
	public ChartService(String defMvId, String outType){
		this.defMvid = defMvId;
		this.outType = outType;
	}
	
	public ChartService(String outType, boolean isexport){
		this.outType = outType;
		this.isexport = isexport;
	}
	
	public DimInfo getDimFromJson(JSONObject obj){
		DimInfo dim = new DimInfo();
		dim.setType(obj.getString("type"));
		dim.setId(obj.getString("id"));
		dim.setColName((String)obj.get("id"));
		dim.setTableName((String)obj.get("tableName"));
		dim.setTableColKey((String)obj.get("tableColKey"));
		dim.setTableColName((String)obj.get("tableColName"));
		dim.setVals((String)obj.get("vals"));
		dim.setIssum((String)obj.get("issum"));
		dim.setTid(obj.get("tid") != null ? obj.get("tid").toString() : null );
		dim.setDimOrd((String)obj.get("dimord"));
		dim.setColDesc((String)obj.get("dimdesc"));
		dim.setValDesc((String)obj.get("valDesc"));
		
		//日期、月份特殊处理
		if("day".equals(dim.getType()) && obj.get("startdt") != null && obj.get("startdt").toString().length() > 0){
			TableSqlJsonVO.QueryDay d = new TableSqlJsonVO.QueryDay();
			d.setStartDay((String)obj.get("startdt"));
			d.setEndDay((String)obj.get("enddt"));
			dim.setDay(d);
		}
		
		if("month".equals(dim.getType()) && obj.get("startmt") != null && obj.get("startmt").toString().length() > 0){
			TableSqlJsonVO.QueryMonth m = new TableSqlJsonVO.QueryMonth();
			m.setStartMonth((String)obj.get("startmt"));
			m.setEndMonth((String)obj.get("endmt"));
			dim.setMonth(m);
		}
		return dim;
	}
	
	public TableSqlJsonVO json2ChartSql(JSONObject chartJson, JSONArray kpiJson){
		TableSqlJsonVO vo = new TableSqlJsonVO();
		
		//取时间
		JSONObject baseDate = (JSONObject)chartJson.get("baseDate");
		if(baseDate != null && !baseDate.isNullObject()){
			TableSqlJsonVO.BaseDate bd = new TableSqlJsonVO.BaseDate();
			bd.setStart((String)baseDate.get("start"));
			bd.setEnd((String)baseDate.get("end"));
			vo.setBaseDate(bd);
		}
		
		//取维度
		JSONObject xcol = chartJson.getJSONObject("xcol");
		if(xcol != null && !xcol.isNullObject() && !xcol.isEmpty()){
			DimInfo dim = this.getDimFromJson(xcol);
			dim.setDimpos("xcol"); //表示维度来路
			vo.getDims().add(dim);
		}
		JSONObject scol = chartJson.getJSONObject("scol");
		if(scol != null && !scol.isNullObject() && !scol.isEmpty()){
			DimInfo dim = this.getDimFromJson(scol);
			dim.setDimpos("xcol"); //表示维度来路
			vo.getDims().add(dim);
		}
		
		//取参数
		JSONArray params = chartJson.get("params") == null ? null : chartJson.getJSONArray("params");
		if(params != null && !params.isEmpty()){
			for(int i=0; i<params.size(); i++){
				JSONObject obj = params.getJSONObject(i);
				DimInfo dim = this.getDimFromJson(obj);
				dim.setDimpos("param"); //表示维度来路
				vo.getDims().add(dim);
			}
		}
		
		//取指标
		for(int i=0; i<kpiJson.size(); i++){
			JSONObject kpij = kpiJson.getJSONObject(i);
			if(kpij == null || kpij.isNullObject() || kpij.isEmpty()){
				continue;
			}
			KpiInfo kpi = new KpiInfo();
			kpi.setAggre(kpij.getString("aggre"));
			kpi.setAlias(kpij.getString("alias"));
			kpi.setColName(kpij.getString("colname"));
			kpi.setFmt((String)kpij.get("fmt"));
			kpi.setKpiName((String)kpij.get("kpi_name"));
			kpi.setTid((String)kpij.get("tid"));
			kpi.setDescKey((String)kpij.get("descKey"));
			if(kpij.get("rate") != null && !kpij.get("rate").toString().equals("null") && kpij.get("rate").toString().length() > 0 ){
				kpi.setRate(kpij.getInt("rate"));
			}
			//kpi.setId(kpij.getInt("kpi_id"));
			kpi.setUnit((String)kpij.get("unit"));
			kpi.setSort((String)kpij.get("sort"));
			kpi.setMin((String)kpij.get("min"));
			kpi.setMax((String)kpij.get("ymax"));
			vo.getKpis().add(kpi);
		}
		
		return vo;
	}
	
	public static String formatUnits(KpiInfo kpi){
		Integer rate = kpi.getRate();
		if(rate == null){
			return "";
		}
		if(rate.intValue() == 1000){
			return "千";
		}else if(rate.intValue() == 10000){
			return "万";
		}else if(rate.intValue() == 1000000){
			return "百万";
		}else if(rate.intValue() == 100000000){
			return "亿";
		}
		return "";
	}
	
	public String[] list2Array(List<String> ls){
		String[] ret = new String[ls.size()];
		for(int i=0; i<ls.size(); i++){
			ret[i] = ls.get(i);
		}
		return ret;
	}
	
	public ChartContext json2Chart(JSONObject chartJson, TableSqlJsonVO sqlVO, boolean defChartSize, boolean useCube, String compId, boolean useLink) throws IOException{
		ChartContext ctx = new ChartContextImpl();
		//设置x
		JSONObject obj = chartJson.getJSONObject("xcol");
		if(obj != null && !obj.isNullObject() && !obj.isEmpty()){
			String codetable = (String)obj.get("tname");
			if(useCube && (codetable != null && codetable.length() > 0)){
				ctx.setXcol(obj.getString("id") + "_desc"); //字段ID + _DESC 作为KEY
				ctx.setXcolDesc(obj.getString("id"));
			}else{
				ctx.setXcol(obj.getString("id"));
				ctx.setXcolDesc(obj.getString("id"));
			}
		}
		
		KpiInfo kpiInfo = sqlVO.getKpis().get(0);
		String y = kpiInfo.getAlias();
		ctx.setYcol(y);
		
		//如果是散点图或气泡图，需要 y2col
		String chartType = chartJson.getString("type");
		if(chartType.equals("scatter")){
			ctx.setY2col(sqlVO.getKpis().get(1).getAlias());
		}
		if(chartType.equals("bubble")){
			ctx.setY2col(sqlVO.getKpis().get(1).getAlias());
			ctx.setY3col(sqlVO.getKpis().get(2).getAlias());
		}
		
		//设置倍率
		if(kpiInfo.getRate() != null){
			ctx.setRate(kpiInfo.getRate());
		}
		if(sqlVO.getKpis().size() > 1){
			ctx.setRate2(sqlVO.getKpis().get(1).getRate());
		}
		if(sqlVO.getKpis().size() > 2){
			ctx.setRate3(sqlVO.getKpis().get(2).getRate());
		}
		
		JSONObject scol = chartJson.getJSONObject("scol");
		if(scol != null && !scol.isNullObject() && !scol.isEmpty()){
			String codetable = (String)obj.get("tname");
			if(useCube && (codetable != null && codetable.length() > 0)){
				ctx.setScol(scol.getString("id")+"_desc"); //字段ID + _DESC 作为KEY
			}else{
				ctx.setScol(scol.getString("id"));
			}
		}
		ctx.setShape(chartJson.getString("type"));
		if(defChartSize){
			ctx.setWidth("600");
			ctx.setHeight("250");
		}else{
			//如果是PHONE模式， 并且不是导出，自动生成图形宽度为100% 
			if("PHONE".equals(outType) && isexport == false){
				ctx.setWidth("100%");
			}else{
				ctx.setWidth(chartJson.get("width") == null ? "600" : (String)chartJson.get("width"));
			}
			ctx.setHeight(chartJson.get("height") == null ? "250" : (String)chartJson.get("height"));
		}
		//设置ID
		String chartId = (String)chartJson.get("compid");
		if(chartId == null || chartId.length() == 0){
			chartId = ExtConstants.chartIdPrefix + IdCreater.create();
		}
		ctx.setId(chartId);
		
		//设置图形位置，默认为居中
		String align = (String)chartJson.get("align");
		if(align == null || align.length() == 0){
			align = "center";
		}
		ctx.setAlign(align);
		
		//设置配置信息
		List<ChartKeyContext> properties = new ArrayList<ChartKeyContext>();
		String unitStr = "";
		String unit = kpiInfo.getUnit();
		if(unit != null){
			unitStr =  "(" + formatUnits(kpiInfo) +kpiInfo.getUnit()+")";
		}
		properties.add(new ChartKeyContext("ydesc",kpiInfo.getKpiName()+unitStr));
		if("bubble".equals(ctx.getShape()) || "scatter".equals(ctx.getShape())){
			KpiInfo kpiInfo2 = sqlVO.getKpis().get(1);
			//对于散点图和气泡图，需要设置xdesc
			properties.add(new ChartKeyContext("xdesc", kpiInfo2.getKpiName() + "(" + formatUnits(kpiInfo2) +kpiInfo2.getUnit()+")"));
		}else
		if(!chartJson.getJSONObject("xcol").isNullObject() && !chartJson.getJSONObject("xcol").isEmpty()){
			properties.add(new ChartKeyContext("xdesc", chartJson.getJSONObject("xcol").getString("dimdesc")));
		}
		//title
		String tit = (String)chartJson.get("title");
		if(tit != null && tit.length() > 0){
			ChartTitleContext title = new ChartTitleContext();
			if(tit.indexOf("$") >= 0){
				String tname = TemplateManager.getInstance().createTemplate(tit);
				title.setTemplateName(tname);
				title.setType("template");
			}else{
				title.setText(tit);
				title.setType("text");
			}
			ctx.setTitle(title);
		}
		
		//格式化配置信息
		if(kpiInfo.getFmt() != null && kpiInfo.getFmt().length() > 0){
			properties.add(new ChartKeyContext("formatCol", kpiInfo.getFmt()));
		}
		
		if(kpiInfo.getUnit() != null && kpiInfo.getUnit().length() > 0){
			properties.add(new ChartKeyContext("unitCol", kpiInfo.getUnit()));
		}
		if(kpiInfo.getMin() != null && kpiInfo.getMin().length() > 0){
			properties.add(new ChartKeyContext("ymin", kpiInfo.getMin()));
		}
		if(kpiInfo.getMax() != null && kpiInfo.getMax().length() > 0){
			properties.add(new ChartKeyContext("ymax", kpiInfo.getMax()));
		}
		//lengend
		if(chartJson.get("showLegend") != null && (Boolean)chartJson.get("showLegend") == true){
			ChartKeyContext val1 = new ChartKeyContext("showLegend", "true");
			properties.add(val1);
		}else{
			ChartKeyContext val1 = new ChartKeyContext("showLegend", "false");
			properties.add(val1);
		}
		//legendLayout
		String legendLayout = (String)chartJson.get("legendLayout");
		if(legendLayout != null){
			ChartKeyContext val1 = new ChartKeyContext("legendLayout", legendLayout);
			properties.add(val1);
		}
		String legendpos = (String)chartJson.get("legendpos");
		if(legendpos != null && legendpos.length() > 0){
			ChartKeyContext val1 = new ChartKeyContext("legendPosition", legendpos);
			properties.add(val1);
		}
		
		if(obj != null && !obj.isNullObject() && !obj.isEmpty()){
			if(obj.get("tickInterval") != null){
				ChartKeyContext val1 = new ChartKeyContext("tickInterval", (String)obj.get("tickInterval"));
				properties.add(val1);
			}
			if(obj.get("routeXaxisLable") != null){
				ChartKeyContext val1 = new ChartKeyContext("routeXaxisLable", (String)obj.get("routeXaxisLable"));
				properties.add(val1);
			}
			if(obj.get("top") != null){
				ChartKeyContext val1 = new ChartKeyContext("xcnt", (String)obj.get("top"));
				properties.add(val1);
			}
		}
		//设置饼图是否显示标签
		if(chartJson.get("dataLabel") != null && (Boolean)chartJson.get("dataLabel") == true){
			ChartKeyContext val1 = new ChartKeyContext("showLabel", "true");
			properties.add(val1);
		}
		//饼图增加标签长度和标签类型字段
		if(chartJson.get("labelType") != null){
			ChartKeyContext val1 = new ChartKeyContext("labelType", (String)chartJson.get("labelType"));
			properties.add(val1);
		}
		if(chartJson.get("distance") != null){
			ChartKeyContext val1 = new ChartKeyContext("distance", (String)chartJson.get("distance"));
			properties.add(val1);
		}
		//设置仪表盘数量
		ChartKeyContext val1 = new ChartKeyContext("gaugeCnt", "1");
		properties.add(val1);
		
		//面积图不显示描点
		if("area".equals(ctx.getShape())){
			ChartKeyContext md = new ChartKeyContext("markerEnabled", "false");
			properties.add(md);
		}
		
		ctx.setProperties(properties);
		
		//判断是否有事件
		JSONObject link = (JSONObject)chartJson.get("link");
		if(link != null && !link.isNullObject() && !link.isEmpty() && useLink){
			ctx.setLink(createChartLink(link));
		}
		JSONObject linkAccept = (JSONObject)chartJson.get("linkAccept");
		if(linkAccept != null && !linkAccept.isNullObject() && !linkAccept.isEmpty()){
			if(useLink){
				ctx.setLabel(compId);
			}
		}
		
		//判断曲线图、柱状图是否双坐标轴
		String typeIndex = (String)chartJson.get("typeIndex");
		if((chartType.equals("column")||chartType.equals("line")) && "2".equals(typeIndex) && sqlVO.getKpis().size() > 1 && sqlVO.getKpis().get(1) != null){
			List<KpiInfo> kpis = sqlVO.getKpis();
			ctx.setY2col(kpis.get(1).getAlias());
			String y2unit = formatUnits(kpis.get(1)) + (kpis.get(1).getUnit() == null ? "" : kpis.get(1).getUnit()) ;
			ChartKeyContext y2desc = new ChartKeyContext("y2desc", kpis.get(1).getKpiName() + (y2unit.length() ==0 ? "" : "("+y2unit+")"));
			properties.add(y2desc);
			ChartKeyContext y2fmtcol = new ChartKeyContext("formatCol2", kpis.get(1).getFmt());
			properties.add(y2fmtcol);
			if(y2unit != null && y2unit.length() > 0){
				ChartKeyContext unitCol2 = new ChartKeyContext("unitCol2", y2unit);
				properties.add(unitCol2);
			}
		}
		
		return ctx;
	}
	
	public ChartLinkContext createChartLink(JSONObject link){
		String type = (String)link.get("type");
		String target = (String)link.get("target");
		String url = (String)link.get("url");
		
		ChartLinkContext clink = new ChartLinkContext();
		clink.setTarget(target.split(","));
		clink.setType(type.split(","));
		return clink;
	}
	
	public GridDataCenterContext createDataCenter(TableSqlJsonVO sqlVO, String sql, ChartContext chart, String dsid, boolean useCube, JSONObject linkAccept) throws IOException{
		GridDataCenterContext ctx = new GridDataCenterContextImpl();
		GridSetConfContext conf = new GridSetConfContext();
		if(dsid != null){
			conf.setRefDsource(dsid);
		}
		ctx.setConf(conf);
		ctx.setId("DC-" + IdCreater.create());
		
		String name = TemplateManager.getInstance().createTemplate(sql);
		ctx.getConf().setTemplateName(name);
		
		//是否处理process,如果是多维分析的CUBE,不需要process
		if(useCube == false){
			//判断是否需要事件中的参数过滤
			if(linkAccept != null && !linkAccept.isNullObject() && !linkAccept.isEmpty()){
				GridFilterContext filter = new GridFilterContext();
				filter.setColumn(linkAccept.getString("col"));
				filter.setValue("${"+linkAccept.getString("col")+"}");
				filter.setFilterType(GridFilter.equal);
				ctx.getProcess().add(filter);
			}
			
			KpiInfo k = sqlVO.getKpis().get(0);
			//先聚合数据
			GridAggregationContext agg = new GridAggregationContext();
			if(chart.getScol() != null && chart.getScol().length() > 0){
				agg.setColumn(new String[]{chart.getXcol(), chart.getScol()});
			}else{
				agg.setColumn(new String[]{chart.getXcol()});
			}
			List<String> ycols = new ArrayList<String>();
			ycols.add(chart.getYcol());
			if(chart.getY2col() != null && chart.getY2col().length() > 0){
				ycols.add(chart.getY2col());
			}
			if(chart.getY3col() != null && chart.getY3col().length() > 0){
				ycols.add(chart.getY3col());
			}
			List<String> types = new ArrayList<String>();
			types.add(k.getAggre());
			if(sqlVO.getKpis().size() > 1 && sqlVO.getKpis().get(1) != null){
				types.add(sqlVO.getKpis().get(1).getAggre());
			}
			if(sqlVO.getKpis().size() > 2 && sqlVO.getKpis().get(2) != null){
				types.add(sqlVO.getKpis().get(2).getAggre());
			}
			//设置聚合配置对象
			AggreVO[] avo = new AggreVO[ycols.size()];
			for(int i=0; i<ycols.size(); i++){
				AggreVO vo = new AggreVO();
				vo.setName(ycols.get(i));
				vo.setType(types.get(i));
				avo[i] = vo;
			}
			agg.setAggreVO(avo);
			ctx.getProcess().add(agg);
			
			//处理排序
			List<DimInfo> dims = sqlVO.getDims();
			for(DimInfo dim : dims){
				String ord = dim.getDimOrd();
				if(ord != null && ord.length() > 0){
					GridSortContext sort = new GridSortContext();
					sort.setColumn(dim.getColName());
					sort.setType(ord);
					sort.setChangeOldOrder(true);
					ctx.getProcess().add(sort);
				}
			}
			//处理指标排序
			String kpiSort = k.getSort();
			if(kpiSort != null && kpiSort.length() > 0){
				GridSortContext ksort = new GridSortContext();
				ksort.setColumn(k.getColName());
				ksort.setType(kpiSort);
				ksort.setChangeOldOrder(true);
				ctx.getProcess().add(ksort);
			}
		}
		return ctx;
	}
	
	/**
	 * 通过olap 立方体 构建sql
	 * @return
	 */
	public String crtSqlByOlapCube(JSONObject chartJson, JSONArray kpiJson, JSONObject cube){
		String tname = cube.getString("tname");
		StringBuffer sb = new StringBuffer();
		List<String> groupStr = new ArrayList<String>();
		List<String> sortStr = new ArrayList<String>();
		sb.append("select ");
		int tableIdx = 1;
		//xcol
		JSONObject xcol = chartJson.getJSONObject("xcol");
		if(!xcol.isNullObject() && !xcol.isEmpty()){
			JSONObject col = xcol;
			String codetable = (String)col.get("tname"); //维度表
			if(codetable != null && codetable.length() > 0){  //如果有关联维度表，进行维度关联
				sb.append(" a" + tableIdx+ "." + col.getString("colname"));
				sb.append(" as " + col.getString("id") + ",");
				sb.append(" a" + tableIdx+ "." + col.getString("colnamedesc")); //启用ID加_desc方式做别名
				sb.append(" as "+col.getString("id")+"_desc,");
				groupStr.add("a"+tableIdx+"." + col.getString("colname"));
				groupStr.add("a"+tableIdx+"." +col.getString("colnamedesc"));
				String ord = (String)col.get("dimord");
				if(ord != null && ord.length() > 0){
					//处理维度排序字段
					String ordcol = (String)col.get("ordcol");
					if(ordcol != null && ordcol.length() > 0 && codetable != null && codetable.length() > 0){
						sortStr.add(" a" + tableIdx+ "." + ordcol +  " " + ord);
						groupStr.add(" a" + tableIdx+ "." + ordcol);
						sb.append(" a" + tableIdx+ "." + ordcol+",");
					}else{
						sortStr.add(col.getString("id") + " " + ord);
					}
				}
				tableIdx++;
			}else{
				sb.append(" " + col.getString("colname"));
				groupStr.add(col.getString("colname"));
				sb.append(",");
				String ord = (String)col.get("dimord");
				if(ord != null && ord.length() > 0){
					//处理维度排序字段
					String ordcol =  (String)col.get("ordcol");
					if(ordcol != null && ordcol.length() > 0 && codetable != null && codetable.length() > 0){
						sortStr.add( ordcol +  " " + ord);
						groupStr.add( ordcol);
						sb.append(ordcol+",");
					}else{
						sortStr.add(col.getString("id") + " " + ord);
					}
				}
			}
		}
		JSONObject scolObj = (JSONObject)chartJson.get("scol");
		if(scolObj != null &&  !scolObj.isNullObject() && !scolObj.isEmpty()){
			JSONObject row = scolObj;
			String codetable = (String)row.get("tname"); //维度表
			if(codetable != null && codetable.length() > 0){  //如果有关联维度表，进行维度关联
				sb.append(" a" + tableIdx+ "." + row.getString("colname"));
				sb.append(" as " + row.getString("id") + ",");
				sb.append(" a" + tableIdx+ "." + row.getString("colnamedesc")); //启用ID加_desc方式做别名
				sb.append(" as "+row.getString("id")+"_desc,");
				groupStr.add("a"+tableIdx+"." +row.getString("colname"));
				groupStr.add("a"+tableIdx+"." +row.getString("colnamedesc"));
				String ord = (String)row.get("dimord");
				if(ord != null && ord.length() > 0){
					//处理维度排序字段
					String ordcol =  (String)row.get("ordcol");
					if(ordcol != null && ordcol.length() > 0 && codetable != null && codetable.length() > 0){
						sortStr.add(" a" + tableIdx+ "." + ordcol +  " " + ord);
						groupStr.add(" a" + tableIdx+ "." + ordcol);
						sb.append(" a" + tableIdx+ "." + ordcol+",");
					}else{
						sortStr.add(row.getString("id") + " " + ord);
					}
				}
				tableIdx++;
			}else{
				sb.append(" " + row.getString("colname"));
				sb.append(",");
				groupStr.add(row.getString("colname"));
				String ord = (String)row.get("dimord");
				if(ord != null && ord.length() > 0){
					//处理维度排序字段
					String ordcol =  (String)row.get("ordcol");
					if(ordcol != null && ordcol.length() > 0 && codetable != null && codetable.length() > 0){
						sortStr.add( ordcol +  " " + ord);
						groupStr.add( ordcol);
						sb.append(ordcol+",");
					}else{
						sortStr.add(row.getString("id") + " " + ord);
					}
				}
			}
		}
		//查询指标
		for(int i=0; i<kpiJson.size(); i++){
			JSONObject kpi = kpiJson.getJSONObject(i);
			if(kpi == null || kpi.isNullObject() || kpi.isEmpty()){
				continue;
			}
			int calc = kpi.getInt("calc");
			if(calc == 1){
				sb.append(kpi.getString("colname"));
			}else{
				sb.append(kpi.getString("aggre") + "(" + kpi.getString("colname") + ")");
			}
			sb.append(" as " + kpi.getString("alias"));
			sb.append(",");
			
			String sort = (String)kpi.get("sort");
			if(sort != null && sort.length() > 0){ //指标排序优先与维度排序
				sortStr.add(0, kpi.getString("alias")+" " + sort);
			}
		}
		sb = new StringBuffer(sb.subSequence(0, sb.length() - 1)); //截取 ， 逗号
		sb.append(" from");
		sb.append(" "+tname+" a0");
		//码表
		tableIdx = 1;
		if(!xcol.isNullObject() && !xcol.isEmpty()){
			JSONObject col = xcol;
			String codetable = (String)col.get("tname"); //维度表
			if(codetable != null && codetable.length() > 0){  //如果有关联维度表，进行维度关联
				sb.append(",");
				sb.append(col.getString("tname"));
				sb.append(" a" + tableIdx);
				tableIdx++;
			}
		}
		if(scolObj != null &&  !scolObj.isNullObject() && !scolObj.isEmpty()){
			JSONObject row = scolObj;
			String codetable = (String)row.get("tname"); //维度表
			if(codetable != null && codetable.length() > 0){  //如果有关联维度表，进行维度关联
				sb.append(",");
				sb.append(row.getString("tname"));
				sb.append(" a" + tableIdx);
				tableIdx++;
			}
		}
		//关联表
		sb.append(" where 1=1");
		tableIdx = 1;
		if(!xcol.isNullObject() && !xcol.isEmpty()){
			JSONObject col = xcol;
			String codetable = (String)col.get("tname"); //维度表
			if(codetable != null && codetable.length() > 0){  //如果有关联维度表，进行维度关联
				sb.append(" and");
				sb.append(" a0." + col.getString("id"));
				sb.append("=");
				sb.append(" a"+tableIdx+"." + col.getString("colname"));
				tableIdx++;
			}
		}
		if(scolObj != null &&  !scolObj.isNullObject() && !scolObj.isEmpty()){
			JSONObject row = scolObj;
			String codetable = (String)row.get("tname"); //维度表
			if(codetable != null && codetable.length() > 0){  //如果有关联维度表，进行维度关联
				sb.append(" and");
				sb.append(" a0." + row.getString("id"));
				sb.append("=");
				sb.append(" a"+tableIdx+"." + row.getString("colname"));
				tableIdx++;
			}
		}
		//处理过滤条件
		JSONArray params = (JSONArray)cube.get("param");
		if(params != null){
			sb.append(CrossReportService.dealCubeParams(params));
		}
		
		//判断是否有关联
		JSONObject linkAccept = (JSONObject)chartJson.get("linkAccept");
		if(linkAccept != null && !linkAccept.isNullObject() && !linkAccept.isEmpty()){
			String col = (String)linkAccept.get("col");
			String dftval = (String)linkAccept.get("dftval");
			String valtype = (String)linkAccept.get("valType");
			String ncol = "$" + col;
			if("string".equalsIgnoreCase(valtype)){
				dftval = "'" + dftval + "'";
			}
			sb.append(" and  " + col + " = " + ncol);
		}
		
		//生成group
		if(groupStr.size() > 0){
			sb.append(" group by ");
			for(int i=0; i<groupStr.size(); i++){
				sb.append(groupStr.get(i));
				if(i != groupStr.size() - 1){
					sb.append(",");
				}
			}
		}
		//排序
		if(sortStr.size() > 0){
			sb.append(" order by ");
			for(int i=0; i<sortStr.size(); i++){
				sb.append(sortStr.get(i));
				if(i != sortStr.size() - 1){
					sb.append(",");
				}
			}
		}
		return sb.toString();
	}
	/**
	 * 生成隐藏参数
	 * @param params
	 * @param mv
	 * @param insertParam
	 * @throws ExtConfigException
	 */
	public void parserHiddenParam(JSONArray params, MVContext mv) throws ExtConfigException{
		if(params == null || params.isEmpty()){
			return;
		}
		for(int i=0; i<params.size(); i++){
			JSONObject param = params.getJSONObject(i);
			if(param.isNullObject() || param.isEmpty()){
				continue;
			}
			String id = param.getString("id");
			String desc = param.getString("desc");
			String tp = param.getString("type");
			String val = param.getString("defvalue");
			TextFieldContext input =  new TextFieldContextImpl();
			input.setType("hidden");
			input.setShow(false);
			input.setDesc(desc);
			input.setId(id);
			input.setDefaultValue(val);
			
			mv.getChildren().add(input);
			input.setParent(mv);
			
			//添加参数,在预览时需要，在发布是不需要
			this.mvParams.put(id, input);
			ExtContext.getInstance().putServiceParam(mv.getMvid(), id, input);
			mv.setShowForm(true);
		}
	}
	
	public MVContext json2MV(JSONObject chartJson, JSONArray kpiJson, JSONObject dsource, JSONObject dset, JSONObject cube, JSONArray params, boolean defSize) throws Exception{
		TableSqlJsonVO sqlVO = json2ChartSql(chartJson, kpiJson);
		
		//创建MV
		MVContext mv = new MVContextImpl();
		mv.setChildren(new ArrayList());
		String formId = ExtConstants.formIdPrefix + IdCreater.create();
		mv.setFormId(formId);
		mv.setMvid(this.defMvid == null ?PageService.deftMvId : this.defMvid);
		
		//创建param
		this.parserHiddenParam(params, mv);
		
		boolean useCube;
		String sql = "";
		//如果使用OLAP的CUBE作为数据源，通过 cube 生成SQL
		if(cube.isNullObject() || cube.isEmpty() || !"olap".equals(cube.get("from"))){
			sql = dataService.createDatasetSql(dset);
			useCube = false;
		}else{
			sql = this.crtSqlByOlapCube(chartJson, kpiJson, cube);
			useCube = true;
		}
		//创建chart
		ChartContext cr = this.json2Chart(chartJson, sqlVO, defSize, useCube, null, false);
		mv.getChildren().add(cr);
		cr.setParent(mv);
		
		//判断是否有事件，是否需要添加参数
		JSONObject linkAccept = (JSONObject)chartJson.get("linkAccept");
		if(linkAccept != null && !linkAccept.isNullObject() && !linkAccept.isEmpty()){
			//创建参数
			TextFieldContext linkText = new TextFieldContextImpl();
			linkText.setType("hidden");
			linkText.setDefaultValue((String)linkAccept.get("dftval"));
			linkText.setId((String)linkAccept.get("col"));
			mv.getChildren().add(0, linkText);
			linkText.setParent(mv);
			this.mvParams.put(linkText.getId(), linkText);
			ExtContext.getInstance().putServiceParam(mv.getMvid(), linkText.getId(), linkText);
		}
		
		Map crs = new HashMap();
		crs.put(cr.getId(), cr);
		mv.setCharts(crs);
		
		//创建datasource
		String dsid = null;
		if(dsource != null && !dsource.isNullObject() && !dsource.isEmpty()){
			DataSourceContext ds = new DataSourceContext();
			ds.putProperty("linktype", dsource.getString("linktype"));
			ds.putProperty("linkname", dsource.getString("linkname"));
			ds.putProperty("linkpwd", dsource.getString("linkpwd"));
			ds.putProperty("linkurl", dsource.getString("linkurl"));
			ds.putProperty("id", dsource.getString("dsid"));
			if(mv.getDsources() == null){
				mv.setDsources(new HashMap<String, DataSourceContext>());
			}
			mv.getDsources().put(ds.getId(), ds);
			dsid = ds.getId();
		}
		//创建datacenter
		GridDataCenterContext dc = this.createDataCenter(sqlVO, sql, cr, dsid, useCube, null);
		cr.setRefDataCenter(dc.getId());
		if(mv.getGridDataCenters() == null){
			mv.setGridDataCenters(new HashMap<String, GridDataCenterContext>());
		}
		mv.getGridDataCenters().put(dc.getId(), dc);
		return mv;
	}

	public Map<String, InputField> getMvParams() {
		return mvParams;
	}

	public void setMvParams(Map<String, InputField> mvParams) {
		this.mvParams = mvParams;
	}
}
