package com.sinoparasoft.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.joda.time.DateTime;
import org.openstack4j.model.storage.block.Volume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinoparasoft.common.ActionResult;
import com.sinoparasoft.common.ActionResultLogLevelEnum;
import com.sinoparasoft.common.AsyncOperationRequest;
import com.sinoparasoft.common.AsyncOperationTypeEnum;
import com.sinoparasoft.common.BatchActionResult;
import com.sinoparasoft.enumerator.DiskStatusEnum;
import com.sinoparasoft.enumerator.OperationSeverityEnum;
import com.sinoparasoft.enumerator.OperationStatusEnum;
import com.sinoparasoft.enumerator.ServiceNameEnum;
import com.sinoparasoft.message.RetryMessageProducer;
import com.sinoparasoft.model.Disk;
import com.sinoparasoft.model.User;
import com.sinoparasoft.model.VirtualMachine;
import com.sinoparasoft.openstack.CloudManipulator;
import com.sinoparasoft.openstack.CloudManipulatorFactory;
import com.sinoparasoft.openstack.type.CloudConfig;
import com.sinoparasoft.type.MQConfig;
import com.sinoparasoft.util.ActionResultHelper;

@Service
public class DiskServiceImpl implements DiskService {
	private static Logger logger = LoggerFactory.getLogger(DiskServiceImpl.class);

	@Autowired
	CloudConfig cloudConfig;

	@Autowired
	OperationLogService operationLogService;

	@Autowired
	MQConfig mqConfig;

	@Autowired
	RetryMessageProducer messageProducer;

	@Autowired
	ActionResultHelper actionResultHelper;

	public List<com.sinoparasoft.type.Disk> getList(List<String> diskIds) {
		List<com.sinoparasoft.type.Disk> diskList = new ArrayList<com.sinoparasoft.type.Disk>();

		for (String diskId : diskIds) {
			Disk disk = Disk.getDisk(diskId);
			com.sinoparasoft.type.Disk diskValue = buildDiskValue(disk);

			diskList.add(diskValue);
		}

		return diskList;
	}

	/**
	 * build disk value can be used in json format.
	 * 
	 * @param disk
	 *            - disk model
	 * @return disk value
	 * @author xiangqian
	 */
	private com.sinoparasoft.type.Disk buildDiskValue(Disk disk) {
		com.sinoparasoft.type.Disk diskValue = new com.sinoparasoft.type.Disk();
		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		diskValue.setId(disk.getDiskId());
		diskValue.setName(disk.getDiskName());
		diskValue.setDescription(disk.getDescription());
		diskValue.setCreator(disk.getCreator());
		diskValue.setCapacity(disk.getCapacity());
		if (null != disk.getManager()) {
			diskValue.setManager(disk.getManager());
		} else {
			diskValue.setManager("");
		}
		if (null != disk.getCreateTime()) {
			diskValue.setCreateTime(dateFormatter.format(disk.getCreateTime()));
		}
		if (null != disk.getModifyTime()) {
			diskValue.setModifyTime(dateFormatter.format(disk.getModifyTime()));
		}
		if (null != disk.getValidTime()) {
			diskValue.setValidTime(dateFormatter.format(disk.getValidTime()));

			// joda datetime type
			diskValue.setExpired(false);
			DateTime validTime = new DateTime(disk.getValidTime());
			if (validTime.isBeforeNow()) {
				diskValue.setExpired(true);
			}
		}

		String diskStatus = null;
		switch (disk.getStatus()) {
		case ATTACHED:
			diskStatus = "已挂载";
			break;
		case ATTACHING:
			diskStatus = "挂载中";
			break;
		case AVAILABLE:
			diskStatus = "可用";
			break;
		case CREATING:
			diskStatus = "创建中";
			break;
		case DELETED:
			diskStatus = "已删除";
			break;
		case DELETING:
			diskStatus = "删除中";
			break;
		case DETATCHING:
			diskStatus = "卸载中";
			break;
		case ERROR:
			diskStatus = "故障";
			break;
		case UNKNOWN:
			diskStatus = "未知";
			break;
		default:
			break;
		}

		String attachHost = "";
		VirtualMachine machine = disk.getVirtualMachine();
		if (null != machine) {
			attachHost = machine.getHostName();
		} else {
			attachHost = "未知";
		}

		diskValue.setStatus(diskStatus);
		diskValue.setAttachHost(attachHost);
		if (null != disk.getAttachPoint()) {
			diskValue.setAttachPoint(disk.getAttachPoint());
		} else {
			diskValue.setAttachPoint("");
		}
		if (null != disk.getAttachTime()) {
			diskValue.setAttachTime(dateFormatter.format(disk.getAttachTime()));
		}

		return diskValue;
	}

	public ActionResult createDisk(String username, String diskName, String description, Integer capacity,
			String manager, int aliveDays) {
		if (false == messageProducer.isOpen()) {
			String action = "检查消息连接状态";
			String message = "创建云硬盘失败，消息连接已关闭，请联系运维人员进行处理";
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		/*
		 * send create request. TODO volume API does not support Chinese characters parameters.
		 */
		CloudManipulator cloud = CloudManipulatorFactory.createCloudManipulator(cloudConfig,
				cloudConfig.getAdminProjectId());
		String rangdomUuidString = UUID.randomUUID().toString();
		String diskNameInCloud = "disk-" + rangdomUuidString;
		String descriptionInCloud = "description-" + rangdomUuidString;
		Volume volume = cloud.createVolume(diskNameInCloud, descriptionInCloud, capacity);

		String diskId = volume.getId();

		/*
		 * save operation log
		 */
		String operator = username;
		Date operationTime = new Date();
		String operation = "发送创建云硬盘请求";
		OperationStatusEnum operationStatus = OperationStatusEnum.OK;
		ServiceNameEnum serviceName = ServiceNameEnum.DISK_MANAGEMENT;
		String objectId = diskId;
		String operationResult = "发送创建云硬盘请求成功，云硬盘ID：" + diskId;
		OperationSeverityEnum operationSeverity = OperationSeverityEnum.LOW;
		operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
				operationResult, operationSeverity);

		/*
		 * save disk data
		 */
		Disk disk = new Disk();
		disk.setDiskId(diskId);
		disk.setDiskName(diskName);
		disk.setDescription(description);
		disk.setCapacity(capacity);
		disk.setCreator(username);
		disk.setCreateTime(new Date());
		if (manager.equalsIgnoreCase("none") == false) {
			disk.setManager(manager);
		}
		disk.setStatus(DiskStatusEnum.CREATING);
		DateTime aliveDateTime = new DateTime().plusDays(aliveDays);
		Date aliveDate = aliveDateTime.toDate();
		disk.setValidTime(aliveDate);
		disk.persist();
		disk.flush();
		disk.clear();

		/*
		 * save operation log
		 */
		operationTime = new Date();
		operation = "保存云硬盘数据";
		operationStatus = OperationStatusEnum.OK;
		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		operationResult = "保存云硬盘数据成功，云硬盘ID：" + diskId + "，云硬盘名称：" + diskName + "，描述：" + description + "，容量：" + capacity
				+ "GB，管理员：" + manager + "，有效期：" + dateFormatter.format(aliveDate);
		operationSeverity = OperationSeverityEnum.LOW;
		operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
				operationResult, operationSeverity);

		/*
		 * send message to message queue
		 */
		AsyncOperationRequest request = new AsyncOperationRequest();
		request.setOperationType(AsyncOperationTypeEnum.CREATE_DISK);
		request.setOperator(username);
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
			operation = "发送创建云硬盘后处理请求";
			operationStatus = OperationStatusEnum.FAILED;
			operationResult = "创建云硬盘失败，发送后处理请求发生错误，云硬盘ID：" + diskId;
			operationSeverity = OperationSeverityEnum.HIGH;
			operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
					operationResult, operationSeverity);

			String action = operation;
			String message = operationResult;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		String action = "创建云硬盘";
		String message = "";
		return actionResultHelper.createActionResult(true, action, message, false, logger,
				ActionResultLogLevelEnum.NONE);
	}

	public BatchActionResult deleteDisks(String username, List<String> diskIds) {
		BatchActionResult result = new BatchActionResult();
		boolean success = false;
		String message = "";
		List<String> finishedList = new ArrayList<String>();
		List<String> failedList = new ArrayList<String>();

		String operator = username;
		Date operationTime;
		String operation;
		OperationStatusEnum operationStatus;
		ServiceNameEnum serviceName = ServiceNameEnum.DISK_MANAGEMENT;
		String objectId;
		String operationResult;
		OperationSeverityEnum operationSeverity;

		CloudManipulator cloud = CloudManipulatorFactory.createCloudManipulator(cloudConfig,
				cloudConfig.getAdminProjectId());
		for (String diskId : diskIds) {
			try {
				ActionResult deleteResult = deleteDisk(cloud, username, diskId);
				if (true == deleteResult.isSuccess()) {
					finishedList.add(diskId);
				} else {
					/*
					 * save operation log
					 */
					operationTime = new Date();
					operation = deleteResult.getAction();
					operationStatus = OperationStatusEnum.FAILED;
					objectId = diskId;
					operationResult = deleteResult.getMessage();
					operationSeverity = OperationSeverityEnum.HIGH;
					operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName,
							objectId, operationResult, operationSeverity);

					message += operationResult;
					message += "；";
					failedList.add(diskId);
				}
			} catch (Exception e) {
				/*
				 * save operation log
				 */
				operationTime = new Date();
				operation = "删除云硬盘";
				operationStatus = OperationStatusEnum.FAILED;
				objectId = diskId;
				operationResult = "删除云硬盘失败，删除云硬盘过程中发生错误，云硬盘ID：" + diskId;
				operationSeverity = OperationSeverityEnum.HIGH;
				operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
						operationResult, operationSeverity);

				// suppress exception so we can continue with next deletion
				message += operationResult;
				message += "；";
				failedList.add(diskId);

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
	 * delete disk.
	 * 
	 * @param diskId
	 *            - disk id
	 * @return action result
	 */
	private ActionResult deleteDisk(CloudManipulator cloud, String username, String diskId) {
		if (false == messageProducer.isOpen()) {
			String action = "检查消息连接状态";
			String message = "删除云硬盘失败，消息连接已关闭，请联系运维人员";
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		Disk disk = Disk.getDisk(diskId);
		if (null == disk) {
			String action = "检查云硬盘";
			String message = "删除云硬盘失败，云硬盘不存在，云硬盘ID：" + diskId;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		/*
		 * check disk status, only available disk is allowed to delete.
		 */
		if (DiskStatusEnum.AVAILABLE != disk.getStatus()) {
			String action = "检查云硬盘状态";
			String message = "删除云硬盘失败，不允许删除非可用状态的云硬盘，云硬盘ID：" + diskId + "， 云硬盘状态：" + disk.getStatus().name();
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		// VirtualMachine machine = disk.getVirtualMachine();
		// if (null != machine) {
		// /*
		// * disk is in attached state, it should not be like that!!!
		// */
		// String action = "检查云硬盘挂载状态";
		// String message = "删除云硬盘失败，云硬盘仍然挂载在虚拟机上，云硬盘ID：" + diskId + "，云硬盘名称：" + disk.getDiskName() + "，挂载虚拟机名称："
		// + machine.getHostName();
		// return actionResultHelper.createActionResult(false, action, message, false, logger,
		// ActionResultLogLevelEnum.ERROR);
		// }

		boolean deleted = cloud.deleteVolume(diskId);
		if (false == deleted) {
			String action = "发送删除云硬盘请求";
			String message = "删除云硬盘失败，发送删除云硬盘请求失败，云硬盘ID：" + diskId;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		/*
		 * save operation log
		 */
		String operator = username;
		Date operationTime = new Date();
		String operation = "发送删除云硬盘请求";
		OperationStatusEnum operationStatus = OperationStatusEnum.OK;
		ServiceNameEnum serviceName = ServiceNameEnum.DISK_MANAGEMENT;
		String objectId = diskId;
		String operationResult = "发送删除云硬盘请求成功，云硬盘ID：" + diskId;
		OperationSeverityEnum operationSeverity = OperationSeverityEnum.HIGH;
		operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
				operationResult, operationSeverity);

		/*
		 * save disk status
		 */
		disk.setStatus(DiskStatusEnum.DELETING);
		boolean merged;
		try {
			merged = disk.lastCommitWinsMerge();
		} catch (Exception e) {
			merged = false;
		}
		if (false == merged) {
			/*
			 * the caller is responsible to save operation log.
			 */
			String action = "保存云硬盘状态";
			String message = "删除云硬盘时，保存云硬盘状态发生错误，请联系运维人员进行处理，云硬盘ID：" + diskId;
			return actionResultHelper.createActionResult(false, action, message, true, logger,
					ActionResultLogLevelEnum.WARN);
		}
		disk.flush();
		disk.clear();

		/*
		 * send message to message queue
		 */
		AsyncOperationRequest request = new AsyncOperationRequest();
		request.setOperationType(AsyncOperationTypeEnum.DELETE_DISK);
		request.setOperator(username);
		request.setDiskId(diskId);
		request.setRequestTime(new Date());

		ObjectMapper mapper = new ObjectMapper();
		try {
			String requestBody = mapper.writeValueAsString(request);
			messageProducer.publish(requestBody);
		} catch (Exception e) {
			/*
			 * the caller is responsible to save operation log.
			 */
			String action = "发送删除云硬盘后处理请求";
			String message = "删除云硬盘时，发送后处理请求发生错误，请联系运维人员进行处理，云硬盘ID：" + diskId;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		String action = "删除云硬盘";
		String message = "";
		return actionResultHelper.createActionResult(true, action, message, false, logger,
				ActionResultLogLevelEnum.NONE);
	}

	public com.sinoparasoft.type.Disk getDiskById(String diskId) {
		com.sinoparasoft.type.Disk diskValue = new com.sinoparasoft.type.Disk();

		Disk disk = Disk.getDisk(diskId);
		if (null == disk) {
			return null;
		}

		diskValue = buildDiskValue(disk);

		return diskValue;
	}

	public ActionResult modifyDisk(String username, String diskId, String diskName, String description) {
		Disk disk = Disk.getDisk(diskId);
		if (null == disk) {
			String action = "检查云硬盘";
			String message = "修改云硬盘失败，云硬盘不存在，云硬盘ID：" + diskId;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		String originalName = disk.getDiskName();
		String originalDescription = disk.getDescription();

		/*
		 * TODO volume API does not support Chinese characters parameters.
		 */
		// CloudManipulator cloud = CloudManipulatorFactory.createCloudManipulator(cloudConfig,
		// cloudConfig.getAdminProjectId());
		// boolean modified = cloud.modifyVolume(diskId, diskName, description);
		// if (false == modified) {
		// return false;
		// }

		/*
		 * save disk data
		 */
		disk = Disk.getDisk(diskId);
		disk.setDiskName(diskName);
		disk.setDescription(description);
		disk.setModifyTime(new Date());
		disk.merge();
		disk.flush();
		disk.clear();

		/*
		 * save operation log
		 */
		String operator = username;
		Date operationTime = new Date();
		String operation = "修改云硬盘信息";
		OperationStatusEnum operationStatus = OperationStatusEnum.OK;
		ServiceNameEnum serviceName = ServiceNameEnum.DISK_MANAGEMENT;
		String objectId = diskId;
		String operationResult = "修改云硬盘信息成功，云硬盘ID：" + diskId + "，初始名称：" + originalName + "，初始描述：" + originalDescription
				+ "，修改后的名称：" + diskName + "，修改后的描述：" + description;
		OperationSeverityEnum operationSeverity = OperationSeverityEnum.MIDDLE;
		operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
				operationResult, operationSeverity);

		String action = "修改云硬盘信息";
		String message = "";
		return actionResultHelper.createActionResult(true, action, message, false, logger,
				ActionResultLogLevelEnum.NONE);
	}

	public ActionResult setManager(String username, String diskId, String manager) {
		Disk disk = Disk.getDisk(diskId);
		if (null == disk) {
			String action = "检查云硬盘";
			String message = "设置云硬盘管理员失败，云硬盘不存在，云硬盘ID：" + diskId;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		/*
		 * save disk data
		 */
		if (manager.equalsIgnoreCase("none") == true) {
			disk.setManager(null);
		} else {
			User user = User.getUserByUsername(manager);
			if (user == null) {
				String action = "检查管理员";
				String message = "设置云硬盘管理员失败，管理员不存在，云硬盘ID：" + diskId + "，管理员：" + manager;
				return actionResultHelper.createActionResult(false, action, message, false, logger,
						ActionResultLogLevelEnum.ERROR);
			}

			disk.setManager(manager);
		}
		disk.merge();
		disk.flush();
		disk.clear();

		/*
		 * save operation log
		 */
		String operator = username;
		Date operationTime = new Date();
		String operation = "设置云硬盘管理员";
		OperationStatusEnum operationStatus = OperationStatusEnum.OK;
		ServiceNameEnum serviceName = ServiceNameEnum.DISK_MANAGEMENT;
		String objectId = diskId;
		String operationResult = "设置云硬盘管理员成功，云硬盘ID：" + diskId + "，管理员：" + manager;
		OperationSeverityEnum operationSeverity = OperationSeverityEnum.MIDDLE;
		operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
				operationResult, operationSeverity);

		String action = "设置云硬盘管理员";
		String message = "";
		return actionResultHelper.createActionResult(true, action, message, false, logger,
				ActionResultLogLevelEnum.NONE);
	}

	public ActionResult setValidTime(String username, String diskId, int aliveDays) {
		Disk disk = Disk.getDisk(diskId);
		if (null == disk) {
			String action = "检查云硬盘";
			String message = "设置云硬盘有效期失败，云硬盘不存在，云硬盘ID：" + diskId;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		/*
		 * save disk data
		 */
		DateTime aliveDateTime;
		if (null == disk.getValidTime()) {
			aliveDateTime = new DateTime().plusDays(aliveDays);
		} else {
			aliveDateTime = new DateTime(disk.getValidTime()).plusDays(aliveDays);
		}
		Date aliveDate = aliveDateTime.toDate();
		disk.setValidTime(aliveDate);
		disk.merge();
		disk.flush();
		disk.clear();

		/*
		 * save operation log
		 */
		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String operator = username;
		Date operationTime = new Date();
		String operation = "设置云硬盘有效期";
		OperationStatusEnum operationStatus = OperationStatusEnum.OK;
		ServiceNameEnum serviceName = ServiceNameEnum.DISK_MANAGEMENT;
		String objectId = diskId;
		String operationResult = "设置云硬盘有效期成功，云硬盘ID：" + diskId + "，有效期截止：" + dateFormatter.format(aliveDate);
		OperationSeverityEnum operationSeverity = OperationSeverityEnum.MIDDLE;
		operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
				operationResult, operationSeverity);

		String action = "设置云硬盘有效期";
		String message = "";
		return actionResultHelper.createActionResult(true, action, message, false, logger,
				ActionResultLogLevelEnum.NONE);
	}

	public List<com.sinoparasoft.type.Disk> getUserAvailableDisks(String manager) {
		List<com.sinoparasoft.type.Disk> diskList = new ArrayList<com.sinoparasoft.type.Disk>();

		List<Disk> disks = Disk.getAvailableDisksByManager(manager);
		for (Disk disk : disks) {
			com.sinoparasoft.type.Disk diskValue = buildDiskValue(disk);
			diskList.add(diskValue);
		}

		return diskList;
	}

	public List<com.sinoparasoft.type.Disk> getDisksByVirtualMachine(String vmId) {
		List<com.sinoparasoft.type.Disk> diskList = new ArrayList<com.sinoparasoft.type.Disk>();

		VirtualMachine machine = VirtualMachine.getVirtualMachine(vmId);
		if (null == machine) {
			return null;
		}

		// sort disk by createTime in desc order
		Set<Disk> diskSet = machine.getDisks();
		List<Disk> disks = new ArrayList<Disk>(diskSet);
		Collections.sort(disks, new Comparator<Disk>() {
			@Override
			public int compare(Disk o1, Disk o2) {
				return (-1) * o1.getCreateTime().compareTo(o2.getCreateTime());
			}
		});

		for (Disk disk : disks) {
			com.sinoparasoft.type.Disk diskValue = getDiskById(disk.getDiskId());
			if (null != diskValue) {
				diskList.add(diskValue);
			}
		}

		return diskList;
	}
}
