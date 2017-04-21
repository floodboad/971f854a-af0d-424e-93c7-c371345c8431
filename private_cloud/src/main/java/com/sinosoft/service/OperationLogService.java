package com.sinosoft.service;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.sinosoft.enumerator.OperationSeverityEnum;
import com.sinosoft.enumerator.OperationStatusEnum;
import com.sinosoft.enumerator.ServiceNameEnum;
import com.sinosoft.type.OperationLog;

@Service
public interface OperationLogService {
	/**
	 * save operation log.
	 * 
	 * @param operator
	 *            - name of user who perform the operation
	 * @param operationTime
	 *            - operation time
	 * @param operation
	 *            - operation brief description
	 * @param serviceName
	 *            - service name
	 * @param objectId
	 *            - id of object being operated
	 * @param operationResult
	 *            - operation result
	 * @param operationSeverity
	 *            - severity level
	 * @author xiangqian
	 */
	public void saveLog(String operator, Date operationTime, String operation, OperationStatusEnum operationStatus,
			ServiceNameEnum serviceName, String objectId, String operationResult,
			OperationSeverityEnum operationSeverity);

	/**
	 * get operation log list.
	 * 
	 * @param username
	 *            - name of user who perform the operation
	 * @param role
	 *            - usr role
	 * @param serviceName
	 *            - service name
	 * @param operator
	 *            - operator name
	 * @param operationSeverity
	 *            - severity level
	 * @param objectId
	 *            - object id
	 * @param startTime
	 *            - operation time start from
	 * @param endTime
	 *            - operation time end with
	 * @param pageNo
	 *            - page number
	 * @param pageSize
	 *            - number of monitor logs in each page
	 * @return map contains operation logs, page number and total page count
	 * @throws ParseException
	 * @author xiangqian
	 */
	public Map<String, Object> getOperationLogEntries(String username, String role, String serviceName,
			String operator, String operationStatus, String operationSeverity, String objectId, String startTime,
			String endTime, int pageNo, int pageSize) throws ParseException;

	/**
	 * get operation log list.
	 * 
	 * @param username
	 *            - name of user who perform the operation
	 * @param role
	 *            - usr role
	 * @param serviceName
	 *            - service name
	 * @param operator
	 *            - operator name
	 * @param operationSeverity
	 *            - severity level
	 * @param objectId
	 *            - object id
	 * @param startTime
	 *            - operation time start from
	 * @param endTime
	 *            - operation time end with
	 * @return operation log list
	 * @throws ParseException
	 * @author xiangqian
	 */
	public List<OperationLog> getOperationLogs(String username, String role, String serviceName, String operator,
			String operationStatus, String operationSeverity, String objectId, String startTime, String endTime)
			throws ParseException;
}
