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

import com.sinosoft.enumerator.OperationSeverityEnum;
import com.sinosoft.enumerator.OperationStatusEnum;
import com.sinosoft.enumerator.ServiceNameEnum;

@RooJavaBean
@RooToString
@RooJpaActiveRecord
public class OperationLog {

	/**
     */
	private String operator;

	/**
     */
	@Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat(style = "M-")
	private Date operationTime;

	/**
     */
	private String operation;

	@Enumerated(EnumType.STRING)
	private OperationStatusEnum operationStatus;

	/**
     */
	@Enumerated(EnumType.STRING)
	private ServiceNameEnum serviceName;

	/**
     */
	private String objectId;

	/**
     */
	private String operationResult;

	/**
     */
	@Enumerated(EnumType.STRING)
	private OperationSeverityEnum severityLevel;

	/**
	 * get operation log count, the operations are executed by the given user.
	 * 
	 * @param username
	 *            - user name
	 * @param serviceName
	 *            - service name
	 * @param operator
	 *            - operator
	 * @param operationSeverity
	 *            - severity level
	 * @param objectId
	 *            - object id
	 * @param startTime
	 *            - operation time start from
	 * @param endTime
	 *            - operation time end with
	 * @return operation log count
	 * @throws ParseException
	 * @author xiangqian
	 */
	public static long countOperationLogsByUser(String username, String serviceName, String operator,
			String operationStatus, String operationSeverity, String objectId, String startTime, String endTime)
			throws ParseException {
		String jpaString = "SELECT COUNT(o) FROM OperationLog o WHERE o.operator = '" + username + "' ";

		if ((null != serviceName) && (serviceName.isEmpty() == false)) {
			jpaString += "AND o.serviceName = :serviceName ";
		}

		if ((null != operator) && (operator.isEmpty() == false)) {
			jpaString += "AND o.operator = :operator ";
		}

		if ((null != operationStatus) && (operationStatus.isEmpty() == false)) {
			jpaString += "AND o.operationStatus = :operationStatus ";
		}

		if ((null != operationSeverity) && (operationSeverity.isEmpty() == false)) {
			jpaString += "AND o.severityLevel = :severityLevel ";
		}

		if ((null != objectId) && (objectId.isEmpty() == false)) {
			jpaString += "AND o.objectId = :objectId ";
		}

		if ((null != startTime) && (startTime.isEmpty() == false)) {
			jpaString += "AND o.operationTime >= :startTime ";
		}

		if ((null != endTime) && (endTime.isEmpty() == false)) {
			jpaString += "AND o.operationTime <= :endTime ";
		}

		TypedQuery<Long> jpaQuery = entityManager().createQuery(jpaString, Long.class);

		/*
		 * parameter type must be the same to the entity field type.
		 */
		if ((null != serviceName) && (serviceName.isEmpty() == false)) {
			jpaQuery.setParameter("serviceName", ServiceNameEnum.valueOf(serviceName));
		}

		if ((null != operator) && (operator.isEmpty() == false)) {
			jpaQuery.setParameter("operator", operator);
		}

		if ((null != operationStatus) && (operationStatus.isEmpty() == false)) {
			jpaQuery.setParameter("operationStatus", OperationStatusEnum.valueOf(operationStatus));
		}

		if ((null != operationSeverity) && (operationSeverity.isEmpty() == false)) {
			jpaQuery.setParameter("severityLevel", OperationSeverityEnum.valueOf(operationSeverity));
		}

		if ((null != objectId) && (objectId.isEmpty() == false)) {
			jpaQuery.setParameter("objectId", objectId);
		}

		if ((null != startTime) && (startTime.isEmpty() == false)) {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			jpaQuery.setParameter("startTime", formatter.parse(startTime));
		}

		if ((null != endTime) && (endTime.isEmpty() == false)) {
			/*
			 * plus one day, so the result include records of the end time day.
			 */
			DateTimeFormatter jodaFormatter = org.joda.time.format.DateTimeFormat.forPattern("yyyy-MM-dd");
			DateTime jodaDateTime = jodaFormatter.parseDateTime(endTime);
			jodaDateTime = jodaDateTime.plusDays(1);
			Date endTimePlus = jodaDateTime.toDate();

			jpaQuery.setParameter("endTime", endTimePlus);
		}

		long count = jpaQuery.getSingleResult();
		return count;
	}

	/**
	 * get operation log list, the operations are executed by the given user.
	 * 
	 * @param username
	 *            - user name
	 * @param serviceName
	 *            - service name
	 * @param operator
	 *            - operator
	 * @param operationSeverity
	 *            - severity level
	 * @param objectId
	 *            - object id
	 * @param startTime
	 *            - operation time start from
	 * @param endTime
	 *            - operation time end with
	 * @param firstResult
	 *            - start position of the first result
	 * @param maxResults
	 *            - maximum number of results
	 * @param sortFieldName
	 *            - sort by field
	 * @param sortOrder
	 *            - sort order
	 * @return operation log list
	 * @throws ParseException
	 * @author xiangqian
	 */
	public static List<OperationLog> getOperationLogEntriesByUser(String username, String serviceName, String operator,
			String operationStatus, String operationSeverity, String objectId, String startTime, String endTime,
			int firstResult, int maxResults, String sortFieldName, String sortOrder) throws ParseException {
		TypedQuery<OperationLog> jpaQuery = getRecordByUserJpaQuery(username, serviceName, operator, operationStatus,
				operationSeverity, objectId, startTime, endTime, sortFieldName, sortOrder);

		List<OperationLog> operationLogs = jpaQuery.setFirstResult(firstResult).setMaxResults(maxResults)
				.getResultList();
		return operationLogs;
	}

	private static TypedQuery<OperationLog> getRecordByUserJpaQuery(String username, String serviceName,
			String operator, String operationStatus, String operationSeverity, String objectId, String startTime,
			String endTime, String sortFieldName, String sortOrder) throws ParseException {
		String jpaString = "SELECT o FROM OperationLog o WHERE o.operator = '" + username + "' ";

		if ((null != serviceName) && (serviceName.isEmpty() == false)) {
			jpaString += "AND o.serviceName = :serviceName ";
		}

		if ((null != operator) && (operator.isEmpty() == false)) {
			jpaString += "AND o.operator = :operator ";
		}

		if ((null != operationStatus) && (operationStatus.isEmpty() == false)) {
			jpaString += "AND o.operationStatus = :operationStatus ";
		}

		if ((null != operationSeverity) && (operationSeverity.isEmpty() == false)) {
			jpaString += "AND o.severityLevel = :severityLevel ";
		}

		if ((null != objectId) && (objectId.isEmpty() == false)) {
			jpaString += "AND o.objectId = :objectId ";
		}

		if ((null != startTime) && (startTime.isEmpty() == false)) {
			jpaString += "AND o.operationTime >= :startTime ";
		}

		if ((null != endTime) && (endTime.isEmpty() == false)) {
			jpaString += "AND o.operationTime <= :endTime ";
		}

		if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
			jpaString = jpaString + " ORDER BY " + sortFieldName;
			if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
				jpaString = jpaString + " " + sortOrder;
			}
		}

		TypedQuery<OperationLog> jpaQuery = entityManager().createQuery(jpaString, OperationLog.class);

		/*
		 * parameter type must be the same to the entity field type.
		 */
		if ((null != serviceName) && (serviceName.isEmpty() == false)) {
			jpaQuery.setParameter("serviceName", ServiceNameEnum.valueOf(serviceName));
		}

		if ((null != operator) && (operator.isEmpty() == false)) {
			jpaQuery.setParameter("operator", operator);
		}

		if ((null != operationStatus) && (operationStatus.isEmpty() == false)) {
			jpaQuery.setParameter("operationStatus", OperationStatusEnum.valueOf(operationStatus));
		}

		if ((null != operationSeverity) && (operationSeverity.isEmpty() == false)) {
			jpaQuery.setParameter("severityLevel", OperationSeverityEnum.valueOf(operationSeverity));
		}

		if ((null != objectId) && (objectId.isEmpty() == false)) {
			jpaQuery.setParameter("objectId", objectId);
		}

		if ((null != startTime) && (startTime.isEmpty() == false)) {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			jpaQuery.setParameter("startTime", formatter.parse(startTime));
		}

		if ((null != endTime) && (endTime.isEmpty() == false)) {
			/*
			 * plus one day, so the result include records of the end time day.
			 */
			DateTimeFormatter jodaFormatter = org.joda.time.format.DateTimeFormat.forPattern("yyyy-MM-dd");
			DateTime jodaDateTime = jodaFormatter.parseDateTime(endTime);
			jodaDateTime = jodaDateTime.plusDays(1);
			Date endTimePlus = jodaDateTime.toDate();

			jpaQuery.setParameter("endTime", endTimePlus);
		}

		return jpaQuery;
	}

	/**
	 * get operation log list, the operations are executed by the given user.
	 * 
	 * @param username
	 *            - user name
	 * @param serviceName
	 *            - service name
	 * @param operator
	 *            - operator
	 * @param operationSeverity
	 *            - severity level
	 * @param objectId
	 *            - object id
	 * @param startTime
	 *            - operation time start from
	 * @param endTime
	 *            - operation time end with
	 * @param sortFieldName
	 *            - sort by field
	 * @param sortOrder
	 *            - sort order
	 * @return operation log list
	 * @throws ParseException
	 * @author xiangqian
	 */
	public static List<OperationLog> getOperationLogsByUser(String username, String serviceName, String operator,
			String operationStatus, String operationSeverity, String objectId, String startTime, String endTime,
			String sortFieldName, String sortOrder) throws ParseException {
		TypedQuery<OperationLog> jpaQuery = getRecordByUserJpaQuery(username, serviceName, operator, operationStatus,
				operationSeverity, objectId, startTime, endTime, sortFieldName, sortOrder);

		List<OperationLog> operationLogs = jpaQuery.getResultList();
		return operationLogs;
	}

	/**
	 * get operation log count.
	 * 
	 * @param serviceName
	 *            - service name
	 * @param operator
	 *            - operator
	 * @param operationSeverity
	 *            - severity level
	 * @param objectId
	 *            - object id
	 * @param startTime
	 *            - operation time start from
	 * @param endTime
	 *            - operation time end with
	 * @return operation log count
	 * @author xiangqian
	 */
	public static long countOperationLogsByManager(String serviceName, String operator, String operationStatus,
			String operationSeverity, String objectId, String startTime, String endTime) {
		/*
		 * not allowed to join tables using JPQL when there is no association in the object model, use NativeSQL
		 * instead. http://arnosoftwaredev.blogspot.hk/2010/10/hibernate-joins-without-associations.html
		 */
		String jpaString = "SELECT COUNT(o.operator) FROM operation_log o INNER JOIN user u on u.username = o.operator INNER JOIN role r ON r.role_id = u.role_id WHERE r.role_name IN ('ROLE_MANAGER', 'ROLE_USER') ";

		/*
		 * Named parameters are not supported by JPA in native queries, only for JPQL. You must use positional
		 * parameters. http://stackoverflow.com/questions/28829818/how-to-create-a-native-query-with-named-parameters
		 */
		if ((null != serviceName) && (serviceName.isEmpty() == false)) {
			jpaString += "AND o.service_name = ?1 ";
		}

		if ((null != operator) && (operator.isEmpty() == false)) {
			jpaString += "AND o.operator = ?2 ";
		}

		if ((null != operationStatus) && (operationStatus.isEmpty() == false)) {
			jpaString += "AND o.operation_status = ?3 ";
		}

		if ((null != operationSeverity) && (operationSeverity.isEmpty() == false)) {
			jpaString += "AND o.severity_level = ?4 ";
		}

		if ((null != objectId) && (objectId.isEmpty() == false)) {
			jpaString += "AND o.object_id = ?5 ";
		}

		if ((null != startTime) && (startTime.isEmpty() == false)) {
			jpaString += "AND o.operation_time >= ?6 ";
		}

		if ((null != endTime) && (endTime.isEmpty() == false)) {
			jpaString += "AND o.operation_time <= ?7 ";
		}

		Query jpaQuery = entityManager().createNativeQuery(jpaString);

		/*
		 * use string parameter.
		 */
		if ((null != serviceName) && (serviceName.isEmpty() == false)) {
			jpaQuery.setParameter(1, serviceName);
		}

		if ((null != operator) && (operator.isEmpty() == false)) {
			jpaQuery.setParameter(2, operator);
		}

		if ((null != operationStatus) && (operationStatus.isEmpty() == false)) {
			jpaQuery.setParameter(3, operationStatus);
		}

		if ((null != operationSeverity) && (operationSeverity.isEmpty() == false)) {
			jpaQuery.setParameter(4, operationSeverity);
		}

		if ((null != objectId) && (objectId.isEmpty() == false)) {
			jpaQuery.setParameter(5, objectId);
		}

		if ((null != startTime) && (startTime.isEmpty() == false)) {
			jpaQuery.setParameter(6, startTime);
		}

		if ((null != endTime) && (endTime.isEmpty() == false)) {
			/*
			 * plus one day, so the result include records of the end time day.
			 */
			DateTimeFormatter jodaFormatter = org.joda.time.format.DateTimeFormat.forPattern("yyyy-MM-dd");
			DateTime jodaDateTime = jodaFormatter.parseDateTime(endTime);
			jodaDateTime = jodaDateTime.plusDays(1);
			Date endTimePlus = jodaDateTime.toDate();

			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			jpaQuery.setParameter(7, formatter.format(endTimePlus));
		}

		BigInteger c = (BigInteger) jpaQuery.getSingleResult();
		long count = c.longValue();
		return count;
	}

	/**
	 * get operation log list.
	 * 
	 * @param serviceName
	 *            - service name
	 * @param operator
	 *            - operator
	 * @param operationSeverity
	 *            - severity level
	 * @param objectId
	 *            - object id
	 * @param startTime
	 *            - operation time start from
	 * @param endTime
	 *            - operation time end with
	 * @param firstResult
	 *            - start position of the first result
	 * @param maxResults
	 *            - maximum number of results
	 * @param sortFieldName
	 *            - sort by field
	 * @param sortOrder
	 *            - sort order
	 * @return operation log list
	 * @author xiangqian
	 */
	public static List<OperationLog> getOperationLogEntriesByManager(String serviceName, String operator,
			String operationStatus, String operationSeverity, String objectId, String startTime, String endTime,
			int firstResult, int maxResults, String sortFieldName, String sortOrder) {
		Query jpaQuery = getOperationLogsByManagerJpaQuery(serviceName, operator, operationStatus, operationSeverity,
				objectId, startTime, endTime, sortFieldName, sortOrder);

		@SuppressWarnings("unchecked")
		List<OperationLog> operationLogs = (List<OperationLog>) jpaQuery.setFirstResult(firstResult)
				.setMaxResults(maxResults).getResultList();

		return operationLogs;
	}

	private static Query getOperationLogsByManagerJpaQuery(String serviceName, String operator, String operationStatus,
			String operationSeverity, String objectId, String startTime, String endTime, String sortFieldName,
			String sortOrder) {
		/*
		 * not allowed to join tables using JPQL when there is no association in the object model, use NativeSQL
		 * instead. http://arnosoftwaredev.blogspot.hk/2010/10/hibernate-joins-without-associations.html
		 */
		String jpaString = "SELECT o.* FROM operation_log o INNER JOIN user u on u.username = o.operator INNER JOIN role r ON r.role_id = u.role_id WHERE r.role_name IN ('ROLE_MANAGER', 'ROLE_USER') ";

		/*
		 * Named parameters are not supported by JPA in native queries, only for JPQL. You must use positional
		 * parameters. http://stackoverflow.com/questions/28829818/how-to-create-a-native-query-with-named-parameters
		 */
		if ((null != serviceName) && (serviceName.isEmpty() == false)) {
			jpaString += "AND o.service_name = ?1 ";
		}

		if ((null != operator) && (operator.isEmpty() == false)) {
			jpaString += "AND o.operator = ?2 ";
		}

		if ((null != operationStatus) && (operationStatus.isEmpty() == false)) {
			jpaString += "AND o.operation_status = ?3 ";
		}

		if ((null != operationSeverity) && (operationSeverity.isEmpty() == false)) {
			jpaString += "AND o.severity_level = ?4 ";
		}

		if ((null != objectId) && (objectId.isEmpty() == false)) {
			jpaString += "AND o.object_id = ?5 ";
		}

		if ((null != startTime) && (startTime.isEmpty() == false)) {
			jpaString += "AND o.operation_time >= ?6 ";
		}

		if ((null != endTime) && (endTime.isEmpty() == false)) {
			jpaString += "AND o.operation_time <= ?7 ";
		}

		jpaString += " ORDER BY " + sortFieldName;
		if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
			jpaString += " " + sortOrder;
		}

		Query jpaQuery = entityManager().createNativeQuery(jpaString, OperationLog.class);

		/*
		 * use string parameter.
		 */
		if ((null != serviceName) && (serviceName.isEmpty() == false)) {
			jpaQuery.setParameter(1, serviceName);
		}

		if ((null != operator) && (operator.isEmpty() == false)) {
			jpaQuery.setParameter(2, operator);
		}

		if ((null != operationStatus) && (operationStatus.isEmpty() == false)) {
			jpaQuery.setParameter(3, operationStatus);
		}

		if ((null != operationSeverity) && (operationSeverity.isEmpty() == false)) {
			jpaQuery.setParameter(4, operationSeverity);
		}

		if ((null != objectId) && (objectId.isEmpty() == false)) {
			jpaQuery.setParameter(5, objectId);
		}

		if ((null != startTime) && (startTime.isEmpty() == false)) {
			jpaQuery.setParameter(6, startTime);
		}

		if ((null != endTime) && (endTime.isEmpty() == false)) {
			/*
			 * plus one day, so the result include records of the end time day.
			 */
			DateTimeFormatter jodaFormatter = org.joda.time.format.DateTimeFormat.forPattern("yyyy-MM-dd");
			DateTime jodaDateTime = jodaFormatter.parseDateTime(endTime);
			jodaDateTime = jodaDateTime.plusDays(1);
			Date endTimePlus = jodaDateTime.toDate();

			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			jpaQuery.setParameter(7, formatter.format(endTimePlus));
		}

		return jpaQuery;
	}

	/**
	 * get operation log list.
	 * 
	 * @param serviceName
	 *            - service name
	 * @param operator
	 *            - operator
	 * @param operationSeverity
	 *            - severity level
	 * @param objectId
	 *            - object id
	 * @param startTime
	 *            - operation time start from
	 * @param endTime
	 *            - operation time end with
	 * @param sortFieldName
	 *            - sort by field
	 * @param sortOrder
	 *            - sort order
	 * @return operation log list
	 * @author xiangqian
	 */
	public static List<OperationLog> getOperationLogsByManager(String serviceName, String operator,
			String operationStatus, String operationSeverity, String objectId, String startTime, String endTime,
			String sortFieldName, String sortOrder) {
		Query jpaQuery = getOperationLogsByManagerJpaQuery(serviceName, operator, operationStatus, operationSeverity,
				objectId, startTime, endTime, sortFieldName, sortOrder);

		@SuppressWarnings("unchecked")
		List<OperationLog> operationLogs = (List<OperationLog>) jpaQuery.getResultList();

		return operationLogs;
	}
}
