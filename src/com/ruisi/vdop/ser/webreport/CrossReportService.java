package com.ruisi.vdop.ser.webreport;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.ruisi.ext.engine.init.TemplateManager;
import com.ruisi.ext.engine.util.IdCreater;
import com.ruisi.ext.engine.view.context.cross.BaseKpiField;
import com.ruisi.ext.engine.view.context.cross.CrossCols;
import com.ruisi.ext.engine.view.context.cross.CrossField;
import com.ruisi.ext.engine.view.context.cross.CrossKpi;
import com.ruisi.ext.engine.view.context.cross.CrossReportContext;
import com.ruisi.ext.engine.view.context.cross.CrossReportContextImpl;
import com.ruisi.ext.engine.view.context.cross.CrossRows;
import com.ruisi.ext.engine.view.context.cross.RowDimContext;
import com.ruisi.ext.engine.view.context.cross.RowHeadContext;
import com.ruisi.ext.engine.view.context.cross.RowLinkContext;
import com.ruisi.ext.engine.view.context.dc.grid.AggreVO;
import com.ruisi.ext.engine.view.context.dc.grid.GridAggregationContext;
import com.ruisi.ext.engine.view.context.dc.grid.GridDataCenterContext;
import com.ruisi.ext.engine.view.context.dc.grid.GridDataCenterContextImpl;
import com.ruisi.ext.engine.view.context.dc.grid.GridFilterContext;
import com.ruisi.ext.engine.view.context.dc.grid.GridSetConfContext;
import com.ruisi.ext.engine.view.context.dc.grid.GridSortContext;
import com.ruisi.ispire.dc.grid.GridFilter;
import com.ruisi.vdop.ser.bireport.MyCrossFieldLoader;
import com.ruisi.vdop.ser.bireport.TableSqlJsonVO;
import com.ruisi.vdop.ser.bireport.TableSqlJsonVO.DimInfo;
import com.ruisi.vdop.ser.bireport.TableSqlJsonVO.KpiInfo;

public class CrossReportService {
	
	private DataService dataService = new DataService();
	private ChartService chartService;
	
	private StringBuffer css;
	private StringBuffer scripts;
	
	public CrossReportService(StringBuffer css, StringBuffer scripts){
		this.css = css;
		this.scripts = scripts;
		this.chartService = new ChartService(null, false);
	}

	public TableSqlJsonVO json2TableSql(JSONObject tableJson, JSONArray kpiJson){
		TableSqlJsonVO vo = new TableSqlJsonVO();
		
		//取时间
		JSONObject baseDate = (JSONObject)tableJson.get("baseDate");
		if(baseDate != null && !baseDate.isNullObject()){
			TableSqlJsonVO.BaseDate bd = new TableSqlJsonVO.BaseDate();
			bd.setStart((String)baseDate.get("start"));
			bd.setEnd((String)baseDate.get("end"));
			vo.setBaseDate(bd);
		}
		
		//取维度
		JSONArray cols = tableJson.getJSONArray("cols");
		if(cols != null && !cols.isEmpty()){
			for(int i=0; i<cols.size(); i++){
				DimInfo dim = chartService.getDimFromJson(cols.getJSONObject(i));
				dim.setDimpos("col"); //表示维度来路
				vo.getDims().add(dim);
			}
		}
		JSONArray rows = tableJson.getJSONArray("rows");
		if(rows != null && !rows.isEmpty()){
			for(int i=0; i<rows.size(); i++){
				DimInfo dim = chartService.getDimFromJson(rows.getJSONObject(i));
				dim.setDimpos("row"); //表示维度来路
				vo.getDims().add(dim);
			}
		}
		
		//取参数
		JSONArray params = tableJson.get("params") == null ? null : tableJson.getJSONArray("params");
		if(params != null && !params.isEmpty()){
			for(int i=0; i<params.size(); i++){
				JSONObject obj = params.getJSONObject(i);
				DimInfo dim = chartService.getDimFromJson(obj);
				dim.setDimpos("param"); //表示维度来路
				vo.getDims().add(dim);
			}
		}
		
		//取指标
		for(int i=0; i<kpiJson.size(); i++){
			JSONObject kpij = kpiJson.getJSONObject(i);
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
			kpi.setFuncname((String)kpij.get("funcname"));
			kpi.setCode((String)kpij.get("code"));
			JSONObject style = (JSONObject)kpij.get("style");
			kpi.setStyle(style);
			JSONObject warn = (JSONObject)kpij.get("warning");
			kpi.setWarn(warn);
			vo.getKpis().add(kpi);
		}
		
		return vo;
	}
	
	
	/**
	 * 通过olap的立方体创建数据中心
	 * @param drillLevel 钻取的层级0,不钻取， 1为一级钻取
	 * @throws IOException 
	 */
	public GridDataCenterContext createDataCenterByOlap(JSONObject json, JSONArray params, JSONObject linkAccept, int drillLevel) throws IOException{
		JSONObject tableJson = json.getJSONObject("tableJson");
		JSONArray kpiJson = json.getJSONArray("kpiJson");
		String tname =  json.getString("tname");
		GridDataCenterContext ctx = new GridDataCenterContextImpl();
		GridSetConfContext conf = new GridSetConfContext();
		ctx.setConf(conf);
		ctx.setId("DC-" + IdCreater.create());
		StringBuffer sb = new StringBuffer();
		List<String> groupStr = new ArrayList<String>();
		List<String> sortStr = new ArrayList<String>();
		sb.append("select ");
		int tableIdx = 1;
		JSONArray cols = tableJson.getJSONArray("cols");
		for(int i=0; i<cols.size(); i++){
			JSONObject col = cols.getJSONObject(i);
			String type = col.getString("type");
			if("kpiOther".equals(type)){
				continue;
			}
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
					if(ordcol != null && ordcol.length() > 0){  
						sortStr.add(" a" + tableIdx+ "." + ordcol + " " + ord);
						groupStr.add(" a" + tableIdx + "." + ordcol);
					}else{
						sortStr.add(" a" + tableIdx+ "." + col.getString("colname") + " " + ord);
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
					String ordcol = (String)col.get("ordcol");
					if(ordcol != null && ordcol.length() > 0){
						sortStr.add(" a" + tableIdx+ "." + ordcol + " " + ord);
						groupStr.add(" a" + tableIdx + "." + ordcol);
					}else{
						sortStr.add(col.getString("colname") + " " + ord);
					}
				}
			}
		}
		JSONArray rows = tableJson.getJSONArray("rows");
		for(int i=0; i<rows.size(); i++){
			JSONObject row = rows.getJSONObject(i);
			String codetable = (String)row.get("tname"); //维度表
			if(codetable != null && codetable.length() > 0){
				sb.append(" a" + tableIdx+ "." + row.getString("colname"));
				sb.append(" as " + row.getString("id") + ",");
				sb.append(" a" + tableIdx+ "." + row.getString("colnamedesc")); //启用ID加_desc方式做别名
				sb.append(" as "+row.getString("id")+"_desc,");
				groupStr.add("a"+tableIdx+"." +row.getString("colname"));
				groupStr.add("a"+tableIdx+"." +row.getString("colnamedesc"));
				String ord = (String)row.get("dimord");
				if(ord != null && ord.length() > 0){
					//处理维度排序字段
					String ordcol = (String)row.get("ordcol");
					if(ordcol != null && ordcol.length() > 0){
						sortStr.add(" a" + tableIdx+ "." + ordcol + " " + ord);
						groupStr.add(" a" + tableIdx + "." + ordcol);
					}else{
						sortStr.add(" a" + tableIdx+ "." + row.getString("colname") + " " + ord);
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
					String ordcol = (String)row.get("ordcol");
					if(ordcol != null && ordcol.length() > 0){
						sortStr.add(" a" + tableIdx+ "." + ordcol + " " + ord);
						groupStr.add(" a" + tableIdx + "." + ordcol);
					}else{
						sortStr.add(row.getString("colname") + " " + ord);
					}
				}
			}
		}
		//查询钻取维, 根据层级设置来查询
		JSONArray drillDim = (JSONArray)json.get("drillDim");
		if(drillDim != null && drillDim.size() >= drillLevel){
			for(int i=0; i<drillLevel; i++){
				JSONObject dim = drillDim.getJSONObject(i);
				String codetable = (String)dim.get("tname"); //维度表
				if(codetable != null && codetable.length() > 0){
					sb.append(" a" + tableIdx+ "." + dim.getString("colname"));
					sb.append(" as " + dim.getString("code") + ",");
					sb.append(" a" + tableIdx+ "." + dim.getString("colnamedesc")); //启用ID加_desc方式做别名
					sb.append(" as "+dim.getString("code")+"_desc,");
					groupStr.add("a"+tableIdx+"." +dim.getString("colname"));
					groupStr.add("a"+tableIdx+"." +dim.getString("colnamedesc"));
					String ord = (String)dim.get("dimord");
					if(ord != null && ord.length() > 0){
						//处理维度排序字段
						String ordcol = (String)dim.get("ordcol");
						if(ordcol != null && ordcol.length() > 0){
							sortStr.add(" a" + tableIdx+ "." + ordcol + " " + ord);
							groupStr.add(" a" + tableIdx + "." + ordcol);
						}else{
							sortStr.add(" a" + tableIdx+ "." + dim.getString("colname") + " " + ord);
						}
					}
					tableIdx++;
				}else{
					sb.append(" " + dim.getString("code"));
					sb.append(",");
					groupStr.add(dim.getString("code"));
					String ord = (String)dim.get("dimord");
					if(ord != null && ord.length() > 0){
						//处理维度排序字段
						String ordcol = (String)dim.get("ordcol");
						if(ordcol != null && ordcol.length() > 0){
							sortStr.add(" a" + tableIdx+ "." + ordcol + " " + ord);
							groupStr.add(" a" + tableIdx + "." + ordcol);
						}else{
							sortStr.add(dim.getString("code") + " " + ord);
						}
					}
				}
			}
		}
		
		//查询指标
		for(int i=0; i<kpiJson.size(); i++){
			JSONObject kpi = kpiJson.getJSONObject(i);
			int calc = kpi.getInt("calc");
			if(calc == 1){
				sb.append(kpi.getString("colname"));
			}else{
				sb.append(kpi.getString("aggre") + "(" + kpi.getString("colname") + ")");
			}
			sb.append(" as " + kpi.getString("alias"));
			if(i != kpiJson.size() - 1){
				sb.append(",");
			}
			String sort = (String)kpi.get("sort");
			if(sort != null && sort.length() > 0){
				sortStr.add(0, kpi.getString("alias") + " " + sort);
			}
		}
		sb.append(" from");
		sb.append(" "+tname+" a0");
		//码表
		int index = 1;
		for(int i=0; i<cols.size(); i++){
			JSONObject col = cols.getJSONObject(i);
			String codetable = (String)col.get("tname"); //维度表
			if(codetable != null && codetable.length() > 0){
				sb.append(",");
				sb.append(col.getString("tname"));
				sb.append(" a"+(index));
				index++;
			}
		}
		for(int i=0; i<rows.size(); i++){
			JSONObject row = rows.getJSONObject(i);
			String codetable = (String)row.get("tname"); //维度表
			if(codetable != null && codetable.length() > 0){
				sb.append(",");
				sb.append(row.getString("tname"));
				sb.append(" a"+(index));
				index++;
			}
		}
		//钻取码表
		if(drillDim != null && drillDim.size() >= drillLevel){
			for(int i=0; i<drillLevel; i++){
				JSONObject dim = drillDim.getJSONObject(i);
				String codetable = (String)dim.get("tname"); //维度表
				if(codetable != null && codetable.length() > 0){
					sb.append(",");
					sb.append(dim.getString("tname"));
					sb.append(" a"+(index));
					index++;
				}
			}
		}
		//关联表
		sb.append(" where 1=1");
		index=1;
		for(int i=0; i<cols.size(); i++){
			JSONObject col = cols.getJSONObject(i);
			String codetable = (String)col.get("tname"); //维度表
			if(codetable != null && codetable.length() > 0){
				sb.append(" and");
				sb.append(" a0." + col.getString("id"));
				sb.append("=");
				sb.append(" a"+(index) + "." + col.getString("colname"));
				index++;
			}
		}
		for(int i=0; i<rows.size(); i++){
			JSONObject row = rows.getJSONObject(i);
			String codetable = (String)row.get("tname"); //维度表
			if(codetable != null && codetable.length() > 0){
				sb.append(" and");
				sb.append(" a0." + row.getString("id"));
				sb.append("=");
				sb.append(" a"+(index) + "." + row.getString("colname"));
				index++;
			}
		}
		//钻取码表关联
		if(drillDim != null && drillDim.size() >= drillLevel){
			for(int i=0; i<drillLevel; i++){
				JSONObject dim = drillDim.getJSONObject(i);
				String codetable = (String)dim.get("tname"); //维度表
				if(codetable != null && codetable.length() > 0){
					sb.append(" and");
					sb.append(" a0." + dim.getString("code"));
					sb.append("=");
					sb.append(" a"+(index) + "." + dim.getString("colname"));
					index++;
				}
			}
		}
		
		//处理过滤条件
		if(params != null){
			sb.append(dealCubeParams(params));
		}
		//在钻取的时候设置过滤
		if(drillLevel == 1){
			JSONObject row = rows.getJSONObject(0);
			sb.append(" and a0." + row.getString("id")+" = $"+row.getString("id"));
		}
		
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
		if(groupStr.size() > 0 ){
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
		
		String name = TemplateManager.getInstance().createTemplate(sb.toString());
		ctx.getConf().setTemplateName(name);
		
		return ctx;
	}
	
	public static String dealCubeParams(JSONArray params){
		StringBuffer sb = new StringBuffer("");
		for(int i=0; i<params.size(); i++){
			JSONObject param = params.getJSONObject(i);
			String filterid = (String)param.get("filterid");
			if(filterid == null || filterid.length() == 0){
				continue;
			}
			String col = param.getString("col");
			String type = param.getString("type");
			String val = (String)param.get("val");
			String val2 = (String)param.get("val2");
			String valuetype = param.getString("valuetype");
			String usetype = param.getString("usetype");
			String linkparam = (String)param.get("linkparam");
			String linkparam2 = (String)param.get("linkparam2");
			String tablealias = "a0";
			
			if(type.equals("like")){
				if(val != null){
					val = "%"+val+"%";
				}
				if(val2 != null){
					val2 = "%"+val2+"%";
				}
			}
			if("string".equals(valuetype)){
				if(val != null){
					val = "'" + val + "'";
				}
				if(val2 != null){
					val2 = "'" + val2 + "'";
				}
			}
			if(type.equals("between")){
				if(usetype.equals("gdz")){
					sb.append(" and " + (tablealias != null && tablealias.length() > 0 ? tablealias+".":"") + col + " " + type + " " + val + " and " + val2);
				}else{
					sb.append("#if([x]"+linkparam+" != '' && [x]"+linkparam2+" != '') ");
					sb.append(" and " + (tablealias != null && tablealias.length() > 0 ? tablealias+".":"")  + col + " " + type + " " + ("string".equals(valuetype)?"'":"") + "[x]"+linkparam +("string".equals(valuetype)?"'":"") + " and " + ("string".equals(valuetype)?"'":"")+ "[x]"+linkparam2 + ("string".equals(valuetype)?"'":"") + " #end");
				}
			}else if(type.equals("in")){
				if(usetype.equals("gdz")){
					sb.append(" and " + (tablealias != null && tablealias.length() > 0 ? tablealias+".":"") + col + " in (" + val + ")");
				}else{
					sb.append("#if([x]"+linkparam+" != '') ");
					sb.append(" and " + (tablealias != null && tablealias.length() > 0 ? tablealias+".":"") + col + " in (" + "[x]"+linkparam + ")");
					sb.append("  #end");
				}
			}else{
				if(usetype.equals("gdz")){
					sb.append(" and " + (tablealias != null && tablealias.length() > 0 ? tablealias+".":"") + col + " " + type + " " + val);
				}else{
					sb.append("#if([x]"+linkparam+" != '') ");
					sb.append(" and " + (tablealias != null && tablealias.length() > 0 ? tablealias+".":"") + col + " "+type+" " + ("string".equals(valuetype) ? "'"+("like".equals(type)?"%":"")+""+"[x]"+linkparam+""+("like".equals(type)?"%":"")+"'":"[x]"+linkparam) + "");
					sb.append("  #end");
				}
			}
		}
		return sb.toString().replaceAll("\\[x\\]", "\\$");
	}
	
	/**
	 * 通过crossReport创建数据中心
	 * @param cube
	 * @param dataset
	 * @param dsourceId
	 * @return
	 * @throws IOException
	 */
	public GridDataCenterContext createDataCenter(JSONObject tableJson, JSONArray kpiJson, JSONObject dataset, String dsourceId, TableSqlJsonVO sqlVO, JSONObject linkAccept) throws IOException{
		GridDataCenterContext ctx = new GridDataCenterContextImpl();
		GridSetConfContext conf = new GridSetConfContext();
		conf.setRefDsource(dsourceId);
		ctx.setConf(conf);
		ctx.setId("DC-" + IdCreater.create());
		String sql = dataService.createDatasetSql(dataset);
		String name = TemplateManager.getInstance().createTemplate(sql);
		ctx.getConf().setTemplateName(name);
		
		//判断是否需要事件中的参数过滤
		if(linkAccept != null && !linkAccept.isNullObject() && !linkAccept.isEmpty()){
			GridFilterContext filter = new GridFilterContext();
			filter.setColumn(linkAccept.getString("col"));
			filter.setValue("${"+linkAccept.getString("col")+"}");
			filter.setFilterType(GridFilter.equal);
			ctx.getProcess().add(filter);
		}
		
		//先聚合数据
		GridAggregationContext agg = new GridAggregationContext();
		//设置聚合维
		JSONArray cols = tableJson.getJSONArray("cols");
		JSONArray rows = tableJson.getJSONArray("rows");
		List<String> dim = new ArrayList<String>();
		for(int i=0; i<cols.size(); i++){
			JSONObject d = cols.getJSONObject(i);
			String tp = d.getString("type");
			if(!"kpiOther".equalsIgnoreCase(tp)){
				String id = d.getString("colname");
				String txt = (String)d.get("colnamedesc");
				dim.add(id);
				if(txt != null && txt.length() > 0 && !dim.contains(id)){
					dim.add(txt);
				}
			}
		}
		for(int i=0; i<rows.size(); i++){
			JSONObject d = rows.getJSONObject(i);
			String tp = d.getString("type");
			if(!"kpiOther".equalsIgnoreCase(tp)){
				String id = d.getString("colname");
				String txt = (String)d.get("colnamedesc");
				dim.add(id);
				if(txt != null && txt.length() > 0 && !dim.contains(id)){
					dim.add(txt);
				}
			}
		}
		String[] dimArray = new String[dim.size()];
		for(int i=0; i<dimArray.length; i++){
			dimArray[i] = dim.get(i);
		}
		agg.setColumn(dimArray);
		
		//设置指标
		JSONArray kpis = kpiJson;
		AggreVO[] ks = new AggreVO[kpis.size()];
		for(int i=0; i<kpis.size(); i++){
			AggreVO vo = new AggreVO();
			JSONObject kpi = kpis.getJSONObject(i);
			String id = kpi.getString("colname");
			String aggre = (String)kpi.get("aggre");
			Object calc = kpi.get("calc");
			if(calc != null && kpi.getInt("calc") == 1){
				vo.setExpression(true);   //计算指标
				vo.setAlias(kpi.getString("kpi_id"));
			}
			vo.setName(id);
			vo.setType(aggre);
			ks[i] = vo;
		}
		agg.setAggreVO(ks);
		ctx.getProcess().add(agg);
	
		//处理指标排序
		boolean isSort = false;
		for(KpiInfo kpi : sqlVO.getKpis()){
			String kpiSort = kpi.getSort();
			if(kpiSort != null && kpiSort.length() > 0){
				GridSortContext ksort = new GridSortContext();
				ksort.setColumn(kpi.getColName());
				ksort.setType(kpiSort);
				ksort.setChangeOldOrder(true);
				ctx.getProcess().add(ksort);
				isSort = true;
			}
		}
		//指标排序优先于维度排序
		if(isSort == false){
			//处理排序
			List<DimInfo> dims = sqlVO.getDims();
			for(DimInfo d : dims){
				String ord = d.getDimOrd();
				if(ord != null && ord.length() > 0){
					GridSortContext sort = new GridSortContext();
					sort.setColumn(d.getColName());
					sort.setType(ord);
					sort.setChangeOldOrder(true);
					ctx.getProcess().add(sort);
				}
			}
		}
		
		return ctx;
	}
	
	/**
	 * 处理交叉表表头
	 */
	public void parserHead(JSONArray head, CrossReportContext ctx, boolean xxbt){
		if(xxbt){
			CrossReportContext parent =	ctx;
			if(parent.getRowHeads() == null){
				parent.setRowHeads(new ArrayList<RowHeadContext>());
			}
			RowHeadContext row = new RowHeadContext();
			parent.getRowHeads().add(row);
			return;
		}
		if(head == null || head.isEmpty()){
			return;
		}
		CrossReportContext parent =	ctx;
		if(parent.getRowHeads() == null){
			parent.setRowHeads(new ArrayList<RowHeadContext>());
		}
		for(int i=0; i<head.size(); i++){
			JSONObject obj = head.getJSONObject(i);
			String desc = obj.getString("desc");
			JSONObject style = (JSONObject)obj.get("style");
			String styleClass = null;
			RowHeadContext row = new RowHeadContext();
			if(style != null && !style.isNullObject() && !style.isEmpty()){
				String id = "H"+IdCreater.create();
				this.css.append("#"+ctx.getId()+" ." + id+"{");
				TableService.parserStyle(style, this.css);
				this.css.append("}");
				styleClass = id;
				row.setWidth((String)style.get("width"));
			}
			row.setDesc(desc);
			row.setStyleClass(styleClass);
			parent.getRowHeads().add(row);
		}
	}
	
	public CrossReportContext json2Table(JSONObject json, TableSqlJsonVO sqlVO) throws ParseException{
		JSONObject tableJson = json.getJSONObject("tableJson");
		//JSONArray kpiJson = json.getJSONArray("kpiJson");
		JSONArray head = (JSONArray)json.get("head");  //交叉表头
		JSONArray kpiHead = (JSONArray)json.get("kpiHead");
		String from = (String)json.get("from");
		
		CrossReportContext ctx = new CrossReportContextImpl();
		String lock = (String)json.get("lockhead");
		if("true".equals(lock)){
			ctx.setOut("lockUI");
		}else{
			ctx.setOut("HTML");
		}
		String rid = (String)json.get("compid");
		if(rid == null || rid.length() == 0){
			rid = "R"+IdCreater.create();
		}
		ctx.setId(rid);
		String width = (String)json.get("compwidth");
		String height = (String)json.get("compheight");
		ctx.setWidth(width);
		ctx.setHeight(height);
		
		CrossCols cols = new CrossCols();
		cols.setCols(new ArrayList<CrossField>());
		ctx.setCrossCols(cols);
		
		CrossRows rows = new CrossRows();
		rows.setRows(new ArrayList<CrossField>());
		ctx.setCrossRows(rows);
		
		//处理交叉表头
		JSONObject lineHead = (JSONObject)json.get("lineHead");  //判断是否生成斜线表头
		this.parserHead(head, ctx, lineHead == null || lineHead.isNullObject() || lineHead.isEmpty() ? false : true);
		
		//判断是否有事件
		JSONObject linkAccept = (JSONObject)json.get("linkAccept");
		if(linkAccept != null && !linkAccept.isNullObject() && !linkAccept.isEmpty()){
			ctx.setLabel(json.getString("id"));
		}
		boolean uselink = false;
		JSONObject link = (JSONObject)json.get("link");
		if(link != null && !link.isNullObject() && !link.isEmpty()){
			RowLinkContext rlink = new RowLinkContext();
			String target = (String)link.get("target");
			String type = (String)link.get("type");
			rlink.setTarget(target.split(","));
			rlink.setType(type.split(","));
			ctx.getCrossRows().setLink(rlink);
			uselink = true;
		}
		
		//表格钻取维度
		List<RowDimContext> drill = this.getDrillDim(json);
		if(drill != null && drill.size() > 0){
			ctx.setDims(drill);
			uselink = true;
		}
		
		JSONArray colsStr = tableJson.getJSONArray("cols");
		JSONArray rowsStr = tableJson.getJSONArray("rows");
		
		if(colsStr.size() == 0 || sqlVO.getKpis().size() > 1){
			JSONObject obj = new JSONObject();
			obj.put("type", "kpiOther");
			obj.put("id", "kpi");
			colsStr.add(obj);
		}else{
			//如果只有一个指标，并且具有列维度，放入baseKpi
			KpiInfo kpi = sqlVO.getKpis().get(0);
			CrossKpi baseKpi = new BaseKpiField();
			baseKpi.setAggregation(kpi.getAggre());
			baseKpi.setAlias(kpi.getAlias());
			baseKpi.setFormatPattern(kpi.getFmt());
			//处理这一个指标的样式(dataClass)
			JSONObject style = kpi.getStyle();
			if(style != null && !style.isNullObject() && !style.isEmpty()){
				String id = "S"+IdCreater.create();
				this.css.append("#"+ctx.getId()+" ." + id+"{");
				TableService.parserStyle(style, this.css);
				this.css.append("}");
				baseKpi.setDataClass(id);
			}
			ctx.setBaseKpi(baseKpi);
		}
		
		loopJsonField(colsStr, head, kpiHead, cols.getCols(), "col", sqlVO, from, ctx, false);
		loopJsonField(rowsStr, head, kpiHead, rows.getRows(), "row", sqlVO, from, ctx, uselink);
		
		//如果没有行维度，添加none维度
		if(rows.getRows().size() == 0){
			CrossField cf = new CrossField();
			cf.setType("none");
			cf.setDesc("合计");
			rows.getRows().add(cf);
		}
		
		return ctx;
	}
	
	public List<RowDimContext> getDrillDim(JSONObject json){
		JSONArray drillDim = (JSONArray)json.get("drillDim");
		if(drillDim == null || drillDim.isEmpty()){
			return null;
		}
		List<RowDimContext> ret = new ArrayList<RowDimContext>();
		for(int i=0; i<drillDim.size(); i++){
			JSONObject obj = drillDim.getJSONObject(i);
			RowDimContext dim = new RowDimContext();
			dim.setCode(obj.getString("code"));
			dim.setName(obj.getString("name"));
			dim.setCodeDesc(dim.getCode()+"_desc");
			dim.setType("frd");
			ret.add(dim);
		}
		return ret;
	}
	
	public static String createWarning(JSONObject warn, String kpiFmt, StringBuffer scripts ){
		String funcName = "warn"+IdCreater.create();
		scripts.append("function " +funcName+"(val, a, b, c, d){");
		//先输出值
		scripts.append("out.print(val, '"+kpiFmt+"');");
		scripts.append("if(d != 'html'){"); //只在html模式下起作用
		scripts.append(" return;");
		scripts.append("}");
		scripts.append("if(val "+warn.getString("logic1")+" "+warn.getString("val1")+"){");
		scripts.append("out.print(\"<span class='"+warn.getString("pic1")+"'></span>\")");
		scripts.append("}else if(val "+(warn.getString("logic1").equals(">=")?"<":"<=")+" "+warn.getString("val1")+" && val "+warn.getString("logic2")+" "+warn.getString("val2")+"){");
		scripts.append("out.print(\"<span class='"+warn.getString("pic2")+"'></span>\")");
		scripts.append("}else{");
		scripts.append("out.print(\"<span class='"+warn.getString("pic3")+"'></span>\")");
		scripts.append("}");
		scripts.append("}");
		return funcName; 
	}
	
	private void loopJsonField(JSONArray arrays, JSONArray crsHead, JSONArray kpiHead, List<CrossField> ls, String income, TableSqlJsonVO sqlVO, String from, CrossReportContext ctx, boolean uselink) throws ParseException{
		List<CrossField> tmp = ls;
		for(int i=0; i<arrays.size(); i++){
			JSONObject obj = arrays.getJSONObject(i);
			String type = obj.getString("type");
			String issum = (String)obj.get("issum");
			String casparent = (String)obj.get("iscas");
			
			if(type.equals("kpiOther")){
				List<CrossField> newCf = new ArrayList<CrossField>();
				if(tmp.size() == 0){
					List<KpiInfo> kpis = sqlVO.getKpis();
					for(int j=0; j<kpis.size(); j++){
						KpiInfo kpi = kpis.get(j);
						CrossField cf = new CrossField();
						cf.setType(type);
						if(kpiHead != null){
							String name = kpiHead.getJSONObject(j).getString("desc");
							cf.setDesc(name);
						}else{
							cf.setDesc(kpi.getKpiName());
						}
						cf.setAggregation(kpi.getAggre());
						cf.setAlias(kpi.getAlias());
						cf.setFormatPattern(kpi.getFmt());
						cf.setSubs(new ArrayList<CrossField>());
						//cf.setStyleToLine(true);
						//用 size来表示指标ID，用在OLAP中
						//cf.setId(kpi.getId().toString());
						if(kpi.getRate() != null){
							cf.setKpiRate(new BigDecimal(kpi.getRate()));
						}
						//当回调函数和指标预警同时起作用时， 指标预警起作用
						cf.setJsFunc(kpi.getFuncname());
						String code = kpi.getCode();
						if(code != null && code.length() > 0){
							try {
								code = URLDecoder.decode(code, "UTF-8");
							} catch (UnsupportedEncodingException e) {
								e.printStackTrace();
							}
							this.scripts.append("function "+cf.getJsFunc()+"(value,col,row,data){"+code+"}");
						}
						JSONObject warn = kpi.getWarn();
						if(warn != null && !warn.isEmpty()){
							String name = createWarning(warn, kpi.getFmt(), this.scripts);
							cf.setJsFunc(name);
						}
						
						if(kpiHead != null){
							JSONObject head = kpiHead.getJSONObject(j);
							JSONObject style = (JSONObject)head.get("style");
							if(style != null && !style.isNullObject() && !style.isEmpty()){
								String id = "S"+IdCreater.create();
								this.css.append("#"+ctx.getId()+" ." + id+"{");
								TableService.parserStyle(style, this.css);
								this.css.append("}");
								cf.setStyleClass(id);
								cf.setWidth((String)style.get("width")); //提取设置的表头宽度
							}
							//根据head判断是否启用客户端排序
							String kpiOrder = (String)head.get("kpiOrder");
							if(kpiOrder != null && kpiOrder.length() > 0){
								cf.setOrder("true".equalsIgnoreCase(kpiOrder));
							}
						}
						//处理dataClass, 数据的样式
						JSONObject style = kpi.getStyle();
						if(style != null && !style.isNullObject() && !style.isEmpty()){
							String id = "S"+IdCreater.create();
							this.css.append("#"+ctx.getId()+" ." + id+"{");
							TableService.parserStyle(style, this.css);
							this.css.append("}");
							cf.setDataClass(id);
						}
						tmp.add(cf);
						newCf.add(cf);
					}
				}else{
					for(CrossField tp : tmp){
						List<KpiInfo> kpis = sqlVO.getKpis();
						for(int j=0; j<kpis.size(); j++){
							KpiInfo kpi = kpis.get(j);
							CrossField cf = new CrossField();
							cf.setType(type);
							if(kpiHead != null){
								String name = kpiHead.getJSONObject(j).getString("desc");
								cf.setDesc(name);
							}else{
								cf.setDesc(kpi.getKpiName());
							}
							cf.setAggregation(kpi.getAggre());
							cf.setAlias(kpi.getAlias());
							cf.setFormatPattern(kpi.getFmt());
							cf.setSubs(new ArrayList<CrossField>());
							//cf.setStyleToLine(true);
							//用 size来表示指标ID，用在OLAP中
							//cf.setId(kpi.getId().toString());
							if(kpi.getRate() != null){
								cf.setKpiRate(new BigDecimal(kpi.getRate()));
							}
							//当回调函数和指标预警同时起作用时， 指标预警起作用
							cf.setJsFunc(kpi.getFuncname());
							String code = kpi.getCode();
							if(code != null && code.length() > 0){
								try {
									code = URLDecoder.decode(code, "UTF-8");
								} catch (UnsupportedEncodingException e) {
									e.printStackTrace();
								}
								this.scripts.append("function "+cf.getJsFunc()+"(value,col,row,data){"+code+"}");
							}
							JSONObject warn = kpi.getWarn();
							if(warn != null && !warn.isEmpty()){
								String name = createWarning(warn, kpi.getFmt(), this.scripts);
								cf.setJsFunc(name);
							}
							if(kpiHead != null){
								JSONObject head = kpiHead.getJSONObject(j);
								JSONObject style = (JSONObject)head.get("style");
								if(style != null && !style.isNullObject() && !style.isEmpty()){
									String id = "S"+IdCreater.create();
									this.css.append("#"+ctx.getId()+" ." + id+"{");
									TableService.parserStyle(style, this.css);
									this.css.append("}");
									cf.setStyleClass(id);
									cf.setWidth((String)style.get("width")); //提取设置的表头宽度
								}
								//根据head判断是否启用客户端排序
								String kpiOrder = (String)head.get("kpiOrder");
								if(kpiOrder != null && kpiOrder.length() > 0){
									cf.setOrder("true".equalsIgnoreCase(kpiOrder));
								}
							}
							//处理dataClass, 数据的样式
							JSONObject style = kpi.getStyle();
							if(style != null && !style.isNullObject() && !style.isEmpty()){
								String id = "S"+IdCreater.create();
								this.css.append("#"+ctx.getId()+" ." + id+"{");
								TableService.parserStyle(style, this.css);
								this.css.append("}");
								cf.setDataClass(id);
							}
							tp.getSubs().add(cf);
							newCf.add(cf);
						}
					}
				}
				tmp = newCf;
				
				
			}else if("day".equals(type)){
				List<CrossField> newCf = new ArrayList<CrossField>();
				
				if(tmp.size() == 0){
					CrossField cf = new CrossField();
					/**
					cf.setType(type);
					if(sqlVO.getDayColumn() != null && sqlVO.getDayColumn().getStartDay() != null && sqlVO.getDayColumn().getStartDay().length() > 0){
						cf.setStart(sqlVO.getDayColumn().getEndDay());
						cf.setSize(sqlVO.getDayColumn().getBetweenDay() + 1);
					}else{
						int size = 365;
						cf.setStart("${s.defDay}");
						cf.setSize(size);
					}
					**/
					if("y".equals(casparent)){
						cf.setCasParent(true);
					}
					cf.setId(obj.get("id").toString());
					cf.setType("frd");
					cf.setDateType("day");
					cf.setValue((String)obj.get("vals"));
					cf.setUselink(uselink);
					cf.setMulti(true);
					cf.setShowWeek(false);
					cf.setDesc(obj.getString("dimdesc"));
					String alias = obj.getString("colname");
					cf.setAlias(alias);
					String top = (String)obj.get("top");
					cf.setTop(top == null || top.length() == 0 ? null : new Integer(top));
					cf.setAliasDesc(alias);
					cf.setSubs(new ArrayList<CrossField>());
					tmp.add(cf);
					newCf.add(cf);
					
					//控制样式
					JSONObject style = (JSONObject)obj.get("style");
					if(style != null && !style.isNullObject() && !style.isEmpty()){
						String id = "S"+IdCreater.create();
						this.css.append("#"+ctx.getId()+" ." + id+"{");
						TableService.parserStyle(style, this.css);
						this.css.append("}");
						cf.setStyleClass(id);
					}
					//根据交叉表头设置的宽度来确定单元格宽度
					if("row".equals(income)){
						JSONObject jcStyle = (JSONObject)crsHead.getJSONObject(i).get("style");
						if(jcStyle != null){
							cf.setWidth((String)jcStyle.get("width")); 
						}
					}
					//添加合计项
					if("y".equals(issum)){
						CrossField sumcf = new CrossField();
						sumcf.setType("none");
						sumcf.setDimAggre((String)obj.get("aggre"));
						sumcf.setDesc(MyCrossFieldLoader.loadFieldName(sumcf.getDimAggre()));
						sumcf.setSubs(new ArrayList<CrossField>());		
						tmp.add(sumcf);
						newCf.add(sumcf);
					}
					
				}else{
					for(CrossField tp : tmp){
						//如果上级是合计，下级不包含维度了
						if(tp.getType().equals("none")){
							continue;
						}
						CrossField cf = new CrossField();
						/**
						cf.setType(type);
						if(sqlVO.getDayColumn() != null && sqlVO.getDayColumn().getStartDay() != null && sqlVO.getDayColumn().getStartDay().length() > 0){
							cf.setStart(sqlVO.getDayColumn().getEndDay());
							cf.setSize(sqlVO.getDayColumn().getBetweenDay() + 1);
						}else{
							int size = 365;
							cf.setStart("${s.defDay}");
							cf.setSize(size);
						}**/
						if("y".equals(casparent)){
							cf.setCasParent(true);
						}
						cf.setId(obj.get("id").toString());
						cf.setType("frd");
						cf.setDateType("day");
						cf.setValue((String)obj.get("vals"));
						cf.setMulti(true);
						String top = (String)obj.get("top");
						cf.setTop(top == null || top.length() == 0 ? null : new Integer(top));
						cf.setShowWeek(false);
						cf.setUselink(uselink);
						cf.setDesc(obj.getString("dimdesc"));
						String alias = obj.getString("colname");
						cf.setAlias(alias);
						cf.setAliasDesc(alias);
						cf.setSubs(new ArrayList<CrossField>());
						cf.setParent(tp);
						
						tp.getSubs().add(cf);
						newCf.add(cf);
						
						//控制样式
						JSONObject style = (JSONObject)obj.get("style");
						if(style != null && !style.isNullObject() && !style.isEmpty()){
							String id = "S"+IdCreater.create();
							this.css.append("#"+ctx.getId()+" ." + id+"{");
							TableService.parserStyle(style, this.css);
							this.css.append("}");
							cf.setStyleClass(id);
						}
						//根据交叉表头设置的宽度来确定单元格宽度
						if("row".equals(income)){
							JSONObject jcStyle = (JSONObject)crsHead.getJSONObject(i).get("style");
							if(jcStyle != null){
								cf.setWidth((String)jcStyle.get("width")); 
							}
						}
						//添加合计项
						if("y".equals(issum)){
							CrossField sumcf = new CrossField();
							sumcf.setType("none");
							sumcf.setDimAggre((String)obj.get("aggre"));
							sumcf.setDesc(MyCrossFieldLoader.loadFieldName(sumcf.getDimAggre()));
							sumcf.setSubs(new ArrayList<CrossField>());		
							tp.getSubs().add(sumcf);
							newCf.add(sumcf);
						}
					}
				}
				tmp = newCf;
				
			}else if("month".equals(type)){
				List<CrossField> newCf = new ArrayList<CrossField>();
				if(tmp.size() == 0){
					CrossField cf = new CrossField();
					/**
					cf.setType(type);
					if(sqlVO.getMonthColumn()!= null && sqlVO.getMonthColumn().getStartMonth() != null && sqlVO.getMonthColumn().getStartMonth().length() > 0){
						cf.setStart(sqlVO.getMonthColumn().getEndMonth());
						cf.setSize(sqlVO.getMonthColumn().getBetweenMonth() + 1);
					}else{
						int size = 12;
						cf.setStart("${s.defMonth}");
						cf.setSize(size);
					}
					**/
					if("y".equals(casparent)){
						cf.setCasParent(true);
					}
					cf.setId(obj.get("id").toString());
					cf.setType("frd");
					cf.setDateType("month");
					cf.setValue((String)obj.get("vals"));
					cf.setMulti(true);
					cf.setDesc(obj.getString("dimdesc"));
					String alias = obj.getString("colname");
					cf.setAlias(alias);
					cf.setAliasDesc(alias);
					cf.setUselink(uselink);
					String top = (String)obj.get("top");
					cf.setTop(top == null || top.length() == 0 ? null : new Integer(top));
					cf.setSubs(new ArrayList<CrossField>());
					tmp.add(cf);
					newCf.add(cf);
					
					//控制样式
					JSONObject style = (JSONObject)obj.get("style");
					if(style != null && !style.isNullObject() && !style.isEmpty()){
						String id = "S"+IdCreater.create();
						this.css.append("#"+ctx.getId()+" ." + id+"{");
						TableService.parserStyle(style, this.css);
						this.css.append("}");
						cf.setStyleClass(id);
					}
					//根据交叉表头设置的宽度来确定单元格宽度
					if("row".equals(income)){
						JSONObject jcStyle = (JSONObject)crsHead.getJSONObject(i).get("style");
						if(jcStyle != null){
							cf.setWidth((String)jcStyle.get("width")); 
						}
					}
					//添加合计项
					if("y".equals(issum)){
						CrossField sumcf = new CrossField();
						sumcf.setType("none");
						sumcf.setDimAggre((String)obj.get("aggre"));
						sumcf.setDesc(MyCrossFieldLoader.loadFieldName(sumcf.getDimAggre()));
						sumcf.setSubs(new ArrayList<CrossField>());		
						tmp.add(sumcf);
						newCf.add(sumcf);
					}
					
				}else{
					for(CrossField tp : tmp){
						//如果上级是合计，下级不包含维度了
						if(tp.getType().equals("none")){
							continue;
						}
						CrossField cf = new CrossField();
						/**
						cf.setType(type);
						if(sqlVO.getMonthColumn()!= null && sqlVO.getMonthColumn().getStartMonth() != null && sqlVO.getMonthColumn().getStartMonth().length() > 0){
							cf.setStart(sqlVO.getMonthColumn().getEndMonth());
							cf.setSize(sqlVO.getMonthColumn().getBetweenMonth() + 1);
						}else{
							int size = 12;
							cf.setStart("${s.defMonth}");
							cf.setSize(size);
						}
						**/
						if("y".equals(casparent)){
							cf.setCasParent(true);
						}
						cf.setId(obj.get("id").toString());
						cf.setType("frd");
						cf.setDateType("month");
						cf.setValue((String)obj.get("vals"));
						cf.setMulti(true);
						cf.setUselink(uselink);
						String top = (String)obj.get("top");
						cf.setTop(top == null || top.length() == 0 ? null : new Integer(top));
						cf.setDesc(obj.getString("dimdesc"));
						String alias = obj.getString("colname");
						cf.setAlias(alias);
						cf.setAliasDesc(alias);
						cf.setSubs(new ArrayList<CrossField>());
						cf.setParent(tp);
						
						//控制样式
						JSONObject style = (JSONObject)obj.get("style");
						if(style != null && !style.isNullObject() && !style.isEmpty()){
							String id = "S"+IdCreater.create();
							this.css.append("#"+ctx.getId()+" ." + id+"{");
							TableService.parserStyle(style, this.css);
							this.css.append("}");
							cf.setStyleClass(id);
						}
						//根据交叉表头设置的宽度来确定单元格宽度
						if("row".equals(income)){
							JSONObject jcStyle = (JSONObject)crsHead.getJSONObject(i).get("style");
							if(jcStyle != null){
								cf.setWidth((String)jcStyle.get("width")); 
							}
						}
						
						tp.getSubs().add(cf);
						newCf.add(cf);
						
						//添加合计项
						if("y".equals(issum)){
							CrossField sumcf = new CrossField();
							sumcf.setType("none");
							sumcf.setDimAggre((String)obj.get("aggre"));
							sumcf.setDesc(MyCrossFieldLoader.loadFieldName(sumcf.getDimAggre()));
							sumcf.setSubs(new ArrayList<CrossField>());		
							tp.getSubs().add(sumcf);
							newCf.add(sumcf);
						}
					}
				}
				tmp = newCf;
			}else{
				List<CrossField> newCf = new ArrayList<CrossField>();
				if(tmp.size() == 0){
					CrossField cf = new CrossField();
					cf.setType("frd");
					cf.setId(obj.get("id").toString());
					cf.setDesc(obj.getString("dimdesc"));
					if("olap".equals(from)){ //如果是通过olap数据集访问，启用id + 别名作为 alias
						cf.setAlias(obj.getString("id"));
						cf.setAliasDesc(cf.getAlias()+"_desc");
					}else{
						cf.setAlias(obj.getString("colname"));
						cf.setAliasDesc((String)obj.get("colnamedesc"));
					}
					if("y".equals(casparent)){
						cf.setCasParent(true);
					}
					cf.setUselink(uselink);
					cf.setValue((String)obj.get("vals"));
					cf.setMulti(true);
					String top = (String)obj.get("top");
					cf.setTop(top == null || top.length() == 0 ? null : new Integer(top));
					cf.setSubs(new ArrayList<CrossField>());			
					tmp.add(cf);
					newCf.add(cf);
					//控制样式
					JSONObject style = (JSONObject)obj.get("style");
					if(style != null && !style.isNullObject() && !style.isEmpty()){
						String id = "S"+IdCreater.create();
						this.css.append("#"+ctx.getId()+" ." + id+"{");
						TableService.parserStyle(style, this.css);
						this.css.append("}");
						cf.setStyleClass(id);
					}
					//根据交叉表头设置的宽度来确定单元格宽度
					if("row".equals(income)){
						JSONObject jcStyle = (JSONObject)crsHead.getJSONObject(i).get("style");
						if(jcStyle != null){
							cf.setWidth((String)jcStyle.get("width")); 
						}
					}
					//添加合计项
					if("y".equals(issum)){
						CrossField sumcf = new CrossField();
						sumcf.setType("none");
						sumcf.setDimAggre((String)obj.get("aggre"));
						sumcf.setDesc(MyCrossFieldLoader.loadFieldName(sumcf.getDimAggre()));
						sumcf.setSubs(new ArrayList<CrossField>());		
						tmp.add(sumcf);
						newCf.add(sumcf);
					}
					
				}else{
					for(CrossField tp : tmp){
						//如果上级是合计，下级不包含维度了
						if(tp.getType().equals("none")){
							continue;
						}
						CrossField cf = new CrossField();
						cf.setType("frd");
						cf.setId(obj.get("id").toString());
						cf.setDesc(obj.getString("dimdesc"));
						if("olap".equals(from)){ //如果是通过olap数据集访问，启用id + 别名作为 alias
							cf.setAlias(obj.getString("id"));
							cf.setAliasDesc(cf.getAlias()+"_desc");
						}else{
							String alias = obj.getString("colname");
							cf.setAlias(alias);
							cf.setAliasDesc((String)obj.get("colnamedesc"));
						}
						if("y".equals(casparent)){
							cf.setCasParent(true);
						}
						cf.setValue((String)obj.get("vals"));
						cf.setMulti(true);
						String top = (String)obj.get("top");
						cf.setTop(top == null || top.length() == 0 ? null : new Integer(top));
						cf.setSubs(new ArrayList<CrossField>());
						cf.setParent(tp);
						cf.setUselink(uselink);
						//控制样式
						JSONObject style = (JSONObject)obj.get("style");
						if(style != null && !style.isNullObject() && !style.isEmpty()){
							String id = "S"+IdCreater.create();
							this.css.append("#"+ctx.getId()+" ." + id+"{");
							TableService.parserStyle(style, this.css);
							this.css.append("}");
							cf.setStyleClass(id);
						}
						//根据交叉表头设置的宽度来确定单元格宽度
						if("row".equals(income)){
							JSONObject jcStyle = (JSONObject)crsHead.getJSONObject(i).get("style");
							if(jcStyle != null){
								cf.setWidth((String)jcStyle.get("width")); 
							}
						}
						
						tp.getSubs().add(cf);
						newCf.add(cf);
						
						//添加合计项
						if("y".equals(issum)){
							CrossField sumcf = new CrossField();
							sumcf.setType("none");
							sumcf.setDimAggre((String)obj.get("aggre"));
							sumcf.setDesc(MyCrossFieldLoader.loadFieldName(sumcf.getDimAggre()));
							sumcf.setSubs(new ArrayList<CrossField>());		
							tp.getSubs().add(sumcf);
							newCf.add(sumcf);
						}
					}
				}
				tmp = newCf;
			}
			
		}
	}	

}
