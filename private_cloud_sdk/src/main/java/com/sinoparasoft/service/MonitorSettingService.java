package com.sinoparasoft.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sinoparasoft.enumerator.MonitorTypeEnum;
import com.sinoparasoft.type.MonitorSetting;

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
