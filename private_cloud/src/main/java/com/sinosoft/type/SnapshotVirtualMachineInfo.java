package com.sinosoft.type;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SnapshotVirtualMachineInfo {
	private String name;
	@JsonProperty("applications")
	private List<String> applicationTagNames;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<String> getApplicationTagNames() {
		return applicationTagNames;
	}
	public void setApplicationTagNames(List<String> applicationTagNames) {
		this.applicationTagNames = applicationTagNames;
	}
}
