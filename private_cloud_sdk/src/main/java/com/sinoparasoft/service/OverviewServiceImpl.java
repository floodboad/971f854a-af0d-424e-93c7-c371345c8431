package com.sinoparasoft.service;

import java.util.List;

import org.openstack4j.model.compute.ext.Hypervisor;
import org.openstack4j.model.image.Image;
import org.openstack4j.model.storage.block.BlockLimits.Absolute;
import org.openstack4j.model.storage.block.BlockQuotaSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sinoparasoft.ceph.CephStatus;
import com.sinoparasoft.ceph.CephStorage;
import com.sinoparasoft.common.AppConfig;
import com.sinoparasoft.model.VirtualMachineDomain;
import com.sinoparasoft.openstack.CloudManipulator;
import com.sinoparasoft.openstack.CloudManipulatorFactory;
import com.sinoparasoft.openstack.type.CloudConfig;
import com.sinoparasoft.type.DomainResourceUsage;
import com.sinoparasoft.type.OverviewResourceUsage;
import com.sinoparasoft.type.PartitionedResourceUsageItem;
import com.sinoparasoft.type.ResourceUsageItem;
import com.sinoparasoft.type.StorageResourceUsage;

@Service
public class OverviewServiceImpl implements OverviewService {
	@Autowired
	CloudConfig cloudConfig;

	@Autowired
	AppConfig appConfig;

	@Autowired
	VirtualMachineDomainService virtualMachineDomainService;

	public OverviewResourceUsage getResourceUsage() {
		PartitionedResourceUsageItem cpuUsage = new PartitionedResourceUsageItem();
		PartitionedResourceUsageItem memoryUsage = new PartitionedResourceUsageItem();
		PartitionedResourceUsageItem storageUsage = new PartitionedResourceUsageItem();

		CloudManipulator cloud = CloudManipulatorFactory.createCloudManipulator(cloudConfig,
				cloudConfig.getAdminProjectId());

		// quota
		long cpuQuota = 0;
		long memoryQuota = 0;
		List<? extends Hypervisor> hypervisors = cloud.getHypervisors();
		for (Hypervisor h : hypervisors) {
			cpuQuota += h.getVirtualCPU();
			memoryQuota += h.getLocalMemory() / 1024;
		}
		cpuUsage.setQuota(cpuQuota);
		memoryUsage.setQuota(memoryQuota);

		CephStorage storage = new CephStorage(appConfig.getCephRestApiUrl());
		int cephPoolReplicaSize = appConfig.getCephPoolReplicaSize();
		CephStatus status = storage.getStatus();
		int effectiveStorageSize = status.getTotalSize() - appConfig.getCephJounralSize();
		long storageQuota = (long) effectiveStorageSize / cephPoolReplicaSize;
		storageUsage.setQuota(storageQuota);

		// allocated
		long allocatedCpu = 0;
		long allocatedMemory = 0;
		long allocatedDisk = 0;
		long usedCpu = 0;
		long usedMemory = 0;
		long usedDisk = 0;
		List<VirtualMachineDomain> domains = VirtualMachineDomain.getDomains();
		for (VirtualMachineDomain domain : domains) {
			DomainResourceUsage domainResourceUsage = virtualMachineDomainService
					.getResourceUsage(domain.getDomainId());
			if (null == domainResourceUsage) {
				continue;
			}

			// cloud allocated value is the sum of domain quota value
			allocatedCpu += domainResourceUsage.getCpuUsage().getQuota();
			allocatedMemory += domainResourceUsage.getMemoryUsage().getQuota();
			allocatedDisk += domainResourceUsage.getDiskUsage().getQuota();

			usedCpu += domainResourceUsage.getCpuUsage().getUsed();
			usedMemory += domainResourceUsage.getMemoryUsage().getUsed();
			usedDisk += domainResourceUsage.getDiskUsage().getUsed();
		}
		cpuUsage.setAllocated(allocatedCpu);
		memoryUsage.setAllocated(allocatedMemory);

		// for storage, allocated = (domain allocated for vm) + (block storage allocated) + (image & shapshot allocated)
		long allocatedStorage = allocatedDisk;
		ResourceUsageItem blockStorageUsage = getBloackStorageUsage();
		allocatedStorage += blockStorageUsage.getQuota();
		ResourceUsageItem imageSnapshotUsage = getImageSnapshotStorageUsage();
		allocatedStorage += imageSnapshotUsage.getQuota();
		storageUsage.setAllocated(allocatedStorage);

		// unallocated
		long unallocatedCpu = cpuQuota - allocatedCpu;
		long unallocatedMemory = memoryQuota - allocatedMemory;
		cpuUsage.setUnallocated(unallocatedCpu);
		memoryUsage.setUnallocated(unallocatedMemory);

		long unallocatedStorage = storageQuota - allocatedStorage;
		storageUsage.setUnallocated(unallocatedStorage);

		// used
		cpuUsage.setUsed(usedCpu);
		memoryUsage.setUsed(usedMemory);

		/*
		 * get used storage from ceph, it contains vms/volumes/images ... pool usage, exclude journal size
		 */
		// int effectiveUsedSize = status.getUsedSize() - appConfig.getCephJounralSize();
		// long usedStorage = (long) effectiveUsedSize / cephPoolReplicaSize;
		// storageUsage.setUsed(usedStorage);
		/*
		 * used storage = domain used + block storage used + image&snapshot used. the usage is nominal usage, and not
		 * take into account thin provision. it's smaller than the actual usage in ceph.
		 */
		long usedStorage = usedDisk;
		usedStorage += blockStorageUsage.getUsed();
		usedStorage += imageSnapshotUsage.getUsed();
		storageUsage.setUsed(usedStorage);

		// unused
		cpuUsage.setUnused(cpuUsage.getAllocated() - cpuUsage.getUsed());
		memoryUsage.setUnused(memoryUsage.getAllocated() - memoryUsage.getUsed());

		storageUsage.setUnused(storageUsage.getAllocated() - storageUsage.getUsed());

		OverviewResourceUsage resourceUsage = new OverviewResourceUsage();
		resourceUsage.setCpuUsage(cpuUsage);
		resourceUsage.setMemoryUsage(memoryUsage);
		resourceUsage.setStorageUsage(storageUsage);
		return resourceUsage;
	}

	public StorageResourceUsage getStorageResourceUsage() {
		StorageResourceUsage resourceUsage = new StorageResourceUsage();
		resourceUsage.setVirtualMachineUsage(getVirtualMachineStorageUsage());
		resourceUsage.setImageSnapshotUsage(getImageSnapshotStorageUsage());
		resourceUsage.setBlockStorageUsage(getBloackStorageUsage());
		return resourceUsage;
	}

	/**
	 * get virtual machine storage usage (nominal) in unified storage
	 * 
	 * @return virtual machine storage usage
	 * @author xiangqian
	 */
	private ResourceUsageItem getVirtualMachineStorageUsage() {
		ResourceUsageItem usageItem = new ResourceUsageItem();

		long storageQuota = 0;
		long usedStorage = 0;
		List<VirtualMachineDomain> domains = VirtualMachineDomain.getDomains();
		for (VirtualMachineDomain domain : domains) {
			DomainResourceUsage domainResourceUsage = virtualMachineDomainService
					.getResourceUsage(domain.getDomainId());
			if (null == domainResourceUsage) {
				continue;
			}
			storageQuota += domainResourceUsage.getDiskUsage().getQuota();
			usedStorage += domainResourceUsage.getDiskUsage().getUsed();
		}

		usageItem.setQuota(storageQuota);
		usageItem.setUsed(usedStorage);
		usageItem.setUnused(storageQuota - usedStorage);

		return usageItem;
	}

	/**
	 * get image and snapshot storage usage (nominal) in unified storage
	 * 
	 * @return image and snapshot usage
	 * @author xiangqian
	 */
	private ResourceUsageItem getImageSnapshotStorageUsage() {
		ResourceUsageItem usageItem = new ResourceUsageItem();

		long storageQuota = appConfig.getCephImagePoolSize();
		long usedStorage = 0;
		CloudManipulator cloud = CloudManipulatorFactory.createCloudManipulator(cloudConfig,
				cloudConfig.getAdminProjectId());
		List<? extends Image> images = cloud.getImages();
		for (Image image : images) {
			// getSize() return null if image is in SAVING status
			Image.Status status = image.getStatus();
			if (Image.Status.ACTIVE == status) {
				usedStorage += image.getSize() / 1024 / 1024 / 1024;
			}
		}

		usageItem.setQuota(storageQuota);
		usageItem.setUsed(usedStorage);
		usageItem.setUnused(storageQuota - usedStorage);

		return usageItem;
	}

	/**
	 * get (admin project) block storage usage (nominal) in unified storage
	 * 
	 * @return block storage usage
	 * @author xiangqian
	 */
	private ResourceUsageItem getBloackStorageUsage() {
		ResourceUsageItem usageItem = new ResourceUsageItem();

		long storageQuota = 0;
		long usedStorage = 0;

		CloudManipulator cloud = CloudManipulatorFactory.createCloudManipulator(cloudConfig,
				cloudConfig.getAdminProjectId());
		Absolute absolute = cloud.getBlockStorageQuotaUsage();
		storageQuota = (long) absolute.getMaxTotalVolumeGigabytes();
		usedStorage = (long) absolute.getTotalGigabytesUsed();

		usageItem.setQuota(storageQuota);
		usageItem.setUsed(usedStorage);
		usageItem.setUnused(storageQuota - usedStorage);

		return usageItem;
	}

	public void updateBlockStorageQuota(int volumes, int gigabytes) {
		CloudManipulator cloud = CloudManipulatorFactory.createCloudManipulator(cloudConfig,
				cloudConfig.getAdminProjectId());
		@SuppressWarnings("unused")
		BlockQuotaSet quota = cloud.updateBlockStorageQuota(volumes, gigabytes);
		return;
	}
}
