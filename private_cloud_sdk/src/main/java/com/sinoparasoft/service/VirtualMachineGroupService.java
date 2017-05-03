package com.sinoparasoft.service;

import org.springframework.stereotype.Service;

import com.sinoparasoft.common.ActionResult;
import com.sinoparasoft.type.GroupResourceUsage;
import com.sinoparasoft.type.VirtualMachineGroup;

@Service
public interface VirtualMachineGroupService {
	/**
	 * get virtual machine group by id.
	 * 
	 * @param groupId
	 *            - virtual machine group id
	 * @return virtual machine group value, or null if virtual machine group is not found
	 * @author xiangqian
	 */
	public VirtualMachineGroup getGroupById(String groupId);

	/**
	 * get resource usage statistics of a group, including cpu, memory and disk. for each resource, get its value of
	 * quota, used and unused. quota = used + unused
	 * 
	 * @param groupId
	 *            - group id
	 * @return resource usage, or null if group not found
	 * @author xiangqian
	 */
	public GroupResourceUsage getReourceUsage(String groupId);

	/**
	 * create virtual machine group.
	 * 
	 * @param username
	 * 
	 * @param domainId
	 *            - domain id
	 * @param groupName
	 *            - group name
	 * @param description
	 *            - group description
	 * @param cpu
	 *            - cpu quota
	 * @param memory
	 *            - memory quota
	 * @param disk
	 *            - disk quota
	 * @return action result
	 * @author xiangqian
	 */
	public ActionResult createGroup(String username, String domainId, String groupName, String description, int cpu,
			int memory, int disk);

	/**
	 * modify virutal machine group.
	 * 
	 * @param username
	 *            - name of user who perform the action
	 * @param groupId
	 *            - group id
	 * @param groupName
	 *            - group name
	 * @param description
	 *            - group description
	 * @param cpu
	 *            - cpu quota
	 * @param memory
	 *            - memory quota
	 * @param disk
	 *            - disk quota
	 * @return action result
	 * @author xiangqian
	 */
	public ActionResult modifyGroup(String username, String groupId, String groupName, String description, int cpu,
			int memory, int disk);

	/**
	 * delete group.
	 * 
	 * @param username
	 *            - name of user who perform the action
	 * @param groupId
	 *            - group id
	 * @return action result
	 * @author xiangqian
	 */
	public ActionResult deleteGroup(String username, String groupId);
}
