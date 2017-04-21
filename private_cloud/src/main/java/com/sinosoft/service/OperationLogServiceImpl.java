package com.sinosoft.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.sinosoft.enumerator.OperationSeverityEnum;
import com.sinosoft.enumerator.OperationStatusEnum;
import com.sinosoft.enumerator.ServiceNameEnum;
import com.sinosoft.model.ApplicationTag;
import com.sinosoft.model.Disk;
import com.sinosoft.model.OperationLog;
import com.sinosoft.model.PhysicalMachine;
import com.sinosoft.model.Snapshot;
import com.sinosoft.model.User;
import com.sinosoft.model.VirtualMachine;
import com.sinosoft.model.VirtualMachineDomain;
import com.sinosoft.model.VirtualMachineGroup;

@Service
public class OperationLogServiceImpl implements OperationLogService {

	public void saveLog(String operator, Date operationTime, String operation, OperationStatusEnum operationStatus,
			ServiceNameEnum serviceName, String objectId, String operationResult,
			OperationSeverityEnum operationSeverity) {
		OperationLog log = new OperationLog();
		log.setOperator(operator);
		log.setOperationTime(operationTime);
		log.setOperation(operation);
		log.setOperationStatus(operationStatus);
		log.setServiceName(serviceName);
		log.setObjectId(objectId);
		log.setOperationResult(operationResult);
		log.setSeverityLevel(operationSeverity);

		log.persist();
		log.flush();
		log.clear();
	}

	public Map<String, Object> getOperationLogEntries(String username, String role, String serviceName,
			String operator, String operationStatus, String operationSeverity, String objectId, String startTime,
			String endTime, int pageNo, int pageSize) throws ParseException {
		Map<String, Object> map = new HashMap<String, Object>();

		int totalOperationLogCount = 0;
		
		int pageTotal = 0;
		int recordCount = 0;
		if (pageNo <= 0) {
			pageNo = 1;
		}

		if (role.equalsIgnoreCase("ROLE_ADMIN") == true) {
			// TODO: filter by search condition later
			recordCount = (int) OperationLog.countOperationLogs();
		} else if (role.equalsIgnoreCase("ROLE_MANAGER") == true) {
			recordCount = (int) OperationLog.countOperationLogsByManager(serviceName, operator, operationStatus,
					operationSeverity, objectId, startTime, endTime);
		} else if (role.equalsIgnoreCase("ROLE_USER") == true) {
			recordCount = (int) OperationLog.countOperationLogsByUser(username, serviceName, operator, operationStatus,
					operationSeverity, objectId, startTime, endTime);
		}

		totalOperationLogCount = recordCount;
		recordCount -= 1;
		if (recordCount < 0) {
			recordCount = 0;
		}

		pageTotal = recordCount / pageSize + 1;

		if (pageNo > pageTotal) {
			pageNo = pageTotal;
		}

		List<OperationLog> operationLogs = new ArrayList<OperationLog>();
		if (role.equalsIgnoreCase("ROLE_ADMIN") == true) {
			operationLogs = OperationLog.findOperationLogEntries((pageNo - 1) * pageSize, pageSize, "operationTime",
					"DESC");
		} else if (role.equalsIgnoreCase("ROLE_MANAGER") == true) {
			// use sql in backend: operationTime --> operation_time
			operationLogs = OperationLog.getOperationLogEntriesByManager(serviceName, operator, operationStatus,
					operationSeverity, objectId, startTime, endTime, (pageNo - 1) * pageSize, pageSize,
					"operation_time", "DESC");
		} else if (role.equalsIgnoreCase("ROLE_USER") == true) {
			operationLogs = OperationLog.getOperationLogEntriesByUser(username, serviceName, operator, operationStatus,
					operationSeverity, objectId, startTime, endTime, (pageNo - 1) * pageSize, pageSize,
					"operationTime", "DESC");
		}

		List<com.sinosoft.type.OperationLog> operationLogList = new ArrayList<com.sinosoft.type.OperationLog>();
		for (OperationLog operationLog : operationLogs) {
			com.sinosoft.type.OperationLog operationLogValue = buildOperationLogValue(operationLog);
			operationLogList.add(operationLogValue);
		}

		map.put("pageNo", pageNo);
		map.put("pageTotal", pageTotal);
		map.put("operation_logs", operationLogList);
		map.put("total_operation_log_count", totalOperationLogCount);

		return map;
	}

	public List<com.sinosoft.type.OperationLog> getOperationLogs(String username, String role, String serviceName,
			String operator, String operationStatus, String operationSeverity, String objectId, String startTime,
			String endTime) throws ParseException {
		List<OperationLog> operationLogs = new ArrayList<OperationLog>();

		if (role.equalsIgnoreCase("ROLE_ADMIN") == true) {
			// TODO: filter by search condition later
			operationLogs = OperationLog.findAllOperationLogs("operationTime", "DESC");
		} else if (role.equalsIgnoreCase("ROLE_MANAGER") == true) {
			operationLogs = OperationLog.getOperationLogsByManager(serviceName, operator, operationStatus,
					operationSeverity, objectId, startTime, endTime, "operation_time", "DESC");
		} else if (role.equalsIgnoreCase("ROLE_USER") == true) {
			operationLogs = OperationLog.getOperationLogsByUser(username, serviceName, operator, operationStatus,
					operationSeverity, objectId, startTime, endTime, "operationTime", "DESC");
		}

		List<com.sinosoft.type.OperationLog> operationLogList = new ArrayList<com.sinosoft.type.OperationLog>();
		for (OperationLog operationLog : operationLogs) {
			com.sinosoft.type.OperationLog operationLogValue = buildOperationLogValue(operationLog);
			operationLogList.add(operationLogValue);
		}

		return operationLogList;
	}

	private com.sinosoft.type.OperationLog buildOperationLogValue(OperationLog operationLog) {
		com.sinosoft.type.OperationLog operationLogValue = new com.sinosoft.type.OperationLog();
		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		operationLogValue.setId(operationLog.getId());

		operationLogValue.setOperator(operationLog.getOperator());
		User user = User.getUserByUsername(operationLog.getOperator());
		if (null != user) {
			operationLogValue.setOperatorDepartment(user.getDepartment());
		} else {
			operationLogValue.setOperatorDepartment("");
		}

		if (null != operationLog.getOperationTime()) {
			operationLogValue.setOperationTime(dateFormatter.format(operationLog.getOperationTime()));
		} else {
			operationLogValue.setOperationTime("");
		}

		operationLogValue.setOperation(operationLog.getOperation());

		OperationStatusEnum operationStatus = operationLog.getOperationStatus();
		if (null != operationStatus) {
			switch (operationStatus) {
			case OK:
				operationLogValue.setOperationStatus("成功");
				break;
			case FAILED:
				operationLogValue.setOperationStatus("失败");
				break;
			default:
				operationLogValue.setOperationStatus("未知");
				break;
			}
		} else {
			operationLogValue.setOperationStatus("未知");
		}

		ServiceNameEnum serviceName = operationLog.getServiceName();
		String objectId = operationLog.getObjectId();
		operationLogValue.setObjectId(objectId);
		if (null != serviceName) {
			switch (serviceName) {
			case APPLICATION_TAG_MANAGEMENT:
				operationLogValue.setServiceName("系统标签管理");

				ApplicationTag tag = ApplicationTag.getApplicationTag(objectId);
				if (null != tag) {
					operationLogValue.setObjectName(tag.getName());
				} else {
					operationLogValue.setObjectName("已删除");
				}

				break;
			case DISK_MANAGEMENT:
				operationLogValue.setServiceName("云硬盘管理");

				Disk disk = Disk.getDisk(objectId);
				if (null != disk) {
					operationLogValue.setObjectName(disk.getDiskName());
				} else {
					operationLogValue.setObjectName("已删除");
				}

				break;
			case DOMAIN_MANAGEMENT:
				operationLogValue.setServiceName("虚拟机域管理");

				VirtualMachineDomain domain = VirtualMachineDomain.getDomainById(objectId);
				if (null != domain) {
					operationLogValue.setObjectName(domain.getDomainName());
				} else {
					operationLogValue.setObjectName("已删除");
				}

				break;
			case GROUP_MANAGEMENT:
				operationLogValue.setServiceName("虚拟机组管理");

				VirtualMachineGroup group = VirtualMachineGroup.getGroup(objectId);
				if (null != group) {
					operationLogValue.setObjectName(group.getGroupName());
				} else {
					operationLogValue.setObjectName("已删除");
				}

				break;
			case PHYSICAL_MACHINE_MANAGEMENT:
				operationLogValue.setServiceName("物理服务器管理");

				PhysicalMachine physicalMachine = PhysicalMachine.getPhysicalMachineById(objectId);
				if (null != physicalMachine) {
					operationLogValue.setObjectName(physicalMachine.getHostName());
				} else {
					operationLogValue.setObjectName("已删除");
				}

				break;
			case SNAPSHOT_MANAGEMENT:
				operationLogValue.setServiceName("快照管理");

				Snapshot snapshot = Snapshot.getSnapshot(objectId);
				if (null != snapshot) {
					operationLogValue.setObjectName(snapshot.getSnapshotName());
				} else {
					operationLogValue.setObjectName("已删除");
				}

				break;
			case USER_MANAGEMENT:
				operationLogValue.setServiceName("用户管理");

				User userObject = User.getUserByUsername(objectId);
				if (null != userObject) {
					operationLogValue.setObjectName(userObject.getRealName());
				} else {
					operationLogValue.setObjectName("已删除");
				}

				break;
			case VIRTURAL_MACHINE_MANAGEMENT:
				operationLogValue.setServiceName("虚拟机管理");

				VirtualMachine machine = VirtualMachine.getVirtualMachine(objectId);
				if (null != machine) {
					operationLogValue.setObjectName(machine.getHostName());

					VirtualMachineDomain virtualMachineDomain = VirtualMachineDomain.getDomainById(machine
							.getDomainId());
					if (null != virtualMachineDomain) {
						operationLogValue.setVirtualMachineDomainName(virtualMachineDomain.getDomainName());
					} else {
						operationLogValue.setVirtualMachineDomainName("已删除");
					}

					VirtualMachineGroup virtualMachineGroup = VirtualMachineGroup.getGroup(machine.getGroupId());
					if (null != virtualMachineGroup) {
						operationLogValue.setVirtualMachineGroupName(virtualMachineGroup.getGroupName());
					} else {
						operationLogValue.setVirtualMachineGroupName("已删除");
					}
				} else {
					operationLogValue.setObjectName("已删除");
					operationLogValue.setVirtualMachineDomainName("未知");
					operationLogValue.setVirtualMachineGroupName("未知");
				}

				break;
			default:
				operationLogValue.setServiceName("未知");
				break;
			}
		} else {
			operationLogValue.setServiceName("未知");
		}

		operationLogValue.setOperationResult(operationLog.getOperationResult());

		OperationSeverityEnum operationSeverity = operationLog.getSeverityLevel();
		if (null != operationSeverity) {
			switch (operationSeverity) {
			case HIGH:
				operationLogValue.setSeverityLevel("高");
				break;
			case LOW:
				operationLogValue.setSeverityLevel("低");
				break;
			case MIDDLE:
				operationLogValue.setSeverityLevel("中");
				break;
			default:
				operationLogValue.setSeverityLevel("未知");
				break;
			}
		} else {
			operationLogValue.setSeverityLevel("未知");
		}

		return operationLogValue;
	}

	// /**
	// *
	// * @param operationLog
	// * @return
	// */
	// private Map<String, Object> buildOperationLogMap(OperationLog operationLog) {
	// SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	// // Map<String, Object> operationLogMap = new HashMap<String, Object>();
	//
	// // operationLogMap.put("id", operationLog.getId());
	//
	// // String serviceName = "";
	// // String objectId = operationLog.getObjectId();
	// // switch (operationLog.getServiceName()) {
	// // case DOMAIN_MANAGEMENT:
	// // serviceName = "虚拟机域管理";
	// // VirtualMachineDomain domain = VirtualMachineDomain.getDomainById(objectId);
	// // if (null != domain) {
	// // operationLogMap.put("object", domain.getDomainName());
	// // } else {
	// // operationLogMap.put("object", "已删除");
	// // }
	// //
	// // break;
	// // case GROUP_MANAGEMENT:
	// // serviceName = "虚拟机组管理";
	// // VirtualMachineGroup group = VirtualMachineGroup.getGroup(objectId);
	// // if (null != group) {
	// // operationLogMap.put("object", group.getGroupName());
	// // } else {
	// // operationLogMap.put("object", "已删除");
	// // }
	// //
	// // break;
	// // case USER_MANAGEMENT:
	// // serviceName = "用户管理";
	// // operationLogMap.put("object", operationLog.getObjectId());
	// // break;
	// // case VIRTURAL_MACHINE_MANAGEMENT:
	// // serviceName = "虚拟机管理";
	// // VirtualMachine vm = VirtualMachine.getVirtualMachine(objectId);
	// // if (null != vm) {
	// // operationLogMap.put("object", vm.getHostName());
	// // VirtualMachineDomain virtualMachineDomain = VirtualMachineDomain.getDomainById(vm.getDomainId());
	// // VirtualMachineGroup virtualMachineGroup = VirtualMachineGroup.getGroup(vm.getGroupId());
	// // operationLogMap.put("virtualMachineDomain", virtualMachineDomain.getDomainName());
	// // operationLogMap.put("virtualMachineGroup", virtualMachineGroup.getGroupName());
	// //
	// // } else {
	// // operationLogMap.put("object", "已删除");
	// // operationLogMap.put("domain", "空");
	// // operationLogMap.put("group", "空");
	// // }
	// //
	// // break;
	// // case DISK_MANAGEMENT:
	// // serviceName = "云硬盘管理";
	// // Disk disk = Disk.getDisk(objectId);
	// // if (null != disk) {
	// // operationLogMap.put("object", disk.getDiskName());
	// // } else {
	// // operationLogMap.put("object", "已删除");
	// // }
	// //
	// // break;
	// // case SNAPSHOT_MANAGEMENT:
	// // serviceName = "快照管理";
	// // Snapshot snapshot = Snapshot.getSnapshot(objectId);
	// // if (null != snapshot) {
	// // operationLogMap.put("object", snapshot.getSnapshotName());
	// // } else {
	// // operationLogMap.put("object", "已删除");
	// // }
	// //
	// // break;
	// // default:
	// // break;
	// // }
	// // operationLogMap.put("serviceName", serviceName);
	//
	// // operationLogMap.put("operator", operationLog.getOperator());
	// // User user = User.getUserByUsername(operationLog.getOperator());
	// // operationLogMap.put("operatorDepartment", user.getDepartment());
	//
	// // if (null != operationLog.getOperationTime()) {
	// // operationLogMap.put("operationTime", dateFormatter.format(operationLog.getOperationTime()));
	// // }
	// // operationLogMap.put("operation", operationLog.getOperation());
	//
	// switch (operationLog.getSeverityLevel()) {
	// case HIGH:
	// operationLogMap.put("severityLevel", "高");
	// break;
	// case MIDDLE:
	// operationLogMap.put("severityLevel", "中");
	// break;
	// case LOW:
	// operationLogMap.put("severityLevel", "低");
	// break;
	// default:
	// break;
	// }
	//
	// // operationLogMap.put("operationResult", operationLog.getOperationResult());
	//
	// return operationLogMap;
	// }
}
