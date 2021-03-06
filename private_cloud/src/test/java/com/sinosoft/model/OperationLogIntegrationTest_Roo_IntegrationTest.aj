// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package com.sinosoft.model;

import com.sinosoft.model.OperationLog;
import com.sinosoft.model.OperationLogDataOnDemand;
import com.sinosoft.model.OperationLogIntegrationTest;
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

privileged aspect OperationLogIntegrationTest_Roo_IntegrationTest {
    
    declare @type: OperationLogIntegrationTest: @RunWith(SpringJUnit4ClassRunner.class);
    
    declare @type: OperationLogIntegrationTest: @ContextConfiguration(locations = "classpath*:/META-INF/spring/applicationContext*.xml");
    
    declare @type: OperationLogIntegrationTest: @Transactional;
    
    @Autowired
    OperationLogDataOnDemand OperationLogIntegrationTest.dod;
    
    @Test
    public void OperationLogIntegrationTest.testCountOperationLogs() {
        Assert.assertNotNull("Data on demand for 'OperationLog' failed to initialize correctly", dod.getRandomOperationLog());
        long count = OperationLog.countOperationLogs();
        Assert.assertTrue("Counter for 'OperationLog' incorrectly reported there were no entries", count > 0);
    }
    
    @Test
    public void OperationLogIntegrationTest.testFindOperationLog() {
        OperationLog obj = dod.getRandomOperationLog();
        Assert.assertNotNull("Data on demand for 'OperationLog' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'OperationLog' failed to provide an identifier", id);
        obj = OperationLog.findOperationLog(id);
        Assert.assertNotNull("Find method for 'OperationLog' illegally returned null for id '" + id + "'", obj);
        Assert.assertEquals("Find method for 'OperationLog' returned the incorrect identifier", id, obj.getId());
    }
    
    @Test
    public void OperationLogIntegrationTest.testFindAllOperationLogs() {
        Assert.assertNotNull("Data on demand for 'OperationLog' failed to initialize correctly", dod.getRandomOperationLog());
        long count = OperationLog.countOperationLogs();
        Assert.assertTrue("Too expensive to perform a find all test for 'OperationLog', as there are " + count + " entries; set the findAllMaximum to exceed this value or set findAll=false on the integration test annotation to disable the test", count < 250);
        List<OperationLog> result = OperationLog.findAllOperationLogs();
        Assert.assertNotNull("Find all method for 'OperationLog' illegally returned null", result);
        Assert.assertTrue("Find all method for 'OperationLog' failed to return any data", result.size() > 0);
    }
    
    @Test
    public void OperationLogIntegrationTest.testFindOperationLogEntries() {
        Assert.assertNotNull("Data on demand for 'OperationLog' failed to initialize correctly", dod.getRandomOperationLog());
        long count = OperationLog.countOperationLogs();
        if (count > 20) count = 20;
        int firstResult = 0;
        int maxResults = (int) count;
        List<OperationLog> result = OperationLog.findOperationLogEntries(firstResult, maxResults);
        Assert.assertNotNull("Find entries method for 'OperationLog' illegally returned null", result);
        Assert.assertEquals("Find entries method for 'OperationLog' returned an incorrect number of entries", count, result.size());
    }
    
    @Test
    public void OperationLogIntegrationTest.testFlush() {
        OperationLog obj = dod.getRandomOperationLog();
        Assert.assertNotNull("Data on demand for 'OperationLog' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'OperationLog' failed to provide an identifier", id);
        obj = OperationLog.findOperationLog(id);
        Assert.assertNotNull("Find method for 'OperationLog' illegally returned null for id '" + id + "'", obj);
        boolean modified =  dod.modifyOperationLog(obj);
        Integer currentVersion = obj.getVersion();
        obj.flush();
        Assert.assertTrue("Version for 'OperationLog' failed to increment on flush directive", (currentVersion != null && obj.getVersion() > currentVersion) || !modified);
    }
    
    @Test
    public void OperationLogIntegrationTest.testMergeUpdate() {
        OperationLog obj = dod.getRandomOperationLog();
        Assert.assertNotNull("Data on demand for 'OperationLog' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'OperationLog' failed to provide an identifier", id);
        obj = OperationLog.findOperationLog(id);
        boolean modified =  dod.modifyOperationLog(obj);
        Integer currentVersion = obj.getVersion();
        OperationLog merged = obj.merge();
        obj.flush();
        Assert.assertEquals("Identifier of merged object not the same as identifier of original object", merged.getId(), id);
        Assert.assertTrue("Version for 'OperationLog' failed to increment on merge and flush directive", (currentVersion != null && obj.getVersion() > currentVersion) || !modified);
    }
    
    @Test
    public void OperationLogIntegrationTest.testPersist() {
        Assert.assertNotNull("Data on demand for 'OperationLog' failed to initialize correctly", dod.getRandomOperationLog());
        OperationLog obj = dod.getNewTransientOperationLog(Integer.MAX_VALUE);
        Assert.assertNotNull("Data on demand for 'OperationLog' failed to provide a new transient entity", obj);
        Assert.assertNull("Expected 'OperationLog' identifier to be null", obj.getId());
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
        Assert.assertNotNull("Expected 'OperationLog' identifier to no longer be null", obj.getId());
    }
    
    @Test
    public void OperationLogIntegrationTest.testRemove() {
        OperationLog obj = dod.getRandomOperationLog();
        Assert.assertNotNull("Data on demand for 'OperationLog' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'OperationLog' failed to provide an identifier", id);
        obj = OperationLog.findOperationLog(id);
        obj.remove();
        obj.flush();
        Assert.assertNull("Failed to remove 'OperationLog' with identifier '" + id + "'", OperationLog.findOperationLog(id));
    }
    
}
