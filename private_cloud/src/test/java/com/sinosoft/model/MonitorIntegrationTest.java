package com.sinosoft.model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.roo.addon.test.RooIntegrationTest;
import org.springframework.transaction.annotation.Transactional;

import com.sinosoft.enumerator.AlarmSeverityEnum;
import com.sinosoft.enumerator.MonitorNameEnum;
import com.sinosoft.enumerator.MonitorSourceEnum;
import com.sinosoft.model.Monitor;

@RooIntegrationTest(entity = Monitor.class)
public class MonitorIntegrationTest {
	@Before
	public void before() {
		System.out.println("@Before");
	}
	
    @Test
    public void testMarkerMethod() {
    }
    
    @Test
    @Transactional
    public void testAddAlarm() {
    	System.out.println("@Test");
    	
    	Monitor newMonitor = new Monitor();
    	newMonitor.setDescription("虚拟机的CPU负载");
    	newMonitor.setMonitorName(MonitorNameEnum.VM_CPU_UTIL);
    	newMonitor.setDefaultThreshold(0.8f);
    	newMonitor.setSeverityLevel(AlarmSeverityEnum.NORMAL);
    	newMonitor.setMonitorSource(MonitorSourceEnum.VIRTUAL_MACHINE);    	
    	newMonitor.persist();
    	newMonitor.flush();
    	newMonitor.clear();
    	
    	System.out.println(newMonitor.getId());
    	
    	Monitor persistAlarm = Monitor.findMonitor(newMonitor.getId());
    	Assert.assertNotNull(persistAlarm);
    	Assert.assertEquals(persistAlarm.getDescription(), newMonitor.getDescription());
    	Assert.assertEquals(persistAlarm.getMonitorName(), newMonitor.getMonitorName());
    	Assert.assertEquals(persistAlarm.getDefaultThreshold(), newMonitor.getDefaultThreshold());
    	Assert.assertEquals(persistAlarm.getSeverityLevel(), newMonitor.getSeverityLevel());
    	Assert.assertEquals(persistAlarm.getSeverityLevel(), newMonitor.getSeverityLevel());
    }
}
