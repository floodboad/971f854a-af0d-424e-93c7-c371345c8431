package com.sinoparasoft.type;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PhysicalMachine {
	private String id;
	private String name;
	private String description;
	private List<String> types;
	private String status;
	private String ip;
	private int cpu;
	private float memory;
	private float disk;
	@JsonProperty("cpu_load")
	private float cpuLoad;
	@JsonProperty("free_memory")
	private float freeMemory;
	@JsonProperty("free_disk")
	private float freeDisk;
	@JsonProperty("monitor_status")
	private String monitorStatus;
	@JsonProperty("update_time")
	private String updateTime;
	@JsonProperty("hypervisor_id")
	private String hypervisorId;
	@JsonProperty("used_cpu")
	private int usedCpu;

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

	public List<String> getTypes() {
		return types;
	}

	public void setTypes(List<String> types) {
		this.types = types;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getCpu() {
		return cpu;
	}

	public void setCpu(int cpu) {
		this.cpu = cpu;
	}

	public float getMemory() {
		return memory;
	}

	public void setMemory(float memory) {
		this.memory = memory;
	}

	public float getDisk() {
		return disk;
	}

	public void setDisk(float disk) {
		this.disk = disk;
	}

	public float getCpuLoad() {
		return cpuLoad;
	}

	public void setCpuLoad(float cpuLoad) {
		this.cpuLoad = cpuLoad;
	}

	public float getFreeMemory() {
		return freeMemory;
	}

	public void setFreeMemory(float freeMemory) {
		this.freeMemory = freeMemory;
	}

	public float getFreeDisk() {
		return freeDisk;
	}

	public void setFreeDisk(float freeDisk) {
		this.freeDisk = freeDisk;
	}

	public String getMonitorStatus() {
		return monitorStatus;
	}

	public void setMonitorStatus(String monitorStatus) {
		this.monitorStatus = monitorStatus;
	}

	public String getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}

	public String getHypervisorId() {
		return hypervisorId;
	}

	public void setHypervisorId(String hypervisorId) {
		this.hypervisorId = hypervisorId;
	}

	public int getUsedCpu() {
		return usedCpu;
	}

	public void setUsedCpu(int freeCpu) {
		this.usedCpu = freeCpu;
	}
}
