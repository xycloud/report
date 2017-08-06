<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%
String showparam = request.getParameter("showparam");
%>
<form id="datasetform" name="datasetform">
<div class="textpanel">
<span class="inputtext">
数据集名称：
</span>
<input type="text" id="datasetname" name="datasetname" class="inputform"><br/>
<span class="inputtext">
连接数据源：
</span>
<span id="dsiddiv"></span>
<script language="javascript">
var ctx = "<select id=\"dsid\" name=\"dsid\" class=\"inputform\"><option value=\"\">当前默认</option>";
for(var i=0; pageInfo.datasource && i<pageInfo.datasource.length; i++){
	ctx = ctx + "<option value=\""+pageInfo.datasource[i].dsid+"\">"+(pageInfo.datasource[i].use=='jndi'?pageInfo.datasource[i].jndiname:pageInfo.datasource[i].dsname)+"</option>";
}
ctx = ctx + "</select>";
$("#pdailog #dsiddiv").html(ctx);
</script>
<table width="100%" border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td valign="top" width="10%">
<span class="inputtext">
查询字符串：<br/>
<%
if("1".equals(showparam)){
%>
<a href="javascript:testReportSql('<%=request.getParameter("datasetid")%>');">测试SQL</a>
<%
}
%>
</span>
    </td>
    <td>
    <textarea name="querysql" cols="90" rows="13" id="querysql"></textarea>
    </td>
  </tr>
</table>
<%
if("1".equals(showparam)){
%>
<div class="actColumn">

</div>
<%
}
%>
</div>
</form>