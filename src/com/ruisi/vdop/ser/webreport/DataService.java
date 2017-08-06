package com.ruisi.vdop.ser.webreport;

import java.util.List;

import com.ruisi.vdop.util.VDOPUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class DataService {
	public static final String mysql = "com.mysql.jdbc.Driver";
	public static final String oracle = "oracle.jdbc.driver.OracleDriver";
	public static final String sqlserver = "net.sourceforge.jtds.jdbc.Driver";
	
	public static final String showTables_mysql = "show tables";
	public static final String showTables_oracle = "select table_name from tabs";
	public static final String showTables_sqlser = "select name from sysobjects where xtype='U' order by name";
	
	public static final String[] dataTypes = new String[]{"String", "Int", "Double"}; 
	
	/**
	 * 在处理 $xx 这种特殊字符的时候，先用 [x] 代替，再替换回来 
	 * @param obj
	 * @return
	 */
	public String createDatasetSql(JSONObject obj){
		String sql = obj.getString("querysql");
		Object objs = obj.get("param");
		if(objs != null){
			JSONArray params = (JSONArray)objs;
			for(int i=0; i<params.size(); i++){
				StringBuffer sb = new StringBuffer("");
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
				String tablealias = (String)param.get("tablealias");
				
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
						sb.append(" and " + (tablealias != null && tablealias.length() > 0 ? tablealias+".":"") + col + " "+type+" " + ("string".equals(valuetype) ? "'" + ("like".equals(type)?"%":"") +"[x]"+linkparam+""+("like".equals(type)?"%":"")+"'":"[x]"+linkparam) + "");
						sb.append("  #end");
					}
				}
				sql = sql.replaceAll("\\["+filterid+"\\]", sb.toString()).replaceAll("\\[x\\]", "\\$");
			}
			return sql;
		}
		return sql;
	}
	
	public RSDataSource json2datasource(JSONObject obj){
		RSDataSource ds = new RSDataSource();
		Object use = obj.get("use");
		ds.setUse(use == null ? null : use.toString());
		if(use == null || "jdbc".equalsIgnoreCase(use.toString())){
			ds.setName(obj.getString("linkname"));
			ds.setPsd(obj.getString("linkpwd"));
			ds.setUrl(obj.getString("linkurl"));
			String linktype = obj.getString("linktype");
			ds.setType(linktype);
			if(linktype.equals("mysql")){
				ds.setLinktype(mysql);
			}else if(linktype.equals("oracle")){
				ds.setLinktype(oracle);
			}else if(linktype.equals("sqlserver")){
				ds.setLinktype(sqlserver);
			}
		}else{
			ds.setJdniname(obj.getString("jndiname"));
		}
		return ds;
	}
	
	public List queryDimValues(String text, String val, String tname){
		String sql = "select "+text+" \"text\", "+val+" \"val\" from "+tname + " order by " + val;
		return VDOPUtils.getDaoHelper().queryForList(sql);
	}
	
	public static class RSDataSource {
		private String  url;
		private String name;
		private String psd;
		private String linktype;
		private String type; //连接方式,mysql/sqlser/oracle
		
		private String use;
		private String jdniname;
		public String getUrl() {
			return url;
		}
		public String getName() {
			return name;
		}
		public String getPsd() {
			return psd;
		}
		public String getLinktype() {
			return linktype;
		}
		public String getType() {
			return type;
		}
		public String getUse() {
			return use;
		}
		public String getJdniname() {
			return jdniname;
		}
		public void setUrl(String url) {
			this.url = url;
		}
		public void setName(String name) {
			this.name = name;
		}
		public void setPsd(String psd) {
			this.psd = psd;
		}
		public void setLinktype(String linktype) {
			this.linktype = linktype;
		}
		public void setType(String type) {
			this.type = type;
		}
		public void setUse(String use) {
			this.use = use;
		}
		public void setJdniname(String jdniname) {
			this.jdniname = jdniname;
		}
		
	}
	
	public static class DSColumn {
		private String name;
		private String type;
		private String dispName;
		private int length; //字段长度
		private String tname; 
		
		public String getName() {
			return name;
		}
		public String getType() {
			return type;
		}
		public void setName(String name) {
			this.name = name;
		}
		public void setType(String type) {
			this.type = type;
		}
		public String getDispName() {
			return dispName;
		}
		public void setDispName(String dispName) {
			this.dispName = dispName;
		}
		public int getLength() {
			return length;
		}
		public void setLength(int length) {
			this.length = length;
		}
		public String getTname() {
			return tname;
		}
		public void setTname(String tname) {
			this.tname = tname;
		}
		
	}
}
