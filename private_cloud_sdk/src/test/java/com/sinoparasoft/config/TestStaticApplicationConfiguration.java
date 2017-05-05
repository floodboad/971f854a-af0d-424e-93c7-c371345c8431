package com.sinoparasoft.config;

import static org.junit.Assert.assertNotNull;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import org.junit.Test;
import org.openstack4j.model.compute.Server;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.sinoparasoft.nagios.NagiosConfig;
import com.sinoparasoft.openstack.CloudManipulator;
import com.sinoparasoft.openstack.CloudManipulatorFactory;
import com.sinoparasoft.openstack.type.CloudConfig;
import com.sinoparasoft.type.MQConfig;

public class TestStaticApplicationConfiguration {
	
	public static AppConfig appconfig;
	public static CloudConfig cloudConfig;
	public static NagiosConfig nagiosConfig;
	public static MQConfig mqConfig;
	
	public TestStaticApplicationConfiguration() {
		// TODO Auto-generated constructor stub
		ApplicationContext ctx = new FileSystemXmlApplicationContext("src/main/resources/META-INF/spring/applicationContext.xml"); 
	}
	
	public void initConfiguration() {
		// TODO Auto-generated constructor stub
		Properties properties=new Properties();		
		try {
			properties.load(new FileInputStream("D:/GGCloud-Origin/private_cloud_sdk/target/classes/META-INF/spring/config.properties"));
			StaticApplicationConfiguration global_config=new StaticApplicationConfiguration();
			global_config.setProperties(properties);			
			appconfig=global_config.initAppConfig();
			cloudConfig=global_config.initCloudConfig();
			nagiosConfig =global_config.initNagiosConfig();
			mqConfig=global_config.initMQConfig();			
			global_config.initMessageProducer(mqConfig);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void TestStaticConfiguration(){
		initConfiguration();
		assertNotNull(appconfig);
		
	}

}
