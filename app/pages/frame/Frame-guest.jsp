<%@ page language="java" contentType="text/html;charset=UTF-8" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>睿思BI商业智能分析系统</title>
<link rel="shortcut icon" type="image/x-icon" href="../resource/img/rs_favicon.ico">
<link rel="stylesheet" href="../resource/css/ht.css" type="text/css">
<link rel="stylesheet" type="text/css" href="../resource/jquery-easyui-1.3.4/themes/gray/easyui.css">
<link rel="stylesheet" type="text/css" href="../resource/jquery-easyui-1.3.4/themes/icon.css">
<script type="text/javascript" src="../ext-res/js/jquery.js"></script>
<script type="text/javascript" src="../resource/jquery-easyui-1.3.4/jquery.easyui.min.js"></script>
<script type="text/javascript" src="../ext-res/js/ext-base.js"></script>


<script language="javascript" type="text/javascript">
	
	function gotourl(url, ts){
		jQuery('.xin-an').each(function(){
			jQuery(this).css("background-image", "url(../resource/img/houtai_15.jpg)");
		});
		jQuery(ts).parent().css("background-image", "url(../resource/img/houtai_23.jpg)");
		jQuery('#maininfo').attr("src", url);
	}
	function logout(){
		var u = "Logout.action";
		if(confirm('是否确认退出登录？')){
			location.href = 'Logout.action';
		}
	}
	function closeMe(){
		var obj = jQuery("#id_panel");
		obj.remove();
		showMark(false);
	}
	function chgpasswd(){
		closeMe();
		showMark(true);
		var obj = jQuery("#id_panel");
		var url = "../control/extControl?serviceid=frame.Password";
		var str = "<div class='pw_panel' id='id_panel'><iframe id='i_pswd' frameborder=\"0\" width=\"100%\" height=\"100%\" src=\""+url+"\"></iframe></div>";
		obj = jQuery(str);
		obj.appendTo("body");
		var doc = jQuery(document);
		var win = jQuery(window);
		var t = doc.scrollTop() + win.height()/2 - 70;
		var l = doc.scrollLeft() + win.width()/2 - 200;
		obj.css({'top':t, 'left':l});
		obj.css("display", "block");
	}
	
	function getfankui(){
		closeMe();
		showMark(true);
		var obj = jQuery("#id_panel");
		var url = "../control/extControl?serviceid=appraise.Feedback";
		var str = "<div class='pw_panel' style='width:480px;height:280px;' id='id_panel'><iframe id='i_pswd' frameborder=\"0\" width=\"100%\" height=\"100%\" src=\""+url+"\"></iframe></div>";
		obj = jQuery(str);
		obj.appendTo("body");
		var doc = jQuery(document);
		var win = jQuery(window);
		var t = doc.scrollTop() + win.height()/2 - 140;
		var l = doc.scrollLeft() + win.width()/2 - 250;
		obj.css({'top':t, 'left':l});
		obj.css("display", "block");
	}
	
	function getuserinfo(){
		closeMe();
		showMark(true);
		var obj = jQuery("#id_panel");
		var url = "../control/extControl?serviceid=frame.UserInfo";
		var str = "<div class='pw_panel' style='width:480px;height:280px;' id='id_panel'><iframe id='i_pswd' frameborder=\"0\" width=\"100%\" height=\"100%\" src=\""+url+"\"></iframe></div>";
		obj = jQuery(str);
		obj.appendTo("body");
		var doc = jQuery(document);
		var win = jQuery(window);
		var t = doc.scrollTop() + win.height()/2 - 140;
		var l = doc.scrollLeft() + win.width()/2 - 250;
		obj.css({'top':t, 'left':l});
		obj.css("display", "block");
	}
	
	jQuery(function(){
		var win = jQuery(window);
		var h = win.height() - 102;
		jQuery('#mylayout').css("height", h+"px");
		jQuery("#mylayout").layout();
		//初始化tree
		jQuery("#menutree").tree({
			data: ${subMenu},
			onSelect:function(node){
				if(node.attributes){
					if(node.attributes.rfile == null){
						document.getElementById("syspageinfo").src = "../bireport/ReportDesign!viewOlapReport.action?pageId="+node.id+"&showtit=false";
					}else{
						var u = node.attributes.rfile;
						document.getElementById("syspageinfo").src = "../control/extView?mvid=usave." + u;
					}
					$('#mylayout').layout('panel','center').panel("setTitle", node.text);  
				}else{
					//节点展开
					if(node.state == 'closed'){
						jQuery("#menutree").tree("expand", node.target);
					}else if(node.state == 'open'){
						jQuery("#menutree").tree("collapse", node.target);
					}
				}
			}
		});
		var ls = $("#menutree").tree("getRoots");
		if(ls && ls.length > 0){
			$("#menutree").tree("select", ls[0].target);
		}
	});
	
	jQuery(window).resize(function() {
  		var win = jQuery(window);
		var h = win.height() - 102;
		jQuery('#mylayout').css("height", h+"px");
		//jQuery("#mylayout").layout();
	});

</script>
<style>
.uinfo {
	margin:5px 10px 5px 5px;
}
.uinfo a {
	text-decoration:underline;
	color:#333333;
}
.uinfo a:hover {
	color:#900;
}
.pw_panel {
	position:absolute;
	width:350px;
	height:150px;
	display:block;
	border: solid 1px #333333;
	background-color:#FFF;
	z-index:1001;
}
#menutree .tree-node{
	height:22px;
}
#menutree .tree-title {
	font-size:14px;
	font-family:Verdana, Geneva, sans-serif;
}
</style>
 
</head>

<body>


<div class="xinh-1">
  <div id="xinh-1-1" class="xinh-1-1">
  
  <div class="xinh-1-1-left">

  </div>
    <div class="xinh-1-1-right" align="right">
  <table width="100%" border="0" cellspacing="0" cellpadding="0" height="100%">
  <tr>
    <td valign="middle" align="left">
    	<div style="margin-left:80px;">
    	您好：<font color="#990000">${uinfo.loginName}</font>
    	<%
    	com.ruisi.vdop.bean.User user = (com.ruisi.vdop.bean.User)request.getAttribute("uinfo");
    	int state = user.getState();
    	if(state == 2){
    	 out.print("(试用账号)");
    	}
    	%>
    	 欢迎登录。
    	 	<div style="margin:5px 0px 0px 0px; width:100px; color:#990000;">
    	 		
		</div>
    	 </div>
    	 </td>
    <td valign="middle" align="right">
    	<div class="uinfo"><a href="Frame3.action">管理视图</a> | <a href="javascript:chgpasswd()">修改密码</a></div>
        <div class="uinfo"><a href="javascript:getuserinfo()">账号信息</a> | <a href="javascript:logout()">退出登录</a></div>
    </td>
  </tr>
</table>
  </div>

  
  </div>
  <div id="xinh-1-2" class="xinh-1-2">
  
  <%
  Integer rootlink = null;
  String linkUrl = "";
  
  %>

<s:iterator var="e" value="#request.menu" status="statu">
  <div id="xinh-1-2-1" class="xinh-1-2-1">
      <table width="2" cellspacing="0" cellpadding="0" border="0">
        <tbody><tr>
          <td width="1" height="26" bgcolor="#000000"/>
          <td width="1" bgcolor="#ffffff"/>
        </tr>
      </tbody></table>
    </div> 
        <div id="goodsActuator" class="xinh-1-2-2 xin-an1">
		<s:if test="#e.id == #request.mid">
        <div class="selected">
        <a href="Frame!guest.action?mid=${e.id}">${e.text}</a> 
        </div>
        
        <%
		Map m = (Map)pageContext.findAttribute("e");
		rootlink = (Integer)m.get("rootlink");
		linkUrl = (String)m.get("menu_url");
		%>
        
        </s:if>
        <s:if test="#e.id != #request.mid">
        <a href="Frame!guest.action?mid=${e.id}">${e.text}</a> 
        </s:if>
        </div>
</s:iterator>

    <div class="xinh-1-2-1" id="xinh-1-2-1">
      <table cellspacing="0" cellpadding="0" border="0" width="2">
        <tbody><tr>
          <td bgcolor="#000000" width="1" height="26">
          </td><td bgcolor="#ffffff" width="1">
        </td></tr>
      </tbody></table>
    </div>
    
    </div>
</div>

<div class="xinh-2">
<div id="mylayout" style="width:100%;" >
	<div data-options="region:'west',split:true,title:'二级菜单'"  style="width:180px;">
    	 <ul id="menutree"></ul>
    </div>
    
	<div data-options="region:'center',title:'展示页面'" id="optarea">
    		<iframe id="syspageinfo" frameborder="0" width="99%" height="99%"></iframe>
	</div>
</div>
</div>

<div id="xinh-3" class="xinh-3">
<a href="http://www.ruisitech.com" target="_blank">
北京睿思科技有限公司
</a>
© 2014 
<a href="http://www.ruisitech.com" target="_blank">www.ruisitech.com</a> <a href="../kefu.html" target="_blank" style="color:#666; text-decoration:underline">联系我们</a><br>

</div>

</body>
</html>
