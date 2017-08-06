<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="bi" uri="/WEB-INF/common.tld"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	 <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
   <title>主题域管理</title>
   <script type="text/javascript" src="../ext-res/js/jquery.min.js"></script>
	<link rel="stylesheet" type="text/css" href="../ext-res/css/fonts-min.css" />
    <link rel="stylesheet" type="text/css" href="../ext-res/css/boncbase.css?v3" />
    <link rel="stylesheet" type="text/css" href="../resource/css/meta.css?v1" />
	<link rel="stylesheet" type="text/css" href="../resource/jquery-easyui-1.3.4/themes/gray/easyui.css">
	<link rel="stylesheet" type="text/css" href="../resource/jquery-easyui-1.3.4/themes/icon.css">
	<script type="text/javascript" src="../resource/jquery-easyui-1.3.4/jquery.easyui.min.js"></script>
    <script type="text/javascript" src="../resource/jquery-easyui-1.3.4/locale/easyui-lang-zh_CN.js"></script>
    <script type="text/javascript" src="../resource/js/meta-compress.js"></script>
</head>
<script language="javascript">
jQuery(function(){
	initSubjectTree();
	initSubjectList();
});
</script>
<body>
<div>
 <div class="bi_tit2 bi_tit2_underline">主题域管理</div>
<div class="cubelist">
    <div class="cubeleft"><ul id="subtypetree"></ul></div>   
    <div class="cuberight">
     <div align="right" style="margin:3px; color:#333">
      </div>
      <table id="subjectlist" title="主题域列表" style="width:auto;height:auto;" >
          <thead>
          <tr>
           <th data-options="field:'ck',checkbox:true"></th>
           <th data-options="field:'name',width:160">名称</th>
           <th data-options="field:'note',width:300">说明</th>
           <th data-options="field:'tname',width:130,align:'center'">默认立方体</th>
           <th data-options="field:'mindt',width:100,align:'center'">最小时间</th>
           <th data-options="field:'maxdt',width:100,align:'center'">最大时间</th>
           </tr>
           </thead>
           </table>
    </div>  
</div>
<div id="subTreeMenu" class="easyui-menu">
	<div onclick="addSubType(false)" id="add">新增...</div>
    <div onclick="addSubType(true)" id="mod">修改...</div>
    <div onclick="delType()" id="del">删除</div>
</div>
<div id="pdailog"></div>
</body>
</html>