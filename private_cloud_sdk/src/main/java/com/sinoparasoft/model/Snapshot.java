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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException;
import org.springframework.orm.jpa.JpaOptimisticLockingFailureException;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

import com.sinoparasoft.enumerator.SnapshotStatusEnum;

@RooJavaBean
@RooToString
@RooJpaActiveRecord
public class Snapshot {
	private static Logger logger = LoggerFactory.getLogger(Snapshot.class);

	/**
	 */
	@Id
	@Column(name = "snapshotId")
	private String snapshotId;

	/**
	 */
	private String snapshotName;

	/**
	 */
	private String description;

	/**
	 */
	private String creator;

	/**
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat(style = "M-")
	private Date createTime;

	/**
	 */
	@Enumerated(EnumType.STRING)
	private SnapshotStatusEnum status;

	/**
	 */
	private long size;

	/**
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat(style = "M-")
	private Date deleteTime;

	/**
	 */
	@ManyToOne
	@JoinColumn(name = "hostId")
	private VirtualMachine virtualMachine;

	/**
	 * get snapshot by id, the snapshot is not been deleted.
	 * 
	 * @param snapshotId
	 *            - snapshot id
	 * @return snapshot with the specified id, or null if the given parameter is bad or the number of qualified snapshot
	 *         not equals to 1
	 * @author xiangqian
	 */
	public static Snapshot getSnapshot(String snapshotId) {
		if (snapshotId == null || snapshotId.length() == 0) {
			return null;
		}

		String jpaString = "SELECT o FROM Snapshot o WHERE o.snapshotId = '" + snapshotId + "' AND o.status <> '"
				+ SnapshotStatusEnum.DELETED.name() + "'";
		TypedQuery<Snapshot> query = entityManager().createQuery(jpaString, Snapshot.class);
		List<Snapshot> snaoshots = query.getResultList();
		if (snaoshots.size() == 1) {
			return snaoshots.get(0);
		} else {
			return null;
		}
	}

	/**
	 * get snapshot count, the snapshots are not been deleted.
	 * 
	 * @return snapshot count
	 * @author xiangqian
	 */
	public static long countSnapshots() {
		return entityManager().createQuery(
				"SELECT COUNT(o) FROM Snapshot o WHERE o.status <> '" + SnapshotStatusEnum.DELETED.name() + "'",
				Long.class).getSingleResult();
	}

	/**
	 * get snapshot list, the snapshots are not been deleted.
	 * 
	 * @return snapshot list
	 * @author xiangqian
	 */
	public static List<Snapshot> getSnapshots() {
		return entityManager().createQuery(
				"SELECT o FROM Snapshot o WHERE o.status <> '" + SnapshotStatusEnum.DELETED.name()
						+ "' ORDER BY o.createTime DESC", Snapshot.class).getResultList();
	}

	/**
	 * get snapshot count, the snapshots are created by the given manager, and are not been deleted.
	 * 
	 * @param manager
	 *            - manager
	 * @return snapshot count
	 * @author xiangqian
	 */
	public static long countSnapshotsByManager(String manager) {
		return entityManager().createQuery(
				"SELECT COUNT(o) FROM Snapshot o WHERE o.creator = '" + manager + "' AND o.status <> '"
						+ SnapshotStatusEnum.DELETED.name() + "'", Long.class).getSingleResult();
	}

	/**
	 * get snapshot list, the snapshots are created by the given manager, and are not been deleted.
	 * 
	 * @param manager
	 * @return
	 * @author xiangqian
	 */
	public static List<Snapshot> getSnapshotsByManager(String manager) {
		TypedQuery<Snapshot> query = entityManager().createQuery(
				"SELECT o FROM Snapshot o WHERE o.creator = '" + manager + "' AND o.status <> '"
						+ SnapshotStatusEnum.DELETED.name() + "' ORDER BY o.createTime DESC", Snapshot.class);
		return query.getResultList();
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
				Snapshot updatedSnapshot = Snapshot.findSnapshot(this.getSnapshotId());
				this.setVersion(updatedSnapshot.getVersion());
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
