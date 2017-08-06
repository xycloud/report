package com.ruisi.vdop.web.webreport;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.ruisi.vdop.ser.webreport.DBUtils;
import com.ruisi.vdop.ser.webreport.DataService;
import com.ruisi.vdop.util.VDOPUtils;

public class DataSetAction {
	
	private String datasetname;
	private String querysql;
	private String ds; //datasource的JSON
	private String dataset; //datasetjson
	private String datsetid;
	private String params; //页面参数的json
	private String dim; //固定表维度对应的CUBE维度, 用在维度分解中
	private String vars; //报表变量

	public String crtDataSet(){
		return "crtDataSet";
	}
	
	public String queryData() throws Exception{
		DataService ser = new DataService();
		DataService.RSDataSource rsds = null;
		if(ds != null && ds.length() > 0){
			rsds = ser.json2datasource(JSONObject.fromObject(ds));
		}
		JSONObject obj = JSONObject.fromObject(dataset);
		obj.put("querysql", this.querysql);
		String sql = ser.createDatasetSql(obj);
		JSONArray ps = JSONArray.fromObject(this.params);
		JSONArray var = JSONArray.fromObject(this.vars);
		sql = DBUtils.evaluateSql(sql, ps, var);
		List ls = DBUtils.queryTopN(sql, rsds, 50);
		VDOPUtils.getRequest().setAttribute("ls", ls);
		return "queryData";
	}
	
	/**
	 * 查询维度值列表，用在固定报表的维度分解中。
	 * @return
	 * @throws IOException 
	 */
	public String queryDimValues() throws IOException{
		JSONObject dJson = JSONObject.fromObject(this.dim);
		String tname = dJson.getString("tname");
		String dimkey = dJson.getString("colname");
		String dimtxt = dJson.getString("colnamedesc");
		DataService ds = new DataService();
		List ls = ds.queryDimValues(dimtxt, dimkey, tname);
		HttpServletResponse resp = VDOPUtils.getResponse();
		resp.setContentType("text/xml; charset=UTF-8");
		resp.getWriter().print(JSONArray.fromObject(ls).toString());
		return null;
	}
	
	public String testSql() throws Exception{
		DataService ser = new DataService();
		DataService.RSDataSource rsds = null;
		if(this.ds != null && ds.length() > 0){
			rsds = ser.json2datasource(JSONObject.fromObject(ds));
		}
		JSONObject obj = JSONObject.fromObject(dataset);
		obj.put("querysql", this.querysql);
		String sql = ser.createDatasetSql(obj);
		JSONArray ps = JSONArray.fromObject(this.params);
		JSONArray var = JSONArray.fromObject(this.vars);
		sql = DBUtils.evaluateSql(sql, ps, var);
		boolean ret = DBUtils.testSql(sql, rsds);
		VDOPUtils.getRequest().setAttribute("ret", ret);
		VDOPUtils.getRequest().setAttribute("sql", sql);
		return "testSql";
	}
	
	/**
	 * 只返回JSON，不返回页面
	 * @return
	 * @throws Exception
	 */
	public String testSql2() throws Exception{
		DataService ser = new DataService();
		DataService.RSDataSource rsds = null;
		if(ds != null && ds.length() > 0){
			rsds = ser.json2datasource(JSONObject.fromObject(ds));
		}
		JSONObject obj = JSONObject.fromObject(dataset);
		obj.put("querysql", this.querysql);
		String sql = ser.createDatasetSql(obj);
		JSONArray ps = JSONArray.fromObject(this.params);
		JSONArray var = JSONArray.fromObject(this.vars);
		sql = DBUtils.evaluateSql(sql, ps, var);
		List<DataService.DSColumn> ls = DBUtils.queryMeta(sql, rsds);
		HttpServletResponse resp = VDOPUtils.getResponse();
		resp.setContentType("text/xml; charset=UTF-8");
		resp.getWriter().print(JSONArray.fromObject(ls).toString());
		return null;
	}
	
	public String queryMeta() throws Exception{
		//剔除sql的特殊符号
		querysql = querysql.replaceAll("\\[\\w+\\]", "");
		DataService ser = new DataService();
		DataService.RSDataSource rsds = null;
		if(ds != null && ds.length() > 0){
			rsds = ser.json2datasource(JSONObject.fromObject(ds));
		}
		List<DataService.DSColumn> ls = DBUtils.queryMeta(querysql, rsds);
		HttpServletResponse resp = VDOPUtils.getResponse();
		resp.setContentType("text/xml; charset=UTF-8");
		String ctx = JSONArray.fromObject(ls).toString();
		resp.getWriter().println(ctx);
		return null;
	}

	public String getDatasetname() {
		return datasetname;
	}

	public String getQuerysql() {
		return querysql;
	}

	public void setDatasetname(String datasetname) {
		this.datasetname = datasetname;
	}

	public void setQuerysql(String querysql) {
		this.querysql = querysql;
	}

	public String getDs() {
		return ds;
	}

	public void setDs(String ds) {
		this.ds = ds;
	}

	public String getDatsetid() {
		return datsetid;
	}

	public void setDatsetid(String datsetid) {
		this.datsetid = datsetid;
	}

	public String getDataset() {
		return dataset;
	}

	public void setDataset(String dataset) {
		this.dataset = dataset;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	public String getDim() {
		return dim;
	}

	public void setDim(String dim) {
		this.dim = dim;
	}

	public String getVars() {
		return vars;
	}

	public void setVars(String vars) {
		this.vars = vars;
	}
	
}
