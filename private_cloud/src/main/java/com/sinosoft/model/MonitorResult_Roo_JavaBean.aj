// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package com.sinosoft.model;

import com.sinosoft.enumerator.MonitorNameEnum;
import com.sinosoft.enumerator.MonitorSourceEnum;
import com.sinosoft.enumerator.MonitorStatusEnum;
import com.sinosoft.enumerator.MonitorTypeEnum;
import com.sinosoft.model.MonitorResult;
import java.util.Date;

privileged aspect MonitorResult_Roo_JavaBean {
    
    public MonitorSourceEnum MonitorResult.getMonitorSource() {
        return this.monitorSource;
    }
    
    public void MonitorResult.setMonitorSource(MonitorSourceEnum monitorSource) {
        this.monitorSource = monitorSource;
    }
    
    public String MonitorResult.getSourceId() {
        return this.sourceId;
    }
    
    public void MonitorResult.setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }
    
    public MonitorTypeEnum MonitorResult.getMonitorType() {
        return this.monitorType;
    }
    
    public void MonitorResult.setMonitorType(MonitorTypeEnum monitorType) {
        this.monitorType = monitorType;
    }
    
    public MonitorNameEnum MonitorResult.getMonitorName() {
        return this.monitorName;
    }
    
    public void MonitorResult.setMonitorName(MonitorNameEnum monitorName) {
        this.monitorName = monitorName;
    }
    
    public MonitorStatusEnum MonitorResult.getMonitorStatus() {
        return this.monitorStatus;
    }
    
    public void MonitorResult.setMonitorStatus(MonitorStatusEnum monitorStatus) {
        this.monitorStatus = monitorStatus;
    }
    
    public Date MonitorResult.getUpdateTime() {
        return this.updateTime;
    }
    
    public void MonitorResult.setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
    
}
