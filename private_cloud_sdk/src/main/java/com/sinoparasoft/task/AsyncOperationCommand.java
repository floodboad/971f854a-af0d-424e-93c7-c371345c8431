package com.sinoparasoft.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinoparasoft.message.MessageProcessorCommand;
import com.sinoparasoft.common.ActionResult;
import com.sinoparasoft.common.AsyncOperationRequest;
import com.sinoparasoft.service.DiskServiceEpilogue;
import com.sinoparasoft.service.SnapshotServiceEpilogue;
import com.sinoparasoft.service.VirtualMachineServiceEpilogue;

@Service
public class AsyncOperationCommand implements MessageProcessorCommand {
	private static Logger logger = LoggerFactory.getLogger(AsyncOperationCommand.class);

	@Autowired
	VirtualMachineServiceEpilogue virtualMachineServiceEpilogue;

	@Autowired
	SnapshotServiceEpilogue snapshotServiceEpilogue;

	@Autowired
	DiskServiceEpilogue diskServiceEpilogue;

	@Override
	public boolean execute(String messageBody) {
		ObjectMapper mapper = new ObjectMapper();
		AsyncOperationRequest request;
		try {
			request = mapper.readValue(messageBody, AsyncOperationRequest.class);
		} catch (Exception e) {
			logger.error("解析消息发生错误，消息内容：" + messageBody, e);
			return false;
		}

		logger.debug("处理异步操作请求：" + request.toString());
		boolean retryHandle = handleRequest(request);
		return retryHandle;
	}

	/**
	 * do async work
	 * 
	 * @param request
	 *            - async operation request
	 * @return retry or not
	 * @author xiangqian
	 */
	private boolean handleRequest(AsyncOperationRequest request) {
		ActionResult result = null;

		switch (request.getOperationType()) {
		case CREATE_VIRTUAL_MACHINE:
			result = virtualMachineServiceEpilogue.createVirtualMachineEpilogue(request);
			break;
		case START_VIRTUAL_MACHINE:
			result = virtualMachineServiceEpilogue.startVirtualMachineEpilogue(request);
			break;
		case REBOOT_VIRTUAL_MACHINE:
			result = virtualMachineServiceEpilogue.rebootVirtualMachineEpilogue(request);
			break;
		case SHUTDOWN_VIRTUAL_MACHINE:
			result = virtualMachineServiceEpilogue.shutdownVirtualMachineEpilogue(request);
			break;
		case DELETE_VIRTUAL_MACHINE:
			result = virtualMachineServiceEpilogue.deleteVirtualMachineEpilogue(request);
			break;
		case ATTACH_DISK:
			result = virtualMachineServiceEpilogue.attachDiskEpilogue(request);
			break;
		case DETACH_DISK:
			result = virtualMachineServiceEpilogue.detachDiskEpilogue(request);
			break;
		case LIVE_MIGRATION_VIRTUAL_MACHINE:
			result = virtualMachineServiceEpilogue.liveMigrateVirtualMachineEpilogue(request);
			break;
		case CREATE_SNAPSHOT:
			result = snapshotServiceEpilogue.createSnapshotEpilogue(request);
			break;
		case DELETE_SNAPSHOT:
			result = snapshotServiceEpilogue.deleteSnapshotEpilogue(request);
			break;
		case CREATE_DISK:
			result = diskServiceEpilogue.createDiskEpilogue(request);
			break;
		case DELETE_DISK:
			result = diskServiceEpilogue.deleteDiskEpilogue(request);
			break;
		default:
			break;
		}

		if ((null != result) && (false == result.isSuccess())) {
			return result.isRetry();
		} else {
			return false;
		}
	}
}
