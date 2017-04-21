package com.sinosoft.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sinosoft.service.OverviewService;
import com.sinosoft.service.VirtualMachineDomainService;
import com.sinosoft.type.DomainOverallResourceUsage;
import com.sinosoft.type.OverviewResourceUsage;
import com.sinosoft.type.StorageResourceUsage;

@RequestMapping("/overview")
@Controller
public class OverviewController {
	private static Logger logger = LoggerFactory.getLogger(OverviewController.class);

	@Autowired
	OverviewService overviewService;

	@Autowired
	VirtualMachineDomainService virtualMachineDomainService;

	// @RequestMapping(method = RequestMethod.POST, value = "{id}")
	// public void post(@PathVariable Long id, ModelMap modelMap, HttpServletRequest request, HttpServletResponse
	// response) {
	// }

	@RequestMapping(value = "", method = RequestMethod.GET)
	public String index() {
		return "overview_tile";
	}

	@RequestMapping(value = "resource-usage", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> getResourceUsage() {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			OverviewResourceUsage resourceUsage = overviewService.getResourceUsage();
			map.put("status", "ok");
			map.put("message", "");
			map.put("resource_usage", resourceUsage);
		} catch (Exception e) {
			String message = "获取资源使用量发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	@RequestMapping(value = "domain-overall-resource-usage", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> getDomainOverallResourceUsage() {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			List<DomainOverallResourceUsage> resourceUsage = virtualMachineDomainService.getOverallReourceUsage();
			map.put("status", "ok");
			map.put("message", "");
			map.put("resource_usage", resourceUsage);
		} catch (Exception e) {
			String message = "获取虚拟机域资源使用量发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	/**
	 * storage resource usage, including virtual machines, image and snapshot, block storage
	 * 
	 * @return
	 */
	@RequestMapping(value = "storage-resource-usage", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> getStorageResourceUsage() {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			StorageResourceUsage resourceUsage = overviewService.getStorageResourceUsage();
			map.put("resource_usage", resourceUsage);
			map.put("status", "ok");
			map.put("message", "");
		} catch (Exception e) {
			String message = "获取存储资源使用量发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	@RequestMapping(value = "block-storage-quota-range", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> getBlockStorageQuotaRange() {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			OverviewResourceUsage overviewResourceUsage = overviewService.getResourceUsage();
			StorageResourceUsage storageResourceUsage = overviewService.getStorageResourceUsage();

			long minValue = storageResourceUsage.getBlockStorageUsage().getUsed();
			long maxValue = overviewResourceUsage.getStorageUsage().getUnallocated()
					+ storageResourceUsage.getBlockStorageUsage().getQuota();
			if (0 == minValue) {
				minValue = 1;
			}
			map.put("min", minValue);
			map.put("max", maxValue);
			
			map.put("status", "ok");
			map.put("message", "");
		} catch (Exception e) {
			String message = "获取云硬盘配额范围发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	@RequestMapping(value = "block-storage-quota", method = RequestMethod.PUT)
	@ResponseBody
	public Map<String, Object> updateBlockStorageQuotaUsage(@RequestParam int volumes, @RequestParam int gigabytes) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			overviewService.updateBlockStorageQuota(volumes, gigabytes);
			map.put("status", "ok");
			map.put("message", "");
		} catch (Exception e) {
			String message = "更新云硬盘配额发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}
}
