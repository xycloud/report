package com.ruisi.vdop.ser.bireport;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

/**
 * 用户表格定制中所选择的纬度，指标等内容，
 * @author hq
 * @date 2010-10-26
 */
public class TableSqlJsonVO {
	
	//当前选择的维度(观察角度, 及维度值)
	private List<DimInfo> dims = new ArrayList<DimInfo>();
	
	//所选择的指标
	private List<KpiInfo> kpis = new ArrayList<KpiInfo>();
	
	private JSONObject linkAccept; //事件接受对象
	private JSONObject link; //事件发起对象
	
	private BaseDate baseDate; //查询数据周期
	
	public BaseDate getBaseDate() {
		return baseDate;
	}

	public void setBaseDate(BaseDate baseDate) {
		this.baseDate = baseDate;
	}
	
	/**
	 * 获取kpi的计算方式，是计算上期值、还是计算同期值、还是都计算
	 * 
	 * @return 返回 0(都不计算)，1（上期值）, 2（同期值）, 3 （都计算） 
	 */
	public int getKpiComputeType(){
		boolean sq = false;
		boolean tq = false;
		for(KpiInfo kpi : kpis){
			String compute = kpi.getCompute();
			if(compute != null && compute.length() > 0){
				String[] jss = compute.split(",");
				for(String js : jss){
					if("sq".equals(js) || "zje".equals(js) || "hb".equals(js)){
						sq = true;
					}else if("tq".equals(js) || "tb".equals(js)){
						tq = true;
					}
				}
			}
		}
		if(sq && tq){
			return 3;
		}else if(sq){
			return 1;
		}else if(tq){
			return 2;
		}else{
			return 0;
		}
	}

	public QueryDay getDayColumn(){
		QueryDay ret = null;
		for(DimInfo dim : dims){
			if(dim.getType().equals("day")){
				ret = dim.getDay();
				break;
			}
		}
		return ret;
	}
	
	public QueryMonth getMonthColumn(){
		QueryMonth ret = null;
		for(DimInfo dim : dims){
			if(dim.getType().equals("month")){
				ret = dim.getMonth();
				break;
			}
		}
		return ret;
	}
	
	/**
	 * 判断维度是否存在，用在固定交叉表中
	 * @param dimId
	 * @return
	 */
	public DimInfo getDimByCol(String col){
		DimInfo ret = null;
		for(DimInfo dim : dims){
			if(dim.getColName().equals(col)){
				ret = dim;
				break;
			}
		}
		return ret;
	}
	/**
	 * 判断指标是否存在，用在固定交叉表中
	 * @param dimId
	 * @return
	 */
	public KpiInfo getKpiByCol(String id){
		KpiInfo ret = null;
		for(KpiInfo kpi : this.kpis){
			if(kpi.colName.equals(id)){
				ret = kpi;
				break;
			}
		}
		return ret;
	}
	
	public List<DimInfo> getDims() {
		return dims;
	}

	public void setDims(List<DimInfo> dims) {
		this.dims = dims;
	}
	
	public int getChartDimCount(){
		int ret = 0;
		for(DimInfo d : dims){
			if(!d.getDimpos().equals("param")){
				ret++;
			}
		}
		return ret;
	}
	
	/**
	 * 数据查询的时间周期设置。限制时间段以提高查询效率
	 * @author hq
	 * @date 2013-11-27
	 */
	public static class BaseDate{
		private String start;
		private String end;
		public String getStart() {
			return start;
		}
		public String getEnd() {
			return end;
		}
		public void setStart(String start) {
			this.start = start;
			if(this.start != null){
				this.start = this.start.replaceAll("-", "");
			}
		}
		public void setEnd(String end) {
			this.end = end;
			if(this.end != null){
				this.end = this.end.replaceAll("-", "");
			}
		}
		
		
	}

	public static class QueryDay{
		private String startDay;
		private String endDay;
		public String getStartDay() {
			return startDay;
		}
		public void setStartDay(String startDay) {
			this.startDay = startDay;
			if(this.startDay != null){
				this.startDay = this.startDay.replaceAll("-", "");
			}
		}
		public String getEndDay() {
			return endDay;
		}
		public void setEndDay(String endDay) {
			this.endDay = endDay;
			if(this.endDay != null){
				this.endDay = this.endDay.replaceAll("-", "");
			}
		}
		
		public int getBetweenDay() throws ParseException{
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");		
			long l1 = sdf.parse(this.startDay).getTime();		
			long l2 = sdf.parse(this.endDay).getTime();	
			long result = Math.abs(l1 - l2) / (24 * 60 * 60 * 1000);
			return (int)result;
		}
	}
	
	public static class QueryMonth{
		private String startMonth;
		private String endMonth;
		public String getStartMonth() {
			return startMonth;
		}
		public void setStartMonth(String startMonth) {
			this.startMonth = startMonth;
			if(this.startMonth != null){
				this.startMonth = this.startMonth.replaceAll("-", "");
			}
		}
		public String getEndMonth() {
			return endMonth;
		}
		public void setEndMonth(String endMonth) {
			this.endMonth = endMonth;
		}
		
		public int getBetweenMonth() throws ParseException{
			int year1 = Integer.parseInt(this.startMonth.substring(0,4));
			int year2 = Integer.parseInt(this.endMonth.substring(0,4));
			
			int month1 = Integer.parseInt(this.startMonth.substring(4,6));
			int month2 = Integer.parseInt(this.endMonth.substring(4,6));
			
			int betweenMonth = month2 - month1;
			int betweenYear = year2 - year1;
			
			
			return betweenYear * 12 + betweenMonth;
		}
		
	}
	/**
	 * 提交的指标信息
	 * @date 2011-5-9
	 */
	public static class KpiInfo{
		
		private String aggre;
		private String colName;
		private String fmt;
		private String alias;
		private String kpiName;
		private String tid; //指标所在表ID
		private String descKey; //指标解释KEY
		private Integer rate; //指标倍率
		private String unit; //指标单位
		private Integer id; //指标ID
		private String sort; //指标排序方式，用在SQL中
		private String min; //y轴最小值
		private String max; //Y轴最大值，用在仪表盘中
		
		private Integer calc;  //是否计算指标
		
		private KpiFilter filter; //对指标进行过滤
		
		private JSONObject style; //指标样式
		private JSONObject warn;  //指标预警
		private String compute; //指标计算方式（同比、环比、占比、排名等计算）
		
		private String funcname;  //回调函数名称
		private String code;  //回调函数内容

		public Integer getId() {
			return id;
		}
		public void setId(Integer id) {
			this.id = id;
		}
		public Integer getRate() {
			return rate;
		}
		public void setRate(Integer rate) {
			this.rate = rate;
		}
		public String getDescKey() {
			return descKey;
		}
		public void setDescKey(String descKey) {
			this.descKey = descKey;
		}
		public String getTid() {
			return tid;
		}
		public void setTid(String tid) {
			this.tid = tid;
		}
		public String getAggre() {
			return aggre;
		}
		public void setAggre(String aggre) {
			this.aggre = aggre;
		}
		public String getColName() {
			return colName;
		}
		public void setColName(String colName) {
			this.colName = colName;
		}
		public String getFmt() {
			return fmt;
		}
		public void setFmt(String fmt) {
			this.fmt = fmt;
		}
		public String getAlias() {
			return alias;
		}
		public void setAlias(String alias) {
			this.alias = alias;
		}
		public String getKpiName() {
			return kpiName;
		}
		public void setKpiName(String kpiName) {
			this.kpiName = kpiName;
		}
		public String getUnit() {
			return unit;
		}
		public void setUnit(String unit) {
			this.unit = unit;
		}
		public String getSort() {
			return sort;
		}
		public void setSort(String sort) {
			this.sort = sort;
		}
		public String getMin() {
			return min;
		}
		public void setMin(String min) {
			this.min = min;
		}
		public KpiFilter getFilter() {
			return filter;
		}
		public void setFilter(KpiFilter filter) {
			this.filter = filter;
		}
		public JSONObject getWarn() {
			return warn;
		}
		public void setWarn(JSONObject warn) {
			this.warn = warn;
		}
		public Integer getCalc() {
			return calc;
		}
		public void setCalc(Integer calc) {
			this.calc = calc;
		}
		public JSONObject getStyle() {
			return style;
		}
		public String getFuncname() {
			return funcname;
		}
		public String getCode() {
			return code;
		}
		public void setFuncname(String funcname) {
			this.funcname = funcname;
		}
		public void setCode(String code) {
			this.code = code;
		}
		public String getMax() {
			return max;
		}
		public void setMax(String max) {
			this.max = max;
		}
		public String getCompute() {
			return compute;
		}
		public void setCompute(String compute) {
			this.compute = compute;
		}
		public void setStyle(JSONObject style) {
			this.style = style;
		}
	}
	
	/**
	 * 指标过滤器
	 * @author hq
	 * @date 2014-3-11
	 */
	public static class KpiFilter{
		private String kpiId;
		private String filterType; //>,<,=,qj 四种
		private Double val1;
		private Double val2; //在区间匹配的时候，需要val2
		
		public String getKpiId() {
			return kpiId;
		}
		public String getFilterType() {
			return filterType;
		}
		
		public void setKpiId(String kpiId) {
			this.kpiId = kpiId;
		}
		public void setFilterType(String filterType) {
			this.filterType = filterType;
		}
		public Double getVal1() {
			return val1;
		}
		public Double getVal2() {
			return val2;
		}
		public void setVal1(Double val1) {
			this.val1 = val1;
		}
		public void setVal2(Double val2) {
			this.val2 = val2;
		}
		
		
	}
	
	
	/**
	 * 提交的维度信息
	 * @author hq
	 * @date 2011-4-29
	 */
	public static class DimInfo {
		private String id;
		private String type;
		private String dimName;
		private String colName; //码表在事实表中对应的字段名
		private String vals; //码表的限制维
		private String valDesc; //码表限制维的名称
		private String issum; //y,n两值
		private String tid; //指标所在表ID
		
		private String tableName; //维度码表表名
		private String tableColKey; //码表表KEY字段
		private String tableColName; //码表表name字段
		
		private String dimOrd; //维度排序方式
		private String ordcol; //维度排序字段
		private String colDesc; //维度名称
		private String valType; //维度value 字段的类型，用在拼接sql中，判断是否增加单引号
		
		private String dimpos; //维度所在位置，行维度还是列维度 还是 参数(param)
		private String pos; //col还是row, 用在图形中表示钻取维度的来源
		
		private QueryDay day;
		private QueryMonth month;
		
		private Boolean isArea; //是否地域维度，因为需要和地图结合，地域维需要特殊处理，并且固定map_id 字段
	
		public Boolean getIsArea() {
			return isArea;
		}
		public void setIsArea(Boolean isArea) {
			this.isArea = isArea;
		}
		
		public String getOrdcol() {
			return ordcol;
		}
		public void setOrdcol(String ordcol) {
			this.ordcol = ordcol;
		}
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		public String getColName() {
			return colName;
		}
		public void setColName(String colName) {
			this.colName = colName;
		}
		public String getVals() {
			return vals;
		}
		public void setVals(String vals) {
			this.vals = vals;
		}
		public QueryDay getDay() {
			return day;
		}
		public QueryMonth getMonth() {
			return month;
		}
		public void setDay(QueryDay day) {
			this.day = day;
		}
		public void setMonth(QueryMonth month) {
			this.month = month;
		}
		public String getDimName() {
			return dimName;
		}
		public void setDimName(String dimName) {
			this.dimName = dimName;
		}
		public String getIssum() {
			return issum;
		}
		public void setIssum(String issum) {
			this.issum = issum;
		}
		public String getTid() {
			return tid;
		}
		public void setTid(String tid) {
			this.tid = tid;
		}
		public String getTableName() {
			if(tableName == null){
				return "";
			}else{
				return tableName;
			}
		}
		public String getTableColKey() {
			return tableColKey;
		}
		public String getTableColName() {
			return tableColName;
		}
		public String getValDesc() {
			return valDesc;
		}
		public void setValDesc(String valDesc) {
			this.valDesc = valDesc;
		}
		public void setTableName(String tableName) {
			this.tableName = tableName;
		}
		public String getPos() {
			return pos;
		}
		public void setPos(String pos) {
			this.pos = pos;
		}
		public void setTableColKey(String tableColKey) {
			this.tableColKey = tableColKey;
		}
		public void setTableColName(String tableColName) {
			this.tableColName = tableColName;
		}
		public String getDimOrd() {
			return dimOrd;
		}
		public void setDimOrd(String dimOrd) {
			this.dimOrd = dimOrd;
		}
		public String getValType() {
			return valType;
		}
		public void setValType(String valType) {
			this.valType = valType;
		}
		public String getColDesc() {
			return colDesc;
		}
		public void setColDesc(String colDesc) {
			this.colDesc = colDesc;
		}
		public String getDimpos() {
			return dimpos;
		}
		public void setDimpos(String dimpos) {
			this.dimpos = dimpos;
		}
		
	}




	public List<KpiInfo> getKpis() {
		return kpis;
	}

	public void setKpis(List<KpiInfo> kpis) {
		this.kpis = kpis;
	}

	public JSONObject getLinkAccept() {
		return linkAccept;
	}

	public void setLinkAccept(JSONObject linkAccept) {
		this.linkAccept = linkAccept;
	}

	public JSONObject getLink() {
		return link;
	}

	public void setLink(JSONObject link) {
		this.link = link;
	}
	
}
