// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package com.sinosoft.model;

import com.sinosoft.model.PhysicalMachine;
import com.sinosoft.model.PhysicalMachineType;
import java.util.Set;

privileged aspect PhysicalMachineType_Roo_JavaBean {
    
    public String PhysicalMachineType.getTypeId() {
        return this.typeId;
    }
    
    public void PhysicalMachineType.setTypeId(String typeId) {
        this.typeId = typeId;
    }
    
    public String PhysicalMachineType.getTypeName() {
        return this.typeName;
    }
    
    public void PhysicalMachineType.setTypeName(String typeName) {
        this.typeName = typeName;
    }
    
    public String PhysicalMachineType.getDescription() {
        return this.description;
    }
    
    public void PhysicalMachineType.setDescription(String description) {
        this.description = description;
    }
    
    public Set<PhysicalMachine> PhysicalMachineType.getPhysicalMachines() {
        return this.physicalMachines;
    }
    
    public void PhysicalMachineType.setPhysicalMachines(Set<PhysicalMachine> physicalMachines) {
        this.physicalMachines = physicalMachines;
    }
    
}