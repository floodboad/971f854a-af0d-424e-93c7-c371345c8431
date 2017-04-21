package com.sinosoft.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sinosoft.common.ActionResult;
import com.sinosoft.enumerator.ExternalIpStatusEnum;
import com.sinosoft.model.ExternalIp;
import com.sinosoft.service.NetworkService;
import com.sinosoft.util.security.Principal;

@RequestMapping("/network")
@Controller
public class NetworkController {
	private static Logger logger = LoggerFactory.getLogger(NetworkController.class);
	
	@Autowired
	NetworkService networkService;

	@Autowired
	Principal principal;

	@RequestMapping(value = "external-ip/reload", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> reloadExternalIp() {
		Map<String, Object> map = new HashMap<String, Object>();
		
		try {
			networkService.reloadExternalIp();
			map.put("status", "ok");
			map.put("message", "");
		} catch (Exception e) {
			String message = "加载外网地址发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}
		
		return map;		
	}


	@RequestMapping(value = "external-ip/list", method = RequestMethod.GET)
	public String getExternalIps(ModelMap map) {		
		return "external_ip_list_tile";
	}

	@RequestMapping(value = "external-ip/pagination-json-list", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> getPaginationJsonList(int pageNo, int pageSize, String ip, String status) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			Map<String, Object> externalIpMap = networkService.getExternalIpEntries(pageNo, pageSize,
					ip, status);

			map.put("status", "ok");
			map.put("message", "");
			map.putAll(externalIpMap);
		} catch (Exception e) {
			String message = "获取网络地址列表发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}
	
	@RequestMapping(value = "external-ip/count", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> getJsonList() {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			List<ExternalIp> externalIps = ExternalIp.findAllExternalIps();

			map.put("status", "ok");
			map.put("message", "");
			map.put("count", externalIps.size());
		} catch (Exception e) {
			String message = "获取网络地址数量发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}
	
	@RequestMapping(value = "external-ip/available-addresses", method = RequestMethod.GET)
	public @ResponseBody Map<String, Object> getAvailableAddresses() {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			List<ExternalIp> externalIps = ExternalIp.getExternalIps(ExternalIpStatusEnum.AVAILABLE);

			map.put("status", "ok");
			map.put("message", "");
			map.put("external_ips", externalIps);
		} catch (Exception e) {
			String message = "获取网络地址列表发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);	
		}

		return map;
	}
	
	@RequestMapping(value = "external-ip/associate-to-server", method = RequestMethod.POST)
	public @ResponseBody Map<String, Object> createFloatingIp(String ipAddress, String vmId) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			ActionResult result = networkService.createFloatingIp(principal.getUsername(), ipAddress, vmId);
			if (true == result.isSuccess()) {
				map.put("status", "ok");
				map.put("message", "");				
			} else {
				map.put("status", "ok");
				map.put("message", result.getMessage());				
			}
		} catch (Exception e) {
			String message = "绑定访问地址发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);	
		}

		return map;
	}
	
	@RequestMapping(value = "external-ip/disassociate-from-server", method = RequestMethod.POST)
	public @ResponseBody Map<String, Object> deleteFloatingIp(String ipAddress, String vmId) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			ActionResult result = networkService.deleteFloatingIp(principal.getUsername(), ipAddress, vmId);
			if (true == result.isSuccess()) {
				map.put("status", "ok");
				map.put("message", "");				
			} else {
				map.put("status", "ok");
				map.put("message", result.getMessage());				
			}
		} catch (Exception e) {
			String message = "解绑访问地址发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);	
		}

		return map;
	}
}
