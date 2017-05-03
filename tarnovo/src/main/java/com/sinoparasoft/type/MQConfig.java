package com.sinoparasoft.type;

public class MQConfig {	
	private String rabbitmqHost;
	private String rabbitmqUsername;
	private String rabbitmqPassword;
	private String rabbitmqVirtualHost;

	public String getRabbitmqHost() {
		return rabbitmqHost;
	}

	public void setRabbitmqHost(String rabbitmqHost) {
		this.rabbitmqHost = rabbitmqHost;
	}

	public String getRabbitmqUsername() {
		return rabbitmqUsername;
	}

	public void setRabbitmqUsername(String rabbitmqUsername) {
		this.rabbitmqUsername = rabbitmqUsername;
	}

	public String getRabbitmqPassword() {
		return rabbitmqPassword;
	}

	public void setRabbitmqPassword(String rabbitmqPassword) {
		this.rabbitmqPassword = rabbitmqPassword;
	}

	public String getRabbitmqVirtualHost() {
		return rabbitmqVirtualHost;
	}

	public void setRabbitmqVirtualHost(String rabbitmqVirtualHost) {
		this.rabbitmqVirtualHost = rabbitmqVirtualHost;
	}
}
