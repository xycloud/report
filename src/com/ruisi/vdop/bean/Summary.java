package com.ruisi.vdop.bean;

import java.util.UUID;

public class Summary 
{
	private String summary_id;
	private String act_id;
	private String act_bg;
	private String act_exp;
	private String act_creativity;
	private String act_lesson;
	public Summary() 
	{
		this.summary_id = UUID.randomUUID().toString();
	}
	public String getSummary_id() {
		return summary_id;
	}
	public void setSummary_id(String summary_id) {
		this.summary_id = summary_id;
	}
	public String getAct_id() {
		return act_id;
	}
	public void setAct_id(String act_id) {
		this.act_id = act_id;
	}
	public String getAct_bg() {
		return act_bg;
	}
	public void setAct_bg(String act_bg) {
		this.act_bg = act_bg;
	}
	public String getAct_exp() {
		return act_exp;
	}
	public void setAct_exp(String act_exp) {
		this.act_exp = act_exp;
	}
	public String getAct_creativity() {
		return act_creativity;
	}
	public void setAct_creativity(String act_creativity) {
		this.act_creativity = act_creativity;
	}
	public String getAct_lesson() {
		return act_lesson;
	}
	public void setAct_lesson(String act_lesson) {
		this.act_lesson = act_lesson;
	}

	
}
