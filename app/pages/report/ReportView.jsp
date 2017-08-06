<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="bi" uri="/WEB-INF/common.tld"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	 <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
   <title>报表访问</title>
<link rel="shortcut icon" type="image/x-icon" href="../resource/img/rs_favicon.ico">
    <script type="text/javascript" src="../ext-res/js/jquery.min.js"></script>
	<script language="javascript" src="../ext-res/js/ext-base.js"></script>
	<script language="javascript" src="../ext-res/js/FusionCharts.js"></script>
	
	<link rel="stylesheet" type="text/css" href="../ext-res/css/fonts-min.css" />
	<link rel="stylesheet" type="text/css" href="../ext-res/css/boncbase.css" />
  
	<script type="text/javascript" src="../ext-res/My97DatePicker/WdatePicker.js"></script>

	<link rel="stylesheet" type="text/css" href="../resource/jquery-easyui-1.3.4/themes/default/easyui.css">
	<link rel="stylesheet" type="text/css" href="../resource/jquery-easyui-1.3.4/themes/icon.css">
	<script type="text/javascript" src="../resource/jquery-easyui-1.3.4/jquery.easyui.min.js"></script>
	
	<script type="text/javascript" src="../ext-res/highcharts/highcharts.js"></script>
	<script type="text/javascript" src="../ext-res/highcharts/highcharts-more.js"></script>

	<script language="javascript" src="../ext-res/js/sortabletable.js"></script>   
</head>

<style>
.exportpanel{
	margin:30px 10px 10px 10px;
}
.exportpanel .exptp{
	display:-moz-inline-box;  
	display:inline-block;
	width:68px;
	text-align:center;
	cursor:pointer;
}
.exportpanel .select {
    border: 1px solid #990000;
}
</style>

<script language="javascript">
var mvid = "usave.${mvid}";
var expType = null;
function exportReport(){
	var ctx = "<form action=\"ReportView!export.action\" method=\"post\"><input type='hidden' name='mvid' id='mvid' value='"+mvid+"'><input type='hidden' name='type' id='type'><div class='exportpanel'><span class='exptp select' tp='html'><img src='../resource/img/export-html.gif'><br>HTML</span>"+
			"<span class='exptp' tp='csv'><img src='../resource/img/export-csv.gif'><br>CSV</span>" +
			"<span class='exptp' tp='excel'><img src='../resource/img/export-excel.gif'><br>EXCEL</span>" + 
			"<span class='exptp' tp='pdf'><img src='../resource/img/export-pdf.gif'><br>PDF</span>" + 
			"</div></form>";
	$('#pdailog').dialog({
		title: '导出数据',
		width: 310,
		height: 200,
		closed: false,
		cache: false,
		modal: true,
		toolbar:null,
		content: ctx,
		onLoad:function(){},
		buttons:[{
					text:'确定',
					handler:function(){
						$("#pdailog #type").val(expType);
						$("#pdailog form").submit();
						$('#pdailog').dialog('close');
					}
				},{
					text:'取消',
					handler:function(){
						$('#pdailog').dialog('close');
					}
				}]
	});
	expType = "html";
	//注册事件
	$(".exportpanel span.exptp").click(function(e) {
		$(".exportpanel span.exptp").removeClass("select");
        $(this).addClass("select");
		expType = $(this).attr("tp");
    });
}
function printReport(){
	var url2 = "ReportView!print.action?mvid="+mvid;
	window.open(url2);
}
$(function(){
	$.ajax({
		type:"GET",
		url:"../control/extView",
		data: {"mvid":"usave.${mvid}","returnJsp":"false"},
		dataType: "html",
		success: function(msg){
			$("#pagectx").html(msg);
		},
		error:function(a,b,c){
			$.messager.alert("出错了。","报表展现出错，请查看后台日志。", "error");
		}
	});
});
</script>

 <body class="yui-skin-sam" style="margin:0px; padding:0px;">
 <div class="bi_tit2 bi_tit2_underline">报表浏览</div>
 <div align="left" style="margin:5px;">
 <a href="ReportCatalog.action" class="easyui-linkbutton" data-options="iconCls:'icon-back'">返回</a>
 <a href="javascript:exportReport();" class="easyui-linkbutton" data-options="iconCls:'icon-export'">导出</a>
 <a href="javascript:printReport();" class="easyui-linkbutton" data-options="iconCls:'icon-print'">打印</a>
 </div>

<div id="pagectx" style="margin:10px;">数据加载中...</div>

<div id="pdailog"></div>

</body>
</html>