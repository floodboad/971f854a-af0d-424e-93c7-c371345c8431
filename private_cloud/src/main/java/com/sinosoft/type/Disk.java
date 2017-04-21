package com.sinosoft.type;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Disk {
	private String id;
	private String name;
	private String description;
	private String creator;
	private int capacity;
	private String manager;
	@JsonProperty("create_time")
	private String createTime;
	@JsonProperty("modify_time")
	private String modifyTime;
	@JsonProperty("valid_time")
	private String validTime;
	private boolean expired;
	private String status;
	@JsonProperty("attach_host")
	private String attachHost;
	@JsonProperty("attach_point")
	private String attachPoint;
	@JsonProperty("attach_time")
	private String attachTime;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getCreator() {
		return creator;
	}
	public void setCreator(String creator) {
		this.creator = creator;
	}
	public int getCapacity() {
		return capacity;
	}
	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}
	public String getManager() {
		return manager;
	}
	public void setManager(String manager) {
		this.manager = manager;
	}
	public String getCreateTime() {
		return createTime;
	}
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
	public String getModifyTime() {
		return modifyTime;
	}
	public void setModifyTime(String modifyTime) {
		this.modifyTime = modifyTime;
	}
	public String getValidTime() {
		return validTime;
	}
	public void setValidTime(String validTime) {
		this.validTime = validTime;
	}
	public boolean isExpired() {
		return expired;
	}
	public void setExpired(boolean expired) {
		this.expired = expired;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getAttachHost() {
		return attachHost;
	}
	public void setAttachHost(String attachHost) {
		this.attachHost = attachHost;
	}
	public String getAttachPoint() {
		return attachPoint;
	}
	public void setAttachPoint(String attachPoint) {
		this.attachPoint = attachPoint;
	}
	public String getAttachTime() {
		return attachTime;
	}
	public void setAttachTime(String attachTime) {
		this.attachTime = attachTime;
	}
}
