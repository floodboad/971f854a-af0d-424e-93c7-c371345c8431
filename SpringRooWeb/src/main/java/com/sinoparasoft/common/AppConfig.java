package com.sinoparasoft.common;

import org.springframework.stereotype.Component;

@Component
public class AppConfig {
	private int portalWaitTimeout;
	private int portalLoopInterval;
	
	private String[] gangliaServers;
	
	private String cephRestApiUrl;
	private int cephPoolReplicaSize;
	private int cephImagePoolSize;
	private int cephJounralSize;

	public int getPortalWaitTimeout() {
		return portalWaitTimeout;
	}

	public void setPortalWaitTimeout(int portalWaitTimeout) {
		this.portalWaitTimeout = portalWaitTimeout;
	}

	public int getPortalLoopInterval() {
		return portalLoopInterval;
	}

	public void setPortalLoopInterval(int portalLoopInterval) {
		this.portalLoopInterval = portalLoopInterval;
	}

	public String[] getGangliaServers() {
		return gangliaServers;
	}

	public void setGangliaServers(String[] gangliaServers) {
		this.gangliaServers = gangliaServers;
	}

	public String getCephRestApiUrl() {
		return cephRestApiUrl;
	}

	public void setCephRestApiUrl(String cephRestApiUrl) {
		this.cephRestApiUrl = cephRestApiUrl;
	}

	public int getCephPoolReplicaSize() {
		return cephPoolReplicaSize;
	}

	public void setCephPoolReplicaSize(int cephPoolReplicaSize) {
		this.cephPoolReplicaSize = cephPoolReplicaSize;
	}

	public int getCephImagePoolSize() {
		return cephImagePoolSize;
	}

	public void setCephImagePoolSize(int cephImagePoolSize) {
		this.cephImagePoolSize = cephImagePoolSize;
	}

	public int getCephJounralSize() {
		return cephJounralSize;
	}

	public void setCephJounralSize(int cephJounralSize) {
		this.cephJounralSize = cephJounralSize;
	}
}
