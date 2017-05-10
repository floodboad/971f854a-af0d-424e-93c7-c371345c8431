package com.sinoparasoft.task;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.dom4j.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sinoparasoft.nagios.NagiosConfig;
import com.sinoparasoft.nagios.NagiosMonitor;
import com.sinoparasoft.nagios.ServiceStatus;
import com.sinoparasoft.openstack.CloudManipulator;
import com.sinoparasoft.openstack.CloudManipulatorFactory;
import com.sinoparasoft.openstack.type.CloudConfig;
import com.sinoparasoft.config.AppConfig;
import com.sinoparasoft.enumerator.AlarmSeverityEnum;
import com.sinoparasoft.enumerator.MonitorNameEnum;
import com.sinoparasoft.enumerator.MonitorSourceEnum;
import com.sinoparasoft.enumerator.MonitorStatusEnum;
import com.sinoparasoft.enumerator.MonitorTypeEnum;
import com.sinoparasoft.enumerator.PhysicalMachineStatusEnum;
import com.sinoparasoft.enumerator.VirtualMachineStatusEnum;
import com.sinoparasoft.model.Monitor;
import com.sinoparasoft.model.MonitorLog;
import com.sinoparasoft.model.MonitorResult;
import com.sinoparasoft.model.MonitorSetting;
import com.sinoparasoft.model.PhysicalMachine;
import com.sinoparasoft.model.PhysicalMachineLoad;
import com.sinoparasoft.model.VirtualMachine;

@Service
public class MonitorAlarmWorker {
	private ThreadPoolTaskExecutor taskExecutor;
	private static Logger logger = LoggerFactory.getLogger(MonitorAlarmWorker.class);

	@Autowired
	AppConfig appConfig;

	@Autowired
	CloudConfig cloudConfig;

	@Autowired
	NagiosConfig nagiosConfig;

	/**
	 * run thread pool after servlet constructor
	 * 
	 * @author xiangqian
	 */
	@PostConstruct
	public void init() {
		taskExecutor = new ThreadPoolTaskExecutor();
		taskExecutor.setCorePoolSize(10);
		taskExecutor.setMaxPoolSize(20);
		taskExecutor.setKeepAliveSeconds(300);
		taskExecutor.initialize();

		VirtualMachineAlarmTask vmAlarmTask = new VirtualMachineAlarmTask();
		taskExecutor.execute(vmAlarmTask);

		PhysicalMachineMonitorTask pmMonitorTask = new PhysicalMachineMonitorTask();
		taskExecutor.execute(pmMonitorTask);

		PhysicalMachineAlarmTask pmAlarmTask = new PhysicalMachineAlarmTask();
		taskExecutor.execute(pmAlarmTask);

		// for (int i = 0; i < 5; i++) {
		// DataThread thread = new DataThread();
		// taskExecutor.execute(thread);
		// }
	}

	/**
	 * trigger thread InterruptedException before servlet destroy
	 * 
	 * @author xiangqian
	 */
	@PreDestroy
	public void destroy() {
		// trigger InterruptedException to each thread
		taskExecutor.shutdown();
	}

	/**
	 * save monitor log. add new log if (1) there's no history before and (2) the monitor status changed, change the
	 * check time if the monitor status does not change.
	 * 
	 * @param monitorSource
	 *            - monitor source
	 * @param hostId
	 *            - machine id
	 * @param serviceType
	 *            - service type
	 * @param monitorName
	 *            - monitor name
	 * @param monitorStatus
	 *            - monitor status
	 * @param message
	 *            - message
	 * @param severityLevel
	 *            - sevirity level
	 * @author xiangqian
	 */
	@Transactional
	private void saveMonitorLog(MonitorSourceEnum monitorSource, String hostId, MonitorTypeEnum serviceType,
			MonitorNameEnum monitorName, MonitorStatusEnum monitorStatus, String message,
			AlarmSeverityEnum severityLevel) {
		MonitorLog latestMonitorLog = MonitorLog.getLatestMonitorLog(hostId, serviceType, monitorName);
		if (null == latestMonitorLog) {
			/*
			 * add new monitor history.
			 */
			addMonitorLog(monitorSource, hostId, serviceType, monitorName, monitorStatus, message, severityLevel);
		} else {
			MonitorStatusEnum latestMonitorStatus = latestMonitorLog.getMonitorStatus();
			if (true == latestMonitorStatus.name().equalsIgnoreCase(monitorStatus.name())) {
				/*
				 * change the check time if the monitor status does not change.
				 */
				latestMonitorLog.setCheckTime(new Date());
				latestMonitorLog.merge();
				latestMonitorLog.flush();
				latestMonitorLog.clear();
			} else {
				/*
				 * add new monitor history if the monitor status changed.
				 */
				addMonitorLog(monitorSource, hostId, serviceType, monitorName, monitorStatus, message, severityLevel);
			}
		}

		return;
	}

	/**
	 * add new monitor log.
	 * 
	 * @param monitorSource
	 *            - monitor source
	 * @param hostId
	 *            - machine id
	 * @param monitorType
	 *            - service type
	 * @param monitorName
	 *            - monitor name
	 * @param monitorStatus
	 *            - monitor status
	 * @param message
	 *            - message
	 * @param severityLevel
	 *            - sevirity level
	 * @author xiangqian
	 */
	@Transactional
	private void addMonitorLog(MonitorSourceEnum monitorSource, String hostId, MonitorTypeEnum monitorType,
			MonitorNameEnum monitorName, MonitorStatusEnum monitorStatus, String message,
			AlarmSeverityEnum severityLevel) {
		MonitorLog newMonitorLog = new MonitorLog();
		newMonitorLog.setMonitorSource(monitorSource);
		newMonitorLog.setSourceId(hostId);
		newMonitorLog.setMonitorType(monitorType);
		newMonitorLog.setMonitorName(monitorName);
		newMonitorLog.setMonitorStatus(monitorStatus);
		newMonitorLog.setMessage(message);
		newMonitorLog.setSeverityLevel(severityLevel);
		newMonitorLog.setUpdateTime(new Date());
		newMonitorLog.setCheckTime(new Date());
		newMonitorLog.persist();
		newMonitorLog.flush();
		newMonitorLog.clear();

		return;
	}

	/**
	 * save monitor result. add new monitor result if there's no result before, or change monitor status and update time
	 * of existing result.
	 * 
	 * @param hostId
	 *            - machine id
	 * @param monitorType
	 *            - monitor type
	 * @param monitorName
	 *            - monitor name
	 * @param monitorStatus
	 *            - monitor status
	 * @author xiangqian
	 */
	@Transactional
	private void saveMonitorResult(MonitorSourceEnum monitorSource, String hostId, MonitorTypeEnum monitorType,
			MonitorNameEnum monitorName, MonitorStatusEnum monitorStatus) {
		MonitorResult currentMonitorResult = MonitorResult.getMonitorResult(hostId, monitorType, monitorName);
		if (null == currentMonitorResult) {
			/*
			 * add new monitor result.
			 */
			MonitorResult newMonitorResult = new MonitorResult();
			newMonitorResult.setMonitorSource(monitorSource);
			newMonitorResult.setSourceId(hostId);
			newMonitorResult.setMonitorType(monitorType);
			newMonitorResult.setMonitorName(monitorName);
			newMonitorResult.setMonitorStatus(monitorStatus);
			newMonitorResult.setUpdateTime(new Date());
			newMonitorResult.persist();
			newMonitorResult.flush();
			newMonitorResult.clear();
		} else {
			/*
			 * update monitor result, change monitor status and update time.
			 */
			currentMonitorResult.setMonitorStatus(monitorStatus);
			currentMonitorResult.setUpdateTime(new Date());
			currentMonitorResult.merge();
			currentMonitorResult.flush();
			currentMonitorResult.clear();
		}
	}

	private class VirtualMachineAlarmTask implements Runnable {
		private boolean fireBullet;

		@Override
		public void run() {
			fireBullet = true;

			while (true == fireBullet) {
				try {
					Thread.sleep(appConfig.getPortalLoopInterval() * 1000);
				} catch (InterruptedException e) {
					// set fire flag
					fireBullet = false;

					logger.error("虚拟机告警服务发生错误，监控被中断", e);
				} catch (Exception e) {
					logger.error("虚拟机告警服务发生错误", e);
				}

				try {
					saveVirtualMachineMonitorHistory();
				} catch (Exception e) {
					logger.error("虚拟机告警服务发生错误，保存节点监控历史发生错误", e);
				}

				try {
					saveVirtualMachineMonitorResult();
				} catch (Exception e) {
					logger.error("虚拟机告警服务发生错误，保存节点监控结果发生错误", e);
				}
			}
		}

		/**
		 * save monitor history for all virtual machines. the monitor history include all load metrics, and for each
		 * load metric, a new history record is added if the monitor status changes.
		 * 
		 * @author xiangqian
		 */
		private void saveVirtualMachineMonitorHistory() {
			List<VirtualMachine> machines = VirtualMachine.getVirtualMachines();
			for (VirtualMachine machine : machines) {
				try {
					saveVirtualMachineLoadMonitorHistory(machine);
				} catch (Exception e) {
					// continue to handle next machine
					logger.error("虚拟机告警服务发生错误，保存节点负载监控历史发生错误", e);
				}
			}
		}

		/**
		 * save load monitor history for a virtual machine. the load monitor history include all load metrics, and for
		 * each metric, a new history record is added if the monitor status changes.
		 * 
		 * @param machine
		 *            - virtual machine
		 * @author xiangqian
		 */
		private void saveVirtualMachineLoadMonitorHistory(VirtualMachine machine) {
			CloudManipulator cloud = CloudManipulatorFactory.createCloudManipulator(cloudConfig,
					cloudConfig.getAdminProjectId());

			MonitorSourceEnum monitorSource = MonitorSourceEnum.VIRTUAL_MACHINE;
			String hostId = machine.getHostId();
			MonitorTypeEnum monitorType = MonitorTypeEnum.LOAD;

			List<MonitorSetting> monitorSettings = MonitorSetting.getMonitorSettings(hostId, MonitorTypeEnum.LOAD);
			for (MonitorSetting monitorSetting : monitorSettings) {
				if (monitorSetting.getEnabled() == false) {
					/*
					 * skip if the monitor is disabled.
					 */
					continue;
				}

				MonitorNameEnum monitorName = monitorSetting.getMonitorName();
				MonitorStatusEnum monitorStatus;
				String message = null;
				AlarmSeverityEnum severityLevel = null;
				if (VirtualMachineStatusEnum.ACTIVE == machine.getStatus()) {
					/*
					 * set monitor status based on alarm state if virtual machine is active.
					 */
					String alarmId = monitorSetting.getOsAlarmId();
					String alarmState = cloud.getAlarmState(alarmId);
					if ((null != alarmState) && (alarmState.equalsIgnoreCase("alarm"))) {
						/*
						 * set monitor status, message and severity level if alarmed.
						 */
						monitorStatus = MonitorStatusEnum.WARNING;
						severityLevel = monitorSetting.getSeverityLevel();

						switch (monitorName) {
						case VM_CPU_UTIL:
							message = "虚拟机" + machine.getHostName() + "的处理器利用率超出告警阈值，当前告警阈值设置为："
									+ monitorSetting.getThreshold();

							break;
						case VM_MEMORY_USAGE:
							message = "虚拟机" + machine.getHostName() + "的内存利用率超出告警阈值，当前告警阈值设置为："
									+ monitorSetting.getThreshold();

							break;
						case VM_DISK_READ_BYTES_RATE:
							message = "虚拟机" + machine.getHostName() + "的磁盘读取速率超出告警阈值，当前告警阈值设置为："
									+ monitorSetting.getThreshold();

							break;
						case VM_DISK_WRITE_BYTES_RATE:
							message = "虚拟机" + machine.getHostName() + "的磁盘写入速率超出告警阈值，当前告警阈值设置为："
									+ monitorSetting.getThreshold();

							break;
						case VM_NETWORK_OUTGOING_BYTES_RATE:
							message = "虚拟机" + machine.getHostName() + "的网络流出速率超出告警阈值，当前告警阈值设置为："
									+ monitorSetting.getThreshold();

							break;
						case VM_NETWORK_INCOMING_BYTES_RATE:
							message = "虚拟机" + machine.getHostName() + "的网络流入速率超出告警阈值，当前告警阈值设置为："
									+ monitorSetting.getThreshold();

							break;
						default:
							break;
						}
					} else if ((null != alarmState) && (alarmState.equalsIgnoreCase("ok"))) {
						/*
						 * set monitor status.
						 */
						monitorStatus = MonitorStatusEnum.NORMAL;
					} else {
						/*
						 * set monitor status.
						 */
						monitorStatus = MonitorStatusEnum.UNKNOWN;
					}
				} else {
					/*
					 * set monitor status to unknown if virtual machine in not active.
					 */
					monitorStatus = MonitorStatusEnum.UNKNOWN;
				}

				/*
				 * save monitor history
				 */
				saveMonitorLog(monitorSource, hostId, monitorType, monitorName, monitorStatus, message, severityLevel);
			}
		}

		/**
		 * save monitor result for all virtual machines.
		 * 
		 * @author xiangqian
		 */
		private void saveVirtualMachineMonitorResult() {
			List<VirtualMachine> machines = VirtualMachine.getVirtualMachines();
			for (VirtualMachine machine : machines) {
				try {
					saveVirtualMachineLoadMonitorResult(machine);
				} catch (Exception e) {
					// continue to handle
					logger.error("虚拟机告警服务发生错误，保存节点负载监控结果发生错误", e);
				}

				try {
					saveVirtualMachineNodeMonitorResult(machine);
				} catch (Exception e) {
					// continue to handle
					logger.error("虚拟机告警服务发生错误，保存节点监控结果发生错误", e);
				}
			}
		}

		/**
		 * save load monitor result for a virtual machine. each load monitor result is based on the latest monitor
		 * history.
		 * 
		 * @param machine
		 *            - virtual machine
		 * @author xiangqian
		 */
		private void saveVirtualMachineLoadMonitorResult(VirtualMachine machine) {
			MonitorSourceEnum monitorSource = MonitorSourceEnum.VIRTUAL_MACHINE;
			String hostId = machine.getHostId();
			MonitorTypeEnum monitorType = MonitorTypeEnum.LOAD;

			List<MonitorSetting> monitorSettings = MonitorSetting.getMonitorSettings(hostId, monitorType);
			for (MonitorSetting monitorSetting : monitorSettings) {
				if (monitorSetting.getEnabled() == false) {
					/*
					 * skip if the monitor is disabled.
					 */
					continue;
				}

				MonitorNameEnum monitorName = monitorSetting.getMonitorName();
				MonitorStatusEnum monitorStatus = null;
				MonitorLog latestHistory = MonitorLog.getLatestMonitorLog(hostId, monitorType, monitorName);
				if (null == latestHistory) {
					/*
					 * set monitor status to unknown if latest load metric monitor history is not found.
					 */
					monitorStatus = MonitorStatusEnum.UNKNOWN;
				} else {
					/*
					 * set monitor status to the latest load metric monitor status.
					 */
					monitorStatus = latestHistory.getMonitorStatus();
				}

				/*
				 * save monitor result.
				 */
				saveMonitorResult(monitorSource, hostId, monitorType, monitorName, monitorStatus);
			}
		}

		/**
		 * save node monitor result for a virtual machine. the node monitor result is based on all the machine's load
		 * monitor results.
		 * 
		 * @param machine
		 *            - virtual machine
		 * @author xiangqian
		 */
		private void saveVirtualMachineNodeMonitorResult(VirtualMachine machine) {
			MonitorSourceEnum monitorSource = MonitorSourceEnum.VIRTUAL_MACHINE;
			String hostId = machine.getHostId();
			MonitorTypeEnum monitorType = MonitorTypeEnum.NODE;
			MonitorNameEnum monitorName = MonitorNameEnum.NONE;

			int MONITOR_STATUS_ACTIVE = 1;
			int MONITOR_STATUS_WARNING = 2;
			int MONITOR_STATUS_UNKNOWN = 4;
			int statusCode = 0;

			/*
			 * load monitor result
			 */
			List<MonitorResult> monitorResults = MonitorResult.getMonitorResults(hostId, MonitorTypeEnum.LOAD);
			for (MonitorResult monitorResult : monitorResults) {
				MonitorSetting monitorSetting = MonitorSetting.getMonitorSetting(hostId,
						monitorResult.getMonitorName());
				if ((null == monitorSetting) || (false == monitorSetting.getEnabled())) {
					/*
					 * skip.
					 */
					continue;
				}

				/*
				 * the status code is bitwise of all load monitor status.
				 */
				if (MonitorStatusEnum.NORMAL == monitorResult.getMonitorStatus()) {
					statusCode |= MONITOR_STATUS_ACTIVE;
				} else if (MonitorStatusEnum.WARNING == monitorResult.getMonitorStatus()) {
					statusCode |= MONITOR_STATUS_WARNING;
				} else {
					statusCode |= MONITOR_STATUS_UNKNOWN;
				}
			}

			/*
			 * get monitor status from the highest bit of status code.
			 */
			MonitorStatusEnum monitorStatus = null;
			if (MONITOR_STATUS_UNKNOWN == (statusCode & MONITOR_STATUS_UNKNOWN)) {
				monitorStatus = MonitorStatusEnum.UNKNOWN;
			} else if (MONITOR_STATUS_WARNING == (statusCode & MONITOR_STATUS_WARNING)) {
				monitorStatus = MonitorStatusEnum.WARNING;
			} else if (MONITOR_STATUS_ACTIVE == (statusCode & MONITOR_STATUS_ACTIVE)) {
				monitorStatus = MonitorStatusEnum.NORMAL;
			} else {
				/*
				 * status code is the initial value.
				 */
				monitorStatus = MonitorStatusEnum.UNKNOWN;
			}

			/*
			 * save monitor result.
			 */
			saveMonitorResult(monitorSource, hostId, monitorType, monitorName, monitorStatus);
		}
	}

	private class PhysicalMachineMonitorTask implements Runnable {
		private boolean fireBullet;
		private final long keepRecords = 60;

		@Override
		public void run() {
			fireBullet = true;

			while (true == fireBullet) {
				try {
					Thread.sleep(appConfig.getPortalLoopInterval() * 1000);
				} catch (InterruptedException e) {
					fireBullet = false;

					logger.error("物理服务器监控服务发生错误，监控被中断", e);
				} catch (Exception e) {
					logger.error("物理服务器监控服务发生错误", e);
				}

				try {
					savePhysicalMachineLoad();
				} catch (Exception e) {
					logger.error("物理服务器监控服务发生错误，保存节点负载发生错误", e);
				}

				try {
					savePhysicalMachineMonitorHistory();
				} catch (Exception e) {
					logger.error("物理服务器监控服务发生错误，保存节点监控历史发生错误", e);
				}
			}
		}

		/**
		 * save machine load.
		 * 
		 * @throws DocumentException
		 * @throws IOException
		 * @throws UnknownHostException
		 * @author xiangqian
		 */
		@Transactional
		private void savePhysicalMachineLoad() throws DocumentException, UnknownHostException, IOException {
			String[] serverIps = appConfig.getGangliaServers();
			GangliaMonitor ganglia = new GangliaMonitor(serverIps);
			Map<String, GangliaMetric> metrics = ganglia.getMetrics();

			for (Map.Entry<String, GangliaMetric> entry : metrics.entrySet()) {
				String hostName = entry.getKey();
				PhysicalMachine machine = PhysicalMachine.getPhysicalMachineByName(hostName);
				if (null == machine) {
					/*
					 * skip invalid ganglia node.
					 */
					continue;
				}

				/*
				 * save load, the metric value maybe null.
				 */
				GangliaMetric metric = entry.getValue();
				PhysicalMachineLoad load = new PhysicalMachineLoad();

				load.setHostName(hostName);
				load.setCpuLoad(metric.getAvgOneMinuteLoad()); // %
				load.setCpuIdle(metric.getCpuIdlePercent());
				load.setCpuSystem(metric.getCpuSystemPercent());
				load.setCpuUser(metric.getCpuUserPercent());

				if ((null != metric.getFreeMemory()) && (null != metric.getCachedMemory())
						&& (null != metric.getBufferedMemory())) {
					/*
					 * memory in MB.
					 */
					float freeMemory = (metric.getFreeMemory() + metric.getCachedMemory() + metric.getBufferedMemory())
							/ 1024;
					load.setFreeMemory(freeMemory);
				} else {
					load.setFreeMemory(null);
				}
				load.setFreeDisk(metric.getFreeDisk()); // GB

				if (null != metric.getReceivedBytes()) {
					/*
					 * rate in KB/s
					 */
					float bytesIn = metric.getReceivedBytes() / 1024;
					load.setBytesIn(bytesIn);
				} else {
					load.setBytesIn(null);
				}

				if (null != metric.getSentBytes()) {
					/*
					 * rate in KB/s
					 */
					float bytesOut = metric.getSentBytes() / 1024;
					load.setBytesOut(bytesOut);
				} else {
					load.setBytesOut(null);
				}

				load.setReportTime(metric.getReportedTime());

				load.persist();
				load.flush();
				load.clear();

				/*
				 * delete outdated metric
				 */
				long recordCount = PhysicalMachineLoad.countPhysicalMachineLoads(hostName);
				while (recordCount > keepRecords) {
					PhysicalMachineLoad.roundRobinPhysicalMachineLoad(hostName);
					recordCount = PhysicalMachineLoad.countPhysicalMachineLoads(hostName);
				}
			}
		}

		/**
		 * save monitor history for all physical machines. the monitor history include all load metrics and services.
		 * and for each metric and service, a new history record is added if the monitor status changes.
		 * 
		 * @author xiangqian
		 */
		private void savePhysicalMachineMonitorHistory() {
			List<PhysicalMachine> machines = PhysicalMachine.findAllPhysicalMachines();
			for (PhysicalMachine machine : machines) {
				try {
					savePhysicalMachineLoadMonitorHistory(machine);
				} catch (Exception e) {
					// continue to handle
					logger.error("物理服务器监控服务发生错误，保存节点负载监控历史发生错误", e);
				}

				try {
					savePhysicalMachineServiceMonitorHistory(machine);
				} catch (Exception e) {
					// continue to handle
					logger.error("物理服务器监控服务发生错误，保存节点服务监控历史发生错误", e);
				}
			}
		}

		/**
		 * save load monitor history for a physical machine. the load monitor history include all load metrics. and for
		 * each metric, a new history record is added if the monitor status changes.
		 * 
		 * @param machine
		 *            - physical machine
		 * @author xiangqian
		 */
		private void savePhysicalMachineLoadMonitorHistory(PhysicalMachine machine) {
			MonitorSourceEnum monitorSource = MonitorSourceEnum.PHYSICAL_MACHINE;
			String hostId = machine.getHostId();
			MonitorTypeEnum monitorType = MonitorTypeEnum.LOAD;

			PhysicalMachineLoad latestLoad = PhysicalMachineLoad.getLatestPhyscalMachineLoad(machine.getHostName());
			List<MonitorSetting> monitorSettings = MonitorSetting.getMonitorSettings(hostId, monitorType);
			for (MonitorSetting monitorSetting : monitorSettings) {
				if (monitorSetting.getEnabled() == false) {
					/*
					 * skip if the monitor is disabled.
					 */
					continue;
				}

				MonitorNameEnum monitorName = monitorSetting.getMonitorName();
				MonitorStatusEnum monitorStatus;
				String message = null;
				AlarmSeverityEnum severityLevel = null;

				float alarmThreshold = monitorSetting.getThreshold();
				String thresholdUnit = monitorSetting.getThresholdUnit();
				float currentLoad;
				switch (monitorName) {
				case PM_CPU_UTIL:
					if ((null == latestLoad) || (null == latestLoad.getCpuLoad())) {
						/*
						 * set monitor status to unknown if load data is not found.
						 */
						monitorStatus = MonitorStatusEnum.UNKNOWN;
					} else {
						currentLoad = latestLoad.getCpuLoad();

						if (currentLoad > alarmThreshold) {
							/*
							 * set monitor status to warning if load data surpass threshold.
							 */
							monitorStatus = MonitorStatusEnum.WARNING;
							message = "物理服务器" + machine.getHostName() + "的处理器利用率超出告警阈值服务器ID：" + hostId + "，预设告警阈值："
									+ alarmThreshold + thresholdUnit + "，当前处理器负载：" + currentLoad + thresholdUnit;
							severityLevel = monitorSetting.getSeverityLevel();
						} else {
							/*
							 * set monitor status to normal if load data below threshold.
							 */
							monitorStatus = MonitorStatusEnum.NORMAL;
						}
					}

					break;
				case PM_MEMORY_USAGE:
					if ((null == latestLoad) || (null == latestLoad.getCpuLoad())) {
						/*
						 * set monitor status to unknown if load data is not found.
						 */
						monitorStatus = MonitorStatusEnum.UNKNOWN;
					} else {
						currentLoad = (latestLoad.getFreeMemory() / machine.getMemorySize()) * 100;

						if (currentLoad > alarmThreshold) {
							/*
							 * set monitor status to warning if load data surpass threshold.
							 */
							monitorStatus = MonitorStatusEnum.WARNING;
							message = "物理服务器" + machine.getHostName() + "的内存利用率超出告警阈值服务器ID：" + hostId + "，预设告警阈值："
									+ alarmThreshold + thresholdUnit + "，当前内存利用率：" + currentLoad + thresholdUnit;
							severityLevel = monitorSetting.getSeverityLevel();
						} else {
							/*
							 * set monitor status to normal if load data below threshold.
							 */
							monitorStatus = MonitorStatusEnum.NORMAL;
						}
					}

					break;
				case PM_NETWORK_OUTGOING_BYTES_RATE:
					if ((null == latestLoad) || (null == latestLoad.getCpuLoad())) {
						/*
						 * set monitor status to unknown if load data is not found.
						 */
						monitorStatus = MonitorStatusEnum.UNKNOWN;
					} else {
						currentLoad = latestLoad.getBytesOut();

						if (currentLoad > alarmThreshold) {
							/*
							 * set monitor status to warning if load data surpass threshold.
							 */
							monitorStatus = MonitorStatusEnum.WARNING;
							message = "物理服务器" + machine.getHostName() + "的网络流出速率超出告警阈值服务器ID：" + hostId + "，预设告警阈值："
									+ alarmThreshold + thresholdUnit + "，当前网络流出速率：" + currentLoad + thresholdUnit;
							severityLevel = monitorSetting.getSeverityLevel();
						} else {
							/*
							 * set monitor status to normal if load data below threshold.
							 */
							monitorStatus = MonitorStatusEnum.NORMAL;
						}
					}

					break;
				case PM_NETWORK_INCOMING_BYTES_RATE:
					if ((null == latestLoad) || (null == latestLoad.getCpuLoad())) {
						/*
						 * set monitor status to unknown if load data is not found.
						 */
						monitorStatus = MonitorStatusEnum.UNKNOWN;
					} else {
						currentLoad = latestLoad.getBytesIn();

						if (currentLoad > alarmThreshold) {
							/*
							 * set monitor status to warning if load data surpass threshold.
							 */
							monitorStatus = MonitorStatusEnum.WARNING;
							message = "物理服务器" + machine.getHostName() + "的网络流入速率超出告警阈值服务器ID：" + hostId + "，预设告警阈值："
									+ alarmThreshold + thresholdUnit + "，当前网络流入速率：" + currentLoad + thresholdUnit;
							severityLevel = monitorSetting.getSeverityLevel();
						} else {
							/*
							 * set monitor status to normal if load data below threshold.
							 */
							monitorStatus = MonitorStatusEnum.NORMAL;
						}
					}

					break;
				default:
					continue;
				}

				/*
				 * save monitor history
				 */
				saveMonitorLog(monitorSource, hostId, monitorType, monitorName, monitorStatus, message, severityLevel);
			}
		}

		/**
		 * save service monitor history for a physical machine. the service monitor history include all services. and
		 * for each service, a new history record is added if the monitor status changes.
		 * 
		 * @param machine
		 *            - physical machine
		 * @author xiangqian
		 */
		private void savePhysicalMachineServiceMonitorHistory(PhysicalMachine machine) {
			MonitorSourceEnum monitorSource = MonitorSourceEnum.PHYSICAL_MACHINE;
			String hostId = machine.getHostId();
			MonitorTypeEnum monitorType = MonitorTypeEnum.SERVICE;

			NagiosMonitor nagios = new NagiosMonitor(nagiosConfig);
			List<ServiceStatus> serviceStatusList = nagios.getHostServiceStatus(machine.getHostName());

			List<MonitorSetting> monitorSettings = MonitorSetting.getMonitorSettings(hostId, monitorType);
			for (MonitorSetting monitorSetting : monitorSettings) {
				if (monitorSetting.getEnabled() == false) {
					/*
					 * skip if the monitor is disabled.
					 */
					continue;
				}

				MonitorNameEnum monitorName = monitorSetting.getMonitorName();
				MonitorStatusEnum monitorStatus;
				String message = null;
				AlarmSeverityEnum severityLevel = null;

				/*
				 * find from the nagios monitor result.
				 */
				ServiceStatus nagiosServiceStatus = null;
				for (ServiceStatus serviceStatus : serviceStatusList) {
					if (true == serviceStatus.getServiceName().equalsIgnoreCase(monitorName.name())) {
						nagiosServiceStatus = serviceStatus;
					}
				}

				if (null == nagiosServiceStatus) {
					/*
					 * set monitor status to unknown if nagios monitor result is not found.
					 */
					monitorStatus = MonitorStatusEnum.UNKNOWN;
				} else {
					/*
					 * set monitor status and message based on the nagios monitor result.
					 */
					switch (nagiosServiceStatus.getServiceStatus()) {
					case CRITICAL:
					case WARNING:
						monitorStatus = MonitorStatusEnum.WARNING;

						switch (monitorName) {
						case CONNECTIVITY:
							message = "物理服务器" + machine.getHostName() + "无法连通";
							break;
						case AODH_API:
						case AODH_EVALUATOR:
						case AODH_LISTENER:
						case AODH_NOTIFIER:
						case CEILOMETER_API:
						case CEILOMETER_CENTRAL:
						case CEILOMETER_COLLECTOR:
						case CEILOMETER_NOTIFICATION:
						case CINDER_API:
						case CINDER_SCHEDULER:
						case CINDER_VOLUME:
						case GLANCE_API:
						case GLANCE_REGISTRY:
						case KEYSTONE:
						case MONGOD:
						case NEUTRON_DHCP_AGENT:
						case NEUTRON_L3_AGENT:
						case NEUTRON_METADATA_AGENT:
						case NEUTRON_OPENVSWITCH_AGENT:
						case NEUTRON_SERVER:
						case NOVA_API:
						case NOVA_CONDUCTOR:
						case NOVA_CONSOLEAUTH:
						case NOVA_NOVNCPROXY:
						case NOVA_SCHEDULER:
						case REDIS_SERVER:
						case CEILOMETER_COMPUTE:
						case LIBVIRTD:
						case NOVA_COMPUTE:
							Monitor monitor = Monitor.getMonitor(monitorName);
							if (null != monitor) {
								message = "物理服务器" + machine.getHostName() + "上的" + monitor.getDescription() + "服务中止";
							} else {
								message = "物理服务器" + machine.getHostName() + "上的服务中止";
							}
							break;
						default:
							break;
						}

						break;
					case OK:
						monitorStatus = MonitorStatusEnum.NORMAL;
						break;
					case PENDING:
					case UNKNOWN:
					default:
						monitorStatus = MonitorStatusEnum.UNKNOWN;
						break;
					}
				}

				/*
				 * save monitor history
				 */
				saveMonitorLog(monitorSource, hostId, monitorType, monitorName, monitorStatus, message, severityLevel);
			}
		}
	}

	private class PhysicalMachineAlarmTask implements Runnable {
		private boolean fireBullet;

		@Override
		public void run() {
			fireBullet = true;

			while (true == fireBullet) {
				try {
					Thread.sleep(appConfig.getPortalLoopInterval() * 1000);
				} catch (InterruptedException e) {
					fireBullet = false;

					logger.error("物理服务器告警服务发生错误，监控被中断", e);
				} catch (Exception e) {
					logger.error("物理服务器告警服务发生错误", e);
				}

				try {
					savePhysicalMachineMonitorResult();
				} catch (Exception e) {
					logger.error("物理服务器告警服务发生错误，保存节点监控历史发生错误", e);
				}
			}
		}

		/**
		 * save monitor result for all physical machines. the monitor result is based on the latest monitor history of
		 * all load metrics and services.
		 * 
		 * @author xiangqian
		 */
		private void savePhysicalMachineMonitorResult() {
			List<PhysicalMachine> machines = PhysicalMachine.findAllPhysicalMachines();
			for (PhysicalMachine machine : machines) {
				try {
					savePhysicalMachineLoadMonitorResult(machine);
				} catch (Exception e) {
					// continue to handle
					logger.error("物理服务器告警服务发生错误，保存节点负载监控结果发生错误", e);
				}

				try {
					savePhysicalMachineServiceMonitorResult(machine);
				} catch (Exception e) {
					// continue to handle
					logger.error("物理服务器告警服务发生错误，保存节点服务监控结果发生错误", e);
				}

				try {
					savePhysicalMachineNodeMonitorResult(machine);
				} catch (Exception e) {
					// continue to handle
					logger.error("物理服务器告警服务发生错误，保存节点监控结果发生错误", e);
				}
			}
		}

		/**
		 * save load monitor result for a physical machine. each load monitor result is based on the latest monitor
		 * history.
		 * 
		 * @param machine
		 *            - physical machine
		 * @author xiangqian
		 */
		private void savePhysicalMachineLoadMonitorResult(PhysicalMachine machine) {
			MonitorSourceEnum monitorSource = MonitorSourceEnum.PHYSICAL_MACHINE;
			String hostId = machine.getHostId();
			MonitorTypeEnum monitorType = MonitorTypeEnum.LOAD;

			List<MonitorSetting> monitorSettings = MonitorSetting.getMonitorSettings(hostId, monitorType);
			for (MonitorSetting monitorSetting : monitorSettings) {
				if (monitorSetting.getEnabled() == false) {
					/*
					 * skip if the monitor is disabled.
					 */
					continue;
				}

				MonitorNameEnum monitorName = monitorSetting.getMonitorName();
				MonitorStatusEnum monitorStatus = null;
				MonitorLog latestHistory = MonitorLog.getLatestMonitorLog(hostId, monitorType, monitorName);
				if ((PhysicalMachineStatusEnum.ACTIVE != machine.getStatus()) || (null == latestHistory)) {
					/*
					 * set machine status to unknown if machine status is not active, or the latest load metric monitor
					 * history is not found.
					 */
					monitorStatus = MonitorStatusEnum.UNKNOWN;
				} else {
					/*
					 * set monitor status to the latest load metric monitor status.
					 */
					monitorStatus = latestHistory.getMonitorStatus();
				}

				/*
				 * save monitor result.
				 */
				saveMonitorResult(monitorSource, hostId, monitorType, monitorName, monitorStatus);
			}
		}

		/**
		 * save service monitor result for a physical machine. each service monitor result is based on the latest
		 * monitor history.
		 * 
		 * @param machine
		 *            - physical machine
		 * @author xiangqian
		 */
		private void savePhysicalMachineServiceMonitorResult(PhysicalMachine machine) {
			MonitorSourceEnum monitorSource = MonitorSourceEnum.PHYSICAL_MACHINE;
			String hostId = machine.getHostId();
			MonitorTypeEnum monitorType = MonitorTypeEnum.SERVICE;

			List<MonitorSetting> monitorSettings = MonitorSetting.getMonitorSettings(hostId, monitorType);
			for (MonitorSetting monitorSetting : monitorSettings) {
				if (monitorSetting.getEnabled() == false) {
					/*
					 * skip if the monitor is disabled.
					 */
					continue;
				}

				MonitorNameEnum monitorName = monitorSetting.getMonitorName();
				MonitorStatusEnum monitorStatus = null;
				MonitorLog latestHistory = MonitorLog.getLatestMonitorLog(hostId, monitorType, monitorName);
				if ((PhysicalMachineStatusEnum.ACTIVE != machine.getStatus()) || (null == latestHistory)) {
					/*
					 * set machine status to unknown if machine status is not active, or the latest service monitor
					 * history is not found.
					 */
					monitorStatus = MonitorStatusEnum.UNKNOWN;
				} else {
					/*
					 * set monitor status to the latest load metric monitor status.
					 */
					monitorStatus = latestHistory.getMonitorStatus();
				}

				/*
				 * save monitor result.
				 */
				saveMonitorResult(monitorSource, hostId, monitorType, monitorName, monitorStatus);
			}
		}

		/**
		 * save node monitor result for a physical machine. the node monitor result is based on all the machine's load
		 * monitor results and service monitor results.
		 * 
		 * @param machine
		 *            - physical machine
		 * @author xiangqian
		 */
		private void savePhysicalMachineNodeMonitorResult(PhysicalMachine machine) {
			MonitorSourceEnum monitorSource = MonitorSourceEnum.PHYSICAL_MACHINE;
			String hostId = machine.getHostId();
			MonitorTypeEnum monitorType = MonitorTypeEnum.NODE;
			MonitorNameEnum monitorName = MonitorNameEnum.NONE;

			int MONITOR_STATUS_ACTIVE = 1;
			int MONITOR_STATUS_WARNING = 2;
			int MONITOR_STATUS_UNKNOWN = 4;
			int statusCode = 0;

			List<MonitorResult> monitorResults;
			/*
			 * load monitor result
			 */
			monitorResults = MonitorResult.getMonitorResults(hostId, MonitorTypeEnum.LOAD);
			for (MonitorResult monitorResult : monitorResults) {
				MonitorSetting monitorSetting = MonitorSetting.getMonitorSetting(hostId,
						monitorResult.getMonitorName());
				if ((null == monitorSetting) || (false == monitorSetting.getEnabled())) {
					/*
					 * skip.
					 */
					continue;
				}

				/*
				 * set status code based on the machine's load monitor results.
				 */
				if (MonitorStatusEnum.NORMAL == monitorResult.getMonitorStatus()) {
					statusCode |= MONITOR_STATUS_ACTIVE;
				} else if (MonitorStatusEnum.WARNING == monitorResult.getMonitorStatus()) {
					statusCode |= MONITOR_STATUS_WARNING;
				} else {
					statusCode |= MONITOR_STATUS_UNKNOWN;
				}
			}

			/*
			 * service monitor result
			 */
			monitorResults = MonitorResult.getMonitorResults(hostId, MonitorTypeEnum.SERVICE);
			for (MonitorResult monitorResult : monitorResults) {
				MonitorSetting monitorSetting = MonitorSetting.getMonitorSetting(hostId,
						monitorResult.getMonitorName());
				if ((null == monitorSetting) || (false == monitorSetting.getEnabled())) {
					/*
					 * skip.
					 */
					continue;
				}

				/*
				 * set status code based on the machine's service monitor results.
				 */
				if (MonitorStatusEnum.NORMAL == monitorResult.getMonitorStatus()) {
					statusCode |= MONITOR_STATUS_ACTIVE;
				} else if (MonitorStatusEnum.WARNING == monitorResult.getMonitorStatus()) {
					statusCode |= MONITOR_STATUS_WARNING;
				} else {
					statusCode |= MONITOR_STATUS_UNKNOWN;
				}
			}

			/*
			 * get monitor status from the highest bit of status code.
			 */
			MonitorStatusEnum monitorStatus = null;
			if (MONITOR_STATUS_UNKNOWN == (statusCode & MONITOR_STATUS_UNKNOWN)) {
				monitorStatus = MonitorStatusEnum.UNKNOWN;
			} else if (MONITOR_STATUS_WARNING == (statusCode & MONITOR_STATUS_WARNING)) {
				monitorStatus = MonitorStatusEnum.WARNING;
			} else if (MONITOR_STATUS_ACTIVE == (statusCode & MONITOR_STATUS_ACTIVE)) {
				monitorStatus = MonitorStatusEnum.NORMAL;
			} else {
				/*
				 * status code is the initial value.
				 */
				monitorStatus = MonitorStatusEnum.UNKNOWN;
			}

			/*
			 * save monitor result
			 */
			saveMonitorResult(monitorSource, hostId, monitorType, monitorName, monitorStatus);
		}
	}
}
