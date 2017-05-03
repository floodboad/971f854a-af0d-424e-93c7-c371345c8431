package com.sinoparasoft.type;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OverviewResourceUsage {
	@JsonProperty("cpu")
	private PartitionedResourceUsageItem cpuUsage;
	@JsonProperty("memory")
	private PartitionedResourceUsageItem memoryUsage;
	@JsonProperty("storage")
	private PartitionedResourceUsageItem storageUsage;
	
	public PartitionedResourceUsageItem getCpuUsage() {
		return cpuUsage;
	}
	public void setCpuUsage(PartitionedResourceUsageItem cpuUsage) {
		this.cpuUsage = cpuUsage;
	}
	public PartitionedResourceUsageItem getMemoryUsage() {
		return memoryUsage;
	}
	public void setMemoryUsage(PartitionedResourceUsageItem memoryUsage) {
		this.memoryUsage = memoryUsage;
	}
	public PartitionedResourceUsageItem getStorageUsage() {
		return storageUsage;
	}
	public void setStorageUsage(PartitionedResourceUsageItem storageUsage) {
		this.storageUsage = storageUsage;
	}
}
