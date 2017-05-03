package com.sinoparasoft.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.sinoparasoft.common.ActionResult;

@Service
public interface NetworkService {
	/**
	 * reload external ip table, the table must be truncated before this operation.
	 * 
	 * @author xiangqian
	 */
	public void reloadExternalIp();

	/**
	 * get external ips in pagination style.
	 * 
	 * @param pageNo
	 *            - page number
	 * @param pageSize
	 *            - number of monitor logs in each page
	 * @param ip
	 *            - ip
	 * @param status
	 *            - status
	 * @return map contains external ips, page number and total page count
	 * @author xiangqian
	 */
	public Map<String, Object> getExternalIpEntries(int pageNo, int pageSize, String ip, String status);

	/**
	 * create floating ip and associate it to the given virtual machine.
	 * 
	 * @param username
	 *            - name of user who perform the action
	 * @param ipAddress
	 *            - floating ip address
	 * @param vmId
	 *            - machine id
	 * @return action result
	 * @author xiangqian
	 */
	public ActionResult createFloatingIp(String username, String ipAddress, String vmId);

	/**
	 * delete floating ip, it'll be disassociated from the virtual machine.
	 * 
	 * @param username
	 *            - name of user who perform the action
	 * @param ipAddress
	 *            - floating ip address
	 * @param vmId
	 *            - machine id
	 * @return action result
	 * @author xiangqian
	 */
	public ActionResult deleteFloatingIp(String username, String ipAddress, String vmId);
}
