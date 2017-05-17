package com.sinoparasoft.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.sinoparasoft.enumerator.MonitorNameEnum;
import com.sinoparasoft.enumerator.MonitorTypeEnum;
import com.sinoparasoft.model.Monitor;
import com.sinoparasoft.model.MonitorResult;

@Service
public class MonitorResultServiceImpl implements MonitorResultService {
	public Map<String, Object> getMonitorResult(String hostId, MonitorTypeEnum monitorType, MonitorNameEnum monitorName) {
		Map<String, Object> monitorRecord = new HashMap<String, Object>();
		
		MonitorResult result = MonitorResult.getMonitorResult(hostId, monitorType, monitorName);
		if (null != result) {
			monitorRecord =  buildMonitorResult(result);
			return monitorRecord;
		} 
		
		return null;
	}
	
	public List<Map<String, Object>> getMonitorResults(String hostId, MonitorTypeEnum monitorType) {
		List<Map<String, Object>> monitorRecords = new ArrayList<Map<String,Object>>();
		
		List<MonitorResult> results = MonitorResult.getMonitorResults(hostId, monitorType);
		for (MonitorResult result : results) {
			Map<String, Object> monitorRecord = buildMonitorResult(result);
			monitorRecords.add(monitorRecord);
		}
		
		return monitorRecords;
	}
	
	/**
	 * 
	 * @param result
	 * @return
	 * @author xiangqian
	 */
	private Map<String, Object> buildMonitorResult(MonitorResult result) {
		// TODO need monitor result type
		Map<String, Object> monitorRecord = new HashMap<String, Object>();
		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		monitorRecord.put("hostId", result.getSourceId());

		String monitorName = "未知";
		Monitor monitor = Monitor.getMonitor(result.getMonitorName());
		if (null != monitor) {
			monitorName = monitor.getDescription();
		}
		monitorRecord.put("monitorName", monitorName);

		String monitorStatus = "";
		switch (result.getMonitorStatus()) {
		case NORMAL:
			monitorStatus = "正常";
			break;
		case UNKNOWN:
			monitorStatus = "未知";
			break;
		case WARNING:
			monitorStatus = "告警";
			break;
		default:
			monitorStatus = "未知类型";
			break;
		
		}
		monitorRecord.put("monitorStatus", monitorStatus);

		String serviceType = "";
		switch (result.getMonitorType()) {
		case LOAD:
			serviceType = "负载类";
			break;
		case SERVICE:
			serviceType = "服务类";
			break;
		default:
			serviceType = "未知类型";
			break;		
		}
		monitorRecord.put("serviceType", serviceType);

		if (null != result.getUpdateTime()) {
			monitorRecord.put("updateTime", dateFormatter.format(result.getUpdateTime()));
		}
		
		return monitorRecord;
	}
}
