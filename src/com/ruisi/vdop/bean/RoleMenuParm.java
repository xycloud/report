package com.ruisi.vdop.bean;

import com.ruisi.vdop.util.SysUserBase;

/**
 * 这个类是一个参数实体类，用于封装传给ibatis的sqlmap的参数，封装的参数有3个
 * @author 毛子源
 *
 */
public class RoleMenuParm extends SysUserBase
{
	private String user_id;
	private String menu_pid;
	private String role_id;
	public RoleMenuParm(String user_id, String menu_pid, String role_id) 
	{
	
		this.user_id = user_id;
		this.menu_pid = menu_pid;
		this.role_id = role_id;
	}
	public String getUser_id() {
		return user_id;
	}
	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}
	public String getMenu_pid() {
		return menu_pid;
	}
	public void setMenu_pid(String menu_pid) {
		this.menu_pid = menu_pid;
	}
	public String getRole_id() {
		return role_id;
	}
	public void setRole_id(String role_id) {
		this.role_id = role_id;
	}
	
}
