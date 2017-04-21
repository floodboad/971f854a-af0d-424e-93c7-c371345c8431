package com.sinosoft.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.sinosoft.common.BatchActionResult;

@Service
public interface SnapshotService {
	/**
	 * get snapshot list
	 * 
	 * @param userName
	 *            - user name
	 * @param role
	 *            - user role
	 * @param pageNo
	 *            - page number
	 * @param pageSize
	 *            - page size
	 * @return map contains page number, page count and snapshot list
	 * @author xiangqian
	 */
	public Map<String, Object> getList(String userName, String role, int pageNo, int pageSize);

	/**
	 * get snapshots created from given virtual machine.
	 * 
	 * @param vmId
	 *            - virtual machine id
	 * @return snapshot list, or null if given virtual machine is not found
	 * @author xiangqian
	 */
	public List<com.sinosoft.type.Snapshot> getListByVirtualMachine(String vmId);

	/**
	 * delete snapshots
	 * 
	 * @param username
	 *            - name of user who perform the action
	 * @param snapshotIds
	 *            - snapshot id list
	 * @return batch action result
	 * @author xiangqian
	 */
	public BatchActionResult deleteSnapshots(String username, List<String> snapshotIds);
}
