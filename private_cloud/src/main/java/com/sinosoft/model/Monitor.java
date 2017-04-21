package com.sinosoft.model;

import java.util.List;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

import com.sinosoft.enumerator.AlarmSeverityEnum;
import com.sinosoft.enumerator.MonitorNameEnum;
import com.sinosoft.enumerator.MonitorSourceEnum;
import com.sinosoft.enumerator.MonitorTypeEnum;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.TypedQuery;

@RooJavaBean
@RooToString
@RooJpaActiveRecord
public class Monitor {

	/**
     */
	@Enumerated(EnumType.STRING)
	private MonitorSourceEnum monitorSource;

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
	private String description;

	/**
     */
	private Float defaultThreshold;

	/**
     */
	private String thresholdUnit;

	/**
     */
	@Enumerated(EnumType.STRING)
	private AlarmSeverityEnum severityLevel;

	/**
	 * get monitors.
	 * 
	 * @param monitorSource
	 *            - monitor source type
	 * @param monitorType
	 *            - monitor type
	 * @return motitor list
	 * @author xiangqian
	 */
	public static List<Monitor> getMonitors(MonitorSourceEnum monitorSource, MonitorTypeEnum monitorType) {
		String jpaString = "SELECT m FROM Monitor m WHERE m.monitorSource = '" + monitorSource.name()
				+ "' AND m.monitorType = '" + monitorType.name() + "'";
		TypedQuery<Monitor> jpaQuery = entityManager().createQuery(jpaString, Monitor.class);
		List<Monitor> monitors = jpaQuery.getResultList();
		return monitors;
	}

	/**
	 * get monitor.
	 * 
	 * @param monitorName
	 *            - monitor name
	 * @return monitor, or null if the number of qualified monitor not equals to 1.
	 * @author xiangqian
	 */
	public static Monitor getMonitor(MonitorNameEnum monitorName) {
		String jpaString = "SELECT m FROM Monitor m WHERE m.monitorName = '" + monitorName.name() + "'";
		TypedQuery<Monitor> jpaQuery = entityManager().createQuery(jpaString, Monitor.class);
		List<Monitor> monitors = jpaQuery.setFirstResult(0).setMaxResults(1).getResultList();
		if (1 == monitors.size()) {
			return monitors.get(0);
		} else {
			return null;
		}
	}
}
