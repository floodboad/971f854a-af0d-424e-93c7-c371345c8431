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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sinosoft.service.ImageService;
import com.sinosoft.type.Image;
import com.sinosoft.util.security.Principal;

@RequestMapping("/image")
@Controller
public class ImageController {
	@Autowired
	ImageService imageService;
	
	@Resource
	Principal getusername;

	private static Logger logger = LoggerFactory.getLogger(ImageController.class);

	@RequestMapping(value = "list", method = RequestMethod.GET)
	public String getList(ModelMap map) {
		return "image_list_tile";
	}

	@RequestMapping(value = "json-list", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> getJsonList(int pageNo) {
		Map<String, Object> map = new HashMap<String, Object>();
		int pageSize = 10;

		try {
			Map<String, Object> imageMap = imageService.getList(pageNo, pageSize);		

			map.put("status", "ok");
			map.put("message", "");
			map.putAll(imageMap);
		} catch (Exception e) {
			String message = "获取镜像列表发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
			return map;
		}

		return map;
	}
	
	@RequestMapping(value = "all-in-one-list", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> getJsonList() {
		Map<String, Object> map = new HashMap<String, Object>();
		
		try {
			List<Image> imageList = imageService.getAllInOneList();			
			map.put("status", "ok");
			map.put("message", "");
			map.put("images", imageList);
		} catch (Exception e) {
			String message = "获取镜像列表发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
			return map;			
		}
		
		return map;
	}
}
