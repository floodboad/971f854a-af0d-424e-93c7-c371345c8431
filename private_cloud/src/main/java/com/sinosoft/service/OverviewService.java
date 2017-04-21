package com.sinosoft.service;

import org.springframework.stereotype.Service;

import com.sinosoft.type.OverviewResourceUsage;
import com.sinosoft.type.StorageResourceUsage;

@Service
public interface OverviewService {
	/**
	 * get resource usage of the cloud, including cpu, memory and disk. for each resource, get its value of quota,
	 * allocated, unallocated, used and unused. quota = allocated + unallocated, allocated = used + unused
	 * 
	 * @return resource usage
	 * @author xiangqian
	 */
	public OverviewResourceUsage getResourceUsage();

	/**
	 * get storage usage (nominal) in unified storage, including virtual machine, image & snapshot and block storage.
	 * for each type, get its value of quota, used and unused.	 * 
	 * @return storage usage
	 * @author xiangqian
	 */
	public StorageResourceUsage getStorageResourceUsage();

	/**
	 * update block storage quota (of admin project)
	 * @param volumes
	 * @param gigabytes
	 * @return
	 * @author xiangqian
	 */
	public void updateBlockStorageQuota(int volumes, int gigabytes);
}
