package com.sinoparasoft.type;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StorageResourceUsage {
	@JsonProperty("virtual_machine")
	private ResourceUsageItem virtualMachineUsage;
	@JsonProperty("image_snapshot")
	private ResourceUsageItem imageSnapshotUsage;
	@JsonProperty("block_storage")
	private ResourceUsageItem blockStorageUsage;
	
	public ResourceUsageItem getVirtualMachineUsage() {
		return virtualMachineUsage;
	}
	public void setVirtualMachineUsage(ResourceUsageItem virtualMachineUsage) {
		this.virtualMachineUsage = virtualMachineUsage;
	}
	public ResourceUsageItem getImageSnapshotUsage() {
		return imageSnapshotUsage;
	}
	public void setImageSnapshotUsage(ResourceUsageItem imageSnapshotUsage) {
		this.imageSnapshotUsage = imageSnapshotUsage;
	}
	public ResourceUsageItem getBlockStorageUsage() {
		return blockStorageUsage;
	}
	public void setBlockStorageUsage(ResourceUsageItem blockStorageUsage) {
		this.blockStorageUsage = blockStorageUsage;
	}
}
