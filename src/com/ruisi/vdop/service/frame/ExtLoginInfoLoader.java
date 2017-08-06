package com.ruisi.vdop.service.frame;

import java.util.HashMap;
import java.util.Map;

import com.ruisi.ext.engine.dao.DaoHelper;
import com.ruisi.ext.engine.service.loginuser.LoginUserInfoLoader;
import com.ruisi.ext.engine.wrapper.ExtRequest;
import com.ruisi.vdop.bean.User;
import com.ruisi.vdop.util.VdopConstant;

/**
 * ext 获取登录信息的方法
 * @author hq
 * @date Mar 25, 2010
 */
public class ExtLoginInfoLoader  implements LoginUserInfoLoader {

	public String getUserId() {
		return null;
	}

	public Map<String, Object> loadUserInfo(ExtRequest request, DaoHelper dao) {
		User user = (User)request.getSession().getAttribute(VdopConstant.USER_KEY_IN_SESSION);
		if(user == null){
			user = (User)request.getSession().getAttribute(VdopConstant.USER_KEY_IN_SESSION_3G);
		}
		Map<String, Object> m = new HashMap();
		m.put("userId", user.getUserId());
		m.put("staffId", user.getStaffId());
		m.put("siteId", user.getSiteId());
		m.put("state", user.getState());
		
		m.put("defDay", user.getDefDay());
		m.put("defMonth", user.getDefMonth());
		
		//设置竞争对手
		int sid = user.getSiteId();
		if(sid == 1){
			m.put("jzids", "1,2,5");
		}
		if(sid == 2){
			m.put("jzids", "2,1,5");
		}
		if(sid == 3){
			m.put("jzids", "3,2,4");
		}
		if(sid == 4){
			m.put("jzids", "4,3,2");
		}
		if(sid == 5){
			m.put("jzids", "5,2,1");
		}
		if(sid == 7){
			m.put("jzids", "7,4,3");
		}
		if(sid == 8){
			m.put("jzids", "8,2,4");
		}
		
		return m;
	}

}
