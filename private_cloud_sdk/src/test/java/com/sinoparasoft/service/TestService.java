package com.sinoparasoft.service;

import org.junit.Test;
import org.openstack4j.model.compute.Server;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.sinoparasoft.config.AppConfig;
import com.sinoparasoft.config.StaticApplicationConfiguration;
import com.sinoparasoft.config.TestStaticApplicationConfiguration;
import com.sinoparasoft.nagios.NagiosConfig;
import com.sinoparasoft.openstack.CloudManipulator;
import com.sinoparasoft.openstack.CloudManipulatorFactory;
import com.sinoparasoft.openstack.type.CloudConfig;
import com.sinoparasoft.type.Image;
import com.sinoparasoft.type.MQConfig;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class TestService {
	NetworkServiceImpl networkService= new NetworkServiceImpl();
	ImageServiceImpl imageService=new ImageServiceImpl();

	public TestService() {
		// TODO Auto-generated constructor stub
		TestStaticApplicationConfiguration configuration = new TestStaticApplicationConfiguration();
		configuration.initConfiguration();
	}

	@Test
	public void testIPService() {
		// Test network service
		networkService.cloudConfig = TestStaticApplicationConfiguration.cloudConfig;
		Map<String, Object> maps = networkService.getExternalIpEntries(1, 10, null, null);
		getClass();
		Iterator<String> iterator = maps.keySet().iterator();
		while (iterator.hasNext()) {
			String string = (String) iterator.next();
			System.out.println(string + ":" + maps.get(string));

		}
		assertTrue(maps.size() > 0);
		assertNotNull(maps);
	}
	
	@Test
	public void testImageService(){
		imageService.cloudConfig=TestStaticApplicationConfiguration.cloudConfig;
		List<Image> images=imageService.getAllInOneList();
		for (Iterator iterator = images.iterator(); iterator.hasNext();) {
			Image image = (Image) iterator.next();
			System.out.println(image.getName());
		}
		assertTrue(images.size()>0);		
	}	
}
