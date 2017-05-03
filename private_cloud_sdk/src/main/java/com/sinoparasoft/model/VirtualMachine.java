package com.sinoparasoft.model;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OptimisticLockException;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException;
import org.springframework.orm.jpa.JpaOptimisticLockingFailureException;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

import com.sinosoft.enumerator.VirtualMachineStatusEnum;
import com.sinosoft.enumerator.VirtualMachineTaskStatusEnum;

@RooJavaBean
@RooToString
@RooJpaActiveRecord
public class VirtualMachine {
	private static Logger logger = LoggerFactory.getLogger(VirtualMachine.class);

	/**
	 */
	// custom value for id, not want hibernate to generate value
	@Id
	@Column(name = "hostId")
	private String hostId;

	/**
	 */
	private String hostName;

	/**
	 */
	private Integer cpu;

	/**
	 */
	private Integer memory;

	/**
	 */
	private Integer disk;

	/**
	 */
	private String description;

	/**
	 */
	private String creator;

	/**
	 */
	private String manager;

	/**
	 */
	@Enumerated(EnumType.STRING)
	private VirtualMachineStatusEnum status;

	/**
	 */
	@Enumerated(EnumType.STRING)
	private VirtualMachineTaskStatusEnum taskStatus;

	/**
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat(style = "M-")
	private Date createTime;

	/**
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat(style = "M-")
	private Date modifyTime;

	/**
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat(style = "M-")
	private Date deleteTime;

	/**
	 */
	private String domainId;

	/**
	 */
	private String groupId;

	/**
	 */
	private String imageName;

	/**
	 */
	private String privateIp;

	/**
	 */
	private String floatingIp;

	/**
	 */
	private String hypervisorName;

	/**
	 * fetch = FetchType.EAGER: allow to get disks in background process, such as @Async. otherwise
	 * "org.hibernate.LazyInitializationException: failed to lazily initialize a collection of role:
	 * com.sinosoft.model.VirtualMachine.disks" will be thrown
	 */
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "virtualMachine")
	private Set<Disk> disks = new HashSet<Disk>();

	/**
	 */
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "virtualMachine")
	private Set<Snapshot> snapshots = new HashSet<Snapshot>();

	/**
	 * 
	 */
	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinTable(name = "virtual_machine_application_tag", joinColumns = @JoinColumn(name = "host_id"), inverseJoinColumns = @JoinColumn(name = "app_id", referencedColumnName = "id"))
	private Set<ApplicationTag> applicationTags = new HashSet<ApplicationTag>();

	/**
	 * get virtual machine by id, the virtual machine is not been deleted.
	 * 
	 * @param hostId
	 *            - virtual machine id
	 * @return virtual machine with the specified id, or null if the given parameter is bad or the number of qualified
	 *         virtual machine not equals to 1.
	 * @author xiangqian
	 */
	public static VirtualMachine getVirtualMachine(String hostId) {
		if (hostId == null || hostId.length() == 0) {
			return null;
		}

		List<VirtualMachine> l = entityManager().createQuery(
				"SELECT o FROM VirtualMachine o WHERE o.hostId = '" + hostId + "' AND o.status <> '"
						+ VirtualMachineStatusEnum.DELETED.name() + "'", VirtualMachine.class).getResultList();
		if (l.size() == 1) {
			return l.get(0);
		}
		return null;
	}

	/**
	 * get virtual machine count, the virtual machines are not been deleted.
	 * 
	 * @return virtual machine count
	 * @author xiangqian
	 */
	public static long countVirtualMachines() {
		return entityManager().createQuery(
				"SELECT COUNT(o) FROM VirtualMachine o WHERE o.status <> '" + VirtualMachineStatusEnum.DELETED.name()
						+ "'", Long.class).getSingleResult();
	}

	// public static long countActiveVirtualMachines() {
	// return entityManager().createQuery("SELECT COUNT(o) FROM VirtualMachine o WHERE o.status <> '"
	// + VirtualMachineStatusEnum.DELETED.name() + "'", Long.class).getSingleResult();
	// }

	/**
	 * get virtual machine list in create time descending order. the virtual machines are not been deleted.
	 * 
	 * @return virtual machine list
	 * @author xiangqian
	 */
	public static List<VirtualMachine> getVirtualMachines() {
		return entityManager().createQuery(
				"SELECT o FROM VirtualMachine o WHERE o.status <> '" + VirtualMachineStatusEnum.DELETED.name()
						+ "' ORDER BY o.createTime DESC", VirtualMachine.class).getResultList();
	}

	/**
	 * get virtual machine list in create time descending order. the virtual machines are not been deleted.
	 *
	 * @param firstResult
	 *            - start position of the first result
	 * @param maxResults
	 *            - maximum number of results
	 * @return virtual machine list
	 * @author xiangqian
	 */
	public static List<VirtualMachine> getVirtualMachineEntries(int firstResult, int maxResults) {
		return entityManager()
				.createQuery(
						"SELECT o FROM VirtualMachine o WHERE o.status <> '" + VirtualMachineStatusEnum.DELETED.name()
								+ "' ORDER BY o.createTime DESC", VirtualMachine.class).setFirstResult(firstResult)
				.setMaxResults(maxResults).getResultList();
	}

	/**
	 * get virtual machine count, the virtual machines are managed by the given manager, and are not been deleted.
	 *
	 * @param manager
	 *            - manager
	 * @return virtual machine count
	 * @author xiangqian
	 */
	public static long countVirtualMachinesByManager(String manager) {
		return entityManager().createQuery(
				"SELECT COUNT(o) FROM VirtualMachine o WHERE o.manager = '" + manager + "' AND o.status <> '"
						+ VirtualMachineStatusEnum.DELETED.name() + "'", Long.class).getSingleResult();
	}

	/**
	 * get virtual machine list in create time descending order. the virtual machines managed by the given manager, and
	 * are not been deleted.
	 * 
	 * @param manager
	 *            - manager name
	 * @return virtual machine list
	 * @author xiangqian
	 */
	public static List<VirtualMachine> getVirtualMachinesByManager(String manager) {
		return entityManager().createQuery(
				"SELECT o FROM VirtualMachine o WHERE o.manager = '" + manager + "' AND o.status <> '"
						+ VirtualMachineStatusEnum.DELETED.name() + "' ORDER BY o.createTime DESC",
				VirtualMachine.class).getResultList();
	}

	/**
	 * get virtual machine list in create time descending order. the virtual machines are managed by the given manager,
	 * and are not been deleted.
	 *
	 * @param firstResult
	 *            - start position of the first result
	 * @param maxResults
	 *            - maximum number of results
	 * @param manager
	 *            - manager
	 * @return virtual machine list
	 * @author xiangqian
	 */
	public static List<VirtualMachine> getVirtualMachineEntriesByManager(int firstResult, int maxResults, String manager) {
		return entityManager()
				.createQuery(
						"SELECT o FROM VirtualMachine o WHERE o.manager = '" + manager + "' AND o.status <> '"
								+ VirtualMachineStatusEnum.DELETED.name() + "' ORDER BY o.createTime DESC",
						VirtualMachine.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
	}

	/**
	 * get virtual machines by group in create time descending order. the virtual machines are not been deleted.
	 * 
	 * @param groupId
	 *            - group id
	 * @return virtual machine list
	 * @author chaoyue, xiangqian
	 */
	public static List<VirtualMachine> getVirtualMachinesByGroupId(String groupId) {
		return entityManager().createQuery(
				"SELECT o FROM VirtualMachine o WHERE o.groupId = '" + groupId + "' AND o.status <> '"
						+ VirtualMachineStatusEnum.DELETED.name() + "' ORDER BY o.createTime DESC",
				VirtualMachine.class).getResultList();
	}

	/**
	 * get used resource of specified group.
	 * 
	 * @param groupId
	 *            - group id
	 * @return used resource of group
	 * @author xiangqian
	 */
	public static Map<String, Long> getGroupUsedResource(String groupId) {
		Map<String, Long> map = new HashMap<String, Long>();

		Object[] objs = (Object[]) entityManager().createQuery(
				"SELECT SUM(v.cpu), SUM(v.memory), SUM(v.disk), COUNT(v.hostId) FROM VirtualMachine v WHERE v.groupId = '"
						+ groupId + "' AND v.status <> '" + VirtualMachineStatusEnum.DELETED.name() + "'")
				.getSingleResult();

		Long[] usedResource = Arrays.copyOf(objs, objs.length, Long[].class);
		long usedCpu = (null != usedResource[0]) ? usedResource[0] : 0;
		long usedMemory = (null != usedResource[1]) ? usedResource[1] : 0;
		long usedDisk = (null != usedResource[2]) ? usedResource[2] : 0;
		map.put("cpu", usedCpu);
		map.put("memory", usedMemory);
		map.put("disk", usedDisk);

		return map;
	}

	/**
	 * get used resource of a specified domain.
	 * 
	 * @param domainId
	 *            - domain id
	 * @return used resource of domain
	 * @author xiangqian
	 */
	public static Map<String, Long> getDomainUsedResource(String domainId) {
		Map<String, Long> map = new HashMap<String, Long>();

		Object[] objs = (Object[]) entityManager().createQuery(
				"SELECT SUM(v.cpu), SUM(v.memory), SUM(v.disk), COUNT(v.hostId) FROM VirtualMachine v WHERE v.domainId = '"
						+ domainId + "' AND v.status <> '" + VirtualMachineStatusEnum.DELETED.name() + "'")
				.getSingleResult();

		Long[] usedResource = Arrays.copyOf(objs, objs.length, Long[].class);
		long usedCpu = (null != usedResource[0]) ? usedResource[0] : 0;
		long usedMemory = (null != usedResource[1]) ? usedResource[1] : 0;
		long usedDisk = (null != usedResource[2]) ? usedResource[2] : 0;
		map.put("cpu", usedCpu);
		map.put("memory", usedMemory);
		map.put("disk", usedDisk);

		return map;
	}

	/**
	 * get virtual machines running on the specified hypervisor, the virtual machines are not been deleted.
	 * 
	 * @param hypervisorName
	 *            - hypervisor name
	 * @return virtual machine list
	 * @author xiangqian
	 */
	public static List<VirtualMachine> getVirtualMachinesByHypervisorName(String hypervisorName) {
		return entityManager().createQuery(
				"SELECT o FROM VirtualMachine o WHERE o.hypervisorName = '" + hypervisorName + "' AND o.status <> '"
						+ VirtualMachineStatusEnum.DELETED.name() + "'", VirtualMachine.class).getResultList();
	}

	/**
	 * merge data, last-commit-wins version.
	 * 
	 * @throws InterruptedException
	 * @author xiangqian
	 */
	public boolean lastCommitWinsMerge() {
		try {
			this.merge();
			// logger.debug("首次更新数据成功");
			return true;
		} catch (OptimisticLockException e) {
			return retryMerge();
		} catch (HibernateOptimisticLockingFailureException e) {
			return retryMerge();
		} catch (JpaOptimisticLockingFailureException e) {
			return retryMerge();
		}
	}

	private boolean retryMerge() {
		int sleepMilliseconds = 500, sleepIndex = 1, sleepCount = 30;

		for (; sleepIndex < sleepCount; sleepIndex++) {
			try {
				/*
				 * set new version
				 */
				VirtualMachine updatedMachine = findVirtualMachine(this.getHostId());
				this.setVersion(updatedMachine.getVersion());
				this.merge();

				logger.debug("重试更新数据成功，重试次数：" + sleepIndex);

				break;
			} catch (OptimisticLockException e) {
				try {
					Thread.sleep(sleepMilliseconds);
				} catch (InterruptedException e1) {
					logger.error("更新数据被中断", e);
					return false;
				}
			} catch (HibernateOptimisticLockingFailureException e) {
				try {
					Thread.sleep(sleepMilliseconds);
				} catch (InterruptedException e1) {
					logger.error("更新数据被中断", e);
					return false;
				}
			} catch (JpaOptimisticLockingFailureException e) {
				try {
					Thread.sleep(sleepMilliseconds);
				} catch (InterruptedException e1) {
					logger.error("更新数据被中断", e);
					return false;
				}
			}
		}

		if (sleepIndex == sleepCount) {
			logger.error("重试更新数据出错，达到最大重试次数");
			return false;
		}

		return true;
	}
}
