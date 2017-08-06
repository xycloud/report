package com.ruisi.vdop.service.frame;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.PreparedStatementCallback;

import com.ruisi.ext.engine.ExtConstants;
import com.ruisi.ext.engine.control.InputOption;
import com.ruisi.ext.engine.scan.ResultRef;
import com.ruisi.ext.engine.service.ServiceSupport;
import com.ruisi.ext.engine.wrapper.ExtRequest;
import com.ruisi.vdop.util.VDOPUtils;
import com.ruisi.vdop.util.VdopConstant;



public class MenuService extends ServiceSupport {
	
	public void list(InputOption option){
		
	}
	@ResultRef("frame.Menu")
	public void delete(InputOption option) throws Exception{
		super.setNoResult(option.getRequest());
		String id = option.getParamValue("id");
		String chkSql = "select count(*) from sc_menu where menu_pid = "+"'"+id+"'";
		BigDecimal ct = (BigDecimal)daoHelper.queryForObject(chkSql, BigDecimal.class);
		if(ct.intValue() > 0){
			option.getResponse().setStatus(500);
			throw new Exception();
		}
		
		String sql = "delete from sc_menu where menu_id = "+"'"+id+"'";
		daoHelper.execute(sql);
		
		//删除菜单角色关系
		daoHelper.execute("delete from role_menu_rela where menu_id = " + id);
		//删除菜单用户关系
		daoHelper.execute("delete from user_menu_rela where menu_id = " + id);
	}
	
	@ResultRef("frame.Menu")
	public void loadData(InputOption option) throws IOException{
		ExtRequest req = option.getRequest();
		String id = option.getParamValue("id");
		Map params=new HashMap();
		params.put("id",id);
		List ls = this.daoHelper.getSqlMapClientTemplate().queryForList("vdop.frame.menu.list",params);
		
		String str = JSONArray.fromObject(ls).toString();
		option.getResponse().setContentType("text/html; charset=UTF-8");
		option.getResponse().getWriter().print(str);
		super.setNoResult(req);
	}
	@ResultRef("frame.Menu")
	public  void saveMenu(final InputOption option) throws IOException{
		String idSql = "select max(menu_id) from sc_menu";
		Integer maxId = (Integer)daoHelper.queryForObject(idSql, Integer.class);
		final int mid = maxId.intValue() + 1;
		final String pid = option.getParamValue("pid");
		String dbName = VDOPUtils.getConstant(ExtConstants.dbName);
		String sql="insert into sc_menu(menu_id,menu_pid,menu_name,menu_desc,menu_date,menu_order,menu_url) values(?,?,?,?,";
		if("mysql".equalsIgnoreCase(dbName)){
			sql+="now()";
		}else if("oracle".equalsIgnoreCase(dbName)){
			sql += "sysdate";
		}else if("sqlser".equalsIgnoreCase(dbName)){
			sql += "getdate()";
		}
		sql += ",?,?)";
		daoHelper.execute(sql, new PreparedStatementCallback(){
			public Object doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
				ps.setInt(1, mid);
				ps.setString(3, option.getParamValue("name"));
				ps.setString(4, option.getParamValue("desc"));
				ps.setString(2, pid);
				ps.setInt(5, Integer.parseInt(option.getParamValue("order")));
				ps.setString(6, option.getParamValue("url"));
				ps.executeUpdate();
				return null;
			}
		});
		
		this.setNoResult(option.getRequest());
		option.getResponse().getWriter().print(mid);
	}
	@ResultRef("frame.Menu")
	public void updateMenu(final InputOption option) throws IOException{
		String dbName =  VDOPUtils.getConstant(ExtConstants.dbName);
		String dt = "";
		if("oracle".equals(dbName)){
			dt = "sysdate";
		}else if("mysql".equals(dbName)){
			dt = "now()";
		}else if("sqlser".equals(dbName)){
			dt = "GETDATE()";
		}
		String sql="update sc_menu set menu_name=?,menu_desc=?,menu_date="+dt+",menu_order=?,menu_url=? where menu_id=?";
		daoHelper.execute(sql, new PreparedStatementCallback(){
			public Object doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
				String order = option.getParamValue("order");
				if(order == null || order.length() == 0){
					ps.setInt(3, 0);
				}else{
					ps.setInt(3, Integer.parseInt(order));
				}
				ps.setString(2, option.getParamValue("desc"));
				ps.setString(1, option.getParamValue("name"));
				ps.setString(4, option.getParamValue("url"));
				ps.setString(5, option.getParamValue("id"));	
				ps.executeUpdate();
				return null;
			}
		});
		this.setNoResult(option.getRequest());
		option.getResponse().getWriter().print("{suc:true}");
	}
	@ResultRef("frame.Menu")
	public void getMenu(InputOption option) throws IOException{
		String menuId = option.getParamValue("id");
		String sql="select menu_id \"id\",menu_pid \"pid\",menu_name \"name\",menu_desc \"desc\",menu_order \"order\",menu_url \"url\" from sc_menu where menu_id="+"'"+menuId+"'";
		Map ret = this.daoHelper.queryForMap(sql);
		String str = JSONObject.fromObject(ret).toString();
		option.getResponse().setContentType("text/html;charset=UTF-8");
		option.getResponse().getWriter().print(str);
		super.setNoResult(option.getRequest());
	}

	public void execute(InputOption option) {
		
	}
	
}
