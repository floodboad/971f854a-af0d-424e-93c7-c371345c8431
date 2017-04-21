package com.sinosoft.type;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Image {
	private String id;
	private String name;
	private String owner;
	private String status;
	private long size;
	@JsonProperty("min_disk")
	private long minDisk;
	@JsonProperty("public")
	private boolean isPublic;
	@JsonProperty("create_time")
	private String createTime;
	@JsonProperty("container_format")
	private String containerFormat;
	
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
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	public long getMinDisk() {
		return minDisk;
	}
	public void setMinDisk(long minDisk) {
		this.minDisk = minDisk;
	}
	public boolean isPublic() {
		return isPublic;
	}
	public void setPublic(boolean isPublic) {
		this.isPublic = isPublic;
	}
	public String getCreateTime() {
		return createTime;
	}
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
	public String getContainerFormat() {
		return containerFormat;
	}
	public void setContainerFormat(String containerFormat) {
		this.containerFormat = containerFormat;
	}
	
	
}
