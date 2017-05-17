package com.sinoparasoft.web;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sinoparasoft.common.BatchActionResult;
import com.sinoparasoft.service.OperationLogService;
import com.sinoparasoft.service.SnapshotService;
import com.sinoparasoft.util.security.Principal;

@RequestMapping("/snapshot/**")
@Controller
public class SnapshotController {
	private static Logger logger = LoggerFactory.getLogger(SnapshotController.class);

	@Autowired
	SnapshotService snapshotService;

	@Resource
	Principal principal;

	@Autowired
	OperationLogService operationLogService;

	@RequestMapping(value = "list", method = RequestMethod.GET)
	public String getList(ModelMap map) {
		return "snapshot_list_tile";
	}

	@RequestMapping(value = "json-list", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> getJsonList(@RequestParam("pageNo") int pageNo) {
		Map<String, Object> map = new HashMap<String, Object>();
		int pageSize = 10;

		try {
			Map<String, Object> snapshotMap = snapshotService.getList(principal.getUsername(), principal.getRoleName(),
					pageNo, pageSize);

			map.put("status", "ok");
			map.put("message", "");
			map.putAll(snapshotMap);
		} catch (Exception e) {
			String message = "获取快照列表发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	@RequestMapping(value = "", method = RequestMethod.PUT)
	@ResponseBody
	public Map<String, Object> deleteSnapshots(@RequestParam String snapshotIds) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			List<String> snapshotIdList = new ArrayList<String>();
			String[] ids = snapshotIds.split(",");
			for (int i = 0; i < ids.length; i++) {
				snapshotIdList.add(ids[i]);
			}

			BatchActionResult actionResult = snapshotService.deleteSnapshots(principal.getUsername(), snapshotIdList);
			if (true == actionResult.isSuccess()) {
				map.put("status", "ok");
				map.put("message", "");
			} else {
				map.put("status", "error");
				map.put("message", actionResult.getMessage());
			}
		} catch (Exception e) {
			String message = "删除快照请求发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}
}
