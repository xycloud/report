<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="bi" uri="/WEB-INF/common.tld"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	 <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	<link rel="shortcut icon" type="image/x-icon" href="../resource/img/rs_favicon.ico">
    <script type="text/javascript" src="../ext-res/js/jquery.min.js"></script>
	<link rel="stylesheet" type="text/css" href="../ext-res/css/fonts-min.css" />   
    <link rel="stylesheet" type="text/css" href="../resource/css/meta.css?v1" />
    <script type="text/javascript" src="../resource/js/meta-compress.js"></script>
</head>


<script language="javascript">
jQuery(function(){
	var c = viewCubeChart("${param.cubeId}");
	$(c).appendTo("body");
});
</script>

 <body style="margin:0px; padding:0px;">


</body>
</html>