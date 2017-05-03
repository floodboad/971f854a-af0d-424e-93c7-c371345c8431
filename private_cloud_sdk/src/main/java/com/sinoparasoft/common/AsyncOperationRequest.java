package com.sinoparasoft.common;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AsyncOperationRequest {
	private static Logger logger = LoggerFactory.getLogger(AsyncOperationRequest.class);
	
	private AsyncOperationTypeEnum operationType;
	private String operator;
	private String virtualMachineId;
	private String diskId;
	private String imageId;
	private String hypervisorName;
	private Date requestTime; 
	
	public AsyncOperationTypeEnum getOperationType() {
		return operationType;
	}
	public void setOperationType(AsyncOperationTypeEnum operationType) {
		this.operationType = operationType;
	}
	public String getOperator() {
		return operator;
	}
	public void setOperator(String operator) {
		this.operator = operator;
	}
	public String getVirtualMachineId() {
		return virtualMachineId;
	}
	public void setVirtualMachineId(String virtualMachineId) {
		this.virtualMachineId = virtualMachineId;
	}
	public String getDiskId() {
		return diskId;
	}
	public void setDiskId(String diskId) {
		this.diskId = diskId;
	}
	public String getImageId() {
		return imageId;
	}
	public void setImageId(String imageId) {
		this.imageId = imageId;
	}
	public String getHypervisorName() {
		return hypervisorName;
	}
	public void setHypervisorName(String hypervisorName) {
		this.hypervisorName = hypervisorName;
	}
	public Date getRequestTime() {
		return requestTime;
	}
	public void setRequestTime(Date requestTime) {
		this.requestTime = requestTime;
	}
	
	public String toString() {
		ObjectMapper mapper = new ObjectMapper();
		String output = "";
		try {
			output = mapper.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			// suppress the exception
			logger.error("输出异步操作请求发生错误", e);
		}
		return output;
	}	
}
