package com.sinosoft.web;

import java.util.ArrayList;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sinosoft.common.ActionResult;
import com.sinosoft.common.BatchActionResult;
import com.sinosoft.model.Disk;
import com.sinosoft.service.DiskService;
import com.sinosoft.service.OperationLogService;
import com.sinosoft.service.VirtualMachineService;
import com.sinosoft.util.security.Principal;

@RequestMapping("/disk")
@Controller
// @RooWebScaffold(path = "disk", formBackingObject = Disk.class)
public class DiskController {
	@Autowired
	DiskService diskService;

	@Autowired
	VirtualMachineService virtualMachineService;

	@Autowired
	OperationLogService operationLogService;

	@Resource
	Principal principal;

	private static Logger logger = LoggerFactory.getLogger(DiskController.class);

	@RequestMapping(value = "list", method = RequestMethod.GET)
	public String getList(ModelMap map) {
		return "disk_list_tile";
	}

	@RequestMapping(value = "json-list", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> getJsonList(@RequestParam("pageNo") int pageNo) {
		Map<String, Object> map = new HashMap<String, Object>();

		String username = principal.getUsername();
		String role = principal.getRoleName();

		int totalDiskCount = 0;

		if (pageNo < 1) {
			pageNo = 1;
		}

		int pageTotal = 0;
		int pageSize = 10;

		try {
			List<Disk> list = null;
			if (("ROLE_MANAGER").equals(role)) {
				int recordCount = (int) Disk.countActiveDisks();
				totalDiskCount = recordCount;
				recordCount -= 1;
				if (recordCount < 0) {
					recordCount = 0;
				}
				pageTotal = recordCount / pageSize + 1;
				if (pageNo > pageTotal) {
					pageNo = pageTotal;
				}

				list = Disk.getDiskEntries((pageNo - 1) * pageSize, pageSize);
			} else {
				int recordCount = (int) Disk.countActiveDisksByManager(username);
				totalDiskCount = recordCount;
				recordCount -= 1;
				if (recordCount < 0) {
					recordCount = 0;
				}
				pageTotal = recordCount / pageSize + 1;
				if (pageNo > pageTotal) {
					pageNo = pageTotal;
				}

				list = Disk.getDiskEntriesByManager((pageNo - 1) * pageSize, pageSize, username);
			}

			List<String> diskIds = new ArrayList<String>();
			for (Disk disk : list) {
				diskIds.add(disk.getDiskId());
			}
			List<com.sinosoft.type.Disk> diskList = diskService.getList(diskIds);

			map.put("status", "ok");
			map.put("message", "");
			map.put("disks", diskList);
			map.put("total_disk_count", totalDiskCount);
			map.put("pageTotal", pageTotal);
			map.put("pageNo", pageNo);
		} catch (Exception e) {
			String message = "获取云硬盘列表发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	@RequestMapping(method = RequestMethod.POST, value = "", produces = "application/json")
	public @ResponseBody Map<String, Object> createDisk(String diskName, String description, Integer capacity,
			String manager, String aliveDays) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			int intAliveDays = Integer.parseInt(aliveDays);
			ActionResult actionResult = diskService.createDisk(principal.getUsername(), diskName, description,
					capacity, manager, intAliveDays);
			if (true == actionResult.isSuccess()) {
				map.put("status", "ok");
				map.put("message", "创建云硬盘请求发送成功");
			} else {
				map.put("status", "error");
				map.put("message", actionResult.getMessage());
			}
		} catch (Exception e) {
			String message = "创建云硬盘请求发送失败";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	// cannot pass in parameter if use DELETE verb !!!
	@RequestMapping(value = "", method = RequestMethod.PUT)
	@ResponseBody
	public Map<String, Object> deleteDisks(String diskIds) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			List<String> diskIdList = new ArrayList<String>();
			String[] ids = diskIds.split(",");
			for (int i = 0; i < ids.length; i++) {
				diskIdList.add(ids[i]);
			}

			BatchActionResult actionResult = diskService.deleteDisks(principal.getUsername(), diskIdList);
			if (true == actionResult.isSuccess()) {
				map.put("status", "ok");
				map.put("message", "删除云硬盘请求发送成功");
			} else {
				map.put("status", "error");
				map.put("message", actionResult.getMessage());
			}
		} catch (Exception e) {
			String message = "发送删除云硬盘请求发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	@RequestMapping(value = "{id}/detail", method = RequestMethod.GET)
	public String getDetail(@PathVariable("id") String id, ModelMap map) {
		return "disk_detail_tile";
	}

	@RequestMapping(value = "{id}/json-detail", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> getJsonDetail(@PathVariable("id") String id) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			com.sinosoft.type.Disk diskValue = diskService.getDiskById(id);
			if (null == diskValue) {
				map.put("status", "error");
				map.put("message", "指定的云硬盘不存在");
			} else {
				map.put("status", "ok");
				map.put("message", "");
				map.put("disk", diskValue);
			}
		} catch (Exception e) {
			String message = "获取云硬盘详细信息发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	@RequestMapping(value = "{id}", method = RequestMethod.PUT)
	@ResponseBody
	public Map<String, Object> modifyDisk(@PathVariable("id") String id, String diskName, String description) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			ActionResult actionResult = diskService.modifyDisk(principal.getUsername(), id, diskName, description);
			if (true == actionResult.isSuccess()) {
				map.put("status", "ok");
				map.put("message", "修改云硬盘信息成功");
			} else {
				map.put("status", "error");
				map.put("message", actionResult.getMessage());
			}
		} catch (Exception e) {
			String message = "修改云硬盘信息发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	@RequestMapping(value = "{id}/manager", method = RequestMethod.PUT)
	@ResponseBody
	public Map<String, Object> setManager(@PathVariable("id") String diskId, String manager) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			ActionResult actionResult = diskService.setManager(principal.getUsername(), diskId, manager);
			if (true == actionResult.isSuccess()) {
				map.put("status", "ok");
				map.put("message", "设置云硬盘管理员成功");
			} else {
				map.put("status", "error");
				map.put("message", actionResult.getMessage());
			}
		} catch (Exception e) {
			String message = "设置云硬盘管理员发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	@RequestMapping(method = RequestMethod.POST, value = "{id}/valid-time", produces = "application/json")
	public @ResponseBody Object setValidTime(@PathVariable("id") String diskId, String aliveDays) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			int intAliveDays = Integer.parseInt(aliveDays);
			ActionResult actionResult = diskService.setValidTime(principal.getUsername(), diskId, intAliveDays);
			if (true == actionResult.isSuccess()) {
				map.put("status", "ok");
				map.put("message", "设置云硬盘有效期成功");
			} else {
				map.put("status", "error");
				map.put("message", actionResult.getMessage());
			}
		} catch (NumberFormatException e) {
			String message = "设置云硬盘有效期发生错误：输入的日期数据格式错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		} catch (Exception e) {
			String message = "设置云硬盘有效期发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	@RequestMapping(method = RequestMethod.PUT, value = "{id}/attach", produces = "application/json")
	public @ResponseBody Map<String, Object> attachDisk(@PathVariable("id") String diskId, String vmId) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			ActionResult result = virtualMachineService.attachDisk(principal.getUsername(), vmId, diskId);
			if (true == result.isSuccess()) {
				map.put("status", "ok");
				map.put("message", "");
			} else {
				map.put("status", "error");
				map.put("message", result.getMessage());
			}
		} catch (Exception e) {
			String message = "挂载云硬盘发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	@RequestMapping(method = RequestMethod.PUT, value = "{id}/detach", produces = "application/json")
	public @ResponseBody Map<String, Object> detachDisk(@PathVariable("id") String diskId) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			Disk disk = Disk.getDisk(diskId);
			String vmId = disk.getVirtualMachine().getHostId();

			ActionResult result = virtualMachineService.detachDisk(principal.getUsername(), vmId, diskId);
			if (true == result.isSuccess()) {
				map.put("status", "ok");
				map.put("message", "");
			} else {
				map.put("status", "error");
				map.put("message", result.getMessage());
			}

		} catch (Exception e) {
			String message = "卸载云硬盘发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	@RequestMapping(method = RequestMethod.GET, value = "user-available-disks", produces = "application/json")
	public @ResponseBody Map<String, Object> getUserAvailableDisks() {
		Map<String, Object> map = new HashMap<String, Object>();
		String username = principal.getUsername();
		try {
			List<com.sinosoft.type.Disk> diskList = diskService.getUserAvailableDisks(username);

			map.put("status", "ok");
			map.put("message", "");
			map.put("available_disks", diskList);
		} catch (Exception e) {
			String message = "获取用户可用的云硬盘列表发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	@RequestMapping(value = "expiring-disk-list", method = RequestMethod.GET)
	public @ResponseBody Map<String, Object> getExpiringDisks() {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			List<Disk> expiringDisks = Disk.getExpringDisks();

			List<String> diskIds = new ArrayList<String>();
			for (Disk disk : expiringDisks) {
				diskIds.add(disk.getDiskId());
			}
			List<com.sinosoft.type.Disk> diskList = diskService.getList(diskIds);

			map.put("status", "ok");
			map.put("message", "");
			map.put("disks", diskList);
		} catch (Exception e) {
			String message = "获取云硬盘列表发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}
}
