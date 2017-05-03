package com.sinoparasoft.nagios;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.ws.rs.core.MediaType;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jboss.resteasy.client.ClientExecutor;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.core.executors.ApacheHttpClient4Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinoparasoft.nagios.exception.NagiosException;

public class NagiosMonitor {
	private final Logger logger = LoggerFactory.getLogger(NagiosMonitor.class);
	
	private NagiosConfig config;
	private ClientExecutor executor;

	public NagiosMonitor(NagiosConfig config) {
		this.config = config;

		DefaultHttpClient httpClient = new DefaultHttpClient();
		httpClient.getCredentialsProvider().setCredentials(
				new AuthScope(config.getNagiosHost(), config.getNagiosPort()),
				new UsernamePasswordCredentials(config.getNagiosUserName(), config.getNagiosPassword()));
		this.executor = new ApacheHttpClient4Executor(httpClient);
	}

	/**
	 * get service status of specified host
	 * 
	 * @param hostName
	 * @return
	 */
	public List<ServiceStatus> getHostServiceStatus(String hostName) {
		List<ServiceStatus> serviceStatusList = new ArrayList<ServiceStatus>();

		ClientRequest request = new ClientRequest(config.getNagiosUrl()
				+ "/cgi-bin/statusjson.cgi?query=servicelist&hostname=" + hostName, executor)
				.accept(MediaType.APPLICATION_JSON);
		ClientResponse<String> response = null;
		try {
			response = request.get(String.class);
		} catch (Exception e1) {
			throw new NagiosException("请求服务出错。" + e1.getMessage(), e1);
		}
		// int responseCode = response.getResponseStatus().getStatusCode();
		String message = response.getEntity();
		logger.trace(message);
 		
		ObjectMapper mapper = new ObjectMapper();
		JsonNode hostNode = null;
		try {
			hostNode = mapper.readTree(message).get("data").get("servicelist").get(hostName);
		} catch (JsonProcessingException e) {
			throw new NagiosException("解析数据出错。" + e.getMessage(), e);
		} catch (IOException e) {
			throw new NagiosException("解析数据出错。" + e.getMessage(), e);
		}
		
		Iterator<Entry<String, JsonNode>> serviceStatusFields = hostNode.fields();		
		while (serviceStatusFields.hasNext()) {
			Entry<String, JsonNode> serviceStatusField = serviceStatusFields.next();
			// System.out.println(statusField.getKey());
			// System.out.println(statusField.getValue().asInt());
			
			ServiceStatus serviceStatus = new ServiceStatus();
			serviceStatus.setServiceName(serviceStatusField.getKey());
			serviceStatus.setServiceStatus(ServiceStatusEnum.valueOf(serviceStatusField.getValue().asInt()));
			serviceStatusList.add(serviceStatus);
		}

		return serviceStatusList;
	}

}
