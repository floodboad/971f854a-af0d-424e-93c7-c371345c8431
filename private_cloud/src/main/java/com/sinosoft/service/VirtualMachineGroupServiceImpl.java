package com.sinosoft.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sinosoft.common.ActionResult;
import com.sinosoft.common.ActionResultLogLevelEnum;
import com.sinosoft.enumerator.OperationSeverityEnum;
import com.sinosoft.enumerator.OperationStatusEnum;
import com.sinosoft.enumerator.ServiceNameEnum;
import com.sinosoft.enumerator.VirtualMachineGroupStatusEnum;
import com.sinosoft.model.VirtualMachine;
import com.sinosoft.model.VirtualMachineDomain;
import com.sinosoft.model.VirtualMachineGroup;
import com.sinosoft.type.GroupResourceUsage;
import com.sinosoft.type.ResourceUsageItem;
import com.sinosoft.util.ActionResultHelper;

@Service
public class VirtualMachineGroupServiceImpl implements VirtualMachineGroupService {
	private static Logger logger = LoggerFactory.getLogger(VirtualMachineGroupServiceImpl.class);

	@Autowired
	OperationLogService operationLogService;

	@Autowired
	ActionResultHelper actionResultHelper;

	public com.sinosoft.type.VirtualMachineGroup getGroupById(String groupId) {
		VirtualMachineGroup group = VirtualMachineGroup.getGroup(groupId);
		if (null == group) {
			return null;
		}

		com.sinosoft.type.VirtualMachineGroup groupValue = buildVirtualMachineGroupValue(group);
		return groupValue;
	}

	/**
	 * build virtual machine group value can be used in json format.
	 * 
	 * @param group
	 *            - virtual machine group model
	 * @return virtual machine group value
	 * @author xiangqian
	 */
	private com.sinosoft.type.VirtualMachineGroup buildVirtualMachineGroupValue(VirtualMachineGroup group) {
		com.sinosoft.type.VirtualMachineGroup groupValue = new com.sinosoft.type.VirtualMachineGroup();

		groupValue.setId(group.getGroupId());
		groupValue.setName(group.getGroupName());
		groupValue.setDescription(group.getDescription());

		groupValue.setDomainId(group.getDomainId());
		VirtualMachineDomain domain = VirtualMachineDomain.getDomainById(group.getDomainId());
		if (null != domain) {
			groupValue.setDomainName(domain.getDomainName());
		} else {
			groupValue.setDomainName("未知");
		}

		groupValue.setCreator(group.getCreator());
		if (null != group.getCreateTime()) {
			SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			groupValue.setCreateTime(dateFormatter.format(group.getCreateTime()));
		} else {
			groupValue.setCreateTime("未知");
		}

		groupValue.setCpu(group.getCpu());
		groupValue.setMemory(group.getMemory());
		groupValue.setDisk(group.getDisk());

		return groupValue;
	}

	public GroupResourceUsage getReourceUsage(String groupId) {
		ResourceUsageItem cpuUsage = new ResourceUsageItem();
		ResourceUsageItem memoryUsage = new ResourceUsageItem();
		ResourceUsageItem diskUsage = new ResourceUsageItem();

		VirtualMachineGroup group = VirtualMachineGroup.getGroup(groupId);
		if (null == group) {
			logger.error("获取虚拟机组资源使用统计失败，指定的虚拟机组不存在，虚拟机组ID：" + groupId);
			return null;
		}

		// quota
		cpuUsage.setQuota(group.getCpu());
		memoryUsage.setQuota(group.getMemory());
		diskUsage.setQuota(group.getDisk());

		// used
		Map<String, Long> usedResource = VirtualMachine.getGroupUsedResource(groupId);
		cpuUsage.setUsed(usedResource.get("cpu"));
		memoryUsage.setUsed(usedResource.get("memory"));
		diskUsage.setUsed(usedResource.get("disk"));

		// unused
		cpuUsage.setUnused(cpuUsage.getQuota() - cpuUsage.getUsed());
		memoryUsage.setUnused(memoryUsage.getQuota() - memoryUsage.getUsed());
		diskUsage.setUnused(diskUsage.getQuota() - diskUsage.getUsed());

		GroupResourceUsage resourceUsage = new GroupResourceUsage();
		resourceUsage.setGroupName(group.getGroupName());
		resourceUsage.setCpuUsage(cpuUsage);
		resourceUsage.setMemoryUsage(memoryUsage);
		resourceUsage.setDiskUsage(diskUsage);
		return resourceUsage;
	}

	public ActionResult createGroup(String username, String domainId, String groupName, String description, int cpu,
			int memory, int disk) {
		VirtualMachineGroup group = VirtualMachineGroup.getGroup(domainId, groupName);
		if (null != group) {
			String action = "检查同名虚拟机组";
			String message = "创建虚拟机组失败，同名虚拟机组已存在，虚拟机域ID：" + domainId + "，虚拟机组名：" + groupName;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		/*
		 * sava data
		 */
		group = new VirtualMachineGroup();
		group.setDomainId(domainId);
		group.setGroupName(groupName);
		group.setDescription(description);
		group.setCpu(cpu);
		group.setMemory(memory);
		group.setDisk(disk);
		group.setCreator(username);
		group.setCreateTime(new Date());
		group.setStatus(VirtualMachineGroupStatusEnum.ENABLED);
		group.persist();
		group.flush();
		group.clear();

		String groupId = group.getGroupId();

		/*
		 * save operation log
		 */
		String operator = username;
		Date operationTime = new Date();
		String operation = "创建虚拟机组";
		OperationStatusEnum operationStatus = OperationStatusEnum.OK;
		ServiceNameEnum serviceName = ServiceNameEnum.GROUP_MANAGEMENT;
		String objectId = groupId;
		String operationResult = "创建虚拟机组成功，ID：" + groupId + "，组名" + groupName + "，CPU" + cpu + "个，内存" + memory + "GB，磁盘"
				+ disk + "GB";
		OperationSeverityEnum operationSeverity = OperationSeverityEnum.LOW;
		operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
				operationResult, operationSeverity);

		String action = "创建虚拟机组";
		String message = "";
		return actionResultHelper.createActionResult(true, action, message, false, logger,
				ActionResultLogLevelEnum.NONE);
	}

	public ActionResult modifyGroup(String username, String groupId, String groupName, String description, int cpu,
			int memory, int disk) {
		VirtualMachineGroup group = VirtualMachineGroup.getGroup(groupId);
		if (null == group) {
			String action = "检查虚拟机组";
			String message = "修改虚拟机组失败，虚拟机组不存在，虚拟机组ID：" + groupId;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		VirtualMachineGroup tempGroup = VirtualMachineGroup.getGroup(group.getDomainId(), groupName);
		if ((null != tempGroup) && (false == tempGroup.getGroupId().equalsIgnoreCase(groupId))) {
			String action = "检查同名虚拟机组";
			String message = "修改虚拟机组失败，同名虚拟机组已存在，虚拟机组ID：" + groupId + "，虚拟机组名：" + groupName;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		String originalGroupName = group.getGroupName();
		int originalCpu = group.getCpu();
		int originalMemory = group.getMemory();
		int origianlDisk = group.getDisk();
		String origianlDescription = group.getDescription();

		/*
		 * save data
		 */
		group.setGroupName(groupName);
		group.setDescription(description);
		group.setCpu(cpu);
		group.setMemory(memory);
		group.setDisk(disk);
		group.merge();
		group.flush();
		group.clear();

		/*
		 * save operation log
		 */
		String operator = username;
		Date operationTime = new Date();
		String operation = "修改虚拟机组";
		OperationStatusEnum operationStatus = OperationStatusEnum.OK;
		ServiceNameEnum serviceName = ServiceNameEnum.GROUP_MANAGEMENT;
		String objectId = groupId;
		String operationResult = "修改虚拟机组成功，虚拟机组ID：" + groupId + "，修改前的信息：组名" + originalGroupName + "，CPU" + originalCpu
				+ "个，内存" + originalMemory + "GB，磁盘" + origianlDisk + "GB，描述" + origianlDescription + "，修改后的信息：组名"
				+ groupName + "，CPU" + cpu + "颗，内存" + memory + "GB，磁盘" + disk + "GB，描述" + description;
		OperationSeverityEnum operationSeverity = OperationSeverityEnum.MIDDLE;
		operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
				operationResult, operationSeverity);

		String action = "修改虚拟机组";
		String message = "";
		return actionResultHelper.createActionResult(true, action, message, false, logger,
				ActionResultLogLevelEnum.NONE);
	}

	public ActionResult deleteGroup(String username, String groupId) {
		VirtualMachineGroup group = VirtualMachineGroup.getGroup(groupId);
		if (null == group) {
			String action = "检查虚拟机组";
			String message = "刪除虚拟机组失败，虚拟机组不存在，虚拟机组ID：" + groupId;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		List<? extends VirtualMachine> machines = VirtualMachine.getVirtualMachinesByGroupId(groupId);
		if (machines.size() != 0) {
			String action = "检查虚拟机组是否包含虚拟机";
			String message = "刪除虚拟机组失败，当前虚拟机组非空，请删除所属的虚拟机后再尝试删除，虚拟机组ID：" + groupId;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		/*
		 * save data
		 */
		group.setStatus(VirtualMachineGroupStatusEnum.DELETED);
		group.setDeleteTime(new Date());
		group.merge();
		group.flush();
		group.clear();

		/*
		 * save operation log
		 */
		String operator = username;
		Date operationTime = new Date();
		String operation = "删除虚拟机组";
		OperationStatusEnum operationStatus = OperationStatusEnum.OK;
		ServiceNameEnum serviceName = ServiceNameEnum.GROUP_MANAGEMENT;
		String objectId = groupId;
		String operationResult = "删除虚拟机组成功，ID：" + groupId;
		OperationSeverityEnum operationSeverity = OperationSeverityEnum.HIGH;
		operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
				operationResult, operationSeverity);

		String action = "删除虚拟机组";
		String message = "";
		return actionResultHelper.createActionResult(true, action, message, false, logger,
				ActionResultLogLevelEnum.NONE);
	}
}
