package com.sinoparasoft.task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GangliaMonitor {
	private static Logger logger = LoggerFactory.getLogger(GangliaMonitor.class);
	
	private String[] serverIps;

	public GangliaMonitor(String[] ips) {
		serverIps = ips;
	}

	@SuppressWarnings("unchecked")
	public Map<String, GangliaMetric> getMetrics() throws DocumentException, UnknownHostException, IOException {
		Map<String, GangliaMetric> metrics = new HashMap<String, GangliaMetric>();
			
		for (int index = 0; index < serverIps.length; index++) {
			String xml = this.getGangliaXML(serverIps[index]);
			if (xml == null) {
				continue;
			}
	
			Document doc = DocumentHelper.parseText(xml);
			Element root = doc.getRootElement();
			Element cluster = root.element("CLUSTER");
			Iterator<Element> hostIterator = cluster.elementIterator();
			while (hostIterator.hasNext()) {
				Element host = hostIterator.next();
				GangliaMetric metric = parseMetric(host);
				String hostName = metric.getName();
				if (null != hostName) {	
					metrics.put(hostName, metric);
				}
			}
		}
	
		return metrics;
	}

	private String getGangliaXML(String ip) throws UnknownHostException, IOException {
		String xml = null;
		Socket socket = new Socket(ip, 8649);
		BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		StringBuffer buffer = new StringBuffer();
		String line = null;
		while ((line = br.readLine()) != null) {
			buffer.append(line);
		}
		br.close();
		socket.close();
		xml = buffer.toString();

		return xml;
	}

	private GangliaMetric parseMetric(Element host) {
		GangliaMetric metric = new GangliaMetric();
		// Name
		metric.setName(host.attributeValue("NAME"));
		// IP Address
		metric.setIp(host.attributeValue("IP"));
		// Location
		metric.setLocation(host.attributeValue("LOCATION"));
		// Report Time
		metric.setReportedTime(host.attributeValue("REPORTED"));
		// GMOND Started Time
		metric.setGmondStartedTime(host.attributeValue("GMOND_STARTED"));

		@SuppressWarnings("unchecked")
		Iterator<Element> metricIterator = host.elementIterator();
		while (metricIterator.hasNext()) {
			Element e = metricIterator.next();
			// Operating System Release
			if (e.attributeValue("NAME").equals("os_release")) {
				metric.setOsRelease(e.attributeValue("VAL"));
				continue;
			}
			// Operating system name
			if (e.attributeValue("NAME").equals("os_name")) {
				metric.setOsName(e.attributeValue("VAL"));
				continue;
			}
			// The last time that the system was started
			if (e.attributeValue("NAME").equals("boottime")) {
				metric.setLastBootTime(e.attributeValue("VAL"));
				continue;
			}
			// System architecture
			if (e.attributeValue("NAME").equals("machine_type")) {
				metric.setMachineType(e.attributeValue("VAL"));
				continue;
			}
			// One Minute Load Average
			if (e.attributeValue("NAME").equals("load_one")) {
				metric.setAvgOneMinuteLoad(e.attributeValue("VAL"));
				continue;
			}
			// Five Minute Load Average
			if (e.attributeValue("NAME").equals("load_five")) {
				metric.setAvgFiveMinuteLoad(e.attributeValue("VAL"));
				continue;
			}
			// Fifteen Minute Load Average
			if (e.attributeValue("NAME").equals("load_fifteen")) {
				metric.setAvgFifteenMinuteLoad(e.attributeValue("VAL"));
				continue;
			}
			// Total amount of memory displayed in KBs
			if (e.attributeValue("NAME").equals("mem_total")) {
				metric.setTotalMemory(e.attributeValue("VAL"));
				continue;
			}
			// Amount of available memory
			if (e.attributeValue("NAME").equals("mem_free")) {
				metric.setFreeMemory(e.attributeValue("VAL"));
				continue;
			}
			// Amount of cached memory
			if (e.attributeValue("NAME").equals("mem_cached")) {
				metric.setCachedMemory(e.attributeValue("VAL"));
				continue;
			}
			// Amount of buffered memory
			if (e.attributeValue("NAME").equals("mem_buffers")) {
				metric.setBufferedMemory(e.attributeValue("VAL"));
				continue;
			}
			// Amount of shared memory
			if (e.attributeValue("NAME").equals("mem_shared")) {
				metric.setSharedMemory(e.attributeValue("VAL"));
				continue;
			}
			// Total amount of swap space displayed in KBs
			if (e.attributeValue("NAME").equals("swap_total")) {
				metric.setTotalSwap(e.attributeValue("VAL"));
				continue;
			}
			// Amount of available swap memory
			if (e.attributeValue("NAME").equals("swap_free")) {
				metric.setFreeSwap(e.attributeValue("VAL"));
				continue;
			}
			// Total number of processes
			if (e.attributeValue("NAME").equals("proc_total")) {
				metric.setTotalProcesses(e.attributeValue("VAL"));
				continue;
			}
			// Total number of running processes
			if (e.attributeValue("NAME").equals("proc_run")) {
				metric.setRunningProcesses(e.attributeValue("VAL"));
				continue;
			}
			// Gexec Status
			if (e.attributeValue("NAME").equals("gexec")) {
				metric.setGexecAvailable(e.attributeValue("VAL"));
				continue;
			}
			// Total available disk space
			if (e.attributeValue("NAME").equals("disk_total")) {
				metric.setTotalDisk(e.attributeValue("VAL"));
				continue;
			}
			// Total free disk space
			if (e.attributeValue("NAME").equals("disk_free")) {
				metric.setFreeDisk(e.attributeValue("VAL"));
				continue;
			}
			// Maximum percent used for all partitions
			if (e.attributeValue("NAME").equals("part_max_used")) {
				metric.setMaxUsedDiskSpacePercent(e.attributeValue("VAL"));
				continue;
			}
			// Used disk space
			if (e.attributeValue("NAME").endsWith("-disk_used")) {
				String key = null;
				Float value = null;
				try {
					value = Float.parseFloat(e.attributeValue("VAL"));
				} catch (NumberFormatException e1) {
					logger.warn("解析节点负载数据发生错误", e1);
					value = null;
				}
				@SuppressWarnings("unchecked")
				Iterator<Element> extraIterator = e.element("EXTRA_DATA").elementIterator();
				while (extraIterator.hasNext()) {
					Element extra = extraIterator.next();
					if (extra.attributeValue("NAME").equals("mount")) {
						key = extra.attributeValue("VAL");
						break;
					}
				}
				metric.addUsedDiskMap(key, value);
				continue;
			}
			// Available disk space
			if (e.attributeValue("NAME").endsWith("-disk_total")) {
				String key = null;
				Float value;
				try {
					value = Float.parseFloat(e.attributeValue("VAL"));
				} catch (NumberFormatException e1) {
					logger.warn("解析节点负载数据发生错误", e1);
					value = null;
				}
				@SuppressWarnings("unchecked")
				Iterator<Element> extraIterator = e.element("EXTRA_DATA").elementIterator();
				while (extraIterator.hasNext()) {
					Element extra = extraIterator.next();
					if (extra.attributeValue("NAME").equals("mount")) {
						key = extra.attributeValue("VAL");
						break;
					}
				}
				metric.addTotalDiskMap(key, value);
				continue;
			}
			// Percentage of time that the CPU or CPUs were idle
			if (e.attributeValue("NAME").equals("cpu_idle")) {
				metric.setCpuIdlePercent(e.attributeValue("VAL"));
				continue;
			}
			// cpuNicePercent
			if (e.attributeValue("NAME").equals("cpu_nice")) {
				metric.setCpuNicePercent(e.attributeValue("VAL"));
				continue;
			}
			// cpuUserPercent
			if (e.attributeValue("NAME").equals("cpu_user")) {
				metric.setCpuUserPercent(e.attributeValue("VAL"));
				continue;
			}
			// Percent of time since boot idle CPU
			if (e.attributeValue("NAME").equals("cpu_aidle")) {
				metric.setCpuAidlePercent(e.attributeValue("VAL"));
				continue;
			}
			// cpuSystemPercent
			if (e.attributeValue("NAME").equals("cpu_system")) {
				metric.setCpuSystemPercent(e.attributeValue("VAL"));
				continue;
			}
			// cpuWioPercent
			if (e.attributeValue("NAME").equals("cpu_wio")) {
				metric.setCpuWioPercent(e.attributeValue("VAL"));
				continue;
			}
			// Total number of CPUs
			if (e.attributeValue("NAME").equals("cpu_num")) {
				metric.setCpuNum(e.attributeValue("VAL"));
				continue;
			}
			// CPU Speed in terms of MHz
			if (e.attributeValue("NAME").equals("cpu_speed")) {
				metric.setCpuSpeed(e.attributeValue("VAL"));
				continue;
			}
			// Total number of established TCP connections
			if (e.attributeValue("NAME").equals("tcp_established")) {
				metric.setEstablishedTcpConnections(e.attributeValue("VAL"));
				continue;
			}
			// Total number of listening TCP connections
			if (e.attributeValue("NAME").equals("tcp_listen")) {
				metric.setListeningTcpConnections(e.attributeValue("VAL"));
				continue;
			}
			// Total number of syn_wait TCP connections
			if (e.attributeValue("NAME").equals("tcp_synwait")) {
				metric.setSyncWaitTcpConnections(e.attributeValue("VAL"));
				continue;
			}
			// Total number of syn_sent TCP connections
			if (e.attributeValue("NAME").equals("tcp_synsent")) {
				metric.setSyncSentTcpConnections(e.attributeValue("VAL"));
				continue;
			}
			// Total number of syn_recv TCP connections
			if (e.attributeValue("NAME").equals("tcp_synrecv")) {
				metric.setSyncRecvTcpConnections(e.attributeValue("VAL"));
				continue;
			}
			// Total number of last_ack TCP connections
			if (e.attributeValue("NAME").equals("tcp_lastack")) {
				metric.setLastAckTcpConnections(e.attributeValue("VAL"));
				continue;
			}
			// Total number of fin_wait1 TCP connections
			if (e.attributeValue("NAME").equals("tcp_finwait1")) {
				metric.setFinWait1TcpConnections(e.attributeValue("VAL"));
				continue;
			}
			// Total number of time_wait TCP connections
			if (e.attributeValue("NAME").equals("tcp_timewait")) {
				metric.setTimeWaitTcpConnections(e.attributeValue("VAL"));
				continue;
			}
			// Total number of fin_wait2 TCP connections
			if (e.attributeValue("NAME").equals("tcp_finwait2")) {
				metric.setFinWait2TcpConnections(e.attributeValue("VAL"));
				continue;
			}
			// Total number of close_wait TCP connections
			if (e.attributeValue("NAME").equals("tcp_closewait")) {
				metric.setCloseWaitTcpConnections(e.attributeValue("VAL"));
				continue;
			}
			// Total number of closing TCP connections
			if (e.attributeValue("NAME").equals("tcp_closing")) {
				metric.setClosingTcpConnections(e.attributeValue("VAL"));
				continue;
			}
			// Total number of closed TCP connections
			if (e.attributeValue("NAME").equals("tcp_closed")) {
				metric.setClosedTcpConnections(e.attributeValue("VAL"));
				continue;
			}
			// Total number of unknown TCP connections
			if (e.attributeValue("NAME").equals("tcp_unknown")) {
				metric.setUnknownTcpConnections(e.attributeValue("VAL"));
				continue;
			}
			// Packets in per second
			if (e.attributeValue("NAME").equals("pkts_in")) {
				metric.setReceivedPackets(e.attributeValue("VAL"));
				continue;
			}
			// Packets out per second
			if (e.attributeValue("NAME").equals("pkts_out")) {
				metric.setSentPackets(e.attributeValue("VAL"));
				continue;
			}
			// Number of bytes in per second
			if (e.attributeValue("NAME").equals("bytes_in")) {
				metric.setReceivedBytes(e.attributeValue("VAL"));
				continue;
			}
			// Number of bytes out per second
			if (e.attributeValue("NAME").equals("bytes_out")) {
				metric.setSentBytes(e.attributeValue("VAL"));
			}
		}

		return metric;
	}

}
