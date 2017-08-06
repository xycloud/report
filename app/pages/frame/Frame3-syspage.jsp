<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="bi" uri="/WEB-INF/common.tld"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	 <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
   <title>系统页面</title>
<link rel="shortcut icon" type="image/x-icon" href="../resource/img/rs_favicon.ico">
   <script type="text/javascript" src="../ext-res/js/jquery.min.js"></script>
	<link rel="stylesheet" type="text/css" href="../ext-res/css/fonts-min.css" />
  
	<script language="javascript" src="../resource/js/json.js"></script>
	<link rel="stylesheet" type="text/css" href="../resource/jquery-easyui-1.3.4/themes/gray/easyui.css">
	<link rel="stylesheet" type="text/css" href="../resource/jquery-easyui-1.3.4/themes/icon.css">
	<script type="text/javascript" src="../resource/jquery-easyui-1.3.4/jquery.easyui.min.js"></script>
    <script type="text/javascript" src="../resource/jquery-easyui-1.3.4/locale/easyui-lang-zh_CN.js"></script>
  
     
</head>

<script language="javascript">
jQuery(function(){
	var isfirst = false;
	jQuery("#sysmenu").tree({
		url:"Frame3!tree.action?defaultId=${param.defaultId}",
		onLoadSuccess:function(node, data){
			if(isfirst == false){
				if(data.length > 0){
					document.getElementById("syspageinfo").src = data[0].attributes.url;
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
				document.getElementById("syspageinfo").src = u;
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


<body id="syslayout" class="easyui-layout">



	<div data-options="region:'west',split:true,title:'菜单栏'"  style="width:212px;">
    	 <ul id="sysmenu"></ul>
    </div>
    
	<div data-options="region:'center',title:''" id="optarea" style="overflow:hidden;">
   	<iframe id="syspageinfo" frameborder="0" scrolling="auto" width="100%" height="100%"></iframe>
	</div>
   
</body>
</html>