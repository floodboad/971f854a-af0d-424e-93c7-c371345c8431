package com.sinoparasoft.ceph;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CephStatus {
	private String clusterId; 
	private String overallStatus;
	private String summaryMessage;
	private int osdCount;
	private int osdInCount;
	private int osdUpCount;
	private int pgCount;
	// size in GB
	private int totalSize;
	private int usedSize;
	private int availableSize;
	
	public String getClusterId() {
		return clusterId;
	}
	public void setClusterId(String clusterId) {
		this.clusterId = clusterId;
	}
	public String getOverallStatus() {
		return overallStatus;
	}
	public void setOverallStatus(String overallStatus) {
		this.overallStatus = overallStatus;
	}
	public String getSummaryMessage() {
		return summaryMessage;
	}
	public void setSummaryMessage(String summaryMessage) {
		this.summaryMessage = summaryMessage;
	}
	public int getOsdCount() {
		return osdCount;
	}
	public void setOsdCount(int osdCount) {
		this.osdCount = osdCount;
	}
	public int getOsdInCount() {
		return osdInCount;
	}
	public void setOsdInCount(int osdInCount) {
		this.osdInCount = osdInCount;
	}
	public int getOsdUpCount() {
		return osdUpCount;
	}
	public void setOsdUpCount(int osdUpCount) {
		this.osdUpCount = osdUpCount;
	}
	public int getPgCount() {
		return pgCount;
	}
	public void setPgCount(int pgCount) {
		this.pgCount = pgCount;
	}
	public int getTotalSize() {
		return totalSize;
	}
	public void setTotalSize(int totalSize) {
		this.totalSize = totalSize;
	}
	public int getUsedSize() {
		return usedSize;
	}
	public void setUsedSize(int usedSize) {
		this.usedSize = usedSize;
	}
	public int getAvailableSize() {
		return availableSize;
	}
	public void setAvailableSize(int availableSize) {
		this.availableSize = availableSize;
	}

	public String toString() {
		ObjectMapper mapper = new ObjectMapper();
		String output = "";
		try {
			output = mapper.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			// suppress the exception
			e.printStackTrace();
		}
		return output;
	}
}
