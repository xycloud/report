package com.ruisi.vdop.web.frame;

import com.ruisi.vdop.util.VDOPUtils;

public class LogoutAction {
	private void doLogout(){
		VDOPUtils.removeLoginUser(VDOPUtils.getServletContext(), VDOPUtils.getSession(), false);
	}
	public String execute(){
			doLogout();
		return "success";
	}
	
}
