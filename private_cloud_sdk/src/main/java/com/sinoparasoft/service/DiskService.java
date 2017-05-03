package com.sinoparasoft.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sinoparasoft.common.ActionResult;
import com.sinoparasoft.common.BatchActionResult;

@Service
public interface DiskService {
	/**
	 * get disk value list from specified id list.
	 * 
	 * @param diskIds
	 *            - disk id list
	 * @return disk value list
	 * @author xiangqian
	 */
	public List<com.sinoparasoft.type.Disk> getList(List<String> diskIds);

	/**
	 * create disk.
	 * 
	 * @param username
	 *            - name of user who perform the action
	 * @param diskName
	 *            - disk name
	 * @param description
	 *            - disk description
	 * @param capacity
	 *            - disk size
	 * @param manager
	 *            - manager
	 * @param aliveDays
	 *            - number of days the disk is valid
	 * @return action result
	 * @author xiangqian
	 */
	public ActionResult createDisk(String username, String diskName, String description, Integer capacity,
			String manager, int aliveDays);

	/**
	 * delete disks.
	 * 
	 * @param username
	 *            - name of user who perform the action
	 * @param diskIds
	 *            - disk id list
	 * @return batch action result
	 * @author xiangqian
	 */
	public BatchActionResult deleteDisks(String username, List<String> diskIds);

	/**
	 * get disk value by id.
	 * 
	 * @param id
	 *            - disk id
	 * @return disk value, or null if disk is not found
	 * @author xiangqian
	 */
	public com.sinoparasoft.type.Disk getDiskById(String id);

	/**
	 * modify disk.
	 * 
	 * @param username
	 *            - name of user who perform the action
	 * @param diskId
	 *            - disk id
	 * @param diskName
	 *            - new name
	 * @param description
	 *            - new description
	 * @return action result
	 * @author xiangqian
	 */
	public ActionResult modifyDisk(String username, String diskId, String diskName, String description);

	/**
	 * set disk manager.
	 * 
	 * @param username
	 *            - name of user who perform the action
	 * @param diskId
	 *            - disk id
	 * @param manager
	 *            - manager
	 * @return action result
	 * @author xiangqian
	 */
	public ActionResult setManager(String username, String diskId, String manager);

	/**
	 * set valid time. if the current valid time is null, set new valid time = now() + aliveDays, otherwise set valid
	 * time = current + aliveDays.
	 * 
	 * @param username
	 *            - name of user who perform the action
	 * @param diskId
	 *            - disk id
	 * @param aliveDays
	 *            - added days
	 * @return action result
	 * @author xiangqian
	 */
	public ActionResult setValidTime(String username, String diskId, int aliveDays);

	/**
	 * get available disk list managed by given manager.
	 * 
	 * @param manager
	 *            - manager
	 * @return available disk list
	 * @author xiangqian
	 */
	public List<com.sinoparasoft.type.Disk> getUserAvailableDisks(String manager);

	/**
	 * get disks attached to given virtual machine.
	 * 
	 * @param vmId
	 *            - virtual machine id
	 * @return disk list, or null if given virtual machine is not found
	 * @author xiangqian
	 */
	public List<com.sinoparasoft.type.Disk> getDisksByVirtualMachine(String vmId);
}
