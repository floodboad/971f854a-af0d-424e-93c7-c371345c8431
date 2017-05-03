package com.sinoparasoft.type;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DomainOverallResourceUsage {
	@JsonProperty("domain")
	private DomainResourceUsage domainResourceUsage;
	@JsonProperty("groups")
	private List<GroupResourceUsage> groupResourceUsages;
	
	public DomainResourceUsage getDomainResourceUsage() {
		return domainResourceUsage;
	}
	public void setDomainResourceUsage(DomainResourceUsage resourceUsage) {
		this.domainResourceUsage = resourceUsage;
	}
	public List<GroupResourceUsage> getGroupResourceUsages() {
		return groupResourceUsages;
	}
	public void setGroupResourceUsages(List<GroupResourceUsage> groupResourceUsages) {
		this.groupResourceUsages = groupResourceUsages;
	}
}
