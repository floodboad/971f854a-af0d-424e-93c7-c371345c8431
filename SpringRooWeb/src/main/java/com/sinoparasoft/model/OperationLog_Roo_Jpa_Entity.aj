// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package com.sinoparasoft.model;

import com.sinoparasoft.model.OperationLog;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Version;

privileged aspect OperationLog_Roo_Jpa_Entity {
    
    declare @type: OperationLog: @Entity;
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long OperationLog.id;
    
    @Version
    @Column(name = "version")
    private Integer OperationLog.version;
    
    public Long OperationLog.getId() {
        return this.id;
    }
    
    public void OperationLog.setId(Long id) {
        this.id = id;
    }
    
    public Integer OperationLog.getVersion() {
        return this.version;
    }
    
    public void OperationLog.setVersion(Integer version) {
        this.version = version;
    }
    
}
