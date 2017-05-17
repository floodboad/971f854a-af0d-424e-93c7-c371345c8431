package com.sinoparasoft.web;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
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

import com.sinoparasoft.service.OperationLogService;
import com.sinoparasoft.type.OperationLog;
import com.sinoparasoft.util.ExcelOperate;
import com.sinoparasoft.util.security.Principal;

@RequestMapping("/operation-log")
@Controller
public class OperationLogController {
	private static Logger logger = LoggerFactory.getLogger(OperationLogController.class);

	@Autowired
	OperationLogService operationLogService;

	@Autowired
	Principal principal;

	@Autowired
	ExcelOperate excelOperate;

	@RequestMapping(value = "list", method = RequestMethod.GET)
	public String getList(ModelMap map) {
		return "operation_log_list_tile";
	}

	@RequestMapping(value = "json-list", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> getJsonList(int pageNo, int pageSize, String serviceName, String operator,
			String operationStatus, String severityLevel, String objectId, String startTime, String endTime) {
		Map<String, Object> map = new HashMap<String, Object>();

		String username = principal.getUsername();
		String role = principal.getRoleName();

		try {
			Map<String, Object> operationLogMap = operationLogService.getOperationLogEntries(username, role,
					serviceName, operator, operationStatus, severityLevel, objectId, startTime, endTime, pageNo,
					pageSize);

			map.put("status", "ok");
			map.put("message", "");
			map.putAll(operationLogMap);
		} catch (Exception e) {
			String message = "获取操作日志列表发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	@RequestMapping(method = RequestMethod.POST, value = "excel", produces = "application/json")
	@ResponseBody
	public Map<String, Object> createExcel(String serviceName, String operator, String operationStatus,
			String severityLevel, String objectId, String startTime, String endTime) {
		Map<String, Object> map = new HashMap<String, Object>();

		String username = principal.getUsername();
		String role = principal.getRoleName();

		try {
			List<OperationLog> operationLogList = operationLogService.getOperationLogs(username, role, serviceName,
					operator, operationStatus, severityLevel, objectId, startTime, endTime);

			SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMddHHmmss");
			String fileName = dateFormatter.format(new Date()) + ".xls";
			String filePath = "/excel/operation-log/" + fileName;
			excelOperate.createOperationLogsExcel(filePath, operationLogList);

			map.put("status", "ok");
			map.put("message", "");
			map.put("fileName", fileName);
		} catch (Exception e) {
			String message = "生成日志文件发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	@RequestMapping(method = RequestMethod.GET, value = "excel")
	@ResponseBody
	public Map<String, Object> getExcel(String fileName, HttpServletResponse response) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			String filePath = "/excel/operation-log/" + fileName;
			File file = new File(filePath);
			response.setContentType("application/xlsx");
			response.setHeader("Content-Disposition", "attachment; filename=" + file.getName());
			response.setContentLength((int) file.length());
			InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
			FileCopyUtils.copy(inputStream, response.getOutputStream());

			map.put("status", "ok");
			map.put("message", "");
		} catch (Exception e) {
			String message = "下载日志文件发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}
}
