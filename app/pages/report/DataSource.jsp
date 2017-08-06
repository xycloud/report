<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="bi" uri="/WEB-INF/common.tld"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	 <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
   <title>数据源管理</title>
    <script type="text/javascript" src="../ext-res/js/jquery.min.js"></script>
	<script language="javascript" src="../ext-res/js/ext-base.js"></script>
    <script language="javascript" src="../resource/js/json.js"></script>
	<script language="javascript" src="../ext-res/js/FusionCharts.js"></script>
	
	<link rel="stylesheet" type="text/css" href="../ext-res/css/fonts-min.css" />
	<link rel="stylesheet" type="text/css" href="../ext-res/css/boncbase.css" />
    <link rel="stylesheet" type="text/css" href="../resource/css/meta.css" />
  
	<script type="text/javascript" src="../ext-res/My97DatePicker/WdatePicker.js"></script>

	<link rel="stylesheet" type="text/css" href="../resource/jquery-easyui-1.3.4/themes/gray/easyui.css">
	<link rel="stylesheet" type="text/css" href="../resource/jquery-easyui-1.3.4/themes/icon.css">
	<script type="text/javascript" src="../resource/jquery-easyui-1.3.4/jquery.easyui.min.js"></script>
    <script type="text/javascript" src="../resource/jquery-easyui-1.3.4/locale/easyui-lang-zh_CN.js"></script>
    <script type="text/javascript" src="../resource/js/meta-compress.js"></script>
    
</head>

<script>
var curTmpInfo = {};
jQuery(function(){
	$("#dsourcetable").datagrid({
		singleSelect:true,
		collapsible:false,
		pagination:true,
		pageSize:20,
		border:true,
		url:'DataSource!list2.action',
		queryParams:{t:Math.random()},
		method:'get',
		toolbar:[{
		  text:'新增',
		  iconCls:'icon-add',
		  handler:function(){
			newdsource(false);
		  }
		},{
		  text:'修改',
		  iconCls:'icon-edit',
		  handler:function(){
			var row = $("#dsourcetable").datagrid("getChecked");
			if(row == null || row.length == 0){
				$.messager.alert("出错了。","您还未勾选数据。", "error");
				return;
			}
			newdsource(true, row[0].dsid);
		  }
		},{
		  text:'删除',
		  iconCls:'icon-cancel',
		  handler:function(){
			var row = $("#dsourcetable").datagrid("getChecked");
			if(row == null || row.length == 0){
				$.messager.alert("出错了。","您还未勾选数据。", "error");
				return;
			}
			if(confirm("是否确认删除？")){
				$.ajax({
					 type: "POST",
				     url: "DataSource!del.action",
				     dataType:"html",
				     data: {"dsid":row[0].dsid},
				     success: function(resp){
						 $('#dsourcetable').datagrid('load', {t:Math.random()});
				     }
				});
			}
		  }
		}]
	});
});
</script>
 <body style="margin:0px; padding:0px;">
 
  	  <table id="dsourcetable" title="数据源管理" style="width:auto;height:auto;" >
      <thead>
      <tr>
       <th data-options="field:'ck',checkbox:true"></th>
       <th data-options="field:'dsname',width:120,formatter:fb1">名称</th>
       <th data-options="field:'use',width:120">类型</th>
       <th data-options="field:'linktype',width:100">数据库</th>
       <th data-options="field:'linkurl',width:250">连接字符串</th>
       <th data-options="field:'linkname',width:120,align:'center'">用户名</th>
       <th data-options="field:'loginName',width:120,align:'center'">创建人</th>
       </tr>
       </thead>
       </table>
 <div id="pdailog"></div>
</body>
</html>