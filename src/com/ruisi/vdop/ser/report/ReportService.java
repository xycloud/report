package com.ruisi.vdop.ser.report;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.ruisi.ext.engine.dao.DaoHelper;

public class ReportService {
	private DaoHelper daoHelper;
	public ReportService(DaoHelper daoHelper){
		this.daoHelper = daoHelper;
	}
	
	/**
	 * 查询当前分类下的所有分类
	 * @return
	 */
	public List<Integer> queryChildTypes(Integer id, List datas){
		List<Integer> ret = new ArrayList<Integer>();
		ret.add(id);
		List tps = datas;
		this.loopChildren(id, ret, tps);
		return ret;
	}
	
	private void loopChildren(int id, List<Integer> ids, List datas){
		List<Integer> ret = this.getChildren(id, datas);
		if(ret.size() > 0){
			ids.addAll(ret);
			for(Integer tmp : ret){
				this.loopChildren(tmp, ids, datas);
			}
		}
	}
	
	private List<Integer> getChildren(Integer id, List datas){
		List<Integer> ret = new ArrayList<Integer>();
		for(int i=0; i<datas.size(); i++){
			Map m = (Map)datas.get(i);
			Object nid = m.get("id");
			if(nid instanceof Integer){
				nid = (Integer)nid;
			}else if(nid instanceof Long){
				nid = ((Long)nid).intValue();
			}else{
				nid = ((BigDecimal)nid).intValue();
			}
			Object pid = m.get("pid");
			if(pid instanceof Integer){
				pid = (Integer)pid;
			}else if(pid instanceof Long){
				pid = ((Long)pid).intValue();
			}else{
				pid = ((BigDecimal)pid).intValue();
			}
			if(pid.equals(id)){
				ret.add((Integer)nid);
			}
		}
		return ret;
	}
}
