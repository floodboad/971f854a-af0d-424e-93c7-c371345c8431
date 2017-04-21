// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package com.sinosoft.model;

import com.sinosoft.model.PhysicalMachineType;
import com.sinosoft.model.PhysicalMachineTypeDataOnDemand;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import org.springframework.stereotype.Component;

privileged aspect PhysicalMachineTypeDataOnDemand_Roo_DataOnDemand {
    
    declare @type: PhysicalMachineTypeDataOnDemand: @Component;
    
    private Random PhysicalMachineTypeDataOnDemand.rnd = new SecureRandom();
    
    private List<PhysicalMachineType> PhysicalMachineTypeDataOnDemand.data;
    
    public PhysicalMachineType PhysicalMachineTypeDataOnDemand.getNewTransientPhysicalMachineType(int index) {
        PhysicalMachineType obj = new PhysicalMachineType();
        setDescription(obj, index);
        setTypeName(obj, index);
        return obj;
    }
    
    public void PhysicalMachineTypeDataOnDemand.setDescription(PhysicalMachineType obj, int index) {
        String description = "description_" + index;
        obj.setDescription(description);
    }
    
    public void PhysicalMachineTypeDataOnDemand.setTypeName(PhysicalMachineType obj, int index) {
        String typeName = "typeName_" + index;
        obj.setTypeName(typeName);
    }
    
    public PhysicalMachineType PhysicalMachineTypeDataOnDemand.getSpecificPhysicalMachineType(int index) {
        init();
        if (index < 0) {
            index = 0;
        }
        if (index > (data.size() - 1)) {
            index = data.size() - 1;
        }
        PhysicalMachineType obj = data.get(index);
        String id = obj.getTypeId();
        return PhysicalMachineType.findPhysicalMachineType(id);
    }
    
    public PhysicalMachineType PhysicalMachineTypeDataOnDemand.getRandomPhysicalMachineType() {
        init();
        PhysicalMachineType obj = data.get(rnd.nextInt(data.size()));
        String id = obj.getTypeId();
        return PhysicalMachineType.findPhysicalMachineType(id);
    }
    
    public boolean PhysicalMachineTypeDataOnDemand.modifyPhysicalMachineType(PhysicalMachineType obj) {
        return false;
    }
    
    public void PhysicalMachineTypeDataOnDemand.init() {
        int from = 0;
        int to = 10;
        data = PhysicalMachineType.findPhysicalMachineTypeEntries(from, to);
        if (data == null) {
            throw new IllegalStateException("Find entries implementation for 'PhysicalMachineType' illegally returned null");
        }
        if (!data.isEmpty()) {
            return;
        }
        
        data = new ArrayList<PhysicalMachineType>();
        for (int i = 0; i < 10; i++) {
            PhysicalMachineType obj = getNewTransientPhysicalMachineType(i);
            try {
                obj.persist();
            } catch (final ConstraintViolationException e) {
                final StringBuilder msg = new StringBuilder();
                for (Iterator<ConstraintViolation<?>> iter = e.getConstraintViolations().iterator(); iter.hasNext();) {
                    final ConstraintViolation<?> cv = iter.next();
                    msg.append("[").append(cv.getRootBean().getClass().getName()).append(".").append(cv.getPropertyPath()).append(": ").append(cv.getMessage()).append(" (invalid value = ").append(cv.getInvalidValue()).append(")").append("]");
                }
                throw new IllegalStateException(msg.toString(), e);
            }
            obj.flush();
            data.add(obj);
        }
    }
    
}