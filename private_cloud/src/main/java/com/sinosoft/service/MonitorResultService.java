package com.sinosoft.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.sinosoft.enumerator.MonitorNameEnum;
import com.sinosoft.enumerator.MonitorTypeEnum;

@Service
public interface MonitorResultService {
	/**
	 * get monitor result.
	 * 
	 * @param hostId
	 *            - host id
	 * @param monitorType
	 *            - monitor type
	 * @param monitorName
	 *            - monitor name
	 * @return monitor result, or null if monitor result not found
	 * @author xiangqian
	 */
	public Map<String, Object> getMonitorResult(String hostId, MonitorTypeEnum monitorType,
			MonitorNameEnum monitorName);

	/**
	 * get monitor result list by host and type.
	 * 
	 * @param hostId
	 *            - host id
	 * @param monitorType
	 *            - monitor type
	 * @return list of monitor results
	 * @author xiangqian
	 */
	public List<Map<String, Object>> getMonitorResults(String hostId, MonitorTypeEnum monitorType);
}
