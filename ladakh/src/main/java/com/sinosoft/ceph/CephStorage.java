package com.sinosoft.ceph;

import java.io.IOException;
import java.util.Iterator;

import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinosoft.ceph.exception.CephException;

public class CephStorage {
	private static Logger logger = LoggerFactory.getLogger(CephStorage.class);
	
	private String restApiUrl;

	public CephStorage(String restApiUrl) {
		this.restApiUrl = restApiUrl;
	}
	
	/**
	 * get status of unified storage 
	 * @return storage status
	 * @throws CephException
	 */
	public CephStatus getStatus() {
		CephStatus status = new CephStatus();

		// use resteasy client if reasteasy-xxx-2.x is used
		ClientRequest request = new ClientRequest(restApiUrl + "/status").accept(MediaType.APPLICATION_JSON);
		ClientResponse<String> response = null;
		try {
			response = request.get(String.class);
		} catch (Exception e1) {
			throw new CephException("获取统一存储服务状态数据发生错误。", e1);
		}
		// int responseCode = response.getResponseStatus().getStatusCode();
		String message = response.getEntity();
		logger.trace(message);
 		
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = null;
		try {
			rootNode = mapper.readTree(message).get("output");
		} catch (JsonProcessingException e) {
			throw new CephException("解析统一存储服务状态数据出错。", e);
		} catch (IOException e) {
			throw new CephException("解析统一存储服务状态数据出错。", e);
		}

		status.setClusterId(null == rootNode.get("fsid") ? "" : rootNode.get("fsid").asText());
		
		JsonNode healthNode = rootNode.path("health");
		status.setOverallStatus(null == healthNode.get("overall_status") ? "" : healthNode.get("overall_status").asText());
		Iterator<JsonNode> summaryNodes = healthNode.get("summary").elements();
		if (summaryNodes.hasNext()) {
			JsonNode summaryNode = summaryNodes.next();
			status.setSummaryMessage(null == summaryNode.get("summary") ? "" : summaryNode.get("summary").asText());
		}

		JsonNode osdmapNode = rootNode.path("osdmap");
		status.setOsdCount(null == osdmapNode.get("osdmap").get("num_osds") ? -1 : osdmapNode.get("osdmap").get("num_osds").asInt());
		status.setOsdUpCount(null == osdmapNode.get("osdmap").get("num_up_osds") ? -1 : osdmapNode.get("osdmap").get("num_up_osds").asInt());
		status.setOsdInCount(null == osdmapNode.get("osdmap").get("num_in_osds") ? -1 : osdmapNode.get("osdmap").get("num_in_osds").asInt());
				
		JsonNode pgmapNode = rootNode.path("pgmap");
		status.setPgCount(null == pgmapNode.get("num_pgs") ? -1 : pgmapNode.get("num_pgs").asInt());
		status.setTotalSize(null == pgmapNode.get("bytes_total") ? -1 : (int)(pgmapNode.get("bytes_total").asLong() / 1024 / 1024 / 1024));
		status.setUsedSize(null == pgmapNode.get("bytes_used") ? -1 : (int)(pgmapNode.get("bytes_used").asLong() / 1024 / 1024 / 1024));
		status.setAvailableSize(null == pgmapNode.get("bytes_avail") ? -1 : (int)(pgmapNode.get("bytes_avail").asLong() / 1024 / 1024 / 1024));
		
		return status;
	}
}
