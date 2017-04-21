package com.sinosoft.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.sinosoft.common.ActionResult;
import com.sinosoft.type.ApplicationTag;

@Service
public interface ApplicationTagService {
	/**
	 * get application tag list in pagination style.
	 * 
	 * @param pageNo
	 *            - page number
	 * @param pageSize
	 *            - number of monitor logs in each page
	 * @return map contains application tag list, page number and total page count
	 * @author xiangqian
	 */
	public Map<String, Object> getApplicationTagEntries(int pageNo, int pageSize);

	/**
	 * check if application tag with the given name exist.
	 * 
	 * @param name
	 *            - application tag
	 * @return true if application tag exist, return false otherwise
	 * @author xiangqian
	 */
	public boolean checkExistenceByName(String name);

	/**
	 * create application tag.
	 * 
	 * @param username
	 *            - name of user who perform the action
	 * @param name
	 *            - application tag name
	 * @param description
	 *            - application tag description
	 * @return action result
	 * @author xiangqian
	 */
	public ActionResult createApplicationTag(String username, String name, String description);

	/**
	 * modify application tag.
	 * 
	 * @param username
	 *            - name of user who perform the action
	 * @param id
	 *            - application tag id
	 * @param newName
	 *            - application tag new name
	 * @param newDescription
	 *            - application tag new description
	 * @return action result
	 * @author xiangqian
	 */
	public ActionResult modifyApplicationTag(String username, String id, String newName, String newDescription);

	/**
	 * delete application tag. the tag is set disabled.
	 * 
	 * @param username
	 *            - name of user who perform the action
	 * @param id
	 *            - application tag id
	 * @return action result
	 * @author xiangqian
	 */
	public ActionResult deleteApplicationTag(String username, String id);

	/**
	 * get application tag list.
	 * 
	 * @return application tag list
	 * @author xiangqian
	 */
	public List<ApplicationTag> getList();
}
