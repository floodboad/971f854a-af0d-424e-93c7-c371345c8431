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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sinoparasoft.common.ActionResult;
import com.sinoparasoft.service.ApplicationTagService;
import com.sinoparasoft.util.security.Principal;

// @RequestMapping("/application/**")
@RequestMapping("/application-tag")
@Controller
public class ApplicationTagController {
	private static Logger logger = LoggerFactory.getLogger(ApplicationTagController.class);

	@Autowired
	ApplicationTagService applicationTagService;

	@Resource
	Principal principal;

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public String getList(ModelMap map) {
		return "application_tag_list_tile";
	}

	@RequestMapping(value = "pagination-json-list", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> getPaginationJsonList(@RequestParam("pageNo") int pageNo,
			@RequestParam("pageSize") int pageSize) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			Map<String, Object> applicationTagMap = applicationTagService.getApplicationTagEntries(pageNo, pageSize);

			map.put("status", "ok");
			map.put("message", "");
			map.putAll(applicationTagMap);
		} catch (Exception e) {
			String message = "获取业务系统标签列表发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	@RequestMapping(value = "check-existence-by-name", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> checkExistenceByName(@RequestParam("name") String name) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			boolean exist = applicationTagService.checkExistenceByName(name);
			if (true == exist) {
				map.put("status", "ok");
				map.put("message", "");
				map.put("existence", true);
			} else {
				map.put("status", "ok");
				map.put("message", "");
				map.put("existence", false);
			}
		} catch (Exception e) {
			String message = "检查业务系统标签是否存在发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	@RequestMapping(value = "", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> createApplicationTag(String name, String description) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			ActionResult result = applicationTagService
					.createApplicationTag(principal.getUsername(), name, description);
			if (true == result.isSuccess()) {
				map.put("status", "ok");
				map.put("message", "");
			} else {
				map.put("status", "error");
				map.put("message", result.getMessage());
			}
		} catch (Exception e) {
			String message = "添加业务系统标签发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.PUT)
	@ResponseBody
	public Map<String, String> modifyApplicationTag(@PathVariable("id") String id,
			@RequestParam("name") String newName, @RequestParam("description") String newDescription) {
		Map<String, String> map = new HashMap<String, String>();

		try {
			ActionResult result = applicationTagService.modifyApplicationTag(principal.getUsername(), id, newName,
					newDescription);
			if (true == result.isSuccess()) {
				map.put("status", "ok");
				map.put("message", "");
			} else {
				map.put("status", "error");
				map.put("message", result.getMessage());
			}
		} catch (Exception e) {
			String message = "修改业务系统标签发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	@ResponseBody
	public Map<String, String> deleteApplicationTag(@PathVariable("id") String id) {
		Map<String, String> map = new HashMap<String, String>();

		try {
			ActionResult result = applicationTagService.deleteApplicationTag(principal.getUsername(), id);
			if (true == result.isSuccess()) {
				map.put("status", "ok");
				map.put("message", "");
			} else {
				map.put("status", "error");
				map.put("message", result.getMessage());
			}
		} catch (Exception e) {
			String message = "删除业务系统标签发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	@RequestMapping(value = "/json-list", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> getJsonList() {
		Map<String, Object> map = new HashMap<String, Object>();
		
		try {
			List<com.sinoparasoft.type.ApplicationTag> applicationTags = applicationTagService.getList();
			map.put("status", "ok");
			map.put("message", "");
			map.put("application_tags", applicationTags);
		} catch (Exception e) {
			String message = "获取业务系统标签列表发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}
		
		return map;
	}
}
