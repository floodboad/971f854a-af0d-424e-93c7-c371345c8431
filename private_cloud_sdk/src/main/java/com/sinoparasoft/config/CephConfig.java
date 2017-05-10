package com.sinoparasoft.config;

import org.springframework.stereotype.Component;

@Component
public class CephConfig {
	private String restApiUrl;

	public String getRestApiUrl() {
		return restApiUrl;
	}

	public void setRestApiUrl(String restApiUrl) {
		this.restApiUrl = restApiUrl;
	}
}
