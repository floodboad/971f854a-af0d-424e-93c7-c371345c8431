package com.sinosoft.web;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.openstack4j.model.compute.ext.Hypervisor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sinosoft.common.AlarmParameter;
import com.sinosoft.common.BatchActionResult;
import com.sinosoft.enumerator.MonitorNameEnum;
import com.sinosoft.enumerator.MonitorTypeEnum;
import com.sinosoft.enumerator.OperationSeverityEnum;
import com.sinosoft.enumerator.OperationStatusEnum;
import com.sinosoft.enumerator.PhysicalMachineTypeNameEnum;
import com.sinosoft.enumerator.ServiceNameEnum;
import com.sinosoft.model.PhysicalMachine;
import com.sinosoft.model.PhysicalMachineLoad;
import com.sinosoft.openstack.CloudManipulator;
import com.sinosoft.openstack.CloudManipulatorFactory;
import com.sinosoft.openstack.type.CloudConfig;
import com.sinosoft.service.MonitorResultService;
import com.sinosoft.service.MonitorSettingService;
import com.sinosoft.service.OperationLogService;
import com.sinosoft.service.PhysicalMachineService;
import com.sinosoft.service.VirtualMachineService;
import com.sinosoft.type.MigrationPolicy;
import com.sinosoft.util.security.Principal;

@RequestMapping("/physical-machine")
@Controller
public class PhysicalMachineController {
	private static Logger logger = LoggerFactory.getLogger(PhysicalMachineController.class);

	@Autowired
	PhysicalMachineService physicalMachineService;

	@Autowired
	VirtualMachineService virtualMachineService;

	@Autowired
	OperationLogService operationLogService;

	@Autowired
	MonitorSettingService monitorSettingService;

	@Resource
	Principal principal;

	@Autowired
	MonitorResultService monitorResultService;

	@Autowired
	CloudConfig cloudConfig;

	@RequestMapping(value = "list", method = RequestMethod.GET)
	public String getList(Model map) {
		return "physical_machine_list_tile";
	}

	@RequestMapping(value = "json-list", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> getJsonList() {
		Map<String, Object> map = new HashMap<String, Object>();
		
		try {
			List<com.sinosoft.type.PhysicalMachine> controllerNodes = physicalMachineService
					.getList(PhysicalMachineTypeNameEnum.CONTROLLER_NODE);
			List<com.sinosoft.type.PhysicalMachine> networkNodes = physicalMachineService
					.getList(PhysicalMachineTypeNameEnum.NETWORK_NODE);
			List<com.sinosoft.type.PhysicalMachine> computeNodes = physicalMachineService
					.getList(PhysicalMachineTypeNameEnum.COMPUTE_NODE);

			map.put("status", "ok");
			map.put("message", "");
			map.put("controller_nodes", controllerNodes);
			map.put("network_nodes", networkNodes);
			map.put("compute_nodes", computeNodes);
		} catch (Exception e) {
			String message = "获取物理服务器列表发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}
	
	@RequestMapping(value = "{id}/detail", method = RequestMethod.GET)
	public String getDetail(@PathVariable("id") String hostId, ModelMap map) {
		return "physical_machine_detail_tile";
	}

	/**
	 * 
	 * @param hostId
	 * @return
	 * @author xiangqian
	 */
	@RequestMapping(value = "{id}/json-detail", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> getJsonDetail(@PathVariable("id") String hostId) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			com.sinosoft.type.PhysicalMachine machineValue = physicalMachineService.getPhysicalMachine(hostId);
			map.put("machine", machineValue);

			List<com.sinosoft.type.VirtualMachine> instances = physicalMachineService.getVirtualMachines(hostId);
			map.put("instances", instances);

			List<Map<String, Object>> loadMonitorRecords = monitorResultService.getMonitorResults(hostId,
					MonitorTypeEnum.LOAD);
			map.put("loadMonitorRecords", loadMonitorRecords);

			List<Map<String, Object>> serviceMonitorRecords = monitorResultService.getMonitorResults(hostId,
					MonitorTypeEnum.SERVICE);
			map.put("serviceMonitorRecords", serviceMonitorRecords);

			map.put("status", "ok");
			map.put("message", "");
			return map;
		} catch (Exception e) {
			String message = "获取物理服务器信息发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
			return map;
		}
	}

	@RequestMapping(value = "{id}/metric", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> getMetric(@PathVariable("id") String hostId) {
		Map<String, Object> map = new HashMap<String, Object>();
		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		List<String> timeSeries = new ArrayList<String>();
		List<Float> freeMemory = new ArrayList<Float>();
		List<Float> cpuLoad = new ArrayList<Float>();
		List<Float> freeDisk = new ArrayList<Float>();
		List<Float> networkUsage = new ArrayList<Float>();

		try {
			PhysicalMachine machine = PhysicalMachine.findPhysicalMachine(hostId);
			List<PhysicalMachineLoad> loads = PhysicalMachineLoad.getPhysicalMachineLoadEntries(machine.getHostName(),
					0, 60, "reportTime", "DESC");
			for (PhysicalMachineLoad load : loads) {
				timeSeries.add(dateFormatter.format(load.getReportTime()));
				
				if (null != load.getCpuLoad()) {
					cpuLoad.add(load.getCpuLoad());
				} else {
					cpuLoad.add(-1.0f);					
				}

				if (null != load.getFreeMemory()) {
					freeMemory.add(load.getFreeMemory());
				} else {
					freeMemory.add(-1.0f);					
				}

				if (null != load.getFreeDisk()) {
					freeDisk.add(load.getFreeDisk());
				} else {
					freeDisk.add(-1.0f);					
				}
				
				if ((null != load.getBytesIn()) && (null != load.getBytesOut())) {
					networkUsage.add(load.getBytesIn() + load.getBytesOut());					
				} else {
					networkUsage.add(-1.0f);					
				}				
			}

			Collections.reverse(timeSeries);
			Collections.reverse(cpuLoad);
			Collections.reverse(freeMemory);
			Collections.reverse(freeDisk);
			Collections.reverse(networkUsage);

			map.put("status", "ok");
			map.put("message", "");
			map.put("time_series", timeSeries);
			map.put("cpu_load", cpuLoad);
			map.put("free_memory", freeMemory);
			map.put("free_disk", freeDisk);
			map.put("network_usage", networkUsage);
		} catch (Exception e) {
			String message = "获取物理服务器负载监控数据发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	@RequestMapping(value = "{id}/load-monitor-settings", method = RequestMethod.GET)
	public @ResponseBody Map<String, Object> getLoadMonitorSettings(@PathVariable("id") String hostId) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			List<com.sinosoft.type.MonitorSetting> monitorSettings = monitorSettingService.getMonitorSettings(hostId,
					MonitorTypeEnum.LOAD);
			map.put("status", "ok");
			map.put("message", "");
			map.put("monitor_settings", monitorSettings);
		} catch (Exception e) {
			String message = "获取物理服务器负载监控设置发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	@RequestMapping(value = "{id}/load-monitor-settings", method = RequestMethod.PUT)
	public @ResponseBody Map<String, String> setLoadMonitorSettings(@PathVariable("id") String hosdId,
			@RequestBody List<AlarmParameter> alarmParameters) {
		Map<String, String> map = new HashMap<String, String>();
				
		try {
			physicalMachineService.setAlarmSettings(hosdId, alarmParameters);

			String operationResult = "设置物理服务器负载监控成功，物理机ID：" + hosdId + "，";
			for (AlarmParameter parameter : alarmParameters) {
				MonitorNameEnum alarmName = MonitorNameEnum.valueOf(parameter.getAlarmName());
				switch (alarmName) {
				case PM_CPU_UTIL:
					if (parameter.getEnabled() == true) {
						operationResult += "处理器利用率阈值：" + parameter.getAlarmThreshold() + "，告警级别："
								+ parameter.getSeverityLevel().name() + "；";
					} else {
						operationResult += "不监控处理器利用率；";
					}
					break;
				// case PM_DISK_READ_BYTES_RATE:
				// if (parameter.getEnabled() == true) {
				// operationResult += "磁盘读取速率阈值：" + parameter.getAlarmThreshold() + "，告警级别："
				// + parameter.getSeverityLevel().name() + "；";
				// } else {
				// operationResult += "不监控磁盘读取速率；";
				// }
				// break;
				// case PM_DISK_WRITE_BYTES_RATE:
				// if (parameter.getEnabled() == true) {
				// operationResult += "磁盘写入速率阈值：" + parameter.getAlarmThreshold() + "，告警级别："
				// + parameter.getSeverityLevel().name() + "；";
				// } else {
				// operationResult += "不监控磁盘写入速率；";
				// }
				// break;
				case PM_MEMORY_USAGE:
					if (parameter.getEnabled() == true) {
						operationResult += "内存利用率阈值：" + parameter.getAlarmThreshold() + "，告警级别："
								+ parameter.getSeverityLevel().name() + "；";
					} else {
						operationResult += "不监控内存利用率；";
					}
					break;
				case PM_NETWORK_INCOMING_BYTES_RATE:
					if (parameter.getEnabled() == true) {
						operationResult += "网络流入速率阈值：" + parameter.getAlarmThreshold() + "，告警级别："
								+ parameter.getSeverityLevel().name() + "；";
					} else {
						operationResult += "不监控网络流入速率；";
					}
					break;
				case PM_NETWORK_OUTGOING_BYTES_RATE:
					if (parameter.getEnabled() == true) {
						operationResult += "网络流出速率阈值：" + parameter.getAlarmThreshold() + "，告警级别："
								+ parameter.getSeverityLevel().name() + "；";
					} else {
						operationResult += "不监控网络流出速率；";
					}
					break;
				default:
					break;
				}
			}
			
			// save operation log
			String operator = principal.getUsername();
			Date operationTime = new Date();
			String operation = "设置物理服务器负载监控";
			OperationStatusEnum operationStatus = OperationStatusEnum.OK;
			ServiceNameEnum serviceName = ServiceNameEnum.PHYSICAL_MACHINE_MANAGEMENT;
			String objectId = hosdId;
			operationResult = operationResult.substring(0, operationResult.lastIndexOf("；"));
			OperationSeverityEnum operationSeverity = OperationSeverityEnum.MIDDLE;
			operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId, operationResult,
					operationSeverity);			

			map.put("status", "ok");
			map.put("message", "");
		} catch (Exception e) {
			String message = "设置物理服务器负载监控发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	@RequestMapping(value = "{id}/migration-policies", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> getMigrationPolicies(@PathVariable("id") String hostId) {
		Map<String, Object> map = new HashMap<String, Object>();
		List<MigrationPolicy> policies = new ArrayList<MigrationPolicy>();

		try {
			PhysicalMachine physicalMachine = PhysicalMachine.findPhysicalMachine(hostId);
			if (null == physicalMachine) {
				map.put("status", "error");
				map.put("message", "获取迁移策略失败，计算节点不存在");

				return map;
			}

			/*
			 * get virtual machines running on this hypervisor, and sort them by cpu in desc order.
			 */
			List<com.sinosoft.type.VirtualMachine> machines = virtualMachineService
					.getVirtualMachinesByHypervisorName(physicalMachine.getHostName());
			Collections.sort(machines, new Comparator<com.sinosoft.type.VirtualMachine>() {
				@Override
				public int compare(com.sinosoft.type.VirtualMachine o1, com.sinosoft.type.VirtualMachine o2) {
					return o2.getCpu() - o1.getCpu();
				}
			});

			/*
			 * get hypervisors excluding this one, and sort them by cpu in desc order.
			 */
			List<com.sinosoft.type.PhysicalMachine> computeNodes = physicalMachineService
					.getList(PhysicalMachineTypeNameEnum.COMPUTE_NODE);
			CloudManipulator cloud = CloudManipulatorFactory.createCloudManipulator(cloudConfig,
					cloudConfig.getAdminProjectId());
			List<? extends Hypervisor> hypervisors = cloud.getHypervisors();
			List<Map<String, Object>> candidateNodes = new ArrayList<Map<String, Object>>();
			for (com.sinosoft.type.PhysicalMachine machineValue : computeNodes) {
				if (true == machineValue.getId().equalsIgnoreCase(hostId)) {
					continue;
				}

				Map<String, Object> computeNodeMap = new HashMap<String, Object>();
				computeNodeMap.put("name", machineValue.getName());
				computeNodeMap.put("cpu", 0);
				String hypervisorId = machineValue.getHypervisorId();
				for (Hypervisor hypervisor : hypervisors) {
					if (true == hypervisor.getId().equalsIgnoreCase(hypervisorId)) {
						int freeCpus = hypervisor.getVirtualCPU() - hypervisor.getVirtualUsedCPU();
						computeNodeMap.put("cpu", freeCpus);

						break;
					}
				}
				candidateNodes.add(computeNodeMap);
			}
			if (candidateNodes.size() == 0) {
				map.put("status", "error");
				map.put("message", "获取迁移策略失败，没有可用的计算节点");

				return map;
			}
			candidateNodes = sortItemsByCpu(candidateNodes);

			/*
			 * calculate migration policies
			 */
			for (com.sinosoft.type.VirtualMachine machine : machines) {
				Map<String, Object> computeNode = candidateNodes.get(0);
				int instanceCpu = machine.getCpu(), computeNodeFreeCpu = (Integer) computeNode.get("cpu");
				if (instanceCpu > computeNodeFreeCpu) {
					map.put("status", "error");
					map.put("message", "获取迁移策略失败，计算节点没有足够的空闲处理器资源");

					return map;
				}

				MigrationPolicy policy = new MigrationPolicy();
				policy.setVirtualMachine(machine);
				policy.setHypervisorName(computeNode.get("name").toString());

				policies.add(policy);

				// update and sort computeNodes
				computeNode.put("cpu", computeNodeFreeCpu - instanceCpu);
				candidateNodes.set(0, computeNode);
				candidateNodes = sortItemsByCpu(candidateNodes);
			}

			map.put("status", "ok");
			map.put("message", "");
			map.put("policies", policies);
		} catch (Exception e) {
			String message = "获取虚拟机迁移策略发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	@RequestMapping(value = "{id}/batch-migrate", method = RequestMethod.PUT)
	@ResponseBody
	public Map<String, Object> batchMigrate(@PathVariable("id") String id,
			@RequestBody List<MigrationPolicy> migratePolicies) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			BatchActionResult result = virtualMachineService.liveMigrateVirtualMachines(principal.getUsername(),
					migratePolicies);
			if (true == result.isSuccess()) {
				map.put("status", "ok");
				map.put("message", "");
			} else {
				map.put("status", "error");
				map.put("message", result.getMessage());
			}
		} catch (Exception e) {
			String message = "在线迁移虚拟机请求发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	private List<Map<String, Object>> sortItemsByCpu(List<Map<String, Object>> items) {
		Comparator<Map<String, Object>> comparator = new Comparator<Map<String, Object>>() {
			@Override
			public int compare(Map<String, Object> o1, Map<String, Object> o2) {
				// return (Integer)o1.get("cpu") - (Integer)o2.get("cpu");
				return (Integer) o2.get("cpu") - (Integer) o1.get("cpu"); // reverse order
			}
		};

		Collections.sort(items, comparator);

		return items;
	}
}