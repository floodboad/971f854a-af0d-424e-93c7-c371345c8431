package com.sinosoft.type;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MonitorSetting {
	@JsonProperty("monitor_source")
	private String monitorSource;
	@JsonProperty("source_id")
	private String sourceId;
	@JsonProperty("monitor_type")
	private String monitorType;
	@JsonProperty("monitor_name")
	private String monitorName;
	@JsonProperty("monitor_description")
	private String monitorDescription;
	private float threshold;
	@JsonProperty("threshold_unit")
	private String thresholdUnit;
	@JsonProperty("severity_level")
	private String severityLevel;
	@JsonProperty("os_alarm_id")
	private String osAlarmId;
	private boolean enabled;
	@JsonProperty("create_time")
	private String createTime;
	
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
	public String getMonitorDescription() {
		return monitorDescription;
	}
	public void setMonitorDescription(String monitorDescription) {
		this.monitorDescription = monitorDescription;
	}
	public float getThreshold() {
		return threshold;
	}
	public void setThreshold(float threshold) {
		this.threshold = threshold;
	}
	public String getThresholdUnit() {
		return thresholdUnit;
	}
	public void setThresholdUnit(String thresholdUnit) {
		this.thresholdUnit = thresholdUnit;
	}
	public String getSeverityLevel() {
		return severityLevel;
	}
	public void setSeverityLevel(String severityLevel) {
		this.severityLevel = severityLevel;
	}
	public String getOsAlarmId() {
		return osAlarmId;
	}
	public void setOsAlarmId(String osAlarmId) {
		this.osAlarmId = osAlarmId;
	}
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	public String getCreateTime() {
		return createTime;
	}
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
}
