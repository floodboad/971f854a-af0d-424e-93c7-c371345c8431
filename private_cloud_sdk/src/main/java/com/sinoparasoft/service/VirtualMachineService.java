package com.sinoparasoft.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.sinoparasoft.common.ActionResult;
import com.sinoparasoft.common.AlarmParameter;
import com.sinoparasoft.common.BatchActionResult;
import com.sinoparasoft.openstack.type.telemetry.ServerSamples;
import com.sinoparasoft.type.MigrationPolicy;

@Service
public interface VirtualMachineService {
	/**
	 * get virtual machine by id.
	 * 
	 * @param id
	 *            - virtual machine id
	 * @return virtual machine value, or null if virtual machine is not found
	 * @author xiangqian
	 */
	public com.sinoparasoft.type.VirtualMachine getVirtualMachineById(String id);

	/**
	 * create virtual machine.
	 * 
	 * @param creator
	 *            - creator
	 * @param hostName
	 *            - virtual machine name
	 * @param description
	 *            - virtual machine description
	 * @param cpu
	 *            - cpu number
	 * @param memory
	 *            - memory size in GB
	 * @param disk
	 *            - disk size in GB
	 * @param imageId
	 *            - image id
	 * @param domainId
	 *            - virtual machine domain id
	 * @param groupId
	 *            - virtual machine group id
	 * @param applicationIds
	 *            - application tag id comma separated list
	 * @param manager
	 *            - manager
	 * @return action result
	 * @author xiangqian
	 */
	public ActionResult createVirtualMachine(String creator, String hostName, String description, int cpu, int memory,
			int disk, String imageId, String domainId, String groupId, String applicationIds, String manager);

	/**
	 * start virtual machines.
	 * 
	 * @param username
	 *            - name of user who perform the action
	 * @param vmIds
	 *            - virtual machine id list
	 * @return batch action result
	 * @author xiangqian
	 */
	public BatchActionResult startVirtualMachines(String username, String vmIds);

	/**
	 * reboot virtual machine.
	 * 
	 * @param username
	 *            - name of user who perform the action
	 * @param vmIds
	 *            - virtual machine id list
	 * @return batch action result
	 * @author xiangqian
	 */
	public BatchActionResult rebootVirtualMachines(String username, String vmIds);

	/**
	 * shutdown virtual machine.
	 * 
	 * @param username
	 *            - name of user who perform the action
	 * @param vmIds
	 *            - virtual machine id list
	 * @return batch action result
	 * @author xiangqian
	 */
	public BatchActionResult shutdownVirtualMachines(String username, String vmIds);

	/**
	 * delete virtual machine.
	 * 
	 * @param username
	 *            - name of user who perform the action
	 * @param vmIds
	 *            - virtual machine id list
	 * @return batch action result
	 * @author xiangqian
	 */
	public BatchActionResult deleteVirtualMachines(String username, String vmIds);

	/**
	 * get virtual machine load, including cpu, memory, disk and network.
	 * 
	 * @param vmId
	 *            - virtual machine id
	 * @return virtual machine load map, or null if machine not found
	 * @author xiangqian
	 */
	public Map<String, ServerSamples> getMetric(String vmId);

	/**
	 * get virtual machine vnc console.
	 * 
	 * @param vmId
	 *            - virtual machine id
	 * @return console url, or null if console is not found
	 * @author xiangqian
	 */
	public String getVncConsoleUrl(String vmId);

	/**
	 * rename virtual machine.
	 * 
	 * @param username
	 *            - name of user who perform the action
	 * @param vmId
	 *            - virtual machine id
	 * @param newName
	 *            - new name
	 * @return action result
	 * @author xiangqian
	 */
	public ActionResult renameVirtualMachine(String username, String vmId, String newName);

	/**
	 * set machine manager.
	 * 
	 * @param username
	 *            - name of user who perform the action
	 * @param vmId
	 *            - virtual machine id
	 * @param manager
	 *            - machine manager
	 * @return action result
	 * @author xiangqian
	 */
	public ActionResult setVirtualMachineManager(String username, String vmId, String manager);

//	/**
//	 * associate ip to virtual machine.
//	 * 
//	 * @param username
//	 *            - name of user who perform the action
//	 * @param vmId
//	 *            - virtual machine id
//	 * @param ip
//	 *            - ip address
//	 * @return action result
//	 * @author xiangqian
//	 */
//	public ActionResult allocateFloatingIp(String username, String vmId, String ip);
//
//	/**
//	 * remove ip form virtual machine and deallocate it back to cloud.
//	 * 
//	 * @param username
//	 *            - name of user who perform the action
//	 * @param vmId
//	 *            - virtual machine id
//	 * @return action result
//	 * @author xiangqian
//	 */
//	public ActionResult deallocateFloatingIp(String username, String vmId);

	/**
	 * set application tag.
	 * 
	 * @param username
	 *            - name of user who perform the action
	 * @param vmId
	 *            - virtual machine id
	 * @param applicationTagIds
	 *            - application tag id list
	 * @return action result
	 * @author xiangqian
	 */
	public ActionResult setApplicationTags(String username, String vmId, String applicationTagIds);

	/**
	 * change alarm setting.
	 * 
	 * @param username
	 *            - name of user who perform the action
	 * @param vmId
	 *            - virtual machine id
	 * @param alarmParameters
	 *            - new alarm parameters
	 * @return batch action result
	 * @author xiangqian
	 */
	public BatchActionResult setLoadMonitorSettings(String username, String vmId, List<AlarmParameter> alarmParameters);

	/**
	 * attach disk to a virtual machine.
	 * 
	 * @param username
	 *            - name of user who perform the action
	 * @param vmId
	 *            - virtual machine id
	 * @param diskId
	 *            - disk id
	 * @return action result
	 * @author xiangqian
	 */
	public ActionResult attachDisk(String username, String vmId, String diskId);

	/**
	 * detach disk from a virtual machine.
	 * 
	 * @param username
	 *            - name of user who perform the action
	 * @param vmId
	 *            - virtual machine id
	 * @param diskId
	 *            - disk id
	 * @return action result
	 * @author xiangqian
	 */
	public ActionResult detachDisk(String username, String vmId, String diskId);

	/**
	 * create snapshot of virtual machine.
	 * 
	 * @param username
	 *            - name of user who perform the action
	 * @param vmId
	 *            - virtual machine id
	 * @param snapshotName
	 *            - snapshot name
	 * @param snapshotDescription
	 *            - snapshot description
	 * @return action result
	 * @author xiangqian
	 */
	public ActionResult createSnapshot(String username, String vmId, String snapshotName, String snapshotDescription);

	/**
	 * restore snapshot based on specified virtual machine, simply create a new virtual machine based on the old one's
	 * configuration.
	 * 
	 * @param username
	 *            - name of user who perform the action
	 * @param vmId
	 *            - virtual machine id
	 * @param snapshotId
	 *            - snapshot id
	 * @param virtualMachineName
	 *            - new virtual machine name
	 * @param virtualMachineDescription
	 *            - new virtual machine description
	 * @return action result
	 * @author xiangqian
	 */
	public ActionResult restoreSnapshot(String username, String vmId, String snapshotId, String virtualMachineName,
			String virtualMachineDescription);

	/**
	 * live migration virtual machines.
	 * 
	 * @param username
	 *            - name of user who perform the action
	 * @param migratePolicies
	 *            - migrate policies contains virtual machine and destination hypervior pair
	 * @return batch action result
	 * @author xiangqian
	 */
	public BatchActionResult liveMigrateVirtualMachines(String username, List<MigrationPolicy> migratePolicies);

	/**
	 * get virtual machines running on the hypervisor.
	 * 
	 * @param hypervisorName
	 *            - hypervisor name
	 * @return virtual machine list
	 */
	public List<com.sinoparasoft.type.VirtualMachine> getVirtualMachinesByHypervisorName(String hypervisorName);
}
