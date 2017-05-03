package com.sinoparasoft.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.sinoparasoft.enumerator.AlarmSeverityEnum;
import com.sinoparasoft.enumerator.MonitorSourceEnum;
import com.sinoparasoft.enumerator.MonitorStatusEnum;
import com.sinoparasoft.enumerator.MonitorTypeEnum;
import com.sinoparasoft.model.Monitor;
import com.sinoparasoft.model.MonitorLog;
import com.sinoparasoft.model.PhysicalMachine;
import com.sinoparasoft.model.VirtualMachine;

@Service
public class MonitorLogServiceImpl implements MonitorLogService {
	public Map<String, Object> getMonitorLogEntries(String username, String role, int pageNo, int pageSize,
			String monitorSource, String monitorType, String monitorName, String monitorStatus, String sourceId,
			String beginningUpdateTime, String endUpdateTime) throws ParseException {
		Map<String, Object> map = new HashMap<String, Object>();

		int pageTotal = 0;
		if (pageNo <= 0) {
			pageNo = 1;
		}

		List<MonitorLog> logs = null;
		int totalMonitorLogCount = 0;
		if ((("ROLE_ADMIN").equals(role)) || (("ROLE_MANAGER").equals(role))) {
			int recordCount = (int) MonitorLog.countMonitorLogs(monitorSource, monitorType, monitorName, monitorStatus,
					sourceId, beginningUpdateTime, endUpdateTime);
			totalMonitorLogCount = recordCount;
			recordCount -= 1;
			if (recordCount < 0) {
				recordCount = 0;
			}
			pageTotal = recordCount / pageSize + 1;

			logs = MonitorLog.getMonitorLogEntries(monitorSource, monitorType, monitorName, monitorStatus, sourceId,
					beginningUpdateTime, endUpdateTime, (pageNo - 1) * pageSize, pageSize, "updateTime", "DESC");
		} else {
			int recordCount = (int) MonitorLog.countMonitorLogsByUser(username, monitorSource, monitorType,
					monitorName, monitorStatus, sourceId, beginningUpdateTime, endUpdateTime);
			totalMonitorLogCount = recordCount;
			recordCount -= 1;
			if (recordCount < 0) {
				recordCount = 0;
			}
			pageTotal = recordCount / pageSize + 1;

			logs = MonitorLog.getMonitorLogEntriesByUser(username, monitorSource, monitorType, monitorName,
					monitorStatus, sourceId, beginningUpdateTime, endUpdateTime, (pageNo - 1) * pageSize, pageSize,
					"update_time", "DESC");
		}

		List<com.sinoparasoft.type.MonitorLog> monitorLogs = new ArrayList<com.sinoparasoft.type.MonitorLog>();
		for (MonitorLog log : logs) {
			com.sinoparasoft.type.MonitorLog monitorLogValue = buildMonitorLogValue(log);
			monitorLogs.add(monitorLogValue);
		}

		map.put("pageNo", pageNo);
		map.put("pageTotal", pageTotal);
		map.put("total_monitor_log_count", totalMonitorLogCount);
		map.put("monitor_logs", monitorLogs);

		return map;
	}

	/**
	 * build monitor log value can be used in json format.
	 * 
	 * @param monitorLog
	 *            - monitor log model
	 * @return monitor log value
	 * @author xiangqian
	 */
	private com.sinoparasoft.type.MonitorLog buildMonitorLogValue(MonitorLog monitorLog) {
		com.sinoparasoft.type.MonitorLog monitorLogValue = new com.sinoparasoft.type.MonitorLog();
		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		monitorLogValue.setId(monitorLog.getId());

		String sourceId = monitorLog.getSourceId();
		monitorLogValue.setSourceId(sourceId);

		MonitorSourceEnum monitorSource = monitorLog.getMonitorSource();
		switch (monitorSource) {
		case PHYSICAL_MACHINE:
			monitorLogValue.setMonitorSource("物理服务器");

			PhysicalMachine physicalMachine = PhysicalMachine.findPhysicalMachine(sourceId);
			if (null != physicalMachine) {
				monitorLogValue.setSourceName(physicalMachine.getHostName());
			} else {
				monitorLogValue.setSourceName("已删除");
			}
			break;
		case VIRTUAL_MACHINE:
			monitorLogValue.setMonitorSource("虚拟机");

			VirtualMachine virtualMachine = VirtualMachine.getVirtualMachine(sourceId);
			if (null != virtualMachine) {
				monitorLogValue.setSourceName(virtualMachine.getHostName());
			} else {
				monitorLogValue.setSourceName("已删除");
			}
			break;
		default:
			monitorLogValue.setMonitorSource("未知类型");
			break;
		}

		MonitorTypeEnum monitorType = monitorLog.getMonitorType();
		switch (monitorType) {
		case NODE:
			monitorLogValue.setMonitorType("主机");
			break;
		case LOAD:
			monitorLogValue.setMonitorType("负载");
			break;
		case SERVICE:
			monitorLogValue.setMonitorType("服务");
			break;
		default:
			break;
		}

		Monitor monitor = Monitor.getMonitor(monitorLog.getMonitorName());
		if (null != monitor) {
			monitorLogValue.setMonitorName(monitor.getDescription());
		} else {
			monitorLogValue.setMonitorName("未知");
		}

		MonitorStatusEnum monitorStatus = monitorLog.getMonitorStatus();
		switch (monitorStatus) {
		case NORMAL:
			monitorLogValue.setMonitorStatus("正常");
			break;
		case WARNING:
			monitorLogValue.setMonitorStatus("告警");
			break;
		case UNKNOWN:
			monitorLogValue.setMonitorStatus("未知");
			break;
		default:
			break;
		}

		if (null != monitorLog.getMessage()) {
			monitorLogValue.setMessage(monitorLog.getMessage());
		} else {
			monitorLogValue.setMessage("");
		}

		AlarmSeverityEnum severityLevel = monitorLog.getSeverityLevel();
		if (null != severityLevel) {
			switch (severityLevel) {
			case LOWEST:
				monitorLogValue.setSeverityLevel("最低");
				break;
			case BELOW_NORMAL:
				monitorLogValue.setSeverityLevel("较低");
				break;
			case NORMAL:
				monitorLogValue.setSeverityLevel("中等");
				break;
			case ABOVE_NORMAL:
				monitorLogValue.setSeverityLevel("较高");
				break;
			case HIGHEST:
				monitorLogValue.setSeverityLevel("最高");
				break;
			default:
				monitorLogValue.setSeverityLevel("未知");
				break;
			}
		}

		if (null != monitorLog.getUpdateTime()) {
			monitorLogValue.setUpdateTime(dateFormatter.format(monitorLog.getUpdateTime()));
		} else {
			monitorLogValue.setUpdateTime("");
		}

		if (null != monitorLog.getCheckTime()) {
			monitorLogValue.setCheckTime(dateFormatter.format(monitorLog.getCheckTime()));
		} else {
			monitorLogValue.setCheckTime("");
		}

		return monitorLogValue;
	}

	public List<com.sinoparasoft.type.MonitorLog> getMonitorLogs(String username, String role, String monitorSource,
			String monitorType, String monitorName, String monitorStatus, String sourceId, String beginningUpdateTime,
			String endUpdateTime) throws ParseException {
		List<MonitorLog> logs = null;
		if ((("ROLE_ADMIN").equals(role)) || (("ROLE_MANAGER").equals(role))) {
			logs = MonitorLog.getMonitorLogs(monitorSource, monitorType, monitorName, monitorStatus, sourceId,
					beginningUpdateTime, endUpdateTime, "updateTime", "DESC");
		} else {
			logs = MonitorLog.getMonitorLogsByUser(username, monitorSource, monitorType, monitorName, monitorStatus,
					sourceId, beginningUpdateTime, endUpdateTime, "update_time", "DESC");
		}

		List<com.sinoparasoft.type.MonitorLog> monitorLogs = new ArrayList<com.sinoparasoft.type.MonitorLog>();
		for (MonitorLog log : logs) {
			com.sinoparasoft.type.MonitorLog monitorLogValue = buildMonitorLogValue(log);
			monitorLogs.add(monitorLogValue);
		}

		return monitorLogs;
	}
}
