package com.sinosoft.type;

public class PartitionedResourceUsageItem {
	private long quota;
	private long allocated;
	private long unallocated;
	private long used;
	private long unused;
	
	public long getQuota() {
		return quota;
	}
	public void setQuota(long quota) {
		this.quota = quota;
	}
	public long getAllocated() {
		return allocated;
	}
	public void setAllocated(long allocated) {
		this.allocated = allocated;
	}
	public long getUnallocated() {
		return unallocated;
	}
	public void setUnallocated(long unallocated) {
		this.unallocated = unallocated;
	}
	public long getUsed() {
		return used;
	}
	public void setUsed(long used) {
		this.used = used;
	}
	public long getUnused() {
		return unused;
	}
	public void setUnused(long unused) {
		this.unused = unused;
	}
}
