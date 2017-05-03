package com.sinoparasoft.nagios;

public class ServiceStatus {
	private String serviceName;
	private ServiceStatusEnum serviceStatus;
	
	public String getServiceName() {
		return serviceName;
	}
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	public ServiceStatusEnum getServiceStatus() {
		return serviceStatus;
	}
	public void setServiceStatus(ServiceStatusEnum serviceStatus) {
		this.serviceStatus = serviceStatus;
	}
}
