package com.sinosoft.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.sinosoft.type.Image;

@Service
public interface ImageService {
	/**
	 * get image list.
	 * 
	 * @param pageNo
	 *            - page number
	 * @param pageSize
	 *            - page size
	 * @return map contains page number, page count and image list
	 * @author xiangqian
	 */
	public Map<String, Object> getList(int pageNo, int pageSize);

	/**
	 * get image list all in one.
	 * 
	 * @return image list
	 * @author xiangqian
	 */
	public List<Image> getAllInOneList();
}
