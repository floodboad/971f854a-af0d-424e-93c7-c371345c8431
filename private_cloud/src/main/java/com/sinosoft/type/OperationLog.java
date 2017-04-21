package com.sinosoft.type;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OperationLog {
	private long id;
	private String operator;
	@JsonProperty("operator_department")
	private String operatorDepartment;
	@JsonProperty("operation_time")
	private String operationTime;
	private String operation;
	@JsonProperty("operation_status")
	private String operationStatus;
	@JsonProperty("service_name")
	private String serviceName;
	@JsonProperty("object_id")
	private String objectId;
	@JsonProperty("object_name")
	private String objectName;
	@JsonProperty("domain_name")
	private String virtualMachineDomainName;
	@JsonProperty("group_name")
	private String virtualMachineGroupName;
	@JsonProperty("operation_result")
	private String operationResult;
	@JsonProperty("severity_level")
	private String severityLevel;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public String getOperatorDepartment() {
		return operatorDepartment;
	}

	public void setOperatorDepartment(String operatorDepartment) {
		this.operatorDepartment = operatorDepartment;
	}

	public String getOperationTime() {
		return operationTime;
	}

	public void setOperationTime(String operationTime) {
		this.operationTime = operationTime;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public String getOperationStatus() {
		return operationStatus;
	}

	public void setOperationStatus(String operationStatus) {
		this.operationStatus = operationStatus;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getObjectId() {
		return objectId;
	}

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	public String getObjectName() {
		return objectName;
	}

	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}

	public String getVirtualMachineDomainName() {
		return virtualMachineDomainName;
	}

	public void setVirtualMachineDomainName(String virtualMachineDomainName) {
		this.virtualMachineDomainName = virtualMachineDomainName;
	}

	public String getVirtualMachineGroupName() {
		return virtualMachineGroupName;
	}
	
	public String getOperationResult() {
		return operationResult;
	}

	public void setOperationResult(String operationResult) {
		this.operationResult = operationResult;
	}

	public void setVirtualMachineGroupName(String virtualMachineGroupName) {
		this.virtualMachineGroupName = virtualMachineGroupName;
	}

	public String getSeverityLevel() {
		return severityLevel;
	}

	public void setSeverityLevel(String severityLevel) {
		this.severityLevel = severityLevel;
	}
}
