// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package com.sinoparasoft.model;

import com.sinoparasoft.model.MonitorSetting;
import com.sinosoft.enumerator.AlarmSeverityEnum;
import com.sinosoft.enumerator.MonitorNameEnum;
import com.sinosoft.enumerator.MonitorSourceEnum;
import com.sinosoft.enumerator.MonitorTypeEnum;
import java.util.Date;

privileged aspect MonitorSetting_Roo_JavaBean {
    
    public MonitorSourceEnum MonitorSetting.getMonitorSource() {
        return this.monitorSource;
    }
    
    public void MonitorSetting.setMonitorSource(MonitorSourceEnum monitorSource) {
        this.monitorSource = monitorSource;
    }
    
    public String MonitorSetting.getSourceId() {
        return this.sourceId;
    }
    
    public void MonitorSetting.setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }
    
    public MonitorTypeEnum MonitorSetting.getMonitorType() {
        return this.monitorType;
    }
    
    public void MonitorSetting.setMonitorType(MonitorTypeEnum monitorType) {
        this.monitorType = monitorType;
    }
    
    public MonitorNameEnum MonitorSetting.getMonitorName() {
        return this.monitorName;
    }
    
    public void MonitorSetting.setMonitorName(MonitorNameEnum monitorName) {
        this.monitorName = monitorName;
    }
    
    public Float MonitorSetting.getThreshold() {
        return this.threshold;
    }
    
    public void MonitorSetting.setThreshold(Float threshold) {
        this.threshold = threshold;
    }
    
    public String MonitorSetting.getThresholdUnit() {
        return this.thresholdUnit;
    }
    
    public void MonitorSetting.setThresholdUnit(String thresholdUnit) {
        this.thresholdUnit = thresholdUnit;
    }
    
    public AlarmSeverityEnum MonitorSetting.getSeverityLevel() {
        return this.severityLevel;
    }
    
    public void MonitorSetting.setSeverityLevel(AlarmSeverityEnum severityLevel) {
        this.severityLevel = severityLevel;
    }
    
    public String MonitorSetting.getOsAlarmId() {
        return this.osAlarmId;
    }
    
    public void MonitorSetting.setOsAlarmId(String osAlarmId) {
        this.osAlarmId = osAlarmId;
    }
    
    public Boolean MonitorSetting.getEnabled() {
        return this.enabled;
    }
    
    public void MonitorSetting.setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
    
    public Date MonitorSetting.getCreateTime() {
        return this.createTime;
    }
    
    public void MonitorSetting.setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
    
}
