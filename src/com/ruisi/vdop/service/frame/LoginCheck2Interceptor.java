package com.ruisi.vdop.service.frame;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import com.ruisi.vdop.bean.User;
import com.ruisi.vdop.util.VDOPUtils;
import com.ruisi.vdop.util.VdopConstant;

/**
 * 判断用户是否登录的拦截器
 * @author hq
 * @date Mar 24, 2010
 */
public class LoginCheck2Interceptor extends AbstractInterceptor  {

	public String intercept(ActionInvocation arg0) throws Exception {		
		User user = VDOPUtils.getLoginedUser(true);
		if(user == null){
			return "noLogin2";
		}else{
			
			return arg0.invoke();
		}
	}

}
