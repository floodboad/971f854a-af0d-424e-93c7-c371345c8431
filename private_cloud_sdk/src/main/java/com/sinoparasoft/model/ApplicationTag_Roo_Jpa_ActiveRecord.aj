// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package com.sinoparasoft.model;

import com.sinoparasoft.model.ApplicationTag;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;

privileged aspect ApplicationTag_Roo_Jpa_ActiveRecord {
    
    @PersistenceContext
    transient EntityManager ApplicationTag.entityManager;
    
    public static final List<String> ApplicationTag.fieldNames4OrderClauseFilter = java.util.Arrays.asList("id", "name", "description", "creator", "createTime", "enabled", "virtualMachines");
    
    public static final EntityManager ApplicationTag.entityManager() {
        EntityManager em = new ApplicationTag().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }
    
    public static List<ApplicationTag> ApplicationTag.findAllApplicationTags() {
        return entityManager().createQuery("SELECT o FROM ApplicationTag o", ApplicationTag.class).getResultList();
    }
    
    public static List<ApplicationTag> ApplicationTag.findAllApplicationTags(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM ApplicationTag o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, ApplicationTag.class).getResultList();
    }
    
    public static ApplicationTag ApplicationTag.findApplicationTag(String id) {
        if (id == null || id.length() == 0) return null;
        return entityManager().find(ApplicationTag.class, id);
    }
    
    public static List<ApplicationTag> ApplicationTag.findApplicationTagEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM ApplicationTag o", ApplicationTag.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
    
    public static List<ApplicationTag> ApplicationTag.findApplicationTagEntries(int firstResult, int maxResults, String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM ApplicationTag o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, ApplicationTag.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
    
    @Transactional
    public void ApplicationTag.persist() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.persist(this);
    }
    
    @Transactional
    public void ApplicationTag.remove() {
        if (this.entityManager == null) this.entityManager = entityManager();
        if (this.entityManager.contains(this)) {
            this.entityManager.remove(this);
        } else {
            ApplicationTag attached = ApplicationTag.findApplicationTag(this.id);
            this.entityManager.remove(attached);
        }
    }
    
    @Transactional
    public void ApplicationTag.flush() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.flush();
    }
    
    @Transactional
    public void ApplicationTag.clear() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.clear();
    }
    
    @Transactional
    public ApplicationTag ApplicationTag.merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        ApplicationTag merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }
    
}
