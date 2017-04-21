package com.sinosoft.model;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

import com.sinosoft.enumerator.PhysicalMachineStatusEnum;

import javax.persistence.Enumerated;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.ManyToMany;

@RooJavaBean
@RooToString
@RooJpaActiveRecord(identifierField = "hostId", identifierType = String.class)
public class PhysicalMachine {

	@Id
	@GenericGenerator(name = "uuidGenerator", strategy = "uuid2")
	@GeneratedValue(generator = "uuidGenerator")
	@Column(name = "hostId")
	private String hostId;

	/**
     */
	private String hostName;

	/**
     */
	private String ipAddress;

	/**
     */
	@Enumerated(EnumType.STRING)
	private PhysicalMachineStatusEnum status;

	/**
     */
	private Integer cpuNumber;

	/**
     */
	private Float memorySize;

	/**
     */
	private Float diskSize;

	/**
     */
	private String description;

	/**
     */
	private Boolean verified;

	/**
     */
	private String hypervisorId;

	/**
     */
	@Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat(style = "M-")
	private Date createTime;

	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinTable(name = "physical_machine_physical_machine_type", joinColumns = @JoinColumn(name = "host_id"), inverseJoinColumns = @JoinColumn(name = "type_id"))
	private Set<PhysicalMachineType> types = new HashSet<PhysicalMachineType>();

	/**
	 * get physical machine by id.
	 * 
	 * @param hostId
	 *            - machine id
	 * @return physical machine, or null if the number of qualified physical machine not equals to 1.
	 * @author xiangqian
	 */
	public static PhysicalMachine getPhysicalMachineById(String hostId) {
		String jpaString = "SELECT m FROM PhysicalMachine m WHERE m.hostId = '" + hostId + "'";
		TypedQuery<PhysicalMachine> jpaQuery = entityManager().createQuery(jpaString, PhysicalMachine.class);
		List<PhysicalMachine> machines = jpaQuery.setFirstResult(0).setMaxResults(1).getResultList();
		if (1 == machines.size()) {
			return machines.get(0);
		} else {
			return null;
		}
	}

	/**
	 * get physical machine by name.
	 * 
	 * @param hostName
	 *            - machine name
	 * @return physical machine, or null if the number of qualified physical machine not equals to 1.
	 * @author xiangqian
	 */
	public static PhysicalMachine getPhysicalMachineByName(String hostName) {
		String jpaString = "SELECT m FROM PhysicalMachine m WHERE m.hostName = '" + hostName + "'";
		TypedQuery<PhysicalMachine> jpaQuery = entityManager().createQuery(jpaString, PhysicalMachine.class);
		List<PhysicalMachine> machines = jpaQuery.setFirstResult(0).setMaxResults(1).getResultList();
		if (1 == machines.size()) {
			return machines.get(0);
		} else {
			return null;
		}
	}
}
