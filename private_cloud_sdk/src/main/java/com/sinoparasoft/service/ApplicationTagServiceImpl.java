package com.sinoparasoft.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sinoparasoft.common.ActionResult;
import com.sinoparasoft.common.ActionResultLogLevelEnum;
import com.sinoparasoft.enumerator.OperationSeverityEnum;
import com.sinoparasoft.enumerator.OperationStatusEnum;
import com.sinoparasoft.enumerator.ServiceNameEnum;
import com.sinoparasoft.model.ApplicationTag;
import com.sinoparasoft.model.VirtualMachine;
import com.sinoparasoft.type.ApplicationTagVirtualMachineInfo;
import com.sinoparasoft.util.ActionResultHelper;

@Service
public class ApplicationTagServiceImpl implements ApplicationTagService {
	private static Logger logger = LoggerFactory.getLogger(ApplicationTagServiceImpl.class);
	
	@Autowired
	OperationLogService operationLogService;

	@Autowired
	ActionResultHelper actionResultHelper;

	public Map<String, Object> getApplicationTagEntries(int pageNo, int pageSize) {
		Map<String, Object> map = new HashMap<String, Object>();

		int pageTotal = 0;
		if (pageNo <= 0) {
			pageNo = 1;
		}

		int recordCount = (int) ApplicationTag.countApplicationTags();
		int totalApplicationTagCount = recordCount;
		recordCount -= 1;
		if (recordCount < 0) {
			recordCount = 0;
		}
		pageTotal = recordCount / pageSize + 1;

		List<ApplicationTag> tags = ApplicationTag.getApplicationTagEntries((pageNo - 1) * pageSize, pageSize,
				"createTime", "DESC");

		List<com.sinoparasoft.type.ApplicationTag> applicationTags = new ArrayList<com.sinoparasoft.type.ApplicationTag>();
		for (ApplicationTag tag : tags) {
			com.sinoparasoft.type.ApplicationTag applicationTag = buildApplicationTagValue(tag);
			applicationTags.add(applicationTag);
		}

		map.put("pageNo", pageNo);
		map.put("pageTotal", pageTotal);
		map.put("total_application_tag_count", totalApplicationTagCount);
		map.put("application_tags", applicationTags);

		return map;
	}

	/**
	 * build application tag value can be used in json format.
	 * 
	 * @param applicationTag
	 *            - application tag model
	 * @return application tag value
	 * @author xiangqian
	 */
	private com.sinoparasoft.type.ApplicationTag buildApplicationTagValue(ApplicationTag applicationTag) {
		com.sinoparasoft.type.ApplicationTag applicationTagValue = new com.sinoparasoft.type.ApplicationTag();
		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		applicationTagValue.setId(applicationTag.getId());
		applicationTagValue.setName(applicationTag.getName());
		applicationTagValue.setDescription(applicationTag.getDescription());
		applicationTagValue.setCreator(applicationTag.getCreator());
		if (null != applicationTag.getCreateTime()) {
			applicationTagValue.setCreateTime(dateFormatter.format(applicationTag.getCreateTime()));
		} else {
			applicationTagValue.setCreateTime("");
		}
		applicationTagValue.setEnabled(applicationTag.getEnabled());

		List<ApplicationTagVirtualMachineInfo> virtualMachineInfo = new ArrayList<ApplicationTagVirtualMachineInfo>();
		Set<VirtualMachine> machines = applicationTag.getVirtualMachines();
		for (VirtualMachine machine : machines) {
			ApplicationTagVirtualMachineInfo infoItem = new ApplicationTagVirtualMachineInfo();
			infoItem.setId(machine.getHostId());
			infoItem.setName(machine.getHostName());

			virtualMachineInfo.add(infoItem);
		}
		applicationTagValue.setVirtualMachineInfo(virtualMachineInfo);

		return applicationTagValue;
	}

	public boolean checkExistenceByName(String name) {
		ApplicationTag applicationTag = ApplicationTag.getApplicationTagByName(name);
		if (null != applicationTag) {
			return true;
		} else {
			return false;
		}
	}

	public ActionResult createApplicationTag(String username, String name, String description) {
		/*
		 * check application tag existence
		 */
		boolean exist = checkExistenceByName(name);
		if (true == exist) {
			String action = "检查标签";
			String message = "添加业务系统标签失败，同名的标签已经存在，请更换一个新的标签名，标签名：" + name;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		/*
		 * save application tag
		 */
		ApplicationTag applicationTag = new ApplicationTag();
		applicationTag.setName(name);
		applicationTag.setDescription(description);
		applicationTag.setCreator(username);
		applicationTag.setCreateTime(new Date());
		applicationTag.setEnabled(true);
		applicationTag.persist();
		applicationTag.flush();
		applicationTag.clear();

		String id = applicationTag.getId();

		/*
		 * save operation log
		 */
		String operator = username;
		Date operationTime = new Date();
		String operation = "添加业务系统标签";
		OperationStatusEnum operationStatus = OperationStatusEnum.OK;
		ServiceNameEnum serviceName = ServiceNameEnum.APPLICATION_TAG_MANAGEMENT;
		String objectId = id;
		String operationResult = "添加业务系统标签成功，标签ID：" + id + "，标签名称：" + name + "，标签描述：" + description;
		OperationSeverityEnum operationSeverity = OperationSeverityEnum.LOW;
		operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
				operationResult, operationSeverity);

		String action = "添加业务系统标签";
		String message = "";
		return actionResultHelper.createActionResult(true, action, message, false, logger,
				ActionResultLogLevelEnum.NONE);
	}

	public ActionResult modifyApplicationTag(String username, String id, String newName, String newDescription) {
		ApplicationTag applicationTag = ApplicationTag.getApplicationTag(id);
		if (applicationTag == null) {
			String action = "检查标签";
			String message = "修改业务系统标签失败，标签不存在，标签ID：" + id;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		/*
		 * name conflict with existing one.
		 */
		ApplicationTag tempTag = ApplicationTag.getApplicationTagByName(newName);
		if ((null != tempTag) && (false == tempTag.getId().equalsIgnoreCase(id))) {
			String action = "检查同名标签";
			String message = "修改业务系统标签失败，已经存在同名的标签，标签ID：" + id;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		String oldName = applicationTag.getName();
		String oldDescription = applicationTag.getDescription();

		applicationTag.setName(newName);
		applicationTag.setDescription(newDescription);
		applicationTag.merge();
		applicationTag.flush();
		applicationTag.clear();

		/*
		 * save operation log
		 */
		String operator = username;
		Date operationTime = new Date();
		String operation = "修改业务系统标签";
		OperationStatusEnum operationStatus = OperationStatusEnum.OK;
		ServiceNameEnum serviceName = ServiceNameEnum.APPLICATION_TAG_MANAGEMENT;
		String objectId = id;
		String operationResult = "修改业务系统标签成功，标签ID：" + id + "，标签原始名称：" + oldName + "，标签原始描述：" + oldDescription
				+ "，标签新的名称：" + newName + "，标签新的描述：" + newDescription;
		OperationSeverityEnum operationSeverity = OperationSeverityEnum.LOW;
		operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
				operationResult, operationSeverity);

		String action = "修改业务系统标签";
		String message = "";
		return actionResultHelper.createActionResult(true, action, message, false, logger,
				ActionResultLogLevelEnum.NONE);
	}

	public ActionResult deleteApplicationTag(String username, String id) {
		ApplicationTag applicationTag = ApplicationTag.getApplicationTag(id);
		if (applicationTag == null) {
			String action = "检查标签";
			String message = "删除业务系统标签失败，标签不存在，标签ID：" + id;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		applicationTag.setEnabled(false);
		applicationTag.merge();
		applicationTag.flush();
		applicationTag.clear();

		/*
		 * save operation log
		 */
		String operator = username;
		Date operationTime = new Date();
		String operation = "删除业务系统标签";
		OperationStatusEnum operationStatus = OperationStatusEnum.OK;
		ServiceNameEnum serviceName = ServiceNameEnum.APPLICATION_TAG_MANAGEMENT;
		String objectId = id;
		String operationResult = "删除业务系统标签成功，标签ID：" + id;
		OperationSeverityEnum operationSeverity = OperationSeverityEnum.LOW;
		operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
				operationResult, operationSeverity);

		String action = "删除业务系统标签";
		String message = "";
		return actionResultHelper.createActionResult(true, action, message, false, logger,
				ActionResultLogLevelEnum.NONE);
	}

	public List<com.sinoparasoft.type.ApplicationTag> getList() {
		List<com.sinoparasoft.type.ApplicationTag> applicationTags = new ArrayList<com.sinoparasoft.type.ApplicationTag>();

		List<ApplicationTag> tags = ApplicationTag.getApplicationTags();
		for (ApplicationTag tag : tags) {
			com.sinoparasoft.type.ApplicationTag applicationTag = buildApplicationTagValue(tag);
			applicationTags.add(applicationTag);
		}

		return applicationTags;
	}
}
