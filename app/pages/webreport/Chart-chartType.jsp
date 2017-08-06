<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<div class="chartselect">
<div class="selleft">
<ul>
<li cid="1">曲线图</li>
<li cid="2">柱状图</li>
<li cid="8">条形图</li>
<li cid="9">面积图</li>
<li cid="3">饼图</li>
<li cid="4">仪表盘</li>
<li cid="5">雷达图</li>
<li cid="6">散点图</li>
<li cid="7">气泡图</li>
<div style="height:90px; background-color:#EEE"></div>
</ul>
</div>
<div class="selright">
<div class="one" id="schart1" align="center" style="display:block;" tp="line">
<span class="charttype" index='1'>
<img src="../resource/img/chart/c1.gif">
</span>
<span class="charttype" index='2'>
 <img src="../resource/img/chart/c12.gif">
</span>
</div>
<div class="one" id="schart2" align="center" tp="column">
<span class="charttype" index='1'>
<img src="../resource/img/chart/c2.gif">
</span>
<span class="charttype" index='2'>
 <img src="../resource/img/chart/c22.gif">
</span>
</div>
<div class="one" id="schart3" align="center" tp="pie">
<span class="charttype" index='1'>
<img src="../resource/img/chart/c3.gif">
</span>
</div>
<div align="center" tp="gauge" id="schart4" class="one" style="display: block;">
<span class="charttype" index='1'>
<img src="../resource/img/chart/c4.gif">
</span>
</div>
<div class="one" id="schart5" align="center" tp="radar">
<span class="charttype" index='1'>
<img src="../resource/img/chart/c5.gif">
</span>
</div>
<div class="one" id="schart6" align="center" tp="scatter">
<span class="charttype" index='1'>
<img src="../resource/img/chart/c6.gif">
</span>
</div>
<div class="one" id="schart7" align="center" tp="bubble">
<span class="charttype" index='1'>
<img src="../resource/img/chart/c7.gif">
</span>
</div>
<div class="one" id="schart8" align="center" tp="bar">
<span class="charttype" index='1'>
<img src="../resource/img/chart/bar.gif">
</span>
</div>
<div class="one" id="schart9" align="center" tp="area">
<span class="charttype" index='1'>
<img src="../resource/img/chart/area.gif">
</span>
</div>
</div>
</div>
<script language="javascript">
$(function(){
	chartcss();
});
</script>