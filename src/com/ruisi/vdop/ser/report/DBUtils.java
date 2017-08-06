package com.ruisi.vdop.ser.report;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.PreparedStatementCallback;

import com.ruisi.ext.engine.dao.DaoHelper;
import com.ruisi.ext.engine.util.PasswordEncrypt;
import com.ruisi.vdop.ser.webreport.DataService;
import com.ruisi.vdop.util.VDOPUtils;
import com.ruisi.vdop.util.VdopConstant;

public class DBUtils {

	private static void copyData(ResultSet rs, Map m) throws SQLException{
		String tname = rs.getString(1);
		m.put("id", tname);
		m.put("text", tname);
		m.put("iconCls", "icon-table");
	}
	
	/**
	 * type == 1, 表示 ds 为 ID, 通过ID获取JSON
	 * type == 2, 表示 ds 为JSON,直接使用
	 * @param ctx
	 * @param dsid
	 * @param type
	 * @return
	 * @throws Exception 
	 */
	public static List<Map> queryTables(ServletContext ctx, String dsid, int type) throws Exception{
		DaoHelper dao = VDOPUtils.getDaoHelper(ctx);
		final List ret = new ArrayList();
		if(dsid != null && dsid.length() > 0){
			//采用用户定义的数据源进行连接，而不是采用系统连接
			String str = null;
			if(type == 1){
				String sql = "select content from olap_obj_share where id='"+dsid+"' and tp='dsource'";
				str = (String)dao.queryForObject(sql, String.class);
			}else if(type == 2){
				str = dsid;
			}
			JSONObject json = JSONObject.fromObject(str);
			DataService ser = new DataService();
			DataService.RSDataSource rsds = ser.json2datasource(json);
			Connection conn = null;
			try {
				if(rsds.getUse() != null && "jndi".equalsIgnoreCase(rsds.getUse())){
					conn = com.ruisi.vdop.ser.webreport.DBUtils.getConnection(rsds.getJdniname());
				}else{
					conn = com.ruisi.vdop.ser.webreport.DBUtils.getConnection(rsds.getUrl(), rsds.getName(), PasswordEncrypt.decode(rsds.getPsd()), rsds.getLinktype());
				}
				String qsql = null;
				if("mysql".equals(rsds.getType())){
					qsql = DataService.showTables_mysql;
				}else if("oracle".equals(rsds.getType())){
					qsql = DataService.showTables_oracle;
				}else if("sqlserver".equals(rsds.getType())){
					qsql = DataService.showTables_sqlser;
				}
				PreparedStatement ps = conn.prepareStatement(qsql);
				ResultSet rs = ps.executeQuery();
				while(rs.next()){
					Map m = new HashMap();
					copyData(rs, m);
					ret.add(m);
				}
				rs.close();
				ps.close();
			}catch (SQLException e) {
				e.printStackTrace();
				throw new RuntimeException("sql 执行报错.");
			}finally{
				com.ruisi.vdop.ser.webreport.DBUtils.closeConnection(conn);
			}
			return ret;
		}
		String dbName = VDOPUtils.getConstant("dbName");
		String qsql = null;
		if("mysql".equals(dbName)){
			qsql = DataService.showTables_mysql;
		}else if("oracle".equals(dbName)){
			qsql = DataService.showTables_oracle;
		}else if("sqlser".equals(dbName)){
			qsql = DataService.showTables_sqlser;
		}
		dao.execute(qsql, new PreparedStatementCallback(){

			public Object doInPreparedStatement(PreparedStatement ps)
					throws SQLException, DataAccessException {
				ResultSet rs = ps.executeQuery();
				while(rs.next()){
					Map m = new HashMap();
					copyData(rs, m);
					ret.add(m);
				}
				rs.close();
				return null;
			}
			
		});
		return ret;
	}
	
	public static List<DataService.DSColumn> copyValue(ResultSet rs) throws SQLException{
		ResultSetMetaData meta = rs.getMetaData();
		List<DataService.DSColumn> cols = new ArrayList<DataService.DSColumn>();
		for(int i=0; i<meta.getColumnCount(); i++){
			String name = meta.getColumnName(i+1);
			String tp = meta.getColumnTypeName(i+1);
			//meta.get
			//tp转换
			tp = columnType2java(tp);
			DataService.DSColumn col = new DataService.DSColumn();
			col.setName(name);
			col.setType(tp);
			col.setLength(meta.getColumnDisplaySize(i + 1));
			cols.add(col);
		}
		return cols;
	}
	
	public static String columnType2java(String tp){
		tp = tp.replaceAll(" UNSIGNED", ""); //mysql 存在 UNSIGNED 类型, 比如： INT UNSIGNED
		String ret = null;
		if("varchar".equalsIgnoreCase(tp) || "varchar2".equalsIgnoreCase(tp) || "nvarchar".equalsIgnoreCase(tp) || "char".equalsIgnoreCase("tp")){
			ret = "String";
		}else if("int".equalsIgnoreCase(tp) || "MEDIUMINT".equalsIgnoreCase(tp) || "BIGINT".equalsIgnoreCase(tp) || "smallint".equalsIgnoreCase(tp) || "TINYINT".equalsIgnoreCase(tp)){
			ret = "Int";
		}else if("number".equalsIgnoreCase(tp) || "DECIMAL".equalsIgnoreCase(tp) || "Float".equalsIgnoreCase(tp) || "Double".equalsIgnoreCase(tp)){
			ret = "Double";
		}else if("DATETIME".equalsIgnoreCase(tp) || "DATE".equalsIgnoreCase(tp) || "Timestamp".equalsIgnoreCase(tp)){
			ret = "Date";
		}
		return ret;
	}
	
	public static List queryTableCols(ServletContext ctx, String sql, String ds) throws Exception{
		return queryTableCols(ctx, sql, ds, 1);
	}
	
	/**
	 * 根据SQL获取 字段信息
	 * type == 1, 表示 ds 为 ID, 通过ID获取JSON
	 * type == 2, 表示 ds 为JSON,直接使用
	 * @param ctx
	 * @param sql
	 * @param ds
	 * @param type
	 * @return
	 * @throws Exception 
	 */
	public static List queryTableCols(ServletContext ctx, String sql, String ds, int type) throws Exception{
		DaoHelper dao = VDOPUtils.getDaoHelper(ctx);
		if(ds != null && ds.length() > 0){
			//采用用户定义的数据源进行连接，而不是采用系统连接
			JSONObject json = null;
			if(type == 1){
				String qsql = "select content from olap_obj_share where id='"+ds+"' and tp='dsource'";
				String str = (String)dao.queryForObject(qsql, String.class);
				json = JSONObject.fromObject(str);
			}else{
				json = JSONObject.fromObject(ds);
			}
			DataService ser = new DataService();
			DataService.RSDataSource rsds = ser.json2datasource(json);
			Connection conn = null;
			try {
				if(rsds.getUse() != null && "jndi".equalsIgnoreCase(rsds.getUse())){
					conn = com.ruisi.vdop.ser.webreport.DBUtils.getConnection(rsds.getJdniname());
				}else{
					conn = com.ruisi.vdop.ser.webreport.DBUtils.getConnection(rsds.getUrl(), rsds.getName(), PasswordEncrypt.decode(rsds.getPsd()), rsds.getLinktype());
				}
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery();
				List<DataService.DSColumn> cols = copyValue(rs);
				rs.close();
				ps.close();
				return cols;
			}catch (SQLException e) {
				e.printStackTrace();
				throw new RuntimeException("sql 执行报错.");
			}finally{
				com.ruisi.vdop.ser.webreport.DBUtils.closeConnection(conn);
			}
		}else
			return (List)dao.execute(sql, new PreparedStatementCallback(){

				public Object doInPreparedStatement(PreparedStatement ps)
						throws SQLException, DataAccessException {
					ResultSet rs = ps.executeQuery();
					List<DataService.DSColumn> cols = copyValue(rs);
					rs.close();
					return cols;
				}
				
			});
	}
	
	/**
	 * 通过字段信息，生成创建表的SQL
	 * @param ls
	 * @return
	 */
	public static String createTableSql(List<DataService.DSColumn> ls, String tname, String tnote,String dbName){
		StringBuffer sb = new StringBuffer("");
		sb.append("create table " + tname + " (\n");
		for(int i=0; i<ls.size(); i++){
			sb.append("\t");
			DataService.DSColumn col = ls.get(i);
			sb.append(col.getName());
			sb.append(" ");
			sb.append(javaType2db(col.getType(), dbName,col.getLength()));
			if(i != ls.size() - 1){
				sb.append(",");
			}
			sb.append("\n");
		}
		sb.append(")");
		//如果是mysql, 增加编码代码,默认编码是 utf8
		if("mysql".equals(dbName)){
			sb.append(" CHARSET=utf8 COMMENT='"+tnote+"'");
		}
		return sb.toString();
	}
	
	/**
	 * 转换JAVA类型到SQL类型
	 * @param type
	 * @param dbName
	 * @return
	 */
	private static String javaType2db(String type, String dbName, int length){
		if("mysql".equals(dbName)){
			if("String".equals(type)){
				if(length > 6000){
					return "text";
				}else{
					return "varchar("+length+")";
				}
			}else if("Int".equals(type)){
				return "int("+length+")";
			}else if("Double".equals(type)){
				return "DECIMAL("+(length - 2)+",2)";  //对于 double 类型，保留2位小数
			}else if("Date".equals(type)){
				return "DATETIME";
			}else{
				throw new RuntimeException("类型 " + type + " 未定义。");
			}
		}else if("sqlser".equals(dbName)){
			if("String".equals(type)){
				if(length > 6000){
					return "nvarchar(MAX)";
				}else{
					return "nvarchar("+length+")";
				}
			}else if("Int".equals(type)){
				return "int";
			}else if("Double".equals(type)){
				return "float";
			}else if("Date".equals(type)){
				return "datetime";
			}else{
				throw new RuntimeException("类型 " + type + " 未定义。");
			}
		}else if("oracle".equals(dbName)){
			if("String".equals(type)){
				if(length > 6000){
					return "clob";
				}else{
					return "varchar2("+length+")";
				}
			}else if("Int".equals(type)){
				return "number";
			}else if("Double".equals(type)){
				return "number";
			}else if("Date".equals(type)){
				return "date";
			}else{
				throw new RuntimeException("类型 " + type + " 未定义。");
			}
		}
		return null;
	}
	
	/**
	 * 从数据源返回连接，数据源配置来源于 olap_obj_share 表
	 * @param dsourceId
	 * @return
	 * @throws Exception 
	 */
	public static Connection getConnByDatasource(String dsourceId, DaoHelper daoHelper, Map<String, String> extInfo) throws Exception{
		Connection conn = null;
		if(dsourceId != null && dsourceId.length() > 0){
			//采用用户定义的数据源进行连接，而不是采用系统连接
			String sql = "select content from olap_obj_share where id='"+dsourceId+"' and tp='dsource'";
			String str = (String)daoHelper.queryForObject(sql, String.class);
			JSONObject json = JSONObject.fromObject(str);
			DataService ser = new DataService();
			DataService.RSDataSource rsds = ser.json2datasource(json);
			if(rsds.getUse() != null && "jndi".equalsIgnoreCase(rsds.getUse())){
				conn = com.ruisi.vdop.ser.webreport.DBUtils.getConnection(rsds.getJdniname());
			}else{
				conn = com.ruisi.vdop.ser.webreport.DBUtils.getConnection(rsds.getUrl(), rsds.getName(), PasswordEncrypt.decode(rsds.getPsd()), rsds.getLinktype());
			}
			if(extInfo != null){
				extInfo.put("dbName", rsds.getType());
			}
		}else{
			conn = VDOPUtils.getConnection();
		}
		return conn;
	}
	
	public static Connection getConnByDatasource(String dsourceId, DaoHelper daoHelper) throws Exception{
		return getConnByDatasource(dsourceId, daoHelper, null);
	}
	
	public static List<DataService.DSColumn> queryMetaAndIncome(JSONObject dataset, DataService.RSDataSource rsds) throws Exception{
		List<String> tables = new ArrayList<String>();
		//需要进行关联的表
		JSONArray joinTabs = (JSONArray)dataset.get("joininfo");
		//生成sql
		StringBuffer sb = new StringBuffer("select a0.* ");
		//添加 列的分隔符，方便识别列是从哪个表来
		if(joinTabs!=null&&joinTabs.size() != 0){ //无关联表，不需要该字段
			sb.append(",'' a$idx ");
		}
		for(int i=0; joinTabs!=null&&i<joinTabs.size(); i++){
			sb.append(", a"+(i+1)+".* ");
			if(i != joinTabs.size() - 1){
				//添加 列的分隔符，方便识别列是从哪个表来
				sb.append(",'' a$idx");
			}
		}
		sb.append("from ");
		sb.append(dataset.getString("master") + " a0 ");
		tables.add(dataset.getString("master"));
		for(int i=0; joinTabs!=null&&i<joinTabs.size(); i++){
			JSONObject tab = joinTabs.getJSONObject(i);
			sb.append(", "+tab.getString("ref")+" a"+(i+1)+" ");
			tables.add(tab.getString("ref"));
		}
		sb.append("where 1=2 ");
		for(int i=0; joinTabs!=null&&i<joinTabs.size(); i++){
			JSONObject tab = joinTabs.getJSONObject(i);
			sb.append("and a0."+tab.getString("col")+"=a"+(i+1)+"."+tab.getString("refKey"));
			sb.append(" ");
		}
		
		Connection conn  = null;
		try {
			if(rsds == null){
				conn = VDOPUtils.getConnection();
			}else{
				String use = rsds.getUse();
				if(use == null || "jdbc".equals(use)){
					conn = com.ruisi.vdop.ser.webreport.DBUtils.getConnection(rsds.getUrl(), rsds.getName(), PasswordEncrypt.decode(rsds.getPsd()), rsds.getLinktype());
				}else{
					conn = com.ruisi.vdop.ser.webreport.DBUtils.getConnection(rsds.getJdniname());
				}
			}
			PreparedStatement ps = conn.prepareStatement(sb.toString());
			ResultSet rs = ps.executeQuery();
			
			ResultSetMetaData meta = rs.getMetaData();
			List<DataService.DSColumn> cols = new ArrayList<DataService.DSColumn>();
			String tname = tables.get(0);
			int idx = 1;
			for(int i=0; i<meta.getColumnCount(); i++){
				String name = meta.getColumnName(i+1);
				String tp = meta.getColumnTypeName(i+1);
				//遇到a$idx 表示字段做分割, 需要变换字段所属表信息
				if("a$idx".equalsIgnoreCase(name)){
					tname = tables.get(idx);
					idx++;
					continue;
				}
				tp = columnType2java(tp);
				DataService.DSColumn col = new DataService.DSColumn();
				col.setName(name);
				col.setType(tp);
				col.setTname(tname);
				cols.add(col);
			}
			rs.close();
			ps.close();
			return cols;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("sql 执行报错.");
		}finally{
			com.ruisi.vdop.ser.webreport.DBUtils.closeConnection(conn);
		}
	}
	
	public static List createEmptyHead(int size){
		List ls = new ArrayList();
		for(int i=0; i<size; i++){
			DataService.DSColumn ds = new DataService.DSColumn();
			ds.setDispName("c" + (i+ 1));
			ds.setName("c" + (i+ 1));
			ls.add(ds);
		}
		return ls;
	}
}
