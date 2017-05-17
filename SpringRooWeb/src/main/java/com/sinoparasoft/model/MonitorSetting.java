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

import com.sinoparasoft.enumerator.AlarmSeverityEnum;
import com.sinoparasoft.enumerator.MonitorNameEnum;
import com.sinoparasoft.enumerator.MonitorSourceEnum;
import com.sinoparasoft.enumerator.MonitorTypeEnum;

@RooJavaBean
@RooToString
@RooJpaActiveRecord
public class MonitorSetting {

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
	private Float threshold;

	/**
     */
	private String thresholdUnit;

	/**
     */
	@Enumerated(EnumType.STRING)
	private AlarmSeverityEnum severityLevel;

	/**
     */
	private String osAlarmId;

	/**
     */
	private Boolean enabled;

	/**
     */
	@Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat(style = "M-")
	private Date createTime;

	/**
	 * get monitor settings in create time descending order.
	 * 
	 * @param sourceId
	 *            - source id
	 * @return monitor setting list
	 * @author xiangqian
	 */
	public static List<MonitorSetting> getMonitorSettings(String sourceId, MonitorTypeEnum monitorType) {
		String jpaString = "SELECT m FROM MonitorSetting m WHERE m.sourceId = '" + sourceId + "' AND m.monitorType = '"
				+ monitorType.name() + "' ORDER BY m.createTime DESC";
		TypedQuery<MonitorSetting> jpaQuery = entityManager().createQuery(jpaString, MonitorSetting.class);
		List<MonitorSetting> monitorSettings = jpaQuery.getResultList();
		return monitorSettings;
	}

	/**
	 * get monitor setting.
	 * 
	 * @param sourceId
	 *            - source id
	 * @param monitorName
	 *            - monitor name
	 * @return monitor setting, or null if the number of qualified monitor setting not equals to 1.
	 * @author xiangqian
	 */
	public static MonitorSetting getMonitorSetting(String sourceId, MonitorNameEnum monitorName) {
		String jpaString = "SELECT m FROM MonitorSetting m WHERE m.sourceId = '" + sourceId + "' AND m.monitorName = '"
				+ monitorName.name() + "'";
		TypedQuery<MonitorSetting> jpaQuery = entityManager().createQuery(jpaString, MonitorSetting.class);
		List<MonitorSetting> monitorSettings = jpaQuery.setFirstResult(0).setMaxResults(1).getResultList();
		if (1 == monitorSettings.size()) {
			return monitorSettings.get(0);
		} else {
			return null;
		}
	}
}
