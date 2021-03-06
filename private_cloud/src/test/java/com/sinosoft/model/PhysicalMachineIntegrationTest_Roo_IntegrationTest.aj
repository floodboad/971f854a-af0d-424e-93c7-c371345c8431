// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package com.sinosoft.model;

import com.sinosoft.model.PhysicalMachine;
import com.sinosoft.model.PhysicalMachineDataOnDemand;
import com.sinosoft.model.PhysicalMachineIntegrationTest;
import java.util.Iterator;
import java.util.List;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

privileged aspect PhysicalMachineIntegrationTest_Roo_IntegrationTest {
    
    declare @type: PhysicalMachineIntegrationTest: @RunWith(SpringJUnit4ClassRunner.class);
    
    declare @type: PhysicalMachineIntegrationTest: @ContextConfiguration(locations = "classpath*:/META-INF/spring/applicationContext*.xml");
    
    declare @type: PhysicalMachineIntegrationTest: @Transactional;
    
    @Autowired
    PhysicalMachineDataOnDemand PhysicalMachineIntegrationTest.dod;
    
    @Test
    public void PhysicalMachineIntegrationTest.testCountPhysicalMachines() {
        Assert.assertNotNull("Data on demand for 'PhysicalMachine' failed to initialize correctly", dod.getRandomPhysicalMachine());
        long count = PhysicalMachine.countPhysicalMachines();
        Assert.assertTrue("Counter for 'PhysicalMachine' incorrectly reported there were no entries", count > 0);
    }
    
    @Test
    public void PhysicalMachineIntegrationTest.testFindPhysicalMachine() {
        PhysicalMachine obj = dod.getRandomPhysicalMachine();
        Assert.assertNotNull("Data on demand for 'PhysicalMachine' failed to initialize correctly", obj);
        String id = obj.getHostId();
        Assert.assertNotNull("Data on demand for 'PhysicalMachine' failed to provide an identifier", id);
        obj = PhysicalMachine.findPhysicalMachine(id);
        Assert.assertNotNull("Find method for 'PhysicalMachine' illegally returned null for id '" + id + "'", obj);
        Assert.assertEquals("Find method for 'PhysicalMachine' returned the incorrect identifier", id, obj.getHostId());
    }
    
    @Test
    public void PhysicalMachineIntegrationTest.testFindAllPhysicalMachines() {
        Assert.assertNotNull("Data on demand for 'PhysicalMachine' failed to initialize correctly", dod.getRandomPhysicalMachine());
        long count = PhysicalMachine.countPhysicalMachines();
        Assert.assertTrue("Too expensive to perform a find all test for 'PhysicalMachine', as there are " + count + " entries; set the findAllMaximum to exceed this value or set findAll=false on the integration test annotation to disable the test", count < 250);
        List<PhysicalMachine> result = PhysicalMachine.findAllPhysicalMachines();
        Assert.assertNotNull("Find all method for 'PhysicalMachine' illegally returned null", result);
        Assert.assertTrue("Find all method for 'PhysicalMachine' failed to return any data", result.size() > 0);
    }
    
    @Test
    public void PhysicalMachineIntegrationTest.testFindPhysicalMachineEntries() {
        Assert.assertNotNull("Data on demand for 'PhysicalMachine' failed to initialize correctly", dod.getRandomPhysicalMachine());
        long count = PhysicalMachine.countPhysicalMachines();
        if (count > 20) count = 20;
        int firstResult = 0;
        int maxResults = (int) count;
        List<PhysicalMachine> result = PhysicalMachine.findPhysicalMachineEntries(firstResult, maxResults);
        Assert.assertNotNull("Find entries method for 'PhysicalMachine' illegally returned null", result);
        Assert.assertEquals("Find entries method for 'PhysicalMachine' returned an incorrect number of entries", count, result.size());
    }
    
    @Test
    public void PhysicalMachineIntegrationTest.testFlush() {
        PhysicalMachine obj = dod.getRandomPhysicalMachine();
        Assert.assertNotNull("Data on demand for 'PhysicalMachine' failed to initialize correctly", obj);
        String id = obj.getHostId();
        Assert.assertNotNull("Data on demand for 'PhysicalMachine' failed to provide an identifier", id);
        obj = PhysicalMachine.findPhysicalMachine(id);
        Assert.assertNotNull("Find method for 'PhysicalMachine' illegally returned null for id '" + id + "'", obj);
        boolean modified =  dod.modifyPhysicalMachine(obj);
        Integer currentVersion = obj.getVersion();
        obj.flush();
        Assert.assertTrue("Version for 'PhysicalMachine' failed to increment on flush directive", (currentVersion != null && obj.getVersion() > currentVersion) || !modified);
    }
    
    @Test
    public void PhysicalMachineIntegrationTest.testMergeUpdate() {
        PhysicalMachine obj = dod.getRandomPhysicalMachine();
        Assert.assertNotNull("Data on demand for 'PhysicalMachine' failed to initialize correctly", obj);
        String id = obj.getHostId();
        Assert.assertNotNull("Data on demand for 'PhysicalMachine' failed to provide an identifier", id);
        obj = PhysicalMachine.findPhysicalMachine(id);
        boolean modified =  dod.modifyPhysicalMachine(obj);
        Integer currentVersion = obj.getVersion();
        PhysicalMachine merged = obj.merge();
        obj.flush();
        Assert.assertEquals("Identifier of merged object not the same as identifier of original object", merged.getHostId(), id);
        Assert.assertTrue("Version for 'PhysicalMachine' failed to increment on merge and flush directive", (currentVersion != null && obj.getVersion() > currentVersion) || !modified);
    }
    
    @Test
    public void PhysicalMachineIntegrationTest.testPersist() {
        Assert.assertNotNull("Data on demand for 'PhysicalMachine' failed to initialize correctly", dod.getRandomPhysicalMachine());
        PhysicalMachine obj = dod.getNewTransientPhysicalMachine(Integer.MAX_VALUE);
        Assert.assertNotNull("Data on demand for 'PhysicalMachine' failed to provide a new transient entity", obj);
        Assert.assertNull("Expected 'PhysicalMachine' identifier to be null", obj.getHostId());
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
        Assert.assertNotNull("Expected 'PhysicalMachine' identifier to no longer be null", obj.getHostId());
    }
    
    @Test
    public void PhysicalMachineIntegrationTest.testRemove() {
        PhysicalMachine obj = dod.getRandomPhysicalMachine();
        Assert.assertNotNull("Data on demand for 'PhysicalMachine' failed to initialize correctly", obj);
        String id = obj.getHostId();
        Assert.assertNotNull("Data on demand for 'PhysicalMachine' failed to provide an identifier", id);
        obj = PhysicalMachine.findPhysicalMachine(id);
        obj.remove();
        obj.flush();
        Assert.assertNull("Failed to remove 'PhysicalMachine' with identifier '" + id + "'", PhysicalMachine.findPhysicalMachine(id));
    }
    
}
