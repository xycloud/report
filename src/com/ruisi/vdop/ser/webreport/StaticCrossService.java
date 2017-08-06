package com.ruisi.vdop.ser.webreport;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.ruisi.ext.engine.init.TemplateManager;
import com.ruisi.ext.engine.util.IdCreater;
import com.ruisi.ext.engine.view.context.cross.BaseKpiField;
import com.ruisi.ext.engine.view.context.cross.CrossCols;
import com.ruisi.ext.engine.view.context.cross.CrossField;
import com.ruisi.ext.engine.view.context.cross.CrossFieldOther;
import com.ruisi.ext.engine.view.context.cross.CrossKpi;
import com.ruisi.ext.engine.view.context.cross.CrossReportContext;
import com.ruisi.ext.engine.view.context.cross.CrossReportContextImpl;
import com.ruisi.ext.engine.view.context.cross.CrossRows;
import com.ruisi.ext.engine.view.context.cross.RowHeadContext;
import com.ruisi.ext.engine.view.context.dc.grid.AggreVO;
import com.ruisi.ext.engine.view.context.dc.grid.GridAggregationContext;
import com.ruisi.ext.engine.view.context.dc.grid.GridDataCenterContext;
import com.ruisi.ext.engine.view.context.dc.grid.GridDataCenterContextImpl;
import com.ruisi.ext.engine.view.context.dc.grid.GridFilterContext;
import com.ruisi.ext.engine.view.context.dc.grid.GridSetConfContext;
import com.ruisi.ext.engine.view.context.dc.grid.GridSortContext;
import com.ruisi.ispire.dc.grid.GridFilter;
import com.ruisi.vdop.ser.bireport.TableSqlJsonVO;
import com.ruisi.vdop.ser.bireport.TableSqlJsonVO.DimInfo;
import com.ruisi.vdop.ser.bireport.TableSqlJsonVO.KpiInfo;
import com.ruisi.vdop.util.VDOPUtils;

/**
 * 固定交叉表解析类
 * @author hq
 * @date 2014-9-11
 */
public class StaticCrossService {
	
	private StringBuffer css;
	private StringBuffer scripts;
	
	private DataService dataService = new DataService();
	
	public StaticCrossService(StringBuffer css, StringBuffer scripts){
		this.css = css;
		this.scripts = scripts;
	}
	
	public TableSqlJsonVO json2TalbeSqlVO(JSONObject json){
		TableSqlJsonVO vo = new TableSqlJsonVO();
		JSONObject tableJson = json.getJSONObject("tableJson");
		JSONArray colDims = tableJson.getJSONArray("cols");
		JSONArray rowDims = tableJson.getJSONArray("rows");
		this.sqlVOLoopField(colDims, vo, "col");
		this.sqlVOLoopField(rowDims, vo, "row");
		return vo;
	}
	
	private void sqlVOLoopField(JSONArray nodes, TableSqlJsonVO vo, String pos){
		for(int i=0; i<nodes.size(); i++){
			JSONObject node = nodes.getJSONObject(i);
			String tp = node.getString("type");
			if("kpi".equals(tp)){  //指标
				KpiInfo kpi = new KpiInfo();
				kpi.setAggre(node.getString("aggre"));
				kpi.setAlias(node.getString("alias"));
				int calc = node.getInt("calc");
				kpi.setCalc(calc);
				if(calc == 1){
					kpi.setColName(node.getString("colname"));
				}else{
					kpi.setColName(node.getString("col"));
				}
				kpi.setFmt(node.getString("fmt"));
				kpi.setKpiName(node.getString("desc"));
				//kpi.setTid(node.getString("tid"));
				//kpi.setDescKey((String)kpij.get("descKey"));
				if(node.get("rate") != null && !node.get("rate").toString().equals("null")){
					kpi.setRate(node.getInt("rate"));
				}
				//kpi.setId(node.getInt("id"));
				//kpi.setSort((String)kpij.get("sort"));
				kpi.setUnit((String)node.get("unit"));
				//kpi.setCompute((String)kpij.get("compute"));
				
				vo.getKpis().add(kpi);
			}else if("none".equals(tp)){  //none
				
			}else{  //维度
				if(vo.getDimByCol((String)node.get("col")) == null){
					DimInfo dim = new DimInfo();
					dim.setId(node.getString("id"));
					dim.setColName((String)node.get("col"));
					dim.setTableName(node.get("tname") == null ? null : node.get("tname").toString());
					dim.setTableColKey((String)node.get("colname"));
					dim.setTableColName((String)node.get("colnamedesc"));
					dim.setDimOrd((String)node.get("dimord"));
					dim.setDimpos(pos);
					dim.setType(tp);
					vo.getDims().add(dim);
				}
			}
			//处理维度限制条件
			JSONArray others = (JSONArray)node.get("others");
			if(others != null && others.size() > 0){
				for(int j=0; j<others.size(); j++){
					JSONObject other = others.getJSONObject(j);
					String col = other.getString("col");
					if(vo.getDimByCol(col) == null){
						DimInfo dim = new DimInfo();
						dim.setId(other.getString("id"));
						dim.setColName(col);
						dim.setTableName(other.get("tname") == null ? null : other.get("tname").toString());
						dim.setTableColKey((String)other.get("colname"));
						dim.setTableColName((String)other.get("colnamedesc"));
						dim.setDimOrd(null);
						dim.setDimpos(pos);
						dim.setType(tp);
						vo.getDims().add(dim);
					}
				}
			}
			JSONArray children = (JSONArray)node.get("children");
			if(children != null && children.size() > 0){
				this.sqlVOLoopField(children, vo, pos);
			}
		}
	}
	
	public GridDataCenterContext createDataCenter(JSONObject dataset, String dsourceId, TableSqlJsonVO sqlVO) throws IOException{
		GridDataCenterContext ctx = new GridDataCenterContextImpl();
		GridSetConfContext conf = new GridSetConfContext();
		conf.setRefDsource(dsourceId);
		ctx.setConf(conf);
		ctx.setId("DC-" + IdCreater.create());
		String sql = dataService.createDatasetSql(dataset);
		String name = TemplateManager.getInstance().createTemplate(sql);
		ctx.getConf().setTemplateName(name);
		
		//判断是否需要事件中的参数过滤
		/**
		if(linkAccept != null && !linkAccept.isNullObject() && !linkAccept.isEmpty()){
			GridFilterContext filter = new GridFilterContext();
			filter.setColumn(linkAccept.getString("col"));
			filter.setValue("${"+linkAccept.getString("col")+"}");
			filter.setFilterType(GridFilter.equal);
			ctx.getProcess().add(filter);
		}
		**/
		
		//先聚合数据
		GridAggregationContext agg = new GridAggregationContext();
		//设置聚合维
		List<String> dim = new ArrayList<String>();
		for(int i=0; i<sqlVO.getDims().size(); i++){
			DimInfo diminfo = sqlVO.getDims().get(i);
			String id = diminfo.getTableColKey();
			String txt = diminfo.getTableColName();
			dim.add(id);
			if(txt != null && txt.length() > 0){
				dim.add(txt);
			}
		}
		String[] dimArray = new String[dim.size()];
		for(int i=0; i<dimArray.length; i++){
			dimArray[i] = dim.get(i);
		}
		agg.setColumn(dimArray);
		
		//设置指标
		List<KpiInfo> kpis = sqlVO.getKpis();
		AggreVO[] ks = new AggreVO[kpis.size()];
		for(int i=0; i<kpis.size(); i++){
			KpiInfo kpi = kpis.get(i);
			String id = kpi.getAlias();
			String aggre = kpi.getAggre();
			AggreVO vo = new AggreVO();
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
					ctx.getProcess().add(sort);
				}
			}
		}
		
		return ctx;
	}
	
	public GridDataCenterContext createDataCenterByOlap(TableSqlJsonVO sqlVO, JSONArray params, String tname) throws IOException{
		GridDataCenterContext ctx = new GridDataCenterContextImpl();
		GridSetConfContext conf = new GridSetConfContext();
		ctx.setConf(conf);
		ctx.setId("DC-" + IdCreater.create());
		
		String name = TemplateManager.getInstance().createTemplate(this.createSql(sqlVO, params, tname));
		ctx.getConf().setTemplateName(name);
		
		return ctx;
	}
	
	public String createSql(TableSqlJsonVO sqlVO,  JSONArray params, String tname){
		StringBuffer sql = new StringBuffer();
		sql.append("select ");
		List<DimInfo> dims = sqlVO.getDims();
		for(int i=0; i<dims.size(); i++){
			DimInfo dim = dims.get(i);
			//对应type= frd/year/quarter 需要关联码表 
			if(dim.getType().equals("frd") || "year".equals(dim.getType()) || "quarter".equals(dim.getType())){
				sql.append(" a0." + dim.getColName()+", ");
				sql.append(dim.getId() + "."+dim.getTableColName()+" "+dim.getColName()+"_desc, ");
			}else{
				sql.append(" a0."+dim.getColName()+", ");
			}
		}
		
		List<KpiInfo> kpis = sqlVO.getKpis();
		if(kpis.size() == 0){
			sql.append(" null kpi_value ");
		}else{
			for(int i=0; i<kpis.size(); i++){
				KpiInfo kpi = kpis.get(i);
				if(kpi.getCalc() == 1){
					sql.append(kpi.getColName());
				}else{
					sql.append(kpi.getAggre()+"("+kpi.getColName()+")");
				}
				sql.append(" ");
				sql.append(kpi.getAlias());
				if(i != kpis.size() - 1){
					sql.append(",");
				}
			}
		}
		
		sql.append(" from "+tname+" a0 ");
		for(DimInfo dim : dims){
			if(dim.getType().equals("frd") || "year".equals(dim.getType()) || "quarter".equals(dim.getType())){
				sql.append("," + dim.getTableName() + " " + dim.getId());
			}
		}
		sql.append(" where 1=1 ");
		
		//boolean isDealDate = false;
		for(int i=0; i<dims.size(); i++){
			DimInfo dim = dims.get(i);
			if(dim.getType().equals("frd") || "year".equals(dim.getType()) || "quarter".equals(dim.getType())){
				sql.append(" and a0."+ dim.getColName() +" = "+dim.getId()+"." + dim.getTableColKey());
			}
		}
		
		//限制参数的查询条件
		if(params != null && !params.isEmpty()){
			sql.append(CrossReportService.dealCubeParams(params));
		}
		
		//处理事件接受的参数限制条件
		/**
		JSONObject linkAccept = sqlVO.getLinkAccept();
		if(linkAccept != null && !linkAccept.isNullObject() && !linkAccept.isEmpty()){
			String col = (String)linkAccept.get("col");
			String dftval = (String)linkAccept.get("dftval");
			String valtype = (String)linkAccept.get("valType");
			String ncol = "$" + col;
			if("string".equalsIgnoreCase(valtype)){
				dftval = "'" + dftval + "'";
			}
			sql.append(" and  " + col + " = " + ncol);
		}
		**/
		
		if(dims.size() > 0){
			sql.append(" group by ");
			for(int i=0; i<dims.size(); i++){
				DimInfo dim = dims.get(i);
				sql.append("a0."+dim.getColName());
				if(dim.getType().equals("frd") || "year".equals(dim.getType()) || "quarter".equals(dim.getType())){
					sql.append(",");
					sql.append(dim.getId()+"."+dim.getTableColName());
				}
				if(i != dims.size() - 1){
					sql.append(",");
				}
			}
		}
		
		if(dims.size() > 0){
			sql.append(" order by ");
			//先按col排序
			for(int i=0; i<dims.size(); i++){
				DimInfo dim = dims.get(i);
				if(!dim.getDimpos().equals("col")){
					continue;
				}
				if(dim.getDimOrd() != null && dim.getDimOrd().length() > 0){
					if("desc".equalsIgnoreCase(dim.getDimOrd())){
						sql.append("a0." + dim.getColName() + " " + dim.getDimOrd());
					}else{
						sql.append("a0." + dim.getColName());
					}
					sql.append(",");
				}
			}
			/**
			//判断是否按指标排序
			for(int i=0; i<kpis.size(); i++){
				KpiInfo kpi = kpis.get(i);
				if(kpi.getSort() != null && kpi.getSort().length() > 0){
					sql.append(kpi.getAlias() + " " + kpi.getSort());
					sql.append(",");
				}
			}
			**/
			
			//再按row排序
			for(int i=0; i<dims.size(); i++){
				DimInfo dim = dims.get(i);
				if(!dim.getDimpos().equals("row")){
					continue;
				}
				if(dim.getDimOrd() != null && dim.getDimOrd().length() > 0){
					if("desc".equalsIgnoreCase(dim.getDimOrd())){
						sql.append("a0." + dim.getColName() + " " + dim.getDimOrd());
					}else{
						sql.append("a0." + dim.getColName());
					}
					sql.append(",");
				}
			}
			//返回前先去除最后的逗号
			return sql.toString().substring(0, sql.length() - 1); 
		}else{
			return sql.toString();
		}
	}
	
	public void parserHead(JSONObject head, CrossReportContext ctx){
		if(head == null || head.isEmpty()){
			return;
		}
		CrossReportContext parent =	ctx;
		if(parent.getRowHeads() == null){
			parent.setRowHeads(new ArrayList<RowHeadContext>());
		}
		JSONObject obj = head;
		String desc = obj.getString("desc");
		JSONObject style = (JSONObject)obj.get("style");
		String styleClass = null;
		if(style != null && !style.isNullObject() && !style.isEmpty()){
			String id = "H"+IdCreater.create();
			this.css.append("#T_"+ctx.getId()+" ." + id+"{");
			TableService.parserStyle(style, this.css);
			this.css.append("}");
			styleClass = id;
		}
		RowHeadContext row = new RowHeadContext();
		row.setDesc(desc);
		row.setStyleClass(styleClass);
		parent.getRowHeads().add(row);
	}

	public CrossReportContext json2Table(JSONObject json) {
		JSONObject tableJson = json.getJSONObject("tableJson");
		String from = (String)json.get("from");
		
		CrossReportContext ctx = new CrossReportContextImpl();
		ctx.setOut("html");
		ctx.setId("R"+IdCreater.create());
		//ctx.setKpiRate(false);
		
		//处理baseKpi
		JSONObject baseKpij = (JSONObject)tableJson.get("baseKpi");
		if(baseKpij != null){
			CrossKpi baseKpi = new BaseKpiField();
			baseKpi.setAggregation((String)baseKpij.get("aggre"));
			baseKpi.setAlias((String)baseKpij.get("kpi"));
			baseKpi.setFormatPattern((String)baseKpij.get("fmt"));
			ctx.setBaseKpi(baseKpi);
		}
		CrossCols cols = new CrossCols();
		cols.setCols(new ArrayList<CrossField>());
		ctx.setCrossCols(cols);
		
		CrossRows rows = new CrossRows();
		rows.setRows(new ArrayList<CrossField>());
		ctx.setCrossRows(rows);
		
		//处理交叉表头
		this.parserHead((JSONObject)json.get("head"), ctx);
		
		//判断是否有事件
		/**
		JSONObject linkAccept = (JSONObject)json.get("linkAccept");
		if(linkAccept != null && !linkAccept.isNullObject() && !linkAccept.isEmpty()){
			ctx.setLabel(json.getString("id"));
		}
		boolean uselink = false;
		JSONObject link = (JSONObject)json.get("link");
		if(link != null && !link.isNullObject() && !link.isEmpty()){
			RowLinkContext rlink = new RowLinkContext();
			rlink.setTarget(new String[]{(String)link.get("target")});
			ctx.getCrossRows().setLink(rlink);
			uselink = true;
		}
		**/
		
		JSONArray colsStr = tableJson.getJSONArray("cols");
		JSONArray rowsStr = tableJson.getJSONArray("rows");
		loopField(colsStr, cols.getCols(), ctx, from);
		loopField(rowsStr, rows.getRows(), ctx, from);

		return ctx;
	}
	
	/**
	 * 递归节点
	 */
	private void loopField(JSONArray nodes, List<CrossField> ls, CrossReportContext ctx, String from){
		for(int i=0; i<nodes.size(); i++){
			JSONObject node = nodes.getJSONObject(i);
			CrossField cf = new CrossField();
			String tp = node.getString("type");
			String desc= node.getString("desc");
			cf.setDesc(desc);
			if("kpi".equals(tp)){  //指标
				cf.setType("kpiOther");
				cf.setAlias((String)node.get("alias"));
				cf.setAggregation((String)node.get("aggre"));
				cf.setFormatPattern((String)node.get("fmt"));
				if(node.get("rate") != null){
					cf.setKpiRate(new BigDecimal(node.get("rate").toString()));
				}
				cf.setJsFunc((String)node.get("funcname"));
				String code = (String)node.get("code");
				if(code != null && code.length() > 0){
					try {
						code = URLDecoder.decode(code, "UTF-8");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					this.scripts.append("function "+cf.getJsFunc()+"(value,col,row,data){"+code+"}");
				}
				JSONObject warn = (JSONObject)node.get("warning");
				if(warn != null && !warn.isEmpty()){
					String name = CrossReportService.createWarning(warn, cf.getFormatPattern(), this.scripts);
					cf.setJsFunc(name);
				}
			}else if("none".equals(tp)){   //none
				cf.setType("none");
			}else{
				String spaceNum = (String)node.get("spaceNum");
				String value = (String)node.get("value");
				if(value == null || value.length() == 0){ //有值，设为kpi
					cf.setType("frd");  //维度
					cf.setMulti(true);
					if("olap".equals(from)){ //如果是通过olap数据集访问，启用id + 别名作为 alias
						cf.setAlias((String)node.get("col"));
						cf.setAliasDesc((String)node.get("col") + "_desc");
					}else{
						cf.setAlias(node.getString("colname"));
						cf.setAliasDesc((String)node.get("colnamedesc"));
					}
				}else{
					cf.setType("kpi");
					cf.setMulti(false);
					cf.setAlias((String)node.get("col"));
					cf.setValue(value);
				}
				cf.setSpaceNum(spaceNum == null ? null : new Integer(spaceNum));
			}
			String sort = (String)node.get("sort");
			if(sort != null && "y".equalsIgnoreCase(sort)){
				cf.setOrder(true);
			}
			//处理样式
			JSONObject style = (JSONObject)node.get("style");
			if(style != null && !style.isNullObject() && !style.isEmpty()){
				String id = "S"+IdCreater.create();
				this.css.append("#T_"+ctx.getId()+" ." + id+"{");
				TableService.parserStyle(style, this.css);
				this.css.append("}");
				cf.setStyleClass(id);
			}
			
			//控制others
			JSONArray others = (JSONArray)node.get("others");
			if(others != null && others.size() > 0){
				if(cf.getOther() == null){
					cf.setOther(new ArrayList<CrossFieldOther>());
				}
				for(int j=0; j<others.size(); j++){
					CrossFieldOther other = new CrossFieldOther();
					JSONObject obj = others.getJSONObject(j);
					other.setType("none");
					other.setAlias(obj.getString("alias"));
					other.setValue((String)obj.get("value"));
					cf.getOther().add(other);
				}
			}
			
			ls.add(cf);
			JSONArray children = (JSONArray)node.get("children");
			if(children != null && children.size() > 0){
				cf.setSubs(new ArrayList<CrossField>());
				this.loopField(children, cf.getSubs(), ctx, from);
			}
		}
	}
}
