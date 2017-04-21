package com.sinosoft.web;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sinosoft.service.MonitorLogService;
import com.sinosoft.service.OperationLogService;
import com.sinosoft.util.ExcelOperate;
import com.sinosoft.util.security.Principal;

@RequestMapping("/monitor-log")
@Controller
public class MonitorLogController {
	private static Logger logger = LoggerFactory.getLogger(MonitorLogController.class);

	@Autowired
	OperationLogService operationLogService;
		
	@Autowired
	Principal principal;
	
	@Autowired
	ExcelOperate excelOperate;

	@Autowired
	MonitorLogService monitorLogService;

	@RequestMapping(value = "list", method = RequestMethod.GET)
	public String getList(ModelMap map) {
		return "monitor_log_list_tile";
	}

	@RequestMapping(value = "json-list", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> getJsonList(int pageNo, int pageSize, String monitorSource, String monitorType,
			String monitorName, String monitorStatus, String sourceId, String beginningUpdateTime, String endUpdateTime) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			String username = principal.getUsername();
			String role = principal.getRoleName();
			Map<String, Object> monitorLogMap = monitorLogService.getMonitorLogEntries(username, role, pageNo, pageSize,
					monitorSource, monitorType, monitorName, monitorStatus, sourceId, beginningUpdateTime, endUpdateTime);

			map.put("status", "ok");
			map.put("message", "");
			map.putAll(monitorLogMap);
		} catch (Exception e) {
			String message = "获取监控日志发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	@RequestMapping(value = "excel", method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	public Map<String, Object> createExcel(String monitorSource, String monitorType,
 String monitorName,
			String monitorStatus, String sourceId, String beginningUpdateTime, String endUpdateTime) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			String username = principal.getUsername();
			String role = principal.getRoleName();
			List<com.sinosoft.type.MonitorLog> monitorLogs  = monitorLogService.getMonitorLogs(username, role, 
					monitorSource, monitorType, monitorName, monitorStatus, sourceId, beginningUpdateTime, endUpdateTime);
			
			SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMddHHmmss");
			String fileName = dateFormatter.format(new Date()) + ".xls";
			String filePath = "/excel/monitor-log/" + fileName;
			boolean created = excelOperate.createMonitorLogExcel(filePath, monitorLogs);
			if (true == created) {
				map.put("status", "ok");
				map.put("message", "");
				map.put("file", fileName);				
			} else {
				map.put("status", "error");
				map.put("message", "生成日志文件失败");
			}			
		} catch (Exception e) {
			String message = "生成日志文件发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}
		
		return map;
	}

	@RequestMapping(value = "excel", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> getExcel(String fileName, HttpServletResponse response) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			String filePath = "/excel/monitor-log/" + fileName;
			File file = new File(filePath);
			response.setContentType("application/xlsx");
			response.setHeader("Content-Disposition", "attachment; filename=" + file.getName());
			response.setContentLength((int) file.length());
			InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
			FileCopyUtils.copy(inputStream, response.getOutputStream());

			map.put("status", "ok");
			map.put("message", "");
		} catch (FileNotFoundException e) {
			String message = "获取日志文件发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		} catch (IOException e) {
			String message = "获取日志文件发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}
}
