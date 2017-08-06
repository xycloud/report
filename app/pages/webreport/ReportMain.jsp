<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="bi" uri="/WEB-INF/common.tld"%>
<%
boolean showtit = true;
String stit = request.getParameter("showtit");
if(stit != null && stit.length() > 0){
	showtit = "true".equals(stit);
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	 <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
   <title><s:if test="pageName != null && pageName !=''">${pageName} - </s:if>睿思报表 - WEB报表工具</title>
   <meta name="keywords" content="北京睿思科技有限公司 商业智能 web 自助式 报表" />
	<meta name="description" content="在线WEB自助式报表" />
<link rel="shortcut icon" type="image/x-icon" href="../resource/img/rs_favicon.ico">
   <script type="text/javascript" src="../ext-res/js/jquery.min.js"></script>
	<link rel="stylesheet" type="text/css" href="../ext-res/css/fonts-min.css" />
	<link rel="stylesheet" type="text/css" href="../ext-res/css/boncbase.css?v2" />
    <link rel="stylesheet" type="text/css" href="../resource/css/webreport.css?v2" />
  
	<script type="text/javascript" src="../ext-res/My97DatePicker/WdatePicker.js"></script>
	<script language="javascript" src="../resource/js/json.js"></script>    

<script language="javascript" src="../resource/js/webreport-compress.js?v5"></script>
 
    <link rel="stylesheet" type="text/css" href="../resource/jquery-easyui-1.3.4/themes/gray/easyui.css">
	<link rel="stylesheet" type="text/css" href="../resource/jquery-easyui-1.3.4/themes/icon.css">
	<script type="text/javascript" src="../resource/jquery-easyui-1.3.4/jquery.easyui.min.js"></script>
    <script type="text/javascript" src="../ext-res/highcharts/highcharts.js"></script>
    <script type="text/javascript" src="../ext-res/highcharts/highcharts-more.js"></script>
   
</head>

<script language="javascript">
<%
String pageInfo = (String)request.getAttribute("pageInfo");
if(pageInfo == null){
%>
var pageInfo = {"layout":1, "body":{tr1:[{colspan:1, rowspan:1, width:100, height:100, id:1}]}};
<%}else{%>
var pageInfo = <%=pageInfo%>;
<%}%>
var showtit = <%=showtit%>;
var curTmpInfo = {"menus":"${menus}"}; //临时对象
jQuery(function(){
	curTmpInfo.scrossTable = new staticCrossTable();
	curTmpInfo.property = new compProperties();
	//初始化datatree
	initmydatatree();
	//初始化布局器
	initlayout(true);
	//初始化组件拖放
	regCompTree();
	//初始化视图
	initviewTree();
	//初始化键盘事件
	initkeyborad();
	//初始化样式
	curTmpInfo.property.crtStyleTree();
	
	$(".d_table span.crskpi a, .tsbd span.grouptype, #d_kpi span.grouptype, .gridtab span.grouptype, div.crskpi span.kpilabel, div.crskpi2 span.kpilabel").live("mouseover", function(){
		$(this).css("opacity", 1);
	}).live("mouseout",function(){
		$(this).css("opacity", 0.6);
	});
	//禁止选中文本
	//document.body.onselectstart=document.body.ondrag=function(){return false}
});

$(document).ready(function(){  
    //禁止退格键 作用于Firefox、Opera   
    document.onkeypress = banBackSpace;  
    //禁止退格键 作用于IE、Chrome  
    document.onkeydown = banBackSpace;  
}); 

//处理键盘事件 禁止后退键（Backspace）密码或单行、多行文本框除外   
function banBackSpace(e){  
    //alert(event.keyCode)  
    var ev = e || window.event;//获取event对象
    var obj = ev.target || ev.srcElement;//获取事件源       
    var t = obj.type || obj.getAttribute('type');//获取事件源类型       
    //获取作为判断条件的事件类型   
    var vReadOnly = obj.readOnly;  
    var vDisabled = obj.disabled;  
    //处理undefined值情况   
    vReadOnly = (vReadOnly == undefined) ? false : vReadOnly;  
    vDisabled = (vDisabled == undefined) ? true : vDisabled;  
    //当敲Backspace键时，事件源类型为密码或单行、多行文本的，    
    //并且readOnly属性为true或disabled属性为true的，则退格键失效    
    var flag1 = ev.keyCode == 8 && (t == "password" || t == "text" || t == "textarea") && (vReadOnly == true || vDisabled == true);  
    //当敲Backspace键时，事件源类型非密码或单行、多行文本的，则退格键失效      
    var flag2 = ev.keyCode == 8 && t != "password" && t != "text" && t != "textarea";  
    //判断     
    if (flag2 || flag1){   
        event.returnValue = false;//这里如果写 return false 无法实现效果   
		return false;
	}
}  
</script>

<body class="easyui-layout" id="reportJLayout">


	<div data-options="region:'north',border:false" style="height:32px;padding:1px; overflow:hidden;">
       <%
        	if(showtit){
        %>
       <div class="bi_tit">
        <div align="right" style="padding:5px 10px 5px 5px; font-size:14px"><a target="_blank" href="http://www.rosetech.cn">公司网站</a> - <a target="_blank" href="http://www.rosetech.cn/product.html">解决方案</a> - <a target="_blank" href="http://bi.rosetech.cn/frame/Frame.action">演示系统</a> - <a target="_blank" href="http://www.rosetech.cn/concat.html">联系我们</a> - <a target="_blank" href="http://www.rosetech.cn/suggest.html">用户反馈</a> </div>
        </div>
        <%
			}else{
		%>
        <%
			}
		%>
        <div class="panel-header" style="padding:3px; background-image:url(../ext-res/image/white-top-bottom.gif);">
        	<a href="javascript:openreport();" id="mb1" class="easyui-linkbutton" plain="true" iconCls="icon-open">打开</a>
            <a href="javascript:newpage();" id="mb1" class="easyui-linkbutton" plain="true" iconCls="icon-newpage">新建</a>
            <a href="javascript:;" id="mb2" menu="#saveinfo" class="easyui-menubutton" plain="true" iconCls="icon-save">保存</a>
            <a href="javascript:;" id="mb3" menu="#insertdsinfo" class="easyui-menubutton" plain="true" iconCls="icon-dataset" title="">数据</a>
            <a href="javascript:setlayout();" id="mb4" class="easyui-linkbutton" plain="true" iconCls="icon-layout" title="">布局</a>
            <a href="javascript:;" id="mb6" menu="#runmenu" class="easyui-menubutton" plain="true" iconCls="icon-run" title="">运行</a>
            <a href="javascript:error_info();" id="mb7" class="easyui-linkbutton" plain="true" iconCls="icon-error" title="">错误</a>
            <a href="javascript:exportPage();" id="mb8" class="easyui-linkbutton" plain="true" iconCls="icon-export" title="">导出</a>
            <a href="javascript:printData()" id="mb10" class="easyui-linkbutton" plain="true" iconCls="icon-print" title="">打印</a>
            <a href="javascript:helper();" id="mb11" class="easyui-linkbutton" plain="true" iconCls="icon-help" title="">帮助</a>
        </div>
    </div>
	<div data-options="region:'west',split:true,title:'对象面板'"  style="width:212px;">
    	<div id="comp_tab" data-options="fit:true,border:false" class="easyui-tabs" style="height:auto; width:auto;">
        	<div title="组件" style="">
                <ul class="easyui-tree" id="comp_tree" data-options="onContextMenu:function(e){e.preventDefault();}">
                    <li>
                        <span>组件列表</span>
                        <ul>
                         	<li data-options="attributes:{tp:'label'},iconCls:'icon-label'">标签</li>
                            <li data-options="attributes:{tp:'text'}, iconCls:'icon-text'">文本</li>
                            <li data-options="attributes:{tp:'pic'},iconCls:'icon-pic'">图片</li>
                            <li data-options="attributes:{tp:'chart'},iconCls:'icon-chart'">图表</li>
                            <li data-options="attributes:{tp:'table'},iconCls:'icon-table'">表格</li>
                            <li data-options="attributes:{tp:'cross'},iconCls:'icon-cross'">交叉表</li>
                            <!--
                            <li data-options="attributes:{tp:'staticCross'},iconCls:'icon-cross2'">固定表</li>
                            -->
                        </ul>
                    </li>
                </ul>
            </div>
            <div title="数据" style="">
        		    <ul id="mydatatree" class="easyui-tree"></ul>
            </div>
            <div title="样式" style="">
        		    <ul id="mystyletree" class="easyui-tree"></ul>
            </div>
            <div title="视图" style=""> 
            	<ul id="viewtree" class="easyui-tree">
                </ul>
            </div>
        </div>
    </div>
    
    <div data-options="region:'east',split:true,collapsed:false,title:'属性面板'" style="width:220px;">
    	<table id="propertytable" class="easyui-propertygrid" style="width:200px;"></table>
	</div>
    
	<div align="center" data-options="region:'center',title:'<s:if test="pageName != null && pageName !=''">${pageName} - </s:if>操作面板'" style="padding:3px; background-color:#DDDDDD;" id="optarea">
    
	</div>

<div id="insertdsinfo" style="width:150px;">
		<div onclick="newdatasource(false,'new')" >创建数据源</div>
        <div onclick="newdatasource(false,'select')" >选择数据源</div>
		<div onclick="newdataset()" >创建数据集</div>
        <div onclick="newcube()">创建立方体</div>
        <div onclick="newcube2()">选择立方体</div>
        <div onclick="newparam('insert')">创建参数</div>
        <div onclick="newCascaParam('insert')">创建级联参数</div>
</div>
<div id="runmenu">
  	<div onclick="savepage(null, true)">预览(shift + v)</div>
	<div onclick="releasePage()">发布(shift + r)</div>
</div>
<div id="saveinfo" style="width:150px;">
		<div onclick="savepage(null, false)" >保存(shift + s)</div>
		<div onclick="saveas()" >另存...</div>
</div>
<div id="pdailog"></div>
<div class="indicator">==></div>
<div id="mydatasetmenu" class="easyui-menu">
	<div id="dataset_add" onclick="newdatactx()">新建</div>
	<div id="dataset_mod" onclick="editmydata()">编辑</div>
    <div id="dataset_del" onclick="deletemydata()">删除</div>
</div>
<div id="comprightmenu" class="easyui-menu">
	<div id="m_editcomp" onclick="editcomp()">编辑</div>
    <div id="m_compevent" onclick="compevent()">事件...</div>
    <div onclick="movecomp()">移动</div>
    <div onclick="copycomp()">拷贝</div>
	<div id="m_paste" onclick="pastecomp('comp')">粘贴</div>
    <div onclick="deletecomp()">删除</div>
</div>
<div id="chartoptmenu" class="easyui-menu">
	<div onclick="chartsort('asc')">升序</div>
    <div onclick="chartsort('desc')">降序</div>
    <div onclick="delChartKpiOrDim()" iconCls="icon-remove">清除</div>
</div>
<div id="aggretypemenu" class="easyui-menu">
	<div iconCls="icon-blank" id="aggre_sum" onclick="kpiAggreType('sum')">求和</div>
    <div iconCls="icon-blank" id="aggre_count" onclick="kpiAggreType('count')">计数</div>
    <div iconCls="icon-blank" id="aggre_avg" onclick="kpiAggreType('avg')">求平均</div>
    <div iconCls="icon-blank" id="aggre_max" onclick="kpiAggreType('max')">最大值</div>
    <div iconCls="icon-blank" id="aggre_min" onclick="kpiAggreType('min')">最小值</div>
</div>
<div id="crossmenu" class="easyui-menu">
	<div onclick="editCross()">编辑...</div>
	<div onclick="crossDimAggre()" inconCls="icon-blank" id="m_aggre">计算...</div>
    <div onclick="crossmove('left')" id="m_left">左移</div>
    <div onclick="crossmove('right')" id="m_right">右移</div>
    <div onclick="crossdelete()">删除</div>
     <div class="menu-sep"></div>
     <div onclick="compevent()">事件...</div>
     <div onclick="crossxxbt()">斜线表头...</div>
     <div onclick="crosszz()">表格转置</div>
     <div onclick="crossdrill()">表格钻取...</div>
     <div class="menu-sep"></div>
    <div onclick="movecomp()">移动组件</div>
    <div onclick="copycomp()">拷贝组件</div>
	<div onclick="pastecomp('comp')">粘贴组件</div>
    <div onclick="deletecomp()">删除组件</div>
</div>
<div id="layoutmenu" class="easyui-menu">
    <div>
    	<span>插入组件</span>
        <div style="width:120px;">
            <div data-options="iconCls:'icon-label'" onclick="layoutInsertComp('label')">标签</div>
            <div data-options="iconCls:'icon-text'" onclick="layoutInsertComp('text')">文本</div>
            <div data-options="iconCls:'icon-pic'" onclick="layoutInsertComp('pic')">图片</div>
            <div data-options="iconCls:'icon-chart'" onclick="layoutInsertComp('chart')">图表</div>
            <div data-options="iconCls:'icon-table'" onclick="layoutInsertComp('table')">表格</div>
            <div data-options="iconCls:'icon-cross'" onclick="layoutInsertComp('cross')">交叉表</div>
            <div data-options="iconCls:'icon-cross2'" onclick="layoutInsertComp('staticCross')">固定表</div>
        </div>
    </div>
    <div id="m_paste2" onclick="pastecomp('layout')">粘贴组件</div>
    <div onclick="setlayout()">布局器</div>
</div>
<div id="tablerightmenu" class="easyui-menu">
	<div onclick="editcells()">编辑</div>
    <div onclick="clearcells()">清除</div>
    <div class="menu-sep"></div>
    <div onclick="insertRow()" id="m_insertrow">插入行</div>
    <div onclick="insertCol()" id="m_insertcol">插入列</div>
    <div onclick="deleteRow()" id="m_deleterow">删除行</div>
    <div onclick="deleteCol()" id="m_deletecol">删除列</div>
    <div onclick="mergeCell()" id="m_mergeCell">合并</div>
    <div onclick="unmergeCell()" id="m_unmergeCell">取消合并</div>
    <div class="menu-sep"></div>
    <div onclick="compevent()">事件...</div>
    <div onclick="movecomp()">移动组件</div>
    <div onclick="copycomp()">拷贝组件</div>
	<div onclick="pastecomp('comp')">粘贴组件</div>
    <div onclick="deletecomp()">删除组件</div>
</div>
<div id="staticCrossMenu" class="easyui-menu">
	<div onclick="curTmpInfo.scrossTable.editcell()">编辑</div>
    <div id="d_fj" onclick="curTmpInfo.scrossTable.resolveDim()">分解</div>
    <div onclick="curTmpInfo.scrossTable.delcell()">删除</div>
    <div onclick="curTmpInfo.scrossTable.delall()">删除整行/列</div>
    <div class="menu-sep"></div>
    <div id="i_child" onclick="curTmpInfo.scrossTable.insertChild()">插入下级</div>
    <div id="i_left" onclick="curTmpInfo.scrossTable.insert('left')">左边插入</div>
    <div id="i_right" onclick="curTmpInfo.scrossTable.insert('right')">右边插入</div>
    <div id="c_up" onclick="curTmpInfo.scrossTable.move('left')">左移</div>
    <div id="c_down" onclick="curTmpInfo.scrossTable.move('right')">右移</div>
    <div class="menu-sep"></div>
    <div onclick="curTmpInfo.scrossTable.baseKpi()">指标...</div>
    <div onclick="movecomp()">移动组件</div>
    <div onclick="copycomp()">拷贝组件</div>
	<div onclick="pastecomp('comp')">粘贴组件</div>
    <div onclick="deletecomp()">删除组件</div>
</div>
<div id="autolayoutmenu" class="easyui-menu">
    <div onclick="lyt_insertRow()" id="lyt_insertrow">插入行</div>
    <div onclick="lyt_insertCol()" id="lyt_insertcol">插入列</div>
    <div onclick="lyt_deleteRow()" id="lyt_deleterow">删除行</div>
    <div onclick="lyt_deleteCol()" id="lyt_deletecol">删除列</div>
    <div onclick="lyt_mergeCell()" id="lyt_mergeCell">合并</div>
    <div onclick="lyt_unmergeCell()" id="lyt_unmergeCell">取消合并</div>
</div>
<div id="stylesmenu" class="easyui-menu" style="width:120px;">
	<div onclick="curTmpInfo.property.crtStyle(false)" id="crtstyles">新建</div>
    <div onclick="curTmpInfo.property.crtStyle(true)" id="modstyles">修改</div>
    <div onclick="curTmpInfo.property.delStyle()" id="delstyles">删除</div>
    <div class="menu-sep"></div>
    <div onclick="curTmpInfo.property.exportcss()" id="expstyles">导出为文件</div>
    <div onclick="curTmpInfo.property.importcss()" id="impstyles">导入CSS文件</div>
</div>
<div>
<form name="pageviewform" id="pageviewform" action="ReportMain!view.action" method="post" target="_blank">
<input type="hidden" name="pageInfo" id="pageInfo">
</form>
</div>
</body>
</html>