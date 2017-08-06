<%@ page language="java" contentType="text/html; charset=utf-8" import="java.util.*" pageEncoding="utf-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>

<strong>sql语句: </strong><br/> 
${sql} <br/> <br/> 

<strong>测试结果： </strong>
<font color="#FF0000"><s:if test="#request.ret == true">SQL正确</s:if> <s:if test="#request.ret == false">SQL错误</s:if></font>