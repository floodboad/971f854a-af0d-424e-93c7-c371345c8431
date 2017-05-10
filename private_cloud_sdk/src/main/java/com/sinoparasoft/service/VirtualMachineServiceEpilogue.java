package com.sinoparasoft.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openstack4j.model.compute.Server;
import org.openstack4j.model.storage.block.Volume;
import org.openstack4j.model.storage.block.Volume.Status;
import org.openstack4j.model.storage.block.VolumeAttachment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sinoparasoft.common.ActionResult;
import com.sinoparasoft.common.ActionResultLogLevelEnum;
import com.sinoparasoft.common.AlarmParameter;
import com.sinoparasoft.config.AppConfig;
import com.sinoparasoft.common.AsyncOperationRequest;
import com.sinoparasoft.enumerator.DiskStatusEnum;
import com.sinoparasoft.enumerator.MonitorNameEnum;
import com.sinoparasoft.enumerator.MonitorSourceEnum;
import com.sinoparasoft.enumerator.MonitorTypeEnum;
import com.sinoparasoft.enumerator.OperationSeverityEnum;
import com.sinoparasoft.enumerator.OperationStatusEnum;
import com.sinoparasoft.enumerator.ServiceNameEnum;
import com.sinoparasoft.enumerator.VirtualMachineStatusEnum;
import com.sinoparasoft.enumerator.VirtualMachineTaskStatusEnum;
import com.sinoparasoft.model.MonitorSetting;
import com.sinoparasoft.model.Monitor;
import com.sinoparasoft.model.Disk;
import com.sinoparasoft.model.VirtualMachine;
import com.sinoparasoft.openstack.CloudManipulator;
import com.sinoparasoft.openstack.CloudManipulatorFactory;
import com.sinoparasoft.openstack.type.CloudConfig;
import com.sinoparasoft.openstack.type.ServerInfo;
import com.sinoparasoft.util.ActionResultHelper;

@Service
public class VirtualMachineServiceEpilogue {
	private static Logger logger = LoggerFactory.getLogger(VirtualMachineServiceEpilogue.class);

	@Autowired
	CloudConfig cloudConfig;

	@Autowired
	AppConfig appConfig;

	@Autowired
	OperationLogService operationLogService;

	@Autowired
	ActionResultHelper actionResultHelper;

	/**
	 * create virtual machine epilogue operation
	 * 
	 * @param request
	 *            - asyn request
	 * @author xiangqian
	 * @throws InterruptedException
	 */
	public ActionResult createVirtualMachineEpilogue(AsyncOperationRequest request) {
		String vmId = request.getVirtualMachineId();
		String username = request.getOperator();

		VirtualMachine machine = VirtualMachine.getVirtualMachine(vmId);
		if (null == machine) {
			String action = "检查虚拟机";
			String message = "创建虚拟机后处理失败，虚拟机不存在，虚拟机ID：" + vmId;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		/*
		 * wait server status.
		 */
		String domainId = machine.getDomainId();
		CloudManipulator cloud = CloudManipulatorFactory.createCloudManipulator(cloudConfig, domainId);
		List<Server.Status> statusList = new ArrayList<Server.Status>();
		statusList.add(Server.Status.ACTIVE);
		statusList.add(Server.Status.ERROR);
		boolean waitResult = false;
		try {
			waitResult = cloud.waitServerStatus(vmId, statusList, appConfig.getPortalWaitTimeout());
		} catch (InterruptedException e) {
			waitResult = false;
		}

		/*
		 * retry if wait returns without excepting result.
		 */
		if (false == waitResult) {
			String action = "监测虚拟机状态";
			String message = "创建虚拟机后处理发生告警，在限定的时间内虚拟机没有进入期待的状态，将会进行下一次尝试，虚拟机ID：" + vmId + "，虚拟机名称："
					+ machine.getHostName();
			return actionResultHelper.createActionResult(false, action, message, true, logger,
					ActionResultLogLevelEnum.WARN);
		}

		String operator = username;
		Date operationTime;
		String operation;
		OperationStatusEnum operationStatus;
		ServiceNameEnum serviceName = ServiceNameEnum.VIRTURAL_MACHINE_MANAGEMENT;
		String objectId = vmId;
		String operationResult;
		OperationSeverityEnum operationSeverity;

		/*
		 * server is now in active or error status.
		 */
		Server server = cloud.getServer(vmId);
		if (Server.Status.ACTIVE == server.getStatus()) {
			/*
			 * save operation log
			 */
			operationTime = new Date();
			operation = "监测虚拟机状态";
			operationStatus = OperationStatusEnum.OK;
			operationResult = "创建虚拟机后处理过程中，监测到虚拟机进入运行中状态，虚拟机ID：" + vmId + "，虚拟机名称：" + machine.getHostName();
			operationSeverity = OperationSeverityEnum.LOW;
			operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
					operationResult, operationSeverity);

			/*
			 * add machine info
			 */
			ActionResult supplementResult = supplementVirtualMachineInfo(username, vmId);
			if (false == supplementResult.isSuccess()) {
				/*
				 * retry
				 */
				if (true == supplementResult.isRetry()) {
					return supplementResult;
				}

				/*
				 * save operation log.
				 */
				operationTime = new Date();
				operation = supplementResult.getAction();
				operationStatus = OperationStatusEnum.FAILED;
				operationResult = supplementResult.getMessage();
				operationSeverity = OperationSeverityEnum.HIGH;
				operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
						operationResult, operationSeverity);

				/*
				 * set machine status
				 */
				VirtualMachineStatusEnum machineStatus = VirtualMachineStatusEnum.ERROR;
				VirtualMachineTaskStatusEnum taskStatus = VirtualMachineTaskStatusEnum.NONE;
				machine = VirtualMachine.getVirtualMachine(vmId);
				machine.setStatus(machineStatus);
				machine.setTaskStatus(taskStatus);
				boolean merged;
				try {
					merged = machine.lastCommitWinsMerge();
				} catch (Exception e) {
					merged = false;
				}
				if (false == merged) {
					String action = "保存虚拟机状态";
					String message = "创建虚拟机后处理过程中，保存虚拟机状态失败，请联系运维人员进行处理，虚拟机ID：" + vmId + "，虚拟机名称："
							+ machine.getHostName();
					return actionResultHelper.createActionResult(false, action, message, false, logger,
							ActionResultLogLevelEnum.ERROR);
				}
				machine.flush();
				machine.clear();

				String action = operation;
				String message = operationResult;
				return actionResultHelper.createActionResult(false, action, message, false, logger,
						ActionResultLogLevelEnum.ERROR);
			}

			/*
			 * create machine alarms.
			 */
			@SuppressWarnings("unused")
			ActionResult createAlarmResult = createVirtualMachineAlarms(username, vmId);

			/*
			 * save machine status
			 */
			VirtualMachineStatusEnum machineStatus = VirtualMachineStatusEnum.ACTIVE;
			VirtualMachineTaskStatusEnum taskStatus = VirtualMachineTaskStatusEnum.NONE;
			machine = VirtualMachine.getVirtualMachine(vmId);
			machine.setStatus(machineStatus);
			machine.setTaskStatus(taskStatus);
			boolean merged;
			try {
				merged = machine.lastCommitWinsMerge();
			} catch (Exception e) {
				merged = false;
			}
			if (false == merged) {
				String action = "保存虚拟机状态";
				String message = "创建虚拟机后处理过程中，保存虚拟机状态失败，请联系运维人员进行处理，虚拟机ID：" + vmId + "，虚拟机名称：" + machine.getHostName();
				return actionResultHelper.createActionResult(false, action, message, false, logger,
						ActionResultLogLevelEnum.ERROR);
			}
			machine.flush();
			machine.clear();

			String action = "创建虚拟机后处理";
			String message = "";
			return actionResultHelper.createActionResult(true, action, message, false, logger,
					ActionResultLogLevelEnum.NONE);
		} else {
			/*
			 * save operation log.
			 */
			operationTime = new Date();
			operation = "监测虚拟机状态";
			operationStatus = OperationStatusEnum.FAILED;
			operationResult = "创建虚拟机后处理过程中，监测到虚拟机未能进入运行中状态，虚拟机ID：" + vmId + "，虚拟机当前状态：" + server.getStatus().name();
			operationSeverity = OperationSeverityEnum.HIGH;
			operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
					operationResult, operationSeverity);

			/*
			 * save machine status
			 */
			VirtualMachineStatusEnum machineStatus = VirtualMachineStatusEnum.ERROR;
			VirtualMachineTaskStatusEnum taskStatus = VirtualMachineTaskStatusEnum.NONE;
			machine = VirtualMachine.getVirtualMachine(vmId);
			machine.setStatus(machineStatus);
			machine.setTaskStatus(taskStatus);
			boolean merged;
			try {
				merged = machine.lastCommitWinsMerge();
			} catch (Exception e) {
				merged = false;
			}
			if (false == merged) {
				String action = "保存虚拟机状态";
				String message = "创建虚拟机后处理过程中，保存虚拟机状态失败，请联系运维人员进行处理，虚拟机ID：" + vmId + "，虚拟机名称：" + machine.getHostName();
				return actionResultHelper.createActionResult(false, action, message, false, logger,
						ActionResultLogLevelEnum.ERROR);
			}
			machine.flush();
			machine.clear();

			String action = "创建虚拟机后处理";
			String message = operationResult;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}
	}

	private ActionResult supplementVirtualMachineInfo(String username, String vmId) {
		VirtualMachine machine = VirtualMachine.getVirtualMachine(vmId);
		if (null == machine) {
			String action = "检查虚拟机";
			String message = "补充虚拟机信息失败，虚拟机不存在，虚拟机ID：" + vmId;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		CloudManipulator cloud = CloudManipulatorFactory.createCloudManipulator(cloudConfig, machine.getDomainId());
		ServerInfo machineInfo = cloud.getServerInfo(vmId);
		if (null == machineInfo) {
			String action = "获取虚拟机信息";
			String message = "补充虚拟机信息失败，获取虚拟机信息失败，虚拟机ID：" + vmId + "，虚拟机名称：" + machine.getHostName();
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		/*
		 * save machine data
		 */
		machine = VirtualMachine.getVirtualMachine(vmId);
		machine.setPrivateIp(machineInfo.getPrivateIp());
		machine.setFloatingIp(machineInfo.getFloatingIp());
		machine.setHypervisorName(machineInfo.getPhysicalMachine());
		boolean merged;
		try {
			merged = machine.lastCommitWinsMerge();
		} catch (Exception e) {
			merged = false;
		}
		if (false == merged) {
			/*
			 * tell the caller to retry.
			 */
			String action = "保存虚拟机数据";
			String message = "在补充虚拟机信息时，保存虚拟机数据发生错误，将会进行下一次尝试，虚拟机ID：" + vmId + "，虚拟机名称：" + machine.getHostName();
			return actionResultHelper.createActionResult(false, action, message, true, logger,
					ActionResultLogLevelEnum.WARN);
		}
		machine.flush();
		machine.clear();

		/*
		 * save operation log
		 */
		String operator = username;
		Date operationTime = new Date();
		String operation = "补充虚拟机信息";
		OperationStatusEnum operationStatus = OperationStatusEnum.OK;
		ServiceNameEnum serviceName = ServiceNameEnum.VIRTURAL_MACHINE_MANAGEMENT;
		String objectId = vmId;
		String operationResult = "补充虚拟机信息成功，虚拟机ID：" + vmId + "，虚拟机名称：" + machine.getHostName();
		OperationSeverityEnum operationSeverity = OperationSeverityEnum.LOW;
		operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
				operationResult, operationSeverity);

		String action = "补充虚拟机信息";
		String message = "";
		return actionResultHelper.createActionResult(true, action, message, false, logger,
				ActionResultLogLevelEnum.NONE);
	}

	private ActionResult createVirtualMachineAlarms(String username, String vmId) {
		VirtualMachine machine = VirtualMachine.getVirtualMachine(vmId);
		if (null == machine) {
			String action = "检查虚拟机";
			String message = "添加虚拟机负载监控设置失败，虚拟机不存在，虚拟机ID：" + vmId;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		String operator = username;
		Date operationTime;
		String operation;
		OperationStatusEnum operationStatus;
		ServiceNameEnum serviceName = ServiceNameEnum.VIRTURAL_MACHINE_MANAGEMENT;
		String objectId;
		String operationResult;
		OperationSeverityEnum operationSeverity;

		boolean success = true;
		String message = "";
		List<Monitor> monitors = Monitor.getMonitors(MonitorSourceEnum.VIRTUAL_MACHINE, MonitorTypeEnum.LOAD);
		for (Monitor monitor : monitors) {
			if (monitor.getDefaultThreshold() == null) {
				continue;
			}

			AlarmParameter parameter = new AlarmParameter();
			parameter.setAlarmName(monitor.getMonitorName().name());
			parameter.setAlarmThreshold(monitor.getDefaultThreshold());
			parameter.setEnabled(true);
			parameter.setThresholdUnit(monitor.getThresholdUnit());
			parameter.setSeverityLevel(monitor.getSeverityLevel());

			try {
				ActionResult createResult = createAlarm(username, vmId, parameter);
				if (true == createResult.isSuccess()) {
					continue;
				} else {
					/*
					 * save operation log
					 */
					operationTime = new Date();
					operation = createResult.getAction();
					operationStatus = OperationStatusEnum.FAILED;
					objectId = vmId;
					operationResult = createResult.getMessage();
					operationSeverity = OperationSeverityEnum.HIGH;
					operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName,
							objectId, operationResult, operationSeverity);

					success = false;
					message += createResult.getMessage();
					message += "；";
				}
			} catch (Exception e) {
				/*
				 * save operation log
				 */
				operationTime = new Date();
				operation = "添加虚拟机负载监控";
				operationStatus = OperationStatusEnum.FAILED;
				objectId = vmId;
				operationResult = "添加虚拟机负载监控失败，添加虚拟机负载监控过程中发生错误，请手动重新进行添加，虚拟机ID：" + vmId + "监控项："
						+ parameter.getAlarmName();
				operationSeverity = OperationSeverityEnum.HIGH;
				operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
						operationResult, operationSeverity);

				// suppress exception so we can continue with next one
				success = false;
				message += operationResult;
				message += "；";

				logger.warn(operationResult, e);
			}
		}

		String action = "添加虚拟机负载监控";
		if (false == success) {
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.WARN);
		} else {
			return actionResultHelper.createActionResult(true, action, message, false, logger,
					ActionResultLogLevelEnum.NONE);
		}
	}

	/**
	 * create virtual machine alarm.
	 * 
	 * @param username
	 *            - name of the user who perform the operation
	 * @param vmId
	 *            - virtual machine id
	 * @param parameter
	 *            - alarm parameter
	 * @return action result
	 * @author xiangqian
	 */
	public ActionResult createAlarm(String username, String vmId, AlarmParameter parameter) {
		VirtualMachine machine = VirtualMachine.getVirtualMachine(vmId);
		if (null == machine) {
			String action = "检查虚拟机";
			String message = "添加虚拟机负载监控失败，虚拟机不存在，虚拟机ID：" + vmId + "监控项：" + parameter.getAlarmName();
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		CloudManipulator cloud = CloudManipulatorFactory.createCloudManipulator(cloudConfig, machine.getDomainId());
		String meterName = null;
		float threshold = parameter.getAlarmThreshold();
		MonitorNameEnum monitorName = MonitorNameEnum.valueOf(parameter.getAlarmName());
		switch (monitorName) {
		case VM_CPU_UTIL:
			meterName = "cpu_util";
			break;
		case VM_MEMORY_USAGE:
			/*
			 * set memory usage threshold, memory.usage meter needs libvirt version 1.1.1+, QEMU version 1.5+, balloon
			 * driver in the image.
			 */
			meterName = "memory.usage";
			threshold = parameter.getAlarmThreshold() / 100 * machine.getMemory() * 1024; // MB
			break;
		case VM_DISK_READ_BYTES_RATE:
			meterName = "disk.read.bytes.rate";
			threshold = parameter.getAlarmThreshold();
			break;
		case VM_DISK_WRITE_BYTES_RATE:
			meterName = "disk.write.bytes.rate";
			threshold = parameter.getAlarmThreshold();
			break;
		case VM_NETWORK_OUTGOING_BYTES_RATE:
			meterName = "network.outgoing.bytes.rate";
			break;
		case VM_NETWORK_INCOMING_BYTES_RATE:
			meterName = "network.incoming.bytes.rate";
			break;
		default:
			String action = "检查告警类型";
			String message = "添加虚拟机负载监控失败，不支持的虚拟机负载监控类型，虚拟机ID：" + vmId + "监控项：" + parameter.getAlarmName();
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		/*
		 * create alarm
		 */
		String alarmId = cloud.createAlarm(vmId, parameter.getAlarmName(), meterName, threshold);

		/*
		 * confirm alarm created
		 */
		boolean createdResult;
		try {
			createdResult = waitAlarmCreated(cloud, alarmId, appConfig.getPortalWaitTimeout());
		} catch (InterruptedException e) {
			String action = "等待虚拟机负载监控状态";
			String message = "添加虚拟机负载监控失败，等待虚拟机负载监控状态被中断，请手动重新进行添加，虚拟机ID：" + vmId + "监控项：" + parameter.getAlarmName();
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.WARN);
		}

		if (false == createdResult) {
			String action = "添加虚拟机负载监控";
			String message = "添加虚拟机负载监控失败，请手动重新进行添加，虚拟机ID：" + vmId + "监控项：" + parameter.getAlarmName();
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		/*
		 * save operation log
		 */
		String operator = username;
		Date operationTime = new Date();
		String operation = "添加虚拟机负载监控";
		OperationStatusEnum operationStatus = OperationStatusEnum.OK;
		ServiceNameEnum serviceName = ServiceNameEnum.VIRTURAL_MACHINE_MANAGEMENT;
		String objectId = vmId;
		String operationResult = "添加虚拟机负载监控成功，虚拟机ID：" + vmId + "监控项：" + parameter.getAlarmName();
		OperationSeverityEnum operationSeverity = OperationSeverityEnum.LOW;
		operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
				operationResult, operationSeverity);

		/*
		 * save monitor setting
		 */
		MonitorSetting monitorSetting = new MonitorSetting();
		monitorSetting.setMonitorSource(MonitorSourceEnum.VIRTUAL_MACHINE);
		monitorSetting.setSourceId(vmId);
		monitorSetting.setMonitorType(MonitorTypeEnum.LOAD);
		monitorSetting.setMonitorName(monitorName);
		monitorSetting.setThreshold(parameter.getAlarmThreshold());
		monitorSetting.setThresholdUnit(parameter.getThresholdUnit());
		monitorSetting.setSeverityLevel(parameter.getSeverityLevel());
		monitorSetting.setEnabled(true);
		monitorSetting.setOsAlarmId(alarmId);
		monitorSetting.setCreateTime(new Date());
		monitorSetting.persist();
		monitorSetting.flush();
		monitorSetting.clear();

		String action = "添加虚拟机负载监控";
		String message = "";
		return actionResultHelper.createActionResult(true, action, message, false, logger,
				ActionResultLogLevelEnum.NONE);
	}

	private boolean waitAlarmCreated(CloudManipulator cloud, String alarmId, int minute) throws InterruptedException {
		int sleepInterval = 6000;
		int sleepCount = minute * 60 * 1000 / sleepInterval;

		int loop = 0;
		while (loop < sleepCount) {
			String alarmState = cloud.getAlarmState(alarmId);
			if (null != alarmState) {
				return true;
			}

			Thread.sleep(sleepInterval);
			loop++;
		}

		return false;
	}

	/**
	 * start virtual machine epilogue operation
	 * 
	 * @param request
	 *            - asyn request
	 * @return action result
	 * @author xiangqian
	 */
	public ActionResult startVirtualMachineEpilogue(AsyncOperationRequest request) {
		String vmId = request.getVirtualMachineId();
		String username = request.getOperator();

		VirtualMachine machine = VirtualMachine.getVirtualMachine(vmId);
		if (null == machine) {
			String action = "检查虚拟机";
			String message = "启动虚拟机后处理失败，虚拟机不存在，虚拟机ID：" + vmId;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		/*
		 * wait server status.
		 */
		CloudManipulator cloud = CloudManipulatorFactory.createCloudManipulator(cloudConfig, machine.getDomainId());
		List<Server.Status> statusList = new ArrayList<Server.Status>();
		statusList.add(Server.Status.ACTIVE);
		statusList.add(Server.Status.ERROR);
		boolean waitResult = false;
		try {
			waitResult = cloud.waitServerStatus(vmId, statusList, appConfig.getPortalWaitTimeout());
		} catch (InterruptedException e) {
			waitResult = false;
		}

		/*
		 * retry if wait returns without excepting result.
		 */
		if (false == waitResult) {
			String action = "监测虚拟机状态";
			String message = "启动虚拟机后处理发生告警，在限定的时间内虚拟机没有进入期待的状态，将会进行下一次尝试，虚拟机ID：" + vmId + "，虚拟机名称："
					+ machine.getHostName();
			return actionResultHelper.createActionResult(false, action, message, true, logger,
					ActionResultLogLevelEnum.WARN);
		}

		/*
		 * server is now in active or error status.
		 */
		Server server = cloud.getServer(vmId);
		if (Server.Status.ACTIVE == server.getStatus()) {
			/*
			 * save operation log
			 */
			String operator = username;
			Date operationTime = new Date();
			String operation = "监测虚拟机状态";
			OperationStatusEnum operationStatus = OperationStatusEnum.OK;
			ServiceNameEnum serviceName = ServiceNameEnum.VIRTURAL_MACHINE_MANAGEMENT;
			String objectId = vmId;
			String operationResult = "启动虚拟机后处理过程中，监测到虚拟机进入运行中状态，虚拟机ID：" + vmId + "，虚拟机名称：" + machine.getHostName();
			OperationSeverityEnum operationSeverity = OperationSeverityEnum.LOW;
			operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
					operationResult, operationSeverity);

			/*
			 * save machine status
			 */
			VirtualMachineStatusEnum machineStatus = VirtualMachineStatusEnum.ACTIVE;
			VirtualMachineTaskStatusEnum taskStatus = VirtualMachineTaskStatusEnum.NONE;
			machine = VirtualMachine.getVirtualMachine(vmId);
			machine.setStatus(machineStatus);
			machine.setTaskStatus(taskStatus);
			boolean merged;
			try {
				merged = machine.lastCommitWinsMerge();
			} catch (Exception e) {
				merged = false;
			}
			if (false == merged) {
				/*
				 * tell the caller to retry.
				 */
				String action = "保存虚拟机状态";
				String message = "启动虚拟机后处理过程中，保存虚拟机状态发生错误，将会进行下一次尝试，虚拟机ID：" + vmId + "，虚拟机名称：" + machine.getHostName();
				return actionResultHelper.createActionResult(false, action, message, true, logger,
						ActionResultLogLevelEnum.WARN);
			}
			machine.flush();
			machine.clear();

			String action = "启动虚拟机后处理";
			String message = "";
			return actionResultHelper.createActionResult(true, action, message, false, logger,
					ActionResultLogLevelEnum.NONE);
		} else {
			/*
			 * save operation log
			 */
			String operator = username;
			Date operationTime = new Date();
			String operation = "监测虚拟机状态";
			OperationStatusEnum operationStatus = OperationStatusEnum.FAILED;
			ServiceNameEnum serviceName = ServiceNameEnum.VIRTURAL_MACHINE_MANAGEMENT;
			String objectId = vmId;
			String operationResult = "启动虚拟机后处理过程中，监测到虚拟机未能进入运行中状态，虚拟机ID：" + vmId + "，虚拟机当前状态："
					+ server.getStatus().name();
			OperationSeverityEnum operationSeverity = OperationSeverityEnum.HIGH;
			operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
					operationResult, operationSeverity);

			/*
			 * save machine status
			 */
			VirtualMachineStatusEnum machineStatus = VirtualMachineStatusEnum.ERROR;
			VirtualMachineTaskStatusEnum taskStatus = VirtualMachineTaskStatusEnum.NONE;
			machine = VirtualMachine.getVirtualMachine(vmId);
			machine.setStatus(machineStatus);
			machine.setTaskStatus(taskStatus);
			boolean merged;
			try {
				merged = machine.lastCommitWinsMerge();
			} catch (Exception e) {
				merged = false;
			}
			if (false == merged) {
				/*
				 * tell the caller to retry.
				 */
				String action = "保存虚拟机状态";
				String message = "启动虚拟机后处理过程中，保存虚拟机状态发生错误，将会进行下一次尝试，虚拟机ID：" + vmId + "，虚拟机名称：" + machine.getHostName();
				return actionResultHelper.createActionResult(false, action, message, true, logger,
						ActionResultLogLevelEnum.WARN);
			}
			machine.flush();
			machine.clear();

			String action = "启动虚拟机后处理";
			String message = operationResult;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.NONE);
		}
	}

	/**
	 * reboot virtual machine epilogue operation
	 * 
	 * @param request
	 *            - asyn request
	 * @return action result
	 * @author xiangqian
	 */
	public ActionResult rebootVirtualMachineEpilogue(AsyncOperationRequest request) {
		String vmId = request.getVirtualMachineId();
		String username = request.getOperator();

		VirtualMachine machine = VirtualMachine.getVirtualMachine(vmId);
		if (null == machine) {
			String action = "检查虚拟机";
			String message = "重启虚拟机后处理失败，虚拟机不存在，虚拟机ID：" + vmId;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		/*
		 * wait server status.
		 */
		CloudManipulator cloud = CloudManipulatorFactory.createCloudManipulator(cloudConfig, machine.getDomainId());
		List<Server.Status> statusList = new ArrayList<Server.Status>();
		statusList.add(Server.Status.ACTIVE);
		statusList.add(Server.Status.ERROR);
		boolean waitResult = false;
		try {
			waitResult = cloud.waitServerStatus(vmId, statusList, appConfig.getPortalWaitTimeout());
		} catch (InterruptedException e) {
			waitResult = false;
		}

		/*
		 * retry if wait returns without excepting result.
		 */
		if (false == waitResult) {
			String action = "监测虚拟机状态";
			String message = "重启虚拟机后处理发生告警，在限定的时间内虚拟机没有进入期待的状态，将会进行下一次尝试，虚拟机ID：" + vmId + "，虚拟机名称："
					+ machine.getHostName();
			return actionResultHelper.createActionResult(false, action, message, true, logger,
					ActionResultLogLevelEnum.WARN);
		}

		/*
		 * server is now in active or error status.
		 */
		Server server = cloud.getServer(vmId);
		if (Server.Status.ACTIVE == server.getStatus()) {
			/*
			 * save operation log
			 */
			String operator = username;
			Date operationTime = new Date();
			String operation = "监测虚拟机状态";
			OperationStatusEnum operationStatus = OperationStatusEnum.OK;
			ServiceNameEnum serviceName = ServiceNameEnum.VIRTURAL_MACHINE_MANAGEMENT;
			String objectId = vmId;
			String operationResult = "重启虚拟机后处理过程中，监测到虚拟机进入运行中状态，虚拟机ID：" + vmId + "，虚拟机名称：" + machine.getHostName();
			OperationSeverityEnum operationSeverity = OperationSeverityEnum.LOW;
			operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
					operationResult, operationSeverity);

			/*
			 * save machine status
			 */
			VirtualMachineStatusEnum machineStatus = VirtualMachineStatusEnum.ACTIVE;
			VirtualMachineTaskStatusEnum taskStatus = VirtualMachineTaskStatusEnum.NONE;
			machine = VirtualMachine.getVirtualMachine(vmId);
			machine.setStatus(machineStatus);
			machine.setTaskStatus(taskStatus);
			boolean merged;
			try {
				merged = machine.lastCommitWinsMerge();
			} catch (Exception e) {
				merged = false;
			}
			if (false == merged) {
				/*
				 * tell the caller to retry.
				 */
				String action = "保存虚拟机状态";
				String message = "重启虚拟机后处理过程中，保存虚拟机状态发生错误，将会进行下一次尝试，虚拟机ID：" + vmId + "，虚拟机名称：" + machine.getHostName();
				return actionResultHelper.createActionResult(false, action, message, true, logger,
						ActionResultLogLevelEnum.WARN);
			}
			machine.flush();
			machine.clear();

			String action = "重启虚拟机后处理";
			String message = "";
			return actionResultHelper.createActionResult(true, action, message, false, logger,
					ActionResultLogLevelEnum.NONE);
		} else {
			/*
			 * save operation log
			 */
			String operator = username;
			Date operationTime = new Date();
			String operation = "监测虚拟机状态";
			OperationStatusEnum operationStatus = OperationStatusEnum.FAILED;
			ServiceNameEnum serviceName = ServiceNameEnum.VIRTURAL_MACHINE_MANAGEMENT;
			String objectId = vmId;
			String operationResult = "重启虚拟机后处理过程中，监测到虚拟机未能进入运行中状态，虚拟机ID：" + vmId + "，虚拟机当前状态："
					+ server.getStatus().name();
			OperationSeverityEnum operationSeverity = OperationSeverityEnum.HIGH;
			operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
					operationResult, operationSeverity);

			/*
			 * save machine status
			 */
			VirtualMachineStatusEnum machineStatus = VirtualMachineStatusEnum.ERROR;
			VirtualMachineTaskStatusEnum taskStatus = VirtualMachineTaskStatusEnum.NONE;
			machine = VirtualMachine.getVirtualMachine(vmId);
			machine.setStatus(machineStatus);
			machine.setTaskStatus(taskStatus);
			boolean merged;
			try {
				merged = machine.lastCommitWinsMerge();
			} catch (Exception e) {
				merged = false;
			}
			if (false == merged) {
				/*
				 * tell the caller to retry.
				 */
				String action = "保存虚拟机状态";
				String message = "重启虚拟机后处理过程中，保存虚拟机状态发生错误，将会进行下一次尝试，虚拟机ID：" + vmId + "，虚拟机名称：" + machine.getHostName();
				return actionResultHelper.createActionResult(false, action, message, true, logger,
						ActionResultLogLevelEnum.WARN);
			}
			machine.flush();
			machine.clear();

			String action = "重启虚拟机后处理";
			String message = operationResult;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}
	}

	/**
	 * shutdown virtual machine epilogue operation.
	 * 
	 * @param request
	 *            - async request
	 * @return action result
	 * @author xiangqian
	 */
	public ActionResult shutdownVirtualMachineEpilogue(AsyncOperationRequest request) {
		String vmId = request.getVirtualMachineId();
		String username = request.getOperator();

		VirtualMachine machine = VirtualMachine.getVirtualMachine(vmId);
		if (null == machine) {
			String action = "检查虚拟机";
			String message = "关闭虚拟机后处理失败，虚拟机不存在，虚拟机ID：" + vmId;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		/*
		 * wait server status
		 */
		CloudManipulator cloud = CloudManipulatorFactory.createCloudManipulator(cloudConfig, machine.getDomainId());
		List<Server.Status> statusList = new ArrayList<Server.Status>();
		statusList.add(Server.Status.SHUTOFF);
		statusList.add(Server.Status.ERROR);
		boolean waitResult = false;
		try {
			waitResult = cloud.waitServerStatus(vmId, statusList, appConfig.getPortalWaitTimeout());
		} catch (InterruptedException e) {
			waitResult = false;
		}

		/*
		 * retry if wait returns without excepting result.
		 */
		if (false == waitResult) {
			String action = "监测虚拟机状态";
			String message = "关闭虚拟机后处理发生告警，在限定的时间内虚拟机没有进入期待的状态，将会进行下一次尝试，虚拟机ID：" + vmId + "，虚拟机名称："
					+ machine.getHostName();
			return actionResultHelper.createActionResult(false, action, message, true, logger,
					ActionResultLogLevelEnum.WARN);
		}

		/*
		 * server is now in shutoff or error status.
		 */
		Server server = cloud.getServer(vmId);
		if (Server.Status.SHUTOFF == server.getStatus()) {
			/*
			 * save operation log
			 */
			String operator = username;
			Date operationTime = new Date();
			String operation = "监测虚拟机状态";
			OperationStatusEnum operationStatus = OperationStatusEnum.OK;
			ServiceNameEnum serviceName = ServiceNameEnum.VIRTURAL_MACHINE_MANAGEMENT;
			String objectId = vmId;
			String operationResult = "关闭虚拟机后处理过程中，监测到虚拟机进入关闭状态，虚拟机ID：" + vmId + "，虚拟机名称：" + machine.getHostName();
			OperationSeverityEnum operationSeverity = OperationSeverityEnum.LOW;
			operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
					operationResult, operationSeverity);

			/*
			 * save machine status
			 */
			VirtualMachineStatusEnum machineStatus = VirtualMachineStatusEnum.STOPPED;
			VirtualMachineTaskStatusEnum taskStatus = VirtualMachineTaskStatusEnum.NONE;
			machine = VirtualMachine.getVirtualMachine(vmId);
			machine.setStatus(machineStatus);
			machine.setTaskStatus(taskStatus);
			boolean merged;
			try {
				merged = machine.lastCommitWinsMerge();
			} catch (Exception e) {
				merged = false;
			}
			if (false == merged) {
				/*
				 * tell the caller to retry.
				 */
				String action = "保存虚拟机状态";
				String message = "关闭虚拟机后处理过程中，保存虚拟机状态发生错误，将会进行下一次尝试，虚拟机ID：" + vmId + "，虚拟机名称：" + machine.getHostName();
				return actionResultHelper.createActionResult(false, action, message, true, logger,
						ActionResultLogLevelEnum.WARN);
			}
			machine.flush();
			machine.clear();

			String action = "关闭虚拟机后处理";
			String message = "";
			return actionResultHelper.createActionResult(true, action, message, false, logger,
					ActionResultLogLevelEnum.NONE);
		} else {
			/*
			 * save operation log
			 */
			String operator = username;
			Date operationTime = new Date();
			String operation = "监测虚拟机状态";
			OperationStatusEnum operationStatus = OperationStatusEnum.FAILED;
			ServiceNameEnum serviceName = ServiceNameEnum.VIRTURAL_MACHINE_MANAGEMENT;
			String objectId = vmId;
			String operationResult = "关闭虚拟机后处理过程中，监测到虚拟机未能进入关闭状态，虚拟机ID：" + vmId + "，虚拟机当前状态："
					+ server.getStatus().name();
			OperationSeverityEnum operationSeverity = OperationSeverityEnum.HIGH;
			operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
					operationResult, operationSeverity);

			/*
			 * save machine status
			 */
			VirtualMachineStatusEnum machineStatus = VirtualMachineStatusEnum.ERROR;
			VirtualMachineTaskStatusEnum taskStatus = VirtualMachineTaskStatusEnum.NONE;
			machine = VirtualMachine.getVirtualMachine(vmId);
			machine.setStatus(machineStatus);
			machine.setTaskStatus(taskStatus);
			boolean merged;
			try {
				merged = machine.lastCommitWinsMerge();
			} catch (Exception e) {
				merged = false;
			}
			if (false == merged) {
				/*
				 * tell the caller to retry.
				 */
				String action = "保存虚拟机状态";
				String message = "关闭虚拟机后处理过程中，保存虚拟机状态发生错误，将会进行下一次尝试，虚拟机ID：" + vmId + "，虚拟机名称：" + machine.getHostName();
				return actionResultHelper.createActionResult(false, action, message, true, logger,
						ActionResultLogLevelEnum.WARN);
			}
			machine.flush();
			machine.clear();

			String action = "关闭虚拟机后处理";
			String message = operationResult;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.NONE);
		}
	}

	/**
	 * delete virtual machine epilogue operation.
	 * 
	 * @param request
	 *            - async request
	 * @return action result
	 * @author xiangqian
	 */
	public ActionResult deleteVirtualMachineEpilogue(AsyncOperationRequest request) {
		String vmId = request.getVirtualMachineId();
		String username = request.getOperator();

		VirtualMachine machine = VirtualMachine.getVirtualMachine(vmId);
		if (null == machine) {
			String action = "检查虚拟机";
			String message = "删除虚拟机后处理失败，虚拟机不存在，虚拟机ID：" + vmId;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		String operator = username;
		ServiceNameEnum serviceName = ServiceNameEnum.VIRTURAL_MACHINE_MANAGEMENT;
		String objectId = vmId;

		/*
		 * clear tags.
		 */
		machine.getApplicationTags().clear();
		boolean merged;
		try {
			merged = machine.lastCommitWinsMerge();
		} catch (Exception e) {
			merged = false;
		}
		if (false == merged) {
			/*
			 * tell the caller to retry.
			 */
			String action = "清除虚拟机业务系统标签";
			String message = "删除虚拟机后处理过程中，清除虚拟机业务系统标签发生错误，将会进行下一次尝试，虚拟机ID：" + vmId + "，虚拟机名称：" + machine.getHostName();
			return actionResultHelper.createActionResult(false, action, message, true, logger,
					ActionResultLogLevelEnum.WARN);
		}
		machine.flush();
		machine.clear();

		/*
		 * save operation log
		 */
		Date operationTime = new Date();
		String operation = "清除虚拟机业务系统标签";
		OperationStatusEnum operationStatus = OperationStatusEnum.OK;
		String operationResult = "清除虚拟机业务系统标签成功，虚拟机ID：" + vmId + "，虚拟机名称：" + machine.getHostName();
		OperationSeverityEnum operationSeverity = OperationSeverityEnum.MIDDLE;
		operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
				operationResult, operationSeverity);

		/*
		 * delete alarms.
		 */
		@SuppressWarnings("unused")
		ActionResult deleteResult = deleteVirtualMachineAlarms(username, vmId);

		/*
		 * wait server status.
		 */
		CloudManipulator cloud = CloudManipulatorFactory.createCloudManipulator(cloudConfig, machine.getDomainId());
		boolean waitResult = false;
		try {
			waitResult = cloud.waitServerDeleted(vmId, appConfig.getPortalWaitTimeout());
		} catch (InterruptedException e) {
			waitResult = false;
		}

		/*
		 * retry if wait returns without excepting result.
		 */
		if (false == waitResult) {
			String action = "监测虚拟机状态";
			String message = "删除虚拟机后处理发生告警，在限定的时间内虚拟机没有被刪除，将会进行下一次尝试，虚拟机ID：" + vmId + "，虚拟机名称：" + machine.getHostName();
			return actionResultHelper.createActionResult(false, action, message, true, logger,
					ActionResultLogLevelEnum.WARN);
		}

		/*
		 * server is not exist.
		 */

		/*
		 * save operation log
		 */
		operationTime = new Date();
		operation = "监测虚拟机状态";
		operationStatus = OperationStatusEnum.OK;
		operationResult = "删除虚拟机后处理过程中，监测到虚拟机删除成功，虚拟机ID：" + vmId + "，虚拟机名称：" + machine.getHostName();
		operationSeverity = OperationSeverityEnum.LOW;
		operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
				operationResult, operationSeverity);

		/*
		 * save machine status
		 */
		VirtualMachineStatusEnum machineStatus = VirtualMachineStatusEnum.DELETED;
		VirtualMachineTaskStatusEnum taskStatus = VirtualMachineTaskStatusEnum.NONE;
		machine = VirtualMachine.getVirtualMachine(vmId);
		machine.setStatus(machineStatus);
		machine.setTaskStatus(taskStatus);
		try {
			merged = machine.lastCommitWinsMerge();
		} catch (Exception e) {
			merged = false;
		}
		if (false == merged) {
			/*
			 * tell the caller to retry.
			 */
			String action = "保存虚拟机状态";
			String message = "删除虚拟机后处理过程中，保存虚拟机状态发生错误，将会进行下一次尝试，虚拟机ID：" + vmId + "，虚拟机名称：" + machine.getHostName();
			return actionResultHelper.createActionResult(false, action, message, true, logger,
					ActionResultLogLevelEnum.WARN);
		}
		machine.flush();
		machine.clear();

		String action = "删除虚拟机后处理";
		String message = "";
		return actionResultHelper.createActionResult(true, action, message, false, logger,
				ActionResultLogLevelEnum.NONE);
	}

	/**
	 * delete virtual machine alarms.
	 * 
	 * @param username
	 *            - name of the user who perform the operation
	 * @param vmId
	 *            - virtual machine id
	 * @return action result
	 * @author xiangqian
	 */
	private ActionResult deleteVirtualMachineAlarms(String username, String vmId) {
		VirtualMachine machine = VirtualMachine.getVirtualMachine(vmId);
		if (null == machine) {
			String action = "检查虚拟机";
			String message = "删除虚拟机负载监控失败，虚拟机不存在，虚拟机ID：" + vmId;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		boolean success = true;
		String message = "";

		String operator = username;
		Date operationTime;
		String operation = "删除虚拟机负载监控";
		OperationStatusEnum operationStatus;
		ServiceNameEnum serviceName = ServiceNameEnum.VIRTURAL_MACHINE_MANAGEMENT;
		String objectId = vmId;
		String operationResult;
		OperationSeverityEnum operationSeverity;

		CloudManipulator cloud = CloudManipulatorFactory.createCloudManipulator(cloudConfig, machine.getDomainId());
		List<MonitorSetting> monitorSettings = MonitorSetting.getMonitorSettings(vmId, MonitorTypeEnum.LOAD);
		for (MonitorSetting monitorSetting : monitorSettings) {
			try {
				boolean deleted = cloud.deleteAlarm(monitorSetting.getOsAlarmId());
				if (true == deleted) {
					/*
					 * save operation log
					 */
					operationTime = new Date();
					operationStatus = OperationStatusEnum.OK;
					operationResult = "删除虚拟机负载监控成功，虚拟机ID：" + vmId + "监控项：" + monitorSetting.getMonitorName().name();
					operationSeverity = OperationSeverityEnum.LOW;
					operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName,
							objectId, operationResult, operationSeverity);

					/*
					 * save data
					 */
					monitorSetting.remove();
					monitorSetting.flush();
					monitorSetting.clear();
				} else {
					/*
					 * save operation log
					 */
					operationTime = new Date();
					operationStatus = OperationStatusEnum.FAILED;
					operationResult = "删除虚拟机负载监控失败，虚拟机ID：" + vmId + "监控项：" + monitorSetting.getMonitorName().name();
					operationSeverity = OperationSeverityEnum.MIDDLE;
					operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName,
							objectId, operationResult, operationSeverity);

					success = false;
					message += operationResult;
					message += "；";
				}
			} catch (Exception e) {
				/*
				 * save operation log
				 */
				operationTime = new Date();
				operationStatus = OperationStatusEnum.FAILED;
				operationResult = "删除虚拟机负载监控发生错误，虚拟机ID：" + vmId + "监控项：" + monitorSetting.getMonitorName().name();
				operationSeverity = OperationSeverityEnum.MIDDLE;
				operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
						operationResult, operationSeverity);

				// suppress exception so we can continue with next one
				success = false;
				message += operationResult;
				message += "；";

				logger.warn(operationResult, e);
			}
		}

		if (false == success) {
			String action = "删除虚拟机负载监控";
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.WARN);
		} else {
			String action = "删除虚拟机负载监控";
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.NONE);
		}
	}

	/**
	 * attach disk epilogue operation.
	 * 
	 * @param request
	 *            - async request
	 * @return action result
	 * @author xiangqian
	 */
	public ActionResult attachDiskEpilogue(AsyncOperationRequest request) {
		String username = request.getOperator();
		String vmId = request.getVirtualMachineId();
		String diskId = request.getDiskId();

		VirtualMachine machine = VirtualMachine.getVirtualMachine(vmId);
		if (null == machine) {
			String action = "检查虚拟机";
			String message = "挂载云硬盘后处理失败，虚拟机不存在，虚拟机ID：" + vmId;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		Disk disk = Disk.getDisk(diskId);
		if (null == disk) {
			String action = "检查云硬盘";
			String message = "挂载云硬盘后处理失败，云硬盘不存在，云硬盘ID：" + diskId;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		/*
		 * wait volume statue
		 */
		CloudManipulator cloud = CloudManipulatorFactory.createCloudManipulator(cloudConfig,
				cloudConfig.getAdminProjectId());
		List<Status> statusList = new ArrayList<Volume.Status>();
		statusList.add(Status.AVAILABLE);
		statusList.add(Status.IN_USE);
		boolean waitResult = false;
		try {
			waitResult = cloud.waitVolumeStatus(diskId, statusList, appConfig.getPortalWaitTimeout());
		} catch (InterruptedException e) {
			waitResult = false;
		}

		/*
		 * retry if wait returns without excepting result.
		 */
		if (false == waitResult) {
			String action = "监测云硬盘状态";
			String message = "挂载云硬盘后处理发生告警，在限定的时间内云硬盘没有进入期待的状态，将会进行下一次尝试，云硬盘ID：" + diskId;
			return actionResultHelper.createActionResult(false, action, message, true, logger,
					ActionResultLogLevelEnum.WARN);
		}

		/*
		 * volume is now in available or in_use status.
		 */
		Volume volume = cloud.getVolume(diskId);
		if (Status.IN_USE == volume.getStatus()) {
			/*
			 * save operation log
			 */
			String operator = username;
			Date operationTime = new Date();
			String operation = "监测云硬盘状态";
			OperationStatusEnum operationStatus = OperationStatusEnum.OK;
			ServiceNameEnum serviceName = ServiceNameEnum.VIRTURAL_MACHINE_MANAGEMENT;
			String objectId = vmId;
			String operationResult = "挂载云硬盘后处理过程中，监测到云硬盘进入已挂载状态，虚拟机ID：" + vmId + "，云硬盘ID：" + diskId;
			OperationSeverityEnum operationSeverity = OperationSeverityEnum.LOW;
			operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
					operationResult, operationSeverity);

			/*
			 * save disk status and attach point.
			 */
			String attachPoint = null;
			List<? extends VolumeAttachment> attachments = volume.getAttachments();
			if (null != attachments) {
				VolumeAttachment attachment = attachments.get(0);
				attachPoint = attachment.getDevice();
			}
			disk = Disk.getDisk(diskId);
			DiskStatusEnum diskStatus = DiskStatusEnum.ATTACHED;
			disk.setStatus(diskStatus);
			disk.setAttachPoint(attachPoint);
			boolean merged;
			try {
				merged = disk.lastCommitWinsMerge();
			} catch (Exception e) {
				merged = false;
			}
			if (false == merged) {
				/*
				 * tell the caller to retry.
				 */
				String action = "保存云硬盘状态和挂载点信息";
				String message = "挂载云硬盘后处理过程中，保存云硬盘状态和挂载点信息发生错误，将会进行下一次尝试，虚拟机ID：" + vmId + "，云硬盘ID：" + diskId;
				return actionResultHelper.createActionResult(false, action, message, true, logger,
						ActionResultLogLevelEnum.WARN);
			}
			disk.flush();
			disk.clear();

			String action = "挂载云硬盘后处理";
			String message = "";
			return actionResultHelper.createActionResult(true, action, message, false, logger,
					ActionResultLogLevelEnum.NONE);
		} else {
			/*
			 * volume status is available (attach failed): reset disk status, clear attach time, break mapping between
			 * virtual machine and disk.
			 */

			/*
			 * save operation log
			 */
			String operator = username;
			Date operationTime = new Date();
			String operation = "监测云硬盘状态";
			OperationStatusEnum operationStatus = OperationStatusEnum.FAILED;
			ServiceNameEnum serviceName = ServiceNameEnum.VIRTURAL_MACHINE_MANAGEMENT;
			String objectId = diskId;
			String operationResult = "挂载云硬盘后处理过程中，监测到云硬盘未能进入已挂载状态，虚拟机ID：" + vmId + "，云硬盘ID：" + diskId + "，云硬盘当前状态："
					+ volume.getStatus().name();
			OperationSeverityEnum operationSeverity = OperationSeverityEnum.HIGH;
			operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
					operationResult, operationSeverity);

			/*
			 * save disk data
			 */
			DiskStatusEnum diskStatus = DiskStatusEnum.AVAILABLE;
			machine = VirtualMachine.getVirtualMachine(vmId);
			disk = Disk.getDisk(diskId);
			disk.setStatus(diskStatus);
			disk.setAttachTime(null);
			disk.setVirtualMachine(null);
			machine.getDisks().remove(disk);
			boolean merged;
			try {
				merged = disk.lastCommitWinsMerge();
			} catch (Exception e) {
				merged = false;
			}
			if (false == merged) {
				/*
				 * tell the caller to retry.
				 */
				String action = "保存云硬盘状态和挂载点信息";
				String message = "挂载云硬盘后处理过程中，保存云硬盘状态和挂载点信息发生错误，将会进行下一次尝试，虚拟机ID：" + vmId + "，云硬盘ID：" + diskId;
				return actionResultHelper.createActionResult(false, action, message, true, logger,
						ActionResultLogLevelEnum.WARN);
			}
			disk.flush();
			disk.clear();

			String action = "挂载云硬盘后处理";
			String message = operationResult;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.NONE);
		}
	}

	/**
	 * detach disk epilogue operation.
	 * 
	 * @param request
	 *            - async request
	 * @return action result
	 * @author xiangqian
	 */
	public ActionResult detachDiskEpilogue(AsyncOperationRequest request) {
		String username = request.getOperator();
		String vmId = request.getVirtualMachineId();
		String diskId = request.getDiskId();

		VirtualMachine machine = VirtualMachine.getVirtualMachine(vmId);
		if (null == machine) {
			String action = "检查虚拟机";
			String message = "卸载云硬盘后处理失败，虚拟机不存在，虚拟机ID：" + vmId;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		Disk disk = Disk.getDisk(diskId);
		if (null == disk) {
			String action = "检查云硬盘";
			String message = "卸载云硬盘后处理失败，云硬盘不存在，云硬盘ID：" + diskId;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		/*
		 * wait volume statue
		 */
		CloudManipulator cloud = CloudManipulatorFactory.createCloudManipulator(cloudConfig,
				cloudConfig.getAdminProjectId());
		List<Status> statusList = new ArrayList<Volume.Status>();
		statusList.add(Status.AVAILABLE);
		statusList.add(Status.IN_USE);
		boolean waitResult = false;
		try {
			waitResult = cloud.waitVolumeStatus(diskId, statusList, appConfig.getPortalWaitTimeout());
		} catch (InterruptedException e) {
			waitResult = false;
		}

		/*
		 * retry if wait returns without excepting result.
		 */
		if (false == waitResult) {
			String action = "监测云硬盘状态";
			String message = "卸载云硬盘后处理发生告警，在限定的时间内云硬盘没有进入期待的状态，将会进行下一次尝试，虚拟机ID：" + vmId + "，云硬盘ID：" + diskId;
			return actionResultHelper.createActionResult(false, action, message, true, logger,
					ActionResultLogLevelEnum.WARN);
		}

		/*
		 * volume is now in available or in_use status.
		 */
		Volume volume = cloud.getVolume(diskId);
		if (Status.AVAILABLE == volume.getStatus()) {
			/*
			 * volume status is available: update disk status, clear attach point, clear attach time, break mapping
			 * between virtual machine and disk.
			 */

			/*
			 * save operation log
			 */
			String operator = username;
			Date operationTime = new Date();
			String operation = "监测云硬盘状态";
			OperationStatusEnum operationStatus = OperationStatusEnum.OK;
			ServiceNameEnum serviceName = ServiceNameEnum.VIRTURAL_MACHINE_MANAGEMENT;
			String objectId = vmId;
			String operationResult = "卸载云硬盘后处理过程中，监测到云硬盘进入可用状态，虚拟机ID：" + vmId + "，云硬盘ID：" + diskId;
			OperationSeverityEnum operationSeverity = OperationSeverityEnum.LOW;
			operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
					operationResult, operationSeverity);

			/*
			 * save disk data
			 */
			DiskStatusEnum diskStatus = DiskStatusEnum.AVAILABLE;
			machine = VirtualMachine.getVirtualMachine(vmId);
			disk = Disk.getDisk(diskId);
			disk.setStatus(diskStatus);
			disk.setAttachPoint(null);
			disk.setAttachTime(null);
			disk.setVirtualMachine(null);
			machine.getDisks().remove(disk);
			boolean merged;
			try {
				merged = disk.lastCommitWinsMerge();
			} catch (Exception e) {
				merged = false;
			}
			if (false == merged) {
				/*
				 * tell the caller to retry.
				 */
				String action = "保存云硬盘数据";
				String message = "卸载云硬盘后处理过程中，保存云硬盘数据发生错误，将会进行下一次尝试，虚拟机ID：" + vmId + "，云硬盘ID：" + diskId;
				return actionResultHelper.createActionResult(false, action, message, true, logger,
						ActionResultLogLevelEnum.WARN);
			}
			disk.flush();
			disk.clear();

			String action = "卸载云硬盘后处理";
			String message = "";
			return actionResultHelper.createActionResult(true, action, message, false, logger,
					ActionResultLogLevelEnum.NONE);
		} else {
			/*
			 * volume status is in_use (detach failed): reset disk status
			 */

			/*
			 * save operation log
			 */
			String operator = username;
			Date operationTime = new Date();
			String operation = "监测云硬盘状态";
			OperationStatusEnum operationStatus = OperationStatusEnum.FAILED;
			ServiceNameEnum serviceName = ServiceNameEnum.VIRTURAL_MACHINE_MANAGEMENT;
			String objectId = vmId;
			String operationResult = "卸载云硬盘后处理过程中，监测到云硬盘未能进入可用状态，虚拟机ID：" + vmId + "，云硬盘ID：" + diskId + "，云硬盘当前状态："
					+ volume.getStatus().name();
			OperationSeverityEnum operationSeverity = OperationSeverityEnum.HIGH;
			operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
					operationResult, operationSeverity);

			/*
			 * sava disk status
			 */
			DiskStatusEnum diskStatus = DiskStatusEnum.ATTACHED;
			disk = Disk.getDisk(diskId);
			disk.setStatus(diskStatus);
			boolean merged;
			try {
				merged = disk.lastCommitWinsMerge();
			} catch (Exception e) {
				merged = false;
			}
			if (false == merged) {
				/*
				 * tell the caller to retry.
				 */
				String action = "保存云硬盘状态";
				String message = "卸载云硬盘后处理过程中，保存云硬盘状态发生错误，将会进行下一次尝试，虚拟机ID：" + vmId + "，云硬盘ID：" + diskId;
				return actionResultHelper.createActionResult(false, action, message, true, logger,
						ActionResultLogLevelEnum.WARN);
			}
			disk.flush();
			disk.clear();

			String action = "卸载云硬盘后处理";
			String message = operationResult;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.NONE);
		}
	}

	/**
	 * live migrate virtual machine epilogue operation
	 * 
	 * @param request
	 *            - async request
	 * @return action result
	 * @author xiangqian
	 */
	public ActionResult liveMigrateVirtualMachineEpilogue(AsyncOperationRequest request) {
		String username = request.getOperator();
		String vmId = request.getVirtualMachineId();
		String hypervisorName = request.getHypervisorName();

		VirtualMachine machine = VirtualMachine.getVirtualMachine(vmId);
		if (null == machine) {
			String action = "检查虚拟机";
			String message = "在线迁移虚拟机后处理失败，虚拟机不存在，虚拟机ID：" + vmId;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		/*
		 * wait server status
		 */
		CloudManipulator cloud = CloudManipulatorFactory.createCloudManipulator(cloudConfig, machine.getDomainId());
		List<Server.Status> statusList = new ArrayList<Server.Status>();
		statusList.add(Server.Status.ACTIVE);
		boolean waitResult = false;
		try {
			waitResult = cloud.waitServerStatus(vmId, statusList, appConfig.getPortalWaitTimeout());
		} catch (InterruptedException e) {
			waitResult = false;
		}

		/*
		 * retry if wait returns without excepting result.
		 */
		if (false == waitResult) {
			String action = "监测虚拟机状态";
			String message = "在线迁移虚拟机后处理发生告警，在限定的时间内虚拟机没有进入期待的状态，将会进行下一次尝试，虚拟机ID：" + vmId + "，虚拟机名称："
					+ machine.getHostName();
			return actionResultHelper.createActionResult(false, action, message, true, logger,
					ActionResultLogLevelEnum.WARN);
		}

		/*
		 * server is now in active status.
		 */

		/*
		 * update machine status.
		 */
		VirtualMachineStatusEnum machineStatus = VirtualMachineStatusEnum.ACTIVE;
		VirtualMachineTaskStatusEnum taskStatus = VirtualMachineTaskStatusEnum.NONE;
		machine = VirtualMachine.getVirtualMachine(vmId);
		machine.setStatus(machineStatus);
		machine.setTaskStatus(taskStatus);
		boolean merged;
		try {
			merged = machine.lastCommitWinsMerge();
		} catch (Exception e) {
			merged = false;
		}
		if (false == merged) {
			/*
			 * tell the caller to retry.
			 */
			String action = "保存虚拟机状态";
			String message = "在线迁移虚拟机后处理过程中，保存虚拟机状态发生错误，将会进行下一次尝试，虚拟机ID：" + vmId + "，虚拟机名称：" + machine.getHostName();
			return actionResultHelper.createActionResult(false, action, message, true, logger,
					ActionResultLogLevelEnum.WARN);
		}
		machine.flush();
		machine.clear();

		/*
		 * check if the migration success
		 */
		boolean verifyResult = verifyLiveMigrationResult(vmId, hypervisorName);
		if (true == verifyResult) {
			machine = VirtualMachine.getVirtualMachine(vmId);
			String oldHypervisorName = machine.getHypervisorName();

			/*
			 * save operation log
			 */
			String operator = username;
			Date operationTime = new Date();
			String operation = "检查在线迁移结果";
			OperationStatusEnum operationStatus = OperationStatusEnum.OK;
			ServiceNameEnum serviceName = ServiceNameEnum.VIRTURAL_MACHINE_MANAGEMENT;
			String objectId = vmId;
			String operationResult = "在线迁移虚拟机后处理过程中，确认虚拟机迁移完成，虚拟机ID：" + vmId + "，迁移前所在主机：" + oldHypervisorName
					+ "，迁移后所在主机：" + hypervisorName;
			OperationSeverityEnum operationSeverity = OperationSeverityEnum.LOW;
			operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
					operationResult, operationSeverity);

			/*
			 * update hypervisor name.
			 */
			machine.setHypervisorName(hypervisorName);
			try {
				merged = machine.lastCommitWinsMerge();
			} catch (Exception e) {
				merged = false;
			}
			if (false == merged) {
				/*
				 * tell the caller to retry.
				 */
				String action = "保存虚拟机所在物理服务器信息";
				String message = "在线迁移虚拟机后处理过程中，保存虚拟机所在物理服务器信息发生错误，将会进行下一次尝试，虚拟机ID：" + vmId + "，虚拟机名称："
						+ machine.getHostName();
				return actionResultHelper.createActionResult(false, action, message, true, logger,
						ActionResultLogLevelEnum.WARN);
			}
			machine.flush();
			machine.clear();

			String action = "在线迁移虚拟机后处理";
			String message = "";
			return actionResultHelper.createActionResult(true, action, message, false, logger,
					ActionResultLogLevelEnum.NONE);
		} else {
			// save operation log
			String operator = username;
			Date operationTime = new Date();
			String operation = "检查在线迁移结果";
			OperationStatusEnum operationStatus = OperationStatusEnum.FAILED;
			ServiceNameEnum serviceName = ServiceNameEnum.VIRTURAL_MACHINE_MANAGEMENT;
			String objectId = vmId;
			String operationResult = "在线迁移虚拟机后处理过程中，确认虚拟机迁移失败，虚拟机仍然运行在原来的主机上，虚拟机ID：" + vmId + "，虚拟机名称："
					+ machine.getHostName();
			OperationSeverityEnum operationSeverity = OperationSeverityEnum.HIGH;
			operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
					operationResult, operationSeverity);

			String action = "在线迁移虚拟机后处理";
			String message = operationResult;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.NONE);
		}
	}

	private boolean verifyLiveMigrationResult(String vmId, String hypervisorName) {
		VirtualMachine machine = VirtualMachine.getVirtualMachine(vmId);
		if (null == machine) {
			return false;
		}

		CloudManipulator cloud = CloudManipulatorFactory.createCloudManipulator(cloudConfig, machine.getDomainId());
		ServerInfo machineInfo = cloud.getServerInfo(vmId);
		if (null == machineInfo) {
			return false;
		}

		if (false == machineInfo.getPhysicalMachine().equalsIgnoreCase(hypervisorName)) {
			return false;
		}

		return true;
	}
}
