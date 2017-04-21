package com.sinosoft.type;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MigrationPolicy {
	@JsonProperty("virtual_machine")
	private VirtualMachine virtualMachine;
	@JsonProperty("host_name")
	private String hypervisorName;

	public VirtualMachine getVirtualMachine() {
		return virtualMachine;
	}

	public void setVirtualMachine(VirtualMachine virtualMachine) {
		this.virtualMachine = virtualMachine;
	}

	public String getHypervisorName() {
		return hypervisorName;
	}

	public void setHypervisorName(String hypervisorName) {
		this.hypervisorName = hypervisorName;
	}
}
