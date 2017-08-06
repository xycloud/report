package com.ruisi.vdop.web.webreport;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import sun.misc.BASE64Encoder;

import com.ruisi.vdop.util.VDOPUtils;

/**
 * 生成交叉表头图形
 * @author hq
 * @date 2014-11-14
 */
public class CrossHeadAction {

	private String json; //交叉表头配置信息
	private String border; //是否显示边框
	
	public String execute() throws IOException{
		JSONObject config = JSONObject.fromObject(json);
		
		String pic = this.drawImage(config);
		pic = "<img "+("true".equals(border)?"border=1":"")+" src=\"data:image/png;base64,"+pic+"\">";
		
		HttpServletResponse resp = VDOPUtils.getResponse();
		resp.setContentType("text/html; charset=UTF-8");
		resp.getWriter().print(pic);
		
		return null;
	}
	
	private String drawImage(JSONObject config) {
		try{
			int w = config.getInt("width");
			int h = config.getInt("height");
			if(h < 30){
				h = 30;
			}
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			Graphics g = image.getGraphics();
			g.setColor(new Color(204 ,204, 204));
			//g.setColor(Color.black);
			//g.drawLine(0, 0, w, h);    //平分上下的斜线
			
			JSONArray top = (JSONArray)config.get("top");  //画top方向的斜线
			if(top != null && top.size() > 0){
				int size = top.size();
				int step = h / size;
				for(int i=0; i<size; i++){
					int x = w;
					int y = (i + 1) * step;
					g.drawLine(0, 0, x, y);
					
					//写字
					String txt = top.getString(i);
					g.setColor(Color.black);
					g.drawString(txt, x - (txt.length() >= 3 ? 50 : 30),  i * step + step/2);
					g.setColor(new Color(204 ,204, 204));
				}
			}
			
			JSONArray bottom = (JSONArray)config.get("bottom");  //画bottom方向的斜线
			if(top != null && bottom.size() > 0){
				int size = bottom.size();
				int step = w / size;
				for(int i=0; i<size; i++){
					int x = (i + 1) * step;
					int y = h;
					g.drawLine(0, 0, x, y);
					
					//写字
					String txt = bottom.getString(i);
					g.setColor(Color.black);
					g.drawString(txt, 5 + (i * step), y - 5);
					g.setColor(new Color(204 ,204, 204));
				}
			}
			
			g.dispose();
			ImageIO.write(image, "png", os);
			BASE64Encoder encode = new BASE64Encoder();
			String ret = encode.encode(os.toByteArray());
			return ret;
		}catch(Exception ex){
			ex.printStackTrace();
			return null;
		}
	}
	public String getJson() {
		return json;
	}

	public void setJson(String json) {
		this.json = json;
	}

	public String getBorder() {
		return border;
	}

	public void setBorder(String border) {
		this.border = border;
	}
}
