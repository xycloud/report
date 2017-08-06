<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<s:iterator var="e" value="#request.ls" status="statu">
<span class='selpics'><a href='javascript:;' path='${e.name}'>${e.name}</a></span>
</s:iterator>