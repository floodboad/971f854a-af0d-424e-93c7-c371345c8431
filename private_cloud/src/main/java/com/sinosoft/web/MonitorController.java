package com.sinosoft.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sinosoft.model.Monitor;

@RequestMapping("/monitor")
@Controller
public class MonitorController {
	private static Logger logger = LoggerFactory.getLogger(MonitorController.class);
	
	@RequestMapping(value = "json-list", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> getJsonList() {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			List<Monitor> monitors = Monitor.findAllMonitors();

			map.put("status", "ok");
			map.put("message", "");
			map.put("monitors", monitors);
		} catch (Exception e) {
			String message = "获取监控项列表发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

}
