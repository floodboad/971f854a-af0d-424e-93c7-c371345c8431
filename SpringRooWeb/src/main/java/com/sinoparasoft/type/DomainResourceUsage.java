package com.sinoparasoft.type;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DomainResourceUsage {
	@JsonProperty("domain_name")
	private String domainName;
	@JsonProperty("cpu")
	private PartitionedResourceUsageItem cpuUsage;
	@JsonProperty("memory")
	private PartitionedResourceUsageItem memoryUsage;
	@JsonProperty("disk")
	private PartitionedResourceUsageItem diskUsage;
	
	public String getDomainName() {
		return domainName;
	}
	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}
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
	public PartitionedResourceUsageItem getDiskUsage() {
		return diskUsage;
	}
	public void setDiskUsage(PartitionedResourceUsageItem diskUsage) {
		this.diskUsage = diskUsage;
	}
}
