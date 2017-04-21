package com.sinosoft.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.sinosoft.enumerator.AlarmSeverityEnum;
import com.sinosoft.enumerator.MonitorSourceEnum;
import com.sinosoft.enumerator.MonitorTypeEnum;
import com.sinosoft.model.Monitor;
import com.sinosoft.model.MonitorSetting;

@Service
public class MonitorSettingServiceImpl implements MonitorSettingService {
	public List<com.sinosoft.type.MonitorSetting> getMonitorSettings(String sourceId, MonitorTypeEnum monitorType) {
		List<com.sinosoft.type.MonitorSetting> monitorSettingValues = new ArrayList<com.sinosoft.type.MonitorSetting>();

		List<MonitorSetting> settings = MonitorSetting.getMonitorSettings(sourceId, MonitorTypeEnum.LOAD);
		for (MonitorSetting monitorSetting : settings) {
			com.sinosoft.type.MonitorSetting monitorSettingValue = buildMonitorSettingValue(monitorSetting);
			monitorSettingValues.add(monitorSettingValue);
		}

		return monitorSettingValues;
	}

	/**
	 * build monitor setting value can be used in json format.
	 * 
	 * @param monitorSetting
	 *            - monitor setting model
	 * @return monitor setting value
	 * @author xiangqian
	 */
	private com.sinosoft.type.MonitorSetting buildMonitorSettingValue(MonitorSetting monitorSetting) {
		com.sinosoft.type.MonitorSetting monitorSettingValue = new com.sinosoft.type.MonitorSetting();
		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		MonitorSourceEnum monitorSource = monitorSetting.getMonitorSource();
		switch (monitorSource) {
		case PHYSICAL_MACHINE:
			monitorSettingValue.setMonitorSource("物理服务器");
			break;
		case VIRTUAL_MACHINE:
			monitorSettingValue.setMonitorSource("虚拟机");
			break;
		default:
			monitorSettingValue.setMonitorSource("未知类型");
			break;
		}

		monitorSettingValue.setSourceId(monitorSetting.getSourceId());

		MonitorTypeEnum monitorType = monitorSetting.getMonitorType();
		switch (monitorType) {
		case NODE:
			monitorSettingValue.setMonitorType("主机");
			break;
		case LOAD:
			monitorSettingValue.setMonitorType("负载");
			break;
		case SERVICE:
			monitorSettingValue.setMonitorType("服务");
			break;
		default:
			break;
		}

		monitorSettingValue.setMonitorName(monitorSetting.getMonitorName().name());

		Monitor monitor = Monitor.getMonitor(monitorSetting.getMonitorName());
		if (null != monitor) {
			monitorSettingValue.setMonitorDescription(monitor.getDescription());
		} else {
			monitorSettingValue.setMonitorDescription("未知");
		}

		monitorSettingValue.setThreshold(monitorSetting.getThreshold());
		monitorSettingValue.setThresholdUnit(monitorSetting.getThresholdUnit());

		AlarmSeverityEnum severityLevel = monitorSetting.getSeverityLevel();
		if (null != severityLevel) {
			/*
			 * severity code, so the web page can use it as select option.
			 */
			monitorSettingValue.setSeverityLevel(severityLevel.name());

			// switch (severityLevel) {
			// case LOWEST:
			// monitorSettingValue.setSeverityLevel("最低");
			// break;
			// case BELOW_NORMAL:
			// monitorSettingValue.setSeverityLevel("较低");
			// break;
			// case NORMAL:
			// monitorSettingValue.setSeverityLevel("中等");
			// break;
			// case ABOVE_NORMAL:
			// monitorSettingValue.setSeverityLevel("较高");
			// break;
			// case HIGHEST:
			// monitorSettingValue.setSeverityLevel("最高");
			// break;
			// default:
			// monitorSettingValue.setSeverityLevel("未知");
			// break;
			// }
		} else {
			monitorSettingValue.setSeverityLevel(AlarmSeverityEnum.NORMAL.name());
		}

		monitorSettingValue.setOsAlarmId(monitorSetting.getOsAlarmId());
		monitorSettingValue.setEnabled(monitorSetting.getEnabled());

		if (null != monitorSetting.getCreateTime()) {
			monitorSettingValue.setCreateTime(dateFormatter.format(monitorSetting.getCreateTime()));
		} else {
			monitorSettingValue.setCreateTime("");
		}

		return monitorSettingValue;
	}
}
