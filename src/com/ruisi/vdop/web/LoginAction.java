package com.ruisi.vdop.web;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;

import com.ruisi.ext.engine.ExtConstants;
import com.ruisi.ext.engine.view.context.ExtContext;
import com.ruisi.vdop.bean.User;
import com.ruisi.vdop.service.frame.LoginLogServ;
import com.ruisi.vdop.service.frame.LoginServ;
import com.ruisi.vdop.util.SysUserBase;
import com.ruisi.vdop.util.VDOPUtils;

public class LoginAction extends SysUserBase {
	private String userName;
	private String password;
	private String errorInfo;
	private String rurl; //登陆后跳转地址
	private String inputUrl; //登录来源网址
	private boolean isEncoded = VDOPUtils.getIsEncoding();
	private String dbName = VDOPUtils.getConstant("dbName");
	
	public String execute() throws IOException{
		if(VDOPUtils.getLoginedUser() != null){
			//用户已经登录，跳转到主页面
			VDOPUtils.getResponse().sendRedirect("frame/Frame3.action");
		}
		return "success";
	}

	public String login() throws IOException, ServletException {

		boolean returnStatus = doLogin();
		if (!returnStatus) {
			
			
			if(inputUrl == null || inputUrl.length() == 0){
				return "success";
			}else{
				VDOPUtils.getRequest().getRequestDispatcher(inputUrl).forward(VDOPUtils.getRequest(), VDOPUtils.getResponse());
				return null;
			}
		}else{
			if(rurl == null || rurl.length() == 0){
				return "login";
			}else{
				VDOPUtils.getResponse().sendRedirect(rurl);
				return null;
			}
		}
	}

	// 进行身份验证
	private boolean doLogin() {

		if (userName == null || "".equals(userName.trim()))
			return false;
		if (password == null || "".equals(password.trim()))
			return false;
		List loginUser = LoginServ.getUserInfo(this);
			//daoHelper.getSqlMapClientTemplate().queryForList(
			//	"vdop.frame.login", this);

		if (loginUser == null || loginUser.size() == 0) {
			errorInfo = "账号不存在，请确认账号是否输入正确！";
			return false;
		}
		//Map map = (Map) loginUser.get(0);
		User user=(User)loginUser.get(0);

		if (user == null) {
			errorInfo = "账号不存在，请确认账号是否输入正确！";
			return false;
		}
		
		if(user.getState() == 0){
			errorInfo = "账号已停用，请联系销售服务人员。";
			return false;
		}
		
		//判断账号是否过期
		/**
		Date dt = new Date();
		if(user.getEnddate().getTime() < dt.getTime()){
			errorInfo = "账号已过有效期，请联系销售服务人员。";
			return false;
		}
		**/
		
		//String temName = (String) map.get("staffId");
		String temName=user.getStaffId();

		if (temName == null) {
			return false;
		}
		boolean flag = false;

		if (temName.equals(userName)) {// 开始进行身份验证

			//String temPassword = (String) map.get("password");
			String temPassword=user.getPassword();

			if (temPassword == null)
				return false;

			if (!isEncoded) {// 没加密时的验证

				if (temPassword.equals(password)) {

					flag = true;
				} else {
					flag = false;
				}
			} else {// 加密时的验证

				String s = VDOPUtils.getEncodedStr(password);

				if (temPassword.equals(s)) {
					flag = true;
				} else {
					flag = false;
					errorInfo = "口令输入错误！";
				}
			}

			// 登录成功，把用户信息写入session
			if (flag) {
				User u = user;
				u.setStaffId(userName);
				 u.setLoginTime(Calendar.getInstance().getTime());
				u.setLastActive(Calendar.getInstance().getTime());
				u.setLoginIp(VDOPUtils.getRequest().getRemoteAddr());
				u.setSessionId(VDOPUtils.getSession().getId());
				VDOPUtils.saveLoginedUser(u, false, VDOPUtils.getRequest(), VDOPUtils.getServletContext());// user 对像存入session
				LoginLogServ.updateLogInfo(u);// 记登录日志
			}
		} else {// 出错时的处理
			errorInfo = "服务器内部错误！";
			return false;
		}
		return flag;

	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getErrorInfo() {
		return errorInfo;
	}

	public void setErrorInfo(String errorInfo) {
		this.errorInfo = errorInfo;
	}

	public String getRurl() {
		return rurl;
	}

	public void setRurl(String rurl) {
		this.rurl = rurl;
	}

	public String getInputUrl() {
		return inputUrl;
	}

	public void setInputUrl(String inputUrl) {
		this.inputUrl = inputUrl;
	}

	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

}
