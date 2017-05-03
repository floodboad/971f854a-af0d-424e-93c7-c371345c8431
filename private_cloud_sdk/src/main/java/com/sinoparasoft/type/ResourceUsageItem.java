package com.sinoparasoft.type;

public class ResourceUsageItem {
	private long quota;
	private long used;
	private long unused;
	
	public long getQuota() {
		return quota;
	}
	public void setQuota(long quota) {
		this.quota = quota;
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
