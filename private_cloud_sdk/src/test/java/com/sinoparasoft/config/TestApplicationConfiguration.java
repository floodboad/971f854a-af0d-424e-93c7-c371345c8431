package com.sinoparasoft.config;

import org.junit.Test;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.sinoparasoft.nagios.NagiosConfig;
import com.sinoparasoft.openstack.type.CloudConfig;
import com.sinoparasoft.type.MQConfig;

public class TestApplicationConfiguration {
	
	
	public TestApplicationConfiguration() {
		// TODO Auto-generated constructor stub
		ApplicationContext ctx = new FileSystemXmlApplicationContext("src/main/resources/META-INF/spring/applicationContext.xml"); 
		System.out.println(ctx.getEnvironment().getProperty("cloud.manipulatorVersion"));
		
		ApplicationConfiguration.setEnv(ctx.getEnvironment());
		
		
		//Resource resource = new ClassPathResource("applicationContext.xml");     
		//XmlBeanFactory bean = new XmlBeanFactory(resource);  
	}
	
	@Test
	public void TestConfiguration(){
//		System.out.println("Test configuration!");
//		ApplicationConfiguration global_config=new ApplicationConfiguration();
//				
//		AppConfig appconfig=global_config.initAppConfig();
//		CloudConfig cloudConfig=global_config.initCloudConfig();
//		NagiosConfig nagiosConfig =global_config.initNagiosConfig();
//		MQConfig mqConfig=global_config.initMQConfig();
	}

	public static void main(String[] args) {
		TestApplicationConfiguration test=new TestApplicationConfiguration();
		test.TestConfiguration();
	}
}
