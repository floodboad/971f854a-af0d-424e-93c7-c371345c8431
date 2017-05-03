package com.sinoparasoft.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openstack4j.model.compute.Flavor;
import org.openstack4j.model.compute.Server;
import org.openstack4j.model.compute.VNCConsole;
import org.openstack4j.model.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinoparasoft.common.ActionResult;
import com.sinoparasoft.common.ActionResultLogLevelEnum;
import com.sinoparasoft.common.AlarmParameter;
import com.sinoparasoft.common.AsyncOperationRequest;
import com.sinoparasoft.common.AsyncOperationTypeEnum;
import com.sinoparasoft.common.BatchActionResult;
import com.sinoparasoft.enumerator.DiskStatusEnum;
import com.sinoparasoft.enumerator.MonitorNameEnum;
import com.sinoparasoft.enumerator.MonitorTypeEnum;
import com.sinoparasoft.enumerator.OperationSeverityEnum;
import com.sinoparasoft.enumerator.OperationStatusEnum;
import com.sinoparasoft.enumerator.ServiceNameEnum;
import com.sinoparasoft.enumerator.SnapshotStatusEnum;
import com.sinoparasoft.enumerator.VirtualMachineStatusEnum;
import com.sinoparasoft.enumerator.VirtualMachineTaskStatusEnum;
import com.sinoparasoft.message.RetryMessageProducer;
import com.sinoparasoft.model.ApplicationTag;
import com.sinoparasoft.model.Disk;
import com.sinoparasoft.model.MonitorResult;
import com.sinoparasoft.model.MonitorSetting;
import com.sinoparasoft.model.Snapshot;
import com.sinoparasoft.model.User;
import com.sinoparasoft.model.VirtualMachine;
import com.sinoparasoft.model.VirtualMachineDomain;
import com.sinoparasoft.model.VirtualMachineGroup;
import com.sinoparasoft.openstack.CloudManipulator;
import com.sinoparasoft.openstack.CloudManipulatorFactory;
import com.sinoparasoft.openstack.type.CloudConfig;
import com.sinoparasoft.openstack.type.telemetry.ServerSamples;
import com.sinoparasoft.type.GroupResourceUsage;
import com.sinoparasoft.type.MQConfig;
import com.sinoparasoft.type.MigrationPolicy;
import com.sinoparasoft.type.StorageResourceUsage;
import com.sinoparasoft.util.ActionResultHelper;

@Service
public class VirtualMachineServiceImpl implements VirtualMachineService {
	private static Logger logger = LoggerFactory.getLogger(VirtualMachineServiceImpl.class);

	@Autowired
	DiskService diskService;

	@Autowired
	OperationLogService operationLogService;

	@Autowired
	CloudConfig cloudConfig;

	@Autowired
	VirtualMachineServiceEpilogue virtualMachineServiceEpilogue;

	@Autowired
	OverviewService overviewService;

	@Autowired
	VirtualMachineGroupService virtualMachineGroupService;

	@Autowired
	MQConfig mqConfig;

	@Autowired
	RetryMessageProducer messageProducer;

	@Autowired
	ActionResultHelper actionResultHelper;

	public com.sinoparasoft.type.VirtualMachine getVirtualMachineById(String id) {
		VirtualMachine machine = VirtualMachine.getVirtualMachine(id);
		if (machine == null) {
			return null;
		}

		com.sinoparasoft.type.VirtualMachine machineValue = buildVirtualMachineValue(machine);
		return machineValue;
	}

	/**
	 * build virtual machine value can be used in json format.
	 * 
	 * @param machine
	 *            - virtual machine model
	 * @return virtual machine value
	 * @author xiangqian
	 */
	private com.sinoparasoft.type.VirtualMachine buildVirtualMachineValue(VirtualMachine machine) {
		com.sinoparasoft.type.VirtualMachine machineValue = new com.sinoparasoft.type.VirtualMachine();
		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		machineValue.setId(machine.getHostId());
		machineValue.setName(machine.getHostName());
		machineValue.setDescription(machine.getDescription());

		String status = "";
		if ((null != machine.getStatus()) && (null != machine.getTaskStatus())) {
			switch (machine.getStatus()) {
			case ACTIVE:
				switch (machine.getTaskStatus()) {
				case DELETING:
					status = "删除中";
					break;
				case MIGRATING:
					status = "在线迁移中";
					break;
				case NONE:
					status = "运行中";
					break;
				case POWERING_OFF:
					status = "关机中";
					break;
				case POWERING_ON:
					status = "开机中";
					break;
				case REBOOTING:
					status = "重启中";
					break;
				default:
					break;
				}

				break;
			case BUILD:
				status = "创建中";
				break;
			case DELETED:
				status = "已删除";
				break;
			case ERROR:
				status = "错误";
				break;
			case STOPPED:
				switch (machine.getTaskStatus()) {
				case NONE:
					status = "已关机";
					break;
				case SNAPSHOTING:
					status = "快照中";
					break;
				default:
					break;
				}

				break;
			case UNKNOWN:
				status = "未知";
				break;
			default:
				break;
			}
		}
		machineValue.setStatus(status);
		machineValue.setCreator(machine.getCreator());
		if (null != machine.getManager()) {
			machineValue.setManager(machine.getManager());
		} else {
			machineValue.setManager("");
		}
		if (null != machine.getCreateTime()) {
			machineValue.setCreateTime(dateFormatter.format(machine.getCreateTime()));
		} else {
			machineValue.setCreateTime("");
		}
		if (null != machine.getModifyTime()) {
			machineValue.setModifyTime(dateFormatter.format(machine.getModifyTime()));
		} else {
			machineValue.setModifyTime("");
		}
		VirtualMachineDomain domain = VirtualMachineDomain.getDomainById(machine.getDomainId());
		if (null != domain) {
			machineValue.setDomainId(machine.getDomainId());
			machineValue.setDomainName(domain.getDomainName());
		} else {
			machineValue.setDomainId("");
			machineValue.setDomainName("");
		}
		VirtualMachineGroup group = VirtualMachineGroup.getGroup(machine.getGroupId());
		if (null != group) {
			machineValue.setGroupId(machine.getGroupId());
			machineValue.setGroupName(group.getGroupName());
		} else {
			machineValue.setGroupId("");
			machineValue.setGroupName("");
		}
		machineValue.setCpu(machine.getCpu());
		machineValue.setMemory(machine.getMemory());
		machineValue.setDisk(machine.getDisk());
		if (null != machine.getImageName()) {
			machineValue.setImageName(machine.getImageName());
		} else {
			machineValue.setImageName("");
		}
		if (null != machine.getPrivateIp()) {
			machineValue.setPrivateIp(machine.getPrivateIp());
		} else {
			machineValue.setPrivateIp("");
		}
		if (null != machine.getFloatingIp()) {
			machineValue.setFloatingIp(machine.getFloatingIp());
		} else {
			machineValue.setFloatingIp("");
		}
		if (null != machine.getHypervisorName()) {
			machineValue.setPhysicalMachine(machine.getHypervisorName());
		} else {
			machineValue.setPhysicalMachine("");
		}

		String monitorStatus = "";
		MonitorResult monitorResult = MonitorResult.getMonitorResult(machine.getHostId(), MonitorTypeEnum.NODE,
				MonitorNameEnum.NONE);
		if (null == monitorResult) {
			monitorStatus = "未知";
		} else {
			switch (monitorResult.getMonitorStatus()) {
			case NORMAL:
				monitorStatus = "正常";
				break;
			case UNKNOWN:
				monitorStatus = "未知";
				break;
			case WARNING:
				monitorStatus = "告警";
				break;
			}
		}
		machineValue.setMonitorStatus(monitorStatus);

		List<com.sinoparasoft.type.ApplicationTag> applications = new ArrayList<com.sinoparasoft.type.ApplicationTag>();
		Set<ApplicationTag> machineApplicationTags = machine.getApplicationTags();
		for (ApplicationTag machineApplicationTag : machineApplicationTags) {
			com.sinoparasoft.type.ApplicationTag application = new com.sinoparasoft.type.ApplicationTag();
			application.setId(machineApplicationTag.getId());
			application.setName(machineApplicationTag.getName());
			application.setDescription(machineApplicationTag.getDescription());
			applications.add(application);
		}
		machineValue.setApplications(applications);

		return machineValue;
	}

	public ActionResult createVirtualMachine(String creator, String hostName, String description, int cpu, int memory,
			int disk, String imageId, String domainId, String groupId, String applicationIds, String manager) {
		if (false == messageProducer.isOpen()) {
			String action = "检查消息连接状态";
			String message = "创建虚拟机失败，消息连接已关闭，请联系运维人员进行处理";
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		/*
		 * check if the group has enough resource
		 */
		GroupResourceUsage groupResourceUsage = virtualMachineGroupService.getReourceUsage(groupId);
		long unusedCpu = groupResourceUsage.getCpuUsage().getUnused();
		long unusedMemory = groupResourceUsage.getMemoryUsage().getUnused();
		long unusedDisk = groupResourceUsage.getDiskUsage().getUnused();
		boolean resourceCheckResult = true;
		String resourceCheckResultMessage = "";
		if (cpu > unusedCpu) {
			resourceCheckResult = false;
			resourceCheckResultMessage += "需要处理器：" + cpu + "颗，剩余处理器：" + unusedCpu + "颗；";
		}
		if (memory > unusedMemory) {
			resourceCheckResult = false;
			resourceCheckResultMessage += "需要内存：" + memory + "GB，剩余内存：" + unusedMemory + "GB；";
		}
		if (disk > unusedDisk) {
			resourceCheckResult = false;
			resourceCheckResultMessage += "需要磁盘：" + disk + "GB，剩余磁盘：" + unusedDisk + "GB；";
		}
		if (false == resourceCheckResult) {
			String action = "检查剩余资源";
			String message = "创建虚拟机失败，虚拟机组剩余资源不足：" + resourceCheckResultMessage;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		CloudManipulator cloud = CloudManipulatorFactory.createCloudManipulator(cloudConfig, domainId);
		Image image = cloud.getImage(imageId);
		if (null == image) {
			String action = "检查镜像";
			String message = "创建虚拟机失败，虚拟机镜像不存在，镜像ID：" + imageId;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		/*
		 * get flavor
		 */
		Flavor flavor = cloud.getFlavor(cpu, memory, disk);
		if (null == flavor) {
			flavor = cloud.createFlavor(cpu, memory, disk);
		}

		/*
		 * create server
		 */
		Server server = cloud.bootServer(hostName, flavor.getId(), imageId);
		String vmId = server.getId();

		/*
		 * save operation log
		 */
		String operator = creator;
		Date operationTime = new Date();
		String operation = "发送创建虚拟机请求";
		OperationStatusEnum operationStatus = OperationStatusEnum.OK;
		ServiceNameEnum serviceName = ServiceNameEnum.VIRTURAL_MACHINE_MANAGEMENT;
		String objectId = vmId;
		String operationResult = "发送创建虚拟机请求成功，虚拟机ID：" + vmId + "，主机名：" + hostName + "，CPU：" + cpu + "个，内存：" + memory
				+ "GB，磁盘：" + disk + "GB，镜像：" + imageId + "，虚拟机域ID：" + domainId + "，虚拟机组ID：" + groupId;
		OperationSeverityEnum operationSeverity = OperationSeverityEnum.LOW;
		operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
				operationResult, operationSeverity);

		/*
		 * getImage() API not support name with Chinese characters, use getImages() instead.
		 */
		// String imageName = image.getName();
		String imageName = "未知镜像";
		List<? extends Image> images = cloud.getImages();
		for (Image img : images) {
			if (img.getId().equalsIgnoreCase(imageId)) {
				imageName = img.getName();
				break;
			}
		}

		/*
		 * save machine data, only id is available after boot
		 */
		VirtualMachine virtualMachine = new VirtualMachine();
		virtualMachine.setHostId(vmId);
		virtualMachine.setHostName(hostName);
		virtualMachine.setDescription(description);
		virtualMachine.setCreator(creator);
		virtualMachine.setStatus(VirtualMachineStatusEnum.BUILD);
		virtualMachine.setTaskStatus(VirtualMachineTaskStatusEnum.SPAWNING);
		virtualMachine.setCpu(cpu);
		virtualMachine.setMemory(memory);
		virtualMachine.setDisk(disk);
		virtualMachine.setImageName(imageName);
		virtualMachine.setDomainId(domainId);
		virtualMachine.setGroupId(groupId);
		/*
		 * application should not call merge(), virtual machine entity will take responsibility to save vm<-->app
		 * relationship
		 */
		String[] appIdList = applicationIds.split(",");
		for (String appId : appIdList) {
			appId = appId.trim();
			if (appId.isEmpty() == false) {
				ApplicationTag applicationTag = ApplicationTag.getApplicationTag(appId);
				applicationTag.getVirtualMachines().add(virtualMachine);

				virtualMachine.getApplicationTags().add(applicationTag);
			}
		}
		if (manager.equalsIgnoreCase("none") == false) {
			virtualMachine.setManager(manager);
		}
		virtualMachine.setCreateTime(new Date());
		virtualMachine.persist();
		virtualMachine.flush();
		virtualMachine.clear();

		/*
		 * send message to message queue
		 */
		AsyncOperationRequest request = new AsyncOperationRequest();
		request.setOperationType(AsyncOperationTypeEnum.CREATE_VIRTUAL_MACHINE);
		request.setOperator(creator);
		request.setVirtualMachineId(vmId);
		request.setRequestTime(new Date());
		ObjectMapper mapper = new ObjectMapper();
		try {
			String requestBody = mapper.writeValueAsString(request);
			messageProducer.publish(requestBody);
		} catch (Exception e) {
			/*
			 * save operation log
			 */
			operationTime = new Date();
			operation = "发送创建虚拟机后处理请求";
			operationStatus = OperationStatusEnum.FAILED;
			operationResult = "创建虚拟机时，发送后处理请求发生错误，请联系运维人员进行处理，虚拟机ID：" + vmId;
			operationSeverity = OperationSeverityEnum.HIGH;
			operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
					operationResult, operationSeverity);

			String action = operation;
			String message = operationResult;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		String action = "创建虚拟机";
		String message = "";
		return actionResultHelper.createActionResult(true, action, message, false, logger,
				ActionResultLogLevelEnum.NONE);
	}

	public BatchActionResult startVirtualMachines(String username, String vmIds) {
		BatchActionResult result = new BatchActionResult();
		boolean success = false;
		String message = "";
		List<String> finishedList = new ArrayList<String>();
		List<String> failedList = new ArrayList<String>();

		String operator = username;
		Date operationTime;
		String operation;
		OperationStatusEnum operationStatus;
		ServiceNameEnum serviceName = ServiceNameEnum.VIRTURAL_MACHINE_MANAGEMENT;
		String objectId;
		String operationResult;
		OperationSeverityEnum operationSeverity;

		String[] ids = vmIds.split(",");
		for (String vmId : ids) {
			VirtualMachine machine = VirtualMachine.getVirtualMachine(vmId);
			if (null == machine) {
				message = "启动虚拟机失败，虚拟机不存在，虚拟机ID：" + vmId;
				message += "；";
				failedList.add(vmId);

				continue;
			}

			try {
				CloudManipulator cloud = CloudManipulatorFactory.createCloudManipulator(cloudConfig,
						machine.getDomainId());
				ActionResult startResult = startVirtualMachine(cloud, username, vmId);
				if (true == startResult.isSuccess()) {
					finishedList.add(vmId);
				} else {
					/*
					 * save operation log
					 */
					operationTime = new Date();
					operation = startResult.getAction();
					operationStatus = OperationStatusEnum.FAILED;
					objectId = vmId;
					operationResult = startResult.getMessage();
					operationSeverity = OperationSeverityEnum.HIGH;
					operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName,
							objectId, operationResult, operationSeverity);

					message += operationResult;
					message += "；";
					failedList.add(vmId);
				}
			} catch (Exception e) {
				/*
				 * save operation log
				 */
				operationTime = new Date();
				operation = "启动虚拟机";
				operationStatus = OperationStatusEnum.FAILED;
				objectId = vmId;
				operationResult = "启动虚拟机失败，启动虚拟机过程中发生错误，虚拟机ID：" + vmId + "，虚拟机名称：" + machine.getHostName();
				operationSeverity = OperationSeverityEnum.HIGH;
				operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
						operationResult, operationSeverity);

				// suppress exception so we can continue with next deletion
				message += operationResult;
				message += "；";
				failedList.add(vmId);

				logger.error(operationResult, e);
			}
		}

		if (failedList.size() > 0) {
			success = false;
		} else {
			success = true;
		}
		result.setSuccess(success);
		result.setMessage(message);
		result.setFinishedList(finishedList);
		result.setFailedList(failedList);

		if (false == success) {
			logger.error(message);
		}
		return result;
	}

	private ActionResult startVirtualMachine(CloudManipulator cloud, String username, String vmId) {
		if (false == messageProducer.isOpen()) {
			String action = "检查消息连接状态";
			String message = "启动虚拟机失败，消息连接已关闭，请联系运维人员进行处理";
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		VirtualMachine machine = VirtualMachine.getVirtualMachine(vmId);
		if (null == machine) {
			String action = "检查虚拟机";
			String message = "启动虚拟机失败，虚拟机不存在，虚拟机ID：" + vmId;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		/*
		 * start virtual machine
		 */
		boolean started = cloud.startServer(vmId);
		if (false == started) {
			String action = "发送启动虚拟机请求";
			String message = "启动虚拟机失败，发送启动虚拟机请求失败，虚拟机ID：" + vmId + "，虚拟机名称：" + machine.getHostName();
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		/*
		 * save operation log
		 */
		String operator = username;
		Date operationTime = new Date();
		String operation = "发送启动虚拟机请求";
		OperationStatusEnum operationStatus = OperationStatusEnum.OK;
		ServiceNameEnum serviceName = ServiceNameEnum.VIRTURAL_MACHINE_MANAGEMENT;
		String objectId = vmId;
		String operationResult = "发送启动虚拟机请求成功，虚拟机ID：" + vmId + "，虚拟机名称：" + machine.getHostName();
		OperationSeverityEnum operationSeverity = OperationSeverityEnum.LOW;
		operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
				operationResult, operationSeverity);

		/*
		 * save virtual machine status
		 */
		machine.setStatus(VirtualMachineStatusEnum.ACTIVE);
		machine.setTaskStatus(VirtualMachineTaskStatusEnum.POWERING_ON);
		boolean merged;
		try {
			merged = machine.lastCommitWinsMerge();
		} catch (Exception e) {
			merged = false;
		}
		if (false == merged) {
			/*
			 * the caller is responsible to save operation log.
			 */
			String action = "保存虚拟机状态";
			String message = "启动虚拟机时，保存虚拟机状态发生错误，请联系运维人员进行处理，虚拟机ID：" + vmId + "，虚拟机名称：" + machine.getHostName();
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}
		machine.flush();
		machine.clear();

		/*
		 * send message to message queue
		 */
		AsyncOperationRequest request = new AsyncOperationRequest();
		request.setOperationType(AsyncOperationTypeEnum.START_VIRTUAL_MACHINE);
		request.setOperator(username);
		request.setVirtualMachineId(vmId);
		request.setRequestTime(new Date());
		ObjectMapper mapper = new ObjectMapper();
		try {
			String requestBody = mapper.writeValueAsString(request);
			messageProducer.publish(requestBody);
		} catch (Exception e) {
			/*
			 * the caller is responsible to save operation log.
			 */
			String action = "发送启动虚拟机后处理请求";
			String message = "启动虚拟机时，发送后处理请求发生错误，请联系运维人员进行处理，虚拟机ID：" + vmId + "，虚拟机名称：" + machine.getHostName();
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		String action = "启动虚拟机";
		String message = "";
		return actionResultHelper.createActionResult(true, action, message, false, logger,
				ActionResultLogLevelEnum.NONE);
	}

	public BatchActionResult rebootVirtualMachines(String username, String vmIds) {
		BatchActionResult result = new BatchActionResult();
		boolean success = false;
		String message = "";
		List<String> finishedList = new ArrayList<String>();
		List<String> failedList = new ArrayList<String>();

		String operator = username;
		Date operationTime;
		String operation;
		OperationStatusEnum operationStatus;
		ServiceNameEnum serviceName = ServiceNameEnum.VIRTURAL_MACHINE_MANAGEMENT;
		String objectId;
		String operationResult;
		OperationSeverityEnum operationSeverity;

		String[] ids = vmIds.split(",");
		for (String vmId : ids) {
			VirtualMachine machine = VirtualMachine.getVirtualMachine(vmId);
			if (null == machine) {
				message = "重启虚拟机失败，虚拟机不存在，虚拟机ID：" + vmId;
				message += "；";
				continue;
			}

			try {
				CloudManipulator cloud = CloudManipulatorFactory.createCloudManipulator(cloudConfig,
						machine.getDomainId());
				ActionResult rebootResult = rebootVirtualMachine(cloud, username, vmId);
				if (true == rebootResult.isSuccess()) {
					finishedList.add(vmId);
				} else {
					/*
					 * save operation log
					 */
					operationTime = new Date();
					operation = rebootResult.getAction();
					operationStatus = OperationStatusEnum.FAILED;
					objectId = vmId;
					operationResult = rebootResult.getMessage();
					operationSeverity = OperationSeverityEnum.HIGH;
					operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName,
							objectId, operationResult, operationSeverity);

					message += operationResult;
					message += "；";
					failedList.add(vmId);
				}
			} catch (Exception e) {
				/*
				 * save operation log
				 */
				operationTime = new Date();
				operation = "重启虚拟机";
				operationStatus = OperationStatusEnum.FAILED;
				objectId = vmId;
				operationResult = "重启虚拟机失败，重启虚拟机过程中发生错误，虚拟机ID：" + vmId + "，虚拟机名称：" + machine.getHostName();
				operationSeverity = OperationSeverityEnum.HIGH;
				operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
						operationResult, operationSeverity);

				// suppress exception so we can continue with next deletion
				message += operationResult;
				message += "；";
				failedList.add(vmId);

				logger.error(operationResult, e);
			}
		}

		if (failedList.size() > 0) {
			success = false;
		} else {
			success = true;
		}
		result.setSuccess(success);
		result.setMessage(message);
		result.setFinishedList(finishedList);
		result.setFailedList(failedList);

		if (false == success) {
			logger.error(message);
		}
		return result;
	}

	private ActionResult rebootVirtualMachine(CloudManipulator cloud, String username, String vmId) {
		if (false == messageProducer.isOpen()) {
			String action = "检查消息连接状态";
			String message = "重启虚拟机失败，消息连接已关闭，请联系运维人员进行处理";
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		VirtualMachine machine = VirtualMachine.getVirtualMachine(vmId);
		if (null == machine) {
			String action = "检查虚拟机";
			String message = "重启虚拟机失败，虚拟机不存在，虚拟机ID：" + vmId;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		boolean started = cloud.rebootServer(vmId);
		if (false == started) {
			String action = "发送重启虚拟机请求";
			String message = "重启虚拟机失败，发送重启虚拟机请求失败，虚拟机ID：" + vmId + "，虚拟机名称：" + machine.getHostName();
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		/*
		 * save operation log
		 */
		String operator = username;
		Date operationTime = new Date();
		String operation = "发送重启虚拟机请求";
		OperationStatusEnum operationStatus = OperationStatusEnum.OK;
		ServiceNameEnum serviceName = ServiceNameEnum.VIRTURAL_MACHINE_MANAGEMENT;
		String objectId = vmId;
		String operationResult = "发送重启虚拟机请求成功，虚拟机ID：" + vmId + "，虚拟机名称：" + machine.getHostName();
		OperationSeverityEnum operationSeverity = OperationSeverityEnum.HIGH;
		operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
				operationResult, operationSeverity);

		/*
		 * save virtual machine status
		 */
		machine.setStatus(VirtualMachineStatusEnum.ACTIVE);
		machine.setTaskStatus(VirtualMachineTaskStatusEnum.REBOOTING);
		boolean merged;
		try {
			merged = machine.lastCommitWinsMerge();
		} catch (Exception e) {
			merged = false;
		}
		if (false == merged) {
			/*
			 * the caller is responsible to save operation log.
			 */
			String action = "保存虚拟机状态";
			String message = "重启虚拟机时，保存虚拟机状态发生错误，请联系运维人员进行处理，虚拟机ID：" + vmId + "，虚拟机名称：" + machine.getHostName();
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}
		machine.flush();
		machine.clear();

		/*
		 * send message to message queue
		 */
		AsyncOperationRequest request = new AsyncOperationRequest();
		request.setOperationType(AsyncOperationTypeEnum.REBOOT_VIRTUAL_MACHINE);
		request.setOperator(username);
		request.setVirtualMachineId(vmId);
		request.setRequestTime(new Date());
		ObjectMapper mapper = new ObjectMapper();
		try {
			String requestBody = mapper.writeValueAsString(request);
			messageProducer.publish(requestBody);
		} catch (Exception e) {
			/*
			 * the caller is responsible to save operation log.
			 */
			String action = "发送重启虚拟机后处理请求";
			String message = "重启虚拟机时，发送后处理请求发生错误，请联系运维人员进行处理，虚拟机ID：" + vmId + "，虚拟机名称：" + machine.getHostName();
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		String action = "重启虚拟机";
		String message = "";
		return actionResultHelper.createActionResult(true, action, message, false, logger,
				ActionResultLogLevelEnum.NONE);
	}

	public BatchActionResult shutdownVirtualMachines(String username, String vmIds) {
		BatchActionResult result = new BatchActionResult();
		boolean success = true;
		String message = "";
		List<String> finishedList = new ArrayList<String>();
		List<String> failedList = new ArrayList<String>();

		String operator = username;
		Date operationTime;
		String operation;
		OperationStatusEnum operationStatus;
		ServiceNameEnum serviceName = ServiceNameEnum.VIRTURAL_MACHINE_MANAGEMENT;
		String objectId;
		String operationResult;
		OperationSeverityEnum operationSeverity;

		String[] ids = vmIds.split(",");
		for (String vmId : ids) {
			VirtualMachine machine = VirtualMachine.getVirtualMachine(vmId);
			if (null == machine) {
				message = "关闭虚拟机失败，虚拟机不存在，虚拟机ID：" + vmId;
				message += "；";
				failedList.add(vmId);

				continue;
			}

			try {
				CloudManipulator cloud = CloudManipulatorFactory.createCloudManipulator(cloudConfig,
						machine.getDomainId());
				ActionResult shutdownResult = shutdownVirtualMachine(cloud, username, vmId);
				if (true == shutdownResult.isSuccess()) {
					finishedList.add(vmId);
				} else {
					/*
					 * save operation log
					 */
					operationTime = new Date();
					operation = shutdownResult.getAction();
					operationStatus = OperationStatusEnum.FAILED;
					objectId = vmId;
					operationResult = shutdownResult.getMessage();
					operationSeverity = OperationSeverityEnum.HIGH;
					operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName,
							objectId, operationResult, operationSeverity);

					message += operationResult;
					message += "；";
					failedList.add(vmId);
				}
			} catch (Exception e) {
				/*
				 * save operation log
				 */
				operationTime = new Date();
				operation = "关闭虚拟机";
				operationStatus = OperationStatusEnum.FAILED;
				objectId = vmId;
				operationResult = "关闭虚拟机失败，关闭虚拟机过程中发生错误，虚拟机ID：" + vmId + "，虚拟机名称：" + machine.getHostName();
				operationSeverity = OperationSeverityEnum.HIGH;
				operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
						operationResult, operationSeverity);

				// suppress exception so we can continue with next deletion
				message += operationResult;
				message += "；";
				failedList.add(vmId);

				logger.error(operationResult, e);
			}
		}

		if (failedList.size() > 0) {
			success = false;
		} else {
			success = true;
		}
		result.setSuccess(success);
		result.setMessage(message);
		result.setFinishedList(finishedList);
		result.setFailedList(failedList);

		if (false == success) {
			logger.error(message);
		}
		return result;
	}

	private ActionResult shutdownVirtualMachine(CloudManipulator cloud, String username, String vmId) {
		if (false == messageProducer.isOpen()) {
			String action = "检查消息连接状态";
			String message = "关闭虚拟机失败，消息连接已关闭，请联系运维人员进行处理";
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		VirtualMachine machine = VirtualMachine.getVirtualMachine(vmId);
		if (null == machine) {
			String action = "检查虚拟机";
			String message = "关闭虚拟机失败，虚拟机不存在，虚拟机ID：" + vmId;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		boolean started = cloud.stopServer(vmId);
		if (false == started) {
			String action = "发送关闭虚拟机请求";
			String message = "关闭虚拟机失败，发送关闭虚拟机请求失败，虚拟机ID：" + vmId + "，虚拟机名称：" + machine.getHostName();
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		/*
		 * save operation log
		 */
		String operator = username;
		Date operationTime = new Date();
		String operation = "发送关闭虚拟机请求";
		OperationStatusEnum operationStatus = OperationStatusEnum.OK;
		ServiceNameEnum serviceName = ServiceNameEnum.VIRTURAL_MACHINE_MANAGEMENT;
		String objectId = vmId;
		String operationResult = "发送关闭虚拟机请求成功，虚拟机ID：" + vmId + "，虚拟机名称：" + machine.getHostName();
		OperationSeverityEnum operationSeverity = OperationSeverityEnum.HIGH;
		operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
				operationResult, operationSeverity);

		/*
		 * save virtual machine status
		 */
		machine.setStatus(VirtualMachineStatusEnum.ACTIVE);
		machine.setTaskStatus(VirtualMachineTaskStatusEnum.POWERING_OFF);
		boolean merged;
		try {
			merged = machine.lastCommitWinsMerge();
		} catch (Exception e) {
			merged = false;
		}
		if (false == merged) {
			/*
			 * the caller is responsible to save operation log.
			 */
			String action = "保存虚拟机状态";
			String message = "关闭虚拟机时，保存虚拟机状态发生错误，请联系运维人员进行处理，虚拟机ID：" + vmId + "，虚拟机名称：" + machine.getHostName();
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}
		machine.flush();
		machine.clear();

		/*
		 * send message to message queue
		 */
		AsyncOperationRequest request = new AsyncOperationRequest();
		request.setOperationType(AsyncOperationTypeEnum.SHUTDOWN_VIRTUAL_MACHINE);
		request.setOperator(username);
		request.setVirtualMachineId(vmId);
		request.setRequestTime(new Date());

		ObjectMapper mapper = new ObjectMapper();
		try {
			String requestBody = mapper.writeValueAsString(request);
			messageProducer.publish(requestBody);
		} catch (Exception e) {
			/*
			 * the caller is responsible to save operation log.
			 */
			String action = "发送关闭虚拟机后处理请求";
			String message = "关闭虚拟机时，发送后处理请求发生错误，请联系运维人员进行处理，虚拟机ID：" + vmId + "，虚拟机名称：" + machine.getHostName();
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		String action = "关闭虚拟机";
		String message = "";
		return actionResultHelper.createActionResult(true, action, message, false, logger,
				ActionResultLogLevelEnum.NONE);
	}

	public BatchActionResult deleteVirtualMachines(String username, String vmIds) {
		BatchActionResult result = new BatchActionResult();
		boolean success = true;
		String message = "";
		List<String> finishedList = new ArrayList<String>();
		List<String> failedList = new ArrayList<String>();

		String operator = username;
		Date operationTime;
		String operation;
		OperationStatusEnum operationStatus;
		ServiceNameEnum serviceName = ServiceNameEnum.VIRTURAL_MACHINE_MANAGEMENT;
		String objectId;
		String operationResult;
		OperationSeverityEnum operationSeverity;

		String[] ids = vmIds.split(",");
		for (String vmId : ids) {
			VirtualMachine machine = VirtualMachine.getVirtualMachine(vmId);
			if (null == machine) {
				message += "删除虚拟机失败，虚拟机不存在，虚拟机ID：" + vmId;
				message += "；";
				failedList.add(vmId);

				continue;
			}

			Set<Disk> disks = machine.getDisks();
			if (disks.size() > 0) {
				message += "删除虚拟机失败，虚拟机挂载有云硬盘，虚拟机ID：" + vmId + "，虚拟机名称：" + machine.getHostName();
				message += "；";
				failedList.add(vmId);

				continue;
			}

			if ((null != machine.getFloatingIp()) && (false == machine.getFloatingIp().isEmpty())) {
				message += "删除虚拟机失败，虚拟机绑定有访问地址，虚拟机ID：" + vmId + "，虚拟机名称：" + machine.getHostName();
				message += "；";
				failedList.add(vmId);

				continue;
			}

			/*
			 * check machine status, only active/stopped/error machine is allowed to delete.
			 */
			boolean allowDelete = ((VirtualMachineTaskStatusEnum.NONE == machine.getTaskStatus()) && (VirtualMachineStatusEnum.ACTIVE == machine
					.getStatus() || (VirtualMachineStatusEnum.ERROR == machine.getStatus()) || (VirtualMachineStatusEnum.STOPPED == machine
					.getStatus())));
			if (false == allowDelete) {
				message += "删除虚拟机失败，只允许删除运行中、已关机和错误状态的虚拟机，虚拟机ID：" + vmId + "，虚拟机状态：" + machine.getStatus().name();
				message += "；";
				failedList.add(vmId);

				continue;
			}
			
			try {
				CloudManipulator cloud = CloudManipulatorFactory.createCloudManipulator(cloudConfig,
						machine.getDomainId());

				ActionResult deleteResult = deleteVirtualMachine(cloud, username, vmId);
				if (true == deleteResult.isSuccess()) {
					finishedList.add(vmId);
				} else {
					/*
					 * save operation log
					 */
					operationTime = new Date();
					operation = deleteResult.getAction();
					operationStatus = OperationStatusEnum.FAILED;
					objectId = vmId;
					operationResult = deleteResult.getMessage();
					operationSeverity = OperationSeverityEnum.HIGH;
					operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName,
							objectId, operationResult, operationSeverity);

					message += operationResult;
					message += "；";
					failedList.add(vmId);
				}
			} catch (Exception e) {
				/*
				 * save operation log
				 */
				operationTime = new Date();
				operation = "删除虚拟机";
				operationStatus = OperationStatusEnum.FAILED;
				objectId = vmId;
				operationResult = "删除虚拟机失败，删除虚拟机过程中发生错误，虚拟机ID：" + vmId + "，虚拟机名称：" + machine.getHostName();
				operationSeverity = OperationSeverityEnum.HIGH;
				operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
						operationResult, operationSeverity);

				// suppress exception so we can continue with next one
				message += operationResult;
				message += "；";
				failedList.add(vmId);

				logger.error(operationResult, e);
			}
		}

		if (failedList.size() > 0) {
			success = false;
		} else {
			success = true;
		}
		result.setSuccess(success);
		result.setMessage(message);
		result.setFinishedList(finishedList);
		result.setFailedList(failedList);

		if (false == success) {
			logger.error(message);
		}
		return result;
	}

	private ActionResult deleteVirtualMachine(CloudManipulator cloud, String username, String vmId) {
		if (false == messageProducer.isOpen()) {
			String action = "检查消息连接状态";
			String message = "删除虚拟机失败，消息连接已关闭，请联系运维人员进行处理";
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		VirtualMachine machine = VirtualMachine.getVirtualMachine(vmId);
		if (null == machine) {
			String action = "检查虚拟机";
			String message = "删除虚拟机失败，虚拟机不存在，虚拟机ID：" + vmId;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		boolean deleted = cloud.deleteServer(vmId);
		if (false == deleted) {
			String action = "发送删除虚拟机请求";
			String message = "删除虚拟机失败，发送删除虚拟机请求失败，虚拟机ID：" + vmId + "，虚拟机名称：" + machine.getHostName();
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		/*
		 * save operation log
		 */
		String operator = username;
		Date operationTime = new Date();
		String operation = "发送删除虚拟机请求";
		OperationStatusEnum operationStatus = OperationStatusEnum.OK;
		ServiceNameEnum serviceName = ServiceNameEnum.VIRTURAL_MACHINE_MANAGEMENT;
		String objectId = vmId;
		String operationResult = "发送删除虚拟机请求成功，虚拟机ID：" + vmId + "，虚拟机名称：" + machine.getHostName();
		OperationSeverityEnum operationSeverity = OperationSeverityEnum.HIGH;
		operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
				operationResult, operationSeverity);

		/*
		 * save virtual machine status
		 */
		machine.setStatus(VirtualMachineStatusEnum.ACTIVE);
		machine.setTaskStatus(VirtualMachineTaskStatusEnum.DELETING);
		boolean merged;
		try {
			merged = machine.lastCommitWinsMerge();
		} catch (Exception e) {
			merged = false;
		}
		if (false == merged) {
			/*
			 * the caller is responsible to save operation log.
			 */
			String action = "保存虚拟机状态";
			String message = "删除虚拟机时，保存虚拟机状态发生错误，请联系运维人员进行处理，虚拟机ID：" + vmId + "，虚拟机名称：" + machine.getHostName();
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}
		machine.flush();
		machine.clear();

		/*
		 * send message to message queue
		 */
		AsyncOperationRequest request = new AsyncOperationRequest();
		request.setOperationType(AsyncOperationTypeEnum.DELETE_VIRTUAL_MACHINE);
		request.setOperator(username);
		request.setVirtualMachineId(vmId);
		request.setRequestTime(new Date());
		ObjectMapper mapper = new ObjectMapper();
		try {
			String requestBody = mapper.writeValueAsString(request);
			messageProducer.publish(requestBody);
		} catch (Exception e) {
			/*
			 * the caller is responsible to save operation log.
			 */
			String action = "发送删除虚拟机后处理请求";
			String message = "删除虚拟机时，发送后处理请求发生错误，请联系运维人员进行处理，虚拟机ID：" + vmId + "，虚拟机名称：" + machine.getHostName();
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		String action = "删除虚拟机";
		String message = "";
		return actionResultHelper.createActionResult(true, action, message, false, logger,
				ActionResultLogLevelEnum.NONE);
	}

	public Map<String, ServerSamples> getMetric(String vmId) {
		VirtualMachine machine = VirtualMachine.getVirtualMachine(vmId);
		if (null == machine) {
			return null;
		}

		Map<String, ServerSamples> map = new HashMap<String, ServerSamples>();
		CloudManipulator cloud = CloudManipulatorFactory.createCloudManipulator(cloudConfig, machine.getDomainId());
		List<String> meters = new ArrayList<String>(Arrays.asList("cpu_util", "memory.usage", "disk.read.bytes.rate",
				"disk.write.bytes.rate", "network.outgoing.bytes.rate", "network.incoming.bytes.rate"));

		int metriCount = 60;
		/*
		 * the configured sampling interval in milliseconds.
		 */
		long sampleInterval = 10 * 60 * 1000;
		long timestamp = System.currentTimeMillis() - (metriCount * sampleInterval);
		for (String meterName : meters) {
			ServerSamples serverSamples = cloud.getSamples(vmId, meterName, timestamp);
			map.put(meterName.replace('.', '_'), serverSamples);
		}

		return map;
	}

	public String getVncConsoleUrl(String vmId) {
		VirtualMachine machine = VirtualMachine.getVirtualMachine(vmId);
		if (null == machine) {
			return null;
		}

		CloudManipulator cloud = CloudManipulatorFactory.createCloudManipulator(cloudConfig, machine.getDomainId());
		VNCConsole console = cloud.getServerVNCConsole(vmId);
		if (null == console) {
			return null;
		}
		return console.getURL();
	}

	public ActionResult renameVirtualMachine(String username, String vmId, String newName) {
		VirtualMachine machine = VirtualMachine.getVirtualMachine(vmId);
		if (null == machine) {
			String action = "检查虚拟机";
			String message = "重命名虚拟机失败，虚拟机不存在，虚拟机ID：" + vmId;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		/*
		 * rename virtual machine
		 */
		CloudManipulator cloud = CloudManipulatorFactory.createCloudManipulator(cloudConfig, machine.getDomainId());
		cloud.renameServer(vmId, newName);

		/*
		 * save operation log
		 */
		String operator = username;
		Date operationTime = new Date();
		String operation = "重命名虚拟机";
		OperationStatusEnum operationStatus = OperationStatusEnum.OK;
		ServiceNameEnum serviceName = ServiceNameEnum.VIRTURAL_MACHINE_MANAGEMENT;
		String objectId = vmId;
		String operationResult = "重命名虚拟机成功，虚拟机ID：" + vmId;
		OperationSeverityEnum operationSeverity = OperationSeverityEnum.LOW;
		operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
				operationResult, operationSeverity);

		/*
		 * save machine data
		 */
		machine = VirtualMachine.getVirtualMachine(vmId);
		String oldName = machine.getHostName();
		machine.setHostName(newName);
		boolean merged;
		try {
			merged = machine.lastCommitWinsMerge();
		} catch (Exception e) {
			merged = false;
		}
		if (false == merged) {
			/*
			 * save operation log
			 */
			operationTime = new Date();
			operation = "保存虚拟机名称";
			operationStatus = OperationStatusEnum.FAILED;
			operationResult = "重命名虚拟机时，保存虚拟机名称发生错误，请联系运维人员进行处理，虚拟机ID：" + vmId;
			operationSeverity = OperationSeverityEnum.HIGH;
			operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
					operationResult, operationSeverity);

			String action = operation;
			String message = operationResult;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);

		}
		machine.flush();
		machine.clear();

		/*
		 * save operation log
		 */
		operationTime = new Date();
		operation = "保存虚拟机名称";
		operationStatus = OperationStatusEnum.OK;
		operationResult = "重命名虚拟机时，保存虚拟机名称成功，虚拟机ID：" + vmId + "，原名称：" + oldName + "，新名称：" + newName;
		operationSeverity = OperationSeverityEnum.LOW;
		operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
				operationResult, operationSeverity);

		String action = "重命名虚拟机";
		String message = "";
		return actionResultHelper.createActionResult(true, action, message, false, logger,
				ActionResultLogLevelEnum.NONE);
	}

	public ActionResult setVirtualMachineManager(String username, String vmId, String manager) {
		VirtualMachine machine = VirtualMachine.getVirtualMachine(vmId);
		if (null == machine) {
			String action = "检查虚拟机";
			String message = "设置虚拟机管理员失败，虚拟机不存在，虚拟机ID：" + vmId;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		/*
		 * save machine data
		 */
		if (manager.equalsIgnoreCase("none") == true) {
			machine.setManager(null);
		} else {
			User user = User.getUserByUsername(manager);
			if (user == null) {
				String action = "检查用户";
				String message = "设置虚拟机管理员失败，用户不存在，虚拟机ID：" + vmId + "管理员：" + manager;
				return actionResultHelper.createActionResult(false, action, message, false, logger,
						ActionResultLogLevelEnum.ERROR);
			}

			machine.setManager(manager);
		}
		machine.merge();
		machine.flush();
		machine.clear();

		/*
		 * save operation log
		 */
		String operator = username;
		Date operationTime = new Date();
		String operation = "设置虚拟机管理员";
		OperationStatusEnum operationStatus = OperationStatusEnum.OK;
		ServiceNameEnum serviceName = ServiceNameEnum.VIRTURAL_MACHINE_MANAGEMENT;
		String objectId = vmId;
		String operationResult = "设置虚拟机管理员成功，虚拟机ID：" + vmId + "，管理员：" + manager;
		OperationSeverityEnum operationSeverity = OperationSeverityEnum.MIDDLE;
		operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
				operationResult, operationSeverity);

		String action = "设置虚拟机管理员";
		String message = "";
		return actionResultHelper.createActionResult(true, action, message, false, logger,
				ActionResultLogLevelEnum.NONE);
	}

	public ActionResult setApplicationTags(String username, String vmId, String applicationTagIds) {
		// TODO two merges here, need @Transactional or not?

		VirtualMachine machine = VirtualMachine.getVirtualMachine(vmId);
		if (null == machine) {
			String action = "检查虚拟机";
			String message = "设置虚拟机业务系统标签失败，虚拟机不存在，虚拟机ID：" + vmId;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		/*
		 * clear current application tags
		 */
		machine.getApplicationTags().clear();
		machine.merge();
		machine.flush();
		machine.clear();

		/*
		 * save operation log
		 */
		String operator = username;
		Date operationTime = new Date();
		String operation = "清除虚拟机业务系统标签";
		OperationStatusEnum operationStatus = OperationStatusEnum.OK;
		ServiceNameEnum serviceName = ServiceNameEnum.VIRTURAL_MACHINE_MANAGEMENT;
		String objectId = vmId;
		String operationResult = "清除虚拟机业务系统标签成功，虚拟机ID：" + vmId + "，虚拟机名称：" + machine.getHostName();
		OperationSeverityEnum operationSeverity = OperationSeverityEnum.MIDDLE;
		operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
				operationResult, operationSeverity);

		/*
		 * set new application tags
		 */
		boolean success = true;
		String message = "";
		machine = VirtualMachine.getVirtualMachine(vmId);
		String[] tagIdList = applicationTagIds.split(",");
		for (String tagId : tagIdList) {
			tagId = tagId.trim();
			if (true == tagId.isEmpty()) {
				continue;
			}

			ApplicationTag applicationTag = ApplicationTag.getApplicationTag(tagId);
			if (null == applicationTag) {
				message += "设置虚拟机业务系统标签失败，标签不存在，虚拟机ID：" + vmId + "，标签ID：" + tagId;
				message += "；";
				success = false;

				continue;
			}

			applicationTag.getVirtualMachines().add(machine);
			machine.getApplicationTags().add(applicationTag);
		}
		machine.merge();
		machine.flush();
		machine.clear();

		if (true == success) {
			/*
			 * save operation log
			 */
			operationTime = new Date();
			operation = "添加虚拟机业务系统标签";
			operationStatus = OperationStatusEnum.OK;
			operationResult = "添加虚拟机业务系统标签成功，虚拟机ID：" + vmId + "，标签ID：" + applicationTagIds;
			operationSeverity = OperationSeverityEnum.MIDDLE;
			operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
					operationResult, operationSeverity);

			String action = "设置虚拟机业务系统标签";
			return actionResultHelper.createActionResult(success, action, operationResult, false, logger,
					ActionResultLogLevelEnum.NONE);
		} else {
			/*
			 * save operation log
			 */
			operationTime = new Date();
			operation = "添加虚拟机业务系统标签";
			operationStatus = OperationStatusEnum.OK;
			operationResult = "添加虚拟机业务系统标签失败，虚拟机ID：" + vmId + "，标签ID：" + applicationTagIds + "，错误信息：" + message;
			operationSeverity = OperationSeverityEnum.HIGH;
			operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
					operationResult, operationSeverity);

			String action = "设置虚拟机业务系统标签";
			return actionResultHelper.createActionResult(success, action, operationResult, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}
	}

	public BatchActionResult setLoadMonitorSettings(String username, String vmId, List<AlarmParameter> alarmParameters) {
		BatchActionResult result = new BatchActionResult();
		boolean success = true;
		String message = "";
		List<String> finishedList = new ArrayList<String>();
		List<String> failedList = new ArrayList<String>();

		String operator = username;
		Date operationTime;
		String operation;
		OperationStatusEnum operationStatus;
		ServiceNameEnum serviceName = ServiceNameEnum.VIRTURAL_MACHINE_MANAGEMENT;
		String objectId = vmId;
		String operationResult;
		OperationSeverityEnum operationSeverity;

		for (AlarmParameter parameter : alarmParameters) {
			MonitorSetting monitorSetting = MonitorSetting.getMonitorSetting(vmId,
					MonitorNameEnum.valueOf(parameter.getAlarmName()));

			if (parameter.getEnabled() == true) {
				if (monitorSetting == null) {
					/*
					 * create new alarm setting if not previously set
					 */
					try {
						ActionResult createResult = virtualMachineServiceEpilogue
								.createAlarm(username, vmId, parameter);
						if (true == createResult.isSuccess()) {
							finishedList.add(vmId);
						} else {
							/*
							 * save operation log
							 */
							operationTime = new Date();
							operation = createResult.getAction();
							operationStatus = OperationStatusEnum.FAILED;
							operationResult = createResult.getMessage();
							operationSeverity = OperationSeverityEnum.HIGH;
							operationLogService.saveLog(operator, operationTime, operation, operationStatus,
									serviceName, objectId, operationResult, operationSeverity);

							message += createResult.getMessage();
							message += "；";
							failedList.add(vmId);
						}
					} catch (Exception e) {
						/*
						 * save operation log
						 */
						operationTime = new Date();
						operation = "添加虚拟机负载监控";
						operationStatus = OperationStatusEnum.FAILED;
						operationResult = "添加虚拟机负载监控失败，添加虚拟机负载监控过程中发生错误，虚拟机ID：" + vmId + "，监控项："
								+ parameter.getAlarmName();
						operationSeverity = OperationSeverityEnum.HIGH;
						operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName,
								objectId, operationResult, operationSeverity);

						// suppress exception so we can continue with next one
						message += operationResult;
						message += "；";
						failedList.add(vmId);

						logger.error(operationResult, e);
					}
				} else if ((monitorSetting.getEnabled() == true)
						&& (parameter.getAlarmThreshold() == monitorSetting.getThreshold())
						&& (parameter.getSeverityLevel() == monitorSetting.getSeverityLevel())) {
					// skip
					finishedList.add(vmId);
					continue;
				} else {
					/*
					 * adjust existing alarm setting. change threshold if previously enabled, or enable if previously
					 * disabled
					 */
					try {
						ActionResult updatedResult = updateAlarm(username, vmId, parameter);
						if (true == updatedResult.isSuccess()) {
							finishedList.add(vmId);
						} else {
							/*
							 * save operation log
							 */
							operationTime = new Date();
							operation = updatedResult.getAction();
							operationStatus = OperationStatusEnum.FAILED;
							operationResult = updatedResult.getMessage();
							operationSeverity = OperationSeverityEnum.HIGH;
							operationLogService.saveLog(operator, operationTime, operation, operationStatus,
									serviceName, objectId, operationResult, operationSeverity);

							message += updatedResult.getMessage();
							message += "；";
							failedList.add(vmId);
						}
					} catch (Exception e) {
						/*
						 * save operation log
						 */
						operationTime = new Date();
						operation = "更新虚拟机负载监控";
						operationStatus = OperationStatusEnum.FAILED;
						operationResult = "更新虚拟机负载监控失败，更新虚拟机负载监控过程中发生错误，虚拟机ID：" + vmId + "，监控项："
								+ parameter.getAlarmName();
						operationSeverity = OperationSeverityEnum.HIGH;
						operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName,
								objectId, operationResult, operationSeverity);

						// suppress exception so we can continue with next one
						message += operationResult;
						message += "；";
						failedList.add(vmId);

						logger.error(operationResult, e);
					}
				}
			} else {
				if ((monitorSetting == null) || (monitorSetting.getEnabled() == false)) {
					// skip
					finishedList.add(vmId);
					continue;
				} else {
					/*
					 * adjust existing alarm setting, disable if previously enabled
					 */
					try {
						ActionResult updatedResult = updateAlarm(username, vmId, parameter);
						if (true == updatedResult.isSuccess()) {
							finishedList.add(vmId);
						} else {
							/*
							 * save operation log
							 */
							operationTime = new Date();
							operation = updatedResult.getAction();
							operationStatus = OperationStatusEnum.FAILED;
							operationResult = updatedResult.getMessage();
							operationSeverity = OperationSeverityEnum.HIGH;
							operationLogService.saveLog(operator, operationTime, operation, operationStatus,
									serviceName, objectId, operationResult, operationSeverity);

							message += updatedResult.getMessage();
							message += "；";
							failedList.add(vmId);
						}
					} catch (Exception e) {
						/*
						 * save operation log
						 */
						operationTime = new Date();
						operation = "更新虚拟机负载监控";
						operationStatus = OperationStatusEnum.FAILED;
						operationResult = "更新虚拟机负载监控失败，更新虚拟机负载监控过程中发生错误，虚拟机ID：" + vmId + "，监控项："
								+ parameter.getAlarmName();
						operationSeverity = OperationSeverityEnum.HIGH;
						operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName,
								objectId, operationResult, operationSeverity);

						// suppress exception so we can continue with next one
						message += operationResult;
						message += "；";
						failedList.add(vmId);

						logger.error(operationResult, e);
					}
				}
			}
		}

		if (failedList.size() > 0) {
			success = false;
		}
		result.setSuccess(success);
		result.setMessage(message);
		result.setFinishedList(finishedList);
		result.setFailedList(failedList);
		return result;
	}

	/**
	 * update alarm.
	 * 
	 * @param vmId
	 *            - virtual machine id
	 * @param newAlarmParameter
	 *            - new alarm parameter
	 * @return action result
	 * @author xiangqian
	 */
	private ActionResult updateAlarm(String username, String vmId, AlarmParameter newAlarmParameter) {
		VirtualMachine machine = VirtualMachine.getVirtualMachine(vmId);
		if (null == machine) {
			String action = "检查虚拟机";
			String message = "更新虚拟机负载监控失败，虚拟机不存在，虚拟机ID：" + vmId + "，监控项：" + newAlarmParameter.getAlarmName();
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		MonitorSetting monitorSetting = MonitorSetting.getMonitorSetting(vmId,
				MonitorNameEnum.valueOf(newAlarmParameter.getAlarmName()));
		if (null == monitorSetting) {
			String action = "检查负载监控设置";
			String message = "更新虚拟机负载监控失败，虚拟机负载监控设置记录不存在，虚拟机ID：" + vmId + "，监控项：" + newAlarmParameter.getAlarmName();
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		if (monitorSetting.getOsAlarmId() == null) {
			String action = "检查负载监控设置";
			String message = "更新虚拟机负载监控失败，虚拟机负载监控设置记录有误，虚拟机ID：" + vmId + "，监控项：" + newAlarmParameter.getAlarmName();
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		CloudManipulator cloud = CloudManipulatorFactory.createCloudManipulator(cloudConfig, machine.getDomainId());
		float threshold = newAlarmParameter.getAlarmThreshold();
		// set memory threshold
		if ((newAlarmParameter.getEnabled() == true)
				&& (true == newAlarmParameter.getAlarmName().equalsIgnoreCase("VM_MEMORY_USAGE"))) {
			threshold = newAlarmParameter.getAlarmThreshold() / 100 * machine.getMemory() * 1024; // MB
		}
		boolean updated = cloud.updateAlarm(monitorSetting.getOsAlarmId(), newAlarmParameter.getEnabled(), threshold);
		if (false == updated) {
			String action = "更新虚拟机负载监控";
			String message = "更新虚拟机负载监控失败，虚拟机ID：" + vmId + "，监控项：" + newAlarmParameter.getAlarmName();
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		/*
		 * save operation log
		 */
		String operator = username;
		Date operationTime = new Date();
		String operation = "更新虚拟机负载监控";
		OperationStatusEnum operationStatus = OperationStatusEnum.OK;
		ServiceNameEnum serviceName = ServiceNameEnum.VIRTURAL_MACHINE_MANAGEMENT;
		String objectId = vmId;
		String operationResult = "更新虚拟机负载监控成功，虚拟机ID：" + vmId + "，监控项：" + newAlarmParameter.getAlarmName();
		OperationSeverityEnum operationSeverity = OperationSeverityEnum.LOW;
		operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
				operationResult, operationSeverity);

		/*
		 * save setting data
		 */
		monitorSetting.setEnabled(newAlarmParameter.getEnabled());
		monitorSetting.setThreshold(newAlarmParameter.getAlarmThreshold());
		monitorSetting.setSeverityLevel(newAlarmParameter.getSeverityLevel());
		monitorSetting.merge();
		monitorSetting.flush();
		monitorSetting.clear();

		String action = "更新虚拟机负载监控";
		String message = "";
		return actionResultHelper.createActionResult(true, action, message, false, logger,
				ActionResultLogLevelEnum.NONE);
	}

	public ActionResult attachDisk(String username, String vmId, String diskId) {
		if (false == messageProducer.isOpen()) {
			String action = "检查消息连接状态";
			String message = "挂载云硬盘失败，消息连接已关闭，请联系运维人员进行处理";
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		VirtualMachine machine = VirtualMachine.getVirtualMachine(vmId);
		if (null == machine) {
			String action = "检查虚拟机";
			String message = "挂载云硬盘失败，虚拟机不存在，虚拟机ID：" + vmId;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		Disk disk = Disk.getDisk(diskId);
		if (null == disk) {
			String action = "检查云硬盘";
			String message = "挂载云硬盘失败，云硬盘不存在，云硬盘ID：" + diskId;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		CloudManipulator cloud = CloudManipulatorFactory.createCloudManipulator(cloudConfig, machine.getDomainId());
		boolean attachResult = cloud.attachVolume(vmId, diskId);
		if (false == attachResult) {
			String action = "发送挂载云硬盘请求";
			String message = "挂载云硬盘失败，发送挂载云硬盘请求失败，虚拟机ID：" + vmId + "，云硬盘ID：" + diskId;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		/*
		 * save operation log
		 */
		String operator = username;
		Date operationTime = new Date();
		String operation = "发送挂载云硬盘请求";
		OperationStatusEnum operationStatus = OperationStatusEnum.OK;
		ServiceNameEnum serviceName = ServiceNameEnum.VIRTURAL_MACHINE_MANAGEMENT;
		String objectId = vmId;
		String operationResult = "发送挂载云硬盘请求成功，虚拟机ID：" + vmId + "，云硬盘ID：" + diskId;
		OperationSeverityEnum operationSeverity = OperationSeverityEnum.LOW;
		operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
				operationResult, operationSeverity);

		/*
		 * set disk status and attach time, and build mapping between virtual machine and disk.
		 */
		disk = Disk.getDisk(diskId);
		disk.setStatus(DiskStatusEnum.ATTACHING);
		disk.setAttachTime(new Date());
		machine = VirtualMachine.getVirtualMachine(vmId);
		disk.setVirtualMachine(machine);
		machine.getDisks().add(disk);
		boolean merged;
		try {
			merged = machine.lastCommitWinsMerge();
		} catch (Exception e) {
			merged = false;
		}
		if (false == merged) {
			/*
			 * save operation log
			 */
			operationTime = new Date();
			operation = "保存虚拟机和云硬盘数据";
			operationStatus = OperationStatusEnum.FAILED;
			operationResult = "挂载云硬盘时，保存虚拟机和云硬盘数据发生错误，请联系运维人员进行处理，虚拟机ID：" + vmId + "，云硬盘ID：" + diskId;
			operationSeverity = OperationSeverityEnum.HIGH;
			operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
					operationResult, operationSeverity);

			String action = operation;
			String message = operationResult;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);

		}
		machine.flush();
		machine.clear();

		/*
		 * send message to message queue
		 */
		AsyncOperationRequest request = new AsyncOperationRequest();
		request.setOperationType(AsyncOperationTypeEnum.ATTACH_DISK);
		request.setOperator(username);
		request.setVirtualMachineId(vmId);
		request.setDiskId(diskId);
		request.setRequestTime(new Date());
		ObjectMapper mapper = new ObjectMapper();
		try {
			String requestBody = mapper.writeValueAsString(request);
			messageProducer.publish(requestBody);
		} catch (Exception e) {
			/*
			 * save operation log
			 */
			operationTime = new Date();
			operation = "发送挂载云硬盘后处理请求";
			operationStatus = OperationStatusEnum.FAILED;
			operationResult = "挂载云硬盘时，发送后处理请求发生错误，请联系运维人员进行处理，虚拟机ID：" + vmId + "，云硬盘ID：" + diskId;
			operationSeverity = OperationSeverityEnum.HIGH;
			operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
					operationResult, operationSeverity);

			String action = operation;
			String message = operationResult;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		String action = "挂载云硬盘";
		String message = "";
		return actionResultHelper.createActionResult(true, action, message, false, logger,
				ActionResultLogLevelEnum.NONE);
	}

	public ActionResult detachDisk(String username, String vmId, String diskId) {
		if (false == messageProducer.isOpen()) {
			String action = "检查消息连接状态";
			String message = "卸载云硬盘失败，消息连接已关闭，请联系运维人员进行处理";
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		VirtualMachine machine = VirtualMachine.getVirtualMachine(vmId);
		if (null == machine) {
			String action = "检查虚拟机";
			String message = "卸载云硬盘失败，虚拟机不存在，虚拟机ID：" + vmId;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		Disk disk = Disk.getDisk(diskId);
		if (null == disk) {
			String action = "检查云硬盘";
			String message = "卸载云硬盘失败，云硬盘不存在，云硬盘ID：" + diskId;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		CloudManipulator cloud = CloudManipulatorFactory.createCloudManipulator(cloudConfig, machine.getDomainId());
		boolean detachRresult = cloud.detachVolume(vmId, diskId);
		if (false == detachRresult) {
			String action = "发送卸载云硬盘请求";
			String message = "卸载云硬盘失败，发送卸载云硬盘请求失败，虚拟机ID：" + vmId + "，云硬盘ID：" + diskId;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		/*
		 * save operation log
		 */
		String operator = username;
		Date operationTime = new Date();
		String operation = "发送卸载云硬盘请求";
		OperationStatusEnum operationStatus = OperationStatusEnum.OK;
		ServiceNameEnum serviceName = ServiceNameEnum.VIRTURAL_MACHINE_MANAGEMENT;
		String objectId = vmId;
		String operationResult = "发送卸载云硬盘请求成功，虚拟机ID：" + vmId + "，云硬盘ID：" + diskId;
		OperationSeverityEnum operationSeverity = OperationSeverityEnum.MIDDLE;
		operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
				operationResult, operationSeverity);

		/*
		 * save disk status
		 */
		disk = Disk.getDisk(diskId);
		disk.setStatus(DiskStatusEnum.DETATCHING);
		boolean merged;
		try {
			merged = disk.lastCommitWinsMerge();
		} catch (Exception e) {
			merged = false;
		}
		if (false == merged) {
			/*
			 * save operation log
			 */
			operationTime = new Date();
			operation = "保存云硬盘状态";
			operationStatus = OperationStatusEnum.FAILED;
			operationResult = "卸载云硬盘时，保存云硬盘状态发生错误，请联系运维人员进行处理，虚拟机ID：" + vmId + "，云硬盘ID：" + diskId;
			operationSeverity = OperationSeverityEnum.HIGH;
			operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
					operationResult, operationSeverity);

			String action = operation;
			String message = operationResult;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);

		}
		disk.flush();
		disk.clear();

		/*
		 * send message to message queue
		 */
		AsyncOperationRequest request = new AsyncOperationRequest();
		request.setOperationType(AsyncOperationTypeEnum.DETACH_DISK);
		request.setOperator(username);
		request.setVirtualMachineId(vmId);
		request.setDiskId(diskId);
		request.setRequestTime(new Date());
		ObjectMapper mapper = new ObjectMapper();
		try {
			String requestBody = mapper.writeValueAsString(request);
			messageProducer.publish(requestBody);
		} catch (Exception e) {
			/*
			 * save operation log
			 */
			operationTime = new Date();
			operation = "发送卸载云硬盘后处理请求";
			operationStatus = OperationStatusEnum.FAILED;
			operationResult = "卸载云硬盘时，发送后处理请求发生错误，请联系运维人员进行处理，虚拟机ID：" + vmId + "，云硬盘ID：" + diskId;
			operationSeverity = OperationSeverityEnum.HIGH;
			operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
					operationResult, operationSeverity);

			String action = operation;
			String message = operationResult;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		String action = "卸载云硬盘";
		String message = "";
		return actionResultHelper.createActionResult(true, action, message, false, logger,
				ActionResultLogLevelEnum.NONE);
	}

	public ActionResult createSnapshot(String username, String vmId, String snapshotName, String snapshotDescription) {
		if (false == messageProducer.isOpen()) {
			String action = "检查消息连接状态";
			String message = "创建快照失败，消息连接已关闭，请联系运维人员";
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		VirtualMachine machine = VirtualMachine.getVirtualMachine(vmId);
		if (null == machine) {
			String action = "检查虚拟机";
			String message = "创建快照失败，虚拟机不存在，虚拟机ID：" + vmId;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		boolean allowCreateSnapshot = allowCreateSnapshot(username);
		if (false == allowCreateSnapshot) {
			String action = "检查快照数量限制";
			String message = "创建快照失败，用户快照数量达到上限";
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		StorageResourceUsage storageResourceUsage = overviewService.getStorageResourceUsage();
		int freeStorage = (int) storageResourceUsage.getImageSnapshotUsage().getUnused();
		if (machine.getDisk() > freeStorage) {
			String action = "检查快照存储剩余空间";
			String message = "创建快照失败，镜像和快照存储剩余空间不足";
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		/*
		 * create snapshot
		 */
		CloudManipulator cloud = CloudManipulatorFactory.createCloudManipulator(cloudConfig, machine.getDomainId());
		String snapshotId = cloud.createSnapshot(vmId, snapshotName);

		/*
		 * save operation log
		 */
		String operator = username;
		Date operationTime = new Date();
		String operation = "发送创建快照请求";
		OperationStatusEnum operationStatus = OperationStatusEnum.OK;
		ServiceNameEnum serviceName = ServiceNameEnum.VIRTURAL_MACHINE_MANAGEMENT;
		String objectId = vmId;
		String operationResult = "发送创建快照请求成功，虚拟机ID：" + vmId + "，快照ID：" + snapshotId + "，快照名称：" + snapshotName
				+ "，快照描述：" + snapshotDescription;
		OperationSeverityEnum operationSeverity = OperationSeverityEnum.LOW;
		operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
				operationResult, operationSeverity);

		/*
		 * save machine data
		 */
		machine = VirtualMachine.getVirtualMachine(vmId);
		Snapshot snapshot = new Snapshot();
		snapshot.setSnapshotId(snapshotId);
		snapshot.setSnapshotName(snapshotName);
		snapshot.setDescription(snapshotDescription);
		snapshot.setStatus(SnapshotStatusEnum.SAVING);
		snapshot.setCreator(username);
		snapshot.setCreateTime(new Date());
		snapshot.setVirtualMachine(machine);
		machine.getSnapshots().add(snapshot);
		machine.setTaskStatus(VirtualMachineTaskStatusEnum.SNAPSHOTING); // machine is now in STOPPED/SNAPSHOTING state
		boolean merged;
		try {
			merged = machine.lastCommitWinsMerge();
		} catch (Exception e) {
			merged = false;
		}
		if (false == merged) {
			/*
			 * save operation log
			 */
			operationTime = new Date();
			operation = "保存虚拟机和快照数据";
			operationStatus = OperationStatusEnum.FAILED;
			operationResult = "创建快照时，保存虚拟机和快照数据发生错误，请联系运维人员进行处理，虚拟机ID：" + vmId + "，快照ID：" + snapshotId;
			operationSeverity = OperationSeverityEnum.HIGH;
			operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
					operationResult, operationSeverity);

			String action = operation;
			String message = operationResult;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}
		machine.flush();
		machine.clear();

		/*
		 * send message to message queue
		 */
		AsyncOperationRequest request = new AsyncOperationRequest();
		request.setOperationType(AsyncOperationTypeEnum.CREATE_SNAPSHOT);
		request.setOperator(username);
		request.setVirtualMachineId(vmId);
		request.setImageId(snapshotId);
		request.setRequestTime(new Date());
		ObjectMapper mapper = new ObjectMapper();
		try {
			String requestBody = mapper.writeValueAsString(request);
			messageProducer.publish(requestBody);
		} catch (Exception e) {
			/*
			 * save operation log
			 */
			operationTime = new Date();
			operation = "发送创建快照后处理请求";
			operationStatus = OperationStatusEnum.FAILED;
			operationResult = "创建快照时，发送后处理请求发生错误，请联系运维人员进行处理，虚拟机ID：" + vmId + "，快照ID：" + snapshotId;
			operationSeverity = OperationSeverityEnum.HIGH;
			operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
					operationResult, operationSeverity);

			String action = operation;
			String message = operationResult;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		String action = "创建快照";
		String message = "";
		return actionResultHelper.createActionResult(true, action, message, false, logger,
				ActionResultLogLevelEnum.NONE);
	}

	private boolean allowCreateSnapshot(String username) {
		User user = User.getUserByUsername(username);
		if (null == user) {
			return false;
		}

		int snapshotQuota = 0;
		if (null != user.getSnapshotQuota()) {
			snapshotQuota = user.getSnapshotQuota();
		}

		int snapshotCount = (int) Snapshot.countSnapshotsByManager(username);

		return snapshotQuota > snapshotCount;
	}

	public ActionResult restoreSnapshot(String username, String vmId, String snapshotId, String virtualMachineName,
			String virtualMachineDescription) {
		VirtualMachine machine = VirtualMachine.getVirtualMachine(vmId);
		if (null == machine) {
			String action = "检查虚拟机";
			String message = "恢复快照失败，虚拟机不存在，虚拟机ID：" + vmId + "，快照ID：" + snapshotId;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		Snapshot snapshot = Snapshot.getSnapshot(snapshotId);
		if (null == snapshot) {
			String action = "检查快照";
			String message = "恢复快照失败，快照不存在，虚拟机ID：" + vmId + "，快照ID：" + snapshotId;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		return createVirtualMachine(username, virtualMachineName, virtualMachineDescription, machine.getCpu(),
				machine.getMemory(), machine.getDisk(), snapshotId, machine.getDomainId(), machine.getGroupId(), "",
				machine.getManager());
	}

	public BatchActionResult liveMigrateVirtualMachines(String username, List<MigrationPolicy> migratePolicies) {
		BatchActionResult result = new BatchActionResult();
		boolean success = true;
		String message = "";
		List<String> finishedList = new ArrayList<String>();
		List<String> failedList = new ArrayList<String>();

		String operator = username;
		Date operationTime;
		String operation;
		OperationStatusEnum operationStatus;
		ServiceNameEnum serviceName = ServiceNameEnum.VIRTURAL_MACHINE_MANAGEMENT;
		String objectId;
		String operationResult;
		OperationSeverityEnum operationSeverity;

		for (MigrationPolicy policy : migratePolicies) {
			String vmId = policy.getVirtualMachine().getId();
			VirtualMachine machine = VirtualMachine.getVirtualMachine(vmId);
			if (null == machine) {
				message += "在线迁移虚拟机失败，虚拟机不存在，虚拟机ID：" + vmId;
				message += "；";
				failedList.add(vmId);

				continue;
			}

			String hypervisorName = policy.getHypervisorName();
			try {
				ActionResult migrateResult = liveMigrateVirtualMachine(username, vmId, hypervisorName);
				if (true == migrateResult.isSuccess()) {
					finishedList.add(vmId);
				} else {
					/*
					 * save operation log
					 */
					operationTime = new Date();
					operation = migrateResult.getAction();
					operationStatus = OperationStatusEnum.FAILED;
					objectId = vmId;
					operationResult = migrateResult.getMessage();
					operationSeverity = OperationSeverityEnum.HIGH;
					operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName,
							objectId, operationResult, operationSeverity);

					message += operationResult;
					message += "；";
					failedList.add(vmId);
				}
			} catch (Exception e) {
				/*
				 * save operation log
				 */
				operationTime = new Date();
				operation = "在线迁移虚拟机";
				operationStatus = OperationStatusEnum.FAILED;
				objectId = vmId;
				operationResult = "在线迁移虚拟机失败，在线迁移虚拟机过程中发生错误，虚拟机ID：" + vmId + "，虚拟机名称：" + machine.getHostName();
				operationSeverity = OperationSeverityEnum.HIGH;
				operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
						operationResult, operationSeverity);

				message += operationResult;
				message += "；";
				failedList.add(vmId);

				logger.error(operationResult, e);
			}
		}

		if (failedList.size() > 0) {
			success = false;
		} else {
			success = true;
		}
		result.setSuccess(success);
		result.setMessage(message);
		result.setFinishedList(finishedList);
		result.setFailedList(failedList);

		if (false == success) {
			logger.error(message);
		}
		return result;
	}

	/**
	 * live migration virtual machine to hypervisor.
	 * 
	 * @param username
	 *            - name of user who perform the action
	 * @param vmId
	 *            - virtual machine id
	 * @param hypervisorName
	 *            - hypervisor name
	 * @return action result
	 * @author xiangqian
	 */
	private ActionResult liveMigrateVirtualMachine(String username, String vmId, String hypervisorName) {
		if (false == messageProducer.isOpen()) {
			String action = "检查消息连接状态";
			String message = "在线迁移虚拟机失败，消息连接已关闭，请联系运维人员进行处理";
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		VirtualMachine machine = VirtualMachine.getVirtualMachine(vmId);
		if (null == machine) {
			String action = "检查虚拟机";
			String message = "在线迁移虚拟机失败，虚拟机不存在，虚拟机ID：" + vmId;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		CloudManipulator cloud = CloudManipulatorFactory.createCloudManipulator(cloudConfig, machine.getDomainId());
		boolean migrated = cloud.liveMigrate(vmId, hypervisorName); // pass FQDN hypervisorName parameter
		if (false == migrated) {
			String action = "发送在线迁移虚拟机请求";
			String message = "在线迁移虚拟机失败，发送在线迁移虚拟机请求失败，虚拟机ID：" + vmId + "，虚拟机名称：" + machine.getHostName();
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		/*
		 * save operation log
		 */
		String operator = username;
		Date operationTime = new Date();
		String operation = "发送在线迁移虚拟机请求";
		OperationStatusEnum operationStatus = OperationStatusEnum.OK;
		ServiceNameEnum serviceName = ServiceNameEnum.VIRTURAL_MACHINE_MANAGEMENT;
		String objectId = vmId;
		String operationResult = "发送在线迁移虚拟机请求成功，虚拟机ID：" + vmId + "，虚拟机名称：" + machine.getHostName();
		OperationSeverityEnum operationSeverity = OperationSeverityEnum.MIDDLE;
		operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
				operationResult, operationSeverity);

		/*
		 * save virtual machine status, don't set physical machine now
		 */
		machine = VirtualMachine.getVirtualMachine(vmId);
		machine.setStatus(VirtualMachineStatusEnum.ACTIVE);
		machine.setTaskStatus(VirtualMachineTaskStatusEnum.MIGRATING);
		boolean merged;
		try {
			merged = machine.lastCommitWinsMerge();
		} catch (Exception e) {
			merged = false;
		}
		if (false == merged) {
			/*
			 * the caller is responsible to save operation log.
			 */
			String action = "保存虚拟机状态";
			String message = "在线迁移虚拟机时，保存虚拟机状态发生错误，请联系运维人员进行处理，虚拟机ID：" + vmId + "，虚拟机名称：" + machine.getHostName();
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}
		machine.flush();
		machine.clear();

		/*
		 * send message to message queue
		 */
		AsyncOperationRequest request = new AsyncOperationRequest();
		request.setOperationType(AsyncOperationTypeEnum.LIVE_MIGRATION_VIRTUAL_MACHINE);
		request.setOperator(username);
		request.setVirtualMachineId(vmId);
		request.setHypervisorName(hypervisorName);
		request.setRequestTime(new Date());

		ObjectMapper mapper = new ObjectMapper();
		try {
			String requestBody = mapper.writeValueAsString(request);
			messageProducer.publish(requestBody);
		} catch (Exception e) {
			/*
			 * the caller is responsible to save operation log.
			 */
			String action = "发送在线迁移虚拟机后处理请求";
			String message = "在线迁移虚拟机时，发送后处理请求发生错误，虚拟机ID：" + vmId + "，虚拟机名称：" + machine.getHostName();
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		String action = "在线迁移虚拟机";
		String message = "";
		return actionResultHelper.createActionResult(true, action, message, false, logger,
				ActionResultLogLevelEnum.NONE);
	}

	public List<com.sinoparasoft.type.VirtualMachine> getVirtualMachinesByHypervisorName(String hypervisorName) {
		List<com.sinoparasoft.type.VirtualMachine> machineValueList = new ArrayList<com.sinoparasoft.type.VirtualMachine>();

		List<VirtualMachine> machines = VirtualMachine.getVirtualMachinesByHypervisorName(hypervisorName);
		for (VirtualMachine machine : machines) {
			com.sinoparasoft.type.VirtualMachine machineValue = buildVirtualMachineValue(machine);
			machineValueList.add(machineValue);
		}

		return machineValueList;
	}
}
