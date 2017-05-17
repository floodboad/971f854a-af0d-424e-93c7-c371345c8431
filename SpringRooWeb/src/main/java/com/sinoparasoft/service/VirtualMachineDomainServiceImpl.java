package com.sinoparasoft.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openstack4j.model.network.IP;
import org.openstack4j.model.network.Port;
import org.openstack4j.model.network.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sinoparasoft.common.ActionResult;
import com.sinoparasoft.common.ActionResultLogLevelEnum;
import com.sinoparasoft.enumerator.ExternalIpDeviceOwnerEnum;
import com.sinoparasoft.enumerator.ExternalIpStatusEnum;
import com.sinoparasoft.enumerator.OperationSeverityEnum;
import com.sinoparasoft.enumerator.OperationStatusEnum;
import com.sinoparasoft.enumerator.ServiceNameEnum;
import com.sinoparasoft.enumerator.VirtualMachineDomainStatusEnum;
import com.sinoparasoft.model.ExternalIp;
import com.sinoparasoft.model.VirtualMachine;
import com.sinoparasoft.model.VirtualMachineDomain;
import com.sinoparasoft.model.VirtualMachineGroup;
import com.sinoparasoft.openstack.CloudManipulator;
import com.sinoparasoft.openstack.CloudManipulatorFactory;
import com.sinoparasoft.openstack.type.CloudConfig;
import com.sinoparasoft.type.DomainOverallResourceUsage;
import com.sinoparasoft.type.DomainResourceUsage;
import com.sinoparasoft.type.GroupResourceUsage;
import com.sinoparasoft.type.PartitionedResourceUsageItem;
import com.sinoparasoft.util.ActionResultHelper;

@Service
public class VirtualMachineDomainServiceImpl implements VirtualMachineDomainService {
  private static Logger logger = LoggerFactory.getLogger(VirtualMachineDomainServiceImpl.class);

  @Autowired
  CloudConfig cloudConfig;

  @Autowired
  OperationLogService operationLogService;

  @Autowired
  VirtualMachineGroupService virtualMachineGroupService;

  @Autowired
  ActionResultHelper actionResultHelper;

  public ActionResult createDomain(String username, String domainName, String description, int cpu,
      int memory, int disk) {
    VirtualMachineDomain domain = VirtualMachineDomain.getDomainByName(domainName);
    if (null != domain) {
      String action = "检查同名虚拟机域";
      String message = "创建虚拟机域失败，已经存在同名的虚拟机域，请更换一个新的域名，虚拟机域名：" + domainName;
      return actionResultHelper.createActionResult(false, action, message, false, logger,
          ActionResultLogLevelEnum.ERROR);
    }

    /*
     * create domain
     */
    String domainId;
    CloudManipulator cloud;
    try {
      int instances = cpu;
      cloud = CloudManipulatorFactory.createCloudManipulator(cloudConfig,
          cloudConfig.getAdminProjectId());
      domainId = cloud.createProject(domainName, description, instances, cpu, memory);
    } catch (Exception e) {
      String action = "创建虚拟机域";
      String message = "创建虚拟机域发生错误，请联系运维人员进行处理，虚拟机域名：" + domainName;
      logger.error("<<<ROLLBACK-POINT>>>：" + message, e);
      return actionResultHelper.createActionResult(false, action, message, false, logger,
          ActionResultLogLevelEnum.ERROR);
    }

    /*
     * save operation log
     */
    String operator = username;
    Date operationTime = new Date();
    String operation = "创建虚拟机域";
    OperationStatusEnum operationStatus = OperationStatusEnum.OK;
    ServiceNameEnum serviceName = ServiceNameEnum.DOMAIN_MANAGEMENT;
    String objectId = domainId;
    String operationResult = "创建虚拟机域成功，ID：" + domainId + "，域名" + domainName + "，CPU" + cpu + "个，内存"
        + memory + "GB，磁盘" + disk + "GB";
    OperationSeverityEnum operationSeverity = OperationSeverityEnum.LOW;
    operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName,
        objectId, operationResult, operationSeverity);

    /*
     * update external ip usage
     */
    List<? extends Port> gatewayPorts = cloud.getGatewayPorts();
    for (Port gatewayPort : gatewayPorts) {
      // router id.
      String deviceId = gatewayPort.getDeviceId();
      Router router = cloud.getRouter(deviceId);
      if (null == router) {
        continue;
      }

      String projectId = router.getTenantId();
      if (false == domainId.equalsIgnoreCase(projectId)) {
        continue;
      } else {
        Set<? extends IP> ips = gatewayPort.getFixedIps();
        IP[] fixedIps = ips.toArray(new IP[0]);
        if (1 != fixedIps.length) {
          String action = "更新网络地址使用情况";
          String message = "创建虚拟机域时，更新网络地址使用情况发生错误，无法获取网关地址，网关地址数量不为1，请联系运维人员进行处理，虚拟机域ID："
              + domainId;
          return actionResultHelper.createActionResult(false, action, message, false, logger,
              ActionResultLogLevelEnum.ERROR);
        }

        IP fixedIp = fixedIps[0];
        String gatewayIpAddress = fixedIp.getIpAddress();
        ExternalIp externalIp = ExternalIp.findExternalIp(gatewayIpAddress);
        if (null != externalIp) {
          externalIp.setStatus(ExternalIpStatusEnum.IN_USE);
          externalIp.setDeviceOwner(ExternalIpDeviceOwnerEnum.ROUTER_GATEWAY);
          externalIp.setDeviceId(deviceId);
          externalIp.setDomainId(projectId);

          externalIp.merge();
          externalIp.flush();
          externalIp.clear();
        }

        break;
      }
    }

    /*
     * save data
     */
    domain = new VirtualMachineDomain();
    domain.setCreator(username);
    domain.setCreateTime(new Date());
    domain.setDomainName(domainName);
    domain.setDescription(description);
    domain.setDomainId(domainId);
    domain.setCpu(cpu);
    domain.setMemory(memory);
    domain.setDisk(disk);
    domain.setStatus(VirtualMachineDomainStatusEnum.ENABLED);
    domain.persist();

    String action = "创建虚拟机域";
    String message = "";
    return actionResultHelper.createActionResult(true, action, message, false, logger,
        ActionResultLogLevelEnum.NONE);
  }

  public ActionResult modifyDomain(String username, String domainId, String domainName,
      String description, int cpu, int memory, int disk) {
    VirtualMachineDomain domain = VirtualMachineDomain.getDomainById(domainId);
    if (null == domain) {
      String action = "检查虚拟机域";
      String message = "修改虚拟机域失败，指定的虚拟机域不存在，虚拟机域ID：" + domainId;
      return actionResultHelper.createActionResult(false, action, message, false, logger,
          ActionResultLogLevelEnum.ERROR);
    }

    String originalDomainName = domain.getDomainName();
    int originalCpu = domain.getCpu();
    int originalMemory = domain.getMemory();
    int origianlDisk = domain.getDisk();
    String origianlDescription = domain.getDescription();

    // bug if use "!=" to check equal
    if (domainName.equalsIgnoreCase(originalDomainName) == false) {
      VirtualMachineDomain checkDomain = VirtualMachineDomain.getDomainByName(domainName);
      if (null != checkDomain) {
        String action = "检查同名虚拟机域";
        String message = "修改虚拟机域失败，已经存在同名的虚拟机域，请更换一个新的域名，虚拟机域名：" + domainName;
        return actionResultHelper.createActionResult(false, action, message, false, logger,
            ActionResultLogLevelEnum.ERROR);
      }
    }

    int instances = cpu;
    CloudManipulator cloud = CloudManipulatorFactory.createCloudManipulator(cloudConfig, domainId);
    /*
     * update project info
     */
    cloud.updateProjectInfo(domainName, description);
    /*
     * update compute service quota
     */
    cloud.updateComputeServiceQuota(instances, cpu, memory);
    /*
     * update networking service quota
     */
    cloud.updateNetworkingServiceQuota(instances);

    /*
     * save operation log
     */
    String operator = username;
    Date operationTime = new Date();
    String operation = "修改虚拟机域配额";
    OperationStatusEnum operationStatus = OperationStatusEnum.OK;
    ServiceNameEnum serviceName = ServiceNameEnum.DOMAIN_MANAGEMENT;
    String objectId = domainId;
    String operationResult = "修改虚拟机域配额成功，ID：" + domainId + "，修改前的信息：CPU" + originalCpu + "个，内存"
        + originalMemory + "GB，磁盘" + origianlDisk + "GB，修改后的信息：CPU" + cpu + "个，内存" + memory
        + "GB，磁盘" + disk + "GB";
    OperationSeverityEnum operationSeverity = OperationSeverityEnum.MIDDLE;
    operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName,
        objectId, operationResult, operationSeverity);

    /*
     * save data
     */
    domain.setDomainName(domainName);
    domain.setDescription(description);
    domain.setCpu(cpu);
    domain.setMemory(memory);
    domain.setDisk(disk);
    domain.merge();
    domain.flush();
    domain.clear();

    /*
     * save operation log
     */
    operationTime = new Date();
    operation = "保存虚拟机域数据";
    operationStatus = OperationStatusEnum.OK;
    operationResult = "修改虚拟机域成功，ID：" + domainId + "，修改前的信息：域名" + originalDomainName + "，CPU"
        + originalCpu + "个，内存" + originalMemory + "GB，磁盘" + origianlDisk + "GB，描述"
        + origianlDescription + "，修改后的信息：域名" + domainName + "，CPU" + cpu + "个，内存" + memory + "GB，磁盘"
        + disk + "GB，描述" + description;
    operationSeverity = OperationSeverityEnum.LOW;
    operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName,
        objectId, operationResult, operationSeverity);

    String action = "修改虚拟机域";
    String message = "";
    return actionResultHelper.createActionResult(true, action, message, false, logger,
        ActionResultLogLevelEnum.NONE);
  }

  public ActionResult deleteDomain(String username, String domainId) {
    VirtualMachineDomain domain = VirtualMachineDomain.getDomainById(domainId);
    if (null == domain) {
      String action = "检查虚拟机域";
      String message = "删除虚拟机域失败，指定的虚拟机域不存在，虚拟机域ID：" + domainId;
      return actionResultHelper.createActionResult(false, action, message, false, logger,
          ActionResultLogLevelEnum.ERROR);
    }

    List<VirtualMachineGroup> groups = VirtualMachineGroup.getGroupsByDomainId(domainId);
    if (groups.size() > 0) {
      String action = "检查虚拟机域是否包含组";
      String message = "删除虚拟机域失败，当前虚拟机域非空，请删除所属的虚拟机组后再尝试删除，虚拟机域ID：" + domainId;
      return actionResultHelper.createActionResult(false, action, message, false, logger,
          ActionResultLogLevelEnum.ERROR);
    }

    String gatewayIp = getDomainGatewayIp(domainId);
    if (null == gatewayIp) {
      String action = "获取虚拟机域网关地址";
      String message = "删除虚拟机域失败，无法获取虚拟机域网关地址，虚拟机域ID：" + domainId;
      return actionResultHelper.createActionResult(false, action, message, false, logger,
          ActionResultLogLevelEnum.ERROR);
    }

    CloudManipulator cloud = CloudManipulatorFactory.createCloudManipulator(cloudConfig, domainId);
    com.sinoparasoft.openstack.type.ActionResult deletedResult = cloud.deleteProject();
    if (false == deletedResult.isSuccess()) {
      String action = "删除虚拟机域";
      String message = "删除虚拟机域失败，虚拟机域ID：" + domainId;
      return actionResultHelper.createActionResult(false, action, message, false, logger,
          ActionResultLogLevelEnum.ERROR);
    }

    /*
     * save operation log
     */
    String operator = username;
    Date operationTime = new Date();
    String operation = "删除虚拟机域";
    OperationStatusEnum operationStatus = OperationStatusEnum.OK;
    ServiceNameEnum serviceName = ServiceNameEnum.DOMAIN_MANAGEMENT;
    String objectId = domainId;
    String operationResult = "删除虚拟机域成功，ID：" + domainId;
    OperationSeverityEnum operationSeverity = OperationSeverityEnum.HIGH;
    operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName,
        objectId, operationResult, operationSeverity);

    /*
     * update external ip usage
     */
    ExternalIp externalIp = ExternalIp.findExternalIp(gatewayIp);
    if (null != externalIp) {
      externalIp.setStatus(ExternalIpStatusEnum.AVAILABLE);
      externalIp.setDeviceOwner(null);
      externalIp.setDeviceId(null);
      externalIp.setDomainId(null);

      externalIp.merge();
      externalIp.flush();
      externalIp.clear();
    }

    /*
     * save status
     */
    domain = VirtualMachineDomain.getDomainById(domainId);
    domain.setStatus(VirtualMachineDomainStatusEnum.DELETED);
    domain.setDeleteTime(new Date());
    domain.merge();
    domain.flush();
    domain.clear();

    String action = "删除虚拟机域";
    String message = "";
    return actionResultHelper.createActionResult(true, action, message, false, logger,
        ActionResultLogLevelEnum.NONE);
  }

  private String getDomainGatewayIp(String domainId) {
    String gatewayIp = null;

    CloudManipulator cloud = CloudManipulatorFactory.createCloudManipulator(cloudConfig,
        cloudConfig.getAdminProjectId());
    List<? extends Port> gatewayPorts = cloud.getGatewayPorts();
    for (Port gatewayPort : gatewayPorts) {
      // router id.
      String deviceId = gatewayPort.getDeviceId();
      Router router = cloud.getRouter(deviceId);
      if (null == router) {
        continue;
      }

      String projectId = router.getTenantId();
      if (false == domainId.equalsIgnoreCase(projectId)) {
        continue;
      } else {
        Set<? extends IP> ips = gatewayPort.getFixedIps();
        IP[] fixedIps = ips.toArray(new IP[0]);
        if (1 != fixedIps.length) {
          String message = "获取虚拟机域网关地址发生错误，网关地址数量不为1";
          logger.error(message);
          return null;
        }

        IP fixedIp = fixedIps[0];
        gatewayIp = fixedIp.getIpAddress();

        return gatewayIp;
      }
    }

    return gatewayIp;
  }

  public DomainResourceUsage getResourceUsage(String domainId) {
    PartitionedResourceUsageItem cpuUsage = new PartitionedResourceUsageItem();
    PartitionedResourceUsageItem memoryUsage = new PartitionedResourceUsageItem();
    PartitionedResourceUsageItem diskUsage = new PartitionedResourceUsageItem();

    // quota
    VirtualMachineDomain domain = VirtualMachineDomain.getDomainById(domainId);
    if (null == domain) {
      logger.error("指定的虚拟机域（domainId：）" + domainId + "不存在");
      return null;
    }
    cpuUsage.setQuota(domain.getCpu());
    memoryUsage.setQuota(domain.getMemory());
    diskUsage.setQuota(domain.getDisk());

    // allocated
    Map<String, Long> allocatedResource = VirtualMachineGroup.getDomainAllocatedResource(domainId);
    cpuUsage.setAllocated(allocatedResource.get("cpu"));
    memoryUsage.setAllocated(allocatedResource.get("memory"));
    diskUsage.setAllocated(allocatedResource.get("disk"));

    // unallocated
    cpuUsage.setUnallocated(cpuUsage.getQuota() - cpuUsage.getAllocated());
    memoryUsage.setUnallocated(memoryUsage.getQuota() - memoryUsage.getAllocated());
    diskUsage.setUnallocated(diskUsage.getQuota() - diskUsage.getAllocated());

    // used
    Map<String, Long> usedResource = VirtualMachine.getDomainUsedResource(domainId);
    cpuUsage.setUsed(usedResource.get("cpu"));
    memoryUsage.setUsed(usedResource.get("memory"));
    diskUsage.setUsed(usedResource.get("disk"));

    // unused
    cpuUsage.setUnused(cpuUsage.getAllocated() - cpuUsage.getUsed());
    memoryUsage.setUnused(memoryUsage.getAllocated() - memoryUsage.getUsed());
    diskUsage.setUnused(diskUsage.getAllocated() - diskUsage.getUsed());

    DomainResourceUsage resourceUsage = new DomainResourceUsage();
    resourceUsage.setDomainName(domain.getDomainName());
    resourceUsage.setCpuUsage(cpuUsage);
    resourceUsage.setMemoryUsage(memoryUsage);
    resourceUsage.setDiskUsage(diskUsage);
    return resourceUsage;
  }

  /**
   * get resource usage for all domains, including the contained groups
   * 
   * @return
   * @author xiangqian
   */
  public List<DomainOverallResourceUsage> getOverallReourceUsage() {
    List<DomainOverallResourceUsage> resourceUsage = new ArrayList<DomainOverallResourceUsage>();

    List<VirtualMachineDomain> domains = VirtualMachineDomain.getDomains();
    for (VirtualMachineDomain domain : domains) {
      String domainId = domain.getDomainId();
      DomainOverallResourceUsage overallResourceUsage = new DomainOverallResourceUsage();

      DomainResourceUsage domainResourceUsage = getResourceUsage(domainId);
      overallResourceUsage.setDomainResourceUsage(domainResourceUsage);

      List<VirtualMachineGroup> groups = VirtualMachineGroup.getGroupsByDomainId(domainId);
      List<GroupResourceUsage> groupResourceUsages = new ArrayList<GroupResourceUsage>();
      for (VirtualMachineGroup group : groups) {
        GroupResourceUsage groupResourceUsage = virtualMachineGroupService
            .getReourceUsage(group.getGroupId());
        groupResourceUsages.add(groupResourceUsage);
      }
      overallResourceUsage.setGroupResourceUsages(groupResourceUsages);

      resourceUsage.add(overallResourceUsage);
    }

    return resourceUsage;
  }
}
