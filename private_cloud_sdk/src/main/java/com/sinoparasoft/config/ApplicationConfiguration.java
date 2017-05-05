package com.sinoparasoft.config;

import java.io.IOException;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import com.sinoparasoft.message.RetryMessageProducer;
import com.sinoparasoft.nagios.NagiosConfig;
import com.sinoparasoft.openstack.type.CloudConfig;
import com.sinoparasoft.type.MQConfig;
import com.sinoparasoft.config.AppConfig;

@Configuration
@PropertySource("classpath:META-INF/spring/config.properties")
public class ApplicationConfiguration {
	private static Logger logger = LoggerFactory.getLogger(ApplicationConfiguration.class);

	//@Autowired
	@Autowired
	private static Environment env;

	@Bean(name = "appConfig")
	public AppConfig initAppConfig() {
		AppConfig appConfig = new AppConfig();
		
		appConfig.setPortalWaitTimeout(Integer.parseInt(env.getProperty("portal.waitTimeout")));
		appConfig.setPortalLoopInterval(Integer.parseInt(env.getProperty("portal.loopInterval")));

		appConfig.setGangliaServers(env.getProperty("monitor.gangliaServers").split(","));

		appConfig.setCephRestApiUrl(env.getProperty("ceph.restApiUrl"));
		appConfig.setCephPoolReplicaSize(Integer.parseInt(env.getProperty("ceph.poolReplicaSize")));
		appConfig.setCephImagePoolSize(Integer.parseInt(env.getProperty("ceph.imagePoolSize")));
		appConfig.setCephJounralSize(Integer.parseInt(env.getProperty("ceph.jounralSize")));

		return appConfig;
	}

	@Bean(name = "cloudConfig")
	public CloudConfig initCloudConfig() {
		CloudConfig cloudConfig = new CloudConfig();
		cloudConfig.setCloudManipulatorVersion(env.getProperty("cloud.manipulatorVersion"));
		cloudConfig.setAuthUrl(env.getProperty("cloud.authUrl"));
		cloudConfig.setAdminUsername(env.getProperty("cloud.adminUsername"));
		cloudConfig.setAdminPassword(env.getProperty("cloud.adminPassword"));
		cloudConfig.setAdminProjectName(env.getProperty("cloud.adminProjectName"));
		cloudConfig.setPublicNetworkId(env.getProperty("cloud.publicNetworkId"));
		cloudConfig.setAdminUserId(env.getProperty("cloud.adminUserId"));
		cloudConfig.setDomainName(env.getProperty("cloud.domainName"));
		cloudConfig.setDomainId(env.getProperty("cloud.domainId"));
		cloudConfig.setAdminProjectId(env.getProperty("cloud.adminProjectId"));
		cloudConfig.setAdminRoleName(env.getProperty("cloud.adminRoleName"));
		cloudConfig.setAodhServiceUrl(env.getProperty("cloud.aodhServiceUrl"));
		cloudConfig.setAlarmThresholdRulePeriod(Integer.parseInt(env.getProperty("cloud.alarmThresholdRulePeriod")));

		return cloudConfig;
	}

	@Bean(name = "nagiosConfig")
	public NagiosConfig initNagiosConfig() {
		NagiosConfig nagiosConfig = new NagiosConfig();
		nagiosConfig.setNagiosHost(env.getProperty("nagios.host"));
		nagiosConfig.setNagiosPort(Integer.parseInt(env.getProperty("nagios.port")));
		nagiosConfig.setNagiosUserName(env.getProperty("nagios.username"));
		nagiosConfig.setNagiosPassword(env.getProperty("nagios.password"));
		nagiosConfig.setNagiosUrl(env.getProperty("nagios.url"));

		return nagiosConfig;
	}

	@Bean(name = "mqConfig")
	public MQConfig initMQConfig() {
		MQConfig mqConfig = new MQConfig();

		mqConfig.setRabbitmqHost(env.getProperty("rabbitmq.host"));
		mqConfig.setRabbitmqUsername(env.getProperty("rabbitmq.username"));
		mqConfig.setRabbitmqPassword(env.getProperty("rabbitmq.password"));
		mqConfig.setRabbitmqVirtualHost(env.getProperty("rabbitmq.virtualHost"));

		return mqConfig;
	}

	@Bean(name = "messageProducer")
	public RetryMessageProducer initMessageProducer(MQConfig mqConfig) {
		RetryMessageProducer messageProducer = new RetryMessageProducer();
		try {
			messageProducer.connect(mqConfig.getRabbitmqHost(), mqConfig.getRabbitmqVirtualHost(),
					mqConfig.getRabbitmqUsername(), mqConfig.getRabbitmqPassword());
		} catch (IOException e) {
			return null;
		}

		if (true == messageProducer.isOpen()) {
			logger.info("成功建立消息连接");
		}
		return messageProducer;
	}
	
	
	public static Environment getEnv() {
		return env;
	}

	public static void setEnv(Environment env) {
		if (ApplicationConfiguration.env==null) {
			ApplicationConfiguration.env = env;
		}		
	}
}
