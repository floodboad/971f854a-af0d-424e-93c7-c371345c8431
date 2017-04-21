package com.sinosoft.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinosoft.common.ActionResult;
import com.sinosoft.common.ActionResultLogLevelEnum;
import com.sinosoft.common.AsyncOperationRequest;
import com.sinosoft.common.AsyncOperationTypeEnum;
import com.sinosoft.common.BatchActionResult;
import com.sinosoft.enumerator.OperationSeverityEnum;
import com.sinosoft.enumerator.OperationStatusEnum;
import com.sinosoft.enumerator.ServiceNameEnum;
import com.sinosoft.enumerator.SnapshotStatusEnum;
import com.sinosoft.message.RetryMessageProducer;
import com.sinosoft.model.ApplicationTag;
import com.sinosoft.model.Snapshot;
import com.sinosoft.model.VirtualMachine;
import com.sinosoft.openstack.CloudManipulator;
import com.sinosoft.openstack.CloudManipulatorFactory;
import com.sinosoft.openstack.type.CloudConfig;
import com.sinosoft.type.MQConfig;
import com.sinosoft.type.SnapshotVirtualMachineInfo;
import com.sinosoft.util.ActionResultHelper;

@Service
public class SnapshotServiceImpl implements SnapshotService {
	private static Logger logger = LoggerFactory.getLogger(SnapshotServiceImpl.class);

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

	public Map<String, Object> getList(String userName, String role, int pageNo, int pageSize) {
		Map<String, Object> map = new HashMap<String, Object>();

		int totalSnapshotCount = 0;
		int recordCount = 0;
		if (role.equalsIgnoreCase("ROLE_MANAGER") == true) {
			recordCount = (int) Snapshot.countSnapshots();
		} else if (role.equalsIgnoreCase("ROLE_USER") == true) {
			recordCount = (int) Snapshot.countSnapshotsByManager(userName);
		} else {
			return null;
		}

		totalSnapshotCount = recordCount;
		recordCount -= 1;
		if (recordCount < 0) {
			recordCount = 0;
		}

		int pageTotal = recordCount / pageSize + 1;

		if (pageNo <= 0) {
			pageNo = 1;
		} else if (pageNo > pageTotal) {
			pageNo = pageTotal;
		}

		List<Snapshot> snapshots = new ArrayList<Snapshot>();
		if (role.equalsIgnoreCase("ROLE_MANAGER") == true) {
			snapshots = Snapshot.getSnapshots();
		} else if (role.equalsIgnoreCase("ROLE_USER") == true) {
			snapshots = Snapshot.getSnapshotsByManager(userName);
		}

		List<com.sinosoft.type.Snapshot> snapshotList = new ArrayList<com.sinosoft.type.Snapshot>();
		for (Snapshot snapshot : snapshots) {
			com.sinosoft.type.Snapshot snapshotValue = buildSnapshotValue(snapshot);
			snapshotList.add(snapshotValue);
		}

		map.put("pageNo", pageNo);
		map.put("pageTotal", pageTotal);
		map.put("snapshots", snapshotList);
		map.put("total_snapshot_count", totalSnapshotCount);

		return map;
	}

	public List<com.sinosoft.type.Snapshot> getListByVirtualMachine(String vmId) {
		VirtualMachine machine = VirtualMachine.getVirtualMachine(vmId);
		if (null == machine) {
			return null;
		}

		// sort snapshot by createTime in desc order
		Set<Snapshot> snapshotSet = machine.getSnapshots();
		List<Snapshot> snapshots = new ArrayList<Snapshot>(snapshotSet);
		Collections.sort(snapshots, new Comparator<Snapshot>() {
			@Override
			public int compare(Snapshot o1, Snapshot o2) {
				return (-1) * o1.getCreateTime().compareTo(o2.getCreateTime());
			}
		});

		List<com.sinosoft.type.Snapshot> snapshotValueList = new ArrayList<com.sinosoft.type.Snapshot>();
		for (Snapshot snapshot : snapshots) {
			com.sinosoft.type.Snapshot snapshotValue = buildSnapshotValue(snapshot);
			snapshotValueList.add(snapshotValue);
		}

		return snapshotValueList;
	}

	/**
	 * build virtual machine value can be used in json format.
	 * 
	 * @param snapshot
	 *            - snapshot model
	 * @return virtual machine value
	 * @author xiangqian
	 */
	private com.sinosoft.type.Snapshot buildSnapshotValue(Snapshot snapshot) {
		com.sinosoft.type.Snapshot snapshotValue = new com.sinosoft.type.Snapshot();
		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		snapshotValue.setId(snapshot.getSnapshotId());
		snapshotValue.setName(snapshot.getSnapshotName());
		snapshotValue.setDescription(snapshot.getDescription());
		snapshotValue.setCreator(snapshot.getCreator());
		if (null != snapshot.getCreateTime()) {
			snapshotValue.setCreateTime(dateFormatter.format(snapshot.getCreateTime()));
		}

		snapshotValue.setSize(snapshot.getSize());

		String status = "";
		switch (snapshot.getStatus()) {
		case ACTIVE:
			status = "运行中";
			break;
		case DELETED:
			status = "已删除";
			break;
		case DELETING:
			status = "删除中";
			break;
		case ERROR:
			status = "错误";
			break;
		case SAVING:
			status = "保存中";
			break;
		case UNKNOWN:
			status = "未知";
			break;
		default:
			break;
		}
		snapshotValue.setStatus(status);

		SnapshotVirtualMachineInfo virtualMachineInfo = new SnapshotVirtualMachineInfo();
		VirtualMachine machine = snapshot.getVirtualMachine();
		if (null != machine) {
			virtualMachineInfo.setName(machine.getHostName());

			List<String> applicationTagNames = new ArrayList<String>();
			for (ApplicationTag applicationTag : machine.getApplicationTags()) {
				applicationTagNames.add(applicationTag.getName());
			}
			virtualMachineInfo.setApplicationTagNames(applicationTagNames);

			snapshotValue.setVirtualMachineInfo(virtualMachineInfo);
		}

		return snapshotValue;
	}

	public BatchActionResult deleteSnapshots(String username, List<String> snapshotIds) {
		BatchActionResult result = new BatchActionResult();
		boolean success = true;
		String message = "";
		List<String> finishedList = new ArrayList<String>();
		List<String> failedList = new ArrayList<String>();

		String operator = username;
		Date operationTime;
		String operation;
		OperationStatusEnum operationStatus;
		ServiceNameEnum serviceName = ServiceNameEnum.SNAPSHOT_MANAGEMENT;
		String objectId;
		String operationResult;
		OperationSeverityEnum operationSeverity;

		CloudManipulator cloud = CloudManipulatorFactory.createCloudManipulator(cloudConfig,
				cloudConfig.getAdminProjectId());
		for (String snapshotId : snapshotIds) {
			try {
				ActionResult deleteResult = deleteSnapshot(cloud, username, snapshotId);
				if (true == deleteResult.isSuccess()) {
					finishedList.add(snapshotId);
				} else {
					/*
					 * save operation log
					 */
					operationTime = new Date();
					operation = deleteResult.getAction();
					operationStatus = OperationStatusEnum.FAILED;
					objectId = snapshotId;
					operationResult = deleteResult.getMessage();
					operationSeverity = OperationSeverityEnum.HIGH;
					operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName,
							objectId, operationResult, operationSeverity);

					message += operationResult;
					message += "；";
					failedList.add(snapshotId);
				}
			} catch (Exception e) {
				/*
				 * save operation log
				 */
				operationTime = new Date();
				operation = "删除快照";
				operationStatus = OperationStatusEnum.FAILED;
				objectId = snapshotId;
				operationResult = "删除快照失败，删除快照过程中发生错误，快照ID：" + snapshotId;
				operationSeverity = OperationSeverityEnum.HIGH;
				operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
						operationResult, operationSeverity);

				message += operationResult;
				message += "；";
				failedList.add(snapshotId);

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
	 * delete snapshot.
	 * 
	 * @param cloud
	 *            - cloud client
	 * @param username
	 *            - name of user who perform the action
	 * @param snapshotId
	 *            - snapshot id
	 * @return - action result
	 * @author xiangqian
	 */
	private ActionResult deleteSnapshot(CloudManipulator cloud, String username, String snapshotId) {
		if (false == messageProducer.isOpen()) {
			String action = "检查消息连接状态";
			String message = "删除快照失败，消息连接已关闭，请联系运维人员";
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		Snapshot snapshot = Snapshot.getSnapshot(snapshotId);
		if (null == snapshot) {
			String action = "检查快照";
			String message = "删除快照失败，快照不存在，快照ID：" + snapshotId;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		/*
		 * check snapshot status, only active snapshot is allowed to delete.
		 */
		if (SnapshotStatusEnum.ACTIVE != snapshot.getStatus()) {
			String action = "检查快照状态";
			String message = "删除快照失败，不允许删除非运行中状态的快照，快照ID：" + snapshotId + "， 快照状态：" + snapshot.getStatus().name();
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		boolean deleted = cloud.deleteImage(snapshotId);
		if (false == deleted) {
			String action = "发送删除快照请求";
			String message = "删除快照失败，发送删除快照请求失败，确认是否曾用该快照恢复过虚拟机，快照ID：" + snapshotId;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		/*
		 * save operation log
		 */
		String operator = username;
		Date operationTime = new Date();
		String operation = "发送删除快照请求";
		OperationStatusEnum operationStatus = OperationStatusEnum.OK;
		ServiceNameEnum serviceName = ServiceNameEnum.SNAPSHOT_MANAGEMENT;
		String objectId = snapshotId;
		String operationResult = "发送删除快照请求成功，快照ID：" + snapshotId;
		OperationSeverityEnum operationSeverity = OperationSeverityEnum.HIGH;
		operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
				operationResult, operationSeverity);

		/*
		 * save snapshot status
		 */
		snapshot.setStatus(SnapshotStatusEnum.DELETING);
		boolean merged;
		try {
			merged = snapshot.lastCommitWinsMerge();
		} catch (Exception e) {
			merged = false;
		}
		if (false == merged) {
			/*
			 * the caller is responsible to save operation log.
			 */
			String action = "保存快照状态";
			String message = "删除快照时，保存快照状态发生错误，请联系运维人员进行处理，快照ID：" + snapshotId;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}
		snapshot.flush();
		snapshot.clear();

		/*
		 * send message to message queue
		 */
		AsyncOperationRequest request = new AsyncOperationRequest();
		request.setOperationType(AsyncOperationTypeEnum.DELETE_SNAPSHOT);
		request.setOperator(username);
		request.setImageId(snapshotId);
		request.setRequestTime(new Date());
		ObjectMapper mapper = new ObjectMapper();
		try {
			String requestBody = mapper.writeValueAsString(request);
			messageProducer.publish(requestBody);
		} catch (Exception e) {
			/*
			 * the caller is responsible to save operation log.
			 */
			String action = "发送删除快照后处理请求";
			String message = "删除快照时，发送后处理请求发生错误，请联系运维人员进行处理，快照ID：" + snapshotId;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		String action = "删除快照";
		String message = "";
		return actionResultHelper.createActionResult(true, action, message, false, logger,
				ActionResultLogLevelEnum.NONE);
	}
}
