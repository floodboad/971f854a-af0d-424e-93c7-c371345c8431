package com.sinoparasoft.model;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import javax.validation.constraints.NotNull;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.sinosoft.enumerator.VirtualMachineDomainStatusEnum;
import com.sinosoft.util.CustomDateSerializer;

@RooJavaBean
@RooToString
@RooJpaActiveRecord
public class VirtualMachineDomain {

	/**
     */
	@NotNull
	/*
	 * JsonProperty not working !
	 */
	// @JsonProperty("id")
	// custom value for id, not want hibernate to generate value
	@Id
	@Column(name = "domainId")
	private String domainId;

	@NotNull
	// @JsonProperty("name")
	private String domainName;

	private String description;

	@NotNull
	private String creator;

	@NotNull
	@Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat(style = "M-")
	@JsonSerialize(using = CustomDateSerializer.class)
	// @JsonProperty("create_time")
	private Date createTime;

	@NotNull
	private int cpu;

	@NotNull
	private int memory;

	@NotNull
	private int disk;

	private int instances;

	/**
     */
	@Enumerated(EnumType.STRING)
	private VirtualMachineDomainStatusEnum status;

	@Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat(style = "M-")
	@JsonSerialize(using = CustomDateSerializer.class)
	// @JsonProperty("delete_time")
	private Date deleteTime;

	/**
	 * get domain by id, the domain is not been deleted.
	 * 
	 * @param domainId
	 *            - domain id
	 * @return domain with the specified id, or null if the number of qualified domain not equals to 1
	 * @author xiangqian
	 */
	public static VirtualMachineDomain getDomainById(String domainId) {
		TypedQuery<VirtualMachineDomain> query = entityManager().createQuery(
				"SELECT o FROM VirtualMachineDomain o WHERE o.domainId='" + domainId + "' AND o.status <> 'DELETED'",
				VirtualMachineDomain.class);
		List<VirtualMachineDomain> domainList = query.getResultList();
		if (domainList.size() == 1) {
			return domainList.get(0);
		}
		return null;
	}

	/**
	 * get domain by name, the domain is not been deleted.
	 * 
	 * @param domainName
	 *            - domain name
	 * @return domain with the specified name, or null if the number of qualified domain not equals to 1
	 * @author xiangqian
	 */
	public static VirtualMachineDomain getDomainByName(String domainName) {
		TypedQuery<VirtualMachineDomain> query = entityManager().createQuery(
				"SELECT o FROM VirtualMachineDomain o WHERE o.domainName='" + domainName
						+ "' AND o.status <> 'DELETED'", VirtualMachineDomain.class);
		List<VirtualMachineDomain> domainList = query.getResultList();
		if (domainList.size() == 1) {
			return domainList.get(0);
		}
		return null;
	}

	/**
	 * get domain list in create time descending order. the domains are not been deleted.
	 * 
	 * @return domain list
	 * @author chaoyue, xiangqian
	 */
	public static List<VirtualMachineDomain> getDomains() {
		return entityManager().createQuery(
				"SELECT o FROM VirtualMachineDomain o WHERE o.status <> 'DELETED' ORDER BY o.createTime DESC",
				VirtualMachineDomain.class).getResultList();
	}
}
