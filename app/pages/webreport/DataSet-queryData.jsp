<%@ page language="java" contentType="text/html; charset=utf-8" import="java.util.*" pageEncoding="utf-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>

<%
List ds = (List)request.getAttribute("ls");
List tit = (List)ds.get(0);
%>
(前100行数据。)
<table class="grid3" id="T_report54" cellpadding="0" cellspacing="0">
<thead>
<tr class="scrollColThead" style="background-color:#FFF">
<%
for(int i=0; i<tit.size(); i++){
%>
	<th  class="null" colspan="1"  rowspan="1"><%=tit.get(i)%></th>
<%
}
%>
</tr>
<%
for(int i=1; i<ds.size(); i++){
	Map m = (Map)ds.get(i);
	out.print("<tr>");
	for(int j=0; j<tit.size(); j++){
%>
	<td class='kpiData1 grid3-td'><%=(m.get(tit.get(j)) == null ? "" : m.get(tit.get(j)))%></td>	
<%
	}
	out.print("</tr>");
}
%>
 </thead>
</table>