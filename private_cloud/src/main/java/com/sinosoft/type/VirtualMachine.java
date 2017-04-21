package com.sinosoft.type;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VirtualMachine {
	private String id;
	private String name;
	private String description;
	private String status;
	private String creator;
	private String manager;
	@JsonProperty("create_time")
	private String createTime;
	@JsonProperty("modify_time")
	private String modifyTime;
	@JsonProperty("domain_id")
	private String domainId;
	@JsonProperty("domain_name")
	private String domainName;
	@JsonProperty("group_id")
	private String groupId;
	@JsonProperty("group_name")
	private String groupName;
	private int cpu;
	private int memory;
	private int disk;
	@JsonProperty("image_name")
	private String imageName;
	@JsonProperty("private_ip")
	private String privateIp;
	@JsonProperty("floating_ip")
	private String floatingIp;
	@JsonProperty("physical_machine")
	private String physicalMachine;
	@JsonProperty("monitor_status")
	private String monitorStatus;
	private List<ApplicationTag> applications;
	
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
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getCreator() {
		return creator;
	}
	public void setCreator(String creator) {
		this.creator = creator;
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
	public String getDomainId() {
		return domainId;
	}
	public void setDomainId(String domainId) {
		this.domainId = domainId;
	}
	public String getDomainName() {
		return domainName;
	}
	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}
	public String getGroupId() {
		return groupId;
	}
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	public int getCpu() {
		return cpu;
	}
	public void setCpu(int cpu) {
		this.cpu = cpu;
	}
	public int getMemory() {
		return memory;
	}
	public void setMemory(int memory) {
		this.memory = memory;
	}
	public int getDisk() {
		return disk;
	}
	public void setDisk(int disk) {
		this.disk = disk;
	}
	public String getImageName() {
		return imageName;
	}
	public void setImageName(String imageName) {
		this.imageName = imageName;
	}
	public String getPrivateIp() {
		return privateIp;
	}
	public void setPrivateIp(String privateIp) {
		this.privateIp = privateIp;
	}
	public String getFloatingIp() {
		return floatingIp;
	}
	public void setFloatingIp(String floatingIp) {
		this.floatingIp = floatingIp;
	}
	public String getPhysicalMachine() {
		return physicalMachine;
	}
	public void setPhysicalMachine(String physicalMachine) {
		this.physicalMachine = physicalMachine;
	}
	public String getMonitorStatus() {
		return monitorStatus;
	}
	public void setMonitorStatus(String monitorStatus) {
		this.monitorStatus = monitorStatus;
	}
	public List<ApplicationTag> getApplications() {
		return applications;
	}
	public void setApplications(List<ApplicationTag> applications) {
		this.applications = applications;
	}
}
