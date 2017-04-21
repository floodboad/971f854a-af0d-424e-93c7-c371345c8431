package com.sinosoft.model;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

import java.util.Date;
import java.util.List;

import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

import org.springframework.format.annotation.DateTimeFormat;

@RooJavaBean
@RooToString
@RooJpaActiveRecord
public class PhysicalMachineLoad {

	/**
     */
	private String hostName;

	/**
     */
	private Float cpuLoad;

	/**
     */
	private Float cpuIdle;

	/**
     */
	private Float cpuUser;

	/**
     */
	private Float cpuSystem;

	/**
     */
	private Float freeMemory;

	/**
     */
	private Float freeDisk;

	/**
     */
	private Float bytesIn;

	/**
     */
	private Float bytesOut;

	/**
     */
	@Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat(style = "M-")
	private Date reportTime;

	/**
	 * get the latest load of a given machine, the load must be reported within 5 minutes.
	 * 
	 * @param hostName
	 *            - physical machine name
	 * @return latest load, or null if the number of qualified load not equals to 1.
	 * @author xiangqian
	 */
	public static PhysicalMachineLoad getLatestPhyscalMachineLoad(String hostName) {
		DateTime limitTime = new DateTime().minusMinutes(5);
		DateTimeFormatter formatter = org.joda.time.format.DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

		String jpaQuery = "SELECT l FROM PhysicalMachineLoad l WHERE l.hostName = '" + hostName
				+ "' AND l.reportTime >= '" + limitTime.toString(formatter) + "' ORDER BY l.reportTime DESC";
		List<PhysicalMachineLoad> list = entityManager().createQuery(jpaQuery, PhysicalMachineLoad.class)
				.setFirstResult(0).setMaxResults(1).getResultList();
		if (list.size() == 1) {
			return list.get(0);
		} else {
			return null;
		}
	}

	/**
	 * get physical machine load list.
	 * 
	 * @param hostName - machine name
	 * @param firstResult
	 *            - start position of the first result
	 * @param maxResults
	 *            - maximum number of results
	 * @param sortFieldName
	 *            - sort by field
	 * @param sortOrder
	 *            - sort order
	 * @return physical machine load list
	 * @author xiangqian
	 */
	public static List<PhysicalMachineLoad> getPhysicalMachineLoadEntries(String hostName, int firstResult,
			int maxResults, String sortFieldName, String sortOrder) {
		String jpaQuery = "SELECT o FROM PhysicalMachineLoad o WHERE o.hostName = '" + hostName + "'";
		if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
			jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
			if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
				jpaQuery = jpaQuery + " " + sortOrder;
			}
		}
		return entityManager().createQuery(jpaQuery, PhysicalMachineLoad.class).setFirstResult(firstResult)
				.setMaxResults(maxResults).getResultList();
	}

	/**
	 * get physical machine load count of specified machine.
	 * 
	 * @param hostName
	 *            - machine name
	 * @return physical machine load count
	 * @author xiangqian
	 */
	public static long countPhysicalMachineLoads(String hostName) {
		String jpaString = "SELECT COUNT(o) FROM PhysicalMachineLoad o WHERE o.hostName = '" + hostName + "'";
		TypedQuery<Long> jpaQuery = entityManager().createQuery(jpaString, Long.class);
		Long count = jpaQuery.getSingleResult();
		return count;
	}

	/**
	 * remove the oldest load data of specified machine.
	 * 
	 * @param hostName
	 *            - machine name
	 * @author xiangqian
	 */
	public static void roundRobinPhysicalMachineLoad(String hostName) {
		String jpaString = "SELECT o FROM PhysicalMachineLoad o WHERE o.hostName = '" + hostName
				+ "' ORDER BY o.reportTime ASC";
		TypedQuery<PhysicalMachineLoad> jpaQuery = entityManager().createQuery(jpaString, PhysicalMachineLoad.class);
		List<PhysicalMachineLoad> list = jpaQuery.getResultList();
		if (list.size() > 0) {
			PhysicalMachineLoad oldestLoad = list.get(0);
			oldestLoad.remove();
			oldestLoad.flush();
			oldestLoad.clear();
		}

		return;
	}
}
