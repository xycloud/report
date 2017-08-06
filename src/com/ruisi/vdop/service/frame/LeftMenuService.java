package com.ruisi.vdop.service.frame;

import java.util.List;

import net.sf.json.JSONArray;

import com.ruisi.ext.engine.control.InputOption;
import com.ruisi.ext.engine.scan.ResultRef;
import com.ruisi.ext.engine.service.ServiceSupport;
import com.ruisi.vdop.bean.TreeNode;
import com.ruisi.vdop.util.VDOPUtils;
import com.ruisi.vdop.util.VdopConstant;

public class LeftMenuService extends ServiceSupport {

	public void execute(InputOption arg0)  {
		
	}
	
	@ResultRef("frame.LeftMenu")
	public void loadData(InputOption option) throws Exception{
		TreeNode node = new TreeNode();
		node.setUserId(VDOPUtils.getLoginedUser(option.getRequest()).getUserId());
		node.setPid(option.getParamValue("node"));
		List ls = this.daoHelper.getSqlMapClientTemplate().queryForList("vdop.frame.frame.queryUserMenu", node);
		String str = JSONArray.fromObject(ls).toString();
		option.getResponse().setContentType("text/html;charset=UTF-8");
		option.getResponse().getWriter().print(str);
		super.setNoResult(option.getRequest());
	}

	

}
