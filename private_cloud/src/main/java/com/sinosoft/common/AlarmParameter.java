package com.sinosoft.common;

import com.sinosoft.enumerator.AlarmSeverityEnum;

public class AlarmParameter {
	private String alarmName;
	private Boolean enabled;
	private float alarmThreshold;
	private String thresholdUnit;
	private AlarmSeverityEnum severityLevel; 

	public String getAlarmName() {
		return alarmName;
	}

	public void setAlarmName(String alarmName) {
		this.alarmName = alarmName;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public float getAlarmThreshold() {
		return alarmThreshold;
	}

	public void setAlarmThreshold(float alarmThreshold) {
		this.alarmThreshold = alarmThreshold;
	}

	public String getThresholdUnit() {
		return thresholdUnit;
	}

	public void setThresholdUnit(String thresholdUnit) {
		this.thresholdUnit = thresholdUnit;
	}

	public AlarmSeverityEnum getSeverityLevel() {
		return severityLevel;
	}

	public void setSeverityLevel(AlarmSeverityEnum severityLevel) {
		this.severityLevel = severityLevel;
	}
}
