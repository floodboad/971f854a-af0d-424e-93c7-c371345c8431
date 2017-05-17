package com.sinoparasoft.task;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
public class GangliaMetric {
	/**
	 * Host - Name
	 */
	private String name;
	/**
	 * Host - IP Address
	 */
	private String ip;
	/**
	 * Host - Location
	 */
	private String location;
	/**
	 * Host - Report Time
	 */
	private Date reportedTime;
	/**
	 * Host - GMOND Started Time
	 */
	private Date gmondStartedTime;

	/**
	 * System - Operating System Release
	 */
	private String osRelease;
	/**
	 * System - Operating system name
	 */
	private String osName;
	/**
	 * System - The last time that the system was started
	 */
	private Date lastBootTime;
	/**
	 * System - System architecture
	 */
	private String machineType;

	/**
	 * Load - One Minute Load Average
	 */
	private Float avgOneMinuteLoad; 
	/**
	 * Load - Five Minute Load Average
	 */
	private Float avgFiveMinuteLoad;
	/**
	 * Load - Fifteen Minute Load Average
	 */
	private Float avgFifteenMinuteLoad;

	/**
	 * Memory - Total amount of memory displayed in KBs
	 */
	private Float totalMemory;   
	/**
	 * Memory - Amount of available memory
	 */
	private Float freeMemory; 
	/**
	 * Memory - Amount of cached memory
	 */
	private Float cachedMemory;
	/**
	 * Memory - Amount of buffered memory
	 */
	private Float bufferedMemory;
	/**
	 * Memory - Amount of shared memory
	 */
	private Float sharedMemory;
	/**
	 * Memory - Total amount of swap space displayed in KBs
	 */
	private Float totalSwap;
	/**
	 * Memory - Amount of available swap memory
	 */
	private Float freeSwap;

	/**
	 * Process - Total number of processes
	 */
	private Integer totalProcesses;
	/**
	 * Process - Total number of running processes
	 */
	private Integer runningProcesses;

	/**
	 * Core - Gexec Status
	 */
	private Boolean gexecAvailable;

	/**
	 * Disk - Total available disk space
	 */
	private Float totalDisk;  
	/**
	 * Disk - Total free disk space
	 */
	private Float freeDisk;  
	/**
	 * Disk - Maximum percent used for all partitions
	 */
	private Float maxUsedDiskSpacePercent;
	/**
	 * Disk - Used disk space
	 */
	private Map<String, Float> usedDiskMap;
	/**
	 * Disk - Available disk space
	 */
	private Map<String, Float> totalDiskMap;

	/**
	 * CPU - Percentage of time that the CPU or CPUs were idle and the system
	 * did not have an outstanding disk I/O request
	 */
	private Float cpuIdlePercent;
	/**
	 * CPU - Percentage of CPU utilization that occurred while executing at the
	 * user level with nice priority
	 */
	private Float cpuNicePercent;
	/**
	 * CPU - Percentage of CPU utilization that occurred while executing at the
	 * user level
	 */
	private Float cpuUserPercent;
	/**
	 * CPU - Percent of time since boot idle CPU
	 */
	private Float cpuAidlePercent;
	/**
	 * CPU - Percentage of CPU utilization that occurred while executing at the
	 * system level
	 */
	private Float cpuSystemPercent;
	/**
	 * CPU - Percentage of time that the CPU or CPUs were idle during which the
	 * system had an outstanding disk I/O request
	 */
	private Float cpuWioPercent;
	/**
	 * CPU - Total number of CPUs
	 */
	private Integer cpuNum;
	/**
	 * CPU - CPU Speed in terms of MHz
	 */
	private Integer cpuSpeed;

	/**
	 * Network - Total number of established TCP connections
	 */
	private Integer establishedTcpConnections;
	/**
	 * Network - Total number of listening TCP connections
	 */
	private Integer listeningTcpConnections;
	/**
	 * Network - Total number of syn_wait TCP connections
	 */
	private Integer syncWaitTcpConnections;
	/**
	 * Network - Total number of syn_sent TCP connections
	 */
	private Integer syncSentTcpConnections;
	/**
	 * Network - Total number of syn_recv TCP connections
	 */
	private Integer syncRecvTcpConnections;
	/**
	 * Network - Total number of last_ack TCP connections
	 */
	private Integer lastAckTcpConnections;
	/**
	 * Network - Total number of fin_wait1 TCP connections
	 */
	private Integer finWait1TcpConnections;
	/**
	 * Network - Total number of time_wait TCP connections
	 */
	private Integer timeWaitTcpConnections;
	/**
	 * Network - Total number of fin_wait2 TCP connections
	 */
	private Integer finWait2TcpConnections;
	/**
	 * Network - Total number of close_wait TCP connections
	 */
	private Integer closeWaitTcpConnections;
	/**
	 * Network - Total number of closing TCP connections
	 */
	private Integer closingTcpConnections;
	/**
	 * Network - Total number of closed TCP connections
	 */
	private Integer closedTcpConnections;
	/**
	 * Network - Total number of unknown TCP connections
	 */
	private Integer unknownTcpConnections;
	/**
	 * Network - Packets in per second
	 */
	private Float receivedPackets;
	/**
	 * Network - Packets out per second
	 */
	private Float sentPackets; 
	/**
	 * Network - Number of bytes in per second
	 */
	private Float receivedBytes;  
	/**
	 * Network - Number of bytes out per second
	 */
	private Float sentBytes;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public Date getReportedTime() {
		return reportedTime;
	}

	public void setReportedTime(Date reportedTime) {
		this.reportedTime = reportedTime;
	}

	public void setReportedTime(String reportedTime) {
		if (reportedTime != null && reportedTime.isEmpty() == false) {
			this.reportedTime = new Date(1000 * Long.parseLong(reportedTime));
		}
	}

	public Date getGmondStartedTime() {
		return gmondStartedTime;
	}

	public void setGmondStartedTime(Date gmondStartedTime) {
		this.gmondStartedTime = gmondStartedTime;
	}

	public void setGmondStartedTime(String gmondStartedTime) {
		if (gmondStartedTime != null && gmondStartedTime.isEmpty() == false) {
			this.gmondStartedTime = new Date(
					1000 * Long.parseLong(gmondStartedTime));
		}
	}

	public String getOsRelease() {
		return osRelease;
	}

	public void setOsRelease(String osRelease) {
		this.osRelease = osRelease;
	}

	public String getOsName() {
		return osName;
	}

	public void setOsName(String osName) {
		this.osName = osName;
	}

	public Date getLastBootTime() {
		return lastBootTime;
	}

	public void setLastBootTime(Date lastBootTime) {
		this.lastBootTime = lastBootTime;
	}

	public void setLastBootTime(String lastBootTime) {
		if (lastBootTime != null && lastBootTime.isEmpty() == false) {
			this.lastBootTime = new Date(1000 * Long.parseLong(lastBootTime));
		}
	}

	public String getMachineType() {
		return machineType;
	}

	public void setMachineType(String machineType) {
		this.machineType = machineType;
	}

	public Float getAvgOneMinuteLoad() {
		return avgOneMinuteLoad;
	}

	public void setAvgOneMinuteLoad(Float avgOneMinuteLoad) {
		this.avgOneMinuteLoad = avgOneMinuteLoad;
	}

	public void setAvgOneMinuteLoad(String avgOneMinuteLoad) {
		if (avgOneMinuteLoad != null && avgOneMinuteLoad.isEmpty() == false) {
			this.avgOneMinuteLoad = Float.parseFloat(avgOneMinuteLoad);
		}
	}

	public Float getAvgFiveMinuteLoad() {
		return avgFiveMinuteLoad;
	}

	public void setAvgFiveMinuteLoad(Float avgFiveMinuteLoad) {
		this.avgFiveMinuteLoad = avgFiveMinuteLoad;
	}

	public void setAvgFiveMinuteLoad(String avgFiveMinuteLoad) {
		if (avgFiveMinuteLoad != null && avgFiveMinuteLoad.isEmpty() == false) {
			this.avgFiveMinuteLoad = Float.parseFloat(avgFiveMinuteLoad);
		}
	}

	public Float getAvgFifteenMinuteLoad() {
		return avgFifteenMinuteLoad;
	}

	public void setAvgFifteenMinuteLoad(Float avgFifteenMinuteLoad) {
		this.avgFifteenMinuteLoad = avgFifteenMinuteLoad;
	}

	public void setAvgFifteenMinuteLoad(String avgFifteenMinuteLoad) {
		if (avgFifteenMinuteLoad != null
				&& avgFifteenMinuteLoad.isEmpty() == false) {
			this.avgFifteenMinuteLoad = Float.parseFloat(avgFifteenMinuteLoad);
		}
	}

	public Float getTotalMemory() {
		return totalMemory;
	}

	public void setTotalMemory(Float totalMemory) {
		this.totalMemory = totalMemory;
	}

	public void setTotalMemory(String totalMemory) {
		if (totalMemory != null && totalMemory.isEmpty() == false) {
			this.totalMemory = Float.parseFloat(totalMemory);
		}
	}
	
	public Boolean hasTotalMemory() {
		Boolean retValue = false;
		if(this.totalMemory != null) {
			retValue = true;
		}
		
		return retValue;
	}

	public Float getFreeMemory() {
		return freeMemory;
	}

	public void setFreeMemory(Float freeMemory) {
		this.freeMemory = freeMemory;
	}

	public void setFreeMemory(String freeMemory) {
		if (freeMemory != null && freeMemory.isEmpty() == false) {
			this.freeMemory = Float.parseFloat(freeMemory);
		}
	}
	
	public Boolean hasFreeMemory() {
		Boolean retValue = false;
		if(this.freeMemory != null) {
			retValue = true;
		}
		
		return retValue;
	}

	public Float getCachedMemory() {
		return cachedMemory;
	}

	public void setCachedMemory(Float cachedMemory) {
		this.cachedMemory = cachedMemory;
	}

	public void setCachedMemory(String cachedMemory) {
		if (cachedMemory != null && cachedMemory.isEmpty() == false) {
			this.cachedMemory = Float.parseFloat(cachedMemory);
		}
	}

	public Float getBufferedMemory() {
		return bufferedMemory;
	}

	public void setBufferedMemory(Float bufferedMemory) {
		this.bufferedMemory = bufferedMemory;
	}

	public void setBufferedMemory(String bufferedMemory) {
		if (bufferedMemory != null && bufferedMemory.isEmpty() == false) {
			this.bufferedMemory = Float.parseFloat(bufferedMemory);
		}
	}

	public Float getSharedMemory() {
		return sharedMemory;
	}

	public void setSharedMemory(Float sharedMemory) {
		this.sharedMemory = sharedMemory;
	}

	public void setSharedMemory(String sharedMemory) {
		if (sharedMemory != null && sharedMemory.isEmpty() == false) {
			this.sharedMemory = Float.parseFloat(sharedMemory);
		}
	}

	public Float getTotalSwap() {
		return totalSwap;
	}

	public void setTotalSwap(Float totalSwap) {
		this.totalSwap = totalSwap;
	}

	public void setTotalSwap(String totalSwap) {
		if (totalSwap != null && totalSwap.isEmpty() == false) {
			this.totalSwap = Float.parseFloat(totalSwap);
		}
	}

	public Float getFreeSwap() {
		return freeSwap;
	}

	public void setFreeSwap(Float freeSwap) {
		this.freeSwap = freeSwap;
	}

	public void setFreeSwap(String freeSwap) {
		if (freeSwap != null && freeSwap.isEmpty() == false) {
			this.freeSwap = Float.parseFloat(freeSwap);
		}
	}

	public Integer getTotalProcesses() {
		return totalProcesses;
	}

	public void setTotalProcesses(Integer totalProcesses) {
		this.totalProcesses = totalProcesses;
	}

	public void setTotalProcesses(String totalProcesses) {
		if (totalProcesses != null && totalProcesses.isEmpty() == false) {
			this.totalProcesses = Integer.parseInt(totalProcesses);
		}
	}

	public Integer getRunningProcesses() {
		return runningProcesses;
	}

	public void setRunningProcesses(Integer runningProcesses) {
		this.runningProcesses = runningProcesses;
	}

	public void setRunningProcesses(String runningProcesses) {
		if (runningProcesses != null && runningProcesses.isEmpty() == false) {
			this.runningProcesses = Integer.parseInt(runningProcesses);
		}
	}

	public Boolean getGexecAvailable() {
		return gexecAvailable;
	}

	public void setGexecAvailable(Boolean gexecAvailable) {
		this.gexecAvailable = gexecAvailable;
	}

	public void setGexecAvailable(String gexecAvailable) {
		if (gexecAvailable != null && gexecAvailable.isEmpty() == false) {
			if (gexecAvailable.equals("OFF")) {
				this.gexecAvailable = false;
			} else {
				this.gexecAvailable = true;
			}
		}
	}

	public Float getTotalDisk() {
		return totalDisk;
	}

	public void setTotalDisk(Float totalDisk) {
		this.totalDisk = totalDisk;
	}

	public void setTotalDisk(String totalDisk) {
		if (totalDisk != null && totalDisk.isEmpty() == false) {
			this.totalDisk = Float.parseFloat(totalDisk);
		}
	}

	public Float getFreeDisk() {
		return freeDisk;
	}

	public void setFreeDisk(Float freeDisk) {
		this.freeDisk = freeDisk;
	}

	public void setFreeDisk(String freeDisk) {
		if (freeDisk != null && freeDisk.isEmpty() == false) {
			this.freeDisk = Float.parseFloat(freeDisk);
		}
	}

	public Float getMaxUsedDiskSpacePercent() {
		return maxUsedDiskSpacePercent;
	}

	public void setMaxUsedDiskSpacePercent(Float maxUsedDiskSpacePercent) {
		this.maxUsedDiskSpacePercent = maxUsedDiskSpacePercent;
	}

	public void setMaxUsedDiskSpacePercent(String maxUsedDiskSpacePercent) {
		if (maxUsedDiskSpacePercent != null
				&& maxUsedDiskSpacePercent.isEmpty() == false) {
			this.maxUsedDiskSpacePercent = Float
					.parseFloat(maxUsedDiskSpacePercent);
		}
	}

	public Map<String, Float> getUsedDiskMap() {
		return usedDiskMap;
	}

	public void setUsedDiskMap(Map<String, Float> usedDiskMap) {
		this.usedDiskMap = usedDiskMap;
	}

	public void addUsedDiskMap(String disk, Float percent) {
		if (this.usedDiskMap == null) {
			this.usedDiskMap = new HashMap<String, Float>();
		}
		this.usedDiskMap.put(disk, percent);
	}

	public Map<String, Float> getTotalDiskMap() {
		return totalDiskMap;
	}

	public void setTotalDiskMap(Map<String, Float> totalDiskMap) {
		this.totalDiskMap = totalDiskMap;
	}

	public void addTotalDiskMap(String disk, Float percent) {
		if (this.totalDiskMap == null) {
			this.totalDiskMap = new HashMap<String, Float>();
		}
		this.totalDiskMap.put(disk, percent);
	}

	public Float getCpuIdlePercent() {
		return cpuIdlePercent;
	}

	public void setCpuIdlePercent(Float cpuIdlePercent) {
		this.cpuIdlePercent = cpuIdlePercent;
	}

	public void setCpuIdlePercent(String cpuIdlePercent) {
		if (cpuIdlePercent != null && cpuIdlePercent.isEmpty() == false) {
			this.cpuIdlePercent = Float.parseFloat(cpuIdlePercent);
		}
	}

	public Float getCpuNicePercent() {
		return cpuNicePercent;
	}

	public void setCpuNicePercent(Float cpuNicePercent) {
		this.cpuNicePercent = cpuNicePercent;
	}

	public void setCpuNicePercent(String cpuNicePercent) {
		if (cpuNicePercent != null && cpuNicePercent.isEmpty() == false) {
			this.cpuNicePercent = Float.parseFloat(cpuNicePercent);
		}
	}

	public Float getCpuUserPercent() {
		return cpuUserPercent;
	}

	public void setCpuUserPercent(Float cpuUserPercent) {
		this.cpuUserPercent = cpuUserPercent;
	}

	public void setCpuUserPercent(String cpuUserPercent) {
		if (cpuUserPercent != null && cpuUserPercent.isEmpty() == false) {
			this.cpuUserPercent = Float.parseFloat(cpuUserPercent);
		}
	}

	public Float getCpuAidlePercent() {
		return cpuAidlePercent;
	}

	public void setCpuAidlePercent(Float cpuAidlePercent) {
		this.cpuAidlePercent = cpuAidlePercent;
	}

	public void setCpuAidlePercent(String cpuAidlePercent) {
		this.cpuAidlePercent = Float.parseFloat(cpuAidlePercent);
	}

	public Float getCpuSystemPercent() {
		return cpuSystemPercent;
	}

	public void setCpuSystemPercent(Float cpuSystemPercent) {
		this.cpuSystemPercent = cpuSystemPercent;
	}

	public void setCpuSystemPercent(String cpuSystemPercent) {
		if (cpuSystemPercent != null && cpuSystemPercent.isEmpty() == false) {
			this.cpuSystemPercent = Float.parseFloat(cpuSystemPercent);
		}
	}

	public Float getCpuWioPercent() {
		return cpuWioPercent;
	}

	public void setCpuWioPercent(Float cpuWioPercent) {
		this.cpuWioPercent = cpuWioPercent;
	}

	public void setCpuWioPercent(String cpuWioPercent) {
		if (cpuWioPercent != null && cpuWioPercent.isEmpty() == false) {
			this.cpuWioPercent = Float.parseFloat(cpuWioPercent);
		}
	}

	public Integer getCpuNum() {
		return cpuNum;
	}

	public void setCpuNum(Integer cpuNum) {
		this.cpuNum = cpuNum;
	}

	public void setCpuNum(String cpuNum) {
		if (cpuNum != null && cpuNum.isEmpty() == false) {
			this.cpuNum = Integer.parseInt(cpuNum);
		}
	}

	public Integer getCpuSpeed() {
		return cpuSpeed;
	}

	public void setCpuSpeed(Integer cpuSpeed) {
		this.cpuSpeed = cpuSpeed;
	}
	
	

	public void setCpuSpeed(String cpuSpeed) {
		if (cpuSpeed != null && cpuSpeed.isEmpty() == false) {
			this.cpuSpeed = Integer.parseInt(cpuSpeed);
		}
	}
	
	public Boolean hasCpuSpeed() {
		Boolean retValue = false;
		if(this.cpuSpeed != null) {
			retValue = true;
		}
		
		return retValue;
	}

	public Integer getEstablishedTcpConnections() {
		return establishedTcpConnections;
	}

	public void setEstablishedTcpConnections(Integer establishedTcpConnections) {
		this.establishedTcpConnections = establishedTcpConnections;
	}

	public void setEstablishedTcpConnections(String establishedTcpConnections) {
		if (establishedTcpConnections != null
				&& establishedTcpConnections.isEmpty() == false) {
			this.establishedTcpConnections = Integer
					.parseInt(establishedTcpConnections);
		}
	}

	public Integer getListeningTcpConnections() {
		return listeningTcpConnections;
	}

	public void setListeningTcpConnections(Integer listeningTcpConnections) {
		this.listeningTcpConnections = listeningTcpConnections;
	}

	public void setListeningTcpConnections(String listeningTcpConnections) {
		if (listeningTcpConnections != null
				&& listeningTcpConnections.isEmpty() == false) {
			this.listeningTcpConnections = Integer
					.parseInt(listeningTcpConnections);
		}
	}

	public Integer getSyncWaitTcpConnections() {
		return syncWaitTcpConnections;
	}

	public void setSyncWaitTcpConnections(Integer syncWaitTcpConnections) {
		this.syncWaitTcpConnections = syncWaitTcpConnections;
	}

	public void setSyncWaitTcpConnections(String syncWaitTcpConnections) {
		if (syncWaitTcpConnections != null
				&& syncWaitTcpConnections.isEmpty() == false) {
			this.syncWaitTcpConnections = Integer
					.parseInt(syncWaitTcpConnections);
		}
	}

	public Integer getSyncSentTcpConnections() {
		return syncSentTcpConnections;
	}

	public void setSyncSentTcpConnections(Integer syncSentTcpConnections) {
		this.syncSentTcpConnections = syncSentTcpConnections;
	}

	public void setSyncSentTcpConnections(String syncSentTcpConnections) {
		if (syncSentTcpConnections != null
				&& syncSentTcpConnections.isEmpty() == false) {
			this.syncSentTcpConnections = Integer
					.parseInt(syncSentTcpConnections);
		}
	}

	public Integer getSyncRecvTcpConnections() {
		return syncRecvTcpConnections;
	}

	public void setSyncRecvTcpConnections(Integer syncRecvTcpConnections) {
		this.syncRecvTcpConnections = syncRecvTcpConnections;
	}

	public void setSyncRecvTcpConnections(String syncRecvTcpConnections) {
		if (syncRecvTcpConnections != null
				&& syncRecvTcpConnections.isEmpty() == false) {
			this.syncRecvTcpConnections = Integer
					.parseInt(syncRecvTcpConnections);
		}
	}

	public Integer getLastAckTcpConnections() {
		return lastAckTcpConnections;
	}

	public void setLastAckTcpConnections(Integer lastAckTcpConnections) {
		this.lastAckTcpConnections = lastAckTcpConnections;
	}

	public void setLastAckTcpConnections(String lastAckTcpConnections) {
		if (lastAckTcpConnections != null
				&& lastAckTcpConnections.isEmpty() == false) {
			this.lastAckTcpConnections = Integer
					.parseInt(lastAckTcpConnections);
		}
	}

	public Integer getFinWait1TcpConnections() {
		return finWait1TcpConnections;
	}

	public void setFinWait1TcpConnections(Integer finWait1TcpConnections) {
		this.finWait1TcpConnections = finWait1TcpConnections;
	}

	public void setFinWait1TcpConnections(String finWait1TcpConnections) {
		if (finWait1TcpConnections != null
				&& finWait1TcpConnections.isEmpty() == false) {
			this.finWait1TcpConnections = Integer
					.parseInt(finWait1TcpConnections);
		}
	}

	public Integer getTimeWaitTcpConnections() {
		return timeWaitTcpConnections;
	}

	public void setTimeWaitTcpConnections(Integer timeWaitTcpConnections) {
		this.timeWaitTcpConnections = timeWaitTcpConnections;
	}

	public void setTimeWaitTcpConnections(String timeWaitTcpConnections) {
		if (timeWaitTcpConnections != null
				&& timeWaitTcpConnections.isEmpty() == false) {
			this.timeWaitTcpConnections = Integer
					.parseInt(timeWaitTcpConnections);
		}
	}

	public Integer getFinWait2TcpConnections() {
		return finWait2TcpConnections;
	}

	public void setFinWait2TcpConnections(Integer finWait2TcpConnections) {
		this.finWait2TcpConnections = finWait2TcpConnections;
	}

	public void setFinWait2TcpConnections(String finWait2TcpConnections) {
		if (finWait2TcpConnections != null
				&& finWait2TcpConnections.isEmpty() == false) {
			this.finWait2TcpConnections = Integer
					.parseInt(finWait2TcpConnections);
		}
	}

	public Integer getCloseWaitTcpConnections() {
		return closeWaitTcpConnections;
	}

	public void setCloseWaitTcpConnections(Integer closeWaitTcpConnections) {
		this.closeWaitTcpConnections = closeWaitTcpConnections;
	}

	public void setCloseWaitTcpConnections(String closeWaitTcpConnections) {
		if (closeWaitTcpConnections != null
				&& closeWaitTcpConnections.isEmpty() == false) {
			this.closeWaitTcpConnections = Integer
					.parseInt(closeWaitTcpConnections);
		}
	}

	public Integer getClosingTcpConnections() {
		return closingTcpConnections;
	}

	public void setClosingTcpConnections(Integer closingTcpConnections) {
		this.closingTcpConnections = closingTcpConnections;
	}

	public void setClosingTcpConnections(String closingTcpConnections) {
		if (closingTcpConnections != null
				&& closingTcpConnections.isEmpty() == false) {
			this.closingTcpConnections = Integer
					.parseInt(closingTcpConnections);
		}
	}

	public Integer getClosedTcpConnections() {
		return closedTcpConnections;
	}

	public void setClosedTcpConnections(Integer closedTcpConnections) {
		this.closedTcpConnections = closedTcpConnections;
	}

	public void setClosedTcpConnections(String closedTcpConnections) {
		if (closedTcpConnections != null
				&& closedTcpConnections.isEmpty() == false) {
			this.closedTcpConnections = Integer.parseInt(closedTcpConnections);
		}
	}

	public Integer getUnknownTcpConnections() {
		return unknownTcpConnections;
	}

	public void setUnknownTcpConnections(Integer unknownTcpConnections) {
		this.unknownTcpConnections = unknownTcpConnections;
	}

	public void setUnknownTcpConnections(String unknownTcpConnections) {
		if (unknownTcpConnections != null
				&& unknownTcpConnections.isEmpty() == false) {
			this.unknownTcpConnections = Integer
					.parseInt(unknownTcpConnections);
		}
	}

	public Float getReceivedPackets() {
		return receivedPackets;
	}

	public void setReceivedPackets(Float receivedPackets) {
		this.receivedPackets = receivedPackets;
	}

	public void setReceivedPackets(String receivedPackets) {
		if (receivedPackets != null && receivedPackets.isEmpty() == false) {
			this.receivedPackets = Float.parseFloat(receivedPackets);
		}
	}

	public Float getSentPackets() {
		return sentPackets;
	}

	public void setSentPackets(Float sentPackets) {
		this.sentPackets = sentPackets;
	}

	public void setSentPackets(String sentPackets) {
		if (sentPackets != null && sentPackets.isEmpty() == false) {
			this.sentPackets = Float.parseFloat(sentPackets);
		}
	}

	public Float getReceivedBytes() {
		return receivedBytes;
	}

	public void setReceivedBytes(Float receivedBytes) {
		this.receivedBytes = receivedBytes;
	}

	public void setReceivedBytes(String receivedBytes) {
		if (receivedBytes != null && receivedBytes.isEmpty() == false) {
			this.receivedBytes = Float.parseFloat(receivedBytes);
		}
	}

	public Float getSentBytes() {
		return sentBytes;
	}

	public void setSentBytes(Float sentBytes) {
		this.sentBytes = sentBytes;
	}

	public void setSentBytes(String sentBytes) {
		if (sentBytes != null && sentBytes.isEmpty() == false) {
			this.sentBytes = Float.parseFloat(sentBytes);
		}
	}
}