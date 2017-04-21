package com.sinosoft.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sinosoft.common.ActionResult;
import com.sinosoft.common.AlarmParameter;
import com.sinosoft.common.BatchActionResult;
import com.sinosoft.enumerator.MonitorTypeEnum;
import com.sinosoft.model.VirtualMachine;
import com.sinosoft.openstack.type.CloudConfig;
import com.sinosoft.openstack.type.telemetry.ServerSamples;
import com.sinosoft.service.DiskService;
import com.sinosoft.service.MonitorSettingService;
import com.sinosoft.service.OperationLogService;
import com.sinosoft.service.SnapshotService;
import com.sinosoft.service.VirtualMachineService;
import com.sinosoft.type.Disk;
import com.sinosoft.util.security.Principal;

@Component("conf")
@RequestMapping("/virtual-machine")
@Controller
public class VirtualMachineController {
	@Autowired
	VirtualMachineService virtualMachineService;

	@Autowired
	OperationLogService operationLogService;

	@Autowired
	MonitorSettingService monitorSettingService;

	@Resource
	Principal principal;

	@Autowired
	DiskService diskService;

	@Autowired
	SnapshotService snapshotService;

	@Autowired
	CloudConfig cloudConfig;

	private static Logger logger = LoggerFactory.getLogger(VirtualMachineController.class);

	@RequestMapping(value = "list")
	public String getList(ModelMap map) {
		return "virtual_machine_list_tile";
	}

	@RequestMapping(value = "json-list", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> getJsonList(@RequestParam("pageNo") int pageNo) {
		Map<String, Object> map = new HashMap<String, Object>();

		int totalMachineCount = 0;

		String username = principal.getUsername();
		String role = principal.getRoleName();

		int pageSize = 10;
		int pageTotal = 0;
		if (pageNo < 1) {
			pageNo = 1;
		}

		try {
			List<VirtualMachine> machines = null;
			if (("ROLE_MANAGER").equals(role)) {
				int recordCount = (int) VirtualMachine.countVirtualMachines();
				totalMachineCount = recordCount;
				recordCount -= 1;
				if (recordCount < 0) {
					recordCount = 0;
				}
				pageTotal = recordCount / pageSize + 1;
				if (pageNo > pageTotal) {
					pageNo = pageTotal;
				}

				machines = VirtualMachine.getVirtualMachineEntries((pageNo - 1) * pageSize, pageSize);
			} else {
				int recordCount = (int) VirtualMachine.countVirtualMachinesByManager(username);
				totalMachineCount = recordCount;
				recordCount -= 1;
				if (recordCount < 0) {
					recordCount = 0;
				}
				pageTotal = recordCount / pageSize + 1;

				machines = VirtualMachine.getVirtualMachineEntriesByManager((pageNo - 1) * pageSize, pageSize,
						username);
			}

			List<com.sinosoft.type.VirtualMachine> machineList = new ArrayList<com.sinosoft.type.VirtualMachine>();
			for (VirtualMachine machine : machines) {
				com.sinosoft.type.VirtualMachine machineValue = virtualMachineService
						.getVirtualMachineById(machine.getHostId());

				machineList.add(machineValue);
			}

			map.put("status", "ok");
			map.put("message", "");
			map.put("machines", machineList);
			map.put("total_machine_count", totalMachineCount);
			map.put("pageTotal", pageTotal);
			map.put("pageNo", pageNo);
		} catch (Exception e) {
			String message = "获取虚拟机列表发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	@RequestMapping(value = "form", method = RequestMethod.GET)
	public String openCreateVirtualMachineForm(ModelMap map) {
		return "virtual_machine_form_tile";
	}

	@RequestMapping(method = RequestMethod.POST, value = "", produces = "application/json")
	public @ResponseBody Map<String, Object> createVirtualMachine(String hostName, String description, int cpu,
			int memory, int disk, String imageId, String domainId, String groupId, String applicationIds,
			String manager) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			String creator = principal.getUsername();

			ActionResult result = virtualMachineService.createVirtualMachine(creator, hostName, description, cpu,
					memory, disk, imageId, domainId, groupId, applicationIds, manager);
			if (true == result.isSuccess()) {
				map.put("status", "ok");
				map.put("message", "");
			} else {
				map.put("status", "error");
				map.put("message", result.getMessage());
			}
		} catch (Exception e) {
			String message = "创建虚拟机请求发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	@RequestMapping(value = "start-virtual-machines", method = RequestMethod.PUT)
	@ResponseBody
	public Map<String, Object> startVirtualMachines(@RequestParam("vmIds") String vmIds) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			BatchActionResult result = virtualMachineService.startVirtualMachines(principal.getUsername(), vmIds);
			if (true == result.isSuccess()) {
				map.put("status", "ok");
				map.put("message", "");
			} else {
				map.put("status", "error");
				map.put("message", result.getMessage());
			}
		} catch (Exception e) {
			String message = "启动虚拟机请求发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	@RequestMapping(value = "reboot-virtual-machines", method = RequestMethod.PUT)
	@ResponseBody
	public Map<String, Object> rebootVirtualMachines(@RequestParam("vmIds") String vmIds) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			BatchActionResult result = virtualMachineService.rebootVirtualMachines(principal.getUsername(), vmIds);
			if (true == result.isSuccess()) {
				map.put("status", "ok");
				map.put("message", "");
			} else {
				map.put("status", "error");
				map.put("message", result.getMessage());
			}
		} catch (Exception e) {
			String message = "重启虚拟机请求发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	@RequestMapping(value = "shutdown-virtual-machines", method = RequestMethod.PUT)
	@ResponseBody
	public Map<String, Object> shutdownVirtualMachines(@RequestParam("vmIds") String vmIds) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			BatchActionResult result = virtualMachineService.shutdownVirtualMachines(principal.getUsername(), vmIds);
			if (true == result.isSuccess()) {
				map.put("status", "ok");
				map.put("message", "");
			} else {
				map.put("status", "error");
				map.put("message", result.getMessage());
			}
		} catch (Exception e) {
			String message = "关闭虚拟机请求发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	@RequestMapping(value = "delete-virtual-machines", method = RequestMethod.PUT)
	@ResponseBody
	public Map<String, Object> deleteVirtualMachines(@RequestParam("vmIds") String vmIds) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			BatchActionResult result = virtualMachineService.deleteVirtualMachines(principal.getUsername(), vmIds);
			if (true == result.isSuccess()) {
				map.put("status", "ok");
				map.put("message", "");
			} else {
				map.put("status", "error");
				map.put("message", result.getMessage());
			}
		} catch (Exception e) {
			String message = "删除虚拟机请求发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	@RequestMapping(value = "{id}/detail", method = RequestMethod.GET)
	public String getDetail(@PathVariable("id") String id, ModelMap map) {
		return "virtual_machine_detail_tile";
	}

	@RequestMapping(value = "{id}/machine-info", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> getMachineInfo(@PathVariable("id") String id) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			com.sinosoft.type.VirtualMachine machineValue = virtualMachineService.getVirtualMachineById(id);
			if (null == machineValue) {
				map.put("status", "error");
				map.put("message", "获取虚拟机基本信息失败，虚拟机不存在，虚拟机ID：" + id);
				return map;
			}

			map.put("status", "ok");
			map.put("message", "");
			map.put("machine", machineValue);
		} catch (Exception e) {
			String message = "获取虚拟机基本信息发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	@RequestMapping(value = "{id}/load-monitor-settings", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> getLoadMonitorSettings(@PathVariable("id") String id) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			List<com.sinosoft.type.MonitorSetting> monitorSettings = monitorSettingService.getMonitorSettings(id,
					MonitorTypeEnum.LOAD);

			map.put("status", "ok");
			map.put("message", "");
			map.put("load_monitor_settings", monitorSettings);
		} catch (Exception e) {
			String message = "获取虚拟机负载监控设置发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	@RequestMapping(value = "{id}/attached-disks", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> getAttachedDisks(@PathVariable("id") String id) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			List<Disk> diskList = diskService.getDisksByVirtualMachine(id);
			if (null == diskList) {
				map.put("status", "error");
				map.put("message", "获取虚拟机挂载的云硬盘失败");
				return map;
			}

			map.put("status", "ok");
			map.put("message", "");
			map.put("disks", diskList);
		} catch (Exception e) {
			String message = "获取虚拟机挂载的云硬盘发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	@RequestMapping(value = "{id}/snapshots", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> getSnapshots(@PathVariable("id") String id) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			List<com.sinosoft.type.Snapshot> snapshotList = snapshotService.getListByVirtualMachine(id);
			if (null == snapshotList) {
				map.put("status", "error");
				map.put("message", "获取从虚拟机生成的快照失败");
				return map;
			}

			map.put("status", "ok");
			map.put("message", "");
			map.put("snapshots", snapshotList);
		} catch (Exception e) {
			String message = "获取从虚拟机生成的快照发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	@RequestMapping(value = "{id}/metric", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> getMetric(@PathVariable("id") String vmId) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			Map<String, ServerSamples> serverSapmles = virtualMachineService.getMetric(vmId);
			if (null != serverSapmles) {
				map.put("status", "ok");
				map.put("message", "");
				map.putAll(serverSapmles);
			} else {
				map.put("status", "error");
				map.put("message", "获取虚拟机负载数据失败");
			}
		} catch (Exception e) {
			String message = "获取虚拟机负载数据发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	@RequestMapping(value = "{id}/vnc", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> getVncConsole(@PathVariable("id") String vmId) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			String vncConsoleUrl = virtualMachineService.getVncConsoleUrl(vmId);
			if (null != vncConsoleUrl) {
				map.put("status", "ok");
				map.put("message", "");
				map.put("vnc", vncConsoleUrl);
			} else {
				map.put("status", "error");
				map.put("message", "获取虚拟机控制台失败");
			}
		} catch (Exception e) {
			String message = "获取虚拟机控制台发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	@RequestMapping(value = "{id}/rename", method = RequestMethod.PUT, produces = "application/json")
	public @ResponseBody Map<String, Object> renameVirtualMachine(@PathVariable("id") String vmId, String newName) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			ActionResult result = virtualMachineService.renameVirtualMachine(principal.getUsername(), vmId, newName);
			if (true == result.isSuccess()) {
				map.put("status", "ok");
				map.put("message", "");
			} else {
				map.put("status", "error");
				map.put("message", result.getMessage());
			}
		} catch (Exception e) {
			String message = "重命名虚拟机发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	@RequestMapping(value = "{id}/manager", method = RequestMethod.PUT)
	@ResponseBody
	public Map<String, Object> setManager(@PathVariable("id") String vmId, String manager) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			ActionResult result = virtualMachineService.setVirtualMachineManager(principal.getUsername(), vmId,
					manager);
			if (true == result.isSuccess()) {
				map.put("status", "ok");
				map.put("message", "");
			} else {
				map.put("status", "error");
				map.put("message", result.getMessage());
			}
		} catch (Exception e) {
			String message = "设置虚拟机管理员发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	@RequestMapping(value = "{id}/application-tag", method = RequestMethod.PUT)
	@ResponseBody
	public Map<String, Object> setApplicationTags(@PathVariable("id") String vmId, String applicationTagIds) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			ActionResult result = virtualMachineService.setApplicationTags(principal.getUsername(), vmId,
					applicationTagIds);
			if (true == result.isSuccess()) {
				map.put("status", "ok");
				map.put("message", "");
			} else {
				map.put("status", "error");
				map.put("message", result.getMessage());
			}
		} catch (Exception e) {
			String message = "设置虚拟机业务系统标签发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	@RequestMapping(value = "{id}/load-monitor-settings", method = RequestMethod.PUT)
	public @ResponseBody Map<String, String> setLoadMonitorSettings(@PathVariable("id") String vmId,
			@RequestBody List<AlarmParameter> alarmParameters) {
		Map<String, String> map = new HashMap<String, String>();

		try {
			BatchActionResult result = virtualMachineService.setLoadMonitorSettings(principal.getUsername(), vmId,
					alarmParameters);
			if (true == result.isSuccess()) {
				map.put("status", "ok");
				map.put("message", "");
			} else {
				map.put("status", "error");
				map.put("message", result.getMessage());
			}
		} catch (Exception e) {
			String message = "设置虚拟机负载监控发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	@RequestMapping(value = "{id}/snapshot", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> createSnapshot(@PathVariable("id") String vmId, String snapshotName,
			String snapshotDescription) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			ActionResult result = virtualMachineService.createSnapshot(principal.getUsername(), vmId, snapshotName,
					snapshotDescription);
			if (true == result.isSuccess()) {
				map.put("status", "ok");
				map.put("message", "");
			} else {
				map.put("status", "error");
				map.put("message", result.getMessage());
			}
		} catch (Exception e) {
			String message = "创建虚拟机快照请求发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	@RequestMapping(value = "{id}/restore-snapshot", method = RequestMethod.PUT)
	@ResponseBody
	public Map<String, Object> restoreSnapshot(@PathVariable("id") String vmId, String snapshotId,
			String virtualMachineName, String virtualMachineDescription) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			ActionResult result = virtualMachineService.restoreSnapshot(principal.getUsername(), vmId, snapshotId,
					virtualMachineName, virtualMachineDescription);
			if (true == result.isSuccess()) {
				map.put("status", "ok");
				map.put("message", "");
			} else {
				map.put("status", "error");
				map.put("message", result.getMessage());
			}
		} catch (Exception e) {
			String message = "恢复虚拟机快照请求发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	@RequestMapping(value = "list-by-manager", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> getListByManager() {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			String manager = principal.getUsername();
			List<VirtualMachine> machines = VirtualMachine.getVirtualMachinesByManager(manager);
			List<com.sinosoft.type.VirtualMachine> machineList = new ArrayList<com.sinosoft.type.VirtualMachine>();
			for (VirtualMachine machine : machines) {
				com.sinosoft.type.VirtualMachine machineValue = virtualMachineService
						.getVirtualMachineById(machine.getHostId());
				if (null != machineValue) {
					machineList.add(machineValue);
				}
			}

			map.put("status", "ok");
			map.put("message", "");
			map.put("machineList", machineList);
		} catch (Exception e) {
			String message = "获取虚拟机列表发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	@RequestMapping(value = "list-by-group", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> getListByGroup(String groupId) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			List<com.sinosoft.type.VirtualMachine> machineList = new ArrayList<com.sinosoft.type.VirtualMachine>();
			List<VirtualMachine> machines = VirtualMachine.getVirtualMachinesByGroupId(groupId);
			for (VirtualMachine machine : machines) {
				com.sinosoft.type.VirtualMachine machineValue = virtualMachineService
						.getVirtualMachineById(machine.getHostId());
				if (null != machineValue) {
					machineList.add(machineValue);
				}
			}

			map.put("status", "ok");
			map.put("message", "");
			map.put("machines", machineList);
		} catch (Exception e) {
			String message = "获取虚拟机列表发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}
}
