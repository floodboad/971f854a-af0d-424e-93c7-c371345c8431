package com.sinosoft.model;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaActiveRecord(identifierField = "id", identifierType = String.class)
public class ApplicationTag {

	/**
     */
	@Id
	@GenericGenerator(name = "uuidGenerator", strategy = "uuid2")
	@GeneratedValue(generator = "uuidGenerator")
	@Column(name = "id")
	private String id;

	/**
     */
	private String name;

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
	private Boolean enabled;

	/**
     */
	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "applicationTags")
	private Set<VirtualMachine> virtualMachines = new HashSet<VirtualMachine>();

	/**
	 * get application tag count, the application tag is enabled.
	 * 
	 * @return application tag count
	 * @author xiangqian
	 */
	public static long countApplicationTags() {
		String jpaString = "SELECT COUNT(a) FROM ApplicationTag a WHERE a.enabled = true";
		TypedQuery<Long> jpaQuery = entityManager().createQuery(jpaString, Long.class);
		Long count = jpaQuery.getSingleResult();

		return count;
	}

	/**
	 * get application tag list in specified order, the application tag is enabled.
	 * 
	 * @param firstResult
	 *            - start position of the first result
	 * @param maxResults
	 *            - maximum number of results
	 * @param sortFieldName
	 *            - sort by field
	 * @param sortOrder
	 *            - sort order
	 * @author xiangqian
	 */
	public static List<ApplicationTag> getApplicationTagEntries(int firstResult, int maxResults, String sortFieldName,
			String sortOrder) {
		String jpaString = "SELECT a FROM ApplicationTag a WHERE a.enabled = true ";

		if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
			jpaString = jpaString + " ORDER BY " + sortFieldName;
			if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
				jpaString = jpaString + " " + sortOrder;
			}
		}

		TypedQuery<ApplicationTag> jpaQuery = entityManager().createQuery(jpaString, ApplicationTag.class);
		List<ApplicationTag> applicationTags = jpaQuery.setFirstResult(firstResult).setMaxResults(maxResults)
				.getResultList();

		return applicationTags;
	}

	/**
	 * get application tag by name, the application tag is enabled.
	 * 
	 * @param name
	 *            - application tag name
	 * @return application tag with the specified name, or null if the number of qualified application tag not equals to
	 *         1.
	 * @author xiangqian
	 */
	public static ApplicationTag getApplicationTagByName(String name) {
		String jpaString = "SELECT a FROM ApplicationTag a WHERE a.enabled = true AND a.name = '" + name + "'";
		TypedQuery<ApplicationTag> jpaQuery = entityManager().createQuery(jpaString, ApplicationTag.class);
		List<ApplicationTag> applicationTags = jpaQuery.setFirstResult(0).setMaxResults(1).getResultList();
		if (1 == applicationTags.size()) {
			return applicationTags.get(0);
		} else {
			return null;
		}
	}

	/**
	 * get application tag by id. the application tag is enabled.
	 * 
	 * @param id
	 *            - application tag id
	 * @return application tag with the specified id, or null if the given parameter is bad or the number of qualified
	 *         application tag not equals to 1.
	 * @author xiangqian
	 */
	public static ApplicationTag getApplicationTag(String id) {
		if (id == null || id.length() == 0) {
			return null;
		}

		String jpaString = "SELECT a FROM ApplicationTag a WHERE a.enabled = true AND a.id = '" + id + "'";
		TypedQuery<ApplicationTag> jpaQuery = entityManager().createQuery(jpaString, ApplicationTag.class);
		List<ApplicationTag> applicationTags = jpaQuery.getResultList();
		if (1 == applicationTags.size()) {
			return applicationTags.get(0);
		} else {

			return null;
		}
	}

	/**
	 * get application tag list in create time descending order. the application tag is enabled.
	 * 
	 * @return application tag list
	 * @author xiangqian
	 */
	public static List<ApplicationTag> getApplicationTags() {
		String jpaString = "SELECT a FROM ApplicationTag a WHERE a.enabled = true ORDER BY a.createTime DESC";
		TypedQuery<ApplicationTag> jpaQuery = entityManager().createQuery(jpaString, ApplicationTag.class);
		List<ApplicationTag> applicationTags = jpaQuery.getResultList();
		return applicationTags;
	}
}
