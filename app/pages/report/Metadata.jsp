<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="bi" uri="/WEB-INF/common.tld"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	 <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
   <title>数据建模工具</title>
<link rel="shortcut icon" type="image/x-icon" href="../resource/img/rs_favicon.ico">
   <script type="text/javascript" src="../ext-res/js/jquery.min.js"></script>
	<link rel="stylesheet" type="text/css" href="../ext-res/css/fonts-min.css" />
	<link rel="stylesheet" type="text/css" href="../ext-res/css/boncbase.css?v2" />
    <link rel="stylesheet" type="text/css" href="../resource/css/meta.css?v1" />
  
	<script type="text/javascript" src="../ext-res/My97DatePicker/WdatePicker.js"></script>
	<script language="javascript" src="../resource/js/json.js"></script>
	<link rel="stylesheet" type="text/css" href="../resource/jquery-easyui-1.3.4/themes/default/easyui.css">
	<link rel="stylesheet" type="text/css" href="../resource/jquery-easyui-1.3.4/themes/icon.css">
	<script type="text/javascript" src="../resource/jquery-easyui-1.3.4/jquery.easyui.min.js"></script>
    <script type="text/javascript" src="../resource/jquery-easyui-1.3.4/locale/easyui-lang-zh_CN.js"></script>
    <!--
    <script type="text/javascript" src="../resource/js/meta.js"></script>
    -->
    <script type="text/javascript" src="../resource/js/meta-compress.js"></script>
     
</head>

<script language="javascript">
var curTmpInfo = {};
jQuery(function(){
	initdatatree();
});
</script>


<body id="metalayout" class="easyui-layout">



	<div data-options="region:'west',split:true,title:'导航栏'"  style="width:212px;">
    	 <ul id="mydatatree"></ul>
    </div>
    
	<div data-options="region:'center',title:''" id="optarea">
	</div>
    
    <div id="pdailog"></div>
    
    <div id="subTreeMenu" class="easyui-menu">
	<div onclick="addSubType(false)" id="add">新增分类...</div>
    <div onclick="addSubType(true)" id="mod">修改分类...</div>
    <div onclick="delType()" id="del">删除分类</div>
	</div>
    
    <div id="dimTreeMenu" class="easyui-menu">
	<div onclick="addGroup(false)" id="add">新增分组...</div>
    <div onclick="addGroup(true)" id="mod">修改分组...</div>
    <div onclick="delGroup()" id="del">删除分组</div>
	</div>
    
    <div id="kpiTreeMenu" class="easyui-menu">
	<div onclick="addKpiGroup(false)" id="add">新增分类...</div>
    <div onclick="addKpiGroup(true)" id="mod">修改分类...</div>
    <div onclick="delKpiGroup()" id="del">删除分类</div>
	</div>
</body>
</html>