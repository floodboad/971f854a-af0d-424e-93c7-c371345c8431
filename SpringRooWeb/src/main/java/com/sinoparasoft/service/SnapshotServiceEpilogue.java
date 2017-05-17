package com.sinoparasoft.service;

import java.util.Date;

import org.openstack4j.model.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sinoparasoft.common.ActionResult;
import com.sinoparasoft.common.ActionResultLogLevelEnum;
import com.sinoparasoft.common.AppConfig;
import com.sinoparasoft.common.AsyncOperationRequest;
import com.sinoparasoft.enumerator.OperationSeverityEnum;
import com.sinoparasoft.enumerator.OperationStatusEnum;
import com.sinoparasoft.enumerator.ServiceNameEnum;
import com.sinoparasoft.enumerator.SnapshotStatusEnum;
import com.sinoparasoft.enumerator.VirtualMachineTaskStatusEnum;
import com.sinoparasoft.model.Snapshot;
import com.sinoparasoft.model.VirtualMachine;
import com.sinoparasoft.openstack.CloudManipulator;
import com.sinoparasoft.openstack.CloudManipulatorFactory;
import com.sinoparasoft.openstack.type.CloudConfig;
import com.sinoparasoft.util.ActionResultHelper;

@Service
public class SnapshotServiceEpilogue {
	private static Logger logger = LoggerFactory.getLogger(SnapshotServiceEpilogue.class);

	@Autowired
	CloudConfig cloudConfig;

	@Autowired
	AppConfig appConfig;

	@Autowired
	OperationLogService operationLogService;

	@Autowired
	ActionResultHelper actionResultHelper;

	/**
	 * create snapshot epilogue operation.
	 * 
	 * @param request
	 *            - asyn request
	 * @return action result
	 * @author xiangqian
	 * @throws InterruptedException
	 */
	public ActionResult createSnapshotEpilogue(AsyncOperationRequest request) {
		String username = request.getOperator();
		String vmId = request.getVirtualMachineId();
		String snapshotId = request.getImageId();

		VirtualMachine machine = VirtualMachine.getVirtualMachine(vmId);
		if (null == machine) {
			String action = "检查虚拟机";
			String message = "创建快照后处理失败，虚拟机不存在，虚拟机ID：" + vmId + "，快照ID：" + snapshotId;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		Snapshot snapshot = Snapshot.getSnapshot(snapshotId);
		if (null == snapshot) {
			String action = "检查快照";
			String message = "创建快照后处理失败，快照不存在，虚拟机ID：" + vmId + "，快照ID：" + snapshotId;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		/*
		 * wait image status.
		 */
		CloudManipulator cloud = CloudManipulatorFactory.createCloudManipulator(cloudConfig,
				cloudConfig.getAdminProjectId());
		Image.Status status = Image.Status.ACTIVE;
		boolean waitResult;
		try {
			waitResult = cloud.waitImageStatus(snapshotId, status, appConfig.getPortalWaitTimeout());
		} catch (InterruptedException e) {
			waitResult = false;
		}

		/*
		 * retry if wait returns without excepting result.
		 */
		if (false == waitResult) {
			String action = "监测快照状态";
			String message = "创建快照后处理发生告警，在限定的时间内快照没有进入期待的状态，将会进行下一次尝试，虚拟机ID：" + vmId + "，快照ID：" + snapshotId;
			return actionResultHelper.createActionResult(false, action, message, true, logger,
					ActionResultLogLevelEnum.WARN);
		}

		/*
		 * save operation log
		 */
		String operator = username;
		Date operationTime = new Date();
		String operation = "监测快照状态";
		OperationStatusEnum operationStatus = OperationStatusEnum.OK;
		ServiceNameEnum serviceName = ServiceNameEnum.SNAPSHOT_MANAGEMENT;
		String objectId = snapshotId;
		String operationResult = "创建快照后处理过程中，监测到快照进入运行中状态，虚拟机ID：" + vmId + "，快照ID：" + snapshotId;
		OperationSeverityEnum operationSeverity = OperationSeverityEnum.LOW;
		operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
				operationResult, operationSeverity);

		/*
		 * image is now in active status.
		 */
		Image image = cloud.getImage(snapshotId);

		/*
		 * set snapshot status
		 */
		snapshot = Snapshot.getSnapshot(snapshotId);
		SnapshotStatusEnum snapshotStatus = SnapshotStatusEnum.ACTIVE;
		snapshot.setStatus(snapshotStatus);
		Long size = image.getSize() / 1024 / 1024 / 1024; // GB
		snapshot.setSize(size);
		boolean merged;
		try {
			merged = snapshot.lastCommitWinsMerge();
		} catch (Exception e) {
			merged = false;
		}
		if (false == merged) {
			/*
			 * tell the caller to retry.
			 */
			String action = "保存快照数据";
			String message = "创建快照后处理过程中，保存快照数据发生错误，将会进行下一次尝试，虚拟机ID：" + vmId + "，快照ID：" + snapshotId;
			return actionResultHelper.createActionResult(false, action, message, true, logger,
					ActionResultLogLevelEnum.WARN);
		}
		snapshot.flush();
		snapshot.clear();

		/*
		 * reset machine task status, retry if merge fails.
		 */
		machine = VirtualMachine.getVirtualMachine(vmId);
		machine.setTaskStatus(VirtualMachineTaskStatusEnum.NONE);
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
			String message = "创建快照后处理过程中，保存虚拟机状态发生错误，将会进行下一次尝试，虚拟机ID：" + vmId + "，快照ID：" + snapshotId;
			return actionResultHelper.createActionResult(false, action, message, true, logger,
					ActionResultLogLevelEnum.ERROR);
		}
		machine.flush();
		machine.clear();

		/*
		 * set snapshot as public after entering active state. set snapshot name again, since the retrieved image name
		 * encoding is bad
		 */
		cloud.updateImage(snapshotId, snapshot.getSnapshotName(), true);

		/*
		 * save operation log
		 */
		operationTime = new Date();
		operation = "设置快照属性";
		operationStatus = OperationStatusEnum.OK;
		operationResult = "创建快照后处理过程中，设置快照属性成功，虚拟机ID：" + vmId + "，快照ID：" + snapshotId;
		operationSeverity = OperationSeverityEnum.LOW;
		operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
				operationResult, operationSeverity);

		String action = "创建快照后处理";
		String message = "";
		return actionResultHelper.createActionResult(true, action, message, false, logger,
				ActionResultLogLevelEnum.NONE);
	}

	/**
	 * delete snapshot epilogue operation
	 * 
	 * @param request
	 *            - asyn request
	 * @return action result
	 * @author xiangqian
	 */
	public ActionResult deleteSnapshotEpilogue(AsyncOperationRequest request) {
		String snapshotId = request.getImageId();
		String username = request.getOperator();

		Snapshot snapshot = Snapshot.getSnapshot(snapshotId);
		if (null == snapshot) {
			String action = "检查快照";
			String message = "删除快照后处理失败，快照不存在，快照ID：" + snapshotId;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		/*
		 * wait image status
		 */
		CloudManipulator cloud = CloudManipulatorFactory.createCloudManipulator(cloudConfig,
				cloudConfig.getAdminProjectId());
		Image.Status status = Image.Status.DELETED;
		boolean waitResult;
		try {
			waitResult = cloud.waitImageStatus(snapshotId, status, appConfig.getPortalWaitTimeout());
		} catch (InterruptedException e) {
			waitResult = false;
		}

		/*
		 * retry if wait returns without excepting result.
		 */
		if (false == waitResult) {
			String action = "监测快照状态";
			String message = "删除快照后处理发生告警，在限定的时间内快照没有进入期待的状态，将会进行下一次尝试，快照ID：" + snapshotId;
			return actionResultHelper.createActionResult(false, action, message, true, logger,
					ActionResultLogLevelEnum.WARN);
		}

		/*
		 * snapshot is now in deleted status.
		 */

		/*
		 * save operation log
		 */
		String operator = username;
		Date operationTime = new Date();
		String operation = "监测快照状态";
		OperationStatusEnum operationStatus = OperationStatusEnum.OK;
		ServiceNameEnum serviceName = ServiceNameEnum.SNAPSHOT_MANAGEMENT;
		String objectId = snapshotId;
		String operationResult = "删除快照后处理过程中，监测到快照删除成功，快照ID：" + snapshotId;
		OperationSeverityEnum operationSeverity = OperationSeverityEnum.LOW;
		operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
				operationResult, operationSeverity);

		/*
		 * set snapshot status and detach from virtual machine
		 */
		snapshot = Snapshot.getSnapshot(snapshotId);
		SnapshotStatusEnum snapshotStatus = SnapshotStatusEnum.DELETED;
		snapshot.setStatus(snapshotStatus);
		snapshot.setDeleteTime(new Date());
		snapshot.setVirtualMachine(null);
		boolean merged;
		try {
			merged = snapshot.lastCommitWinsMerge();
		} catch (Exception e) {
			merged = false;
		}
		if (false == merged) {
			/*
			 * tell the caller to retry.
			 */
			String action = "保存快照数据";
			String message = "删除快照后处理过程中，保存快照数据发生错误，将会进行下一次尝试，快照ID：" + snapshotId;
			return actionResultHelper.createActionResult(false, action, message, true, logger,
					ActionResultLogLevelEnum.WARN);
		}
		snapshot.flush();
		snapshot.clear();

		String action = "删除快照后处理";
		String message = "";
		return actionResultHelper.createActionResult(true, action, message, false, logger,
				ActionResultLogLevelEnum.NONE);
	}
}
