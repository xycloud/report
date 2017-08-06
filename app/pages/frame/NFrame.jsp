<%@ page language="java" contentType="text/html;charset=UTF-8" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>睿思BI - 商业智能产品演示系统</title>
<link rel="shortcut icon" type="image/x-icon" href="../resource/img/rs_favicon.ico">
<meta name="keywords" content="经营分析系统,餐饮连锁企业商业智能分析系统(BI),北京睿思科技有限公司">
<meta name="description" content="餐饮连锁企业经营分析系统以数据仓库技术为依托，采用ETL抽取企业在经营过程中产生的业务数据并集中于总部数据中心，利用数据挖掘、多维分析、智能报表等技术构建商业智能平台，为管理者提供及时、准确、科学的决策依据，降低管理成本，助力构建智慧型企业。">
<link rel="stylesheet" type="text/css" href="../resource/css/main.css">
<link rel="stylesheet" type="text/css" href="../resource/jquery-easyui-1.3.4/themes/default/easyui.css">
<link rel="stylesheet" type="text/css" href="../resource/jquery-easyui-1.3.4/themes/icon.css">
<script type="text/javascript" src="../ext-res/js/jquery.min.js"></script>
<script type="text/javascript" src="../resource/jquery-easyui-1.3.4/jquery.easyui.min.js"></script>
<script type="text/javascript" src="../ext-res/js/ext-base.js"></script>
</head>

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
		var str = "<div class='pw_panel' style='width:480px;height:300px;' id='id_panel'><iframe id='i_pswd' frameborder=\"0\" width=\"100%\" height=\"100%\" src=\""+url+"\"></iframe></div>";
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
	var isfirst = false;
	jQuery(function(){
		jQuery("#sysmenu").tree({
			url:"NFrame!tree.action",
			onLoadSuccess:function(node, data){
				if(isfirst == false){
					if(data.length > 0){
						document.getElementById("maininfo").src = data[0].attributes.url;
					}
				}
				isfirst = true;
			},
			onClick:function(node){
				if(node.attributes.url && node.attributes.url.length > 0){
					var u = node.attributes.url;
					if(u.indexOf('?') > 0){
						u = u + "&t="+Math.random();
					}else{
						u = u + "?t="+Math.random();
					}
					document.getElementById("maininfo").src = u;
				}else{
					//节点展开
					if(node.state == 'closed'){
						jQuery("#sysmenu").tree("expand", node.target);
					}else if(node.state == 'open'){
						jQuery("#sysmenu").tree("collapse", node.target);
					}
				}
			}
		});
	});
	jQuery(function(){
		jQuery("#usercnt").load("Frame!onlineUser.action?T="+Math.random());
		window.setInterval(function(){
			jQuery("#usercnt").load("Frame!onlineUser.action?T="+Math.random());
		},5000);
	});
</script>
<style>
<!--
#sysmenu .tree-node{
	height:22px;
}
#sysmenu .tree-title {
	font-size:14px;
	font-family:Verdana, Geneva, sans-serif;
}
-->
</style>

<body class="easyui-layout">
	<div data-options="region:'north',border:false" style="height:57px; overflow:hidden; background-color:#E6EEF8;">
    	<div class="pheader">
        <table width="100%" border="0" cellspacing="0" cellpadding="0" height="100%">
  <tr>
  <td width="486">
   <img src="../resource/img/nframe/log2.png">
  </td>
    <td valign="middle" align="left">
    	<div style="margin-left:25px;">
    	您好：<font color="#990000">${uinfo.loginName}</font>
    	 欢迎登录。
    	 	<div style="margin:5px 0px 0px 0px; color:#990000;">
    	 		客户您好，本公司提供商业智能技术外包、产品二次开发、系统出售等业务，如有需要请和我们联系。
		</div>
    	 </div>
    	 </td>
    <td valign="middle" align="right" nowrap="nowrap">
    	<div class="uinfo"><a href="http://www.ruisitech.com" target="_blank">公司网站</a> &nbsp; <a href="javascript:getuserinfo()">账号信息</a>  &nbsp; <a href="javascript:chgpasswd()">修改密码</a></div>
        <div class="uinfo"><a href="http://www.ruisitech.com/concat.html" target="_blank">联系我们</a> &nbsp; <a href="javascript:getfankui()">用户反馈</a>  &nbsp; <a href="javascript:logout()">退出登录</a></div>
    </td>
  </tr>
</table>
        </div>
    </div>
	<div data-options="region:'west',split:true,title:'系统菜单'" style="width:222px;padding:2px 0px 0px 5px;">
     <ul id="sysmenu"></ul>
    </div>
	<div data-options="region:'south',border:false" style="height:26px; color:#333; overflow:hidden">
    	<div class="pfooter">
            <div align="left" style="float:left; margin:3px 0px 0px 10px;">
                当前在线用户数：<span id="usercnt">X</span>人
            </div>
            <div style="float:right; margin: 3px 20px 0px 0px;">
                <a href="http://www.ruisitech.com" target="_blank" style="text-decoration:underline">北京睿思科技有限公司(www.ruisitech.com)</a> 版权所有
            </div>
        </div>
    </div>
	<div data-options="region:'center',title:''" style="-webkit-overflow-scrolling:touch; overflow: hidden;" >
    <iframe id="maininfo" frameborder="0" width="100%" height="100%" src="${firstMenu.url}"></iframe>
    </div>
</body>
</html>
