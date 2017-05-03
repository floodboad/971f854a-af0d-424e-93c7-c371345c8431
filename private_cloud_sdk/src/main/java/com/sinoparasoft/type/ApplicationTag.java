package com.sinoparasoft.type;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ApplicationTag {
	private String id;
	private String name;
	private String description;
	private String creator;
	@JsonProperty("create_time")
	private String createTime;
	private boolean enabled;
	@JsonProperty("virtual_machines")
	private List<ApplicationTagVirtualMachineInfo> virtualMachineInfo;

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

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public List<ApplicationTagVirtualMachineInfo> getVirtualMachineInfo() {
		return virtualMachineInfo;
	}

	public void setVirtualMachineInfo(List<ApplicationTagVirtualMachineInfo> virtualMachineInfo) {
		this.virtualMachineInfo = virtualMachineInfo;
	}
}
