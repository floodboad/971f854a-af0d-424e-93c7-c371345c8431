package com.sinosoft.type;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GroupResourceUsage {
	@JsonProperty("group_name")
	private String groupName;
	@JsonProperty("cpu")
	private ResourceUsageItem cpuUsage;
	@JsonProperty("memory")
	private ResourceUsageItem memoryUsage;
	@JsonProperty("disk")
	private ResourceUsageItem diskUsage;
	
	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	public ResourceUsageItem getCpuUsage() {
		return cpuUsage;
	}
	public void setCpuUsage(ResourceUsageItem cpuUsage) {
		this.cpuUsage = cpuUsage;
	}
	public ResourceUsageItem getMemoryUsage() {
		return memoryUsage;
	}
	public void setMemoryUsage(ResourceUsageItem memoryUsage) {
		this.memoryUsage = memoryUsage;
	}
	public ResourceUsageItem getDiskUsage() {
		return diskUsage;
	}
	public void setDiskUsage(ResourceUsageItem diskUsage) {
		this.diskUsage = diskUsage;
	}
}
