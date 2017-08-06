<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<table class="grid3" id="T_report54" cellpadding="0" cellspacing="0">
<thead>
<tr class="scrollColThead" style="background-color:#FFF">
	<th width="10%" colspan="1"  rowspan="1">选择</th>
	<th  class="40%" colspan="1"  rowspan="1">报表名称</th>
    <th  class="20%" colspan="1"  rowspan="1">所属目录</th>
	<th  class="15%" colspan="1"  rowspan="1">创建时间</th>
	<th  class="15%" colspan="1"  rowspan="1">修改时间</th>
</tr>
	<s:iterator var="e" value="#request.ls" status="statu">
<tr>
	<td class='kpiData1 grid3-td'><input type="radio" id="reportId" name="reportId" value="${e.id}" /></td>	
 <td class='kpiData1 grid3-td'>${e.name}</td>	
  <td class='kpiData1 grid3-td'>${e.cata}</td>	
 <td class='kpiData1 grid3-td' align="center"><s:date name="crtdate" format="yyyy-MM-dd" /></td>
 <td class='kpiData1 grid3-td' align="center"><s:date name="updatedate" format="yyyy-MM-dd" /></td>
</tr>
 </s:iterator>

</thead>
</table>