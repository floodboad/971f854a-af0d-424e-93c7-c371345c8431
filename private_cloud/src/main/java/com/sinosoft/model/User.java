package com.sinosoft.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.sinosoft.enumerator.RoleNameEnum;
import com.sinosoft.enumerator.UserStatusEnum;
import com.sinosoft.util.CustomDateSerializer;

@RooJavaBean
@RooToString
@RooJpaActiveRecord
@JsonIgnoreProperties(value = { "password" })
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "userId")
	private Long userId;

	/**
	 */
	private String username;

	/**
	 */
	private String password;

	/**
	 */
	@Enumerated(EnumType.STRING)
	private UserStatusEnum status;

	/**
	 */
	private String realName;

	/**
	 */
	private String phone;

	/**
	 */
	private String email;

	/**
	 */
	private String department;

	/**
	 */
	private Integer snapshotQuota;

	/**
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat(style = "M-")
	@JsonSerialize(using = CustomDateSerializer.class)
	private Date createTime;

	/**
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat(style = "M-")
	private Date enableTime;

	/**
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat(style = "M-")
	private Date disableTime;

	/**
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat(pattern = "yyyy-MM-dd hh:mm:ss")
	private Date deleteTime;

	/**
	 */
	private String sessionId;

	/**
	 */
	@ManyToOne
	@JoinColumn(name = "roleId")
	private Role role;

	/**
	 * get user by name.
	 * 
	 * @param username
	 *            - user name
	 * @return user with the specified user name, or null if the given parameter is bad or the number of qualified user
	 *         not equals to 1
	 * @author xuchen, xiangqian
	 */
	public static User getUserByUsername(String username) {
		if (username == null || username.length() == 0) {
			return null;
		}

		String jpaString = "SELECT u FROM User u WHERE username = '" + username + "'";
		TypedQuery<User> jpaQuery = entityManager().createQuery(jpaString, User.class);
		List<User> users = jpaQuery.getResultList();
		if (users.size() == 1) {
			return users.get(0);
		} else {
			return null;
		}
	}

	/**
	 * get user list by real name, the users are enabled.
	 * 
	 * @param realName
	 *            - real name
	 * @return user list
	 * @author xuchen, xiangqian
	 */
	public static List<User> getEnableUsersByRealName(String realName) {
		String jpaString = "SELECT u FROM User u WHERE u.realName = '" + realName + "' AND u.status = 'ENABLED'";
		TypedQuery<User> jpaQuery = entityManager().createQuery(jpaString, User.class);
		return jpaQuery.getResultList();
	}

	/**
	 * get user list by role. the users are enabled and online.
	 * 
	 * @param roleName
	 *            - role name
	 * @return user list
	 * @author xiangqian
	 */
	public static List<User> getOnlineUsersByRole(String roleName) {
		String jpaString = "SELECT u FROM User u WHERE u.role.roleName = '" + roleName
				+ "' AND u.status = 'ENABLED' AND u.sessionId <> ''";
		TypedQuery<User> jpaQuery = entityManager().createQuery(jpaString, User.class);
		List<User> users = jpaQuery.getResultList();
		return users;
	}

	/**
	 * get user list by role, the users are enabled.
	 * 
	 * @param roleName
	 *            - role name
	 * @return user list
	 * @author xiangqian
	 */
	public static List<User> getEnableUsersByRole(String roleName) {
		String jpaString = "SELECT u FROM User u WHERE u.role.roleName = '" + roleName + "' AND u.status = '"
				+ UserStatusEnum.ENABLED.name() + "'";
		TypedQuery<User> jpaQuery = entityManager().createQuery(jpaString, User.class);
		List<User> users = jpaQuery.getResultList();
		return users;
	}

	/**
	 * get user by session id.
	 * 
	 * @param sessionId
	 *            - session id.
	 * @return user with the specified id, or null if the number of qualified user not equals to 1.
	 * @author xiangqian
	 */
	public static User getUserBySessionId(String sessionId) {
		String jpaString = "SELECT u FROM User u WHERE u.sessionId = '" + sessionId + "'";
		TypedQuery<User> jpaQuery = entityManager().createQuery(jpaString, User.class);
		List<User> users = jpaQuery.getResultList();

		if (users.size() == 1) {
			return users.get(0);
		} else {
			return null;
		}
	}

	/**
	 * get user count, support user name and real name partial matching.
	 * 
	 * @param username
	 *            - user name
	 * @param realName
	 *            - real name
	 * @param commaSeperatedRoleNameList
	 *            - comma separated role name list
	 * @param status
	 *            - user status
	 * @param department
	 *            - department
	 * @param beginningCreateTime
	 *            - create time start from
	 * @param endCreateTime
	 *            - create time end with
	 * @return user count
	 * @throws ParseException
	 * @author xiangqian
	 */
	public static long countUserEntries(String username, String realName, List<RoleNameEnum> userRoles, String status,
			String department, String beginningCreateTime, String endCreateTime) throws ParseException {
		// IN clause w or w/o parenthesis
		String jpaString = "SELECT COUNT(u.username) FROM User u WHERE u.role.roleName IN :roles ";

		if ((null != username) && (username.isEmpty() == false)) {
			jpaString += "AND u.username like :username ";
		}

		if ((null != realName) && (realName.isEmpty() == false)) {
			jpaString += "AND u.realName like :realName ";
		}

		if ((null != status) && (status.isEmpty() == false) && (false == status.equalsIgnoreCase("NONE"))) {
			jpaString += "AND u.status = :status ";
		}

		if ((null != department) && (department.isEmpty() == false)) {
			jpaString += "AND u.department = :department ";
		}

		if ((null != beginningCreateTime) && (beginningCreateTime.isEmpty() == false)) {
			jpaString += "AND u.createTime >= :beginningCreateTime ";
		}

		if ((null != endCreateTime) && (endCreateTime.isEmpty() == false)) {
			jpaString += "AND u.createTime <= :endCreateTime ";
		}

		TypedQuery<Long> jpaQuery = entityManager().createQuery(jpaString, Long.class);
		// parameter type must be the same to the entity field type
		jpaQuery.setParameter("roles", userRoles);

		if ((null != username) && (username.isEmpty() == false)) {
			jpaQuery.setParameter("username", "%" + username + "%");
		}

		if ((null != realName) && (realName.isEmpty() == false)) {
			jpaQuery.setParameter("realName", "%" + realName + "%");
		}

		if ((null != status) && (status.isEmpty() == false) && (false == status.equalsIgnoreCase("NONE"))) {
			jpaQuery.setParameter("status", UserStatusEnum.valueOf(status));
		}

		if ((null != department) && (department.isEmpty() == false)) {
			jpaQuery.setParameter("department", department);
		}

		if ((null != beginningCreateTime) && (beginningCreateTime.isEmpty() == false)) {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			jpaQuery.setParameter("beginningCreateTime", formatter.parse(beginningCreateTime));
		}

		if ((null != endCreateTime) && (endCreateTime.isEmpty() == false)) {
			/*
			 * plus one day, so the result include records of the end time day.
			 */
			DateTimeFormatter jodaFormatter = org.joda.time.format.DateTimeFormat.forPattern("yyyy-MM-dd");
			DateTime jodaDateTime = jodaFormatter.parseDateTime(endCreateTime);
			jodaDateTime = jodaDateTime.plusDays(1);
			Date endTime = jodaDateTime.toDate();

			jpaQuery.setParameter("endCreateTime", endTime);
		}

		long count = jpaQuery.getSingleResult();
		return count;
	}

	/**
	 * get user list in specified order, support user name and real name partial matching.
	 * 
	 * @param username
	 *            - user name
	 * @param realName
	 *            - real name
	 * @param commaSeperatedRoleNameList
	 *            - comma separated role name list
	 * @param status
	 *            - user status
	 * @param department
	 *            - department
	 * @param beginningCreateTime
	 *            - create time start from
	 * @param endCreateTime
	 *            - create time end with
	 * @param firstResult
	 *            - start position of the first result
	 * @param maxResults
	 *            - maximum number of results
	 * @param sortFieldName
	 *            - sort by field
	 * @param sortOrder
	 *            - sort order
	 * @return user list
	 * @throws ParseException
	 * @author xiangqian
	 */
	public static List<User> getUserEntries(String username, String realName, List<RoleNameEnum> userRoles,
			String status, String department, String beginningCreateTime, String endCreateTime, int firstResult,
			int maxResults, String sortFieldName, String sortOrder) throws ParseException {
		String jpaString = "SELECT u FROM User u WHERE u.role.roleName IN (:roles) ";

		if ((null != username) && (username.isEmpty() == false)) {
			jpaString += "AND u.username like :username ";
		}

		if ((null != realName) && (realName.isEmpty() == false)) {
			jpaString += "AND u.realName like :realName ";
		}

		if ((null != status) && (status.isEmpty() == false) && (false == status.equalsIgnoreCase("NONE"))) {
			jpaString += "AND u.status = :status ";
		}

		if ((null != department) && (department.isEmpty() == false)) {
			jpaString += "AND u.department = :department ";
		}

		if ((null != beginningCreateTime) && (beginningCreateTime.isEmpty() == false)) {
			jpaString += "AND u.createTime >= :beginningCreateTime ";
		}

		if ((null != endCreateTime) && (endCreateTime.isEmpty() == false)) {
			jpaString += "AND u.createTime <= :endCreateTime ";
		}

		if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
			jpaString = jpaString + " ORDER BY " + sortFieldName;
			if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
				jpaString = jpaString + " " + sortOrder;
			}
		}

		TypedQuery<User> jpaQuery = entityManager().createQuery(jpaString, User.class);
		jpaQuery.setParameter("roles", userRoles);

		if ((null != username) && (username.isEmpty() == false)) {
			jpaQuery.setParameter("username", "%" + username + "%");
		}

		if ((null != realName) && (realName.isEmpty() == false)) {
			jpaQuery.setParameter("realName", "%" + realName + "%");
		}

		if ((null != status) && (status.isEmpty() == false) && (false == status.equalsIgnoreCase("NONE"))) {
			jpaQuery.setParameter("status", UserStatusEnum.valueOf(status));
		}

		if ((null != department) && (department.isEmpty() == false)) {
			jpaQuery.setParameter("department", department);
		}

		if ((null != beginningCreateTime) && (beginningCreateTime.isEmpty() == false)) {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			jpaQuery.setParameter("beginningCreateTime", formatter.parse(beginningCreateTime));
		}

		if ((null != endCreateTime) && (endCreateTime.isEmpty() == false)) {
			/*
			 * plus one day, so the result include records of the end time day.
			 */
			DateTimeFormatter jodaFormatter = org.joda.time.format.DateTimeFormat.forPattern("yyyy-MM-dd");
			DateTime jodaDateTime = jodaFormatter.parseDateTime(endCreateTime);
			jodaDateTime = jodaDateTime.plusDays(1);
			Date endTime = jodaDateTime.toDate();

			jpaQuery.setParameter("endCreateTime", endTime);
		}

		List<User> users = jpaQuery.setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
		return users;
	}
}
