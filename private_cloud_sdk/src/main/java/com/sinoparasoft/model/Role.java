package com.sinoparasoft.model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.TypedQuery;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

import com.sinosoft.enumerator.RoleNameEnum;

@RooJavaBean
@RooToString
@RooJpaActiveRecord
public class Role {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "roleId")
	private Long roleId;

	/**
     */
	@Enumerated(EnumType.STRING)
	private RoleNameEnum roleName;

	/**
     */
	private String description;

	/**
     */
	// this will cause error when return User object from controller: Unexpected end of JSON input(…)
	// @OneToMany(cascade = CascadeType.ALL, mappedBy = "role")
	// private Set<User> users = new HashSet<User>();

	/**
	 * get role by name.
	 * 
	 * @param roleName
	 *            - role name
	 * @return role with the given name, or null if the number of qualified log not equals to 1.
	 * @author xiangqian
	 */
	public static Role getRole(String roleName) {
		String jpaString = "SELECT r FROM Role r WHERE r.roleName = '" + roleName + "'";
		TypedQuery<Role> jpaQuery = entityManager().createQuery(jpaString, Role.class);
		List<Role> roles = jpaQuery.getResultList();
		if (roles.size() == 1) {
			return roles.get(0);
		} else {
			return null;
		}
	}
}
