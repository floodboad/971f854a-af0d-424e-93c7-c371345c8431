package com.sinoparasoft.model;

import java.util.Date;
import java.util.List;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

import com.sinoparasoft.enumerator.MonitorNameEnum;
import com.sinoparasoft.enumerator.MonitorSourceEnum;
import com.sinoparasoft.enumerator.MonitorStatusEnum;
import com.sinoparasoft.enumerator.MonitorTypeEnum;

@RooJavaBean
@RooToString
@RooJpaActiveRecord
public class MonitorResult {

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
	@Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat(style = "M-")
	private Date updateTime;

	/**
	 * get monitor results.
	 * 
	 * @param sourceId
	 *            - machine id
	 * @param monitorType
	 *            - monitor type
	 * @return monitor result list
	 * @author xiangqian
	 */
	public static List<MonitorResult> getMonitorResults(String sourceId, MonitorTypeEnum monitorType) {
		String jpaString = "SELECT m FROM MonitorResult m WHERE m.sourceId = '" + sourceId + "' AND m.monitorType = '"
				+ monitorType.name() + "'";
		TypedQuery<MonitorResult> jpaQuery = entityManager().createQuery(jpaString, MonitorResult.class);
		List<MonitorResult> monitorResults = jpaQuery.getResultList();
		return monitorResults;
	}

	/**
	 * get monitor result.
	 * 
	 * @param sourceId
	 *            - machine id
	 * @param monitorType
	 *            - monitor type
	 * @param monitorName
	 *            - monitor name
	 * @return monitor result, or null if the monitor result is not found
	 * @author xiangqian
	 */
	public static MonitorResult getMonitorResult(String sourceId, MonitorTypeEnum monitorType,
			MonitorNameEnum monitorName) {
		String jpaString = "SELECT m FROM MonitorResult m WHERE m.sourceId = '" + sourceId + "' AND m.monitorType = '"
				+ monitorType.name() + "' AND m.monitorName = '" + monitorName.name() + "'";
		TypedQuery<MonitorResult> jpaQuery = entityManager().createQuery(jpaString, MonitorResult.class);
		List<MonitorResult> monitorResults = jpaQuery.setFirstResult(0).setMaxResults(1).getResultList();
		if (1 == monitorResults.size()) {
			return monitorResults.get(0);
		} else {
			return null;
		}
	}
}
