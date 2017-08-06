<%@ page language="java" contentType="text/html;charset=UTF-8" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>餐饮连锁企业经营分析系统 - 演示系统 - 睿思BI</title>
<meta name="keywords" content="经营分析系统,餐饮连锁企业商业智能分析系统(BI),北京睿思科技有限公司">
<meta name="description" content="餐饮连锁企业经营分析系统以数据仓库技术为依托，采用ETL抽取企业在经营过程中产生的业务数据并集中于总部数据中心，利用数据挖掘、多维分析、智能报表等技术构建商业智能平台，为管理者提供及时、准确、科学的决策依据，降低管理成本，助力构建智慧型企业。">

<link rel="stylesheet" href="../resource/css/ht.css" type="text/css">
<script type="text/javascript" src="../ext-res/js/jquery.js"></script>
<script type="text/javascript" src="../ext-res/js/ext-base.js"></script>

<script language="javascript" type="text/javascript">
	jQuery(function(){
		location.href = 'NFrame.action';
	});
	
	/**
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
		var h = win.height() - 159;
		jQuery('#maininfo').attr("height", h);
	});
	
	jQuery(window).resize(function() {
  	var win = jQuery(window);
		var h = win.height() - 159;
		jQuery('#maininfo').attr("height", h);
	});

	**/
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
    	<div style="margin-left:40px;">
    	您好：<font color="#990000">${uinfo.loginName}</font>
    	<%
    	com.ruisi.vdop.bean.User user = (com.ruisi.vdop.bean.User)request.getAttribute("uinfo");
    	int state = user.getState();
    	if(state == 2){
    	 out.print("(试用账号)");
    	}
    	%>
    	 欢迎登录。
    	 	<div style="margin:5px 0px 0px 0px; width:400px; color:#990000;">
    	 		<MARQUEE scrollamount=2>当前系统为演示系统，主要演示系统功能，分析数据为测试数据，不具有正确性，请知晓。</MARQUEE>
		</div>
    	 </div>
    	 </td>
    <td valign="middle" align="right">
    	<div class="uinfo"><a href="javascript:getuserinfo()">账号信息</a> &nbsp; &nbsp; <a href="javascript:chgpasswd()">修改密码</a></div>
        <div class="uinfo"><a href="javascript:getfankui()">用户反馈</a> &nbsp; &nbsp; <a href="javascript:logout()">退出登录</a></div>
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
		<s:if test="#e.menu_id == #request.mid">
        <div class="selected">
        <a href="Frame.action?mid=${e.menu_id}">${e.menu_name}</a> 
        </div>
        
        <%
		Map m = (Map)pageContext.findAttribute("e");
		rootlink = (Integer)m.get("rootlink");
		linkUrl = (String)m.get("menu_url");
		%>
        
        </s:if>
        <s:if test="#e.menu_id != #request.mid">
        <a href="Frame.action?mid=${e.menu_id}">${e.menu_name}</a> 
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
<div class="xinh-head"></div>
</div>

<%
if(rootlink != null && rootlink == 1){
%>

<div class="xinh-2">
 <div class="xinh-2-2" style="width:965px;">
    <table width="965" border="0" cellpadding="0" cellspacing="0" class="xinh-2-2-1-qp">
  <tr>
    <td height="5"></td>
  </tr>
</table>
    	
    	<div class="xinh-2-2-2" style="width:958px;">
         <iframe id="maininfo" frameborder="0" width="100%" height="495" src="<%=linkUrl%>"></iframe>
	    </div>
    	
        
        <div class="xinh-2-2-3-qp" style="965px;"></div>
    </div>
</div>

<%	
}else{
%>

<div class="xinh-2">
   <div class="xinh-2-1">
	<div class="xinh-2-1-1" id="pageMenu" style="display:;">


<s:iterator var="e" value="#request.subMenu" status="statu">
	<div class="xinh-2-1-2 xin-an" id="admin_page" <s:if test="#statu.index == 0">style="background-image: url(../resource/img/houtai_23.jpg);"</s:if>>
		<a  href="javascript:;" onclick="gotourl('${e.url}', this)">${e.text}</a>
	</div>
</s:iterator>	
	
</div>

   
   </div>
   <div class="xinh-2-2">
    <table width="831" border="0" cellpadding="0" cellspacing="0" class="xinh-2-2-1">
  <tr>
    <td height="5"></td>
  </tr>
</table>
    	
    	<div class="xinh-2-2-2">
         <iframe id="maininfo" frameborder="0" width="100%" height="495" src="${secMenu.url}"></iframe>
	    </div>
    	
        
        <div class="xinh-2-2-3"></div>
    </div>
</div>
<%}%>

<div id="xinh-3" class="xinh-3">
<a href="http://www.rosetech.cn" target="_blank">
北京睿思科技有限公司
</a>
© 2013 
<a href="http://www.rosetech.cn" target="_blank">www.rosetech.cn</a> <a href="../kefu.html" target="_blank" style="color:#666; text-decoration:underline">联系我们</a><br>

</div>

</body>
</html>
