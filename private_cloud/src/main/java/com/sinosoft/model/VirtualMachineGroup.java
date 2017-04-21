package com.sinosoft.model;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.sinosoft.enumerator.VirtualMachineGroupStatusEnum;
import com.sinosoft.util.CustomDateSerializer;

@RooJavaBean
@RooToString
// @RooJpaActiveRecord
// table name cannot be "group", mariadb refuse to create it
@RooJpaActiveRecord(identifierField = "groupId", identifierType = String.class)
public class VirtualMachineGroup {

	/**
	 */
	@Id
	@GenericGenerator(name = "uuidGenerator", strategy = "uuid2")
	@GeneratedValue(generator = "uuidGenerator")
	@Column(name = "groupId")
	private String groupId;

	/**
	 */
	private String groupName;

	/**
	 */
	private String description;

	/**
	 */
	private String domainId;

	/**
	 */
	private String creator;

	/**
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat(style = "M-")
	@JsonSerialize(using = CustomDateSerializer.class)
	private Date createTime;

	/**
	 */
	private Integer cpu;

	/**
	 */
	private Integer memory;

	/**
	 */
	private Integer disk;

	/**
	 */
	private Integer instances;

	/**
	 */
	@Enumerated(EnumType.STRING)
	private VirtualMachineGroupStatusEnum status;

	/**
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat(style = "M-")
	@JsonSerialize(using = CustomDateSerializer.class)
	private Date deleteTime;

	/**
	 * get group list in create time descending order. the groups belongs to the given domain, and are not been deleted.
	 * 
	 * @param domainId
	 *            - domain id
	 * @return group list
	 * @author xiangqian
	 */
	public static List<VirtualMachineGroup> getGroupsByDomainId(String domainId) {
		return entityManager()
				.createQuery("SELECT o FROM VirtualMachineGroup o WHERE o.domainId = '" + domainId
						+ "' AND o.status <> 'DELETED' ORDER BY o.createTime DESC", VirtualMachineGroup.class)
				.getResultList();
	}

	/**
	 * get group by id, the group is not been deleted.
	 * 
	 * @param groupId
	 *            - group id
	 * @return group with the specified id, or null if the number of qualified domain not equals to 1
	 * @author xiangqian
	 */
	public static VirtualMachineGroup getGroup(String groupId) {
		TypedQuery<VirtualMachineGroup> query = entityManager().createQuery(
				"SELECT o FROM VirtualMachineGroup o WHERE o.groupId = '" + groupId + "' AND o.status <> 'DELETED'",
				VirtualMachineGroup.class);
		List<VirtualMachineGroup> groupList = query.getResultList();
		if (1 == groupList.size()) {
			return groupList.get(0);
		} else {
			return null;
		}
	}

	/**
	 * get virtual machine group, the virtual machine group is not been deleted.
	 * 
	 * @param domainId
	 *            - domain id
	 * @param groupName
	 *            - group name
	 * @return virtual machine group, or null if the number of qualified virtual machine group not equals to 1.
	 */
	public static VirtualMachineGroup getGroup(String domainId, String groupName) {
		String jpaString = "SELECT o FROM VirtualMachineGroup o WHERE o.domainId = '" + domainId
				+ "' AND o.groupName = '" + groupName + "' AND o.status <> 'DELETED'";
		TypedQuery<VirtualMachineGroup> jpaQuery = entityManager().createQuery(jpaString, VirtualMachineGroup.class);
		List<VirtualMachineGroup> groupList = jpaQuery.getResultList();
		if (1 == groupList.size()) {
			return groupList.get(0);
		} else {
			return null;
		}
	}

	/**
	 * get resource allocated from a specified domain
	 * 
	 * @param domainId
	 *            - domain id
	 * @return allocated resource from domain
	 * @author xiangqian
	 */
	public static Map<String, Long> getDomainAllocatedResource(String domainId) {
		Map<String, Long> map = new HashMap<String, Long>();

		Object[] objs = (Object[]) entityManager()
				.createQuery(
						"SELECT SUM(g.cpu), SUM(g.memory), SUM(g.disk), SUM(g.instances) FROM VirtualMachineGroup g WHERE g.domainId = '"
								+ domainId + "' AND g.status <> '" + VirtualMachineGroupStatusEnum.DELETED.name() + "'")
				.getSingleResult();

		Long[] allocatedResource = Arrays.copyOf(objs, objs.length, Long[].class);
		long allocatedCpu = (null != allocatedResource[0]) ? allocatedResource[0] : 0;
		long allocatedMemory = (null != allocatedResource[1]) ? allocatedResource[1] : 0;
		long allocatedDisk = (null != allocatedResource[2]) ? allocatedResource[2] : 0;
		map.put("cpu", allocatedCpu);
		map.put("memory", allocatedMemory);
		map.put("disk", allocatedDisk);

		return map;
	}
}
