<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page session="false" buffer="none" %>
<%@ taglib prefix="ext" uri="/WEB-INF/ext-runtime.tld" %>
<style>
.textpanel{
	line-height:30px;
	margin:10px;
}
.inputform {
	width:200px;
}
span.inputtext {
	display:-moz-inline-box;  
	display:inline-block;
	width:90px;
}
</style>

 <div class="bi_tit2 bi_tit2_underline">菜单管理</div>
<ul id="ggcatatree"></ul>
<script language="javascript">
	var $ = jQuery;
	$(function(){
		$('#ggcatatree').tree({
			url:'extControl?serviceid=frame.Menu&methodId=loadData&t_from_id=frame.Menu&t='+Math.random(),
			dnd:false,
			animate:true,
			data: [{id:'0', text:'系统菜单', state:'closed'}],
			onBeforeLoad: function(node){
				if(!node || node == null){
					return false;
				}
			},
			onContextMenu: function(e, node){
				e.preventDefault();
				$('#ggcatatree').tree('select', node.target);
				if(node.id == '0'){
					$('#treeMenu').menu("disableItem", $("#treeMenu #mod"));
					$('#treeMenu').menu("disableItem", $("#treeMenu #del"));
				}else{
					$('#treeMenu').menu("enableItem", $("#treeMenu #mod"));
					$('#treeMenu').menu("enableItem", $("#treeMenu #del"));
				}
				$('#treeMenu').menu('show', {
					left: e.pageX,
					top: e.pageY
				});
			}
		});
		var node = $('#ggcatatree').tree("getRoot");
		$('#ggcatatree').tree("expand", node.target);
	});
	function addMenu(update){
		var node = $("#ggcatatree").tree("getSelected");
		var obj;
		if(update){
			$.ajax({
				   type: "GET",
				   async: false,
				   url: "extControl?serviceid=frame.Menu&methodId=getMenu&t="+Math.random(),
				   dataType:"JSON",
				   data: {"id":node.id},
				   success: function(resp){
					  obj = resp;
				   }
			});
		}
		var ctx = "<div class=\"textpanel\"><span class=\"inputtext\">名称：</span><input type=\"text\" id=\"name\" class=\"inputform\" value=\""+(obj?obj.name:"")+"\"><br/><span class=\"inputtext\">URL：</span><input type=\"text\" id=\"url\" class=\"inputform\" value=\""+((obj?obj.url:""))+"\"><br/><span class=\"inputtext\">排序：</span><input type=\"text\" id=\"order\" class=\"inputform\" value=\""+(obj?obj.order:"1")+"\"><br/><span class=\"inputtext\">备注：</span><input type=\"text\" id=\"desc\" class=\"inputform\" value=\""+(obj?obj.desc:"")+"\"><br/></div>";
		$('#pdailog').dialog({
			title: update?'修改菜单':'新建菜单',
			width: 350,
			height: 220,
			closed: false,
			cache: false,
			modal: true,
			toolbar:null,
			content: ctx,
			buttons:[{
					text:'确定',
					iconCls:'icon-ok',
					handler:function(){
						var name = $("#pdailog #name").val();
						var url = $("#pdailog #url").val();
						var note = $("#pdailog #note").val();
						var order = $("#pdailog #order").val();
						if(name == ''){
							alert("名称必须填写。");
							return;
						}
						if(isNaN(order)){
							alert("排序字段必须是数字类型。");
							return;
						}
						if(update==false){
							$.ajax({
							   type: "POST",
							   url: "extControl?serviceid=frame.Menu&methodId=saveMenu&t_from_id=frame.Menu",
							   dataType:"text",
							   data: {"name":name,"note":note,"order":order, "url":url, "pid":node.id},
							   success: function(resp){
								   $("#ggcatatree").tree("append", {parent:node.target, data:[{id:resp,text:name}]});
							   }
							});
						}else{
							$.ajax({
							   type: "POST",
							   url: "extControl?serviceid=frame.Menu&methodId=updateMenu&t_from_id=frame.Menu",
							   dataType:"text",
							   data: {"name":name,"note":note,"order":order, "url":url, "id":node.id},
							   success: function(resp){
								   $("#ggcatatree").tree("update", {target:node.target, text:name});
							   },
							   error: function(a, b, c){
								   $.messager.alert("出错了。","修改出错。", "error");
							   }
							});
						}
						$('#pdailog').dialog('close');
					}
				},{
					text:'取消',
					iconCls:"icon-cancel",
					handler:function(){
						$('#pdailog').dialog('close');
					}
				}]
		});
	}
	function delMenu(){
		if(confirm('是否确认删除？')){
			var node = $("#ggcatatree").tree("getSelected");
			$.ajax({
			   type: "POST",
			   url: "extControl?serviceid=frame.Menu&methodId=delete&t_from_id=frame.Menu",
			   dataType:"html",
			   data: {"id":node.id},
			   success: function(resp){
				   $("#ggcatatree").tree("remove", node.target);
			   },
			   error: function(){
				   $.messager.alert("出错了。","该菜单下可能含有子菜单，不能删除。", "error");
			   }
			});
		}
	}
</script>
<div id="pdailog"></div>
<div id="treeMenu" class="easyui-menu">
	<div onclick="addMenu(false)" id="add">新增...</div>
    <div onclick="addMenu(true)" id="mod">修改...</div>
    <div onclick="delMenu()" id="del">删除</div>
</div>
