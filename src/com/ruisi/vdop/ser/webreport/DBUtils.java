package com.ruisi.vdop.ser.webreport;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.ruisi.ext.engine.ExtConstants;
import com.ruisi.ext.engine.util.PasswordEncrypt;
import com.ruisi.vdop.util.VDOPUtils;


public class DBUtils {
	
	private static Logger log = Logger.getLogger(DBUtils.class);
	
	public static boolean testConnection(String url, String name, String psd, String clazz, StringBuffer msg) {
		boolean ret = false;
		Connection conn = null;
		try {
			Class.forName(clazz).newInstance();
			conn= DriverManager.getConnection(url,name, psd);
			if(conn != null){
				ret = true;
			}else{
				ret = false;
			}
		} catch (Exception e) {
			ret = false;
			if(msg != null){
				msg.append(e.getMessage());
			}
			log.error("JDBC测试出错。", e);
		}finally{
			closeConnection(conn);
		}
		return ret;
	}
	
	public static boolean testJndi(String jndiname, StringBuffer msg){
		boolean ret = false;
		Connection con = null;
		try{
		  	Context ctx = new InitialContext();      
		    String strLookup = "java:comp/env/"+jndiname; 
		    DataSource ds =(DataSource) ctx.lookup(strLookup);
		    con = ds.getConnection();
		    if (con != null){
		       ret = true;
		    }else{
		    	ret = false;
		    }
		}catch (Exception e) {
			log.error("JNDI测试出错", e);
			ret = false;
		}finally{
			closeConnection(con);
		}
		return ret;
	}
	
	public static void closeConnection(Connection conn){
		if(conn != null){
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static Connection getConnection(JSONObject ds) throws Exception{
		Connection conn  = null;
		DataService ser = new DataService();
		DataService.RSDataSource rsds = ser.json2datasource(ds);
		
		//使用自定义数据源
		if(rsds.getUse() != null && "jndi".equalsIgnoreCase(rsds.getUse())){
			conn = getConnection(rsds.getJdniname());
		}else{
			conn = getConnection(rsds.getUrl(), rsds.getName(), PasswordEncrypt.decode(rsds.getPsd()), rsds.getLinktype());
		}
		return conn;
	}
	
	public static Connection getConnection(String url, String name, String psd, String clazz) throws Exception{
		try {
			Connection conn = null;
			Class.forName(clazz).newInstance();
			conn= DriverManager.getConnection(url,name, psd);
			return conn;
		} catch (Exception e) {
			throw e;
		}
	}
	
	public static Connection getConnection(String jndiname) throws Exception{
		Connection con = null;
		try {
			Context ctx = new InitialContext();      
		    String strLookup = "java:comp/env/"+jndiname; 
		    DataSource ds =(DataSource) ctx.lookup(strLookup);
		    con = ds.getConnection();
		}catch(Exception ex){
			ex.printStackTrace();
			throw ex;
		}
	    return con;
	}
	
	public static List<DataService.DSColumn> queryMeta(String sql, DataService.RSDataSource rsds) throws Exception{
		Connection conn  = null;
		try {
			if(rsds == null){
				//使用默认系统数据源
				conn = VDOPUtils.getConnection();
			}else{
				//使用自定义数据源
				if(rsds.getUse() != null && "jndi".equalsIgnoreCase(rsds.getUse())){
					conn = getConnection(rsds.getJdniname());
				}else{
					conn = getConnection(rsds.getUrl(), rsds.getName(), PasswordEncrypt.decode(rsds.getPsd()), rsds.getLinktype());
				}
			}
			PreparedStatement ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			
			ResultSetMetaData meta = rs.getMetaData();
			List<DataService.DSColumn> cols = new ArrayList<DataService.DSColumn>();
			for(int i=0; i<meta.getColumnCount(); i++){
				String name = meta.getColumnName(i+1);
				String tp = meta.getColumnTypeName(i+1);
				//tp转换
				tp = com.ruisi.vdop.ser.report.DBUtils.columnType2java(tp);
				DataService.DSColumn col = new DataService.DSColumn();
				col.setName(name);
				col.setType(tp);
				cols.add(col);
			}
			rs.close();
			ps.close();
			return cols;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("sql 执行报错.");
		}finally{
			closeConnection(conn);
		}
	}
	
	/**
	 * 在测试SQL前，需要对参数velocity脚本进行解析
	 * @return
	 * @throws IOException 
	 * @throws ResourceNotFoundException 
	 * @throws MethodInvocationException 
	 * @throws ParseErrorException 
	 */
	public static String evaluateSql(String sql, JSONArray params, JSONArray vars) throws Exception{
		//在测试SQL前，需要对参数velocity脚本进行解析
		VelocityContext ctx = new VelocityContext();
		for(int i=0; i<params.size(); i++){
			JSONObject p = (JSONObject)params.get(i);
			String defvalType = (String)p.get("defvalType");
			if("dtz".equals(defvalType)){
				//动态值，查找变量
				JSONObject var = PageService.findVarById(vars, p.getString("defvalRef"));
				String valtype = (String)var.get("valtype");
				if("dtz".equals(valtype)){
					ctx.put(p.getString("id"), "");
				}else{
					ctx.put(p.getString("id"), var.get("value"));
				}
			}else{
				//是静态值，直接把默认值覆过去
				ctx.put(p.getString("id"), p.get("defvalue"));
			}
		}
		StringWriter sw = new StringWriter();
		StringReader sr = new StringReader(sql);
		Velocity.evaluate(ctx, sw, ExtConstants.velocityEncode, sr);
		
		sql = sw.toString();
		return sql;
	}
	
	/**
	 * 测试SQL,同时返回SQL的 METADATA
	 * @param sql
	 * @param rsds
	 * @return
	 * @throws Exception
	 */
	public static boolean testSql(String sql, DataService.RSDataSource rsds) throws Exception{
		Connection conn  = null;
		try {
			if(rsds == null){
				//使用默认系统数据源
				conn = VDOPUtils.getConnection();
			}else{
				if(rsds.getUse() != null && "jndi".equalsIgnoreCase(rsds.getUse())){
					conn = getConnection(rsds.getJdniname());
				}else{
					conn = getConnection(rsds.getUrl(), rsds.getName(), PasswordEncrypt.decode(rsds.getPsd()), rsds.getLinktype());
				}
			}
			PreparedStatement ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			rs.close();
			ps.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}finally{
			closeConnection(conn);
		}
	}
	
	public static List queryTopN(String sql, DataService.RSDataSource rsds, int n) throws Exception{
		Connection conn  = null;
		try {
			List ret = new ArrayList();
			if(rsds == null){
				conn = VDOPUtils.getConnection();
			}else{
				conn = getConnection(rsds.getUrl(), rsds.getName(),  PasswordEncrypt.decode(rsds.getPsd()), rsds.getLinktype());
			}
			PreparedStatement ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			
			ResultSetMetaData meta = rs.getMetaData();
			List<String> cols = new ArrayList<String>();
			for(int i=0; i<meta.getColumnCount(); i++){
				String name = meta.getColumnName(i+1);
				cols.add(name);
			}
			ret.add(cols);
			int idx = 0;
			while(rs.next() && idx <= n){
				Map<String, Object> m = new HashMap<String, Object>();
				for(String s : cols){
					m.put(s, rs.getString(s));
				}
				ret.add(m);
				idx++;
			}
			rs.close();
			ps.close();
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}finally{
			closeConnection(conn);
		}
	}
	
	public static String createUrl(String linktype, String ip, String port, String dbname){
		String url = "";
		if(linktype.equals("mysql")){
			url = "jdbc:mysql://"+ip+":"+port+"/"+dbname+"?useUnicode=true&characterEncoding=UTF8";
		}else if(linktype.equals("oracle")){
			url = "jdbc:oracle:thin:@"+ip+":"+port+":" + dbname;
		}else if(linktype.equals("sqlserver")){
			url = "jdbc:jtds:sqlserver://"+ip+":"+port+"/" + dbname;
		}
		return url;
	}
	/**
	public String tableExist(String tname, ){
		return null
	}
	**/
}
