package com.sinoparasoft.model;

import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Query;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

import com.sinoparasoft.enumerator.AlarmSeverityEnum;
import com.sinoparasoft.enumerator.MonitorNameEnum;
import com.sinoparasoft.enumerator.MonitorSourceEnum;
import com.sinoparasoft.enumerator.MonitorStatusEnum;
import com.sinoparasoft.enumerator.MonitorTypeEnum;

@RooJavaBean
@RooToString
@RooJpaActiveRecord
public class MonitorLog {

	/**
     */
	@Enumerated(EnumType.STRING)
	private MonitorSourceEnum monitorSource;

	/**
     */
	private String sourceId;

	/**
     */
	@Enumerated(EnumType.STRING)
	private MonitorTypeEnum monitorType;

	/**
     */
	@Enumerated(EnumType.STRING)
	private MonitorNameEnum monitorName;

	/**
     */
	@Enumerated(EnumType.STRING)
	private MonitorStatusEnum monitorStatus;

	/**
	 */
	private String message;

	/**
	 */
	@Enumerated(EnumType.STRING)
	private AlarmSeverityEnum severityLevel;

	/**
	 * time when the monitor status changed.
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat(style = "M-")
	private Date updateTime;

	/**
	 * last check time.
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat(style = "M-")
	private Date checkTime;

	/**
	 * get the latest monitor log.
	 * 
	 * @param sourceId
	 *            - machine id
	 * @param monitorType
	 *            - service type
	 * @param monitorName
	 *            - monitor name
	 * @return latest monitor log, or null if the number of qualified log not equals to 1.
	 * @author xiangqian
	 */
	public static MonitorLog getLatestMonitorLog(String sourceId, MonitorTypeEnum monitorType,
			MonitorNameEnum monitorName) {
		/*
		 * the log must be updated within 5 minutes (x)
		 */
		// DateTime limitTime = new DateTime().minusMinutes(5);
		// DateTimeFormatter formatter = org.joda.time.format.DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
		//
		// String jpaString = "SELECT m FROM MonitorLog m WHERE m.sourceId = '" + sourceId + "' AND m.monitorType = '"
		// + monitorType.name() + "' AND m.monitorName = '" + monitorName.name() + "' AND m.updateTime >= '"
		// + limitTime.toString(formatter) + "' ORDER BY m.updateTime DESC";

		String jpaString = "SELECT m FROM MonitorLog m WHERE m.sourceId = '" + sourceId + "' AND m.monitorType = '"
				+ monitorType.name() + "' AND m.monitorName = '" + monitorName.name() + "' ORDER BY m.updateTime DESC";
		TypedQuery<MonitorLog> jpaQuery = entityManager().createQuery(jpaString, MonitorLog.class);
		List<MonitorLog> monitorLogs = jpaQuery.setFirstResult(0).setMaxResults(1).getResultList();
		if (1 == monitorLogs.size()) {
			return monitorLogs.get(0);
		} else {
			return null;
		}
	}

	/**
	 * get monitor log count.
	 * 
	 * @param monitorSource
	 *            - monitor source
	 * @param monitorType
	 *            - service type
	 * @param monitorName
	 *            - monitor name
	 * @param monitorStatus
	 *            - monitor status
	 * @param sourceId
	 *            - source id
	 * @param beginningUpdateTime
	 *            - update time start from
	 * @param endUpdateTime
	 *            - update time end with
	 * @return monitor log count
	 * @throws ParseException
	 * @author xiangqian
	 */
	public static long countMonitorLogs(String monitorSource, String monitorType, String monitorName,
			String monitorStatus, String sourceId, String beginningUpdateTime, String endUpdateTime)
			throws ParseException {
		String jpaString = "SELECT COUNT(m.id) FROM MonitorLog m WHERE 1 = 1 ";

		if ((null != monitorSource) && (monitorSource.isEmpty() == false)) {
			jpaString += "AND m.monitorSource = :monitorSource ";
		}

		if ((null != monitorType) && (monitorType.isEmpty() == false)) {
			jpaString += "AND m.monitorType = :monitorType ";
		}

		if ((null != monitorName) && (monitorName.isEmpty() == false)) {
			jpaString += "AND m.monitorName = :monitorName ";
		}

		if ((null != monitorStatus) && (monitorStatus.isEmpty() == false)) {
			jpaString += "AND m.monitorStatus = :monitorStatus ";
		}

		if ((null != sourceId) && (sourceId.isEmpty() == false)) {
			jpaString += "AND m.sourceId = :sourceId ";
		}

		if ((null != beginningUpdateTime) && (beginningUpdateTime.isEmpty() == false)) {
			jpaString += "AND m.updateTime >= :beginningUpdateTime ";
		}

		if ((null != endUpdateTime) && (endUpdateTime.isEmpty() == false)) {
			jpaString += "AND m.updateTime <= :endUpdateTime ";
		}

		TypedQuery<Long> jpaQuery = entityManager().createQuery(jpaString, Long.class);

		/*
		 * parameter type must be the same to the entity field type.
		 */
		if ((null != monitorSource) && (monitorSource.isEmpty() == false)) {
			jpaQuery.setParameter("monitorSource", MonitorSourceEnum.valueOf(monitorSource));
		}

		if ((null != monitorType) && (monitorType.isEmpty() == false)) {
			jpaQuery.setParameter("monitorType", MonitorTypeEnum.valueOf(monitorType));
		}

		if ((null != monitorName) && (monitorName.isEmpty() == false)) {
			jpaQuery.setParameter("monitorName", MonitorNameEnum.valueOf(monitorName));
		}

		if ((null != monitorStatus) && (monitorStatus.isEmpty() == false)) {
			jpaQuery.setParameter("monitorStatus", MonitorStatusEnum.valueOf(monitorStatus));
		}

		if ((null != sourceId) && (sourceId.isEmpty() == false)) {
			jpaQuery.setParameter("sourceId", sourceId);
		}

		if ((null != beginningUpdateTime) && (beginningUpdateTime.isEmpty() == false)) {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			jpaQuery.setParameter("beginningUpdateTime", formatter.parse(beginningUpdateTime));
		}

		if ((null != endUpdateTime) && (endUpdateTime.isEmpty() == false)) {
			/*
			 * plus one day, so the result include records of the end time day.
			 */
			DateTimeFormatter jodaFormatter = org.joda.time.format.DateTimeFormat.forPattern("yyyy-MM-dd");
			DateTime jodaDateTime = jodaFormatter.parseDateTime(endUpdateTime);
			jodaDateTime = jodaDateTime.plusDays(1);
			Date endTime = jodaDateTime.toDate();

			jpaQuery.setParameter("endUpdateTime", endTime);
		}

		long count = jpaQuery.getSingleResult();
		return count;
	}

	/**
	 * get monitor log list in specified order.
	 * 
	 * @param monitorSource
	 *            - monitor source
	 * @param monitorType
	 *            - service type
	 * @param monitorName
	 *            - monitor name
	 * @param monitorStatus
	 *            - monitor status
	 * @param sourceId
	 *            - source id
	 * @param beginningUpdateTime
	 *            - update time start from
	 * @param endUpdateTime
	 *            - update time end with
	 * @param firstResult
	 *            - start position of the first result
	 * @param maxResults
	 *            - maximum number of results
	 * @param sortFieldName
	 *            - sort by field
	 * @param sortOrder
	 *            - sort order
	 * @return monitor log list
	 * @throws ParseException
	 * @author xiangqian
	 */
	public static List<MonitorLog> getMonitorLogEntries(String monitorSource, String monitorType, String monitorName,
			String monitorStatus, String sourceId, String beginningUpdateTime, String endUpdateTime, int firstResult,
			int maxResults, String sortFieldName, String sortOrder) throws ParseException {
		TypedQuery<MonitorLog> jpaQuery = getMonitorLogJpaQuery(monitorSource, monitorType, monitorName, monitorStatus,
				sourceId, beginningUpdateTime, endUpdateTime, sortFieldName, sortOrder);

		List<MonitorLog> monitorLogs = jpaQuery.setFirstResult(firstResult).setMaxResults(maxResults).getResultList();

		return monitorLogs;
	}

	private static TypedQuery<MonitorLog> getMonitorLogJpaQuery(String monitorSource, String monitorType,
			String monitorName, String monitorStatus, String sourceId, String beginningUpdateTime,
			String endUpdateTime, String sortFieldName, String sortOrder) throws ParseException {
		String jpaString = "SELECT m FROM MonitorLog m WHERE 1 = 1 ";

		if ((null != monitorSource) && (monitorSource.isEmpty() == false)) {
			jpaString += "AND m.monitorSource = :monitorSource ";
		}

		if ((null != monitorType) && (monitorType.isEmpty() == false)) {
			jpaString += "AND m.monitorType = :monitorType ";
		}

		if ((null != monitorName) && (monitorName.isEmpty() == false)) {
			jpaString += "AND m.monitorName = :monitorName ";
		}

		if ((null != monitorStatus) && (monitorStatus.isEmpty() == false)) {
			jpaString += "AND m.monitorStatus = :monitorStatus ";
		}

		if ((null != sourceId) && (sourceId.isEmpty() == false)) {
			jpaString += "AND m.sourceId = :sourceId ";
		}

		if ((null != beginningUpdateTime) && (beginningUpdateTime.isEmpty() == false)) {
			jpaString += "AND m.updateTime >= :beginningUpdateTime ";
		}

		if ((null != endUpdateTime) && (endUpdateTime.isEmpty() == false)) {
			jpaString += "AND m.updateTime <= :endUpdateTime ";
		}

		if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
			jpaString = jpaString + " ORDER BY " + sortFieldName;
			if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
				jpaString = jpaString + " " + sortOrder;
			}
		}

		TypedQuery<MonitorLog> jpaQuery = entityManager().createQuery(jpaString, MonitorLog.class);

		/*
		 * parameter type must be the same to the entity field type.
		 */
		if ((null != monitorSource) && (monitorSource.isEmpty() == false)) {
			jpaQuery.setParameter("monitorSource", MonitorSourceEnum.valueOf(monitorSource));
		}

		if ((null != monitorType) && (monitorType.isEmpty() == false)) {
			jpaQuery.setParameter("monitorType", MonitorTypeEnum.valueOf(monitorType));
		}

		if ((null != monitorName) && (monitorName.isEmpty() == false)) {
			jpaQuery.setParameter("monitorName", MonitorNameEnum.valueOf(monitorName));
		}

		if ((null != monitorStatus) && (monitorStatus.isEmpty() == false)) {
			jpaQuery.setParameter("monitorStatus", MonitorStatusEnum.valueOf(monitorStatus));
		}

		if ((null != sourceId) && (sourceId.isEmpty() == false)) {
			jpaQuery.setParameter("sourceId", sourceId);
		}

		if ((null != beginningUpdateTime) && (beginningUpdateTime.isEmpty() == false)) {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			jpaQuery.setParameter("beginningUpdateTime", formatter.parse(beginningUpdateTime));
		}

		if ((null != endUpdateTime) && (endUpdateTime.isEmpty() == false)) {
			/*
			 * plus one day, so the result include records of the end time day.
			 */
			DateTimeFormatter jodaFormatter = org.joda.time.format.DateTimeFormat.forPattern("yyyy-MM-dd");
			DateTime jodaDateTime = jodaFormatter.parseDateTime(endUpdateTime);
			jodaDateTime = jodaDateTime.plusDays(1);
			Date endTime = jodaDateTime.toDate();

			jpaQuery.setParameter("endUpdateTime", endTime);
		}
		return jpaQuery;
	}

	/**
	 * get monitor log list in specified order.
	 * 
	 * @param monitorSource
	 *            - monitor source
	 * @param monitorType
	 *            - service type
	 * @param monitorName
	 *            - monitor name
	 * @param monitorStatus
	 *            - monitor status
	 * @param sourceId
	 *            - source id
	 * @param beginningUpdateTime
	 *            - update time start from
	 * @param endUpdateTime
	 *            - update time end with
	 * @param sortFieldName
	 *            - sort by field
	 * @param sortOrder
	 *            - sort order
	 * @return monitor log list
	 * @throws ParseException
	 * @author xiangqian
	 */
	public static List<MonitorLog> getMonitorLogs(String monitorSource, String monitorType, String monitorName,
			String monitorStatus, String sourceId, String beginningUpdateTime, String endUpdateTime,
			String sortFieldName, String sortOrder) throws ParseException {
		TypedQuery<MonitorLog> jpaQuery = getMonitorLogJpaQuery(monitorSource, monitorType, monitorName, monitorStatus,
				sourceId, beginningUpdateTime, endUpdateTime, sortFieldName, sortOrder);

		List<MonitorLog> monitorLogs = jpaQuery.getResultList();

		return monitorLogs;
	}

	/**
	 * get monitor log count. the monitor log is related to virtual machine of specified user.
	 * 
	 * @param username
	 *            - virtual machine manager
	 * @param monitorSource
	 *            - monitor source
	 * @param monitorType
	 *            - service type
	 * @param monitorName
	 *            - monitor name
	 * @param monitorStatus
	 *            - monitor status
	 * @param sourceId
	 *            - source id
	 * @param beginningUpdateTime
	 *            - update time start from
	 * @param endUpdateTime
	 *            - update time end with
	 * @return monitor log count
	 * @throws ParseException
	 * @author xiangqian
	 */
	public static long countMonitorLogsByUser(String username, String monitorSource, String monitorType,
			String monitorName, String monitorStatus, String sourceId, String beginningUpdateTime, String endUpdateTime)
			throws ParseException {
		/*
		 * not allowed to join tables using JPQL when there is no association in the object model, use NativeSQL
		 * instead. http://arnosoftwaredev.blogspot.hk/2010/10/hibernate-joins-without-associations.html
		 */
		String jpaString = "SELECT COUNT(m.id) FROM monitor_log m INNER JOIN virtual_machine v ON v.host_id = m.source_id WHERE v.manager = '"
				+ username + "' ";

		/*
		 * Named parameters are not supported by JPA in native queries, only for JPQL. You must use positional
		 * parameters. http://stackoverflow.com/questions/28829818/how-to-create-a-native-query-with-named-parameters
		 */
		if ((null != monitorSource) && (monitorSource.isEmpty() == false)) {
			jpaString += "AND m.monitor_source = ?1 ";
		}

		if ((null != monitorType) && (monitorType.isEmpty() == false)) {
			jpaString += "AND m.monitor_type = ?2 ";
		}

		if ((null != monitorName) && (monitorName.isEmpty() == false)) {
			jpaString += "AND m.monitor_name = ?3 ";
		}

		if ((null != monitorStatus) && (monitorStatus.isEmpty() == false)) {
			jpaString += "AND m.monitor_status = ?4 ";
		}

		if ((null != sourceId) && (sourceId.isEmpty() == false)) {
			jpaString += "AND m.source_id = ?5 ";
		}

		if ((null != beginningUpdateTime) && (beginningUpdateTime.isEmpty() == false)) {
			jpaString += "AND m.update_time >= ?6 ";
		}

		if ((null != endUpdateTime) && (endUpdateTime.isEmpty() == false)) {
			jpaString += "AND m.update_time <= ?7 ";
		}

		/*
		 * org.hibernate.MappingException: Unknown entity: java.lang.Long;
		 */
		// Query jpaQuery = entityManager().createNativeQuery(jpaString, Long.class);
		Query jpaQuery = entityManager().createNativeQuery(jpaString);

		/*
		 * use string parameter.
		 */
		if ((null != monitorSource) && (monitorSource.isEmpty() == false)) {
			jpaQuery.setParameter(1, monitorSource);
		}

		if ((null != monitorType) && (monitorType.isEmpty() == false)) {
			jpaQuery.setParameter(2, monitorType);
		}

		if ((null != monitorName) && (monitorName.isEmpty() == false)) {
			jpaQuery.setParameter(3, monitorName);
		}

		if ((null != monitorStatus) && (monitorStatus.isEmpty() == false)) {
			jpaQuery.setParameter(4, monitorStatus);
		}

		if ((null != sourceId) && (sourceId.isEmpty() == false)) {
			jpaQuery.setParameter(5, sourceId);
		}

		if ((null != beginningUpdateTime) && (beginningUpdateTime.isEmpty() == false)) {
			// SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			// jpaQuery.setParameter(6, formatter.parse(beginningUpdateTime));
			jpaQuery.setParameter(6, beginningUpdateTime);
		}

		if ((null != endUpdateTime) && (endUpdateTime.isEmpty() == false)) {
			/*
			 * plus one day, so the result include records of the end time day.
			 */
			DateTimeFormatter jodaFormatter = org.joda.time.format.DateTimeFormat.forPattern("yyyy-MM-dd");
			DateTime jodaDateTime = jodaFormatter.parseDateTime(endUpdateTime);
			jodaDateTime = jodaDateTime.plusDays(1);
			Date endTime = jodaDateTime.toDate();

			// jpaQuery.setParameter(7, endTime);
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			jpaQuery.setParameter(7, formatter.format(endTime));
		}

		BigInteger c = (BigInteger) jpaQuery.getSingleResult();
		long count = c.longValue();
		return count;
	}

	/**
	 * get monitor log list in specified order. the monitor log is related to virtual machine of specified user.
	 * 
	 * @param username
	 *            - virtual machine manager
	 * @param monitorSource
	 *            - monitor source
	 * @param monitorType
	 *            - service type
	 * @param monitorName
	 *            - monitor name
	 * @param monitorStatus
	 *            - monitor status
	 * @param sourceId
	 *            - source id
	 * @param beginningUpdateTime
	 *            - update time start from
	 * @param endUpdateTime
	 *            - update time end with
	 * @param firstResult
	 *            - start position of the first result
	 * @param maxResults
	 *            - maximum number of results
	 * @param sortFieldName
	 *            - sort by field
	 * @param sortOrder
	 *            - sort order
	 * @return monitor log list
	 * @throws ParseException
	 * @author xiangqian
	 */
	public static List<MonitorLog> getMonitorLogEntriesByUser(String username, String monitorSource,
			String monitorType, String monitorName, String monitorStatus, String sourceId, String beginningUpdateTime,
			String endUpdateTime, int firstResult, int maxResults, String sortFieldName, String sortOrder)
			throws ParseException {
		Query jpaQuery = getMonitorLogByUserJpaQuery(username, monitorSource, monitorType, monitorName, monitorStatus,
				sourceId, beginningUpdateTime, endUpdateTime, sortFieldName, sortOrder);

		@SuppressWarnings("unchecked")
		List<MonitorLog> monitorLogs = (List<MonitorLog>) jpaQuery.setFirstResult(firstResult)
				.setMaxResults(maxResults).getResultList();

		return monitorLogs;
	}

	private static Query getMonitorLogByUserJpaQuery(String username, String monitorSource, String monitorType,
			String monitorName, String monitorStatus, String sourceId, String beginningUpdateTime,
			String endUpdateTime, String sortFieldName, String sortOrder) throws ParseException {
		/*
		 * not allowed to join tables using JPQL when there is no association in the object model, use NativeSQL
		 * instead. http://arnosoftwaredev.blogspot.hk/2010/10/hibernate-joins-without-associations.html
		 */
		String jpaString = "SELECT m.* FROM monitor_log m INNER JOIN virtual_machine v ON v.host_id = m.source_id WHERE v.manager = '"
				+ username + "' ";

		/*
		 * Named parameters are not supported by JPA in native queries, only for JPQL. You must use positional
		 * parameters. http://stackoverflow.com/questions/28829818/how-to-create-a-native-query-with-named-parameters
		 */
		if ((null != monitorSource) && (monitorSource.isEmpty() == false)) {
			jpaString += "AND m.monitor_source = ?1 ";
		}

		if ((null != monitorType) && (monitorType.isEmpty() == false)) {
			jpaString += "AND m.monitor_type = ?2 ";
		}

		if ((null != monitorName) && (monitorName.isEmpty() == false)) {
			jpaString += "AND m.monitor_name = ?3 ";
		}

		if ((null != monitorStatus) && (monitorStatus.isEmpty() == false)) {
			jpaString += "AND m.monitor_status = ?4 ";
		}

		if ((null != sourceId) && (sourceId.isEmpty() == false)) {
			jpaString += "AND m.source_id = ?5 ";
		}

		if ((null != beginningUpdateTime) && (beginningUpdateTime.isEmpty() == false)) {
			jpaString += "AND m.update_time >= ?6 ";
		}

		if ((null != endUpdateTime) && (endUpdateTime.isEmpty() == false)) {
			jpaString += "AND m.update_time <= ?7 ";
		}

		jpaString += " ORDER BY " + sortFieldName;
		if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
			jpaString += " " + sortOrder;
		}

		/*
		 * use the entity class as a parameter to the createNativeQuery to map a query result to an entity.
		 * http://www.thoughts-on-java.org/result-set-mapping-basics/
		 */
		Query jpaQuery = entityManager().createNativeQuery(jpaString, MonitorLog.class);

		/*
		 * use string parameter.
		 */
		if ((null != monitorSource) && (monitorSource.isEmpty() == false)) {
			jpaQuery.setParameter(1, monitorSource);
		}

		if ((null != monitorType) && (monitorType.isEmpty() == false)) {
			jpaQuery.setParameter(2, monitorType);
		}

		if ((null != monitorName) && (monitorName.isEmpty() == false)) {
			jpaQuery.setParameter(3, monitorName);
		}

		if ((null != monitorStatus) && (monitorStatus.isEmpty() == false)) {
			jpaQuery.setParameter(4, monitorStatus);
		}

		if ((null != sourceId) && (sourceId.isEmpty() == false)) {
			jpaQuery.setParameter(5, sourceId);
		}

		if ((null != beginningUpdateTime) && (beginningUpdateTime.isEmpty() == false)) {
			// SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			// jpaQuery.setParameter(6, formatter.parse(beginningUpdateTime));
			jpaQuery.setParameter(6, beginningUpdateTime);
		}

		if ((null != endUpdateTime) && (endUpdateTime.isEmpty() == false)) {
			/*
			 * plus one day, so the result include records of the end time day.
			 */
			DateTimeFormatter jodaFormatter = org.joda.time.format.DateTimeFormat.forPattern("yyyy-MM-dd");
			DateTime jodaDateTime = jodaFormatter.parseDateTime(endUpdateTime);
			jodaDateTime = jodaDateTime.plusDays(1);
			Date endTime = jodaDateTime.toDate();

			// jpaQuery.setParameter(7, endTime);
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			jpaQuery.setParameter(7, formatter.format(endTime));
		}

		return jpaQuery;
	}

	/**
	 * get monitor log list in specified order. the monitor log is related to virtual machine of specified user.
	 * 
	 * @param username
	 *            - virtual machine manager
	 * @param monitorSource
	 *            - monitor source
	 * @param monitorType
	 *            - service type
	 * @param monitorName
	 *            - monitor name
	 * @param monitorStatus
	 *            - monitor status
	 * @param sourceId
	 *            - source id
	 * @param beginningUpdateTime
	 *            - update time start from
	 * @param endUpdateTime
	 *            - update time end with
	 * @param sortFieldName
	 *            - sort by field
	 * @param sortOrder
	 *            - sort order
	 * @return monitor log list
	 * @throws ParseException
	 * @author xiangqian
	 */
	public static List<MonitorLog> getMonitorLogsByUser(String username, String monitorSource, String monitorType,
			String monitorName, String monitorStatus, String sourceId, String beginningUpdateTime,
			String endUpdateTime, String sortFieldName, String sortOrder) throws ParseException {
		Query jpaQuery = getMonitorLogByUserJpaQuery(username, monitorSource, monitorType, monitorName, monitorStatus,
				sourceId, beginningUpdateTime, endUpdateTime, sortFieldName, sortOrder);

		@SuppressWarnings("unchecked")
		List<MonitorLog> monitorLogs = (List<MonitorLog>) jpaQuery.getResultList();

		return monitorLogs;
	}
}
