package com.sinoparasoft.task;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sinoparasoft.message.RetryMessageConsumer;
import com.sinoparasoft.type.MQConfig;
import com.sinoparasoft.common.AppConfig;

@Service
public class AsyncOperationWorker {
	private static Logger logger = LoggerFactory.getLogger(AsyncOperationWorker.class);

	@Autowired
	AppConfig appConfig;

	RetryMessageConsumer messageConsumer;

	@Autowired
	AsyncOperationCommand command;

	@Autowired
	MQConfig mqConfig;
	
	@PostConstruct
	public void init() {
		try {
			messageConsumer = new RetryMessageConsumer();
			messageConsumer.connect(mqConfig.getRabbitmqHost(), mqConfig.getRabbitmqVirtualHost(),
					mqConfig.getRabbitmqUsername(), mqConfig.getRabbitmqPassword());

			messageConsumer.consume(command);
		} catch (Exception e) {
			logger.error("启动异步操作处理程序发生错误", e);
		}
	}

	@PreDestroy
	public void destroy() {
		try {
			messageConsumer.close();
		} catch (Exception e) {
			logger.error("关闭异步操作处理程序发生错误", e);
		}
	}
}
