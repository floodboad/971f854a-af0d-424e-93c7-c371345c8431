package com.sinoparasoft.model;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OptimisticLockException;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import javax.validation.constraints.NotNull;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException;
import org.springframework.orm.jpa.JpaOptimisticLockingFailureException;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

import com.sinoparasoft.enumerator.DiskStatusEnum;

@RooJavaBean
@RooToString
@RooJpaActiveRecord
public class Disk {
	private static Logger logger = LoggerFactory.getLogger(Disk.class);

	/**
	 * custom speficied id value, hibernate will not generate this one
	 */
	@Id
	@Column(name = "diskId")
	private String diskId;

	/**
     */
	@NotNull
	private String diskName;

	/**
     */
	private Integer capacity;

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
	@Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat(style = "M-")
	private Date createTime;

	/**
     */
	@Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat(style = "M-")
	private Date attachTime;

	/**
     */
	@Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat(style = "M-")
	private Date modifyTime;

	/**
     */
	@Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat(style = "M-")
	private Date validTime;

	/**
     */
	@Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat(style = "M-")
	private Date deleteTime;

	/**
     */
	@Enumerated(EnumType.STRING)
	private DiskStatusEnum status;

	/**
     */
	private String attachPoint;

	/**
     */
	@ManyToOne
	@JoinColumn(name = "hostId")
	private VirtualMachine virtualMachine;

	/**
	 * get disk by id, the disk is not been deleted.
	 *
	 * @param diskId
	 *            - disk id
	 * @return disk with the specified id, or null if the given parameter is bad or the number of qualified disk not
	 *         equals to 1.
	 * @author xiangqian
	 */
	public static Disk getDisk(String diskId) {
		if (diskId == null || diskId.length() == 0) {
			return null;
		}
		String jpaString = "SELECT o FROM Disk o WHERE o.diskId = '" + diskId + "' AND o.status <> '"
				+ DiskStatusEnum.DELETED.name() + "'";
		TypedQuery<Disk> query = entityManager().createQuery(jpaString, Disk.class);
		List<Disk> disks = query.getResultList();
		if (disks.size() == 1) {
			return disks.get(0);
		} else {
			return null;
		}
	}

	/**
	 * get disk list in create time descending order. the disks are about to expire or already expired, and are not been
	 * deleted.
	 *
	 * @return disk list
	 * @author xiangqian
	 */
	public static List<Disk> getExpringDisks() {
		DateTime limitTime = new DateTime().plusDays(7);
		DateTimeFormatter formatter = org.joda.time.format.DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
		String jpaString = "SELECT o FROM Disk o WHERE o.status <> '" + DiskStatusEnum.DELETED.name()
				+ "' AND o.validTime < '" + limitTime.toString(formatter) + "' ORDER BY o.createTime DESC";
		TypedQuery<Disk> query = entityManager().createQuery(jpaString, Disk.class);
		return query.getResultList();
	}

	/**
	 * get disk list in create time descending order. the disks are managed by the given manager, and are available.
	 * 
	 * @param manager
	 *            - manager
	 * @return disk list
	 * @author xiangqian
	 */
	public static List<Disk> getAvailableDisksByManager(String manager) {
		return entityManager().createQuery(
				"SELECT o FROM Disk o WHERE o.manager = '" + manager + "' AND o.status = '"
						+ DiskStatusEnum.AVAILABLE.name() + "' ORDER BY o.createTime DESC", Disk.class).getResultList();
	}

	/**
	 * get disk count, the disks are managed by the given manager, and are not been deleted.
	 *
	 * @param manager
	 *            - manager
	 * @return disk count
	 * @author xiangqian
	 */
	public static long countActiveDisksByManager(String manager) {
		return entityManager().createQuery(
				"SELECT COUNT(o) FROM Disk o WHERE o.manager = '" + manager + "' AND o.status <> '"
						+ DiskStatusEnum.DELETED.name() + "'", Long.class).getSingleResult();
	}

	/**
	 * get disk list in create time descending order. the disks are managed by the given manager, and are not been
	 * deleted.
	 *
	 * @param firstResult
	 *            - start position of the first result
	 * @param maxResults
	 *            - maximum number of results
	 * @param manager
	 *            - manager
	 * @return disk list
	 * @author xiangqian
	 */
	public static List<Disk> getDiskEntriesByManager(int firstResult, int maxResults, String manager) {
		return entityManager()
				.createQuery(
						"SELECT o FROM Disk o WHERE o.manager = '" + manager + "' AND o.status <> '"
								+ DiskStatusEnum.DELETED.name() + "' ORDER BY o.createTime DESC", Disk.class)
				.setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
	}

	/**
	 * get disk count, the disks are not been deleted.
	 *
	 * @return disk count
	 * @author xiangqian
	 */
	public static long countActiveDisks() {
		return entityManager().createQuery(
				"SELECT COUNT(o) FROM Disk o WHERE o.status <> '" + DiskStatusEnum.DELETED.name() + "'", Long.class)
				.getSingleResult();
	}

	/**
	 * get disk list in create time descending order. the disks are not been deleted.
	 *
	 * @param firstResult
	 *            - start position of the first result
	 * @param maxResults
	 *            - maximum number of results
	 * @return disk list
	 * @author xiangqian
	 */
	public static List<Disk> getDiskEntries(int firstResult, int maxResults) {
		return entityManager()
				.createQuery(
						"SELECT o FROM Disk o WHERE o.status <> '" + DiskStatusEnum.DELETED.name()
								+ "' ORDER BY o.createTime DESC", Disk.class).setFirstResult(firstResult)
				.setMaxResults(maxResults).getResultList();
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
				Disk updatedDisk = findDisk(this.getDiskId());
				this.setVersion(updatedDisk.getVersion());
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
