package com.ruisi.vdop.service.frame;

import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.io.IOUtils;

import com.ruisi.ext.engine.dao.DaoHelper;
import com.ruisi.ext.engine.init.ExtXMLLoader;
import com.ruisi.ext.engine.view.exception.ExtConfigException;
import com.ruisi.vdop.util.VDOPUtils;
import com.ruisi.vdop.util.VdopConstant;

/**
 * 对于报表、仪表盘、多维分析等工具生成的mv对象，直接从数据库中读取xml文件 
 * @author hq
 * @date 2015-5-14
 */
public class LoadXmlFromDB implements ExtXMLLoader {

	public InputStream load(String mvId, String absolutePath, String xmlResource, ServletContext sctx) {
		DaoHelper dao = VDOPUtils.getDaoHelper(sctx);
		Map m = new HashMap();
		m.put("id", mvId);
		List ls = dao.getSqlMapClientTemplate().queryForList("web.report.getmv", m);
		try{
			if(ls == null || ls.size() == 0){
				throw new ExtConfigException("id = " + mvId + " 的文件不存在。");
			}
			Map data = (Map)ls.get(0);
			Object pctx = data.get("ctx");
			if(pctx instanceof String){
				String pageInfo = (String)pctx;
				return IOUtils.toInputStream(pageInfo, "utf-8");
			}else if(pctx instanceof oracle.sql.CLOB){
				oracle.sql.CLOB clob = (oracle.sql.CLOB)pctx;
				Reader is = clob.getCharacterStream();
				String pageInfo = IOUtils.toString(is);
				is.close();
				return IOUtils.toInputStream(pageInfo, "utf-8");
			}else if(pctx instanceof net.sourceforge.jtds.jdbc.ClobImpl){
				net.sourceforge.jtds.jdbc.ClobImpl clob = (net.sourceforge.jtds.jdbc.ClobImpl)pctx;
				Reader is = clob.getCharacterStream();
				String pageInfo = IOUtils.toString(is);
				is.close();
				return IOUtils.toInputStream(pageInfo, "utf-8");
			}
			throw new RuntimeException("类型未定义....");
		}catch(Exception ex){
			ex.printStackTrace();
			throw new RuntimeException("获取数据出错....");
		}
	}

}
