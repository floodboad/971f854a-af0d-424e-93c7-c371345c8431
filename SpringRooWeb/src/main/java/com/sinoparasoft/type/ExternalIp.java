package com.sinoparasoft.type;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ExternalIp {
	private String ip;
	private String status;
	@JsonProperty("device_owner")
	private String deviceOwner;
	@JsonProperty("device_id")
	private String deviceId;
	@JsonProperty("device_name")
	private String deviceName;
	@JsonProperty("domain_id")
	private String domainId;
	@JsonProperty("domain_name")
	private String domainName;

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getDeviceOwner() {
		return deviceOwner;
	}

	public void setDeviceOwner(String deviceOwner) {
		this.deviceOwner = deviceOwner;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public String getDomainId() {
		return domainId;
	}

	public void setDomainId(String domainId) {
		this.domainId = domainId;
	}

	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}
}
