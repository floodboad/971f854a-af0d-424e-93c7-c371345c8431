package com.sinoparasoft.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sinoparasoft.common.AlarmParameter;
import com.sinoparasoft.enumerator.MonitorNameEnum;
import com.sinoparasoft.enumerator.MonitorSourceEnum;
import com.sinoparasoft.enumerator.MonitorTypeEnum;
import com.sinoparasoft.enumerator.PhysicalMachineTypeNameEnum;
import com.sinoparasoft.model.MonitorSetting;
import com.sinoparasoft.model.PhysicalMachine;
import com.sinoparasoft.model.PhysicalMachineLoad;
import com.sinoparasoft.model.PhysicalMachineType;
import com.sinoparasoft.model.VirtualMachine;
import com.sinoparasoft.openstack.type.CloudConfig;

@Service
public class PhysicalMachineServiceImpl implements PhysicalMachineService {
	@Autowired
	CloudConfig cloudConfig;

	@Autowired
	VirtualMachineService virtualMachineService;

	@Autowired
	MonitorResultService monitorResultService;

	public List<com.sinoparasoft.type.PhysicalMachine> getList(PhysicalMachineTypeNameEnum typeName) {
		List<com.sinoparasoft.type.PhysicalMachine> physicalMachineList = new ArrayList<com.sinoparasoft.type.PhysicalMachine>();

		PhysicalMachineType type = PhysicalMachineType.getPhysicalMachineType(typeName);
		if (null == type) {
			return physicalMachineList;
		}

		Set<PhysicalMachine> machines = type.getPhysicalMachines();
		for (PhysicalMachine machine : machines) {
			com.sinoparasoft.type.PhysicalMachine machineValue = buildPhysicalMachineValue(machine);
			physicalMachineList.add(machineValue);
		}
		
		physicalMachineList = sortPhysicalMachineList(physicalMachineList);

		return physicalMachineList;
	}
	
	/**
	 * build physical machine value can be used in json format.
	 * 
	 * @param machine
	 *            physical machine model
	 * @return physical machine value
	 * @author xiangqian
	 */
	private com.sinoparasoft.type.PhysicalMachine buildPhysicalMachineValue(PhysicalMachine machine) {
		com.sinoparasoft.type.PhysicalMachine machineValue = new com.sinoparasoft.type.PhysicalMachine();
	
		machineValue.setId(machine.getHostId());
		machineValue.setName(machine.getHostName());
		machineValue.setDescription(machine.getDescription());
	
		List<String> types = new ArrayList<String>();
		int usedCpu = 0;
		for (PhysicalMachineType type : machine.getTypes()) {
			String typeName = type.getTypeName();
			types.add(typeName);
	
			/*
			 * used cpu count
			 */
			if (typeName.equalsIgnoreCase(PhysicalMachineTypeNameEnum.COMPUTE_NODE.name())) {
				List<VirtualMachine> virtualMachines = VirtualMachine.getVirtualMachinesByHypervisorName(machine
						.getHostName());
				for (VirtualMachine virtualMachine : virtualMachines) {
					com.sinoparasoft.type.VirtualMachine virtualMachineValue = virtualMachineService
							.getVirtualMachineById(virtualMachine.getHostId());
					if (null != virtualMachineValue) {
						usedCpu += virtualMachineValue.getCpu();
					}
				}
			}
		}
		machineValue.setTypes(types);
	
		String status;
		switch (machine.getStatus()) {
		case ACTIVE:
			status = "正常";
			break;
		case SHUTDOWN:
			status = "关机";
			break;
		default:
			status = "未知类型";
			break;
		}
		machineValue.setStatus(status);
	
		machineValue.setIp(machine.getIpAddress());
		machineValue.setCpu(machine.getCpuNumber());
		machineValue.setMemory(machine.getMemorySize());
		machineValue.setDisk(machine.getDiskSize());
	
		// latest load metric
		PhysicalMachineLoad load = PhysicalMachineLoad.getLatestPhyscalMachineLoad(machine.getHostName());
		if (null != load) {
			if (null != load.getCpuLoad()) {
				machineValue.setCpuLoad(load.getCpuLoad());
			} else {
				machineValue.setCpuLoad(-1.0f);
			}
	
			if (null != load.getFreeMemory()) {
				machineValue.setFreeMemory(load.getFreeMemory());
			} else {
				machineValue.setFreeMemory(-1.0f);
			}
	
			if (null != load.getFreeDisk()) {
				machineValue.setFreeDisk(load.getFreeDisk());
			} else {
				machineValue.setFreeDisk(-1.0f);
			}
		} else {
			machineValue.setCpuLoad(-1.0f);
			machineValue.setFreeMemory(-1.0f);
			machineValue.setFreeDisk(-1.0f);
		}
	
		Map<String, Object> monitorResult = monitorResultService.getMonitorResult(machine.getHostId(),
				MonitorTypeEnum.NODE, MonitorNameEnum.NONE);
		if (null != monitorResult) {
			machineValue.setMonitorStatus(monitorResult.get("monitorStatus").toString());
			machineValue.setUpdateTime(monitorResult.get("updateTime").toString());
		} else {
			machineValue.setMonitorStatus("");
			machineValue.setUpdateTime("");
		}
	
		machineValue.setHypervisorId(machine.getHypervisorId());
		machineValue.setUsedCpu(usedCpu);
	
		return machineValue;
	}

	private List<com.sinoparasoft.type.PhysicalMachine> sortPhysicalMachineList(List<com.sinoparasoft.type.PhysicalMachine> machines) {
		Comparator<com.sinoparasoft.type.PhysicalMachine> comparator = new Comparator<com.sinoparasoft.type.PhysicalMachine>() {

			@Override
			public int compare(com.sinoparasoft.type.PhysicalMachine o1, com.sinoparasoft.type.PhysicalMachine o2) {
				String hostName1 = o1.getName();
				String hostName2 = o2.getName();
				return hostName1.compareToIgnoreCase(hostName2);
			}
		};

		Collections.sort(machines, comparator);

		return machines;
	}
	
	public com.sinoparasoft.type.PhysicalMachine getPhysicalMachine(String hostId) {
		PhysicalMachine machine = PhysicalMachine.findPhysicalMachine(hostId);
		com.sinoparasoft.type.PhysicalMachine machineValue = buildPhysicalMachineValue(machine);
		return machineValue;
	}

	/**
	 * 
	 * @param hostId
	 * @param alarmParameters
	 * @author xiangqian
	 */
	public void setAlarmSettings(String hostId, List<AlarmParameter> alarmParameters) {
		for (AlarmParameter parameter : alarmParameters) {
			MonitorSetting monitorSetting = MonitorSetting.getMonitorSetting(hostId,
					MonitorNameEnum.valueOf(parameter.getAlarmName()));

			if (parameter.getEnabled() == true) {
				if (monitorSetting == null) {
					// create new alarm setting if not previously set
					createAlarm(hostId, parameter);
				} else if ((monitorSetting.getEnabled() == true)
						&& (parameter.getAlarmThreshold() == monitorSetting.getThreshold())
						&& (parameter.getSeverityLevel() == monitorSetting.getSeverityLevel())) {
					// skip
					continue;
				} else {
					// adjust existing alarm setting, change threshold if previously enabled, or enable if previously
					// disabled
					monitorSetting.setEnabled(parameter.getEnabled());
					monitorSetting.setThreshold(parameter.getAlarmThreshold());
					monitorSetting.setSeverityLevel(parameter.getSeverityLevel());
					monitorSetting.merge();
					monitorSetting.flush();
					monitorSetting.clear();
				}
			} else {
				if ((monitorSetting == null) || (monitorSetting.getEnabled() == false)) {
					// skip
					continue;
				} else {
					// adjust existing alarm setting, disable if previously enabled
					monitorSetting.setEnabled(parameter.getEnabled());
					monitorSetting.merge();
					monitorSetting.flush();
					monitorSetting.clear();
				}
			}
		}

		return;
	}

	/**
	 * 
	 * @param hostId
	 * @param alarmParameter
	 * @author xiangqian
	 */
	private void createAlarm(String hostId, AlarmParameter alarmParameter) {
		MonitorNameEnum monitorName = MonitorNameEnum.valueOf(alarmParameter.getAlarmName());
	
		MonitorSetting monitorSetting = new MonitorSetting();
		monitorSetting.setMonitorSource(MonitorSourceEnum.PHYSICAL_MACHINE);
		monitorSetting.setSourceId(hostId);
		monitorSetting.setMonitorType(MonitorTypeEnum.LOAD);
		monitorSetting.setMonitorName(monitorName);
		monitorSetting.setThreshold(alarmParameter.getAlarmThreshold());
		monitorSetting.setThresholdUnit(alarmParameter.getThresholdUnit());
		monitorSetting.setSeverityLevel(alarmParameter.getSeverityLevel());
		monitorSetting.setEnabled(true);
		monitorSetting.setOsAlarmId(null);
		monitorSetting.setCreateTime(new Date());
		monitorSetting.persist();
		monitorSetting.flush();
		monitorSetting.clear();
	
		return;
	}

	public List<com.sinoparasoft.type.VirtualMachine> getVirtualMachines(String hostId) {
		List<com.sinoparasoft.type.VirtualMachine> instances = new ArrayList<com.sinoparasoft.type.VirtualMachine>();

		PhysicalMachine machine = PhysicalMachine.findPhysicalMachine(hostId);

		List<VirtualMachine> virtualMachines = VirtualMachine.getVirtualMachinesByHypervisorName(machine.getHostName());
		for (VirtualMachine virtualMachine : virtualMachines) {
			com.sinoparasoft.type.VirtualMachine machineValue = virtualMachineService.getVirtualMachineById(virtualMachine
					.getHostId());
			if (null != machineValue) {
				instances.add(machineValue);
			}
		}

		return instances;
	}
}
