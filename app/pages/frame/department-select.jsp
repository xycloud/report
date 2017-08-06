<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page session="false" buffer="none" %>
<%@ taglib prefix="ext" uri="/WEB-INF/ext-runtime.tld" %>
部门选择：<input type="text" id="departmentname" name="departmentname" size="17" onclick="YAHOO.catapanel.show()" value="<%if(request.getParameter("dept")==null){%>${dept}<%}else{%>${param.dept}<%}%>"> <a href="javascript:;" onclick="YAHOO.catapanel.show()"><img border="0" src="../ext-res/image/customization.gif"></a>
<div id="catapanel" style="visibility:hidden; position:absolute">
	<div class="hd">部门选择</div>
	<div class="bd" id="tree-div" style="overflow:auto;height:250px;width:230px;margin:0px; padding:0px; background-color:#FFFFFF;"></div>
</div>
<div id="deptIframe" style="height:250px;width:230px; position:absolute;">
<iframe width="100%" height="100%" frameborder="0"></iframe>	
</div>
<script language="javascript">
function initpanel(){ 
    // shorthand
	
    var Tree = Ext.tree;
    var tree = new Tree.TreePanel({
        useArrows: false,
        autoScroll: true,
        animate: false,
        enableDD: false,
        containerScroll: true,
		rootVisible: false,
        border: false,
		//title:'报表目录',
        loader:new Ext.tree.TreeLoader({url:'extControl?serviceid=frame.User&methodId=loadDeptData&' + fromId +"=frame.User-preAdd"}),
        root: {
            nodeType: 'async',
            text: '部门列表',
            draggable: false,
            id: '0'
        }
    });
	
	tree.on('click',function (node,event){
		var id = node.attributes.id;
		var txt = node.attributes.text;
		document.forms[formId].departmentname.value = txt;
		document.forms[formId].department.value = id;
    	YAHOO.catapanel.hide();	           
    });
	
    // render the tree
    tree.render('tree-div');
    tree.getRootNode().expand();
	
	//panel
	YAHOO.catapanel = new YAHOO.widget.Panel("catapanel", { width:"232", visible:false, iframe:true} );
	YAHOO.catapanel.render();
	var x = YAHOO.util.Dom.getX("departmentname");
	var y = YAHOO.util.Dom.getY("departmentname");
	YAHOO.catapanel.moveTo(x-1, y+23);
	$('deptIframe').style.top = (y + 23) + 'px';
	$('deptIframe').style.left = (x - 1) + 'px';
	$('deptIframe').hide();
	
	YAHOO.catapanel.beforeShowEvent.subscribe(function(){
		$('deptIframe').show();
	});
	YAHOO.catapanel.beforeHideEvent.subscribe(function(){
		$('deptIframe').hide();
	});
}
YAHOO.util.Event.addListener(window, "load", initpanel);
</script>