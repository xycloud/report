package com.ruisi.vdop.service.frame;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ruisi.ext.engine.ExtConstants;
import com.ruisi.vdop.bean.User;
import com.ruisi.vdop.util.VDOPUtils;
import com.ruisi.vdop.util.VdopConstant;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

/**
 * 判断用户是否登录的拦截器
 * @author hq
 * @date Mar 24, 2010
 */
public class LoginCheckInterceptor extends AbstractInterceptor  {

	public String intercept(ActionInvocation arg0) throws Exception {		
		User user = VDOPUtils.getLoginedUser();
		if(user == null){
			return "noLogin";
		}else{
			
			return arg0.invoke();
		}
	}

}
