// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package com.sinoparasoft.model;

import com.sinoparasoft.model.OperationLog;
import com.sinosoft.enumerator.OperationSeverityEnum;
import com.sinosoft.enumerator.OperationStatusEnum;
import com.sinosoft.enumerator.ServiceNameEnum;
import java.util.Date;

privileged aspect OperationLog_Roo_JavaBean {
    
    public String OperationLog.getOperator() {
        return this.operator;
    }
    
    public void OperationLog.setOperator(String operator) {
        this.operator = operator;
    }
    
    public Date OperationLog.getOperationTime() {
        return this.operationTime;
    }
    
    public void OperationLog.setOperationTime(Date operationTime) {
        this.operationTime = operationTime;
    }
    
    public String OperationLog.getOperation() {
        return this.operation;
    }
    
    public void OperationLog.setOperation(String operation) {
        this.operation = operation;
    }
    
    public OperationStatusEnum OperationLog.getOperationStatus() {
        return this.operationStatus;
    }
    
    public void OperationLog.setOperationStatus(OperationStatusEnum operationStatus) {
        this.operationStatus = operationStatus;
    }
    
    public ServiceNameEnum OperationLog.getServiceName() {
        return this.serviceName;
    }
    
    public void OperationLog.setServiceName(ServiceNameEnum serviceName) {
        this.serviceName = serviceName;
    }
    
    public String OperationLog.getObjectId() {
        return this.objectId;
    }
    
    public void OperationLog.setObjectId(String objectId) {
        this.objectId = objectId;
    }
    
    public String OperationLog.getOperationResult() {
        return this.operationResult;
    }
    
    public void OperationLog.setOperationResult(String operationResult) {
        this.operationResult = operationResult;
    }
    
    public OperationSeverityEnum OperationLog.getSeverityLevel() {
        return this.severityLevel;
    }
    
    public void OperationLog.setSeverityLevel(OperationSeverityEnum severityLevel) {
        this.severityLevel = severityLevel;
    }
    
}
