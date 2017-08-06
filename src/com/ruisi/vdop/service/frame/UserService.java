package com.ruisi.vdop.service.frame;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.sf.json.JSONArray;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.PreparedStatementCallback;

import com.ruisi.ext.engine.ExtConstants;
import com.ruisi.ext.engine.control.InputOption;
import com.ruisi.ext.engine.scan.ResultRef;
import com.ruisi.ext.engine.service.ServiceSupport;
import com.ruisi.vdop.bean.RoleMenuParm;
import com.ruisi.vdop.bean.User;
import com.ruisi.vdop.util.VDOPUtils;
import com.ruisi.vdop.util.VdopConstant;
/**
 * 用户管理的服务类,由于采用了EXT0配置框架，方法执行完后会默认返回到以类名-方法名.xml命名的xml配置页面
 * @author 毛子源
 *
 */
public class UserService extends ServiceSupport 
{
	/**
	 * 默认执行方法
	 */
	public void execute(InputOption option){
		
		
	}
	/**
	 * 显示新增用户需要填写的信息页面
	 * @param option
	 */
	
	public void preAdd(InputOption option){
		
	}
	/**
	 * 显示用户已关联的角色列表
	 * @param option
	 */
	public void	roleUserList(InputOption option){
		
	}
	/**
	 * 显示用户已授权的菜单
	 * @param option
	 */
	public void	roleMenuList(InputOption option){
		
	}
	/**
	 * 新增用户的方法，执行SQL
	 * @param option
	 */
	@ResultRef("frame.User")
	public void save(InputOption option) throws IOException
	{
//		String sql = "insert into sc_login_user values(user_id.nextval,?,?,?,?,?,?,?,?,?,now(),'','',?,now(),?,'1')";
//		daoHelper.execute(sql, new PreparedStatementCallback(){
//			public Object doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException 
//			{
//				String area_no = option.getParamValue("areaNo");
//				String city_no = option.getParamValue("cityNo");
//				String town_no = option.getParamValue("townNo");
//				String login_name = option.getParamValue("name");
//				String staff_id = option.getParamValue("staffId");
//				String gender = option.getParamValue("sex");
//				String mobile_phone = option.getParamValue("mobile");
//				String email = option.getParamValue("email");
//				String office_tel = option.getParamValue("offmobile");
//				String update_user = VDOPUtils.getLoginedUser(option.getRequest()).getStaffId();
//				
//			
//				ps.setString(1, area_no);
//				ps.setString(2, city_no);
//				ps.setString(3, town_no);
//				ps.setString(4, login_name);
//				ps.setString(5, "vdop");
//				ps.setString(6, gender);
//				ps.setString(7, mobile_phone);
//				ps.setString(8, email);
//				ps.setString(9, office_tel);
//				ps.setString(10, update_user);
//				ps.setString(11, staff_id);
//				ps.executeUpdate();
//				return null;
//			}
//		});
		User u = VDOPUtils.getNewUser();
		String dbName = VDOPUtils.getConstant(ExtConstants.dbName);
		if("oracle".equals(dbName)){
			u.setUserId(String.valueOf(VDOPUtils.getSEQ(this.servletContext)));
		}
		u.setLoginName(option.getParamValue("name"));
		u.setStaffId(option.getParamValue("staffId"));
		u.setGender(option.getParamValue("sex"));
		u.setMobilePhone(option.getParamValue("mobile"));
		u.setEmail(option.getParamValue("email"));
		u.setOfficeTel(option.getParamValue("offmobile"));
		u.setUpdateUser(VDOPUtils.getLoginedUser(option.getRequest()).getStaffId());
		//FIXME 设置默认密码为1
		u.setPassword(VDOPUtils.getEncodedStr(option.getParamValue("pwd")));
		u.setAreaNo(Integer.parseInt(option.getParamValue("areaNo")));
		u.setSiteId(1);
		u.setDbName(VDOPUtils.getConstant(ExtConstants.dbName));

		daoHelper.getSqlMapClientTemplate().insert("vdop.frame.user.adduser", u);
		
		super.sendRedirect(option, "frame.User", "", false);
	}
	/**
	 * 显示修改用户信息页面
	 * @param option
	 */
	public void preMod(InputOption option){
		/**
		//查询用户部门
		String dept = (String)this.daoHelper.getSqlMapClientTemplate().queryForObject("vdop.frame.user.getdeptName", option.getParamValue("user_id"));
		option.getRequest().setAttribute("dept", dept);
		**/
	}
	/**
	 * 修改用户信息的方法，执行SQL
	 * @param option
	 */
	@ResultRef("frame.User")
	public void mod(InputOption option) throws IOException
	{
//		String sql = "update sc_login_user set area_No=?,city_No=?,town_No=?,login_name=?,gender=?," +
//				"mobile_phone=?,email=?,office_tel=?,update_user=?,update_date=now(),staff_id=? where user_id=?";
//		daoHelper.execute(sql, new PreparedStatementCallback(){
//			public Object doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException 
//			{
//				String area_no = option.getParamValue("areaNo");
//				String city_no = option.getParamValue("cityNo");
//				String town_no = option.getParamValue("townNo");
//				String login_name = option.getParamValue("name");
//				String staff_id = option.getParamValue("staffId");
//				String gender = option.getParamValue("sex");
//				String mobile_phone = option.getParamValue("mobile");
//				String email = option.getParamValue("email");
//				String office_tel = option.getParamValue("offmobile");
//				String user_id = option.getParamValue("user_id");
//				String update_user = VDOPUtils.getLoginedUser(option.getRequest()).getStaffId();
//				ps.setString(1, area_no);
//				ps.setString(2, city_no);
//				ps.setString(3, town_no);
//				ps.setString(4, login_name);
//				ps.setString(5, gender);
//				ps.setString(6, mobile_phone);
//				ps.setString(7, email);
//				ps.setString(8, office_tel);
//				ps.setString(9, update_user);
//				ps.setString(10, staff_id);
//				ps.setString(11, user_id);
//				ps.executeUpdate();
//				return null;
//			}
//		});
//		
		User u = VDOPUtils.getNewUser();
		u.setLoginName(option.getParamValue("name"));
		u.setStaffId(option.getParamValue("staffId"));
		u.setGender(option.getParamValue("sex"));
		u.setMobilePhone(option.getParamValue("mobile"));
		u.setEmail(option.getParamValue("email"));
		u.setOfficeTel(option.getParamValue("offmobile"));
		u.setUserId(option.getRequest().getParameter("user_id"));
		u.setUpdateUser(VDOPUtils.getLoginedUser(option.getRequest()).getStaffId());
		u.setSiteId(1);
		daoHelper.getSqlMapClientTemplate().update("vdop.frame.user.updateuser",u);
		super.sendRedirect(option, "frame.User", "", false);
	}
	/**
	 * 删除用户信息的执行方法，执行SQL
	 * @param option
	 */
	@ResultRef("frame.User")
	public void del(InputOption option) throws IOException
	{
		
		String[] ids = option.getParamValues("user_id");
		for(final String tmp : ids)
		{//这个循环用于循环插入授权数据
			if(tmp.length() > 0)
			{
				User u = VDOPUtils.getNewUser();
				u.setUserId(tmp);
				daoHelper.getSqlMapClientTemplate().delete("vdop.frame.user.deleteuser",u);
				
				//删除用户菜单关系
				daoHelper.execute("delete from user_menu_rela where user_id = " + tmp);
				//删除用户角色关系
				daoHelper.execute("delete from role_user_rela where user_id = " + tmp);
				//删除用户报表关系
				daoHelper.execute("delete from user_report_rela where user_id = " + tmp);
			}
		}
		super.sendRedirect(option, "frame.User", "", false);
	}
}
