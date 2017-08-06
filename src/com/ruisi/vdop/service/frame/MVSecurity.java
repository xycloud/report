package com.ruisi.vdop.service.frame;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.PreparedStatementCallback;

import com.ruisi.ext.engine.dao.DaoHelper;
import com.ruisi.ext.engine.view.builder.BuilderInterceptor;
import com.ruisi.ext.engine.view.context.Element;
import com.ruisi.ext.engine.view.context.MVContext;
import com.ruisi.ext.engine.wrapper.ExtRequest;
import com.ruisi.vdop.bean.User;
import com.ruisi.vdop.util.VDOPUtils;
import com.ruisi.vdop.util.VdopConstant;

/**
 * mv的权限验证，用来对试用用户的页面进行拦截
 * @author hq
 * @date 2013-8-7
 */
public class MVSecurity implements BuilderInterceptor {
	
	/**
	 * 试用用户能访问的MV
	 */
	private String[] mvs = new String[]{
			"bi.src.src_data_analyse",
			"bi.src.all_data_appraise",
			"bi.src.data_segmen",
			"bi.month.month_income_trend",
			"bi.month.month_trend",
			"bi.month.m_data_segmen2",
			"bi.fd.branch_analyse_data",
			"bi.jz.jz_compaire_all",
			"bi.day.day_income_trend",
			"bi.jz.jz_compaire_all_sjl",
			"bi.jz.jz_compaire_all_zb"
	};
	
	private boolean exist(String id){
		boolean ret = false;
		for(String mv : mvs){
			if(mv.equals(id)){
				ret = true;
				break;
			}
		}
		return ret;
	}

	/**
	 * 写日志
	 */
	public void end(Element mv, ExtRequest req, DaoHelper dao) {
		/**
		//剔除admin
		User u = VDOPUtils.getLoginedUser(req);
		String uname = u.getStaffId();
		if("admin".equals(uname)){
			return;
		}
		MVContext ctx = null;
		if(mv instanceof MVContext){
			ctx = (MVContext)mv;
			
			final String uid = u.getUserId();
			final String ip = req.getRemoteAddr();
			final String mvid = ctx.getMvid();
			String sql = "insert into "+VdopConstant.getSysUser()+".sc_log_info(user_id,logDate, logIp, mvid) values(?,now(),?,?)";
			dao.execute(sql, new PreparedStatementCallback(){

				@Override
				public Object doInPreparedStatement(PreparedStatement ps)
						throws SQLException, DataAccessException {
					ps.setString(1, uid);
					ps.setString(2, ip);
					ps.setString(3, mvid);
					ps.executeUpdate();
					return null;
				}
				
			});
			
		}
		**/
	}

	@Override
	public void start(Element mv, ExtRequest req, DaoHelper dao) {
		/**
		User user = (User)req.getSession().getAttribute(VdopConstant.USER_KEY_IN_SESSION);
		int state = user.getState();
		if(state == 2){
			String mvId = ((MVContext)mv).getMvid();
			if(mvId.startsWith("bi.") && !this.exist(mvId)){
				throw new RuntimeException("试用账号只能访问部分功能，需要访问全部功能请联系我们销售人员。");
			}
		}
		**/
	}

}
