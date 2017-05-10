package com.sinoparasoft.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sinoparasoft.message.RetryMessageProducer;
import com.sinoparasoft.nagios.NagiosConfig;
import com.sinoparasoft.openstack.type.CloudConfig;
import com.sinoparasoft.type.MQConfig;
import com.sinoparasoft.config.AppConfig;

public class StaticApplicationConfiguration {
	private static Logger logger = LoggerFactory.getLogger(StaticApplicationConfiguration.class);

	private static Properties properties;

	public AppConfig initAppConfig() {
		AppConfig appConfig = new AppConfig();
		
		appConfig.setPortalWaitTimeout(Integer.parseInt(properties.getProperty("portal.waitTimeout")));
		appConfig.setPortalLoopInterval(Integer.parseInt(properties.getProperty("portal.loopInterval")));

		appConfig.setGangliaServers(properties.getProperty("monitor.gangliaServers").split(","));

		appConfig.setCephRestApiUrl(properties.getProperty("ceph.restApiUrl"));
		appConfig.setCephPoolReplicaSize(Integer.parseInt(properties.getProperty("ceph.poolReplicaSize")));
		appConfig.setCephImagePoolSize(Integer.parseInt(properties.getProperty("ceph.imagePoolSize")));
		appConfig.setCephJounralSize(Integer.parseInt(properties.getProperty("ceph.jounralSize")));
		

		return appConfig;
	}

	public CloudConfig initCloudConfig() {
		CloudConfig cloudConfig = new CloudConfig();
		cloudConfig.setCloudManipulatorVersion(properties.getProperty("cloud.manipulatorVersion"));
		cloudConfig.setAuthUrl(properties.getProperty("cloud.authUrl"));
		cloudConfig.setAdminUsername(properties.getProperty("cloud.adminUsername"));
		cloudConfig.setAdminPassword(properties.getProperty("cloud.adminPassword"));
		cloudConfig.setAdminProjectName(properties.getProperty("cloud.adminProjectName"));
		cloudConfig.setPublicNetworkId(properties.getProperty("cloud.publicNetworkId"));
		cloudConfig.setAdminUserId(properties.getProperty("cloud.adminUserId"));
		cloudConfig.setDomainName(properties.getProperty("cloud.domainName"));
		cloudConfig.setDomainId(properties.getProperty("cloud.domainId"));
		cloudConfig.setAdminProjectId(properties.getProperty("cloud.adminProjectId"));
		cloudConfig.setAdminRoleName(properties.getProperty("cloud.adminRoleName"));
		cloudConfig.setAodhServiceUrl(properties.getProperty("cloud.aodhServiceUrl"));
		cloudConfig.setAlarmThresholdRulePeriod(Integer.parseInt(properties.getProperty("cloud.alarmThresholdRulePeriod")));

		return cloudConfig;
	}

	public NagiosConfig initNagiosConfig() {
		NagiosConfig nagiosConfig = new NagiosConfig();
		nagiosConfig.setNagiosHost(properties.getProperty("nagios.host"));
		nagiosConfig.setNagiosPort(Integer.parseInt(properties.getProperty("nagios.port")));
		nagiosConfig.setNagiosUserName(properties.getProperty("nagios.username"));
		nagiosConfig.setNagiosPassword(properties.getProperty("nagios.password"));
		nagiosConfig.setNagiosUrl(properties.getProperty("nagios.url"));

		return nagiosConfig;
	}

	public MQConfig initMQConfig() {
		MQConfig mqConfig = new MQConfig();
		
		mqConfig.setRabbitmqHost(properties.getProperty("rabbitmq.host"));
		mqConfig.setRabbitmqUsername(properties.getProperty("rabbitmq.username"));
		mqConfig.setRabbitmqPassword(properties.getProperty("rabbitmq.password"));
		mqConfig.setRabbitmqVirtualHost(properties.getProperty("rabbitmq.virtualHost"));

		return mqConfig;
	}

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
	
	public static Properties getProperties() {
		return properties;
	}

	public static void setProperties(Properties properties) {
		StaticApplicationConfiguration.properties = properties;
	}
}
