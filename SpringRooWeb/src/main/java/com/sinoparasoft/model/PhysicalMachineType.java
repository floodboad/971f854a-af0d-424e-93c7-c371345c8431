package com.sinoparasoft.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

import com.sinoparasoft.enumerator.PhysicalMachineTypeNameEnum;

@RooJavaBean
@RooToString
@RooJpaActiveRecord(identifierField = "typeId", identifierType = String.class)
public class PhysicalMachineType {

	@Id
	@GenericGenerator(name = "uuidGenerator", strategy = "uuid2")
	@GeneratedValue(generator = "uuidGenerator")
	@Column(name = "typeId")
	private String typeId;

	/**
     */
	private String typeName;

	/**
     */
	private String description;

	/**
     */
	@ManyToMany(cascade = CascadeType.ALL, mappedBy = "types")
	private Set<PhysicalMachine> physicalMachines = new HashSet<PhysicalMachine>();

	/**
	 * get physical machine type by name.
	 * 
	 * @param typeName
	 *            - type name
	 * @return physical machine type with the given name, or null if the number of qualified type not equals to 1.
	 * @author xiangqian
	 */
	public static PhysicalMachineType getPhysicalMachineType(PhysicalMachineTypeNameEnum typeName) {
		String jpaQuery = "SELECT t FROM PhysicalMachineType t WHERE t.typeName = '" + typeName.name() + "'";
		List<PhysicalMachineType> list = entityManager().createQuery(jpaQuery, PhysicalMachineType.class)
				.getResultList();
		if (list.size() == 1) {
			return list.get(0);
		} else {
			return null;
		}
	}
}
