package com.sinoparasoft.config;

import org.springframework.stereotype.Component;

@Component
public class NagiosConfig {
	private String nagiosHost;
	private int nagiosPort;
	private String nagiosUserName;
	private String nagiosPassword;
	private String nagiosUrl;
	
	public String getNagiosHost() {
		return nagiosHost;
	}
	public void setNagiosHost(String nagiosHost) {
		this.nagiosHost = nagiosHost;
	}
	public int getNagiosPort() {
		return nagiosPort;
	}
	public void setNagiosPort(int nagiosPort) {
		this.nagiosPort = nagiosPort;
	}	
	public String getNagiosUserName() {
		return nagiosUserName;
	}
	public void setNagiosUserName(String nagiosUserName) {
		this.nagiosUserName = nagiosUserName;
	}
	public String getNagiosPassword() {
		return nagiosPassword;
	}
	public void setNagiosPassword(String nagiosPassword) {
		this.nagiosPassword = nagiosPassword;
	}
	public String getNagiosUrl() {
		return nagiosUrl;
	}
	public void setNagiosUrl(String nagiosUrl) {
		this.nagiosUrl = nagiosUrl;
	}

}
