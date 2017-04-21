package com.sinosoft.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sinosoft.enumerator.MonitorTypeEnum;
import com.sinosoft.type.MonitorSetting;

@Service
public interface MonitorSettingService {
	/**
	 * get monitor setting value list.
	 * 
	 * @param sourceId - source id
	 * @param monitorType - monitor type
	 * @return monitor setting value list
	 * @author xiangqian
	 */
	public List<MonitorSetting> getMonitorSettings(String sourceId, MonitorTypeEnum monitorType);
}
