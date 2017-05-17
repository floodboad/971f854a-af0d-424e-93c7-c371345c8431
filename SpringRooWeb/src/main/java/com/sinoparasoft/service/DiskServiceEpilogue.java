package com.sinoparasoft.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openstack4j.model.storage.block.Volume;
import org.openstack4j.model.storage.block.Volume.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sinoparasoft.common.ActionResult;
import com.sinoparasoft.common.ActionResultLogLevelEnum;
import com.sinoparasoft.common.AppConfig;
import com.sinoparasoft.common.AsyncOperationRequest;
import com.sinoparasoft.enumerator.DiskStatusEnum;
import com.sinoparasoft.enumerator.OperationSeverityEnum;
import com.sinoparasoft.enumerator.OperationStatusEnum;
import com.sinoparasoft.enumerator.ServiceNameEnum;
import com.sinoparasoft.model.Disk;
import com.sinoparasoft.openstack.CloudManipulator;
import com.sinoparasoft.openstack.CloudManipulatorFactory;
import com.sinoparasoft.openstack.type.CloudConfig;
import com.sinoparasoft.util.ActionResultHelper;

@Service
public class DiskServiceEpilogue {
	private static Logger logger = LoggerFactory.getLogger(DiskServiceEpilogue.class);

	@Autowired
	CloudConfig cloudConfig;

	@Autowired
	AppConfig appConfig;

	@Autowired
	OperationLogService operationLogService;

	@Autowired
	ActionResultHelper actionResultHelper;

	/**
	 * create disk epilogue operation
	 * 
	 * @param request
	 *            - asyn request
	 * @return action result
	 * @author xiangqian
	 */
	public ActionResult createDiskEpilogue(AsyncOperationRequest request) {
		String username = request.getOperator();
		String diskId = request.getDiskId();

		Disk disk = Disk.getDisk(diskId);
		if (null == disk) {
			String action = "检查云硬盘";
			String message = "创建云硬盘后处理失败，云硬盘不存在，云硬盘ID：" + diskId;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		/*
		 * wait volume status.
		 */
		CloudManipulator cloud = CloudManipulatorFactory.createCloudManipulator(cloudConfig,
				cloudConfig.getAdminProjectId());
		List<Status> statusList = new ArrayList<Volume.Status>();
		statusList.add(Status.AVAILABLE);
		statusList.add(Status.ERROR);
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
			String message = "创建云硬盘后处理发生告警，在限定的时间内云硬盘没有进入期待的状态，将会进行下一次尝试，云硬盘ID：" + diskId;
			return actionResultHelper.createActionResult(false, action, message, true, logger,
					ActionResultLogLevelEnum.WARN);
		}

		/*
		 * volume is now in available or error status.
		 */
		Volume volume = cloud.getVolume(diskId);
		if (Status.AVAILABLE == volume.getStatus()) {
			/*
			 * save operation log
			 */
			String operator = username;
			Date operationTime = new Date();
			String operation = "监测云硬盘状态";
			OperationStatusEnum operationStatus = OperationStatusEnum.OK;
			ServiceNameEnum serviceName = ServiceNameEnum.DISK_MANAGEMENT;
			String objectId = diskId;
			String operationResult = "创建云硬盘后处理过程中，监测到云硬盘进入可用状态，云硬盘ID：" + diskId;
			OperationSeverityEnum operationSeverity = OperationSeverityEnum.LOW;
			operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
					operationResult, operationSeverity);

			/*
			 * update disk status
			 */
			disk = Disk.getDisk(diskId);
			disk.setStatus(DiskStatusEnum.AVAILABLE);
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
				String message = "创建云硬盘后处理过程中，保存云硬盘状态发生错误，将会进行下一次尝试，云硬盘ID：" + diskId;
				return actionResultHelper.createActionResult(false, action, message, true, logger,
						ActionResultLogLevelEnum.WARN);
			}
			disk.flush();
			disk.clear();

			String action = "创建云硬盘后处理";
			String message = "";
			return actionResultHelper.createActionResult(true, action, message, false, logger,
					ActionResultLogLevelEnum.NONE);
		} else {
			/*
			 * save operation log
			 */
			String operator = username;
			Date operationTime = new Date();
			String operation = "监测云硬盘状态";
			OperationStatusEnum operationStatus = OperationStatusEnum.FAILED;
			ServiceNameEnum serviceName = ServiceNameEnum.DISK_MANAGEMENT;
			String objectId = diskId;
			String operationResult = "创建云硬盘后处理过程中，监测到云硬盘未能进入可用状态，云硬盘ID：" + diskId + "，云硬盘当前状态："
					+ volume.getStatus().name();
			OperationSeverityEnum operationSeverity = OperationSeverityEnum.HIGH;
			operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
					operationResult, operationSeverity);

			/*
			 * update disk status
			 */
			disk = Disk.getDisk(diskId);
			disk.setStatus(DiskStatusEnum.ERROR);
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
				String message = "创建云硬盘后处理过程中，保存云硬盘状态发生错误，将会进行下一次尝试，云硬盘ID：" + diskId;
				return actionResultHelper.createActionResult(false, action, message, true, logger,
						ActionResultLogLevelEnum.WARN);
			}
			disk.flush();
			disk.clear();

			String action = "创建云硬盘后处理";
			String message = operationResult;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}
	}

	/**
	 * delete disk epilogue operation.
	 * 
	 * @param request
	 *            - asyn request
	 * @return action result
	 * @author xiangqian
	 */
	public ActionResult deleteDiskEpilogue(AsyncOperationRequest request) {
		String username = request.getOperator();
		String diskId = request.getDiskId();

		Disk disk = Disk.getDisk(diskId);
		if (null == disk) {
			String action = "检查云硬盘";
			String message = "创建云硬盘后处理失败，云硬盘不存在，云硬盘ID：" + diskId;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		/*
		 * wait volume been deleted
		 */
		CloudManipulator cloud = CloudManipulatorFactory.createCloudManipulator(cloudConfig,
				cloudConfig.getAdminProjectId());
		boolean waitResult;
		try {
			waitResult = cloud.waitVolumeDeleted(diskId, appConfig.getPortalWaitTimeout());
		} catch (InterruptedException e) {
			waitResult = false;
		}

		/*
		 * retry if wait returns without excepting result.
		 */
		if (false == waitResult) {
			String action = "监测云硬盘状态";
			String message = "删除云硬盘后处理发生告警，在限定的时间内云硬盘没有被删除，将会进行下一次尝试，云硬盘ID：" + diskId;
			return actionResultHelper.createActionResult(false, action, message, true, logger,
					ActionResultLogLevelEnum.WARN);
		}

		/*
		 * save operation log
		 */
		String operator = username;
		Date operationTime = new Date();
		String operation = "监测云硬盘状态";
		OperationStatusEnum operationStatus = OperationStatusEnum.OK;
		ServiceNameEnum serviceName = ServiceNameEnum.DISK_MANAGEMENT;
		String objectId = diskId;
		String operationResult = "删除云硬盘后处理过程中，监测到云硬盘删除成功，云硬盘ID：" + diskId;
		OperationSeverityEnum operationSeverity = OperationSeverityEnum.LOW;
		operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
				operationResult, operationSeverity);

		/*
		 * update disk status: set deleted if volume is deleted
		 */
		disk = Disk.getDisk(diskId);
		disk.setStatus(DiskStatusEnum.DELETED);
		disk.setDeleteTime(new Date());
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
			String message = "删除云硬盘后处理过程中，保存云硬盘数据发生错误，将会进行下一次尝试，云硬盘ID：" + diskId;
			return actionResultHelper.createActionResult(false, action, message, true, logger,
					ActionResultLogLevelEnum.WARN);
		}
		disk.flush();
		disk.clear();

		String action = "删除云硬盘后处理";
		String message = "";
		return actionResultHelper.createActionResult(true, action, message, false, logger,
				ActionResultLogLevelEnum.NONE);
	}
}
