package com.sinosoft.model;

import java.util.List;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.TypedQuery;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

import com.sinosoft.enumerator.ExternalIpDeviceOwnerEnum;
import com.sinosoft.enumerator.ExternalIpStatusEnum;

@RooJavaBean
@RooToString
@RooJpaActiveRecord(identifierField = "ip")
public class ExternalIp {

	/**
	 */
	// custom value for id, not want hibernate to generate value
	@Id
	@NotNull
	@Size(max = 16)
	private String ip;

	/**
	 */
	@NotNull
	@Enumerated(EnumType.STRING)
	private ExternalIpStatusEnum status;

	/**
	 */
	@Enumerated(EnumType.STRING)
	private ExternalIpDeviceOwnerEnum deviceOwner;

	/**
	 */
	@Size(max = 64)
	private String deviceId;

	/**
	 */
	@Size(max = 64)
	private String domainId;

	/**
	 * get external ip count.
	 * 
	 * @param ip
	 *            - ip
	 * @param status
	 *            - status
	 * @return external ip count
	 * @author xiangqian
	 */
	public static long countExternalIps(String ip, String status) {
		String jpaString = "SELECT COUNT(e.ip) FROM ExternalIp e WHERE 1 = 1 ";

		if ((null != ip) && (ip.isEmpty() == false)) {
			jpaString += "AND e.ip like :ip ";
		}

		if ((null != status) && (status.isEmpty() == false)) {
			jpaString += "AND e.status = :status ";
		}

		TypedQuery<Long> jpaQuery = entityManager().createQuery(jpaString, Long.class);
		// parameter type must be the same to the entity field type
		if ((null != ip) && (ip.isEmpty() == false)) {
			jpaQuery.setParameter("ip", "%" + ip + "%");
		}

		if ((null != status) && (status.isEmpty() == false)) {
			jpaQuery.setParameter("status", ExternalIpStatusEnum.valueOf(status));
		}

		long count = jpaQuery.getSingleResult();
		return count;
	}

	/**
	 * get external ip list in specified order.
	 * 
	 * @param ip
	 *            - ip
	 * @param status
	 *            - status
	 * @param firstResult
	 *            - start position of the first result
	 * @param maxResults
	 *            - maximum number of results
	 * @param sortFieldName
	 *            - sort by field
	 * @param sortOrder
	 *            - sort order
	 * @return external ip list
	 * @author xiangqian
	 */
	public static List<ExternalIp> getExternalIpEntries(String ip, String status, int firstResult, int maxResults,
			String sortFieldName, String sortOrder) {
		String jpaString = "SELECT e FROM ExternalIp e WHERE 1 = 1 ";

		if ((null != ip) && (ip.isEmpty() == false)) {
			jpaString += "AND e.ip like :ip ";
		}

		if ((null != status) && (status.isEmpty() == false)) {
			jpaString += "AND e.status = :status ";
		}

		if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
			jpaString = jpaString + " ORDER BY " + sortFieldName;
			if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
				jpaString = jpaString + " " + sortOrder;
			}
		}

		TypedQuery<ExternalIp> jpaQuery = entityManager().createQuery(jpaString, ExternalIp.class);
		// parameter type must be the same to the entity field type
		if ((null != ip) && (ip.isEmpty() == false)) {
			jpaQuery.setParameter("ip", "%" + ip + "%");
		}

		if ((null != status) && (status.isEmpty() == false)) {
			jpaQuery.setParameter("status", ExternalIpStatusEnum.valueOf(status));
		}

		List<ExternalIp> externalIps = jpaQuery.setFirstResult(firstResult).setMaxResults(maxResults).getResultList();

		return externalIps;
	}

	/**
	 * get external ip list in ip ascending order by status.
	 * 
	 * @param status
	 *            - status
	 * @return external ip list
	 * @author xiangqian
	 */
	public static List<ExternalIp> getExternalIps(ExternalIpStatusEnum status) {
		String jpaString = "SELECT e FROM ExternalIp e WHERE e.status = '" + status.name() + "' ORDER BY e.ip ASC";
		TypedQuery<ExternalIp> jpaQuery = entityManager().createQuery(jpaString, ExternalIp.class);
		List<ExternalIp> externalIps = jpaQuery.getResultList();		
		return externalIps;
	}
}
