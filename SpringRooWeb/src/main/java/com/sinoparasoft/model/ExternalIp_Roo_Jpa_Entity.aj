// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package com.sinoparasoft.model;

import com.sinoparasoft.model.ExternalIp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Version;

privileged aspect ExternalIp_Roo_Jpa_Entity {
    
    declare @type: ExternalIp: @Entity;
    
    @Version
    @Column(name = "version")
    private Integer ExternalIp.version;
    
    public Integer ExternalIp.getVersion() {
        return this.version;
    }
    
    public void ExternalIp.setVersion(Integer version) {
        this.version = version;
    }
    
}
