package com.sinoparasoft.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sinoparasoft.common.ActionResult;
import com.sinoparasoft.model.VirtualMachineDomain;
import com.sinoparasoft.model.VirtualMachineGroup;
import com.sinoparasoft.service.OperationLogService;
import com.sinoparasoft.service.VirtualMachineDomainService;
import com.sinoparasoft.service.VirtualMachineGroupService;
import com.sinoparasoft.service.VirtualMachineService;
import com.sinoparasoft.type.DomainResourceUsage;
import com.sinoparasoft.type.GroupResourceUsage;
import com.sinoparasoft.util.security.Principal;

// use "/domain/group" instead of "/group" to show menu item background highlight
@RequestMapping("/domain/group")
@Controller
public class VirtualMachineGroupController {
	private static Logger logger = LoggerFactory.getLogger(VirtualMachineGroupController.class);

	@Autowired
	VirtualMachineService virtualMachineService;

	@Autowired
	VirtualMachineDomainService virtualMachineDomainService;

	@Autowired
	VirtualMachineGroupService virtualMachineGroupService;

	@Autowired
	OperationLogService operationLogService;

	@Resource
	Principal principal;

	@RequestMapping(value = "json-list", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> getJsonList(String domainId) {
		Map<String, Object> map = new HashMap<String, Object>();
		if (null == domainId) {
			map.put("status", "error");
			map.put("message", "指定的虚拟机域不存在");

			return map;
		}

		List<VirtualMachineGroup> groupList = VirtualMachineGroup.getGroupsByDomainId(domainId);
		map.put("status", "ok");
		map.put("message", "");
		map.put("groups", groupList);

		return map;
	}

	@RequestMapping(value = "{id}/detail", method = RequestMethod.GET)
	public String getDetail(@PathVariable("id") String groupId, ModelMap map) {
		return "group_detail_tile";
	}

	@RequestMapping(value = "{id}/group-info", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> getGroupInfo(@PathVariable("id") String groupId) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			com.sinoparasoft.type.VirtualMachineGroup groupValue = virtualMachineGroupService.getGroupById(groupId);
			if (null == groupValue) {
				map.put("status", "error");
				map.put("message", "获取虚拟机组基本信息失败，虚拟机组不存在，虚拟机组ID：" + groupId);
				return map;
			}

			map.put("status", "ok");
			map.put("message", "");
			map.put("group", groupValue);
		} catch (Exception e) {
			String message = "获取虚拟机组基本信息发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	@RequestMapping(method = RequestMethod.POST, value = "", produces = "application/json")
	public @ResponseBody Map<String, Object> createGroup(String domainId, String groupName, String description, int cpu,
			int memory, int disk) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			ActionResult result = virtualMachineGroupService.createGroup(principal.getUsername(), domainId, groupName,
					description, cpu, memory, disk);
			if (true == result.isSuccess()) {
				map.put("status", "ok");
				map.put("message", "");
			} else {
				map.put("status", "error");
				map.put("message", result.getMessage());
			}
		} catch (Exception e) {
			String message = "新建虚拟机组发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	@RequestMapping(method = RequestMethod.PUT, value = "{id}", produces = "application/json")
	public @ResponseBody Map<String, Object> modifyGroup(@PathVariable("id") String groupId, String groupName,
			String description, int cpu, int memory, int disk) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			ActionResult result = virtualMachineGroupService.modifyGroup(principal.getUsername(), groupId, groupName,
					description, cpu, memory, disk);
			if (true == result.isSuccess()) {
				map.put("status", "ok");
				map.put("message", "");
			} else {
				map.put("status", "error");
				map.put("message", result.getMessage());
			}
		} catch (Exception e) {
			String message = "修改虚拟机组发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "{id}", produces = "application/json")
	public @ResponseBody Map<String, Object> deleteGroup(@PathVariable("id") String groupId) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			ActionResult result = virtualMachineGroupService.deleteGroup(principal.getUsername(), groupId);
			if (true == result.isSuccess()) {
				map.put("status", "ok");
				map.put("message", "");
			} else {
				map.put("status", "error");
				map.put("message", result.getMessage());
			}
		} catch (Exception e) {
			String message = "删除虚拟机组发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	@RequestMapping(method = RequestMethod.GET, value = "{id}/reource-usage", produces = "application/json")
	public @ResponseBody Map<String, Object> getResourceUsage(@PathVariable("id") String groupId) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			GroupResourceUsage resourceUsage = virtualMachineGroupService.getReourceUsage(groupId);
			if (null == resourceUsage) {
				map.put("status", "error");
				map.put("message", "获取虚拟机组资源信息失败：无法获取当前虚拟机组的资源信息");
				return map;
			}

			map.put("status", "ok");
			map.put("message", "");
			map.put("resource_usage", resourceUsage);
		} catch (Exception e) {
			String message = "获取虚拟机组资源信息发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	@RequestMapping(value = "new-group-resource-bound", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody Map<String, Object> getNewGroupResourceBound(String domainId) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			DomainResourceUsage domainResourceUsage = virtualMachineDomainService.getResourceUsage(domainId);
			if (null == domainResourceUsage) {
				map.put("status", "error");
				map.put("message", "获取虚拟机组资源限额失败：无法获取虚拟机域资源信息");
				return map;
			}

			long maxCpu = domainResourceUsage.getCpuUsage().getUnallocated();
			long maxMemory = domainResourceUsage.getMemoryUsage().getUnallocated();
			long maxDisk = domainResourceUsage.getDiskUsage().getUnallocated();
			map.put("max_cpu", maxCpu);
			map.put("max_memory", maxMemory);
			map.put("max_disk", maxDisk);

			long minCpu = 0, minMemory = 0, minDisk = 0;
			if (maxCpu >= 1) {
				minCpu = 1;
			}
			if (maxMemory >= 1) {
				minMemory = 1;
			}
			if (maxDisk >= 1) {
				minDisk = 1;
			}
			map.put("min_cpu", minCpu);
			map.put("min_memory", minMemory);
			map.put("min_disk", minDisk);

			map.put("status", "ok");
			map.put("message", "");
		} catch (Exception e) {
			String message = "获取虚拟机组资源限额发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	@RequestMapping(value = "adjust-group-resource-bound", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody Map<String, Object> getGroupResourceBound(String groupId) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			VirtualMachineGroup group = VirtualMachineGroup.getGroup(groupId);
			if (null == group) {
				map.put("status", "error");
				map.put("message", "获取虚拟机组资源限额失败：虚拟机组不存在");
				return map;
			}

			VirtualMachineDomain domain = VirtualMachineDomain.getDomainById(group.getDomainId());
			if (null == domain) {
				map.put("status", "error");
				map.put("message", "获取虚拟机组资源限额失败：虚拟机域不存在");
				return map;
			}

			DomainResourceUsage domainResourceUsage = virtualMachineDomainService
					.getResourceUsage(domain.getDomainId());
			if (null == domainResourceUsage) {
				map.put("status", "error");
				map.put("message", "获取虚拟机组资源限额失败：无法获取所属虚拟机域的资源信息");
				return map;
			}

			GroupResourceUsage groupResourceUsage = virtualMachineGroupService.getReourceUsage(groupId);
			if (null == groupResourceUsage) {
				map.put("status", "error");
				map.put("message", "获取虚拟机组资源限额失败：无法获取当前虚拟机组的资源信息");
				return map;
			}

			long maxCpu = domainResourceUsage.getCpuUsage().getUnallocated()
					+ groupResourceUsage.getCpuUsage().getQuota();
			long maxMemory = domainResourceUsage.getMemoryUsage().getUnallocated()
					+ groupResourceUsage.getMemoryUsage().getQuota();
			long maxDisk = domainResourceUsage.getDiskUsage().getUnallocated()
					+ groupResourceUsage.getDiskUsage().getQuota();
			map.put("max_cpu", maxCpu);
			map.put("max_memory", maxMemory);
			map.put("max_disk", maxDisk);

			long minCpu = groupResourceUsage.getCpuUsage().getUsed();
			long minMemory = groupResourceUsage.getMemoryUsage().getUsed();
			long minDisk = groupResourceUsage.getDiskUsage().getUsed();
			if (0 == minCpu) {
				minCpu = 1;
			}
			if (0 == minMemory) {
				minMemory = 1;
			}
			if (0 == minDisk) {
				minDisk = 1;
			}
			map.put("min_cpu", minCpu);
			map.put("min_memory", minMemory);
			map.put("min_disk", minDisk);

			map.put("status", "ok");
			map.put("message", "");
		} catch (Exception e) {
			String message = "获取虚拟机组资源限额发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}
}
