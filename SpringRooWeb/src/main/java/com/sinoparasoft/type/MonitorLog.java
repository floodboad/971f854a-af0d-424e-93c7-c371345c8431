package com.sinoparasoft.type;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MonitorLog {
	private long id;
	@JsonProperty("monitor_source")
	private String monitorSource;
	@JsonProperty("source_id")
	private String sourceId;
	@JsonProperty("source_name")
	private String sourceName;
	@JsonProperty("monitor_type")
	private String monitorType;
	@JsonProperty("monitor_name")
	private String monitorName;
	@JsonProperty("monitor_status")
	private String monitorStatus;
	private String message;
	@JsonProperty("severity_level")
	private String severityLevel;
	@JsonProperty("update_time")
	private String updateTime;
	@JsonProperty("check_time")
	private String checkTime;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getMonitorSource() {
		return monitorSource;
	}
	public void setMonitorSource(String monitorSource) {
		this.monitorSource = monitorSource;
	}
	public String getSourceId() {
		return sourceId;
	}
	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}
	public String getSourceName() {
		return sourceName;
	}
	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}
	public String getMonitorType() {
		return monitorType;
	}
	public void setMonitorType(String monitorType) {
		this.monitorType = monitorType;
	}
	public String getMonitorName() {
		return monitorName;
	}
	public void setMonitorName(String monitorName) {
		this.monitorName = monitorName;
	}
	public String getMonitorStatus() {
		return monitorStatus;
	}
	public void setMonitorStatus(String monitorStatus) {
		this.monitorStatus = monitorStatus;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getSeverityLevel() {
		return severityLevel;
	}
	public void setSeverityLevel(String severityLevel) {
		this.severityLevel = severityLevel;
	}
	public String getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}
	public String getCheckTime() {
		return checkTime;
	}
	public void setCheckTime(String checkTime) {
		this.checkTime = checkTime;
	}
}
