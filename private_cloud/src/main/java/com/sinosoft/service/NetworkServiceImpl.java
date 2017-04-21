package com.sinosoft.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openstack4j.model.network.IP;
import org.openstack4j.model.network.NetFloatingIP;
import org.openstack4j.model.network.Port;
import org.openstack4j.model.network.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sinosoft.common.ActionResult;
import com.sinosoft.common.ActionResultLogLevelEnum;
import com.sinosoft.enumerator.ExternalIpDeviceOwnerEnum;
import com.sinosoft.enumerator.ExternalIpStatusEnum;
import com.sinosoft.enumerator.OperationSeverityEnum;
import com.sinosoft.enumerator.OperationStatusEnum;
import com.sinosoft.enumerator.ServiceNameEnum;
import com.sinosoft.model.ExternalIp;
import com.sinosoft.model.VirtualMachine;
import com.sinosoft.model.VirtualMachineDomain;
import com.sinosoft.openstack.CloudManipulator;
import com.sinosoft.openstack.CloudManipulatorFactory;
import com.sinosoft.openstack.type.CloudConfig;
import com.sinosoft.util.ActionResultHelper;

@Service
public class NetworkServiceImpl implements NetworkService {
	private static Logger logger = LoggerFactory.getLogger(NetworkServiceImpl.class);

	@Autowired
	CloudConfig cloudConfig;

	@Autowired
	OperationLogService operationLogService;

	@Autowired
	ActionResultHelper actionResultHelper;

	public void reloadExternalIp() {
		CloudManipulator cloud = CloudManipulatorFactory.createCloudManipulator(cloudConfig,
				cloudConfig.getAdminProjectId());

		List<String> externalIpAddresses = cloud.getExternalIps();
		if (0 == externalIpAddresses.size()) {
			return;
		}

		// initialize external ips.
		Map<String, ExternalIp> externalIpMap = new HashMap<String, ExternalIp>();
		for (String externalIpAddress : externalIpAddresses) {
			ExternalIp externalIp = new ExternalIp();
			externalIp.setIp(externalIpAddress);
			externalIp.setStatus(ExternalIpStatusEnum.AVAILABLE);

			externalIpMap.put(externalIpAddress, externalIp);
		}

		// floating ip.
		List<? extends NetFloatingIP> floatingIps = cloud.getFloatingIps();
		for (NetFloatingIP floatingIp : floatingIps) {
			String floatingIpAddress = floatingIp.getFloatingIpAddress();

			ExternalIp externalIp = externalIpMap.get(floatingIpAddress);
			if (null == externalIp) {
				logger.warn("加载外网地址出现告警，浮动IP不在范围之内，浮动IP的ID：" + floatingIp.getId() + "，浮动IP地址：" + floatingIpAddress);

				continue;
			}

			String portId = floatingIp.getPortId();
			/*
			 * floating ip created, but not associated to any instances. it must be removed, so that it can be created
			 * and associated to instance in any project.
			 */
			if (null == portId) {
				// TODO remove this floating ip

				logger.warn("加载外网地址出现告警，浮动IP需要手动删除，浮动IP的ID：" + floatingIp.getId() + "，浮动IP地址：" + floatingIpAddress);

				// set status to unknown, to prevent it from association.
				externalIp.setStatus(ExternalIpStatusEnum.UNKNOWN);
				externalIpMap.put(floatingIpAddress, externalIp);

				continue;
			}

			/*
			 * port of instance
			 */
			Port port = cloud.getPort(portId);
			if (null == port) {
				logger.error("加载外网地址发生错误，浮动IP对应的实例端口不存在");

				// set status to unknown.
				externalIp.setStatus(ExternalIpStatusEnum.UNKNOWN);
				externalIpMap.put(floatingIpAddress, externalIp);

				continue;
			}

			// instance id.
			String deviceId = port.getDeviceId();
			String projectId = port.getTenantId();

			externalIp.setStatus(ExternalIpStatusEnum.IN_USE);
			externalIp.setDeviceOwner(ExternalIpDeviceOwnerEnum.FLOATING_IP);
			externalIp.setDeviceId(deviceId);
			externalIp.setDomainId(projectId);
			externalIpMap.put(floatingIpAddress, externalIp);
		}

		// router gateway ip.
		List<? extends Port> gatewayPorts = cloud.getGatewayPorts();
		for (Port gatewayPort : gatewayPorts) {
			Set<? extends IP> ips = gatewayPort.getFixedIps();
			IP[] fixedIps = ips.toArray(new IP[0]);
			if (1 != fixedIps.length) {
				logger.warn("加载外网地址出现告警，路由网关固定地址数量不为1，路由网关端口ID：" + gatewayPort.getId());

				continue;
			}

			IP fixedIp = fixedIps[0];
			String gatewayIpAddress = fixedIp.getIpAddress();

			ExternalIp externalIp = externalIpMap.get(gatewayIpAddress);
			if (null == externalIp) {
				logger.warn("加载外网地址出现告警，路由网关IP不在范围之内，路由网关端口ID：" + gatewayPort.getId() + "，路由网关IP地址：" + gatewayIpAddress);

				continue;
			}

			// router id.
			String deviceId = gatewayPort.getDeviceId();
			Router router = cloud.getRouter(deviceId);
			if (null == router) {
				logger.error("加载外网地址发生错误，路由网关端口所对应的路由不存在");

				externalIp.setStatus(ExternalIpStatusEnum.UNKNOWN);
				externalIpMap.put(gatewayIpAddress, externalIp);

				continue;
			}

			// get project id from router, not from port.
			String projectId = router.getTenantId();

			externalIp.setStatus(ExternalIpStatusEnum.IN_USE);
			externalIp.setDeviceOwner(ExternalIpDeviceOwnerEnum.ROUTER_GATEWAY);
			externalIp.setDeviceId(deviceId);
			externalIp.setDomainId(projectId);
			externalIpMap.put(gatewayIpAddress, externalIp);
		}

		/*
		 * sort ip
		 */
		Collection<ExternalIp> externalIpCollections = externalIpMap.values();
		List<ExternalIp> externalIps = new ArrayList<ExternalIp>(externalIpCollections);
		Collections.sort(externalIps, new Comparator<ExternalIp>() {
			@Override
			public int compare(ExternalIp o1, ExternalIp o2) {
				return o1.getIp().compareToIgnoreCase(o2.getIp());
			}
		});

		/*
		 * save ip, the table must be truncated before.
		 */
		for (ExternalIp externalIp : externalIps) {
			// System.out.print(externalIp.getIp() + "\t");
			// System.out.print(externalIp.getStatus() + "\t");
			// System.out.print(externalIp.getDeviceOwner() + "\t");
			// System.out.print(externalIp.getDeviceId() + "\t");
			// System.out.print(externalIp.getDomainId() + "\t");
			// System.out.println();

			externalIp.persist();
			externalIp.flush();
			externalIp.clear();
		}
	}

	public Map<String, Object> getExternalIpEntries(int pageNo, int pageSize, String ip, String status) {
		Map<String, Object> map = new HashMap<String, Object>();

		int pageTotal = 0;
		if (pageNo <= 0) {
			pageNo = 1;
		}

		int recordCount = (int) ExternalIp.countExternalIps(ip, status);
		int externalIpCount = recordCount;
		recordCount -= 1;
		if (recordCount < 0) {
			recordCount = 0;
		}
		pageTotal = recordCount / pageSize + 1;

		List<ExternalIp> eIps = ExternalIp.getExternalIpEntries(ip, status, (pageNo - 1) * pageSize, pageSize, "ip",
				"ASC");
		List<com.sinosoft.type.ExternalIp> externalIps = new ArrayList<com.sinosoft.type.ExternalIp>();
		for (ExternalIp eIp : eIps) {
			com.sinosoft.type.ExternalIp externalIp = buildExternalIpValue(eIp);
			externalIps.add(externalIp);
		}

		map.put("page_no", pageNo);
		map.put("page_count", pageTotal);
		map.put("external_ip_count", externalIpCount);
		map.put("external_ips", externalIps);

		return map;
	}

	private com.sinosoft.type.ExternalIp buildExternalIpValue(ExternalIp externalIp) {
		com.sinosoft.type.ExternalIp externalIpValue = new com.sinosoft.type.ExternalIp();

		externalIpValue.setIp(externalIp.getIp());

		ExternalIpStatusEnum status = externalIp.getStatus();
		switch (status) {
		case AVAILABLE:
			externalIpValue.setStatus("未用");
			break;
		case IN_USE:
			externalIpValue.setStatus("已用");
			break;
		case UNKNOWN:
			externalIpValue.setStatus("未知");
			break;
		default:
			externalIpValue.setStatus("未知");
			break;
		}

		ExternalIpDeviceOwnerEnum deviceOwner = externalIp.getDeviceOwner();
		if (null != deviceOwner) {
			switch (deviceOwner) {
			case FLOATING_IP:
				externalIpValue.setDeviceOwner("虚拟机");
				break;
			case ROUTER_GATEWAY:
				externalIpValue.setDeviceOwner("网关");
				break;
			default:
				externalIpValue.setDeviceOwner("未知");
				break;
			}
		} else {
			externalIpValue.setDeviceOwner("");
		}

		String deviceId = externalIp.getDeviceId();
		if (null != deviceId) {
			externalIpValue.setDeviceId(deviceId);

			/*
			 * show instance name, and the instance must be managed in our portal.
			 */
			if ((null != deviceOwner) && (deviceOwner == ExternalIpDeviceOwnerEnum.FLOATING_IP)) {
				VirtualMachine machine = VirtualMachine.getVirtualMachine(deviceId);
				if (null != machine) {
					externalIpValue.setDeviceName(machine.getHostName());
				} else {
					externalIpValue.setDeviceName("未知虚拟机");
				}
			} else {
				externalIpValue.setDeviceName("");
			}
		} else {
			externalIpValue.setDeviceName("");
		}

		String domainId = externalIp.getDomainId();
		if (null != domainId) {
			externalIpValue.setDomainId(domainId);

			/*
			 * show instance name, and the domain must be managed in our portal.
			 */
			VirtualMachineDomain domain = VirtualMachineDomain.getDomainById(domainId);
			if (null != domain) {
				externalIpValue.setDomainName(domain.getDomainName());
			} else {
				externalIpValue.setDomainName("未知虚拟机域");
			}
		} else {
			externalIpValue.setDomainName("");
		}

		return externalIpValue;
	}

	public ActionResult createFloatingIp(String username, String ipAddress, String vmId) {
		VirtualMachine machine = VirtualMachine.getVirtualMachine(vmId);
		if (null == machine) {
			String action = "检查虚拟机";
			String message = "为虚拟机绑定访问地址失败，虚拟机不存在，虚拟机ID：" + vmId;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		/*
		 * create ip and associate to machine
		 */
		CloudManipulator cloud = CloudManipulatorFactory.createCloudManipulator(cloudConfig,
				cloudConfig.getAdminProjectId());
		com.sinosoft.openstack.type.ActionResult createResult = cloud.createFloatingIp(ipAddress, vmId);
		if (false == createResult.isSuccess()) {
			String action = "为虚拟机绑定访问地址";
			String message = "为虚拟机绑定访问地址失败，虚拟机ID：" + vmId + "，虚拟机名称：" + machine.getHostName();
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		/*
		 * save operation log
		 */
		String operator = username;
		Date operationTime = new Date();
		String operation = "为虚拟机绑定访问地址";
		OperationStatusEnum operationStatus = OperationStatusEnum.OK;
		ServiceNameEnum serviceName = ServiceNameEnum.VIRTURAL_MACHINE_MANAGEMENT;
		String objectId = vmId;
		String operationResult = "为虚拟机绑定访问地址成功，虚拟机ID：" + vmId + "，访问地址：" + ipAddress;
		OperationSeverityEnum operationSeverity = OperationSeverityEnum.MIDDLE;
		operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
				operationResult, operationSeverity);

		/*
		 * update machine
		 */
		machine.setFloatingIp(ipAddress);
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
			operation = "保存虚拟机访问地址信息";
			operationStatus = OperationStatusEnum.FAILED;
			operationResult = "在为虚拟机绑定访问地址时，保存虚拟机访问地址信息发生错误，请联系运维人员进行处理，虚拟机ID：" + vmId + "，访问地址：" + ipAddress;
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
		 * update external ip
		 */
		ExternalIp externalIp = ExternalIp.findExternalIp(ipAddress);
		externalIp.setStatus(ExternalIpStatusEnum.IN_USE);
		externalIp.setDeviceOwner(ExternalIpDeviceOwnerEnum.FLOATING_IP);
		externalIp.setDeviceId(vmId);
		externalIp.setDomainId(machine.getDomainId());
		externalIp.merge();
		externalIp.flush();
		externalIp.clear();

		String action = "为虚拟机绑定访问地址";
		String message = "";
		return actionResultHelper.createActionResult(true, action, message, false, logger,
				ActionResultLogLevelEnum.NONE);
	}

	public ActionResult deleteFloatingIp(String username, String ipAddress, String vmId) {
		VirtualMachine machine = VirtualMachine.getVirtualMachine(vmId);
		if (null == machine) {
			String action = "检查虚拟机";
			String message = "解绑虚拟机访问地址，虚拟机不存在，虚拟机ID：" + vmId;
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		/*
		 * delete ip
		 */
		CloudManipulator cloud = CloudManipulatorFactory.createCloudManipulator(cloudConfig,
				cloudConfig.getAdminProjectId());
		com.sinosoft.openstack.type.ActionResult deleteResult = cloud.deleteFloatingIp(ipAddress);
		if (false == deleteResult.isSuccess()) {
			String action = "解绑虚拟机访问地址";
			String message = "解绑虚拟机访问地址失败，虚拟机ID：" + vmId + "，虚拟机名称：" + machine.getHostName();
			return actionResultHelper.createActionResult(false, action, message, false, logger,
					ActionResultLogLevelEnum.ERROR);
		}

		/*
		 * save operation log
		 */
		String operator = username;
		Date operationTime = new Date();
		String operation = "解绑虚拟机访问地址";
		OperationStatusEnum operationStatus = OperationStatusEnum.OK;
		ServiceNameEnum serviceName = ServiceNameEnum.VIRTURAL_MACHINE_MANAGEMENT;
		String objectId = vmId;
		String operationResult = "解绑虚拟机访问地址成功，虚拟机ID：" + vmId + "，虚拟机名称：" + machine.getHostName();
		OperationSeverityEnum operationSeverity = OperationSeverityEnum.HIGH;
		operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId,
				operationResult, operationSeverity);

		/*
		 * save data
		 */
		machine.setFloatingIp(null);
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
			operation = "保存虚拟机访问地址信息";
			operationStatus = OperationStatusEnum.FAILED;
			operationResult = "解绑虚拟机访问地址时，保存虚拟机访问地址信息发生错误，请联系运维人员进行处理，虚拟机ID：" + vmId + "，虚拟机名称："
					+ machine.getHostName();
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
		 * update external ip
		 */
		ExternalIp externalIp = ExternalIp.findExternalIp(ipAddress);
		externalIp.setStatus(ExternalIpStatusEnum.AVAILABLE);
		externalIp.setDeviceOwner(null);
		externalIp.setDeviceId(null);
		externalIp.setDomainId(null);
		externalIp.merge();
		externalIp.flush();
		externalIp.clear();

		String action = "解绑虚拟机访问地址";
		String message = "";
		return actionResultHelper.createActionResult(true, action, message, false, logger,
				ActionResultLogLevelEnum.NONE);
	}
}
