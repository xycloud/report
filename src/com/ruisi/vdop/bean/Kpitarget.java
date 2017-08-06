package com.ruisi.vdop.bean;

import java.util.UUID;

public class Kpitarget 
{
	private String id;
	private String target_name;
	private String target_sector1;
	private String target_sector2;
	private String target_sector3;
	private String target_sector4;
	private String target_type;
	public Kpitarget()
	{
		this.id = UUID.randomUUID().toString();
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return target_name;
	}
	public void setName(String name) {
		this.target_name = name;
	}
	public String getSector1() {
		return target_sector1;
	}
	public void setSector1(String sector1) {
		this.target_sector1 = sector1;
	}
	public String getSector2() {
		return target_sector2;
	}
	public void setSector2(String sector2) {
		this.target_sector2 = sector2;
	}
	public String getSector3() {
		return target_sector3;
	}
	public void setSector3(String sector3) {
		this.target_sector3 = sector3;
	}
	public String getSector4() {
		return target_sector4;
	}
	public void setSector4(String sector4) {
		this.target_sector4 = sector4;
	}
	public String getTarget_type() {
		return target_type;
	}
	public void setTarget_type(String target_type) {
		this.target_type = target_type;
	}
}
