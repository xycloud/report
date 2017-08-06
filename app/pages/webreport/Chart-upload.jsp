<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<script language="javascript">
parent.document.picForm.picurl2.value = "${fileName}";
</script>
<img src="../pic/${fileName}">