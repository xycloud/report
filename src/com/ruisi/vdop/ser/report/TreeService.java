package com.ruisi.vdop.ser.report;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 递归调用，生成Tree 数据
 * @author hq
 * @date 2014-7-30
 */
public class TreeService {
	
	public List createTreeData(List datas, TreeInterface treeface){
		List roots = this.getChildren(datas, 0);
		this.loopChildren(roots, datas, treeface);
		return roots;
	}
	
	public List createTreeDataById(List datas, TreeInterface treeface, int id){
		List roots = this.getChildren(datas, id);
		this.loopChildren(roots, datas, treeface);
		return roots;
	}
	
	private void loopChildren(List nodes, List datas, TreeInterface treeface){
		for(int i=0; i<nodes.size(); i++){
			Map root = (Map)nodes.get(i);
			treeface.processMap(root);
			int id;
			Object ret = root.get("id");
			if(ret instanceof Integer){
				id = (Integer)ret;
			}else if(ret instanceof BigDecimal){
				id = ((BigDecimal)ret).intValue();
			}else{
				id = ((Long)ret).intValue();
			}
			List child = this.getChildren(datas, id);
			if(child.size() > 0){
				this.loopChildren(child, datas, treeface);
			}
			if(child.size() > 0){
				root.put("state", "closed");
			}
			treeface.processEnd(root, child.size() > 0 ? true : false);
			root.put("children", child);
		}
	}
	
	private List getChildren(List datas, int id){
		List roots = new ArrayList();
		for(int i=0; i<datas.size(); i++){
			Map m = (Map)datas.get(i);
			int pid;
			Object pobj = m.get("pid");
			if(pobj instanceof Integer){
				pid = (Integer)pobj;
			}else if(pobj instanceof Long){
				pid = ((Long)pobj).intValue();
			}else if(pobj instanceof BigDecimal){
				pid = ((BigDecimal)pobj).intValue();
			}else{
				throw new RuntimeException("类型不支持。");
			}
			if(pid == id){
				roots.add(m);
			}
		}
		return roots;
	}
	
	/**
	 * 给目录上添加报表
	 */
	public void addReport2Cata(List catas, List reports){
		for(int i=0; i<catas.size(); i++){
			Map cata = (Map)catas.get(i);
			cata.put("state", "closed");
			List rpt = this.findReports(cata, reports);
			List children = (List)cata.get("children");
			if(children != null && children.size() > 0){
				addReport2Cata(children, reports);
			}
			//给目录的 children 添加报
			//设置图标
			for(int j=0; j<rpt.size(); j++){
				Map rp = (Map)rpt.get(j);
				rp.put("iconCls", "icon-gears");
				//添加attributes
				Map att = new HashMap();
				att.put("rfile", rp.get("rfile"));
				rp.put("attributes", att);
				rp.remove("crtdate");
			}
			children.addAll(rpt);
		}
		return;
	}
	
	public List controlCata(List catas, List reports){
		List ret = new ArrayList();
		this.control(catas, ret, reports);
		return ret;
	}
	
	private void control(List catas, List ret, List reports){
		for(int i=0; i<catas.size(); i++){
			Map cata = (Map)catas.get(i);
			List children = (List)cata.get("children");
			if(children != null && children.size() > 0){
				Map nmap = this.copyMap(cata);
				
				List nchildren = new ArrayList();
				nmap.put("children", nchildren);
				control(children, nchildren, reports);
				if(nchildren.size() == 0){
					nmap.put("state", "open");
				}
				if(nchildren.size() > 0 || this.findReports(cata, reports).size() > 0 ){ //有子或者存在报表
					ret.add(nmap);
				}
				
			}else{
				//判断是否有报表
				if(this.findReports(cata, reports).size() > 0){
					ret.add(cata);
				}
			}
		}
	}
	
	private Map copyMap(Map map){
		Map ret = new HashMap();
		Iterator it = map.keySet().iterator();
		while(it.hasNext()){
			Object key = it.next();
			if("children".equals(key)){
				continue;
			}
			ret.put(key, map.get(key));
		}
		return ret;
	}
	
	private List findReports(Map cata, List reports){
		List ret = new ArrayList();
		int id;
		Object pobj = cata.get("id");
		if(pobj instanceof Integer){
			id = (Integer)pobj;
		}else if(pobj instanceof Long){
			id = ((Long)pobj).intValue();
		}else if(pobj instanceof BigDecimal){
			id = ((BigDecimal)pobj).intValue();
		}else{
			throw new RuntimeException("类型不支持。");
		}
		for(int i=0; i<reports.size(); i++){
			Map report = (Map)reports.get(i);
			int cataId;
			Object cobj = report.get("cataId");
			if(cobj instanceof Integer){
				cataId = (Integer)cobj;
			}else if(cobj instanceof Long){
				cataId = ((Long)cobj).intValue();
			}else if(cobj instanceof BigDecimal){
				cataId = ((BigDecimal)cobj).intValue();
			}else{
				throw new RuntimeException("类型不支持。");
			}
			if(id == cataId){
				ret.add(report);
			}
		}
		return ret;
	}
}
