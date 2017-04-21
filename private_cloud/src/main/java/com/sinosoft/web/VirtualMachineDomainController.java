package com.sinosoft.web;

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

import com.sinosoft.common.ActionResult;
import com.sinosoft.model.VirtualMachineDomain;
import com.sinosoft.openstack.type.CloudConfig;
import com.sinosoft.service.OperationLogService;
import com.sinosoft.service.OverviewService;
import com.sinosoft.service.VirtualMachineDomainService;
import com.sinosoft.service.VirtualMachineGroupService;
import com.sinosoft.service.VirtualMachineService;
import com.sinosoft.type.DomainResourceUsage;
import com.sinosoft.type.OverviewResourceUsage;
import com.sinosoft.util.security.Principal;

@RequestMapping("/domain")
@Controller
public class VirtualMachineDomainController {
	private static Logger logger = LoggerFactory.getLogger(VirtualMachineDomainController.class);

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

	@Autowired
	CloudConfig cloudConfig;

	@Autowired
	OverviewService overviewService;

	@RequestMapping(value = "list", method = RequestMethod.GET)
	public String getList(ModelMap map) {
		return "domain_list_tile";
	}

	@RequestMapping(value = "json-list", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> getJsonList() {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			List<VirtualMachineDomain> domainList = VirtualMachineDomain.getDomains();
			map.put("status", "ok");
			map.put("message", "");
			map.put("domains", domainList);
		} catch (Exception e) {
			String message = "获取虚拟机域列表发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	@RequestMapping(method = RequestMethod.POST, value = "", produces = "application/json")
	public @ResponseBody Map<String, Object> createDomain(String domainName, String description, int cpu, int memory,
			int disk) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			ActionResult result = virtualMachineDomainService.createDomain(principal.getUsername(), domainName,
					description, cpu, memory, disk);
			if (true == result.isSuccess()) {
				map.put("status", "ok");
				map.put("message", "");
			} else {
				map.put("status", "error");
				map.put("message", result.getMessage());
			}
		} catch (Exception e) {
			String message = "创建虚拟机域发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	@RequestMapping(value = "{id}/detail", method = RequestMethod.GET)
	public String getDetail(@PathVariable("id") String id, ModelMap map) {
		return "domain_detail_tile";
	}

	@RequestMapping(value = "{id}/domain-info", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> getJsonDetail(@PathVariable("id") String id) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			VirtualMachineDomain domain = VirtualMachineDomain.getDomainById(id);
			if (null == domain) {
				map.put("status", "error");
				map.put("message", "指定的虚拟机域不存在");
				return map;
			}
			map.put("domain", domain);

			map.put("status", "ok");
			map.put("message", "");
		} catch (Exception e) {
			String message = "获取虚拟机域详细信息发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	@RequestMapping(method = RequestMethod.PUT, value = "{id}", produces = "application/json")
	public @ResponseBody Map<String, Object> modifyDomain(@PathVariable("id") String domainId, String domainName,
			String description, int cpu, int memory, int disk) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			ActionResult result = virtualMachineDomainService.modifyDomain(principal.getUsername(), domainId,
					domainName, description, cpu, memory, disk);
			if (true == result.isSuccess()) {
				map.put("status", "ok");
				map.put("message", "");
			} else {
				map.put("status", "error");
				map.put("message", result.getMessage());
			}
		} catch (Exception e) {
			String message = "修改虚拟机域信息发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "{id}", produces = "application/json")
	public @ResponseBody Map<String, Object> deleteDomain(@PathVariable("id") String domainId) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			ActionResult result = virtualMachineDomainService.deleteDomain(principal.getUsername(), domainId);
			if (true == result.isSuccess()) {
				map.put("status", "ok");
				map.put("message", "");
			} else {
				map.put("status", "error");
				map.put("message", result.getMessage());
			}
		} catch (Exception e) {
			String message = "删除虚拟机域发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	@RequestMapping(method = RequestMethod.GET, value = "{id}/reource-usage", produces = "application/json")
	public @ResponseBody Map<String, Object> getResourceUsage(@PathVariable("id") String domainId) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			DomainResourceUsage resourceUsage = virtualMachineDomainService.getResourceUsage(domainId);
			if (null == resourceUsage) {
				map.put("status", "error");
				map.put("message", "获取虚拟机域资源信息失败：无法获取当前虚拟机域的资源信息");
				return map;
			}

			map.put("status", "ok");
			map.put("message", "");
			map.put("resource_usage", resourceUsage);
		} catch (Exception e) {
			String message = "获取虚拟机域资源信息发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	@RequestMapping(value = "new-domain-resource-bound", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody Map<String, Object> getDomainResourceBound() {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			OverviewResourceUsage cloudResourceUsage = overviewService.getResourceUsage();

			long maxCpu = cloudResourceUsage.getCpuUsage().getUnallocated();
			long maxMemory = cloudResourceUsage.getMemoryUsage().getUnallocated();
			long maxDisk = cloudResourceUsage.getStorageUsage().getUnallocated();
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
			String message = "获取虚拟机域资源限额发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	@RequestMapping(value = "adjust-domain-resource-bound", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody Map<String, Object> getDomainResourceBound(String domainId) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			OverviewResourceUsage cloudResourceUsage = overviewService.getResourceUsage();
			DomainResourceUsage domainResourceUsage = virtualMachineDomainService.getResourceUsage(domainId);

			long maxCpu = cloudResourceUsage.getCpuUsage().getUnallocated()
					+ domainResourceUsage.getCpuUsage().getQuota();
			long maxMemory = cloudResourceUsage.getMemoryUsage().getUnallocated()
					+ domainResourceUsage.getMemoryUsage().getQuota();
			long maxDisk = cloudResourceUsage.getStorageUsage().getUnallocated()
					+ domainResourceUsage.getDiskUsage().getQuota();
			map.put("max_cpu", maxCpu);
			map.put("max_memory", maxMemory);
			map.put("max_disk", maxDisk);

			long minCpu = domainResourceUsage.getCpuUsage().getAllocated();
			long minMemory = domainResourceUsage.getMemoryUsage().getAllocated();
			long minDisk = domainResourceUsage.getDiskUsage().getAllocated();
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
			String message = "获取虚拟机域资源限额发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}
}
