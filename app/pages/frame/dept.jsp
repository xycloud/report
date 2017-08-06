<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page session="false" buffer="none" %>
<%@ taglib prefix="ext" uri="/WEB-INF/ext-runtime.tld" %>
<div id="tree-div" style="overflow:auto; border:1px solid #c3daf9;"></div>
<script language="javascript">
Ext.onReady(function(){
   
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
		title:'部门管理',
        loader:new Ext.tree.TreeLoader({url:'extControl?serviceid=frame.DeptManage&methodId=loadData&' + fromId +"=frame.DeptManage"}),
        root: {
            nodeType: 'async',
            text: '部门',
            draggable: false,
            id: '0'
        }
    });
	
	tree.on('contextmenu',function (node,event){//右键单击
            event.stopEvent();  
            selectedNode=node;
            e=event;
        	var coords = event.getXY();  
        
        	rightMenu.showAt([coords[0], coords[1]]);   
       });

    // render the tree
    tree.render('tree-div');
    tree.getRootNode().expand();

	//修改窗口
	//*******************************//
	var regionStore = new Ext.data.JsonStore({
        autoLoad:true,
        autoDestroy: true,
        url: 'extControl?serviceid=frame.DeptManage&methodId=getSaleAreas&' + fromId +"=frame.DeptManage",
        storeId: 'rgStore',
        
        fields: ['reId', 'reDesc']
    });

	    
	  var deptKindStore = new Ext.data.JsonStore({
	        autoLoad:true,
	        autoDestroy: true,
	        url: 'extControl?serviceid=frame.DeptManage&methodId=getDeptKinds&' + fromId +"=frame.DeptManage",
	        storeId: 'dtStore',
	        
	        fields: ['kindId', 'kindDesc']
	    });
	  var deptRankStore = new Ext.data.JsonStore({
	        autoLoad:true,
	        autoDestroy: true,
	        url: 'extControl?serviceid=frame.DeptManage&methodId=getDeptRanks&' + fromId +"=frame.DeptManage",
	        storeId: 'rkStore',
	        
	        fields: ['rankId', 'rankDesc']
	    });
	//******************************//	
	var operForm = new Ext.form.FormPanel(
			{
			labelAlign: 'right',
			labelWidth: 50,
			frame: true,
			defaultType: 'textfield',
			items: [		
					{
				fieldLabel: '名称',
				name: 'deptDesc',
				id : 'deptDesc',
				width: 300
			},
			
			{fieldLabel:'类型',
				name:'kindId',
				id:'kindId',
				xtype:'combo',
				valueField:'kindId',
				displayField:'kindDesc',
				store:deptKindStore,
				emptyText:'请选部门类型..',
				hiddenName:'deptKind',//hiddenName才是提交到后台的input的name   
		        editable:false,//不允许输入   
		        triggerAction: 'all',
		          
				width:300
				},
				{fieldLabel:'级别',
					name:'rankId',
					id:'rankId',
					xtype:'combo',
					valueField:'rankId',
					displayField:'rankDesc',
					store:deptRankStore,
					emptyText:'请选部门级别..',
					hiddenName:'deptRank',//hiddenName才是提交到后台的input的name   
			        editable:false,//不允许输入   
			        triggerAction: 'all',
			          
					width:300
					},
			{
				fieldLabel: '区域',
				name: 'reId',
				id : 'region',
				xtype:'combo',
				 valueField: 'reId',
				 displayField: 'reDesc',
				 store:regionStore,
				 emptyText:'请选营销地域..',
				 hiddenName:'reId',//hiddenName才是提交到后台的input的name   
		         editable: false,//不允许输入   
		         triggerAction: 'all',
		         listeners:{     
		             select : function(combo, record,index){    
		             },
	             	click:function(){}  
				},
				width: 300
			}, 
			{
				fieldLabel: '排序',
				name: 'order',
				id : 'order',
				width: 300
			},{fieldLabel:'地址',
				name:'address',
				id:'address',
					width:300
				},
				{fieldLabel:'描述',
					name:'deptMemo',
					id:'deptMemo',
					
					width:300
						},
					{
							name: 'pid',
							id : 'pid',
							xtype: 'hidden',
							width: 300},
							
					{
								name: 'id',
								id : 'id',
								xtype: 'hidden',
								width: 300
								}
						
			]
		});
	var nodeText;
	var operWindow = new Ext.Window({
			title: 'xxx',
			width: 400,	
			closeAction : 'hide',
			modal : true,
			buttons : [{
				text: '保存',
				handler: function(){
					//提交数据
					var m = null;
					if(formState=='new'){
						m = "saveDept";
					}else{
						m = "updateDept"
					}
					var tform = operForm.getForm();
					
					var url = "extControl?serviceid=frame.DeptManage&methodId="+m+"&" + fromId +"=frame.DeptManage";
					new Ajax.Request(url,{
						method:'POST',
						parameters: tform.getValues(),
						onFailure: function(){
							alert('操作失败.');
						},
						onSuccess: function(transport){
							
													
							if(formState=='mod'){
							
								nodeText=operForm.get("deptDesc").getValue();
								
								selectedNode.setText(nodeText);
								
							}else{
								
								var data = Ext.decode(transport.responseText);
								
								var t = operForm.get("deptDesc").getValue();
							
								var tid = data.id;
								var tNode = new Ext.tree.TreeNode({id: tid, text: t});
								selectedNode.appendChild(tNode);
								selectedNode.leaf=false;
								selectedNode.expand();
							}
							
							operWindow.hide();
						}
					});
				}
			},{
				text: '关闭',
				handler: function(){
					operWindow.hide();
				}
			}]
		});
	var depts=operForm.get("region");
	//depts.on("click",function(){});
	operWindow.add(operForm);
	
	//右键菜单
	var rightMenu = new Ext.menu.Menu({
		id: 'mainMenu',
		items: [
				new Ext.menu.Item({
                    text: '编辑',
                    handler:function(){
						formState = 'mod';
						operWindow.setTitle("编辑目录");
                    	operWindow.show();
                    	//regionStore.load(null);
						Ext.Ajax.request({
							url: "extControl?serviceid=frame.DeptManage&methodId=getDeptInfo&id="+selectedNode.attributes.id+"&t="+Math.random(),
							method : "get",
							callback: function(options,success,response){
								if(success){
									var data =Ext.decode(response.responseText);
									deptRankStore.load(null);
									deptKindStore.load(null);
									regionStore.load(null);
									operForm.getForm().setValues({id: data[0].id, deptDesc: data[0].deptDesc, address :data[0].address, order:data[0].order,deptMemo:data[0].deptMemo});
									
									if(data[0]&&data[0].region){								
										operForm.get("region").setValue(data[0].region);
										operForm.get("kindId").setValue(data[0].deptKind);
										}
									
									
								}
							}
						});
                     }
				}),
				new Ext.menu.Item({
                    text: '新增',
                    handler:function(){
						formState = 'new';
                    	operWindow.setTitle("新增目录");
                    	operWindow.show();
						operForm.getForm().setValues({id: '', deptDesc: '', address :'', reId:'', kindId:'',order:'0',deptMemo:''});
						operForm.get("pid").setValue(selectedNode.attributes.id);
                     }
				}),
				new Ext.menu.Item({
                    text: '删除',
                    handler:function(){
                    	if(confirm('是否确认？')){
							new Ajax.Request("extControl?serviceid=frame.DeptManage&methodId=deleteDept&id="+selectedNode.attributes.id, {
								method : "get",
								onSuccess: function(response, options){
									selectedNode.remove();
								},
								onFailure : function(response, options){
									Ext.Msg.alert('Error','节点存在子节点，删除失败.');
								}
								
							});
						}
                     }
				})
			]
	});
});
</script>
