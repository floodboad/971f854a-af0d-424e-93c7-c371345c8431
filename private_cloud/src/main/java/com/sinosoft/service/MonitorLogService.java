package com.sinosoft.service;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public interface MonitorLogService {
	/**
	 * get monitor logs in pagination style.
	 * 
	 * @param username
	 *            - name of user who perform the action
	 * @param role
	 *            - user role
	 * @param pageNo
	 *            - page number
	 * @param pageSize
	 *            - number of monitor logs in each page
	 * @param monitorSource
	 *            - monitor source
	 * @param monitorType
	 *            - service type
	 * @param monitorName
	 *            - monitor name
	 * @param monitorStatus
	 *            - monitor status
	 * @param sourceId
	 *            - source id
	 * @param beginningUpdateTime
	 *            - update time start from
	 * @param endUpdateTime
	 *            - update time end with
	 * @return map contains monitor logs, page number and total page count
	 * @throws ParseException
	 * @author xiangqian
	 */
	public Map<String, Object> getMonitorLogEntries(String username, String role, int pageNo, int pageSize,
			String monitorSource, String monitorType, String monitorName, String monitorStatus, String sourceId,
			String beginningUpdateTime, String endUpdateTime) throws ParseException;

	/**
	 * get monitor logs.
	 * 
	 * @param username
	 *            - name of user who perform the action
	 * @param role
	 *            - user role
	 * @param monitorSource
	 *            - monitor source
	 * @param monitorType
	 *            - service type
	 * @param monitorName
	 *            - monitor name
	 * @param monitorStatus
	 *            - monitor status
	 * @param sourceId
	 *            - source id
	 * @param beginningUpdateTime
	 *            - update time start from
	 * @param endUpdateTime
	 *            - update time end with
	 * @return monitor log list
	 * @throws ParseException
	 * @author xiangqian
	 */
	public List<com.sinosoft.type.MonitorLog> getMonitorLogs(String username, String role, String monitorSource,
			String monitorType, String monitorName, String monitorStatus, String sourceId, String beginningUpdateTime,
			String endUpdateTime) throws ParseException;
}
