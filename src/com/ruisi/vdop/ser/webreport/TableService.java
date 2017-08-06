package com.ruisi.vdop.ser.webreport;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.ruisi.ext.engine.ExtConstants;
import com.ruisi.ext.engine.init.TemplateManager;
import com.ruisi.ext.engine.util.IdCreater;
import com.ruisi.ext.engine.view.context.dc.grid.AggreVO;
import com.ruisi.ext.engine.view.context.dc.grid.GridAggregationContext;
import com.ruisi.ext.engine.view.context.dc.grid.GridDataCenterContext;
import com.ruisi.ext.engine.view.context.dc.grid.GridDataCenterContextImpl;
import com.ruisi.ext.engine.view.context.dc.grid.GridFilterContext;
import com.ruisi.ext.engine.view.context.dc.grid.GridSetConfContext;
import com.ruisi.ext.engine.view.context.grid.PageInfo;
import com.ruisi.ext.engine.view.context.gridreport.GridCell;
import com.ruisi.ext.engine.view.context.gridreport.GridCellLink;
import com.ruisi.ext.engine.view.context.gridreport.GridReportContext;
import com.ruisi.ext.engine.view.context.gridreport.GridReportContextImpl;
import com.ruisi.ispire.dc.grid.GridFilter;

public class TableService {
	
	private DataService dataService = new DataService();
	
	private List<JSONObject> aggreCols;
	private StringBuffer css;
	private StringBuffer scripts; //服务端脚本
	
	public TableService(StringBuffer css, StringBuffer scripts){
		this.css = css;
		this.scripts = scripts;
	}
	
	public void init(){
		aggreCols = new ArrayList<JSONObject>();
	}

	public GridReportContext json2Table(JSONObject json){
		GridReportContext grid = new GridReportContextImpl();
		String id = (String)json.get("compid");
		if(id == null || id.length() == 0){
			id = ExtConstants.gridReportPrefix + IdCreater.create();
		}
		grid.setId(id);
		
		//判断是否有事件
		JSONObject linkAccept = (JSONObject)json.get("linkAccept");
		if(linkAccept != null && !linkAccept.isNullObject() && !linkAccept.isEmpty()){
			grid.setLabel(json.getString("id"));
		}
		//是否锁定表头
		String lockhead = (String)json.get("lockhead");
		grid.setLockUI("true".equalsIgnoreCase(lockhead));
		JSONObject link = (JSONObject)json.get("link");
		JSONObject header = (JSONObject)json.get("header");
		grid.setHeaders(parserHeader(header, "header", grid, link));
		JSONObject detail = (JSONObject)json.get("detail");
		grid.setDetails(parserHeader(detail, "detail", grid, link));
		if("true".equals(json.get("isfooter"))){
			JSONObject footer = (JSONObject)json.get("footer");
			grid.setFooters(parserHeader(footer, "footer", grid, link));
		}
		//分页
		String isfy = (String)json.get("isfy");
		String pagesize = (String)json.get("pagesize");
		if(pagesize == null){
			pagesize = "20";
		}
		
		if("true".equals(isfy)){
			PageInfo page = new PageInfo();
			if(pagesize == null || pagesize.length() == 0){
				page.setPagesize(20);
			}else{
				page.setPagesize(Integer.parseInt(pagesize));
			}
			grid.setPageInfo(page);
		}
		return grid;
	}
	
	public GridDataCenterContext createDataCenter(JSONObject dataset, String dsourceId, JSONObject linkAccept) throws IOException{
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
		
		//判断是否有需要聚合的数据
		parserAggreCol(ctx);
		
		return ctx;
	}
	
	private boolean isEmptyCell(GridCell cell){
		if((cell.getAlias() == null || cell.getAlias().length() == 0) &&( cell.getDesc() == null || cell.getDesc().length() == 0) && (cell.getDynamicText() == null || cell.getDynamicText() == false)){
			return true;
		}else{
			return false;
		}
	}
	
	//本行数据是否全部为空
	private boolean isDataEmpty(GridCell[] cells){
		boolean empty = true;
		for(GridCell cell : cells){
			if(!isEmptyCell(cell)){
				empty = false;
				break;
			}
		}
		return empty;
	}
	
	public void parserAggreCol(GridDataCenterContext ctx){
		for(JSONObject col : aggreCols){
			GridAggregationContext agg = new GridAggregationContext();
			agg.setToExt(true);
			AggreVO[] avo = new AggreVO[1];
			AggreVO vo = new AggreVO();
			vo.setName(col.getString("col"));
			vo.setAlias(col.getString("alias"));
			vo.setType(col.getString("aggre"));
			avo[0] = vo;
			agg.setAggreVO(avo);
			ctx.getProcess().add(agg);
		}
	}
	
	public static void parserStyle(JSONObject th, StringBuffer sb){
		String fontsize = (String)th.get("fontsize");
		String fontcolor = (String)th.get("fontcolor");
		String fontweight = (String)th.get("fontweight");
		String italic = (String)th.get("italic");
		String underscore = (String)th.get("underscore");
		String bgcolor = (String)th.get("bgcolor");
		String width = (String)th.get("width");
		String height = (String)th.get("height");
		if(fontsize != null && fontsize.length() > 0){
			sb.append("font-size:"+fontsize+"px;");
		}
		if("true".equals(fontweight)){
			sb.append("font-weight:bold;");
		}
		if(fontcolor != null && fontcolor.length() > 0){
			sb.append("color:"+fontcolor+";");
		}
		if("true".equals(italic)){
			sb.append("font-style:italic;");
		}
		if("true".equals(underscore)){
			sb.append("text-decoration: underline;");
		}
		if(bgcolor != null && bgcolor.length() > 0){
			sb.append("background-color:"+bgcolor+";");
		}
		if(width != null && width.length() > 0){
			sb.append("width:"+width+"px;");
		}
		if(height != null && height.length() > 0){
			sb.append("height:"+height+"px;");
		}
	}
	
	public GridCell[][] parserHeader(JSONObject header, String tp, GridReportContext grid, JSONObject link ){
		if(header == null){
			return null;
		}
		List<GridCell[]> rows = new ArrayList<GridCell[]>();
		for(int i=0; true; i++){
			JSONArray trs = (JSONArray)header.get("tr" + i);
			if(trs != null){
				GridCell[] cells = new GridCell[trs.size()];
				for(int j=0; j<trs.size(); j++){
					JSONObject th = trs.getJSONObject(j);
					String colSpan = (String)th.get("colspan");
					String rowSpan = (String)th.get("rowspan");
					String desc = (String)th.get("desc");
					String align = (String)th.get("align");
					String alias = (String)th.get("alias");
					String order = (String)th.get("order");
					String width = (String)th.get("width");
					String fmt = (String)th.get("fmt");
					String isaggre = (String)th.get("isaggre");
					String id = "C"+IdCreater.create();
					if(grid.getLockUI() == null || grid.getLockUI() == false){
						css.append("div.crossReport #T_"+grid.getId()+" ");
						if("detail".equals(tp)){
							css.append("td");
						}else{
							css.append("th");
						}
					}else{
						css.append("#"+grid.getId()+" ");
						if("detail".equals(tp)){
							css.append("#body-"+grid.getId()+" td");
						}else{
							css.append("#head-"+grid.getId()+" th");
						}
					}
					css.append("."+id+"{");
					this.parserStyle(th, css);
					css.append("}");
					
					GridCell cell = new GridCell();
					cell.setAlias(alias);
					cell.setAlign(align);
					cell.setDesc(desc);
					cell.setWidth(width);
					cell.setStyleClass(id);
					if(fmt != null && fmt.length() > 0){
						cell.setFormatPattern(fmt);
					}
					if("true".equals(order)){
						cell.setOrder(true);
					}
					if(colSpan != null && colSpan.length() > 0){
						cell.setColSpan(Integer.parseInt(colSpan));
					}
					if(rowSpan != null && rowSpan.length() > 0){
						cell.setRowSpan(Integer.parseInt(rowSpan));
					}
					cells[j] = cell;
					
					//判断是否有回调函数
					if("detail".equals(tp)){
						String funcname = (String)th.get("funcname");
						String code = (String)th.get("code");
						if(funcname != null && funcname.length() > 0){
							try {
								code = URLDecoder.decode(code, "UTF-8");
							} catch (UnsupportedEncodingException e) {
								e.printStackTrace();
							}
							this.scripts.append("function "+funcname+"(value, rowData, column){"+code+"}");
						}
						cell.setJsFunc(funcname);
					}
					
					//判断是否有需要聚合的字段
					if(tp.equals("header") || tp.equals("footer")){
						if("y".equals(isaggre)){
							cell.setDynamicText(true);
							cell.setDesc("");
							this.aggreCols.add(th);
						}
					}else{
						//判断是否有链接
						if(link != null && !link.isNullObject() && !link.isEmpty()){
							String linkalias = (String)link.get("alias");
							if(cell.getAlias().equals(linkalias)){
								GridCellLink lk = new GridCellLink();
								lk.setByAlias(linkalias);
								lk.setTarget(link.getString("target").split(","));
								lk.setType(link.getString("type").split(","));
								cell.setLink(lk);
							}
						}
					}
				}
				//if(!isDataEmpty(cells)){
				rows.add(cells);
				//}
			}else{
				break;
			}
		}
		GridCell[][] head = new GridCell[rows.size()][];
		for(int i=0; i<head.length; i++){
			head[i] = rows.get(i);
		}
		return head;
	}
}
