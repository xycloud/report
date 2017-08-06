<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page session="false" buffer="none" %>
<%@ taglib prefix="ext" uri="/WEB-INF/ext-runtime.tld" %>
<style>

.moduleTree .x-panel-header {
	background:transparent url(../ext-res/image/header_bj.gif);
	border:0px;
}
.moduleTree .x-panel-header .x-panel-header-text{
	background:transparent url(../ext-res/image/sanjiaoda.gif) no-repeat scroll 0 2;
	padding:0px 0px 0px 15px;
}
.moduleTree .x-tree-table-td{
	background:transparent url(../ext-res/image/tree_left_top.gif) no-repeat scroll 0 0;
	width:159px;
	height:6px;
}
/*目录树展开隐藏按钮*/
.moduleTree .x-tree-td-right{
	background:#96c0f0;
	width:6px;
	cursor:pointer;
	height: 98%;
}
.moduleTree .x-tree-header-toggle-left{
	background:transparent url(../ext-res/image/toggle_left.gif) no-repeat scroll 0 0;
	width:6px;
	height:30px;
}
.moduleTree .x-tree-header-toggle-right{
	background:transparent url(../ext-res/image/toggle_right.gif) no-repeat scroll center top;
	width:6px;
	height:30px;
}
/*目录树节点*/
.moduleTree .x-tree-node{
	font-size:1em;
	white-space:nowrap;
	letter-spacing:1.5px;
	font-weight: bold;
	border:0px;
	padding:3px;
}
/*目录树节点div*/
.moduleTree .x-tree-node-el {
	cursor:pointer;
	line-height:18px;
}

/*目录树节点鼠标划过、目录树节点选中 背景色*/
.moduleTree .x-tree-node .x-tree-node-over, .moduleTree .x-tree-node .x-tree-selected{
	background:none;
}
/*目录树节点鼠标划过、目录树节点选中 字体*/
.moduleTree .x-tree-node .x-tree-selected a span{
	color:#747474;
}
/*目录树节点内容样式 鼠标划过时*/
.moduleTree .x-tree-node a:active span, .moduleTree .x-tree-node a:hover span{
	color:#747474; 
}
/*目录树节点内容样式 */
.moduleTree .x-tree-node a{
	font-size:12px;
	vertical-align: bottom;
}
/*目录树节点左边的图片*/
.moduleTree .x-tree-node-collapsed .x-tree-node-icon,.moduleTree .x-tree-node-expanded .x-tree-node-icon{
	background:none;
	width: 0;
}
/*目录树中当节点为叶子节点时左边的图片*/
.moduleTree .x-tree-node-leaf .x-tree-node-icon{
	/*不显示叶子节点的图标
	*/
	background:transparent url(../ext-res/image/tree_leaf_green.gif) no-repeat scroll 0 0;
}

/*子节点字体样式*/
.moduleTree .x-tree-node-leaf{
	font-weight: normal;
}

/*目录树节点左边的图片的左边的空位*/
.moduleTree .x-tree-arrows .x-tree-elbow,.moduleTree .x-tree-arrows .x-tree-elbow-end,.moduleTree .x-tree-arrows .x-tree-elbow-line,.moduleTree .x-tree-icon{
	background:none;
	width:1.5px;
}
/*目录树目录操作，当为箭头、目录未打开时*/
.moduleTree .x-tree-arrows .x-tree-elbow-end-plus, .moduleTree .x-tree-arrows .x-tree-elbow-plus{
	background-position:0 0;
	background:transparent url(../ext-res/image/tree_toggle_left.gif) no-repeat scroll 0 0;
}
/*目录树目录操作，当为箭头、目录未打开、鼠标划过时*/
.moduleTree .x-tree-arrows .x-tree-ec-over .x-tree-elbow-end-plus, .moduleTree .x-tree-arrows .x-tree-ec-over .x-tree-elbow-plus{
	background-position:0 0;
}
/*目录树目录操作，当为箭头、目录打开时*/
.moduleTree .x-tree-arrows .x-tree-elbow-end-minus, .moduleTree .x-tree-arrows .x-tree-elbow-minus{
	background-position:0 0;
	background:transparent url(../ext-res/image/tree_toggle_bottom.gif) no-repeat scroll 0 0;
}
/*目录树目录操作，当为箭头、目录打开、鼠标划过时*/
.moduleTree .x-tree-arrows .x-tree-ec-over .x-tree-elbow-end-minus,.moduleTree .x-tree-arrows .x-tree-ec-over .x-tree-elbow-minus{
	background-position:0 0;
}

</style>


<div id="moduleTree" class="moduleTree">
<div id="tree-div" style=""></div>
</div>

<script language="javascript">
Ext.onReady(function(){
    // shorthand
    var Tree = Ext.tree;
	var selectedNode = null;
	var formState = null; //表示当前form是修改还是新增

    var tree = new Tree.TreePanel({
        useArrows: true,
        autoScroll: true,
        animate: true,
        enableDD: false,
        containerScroll: true,
        border: false,
		//title:'功能菜单',
		rootVisible: false,
        loader:new Ext.tree.TreeLoader({url:'extControl?serviceid=frame.LeftMenu&methodId=loadData&' + "t_from_id" +"=frame.LeftMenu"}),
        root: {
            nodeType: 'async',
            text: '报表中心',
            draggable: false,
            id: '${param.node}'
        }
    });
	
	tree.on('click',function (node,event){
	   //菜单
	   
           var l = (node.attributes.leaf);   
		   if(l == 1){
		   		url = node.attributes.url;
				if(url!=''&&url!='null' && url != null){
					var fm=parent.document.getElementById("mainFrame");
					//alert(url);
					fm.src = url;
				}
		   }
       });

    // render the tree
    tree.render('tree-div');
	expandFirst(tree.getRootNode(), tree);
});

function expandFirst(node, tree){
	//只把节点展开到报表目录
	if(node==null){
		return;
	}
	
	if(node.attributes.typ == 'cata'){
		tree.fireEvent('click',node);
	}
	
	if(node.parentNode != null && node.parentNode.attributes.id == '4'){
		return;	
	}
	
		node.expand(false,true,function(){
			expandFirst(node.firstChild,tree);
		});

}

</script>