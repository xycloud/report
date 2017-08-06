<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="bi" uri="/WEB-INF/common.tld"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	 <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
   <title>公有报表管理</title>
  <link rel="shortcut icon" type="image/x-icon" href="../resource/img/rs_favicon.ico">
   <script type="text/javascript" src="../ext-res/js/jquery.min.js"></script>
   	<script language="javascript" src="../ext-res/js/ext-base.js"></script>

	<link rel="stylesheet" type="text/css" href="../ext-res/css/fonts-min.css" />
    <link rel="stylesheet" type="text/css" href="../ext-res/css/boncbase.css?v3" />
    <link rel="stylesheet" type="text/css" href="../resource/css/catalog.css?v1" />
	<link rel="stylesheet" type="text/css" href="../resource/jquery-easyui-1.3.4/themes/gray/easyui.css">
	<link rel="stylesheet" type="text/css" href="../resource/jquery-easyui-1.3.4/themes/icon.css">
	<script type="text/javascript" src="../resource/jquery-easyui-1.3.4/jquery.easyui.min.js"></script>
    <script type="text/javascript" src="../resource/jquery-easyui-1.3.4/locale/easyui-lang-zh_CN.js"></script>
    <script type="text/javascript" src="../resource/js/catalog.js"></script>
    <script type="text/javascript" src="../ext-res/highcharts/highcharts.js"></script>
	<script type="text/javascript" src="../ext-res/highcharts/highcharts-more.js"></script>
    <script type="text/javascript" src="../ext-res/highcharts/modules/map.js"></script>
	<script language="javascript" src="../ext-res/js/sortabletable.js"></script>  
     <script type="text/javascript" src="../ext-res/My97DatePicker/WdatePicker.js"></script>
</head>
<script language="javascript">
jQuery(function(){
	initTree();
	initGrid();
});
</script>
<body>
 <div class="bi_tit2 bi_tit2_underline">公有报表管理</div>
<div style="width:1020px;">
    	<div class="catalist">
        	<div class="cataleft"><ul id="ggcatatree"></ul></div>   
        	<div class="cataright">
             
              <table id="gytablelist" title="公有报表列表" style="width:auto;height:auto;" >
                  <thead>
                  <tr>
                   <th data-options="field:'ck',checkbox:true"></th>
                   <th data-options="field:'id',width:60">ID</th>
                   <th data-options="field:'name',width:200,formatter:fmtAuthreport">名称</th>
                   <th data-options="field:'cata',width:100">所属目录</th>
                   <th data-options="field:'incomeName',width:120">来源</th>
                   <th data-options="field:'crtdate',width:120,align:'center'">发布时间</th>
                   <th data-options="field:'loginName',width:120,align:'center'">发布人</th>
                   </tr>
                   </thead>
                   </table>
            </div>  
        </div>
    </div>
        

<div id="ggcatatreeMenu" class="easyui-menu">
	<div onclick="addCatalog(false, 'ggcatatree')" id="add">新增...</div>
    <div onclick="addCatalog(true, 'ggcatatree')" id="mod">修改...</div>
    <div onclick="delCatalog('ggcatatree')" id="del">删除</div>
</div>

<div id="pdailog"></div>
</body>
</html>