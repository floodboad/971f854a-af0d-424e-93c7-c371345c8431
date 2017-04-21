package com.sinosoft.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sinosoft.common.ActionResult;
import com.sinosoft.type.DomainOverallResourceUsage;
import com.sinosoft.type.DomainResourceUsage;

@Service
public interface VirtualMachineDomainService {
	/**
	 * create domain.
	 * 
	 * @param username
	 *            - name of user who perform the action
	 * @param domainName
	 *            - domain name
	 * @param description
	 *            - domain description
	 * @param cpu
	 *            - cpu quota
	 * @param memory
	 *            - memory quota
	 * @param disk
	 *            - disk quota
	 * @return action result
	 * @author xiangqian
	 */
	public ActionResult createDomain(String username, String domainName, String description, int cpu, int memory,
			int disk);

	/**
	 * update domain.
	 * 
	 * @param username
	 *            - name of user who perform the action
	 * @param domainId
	 *            - domain id
	 * @param domainName
	 *            - domain name
	 * @param description
	 *            - domain description
	 * @param cpu
	 *            - cpu quota
	 * @param memory
	 *            - memory quota
	 * @param disk
	 *            - disk quota
	 * @return action result
	 * @author xiangqian
	 */
	public ActionResult modifyDomain(String username, String domainId, String domainName, String description, int cpu,
			int memory, int disk);

	/**
	 * delete domain.
	 * 
	 * @param username
	 *            - name of user who perform the action
	 * @param domainId
	 *            - domain id
	 * @return action result
	 * @author xiangqian
	 */
	public ActionResult deleteDomain(String username, String domainId);

	/**
	 * get resource usage statistics of a domain, including cpu, memory and disk. for each resource, get its value of
	 * quota, allocated, unallocated, used and unused. quota = allocated + unallocated, allocated = used + unused
	 * 
	 * @param domainId - domain id
	 * @return resource usage, or null if domain not found
	 * @author xiangqian
	 */
	public DomainResourceUsage getResourceUsage(String domainId);

	public List<DomainOverallResourceUsage> getOverallReourceUsage();
}
