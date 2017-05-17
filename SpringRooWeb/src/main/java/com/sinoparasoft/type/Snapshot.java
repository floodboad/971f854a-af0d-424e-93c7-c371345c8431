package com.sinoparasoft.type;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Snapshot {
	private String id;
	private String name;
	private String description;
	private String creator;
	@JsonProperty("create_time")
	private String createTime;
	// size in GB
	private long size;
	private String status;
	@JsonProperty("virtual_machine")
	private SnapshotVirtualMachineInfo virtualMachineInfo;
	
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
	public String getCreateTime() {
		return createTime;
	}
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public SnapshotVirtualMachineInfo getVirtualMachineInfo() {
		return virtualMachineInfo;
	}
	public void setVirtualMachineInfo(SnapshotVirtualMachineInfo virtualMachineInfo) {
		this.virtualMachineInfo = virtualMachineInfo;
	}
}
