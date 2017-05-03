package com.sinoparasoft.message;

import java.io.IOException;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.MessageProperties;

public class RetryMessageProducer extends RetryMessageQueueClient {
	public void publish(String message) throws IOException {
		/*
		 * set message as durable with PERSISTENT_TEXT_PLAIN, so the message will not lost after rabbitmq server dies.
		 */
		BasicProperties properties = MessageProperties.PERSISTENT_TEXT_PLAIN;
		/*
		 * set Per-Message TTL.
		 */
		// properties = properties.builder().expiration("5000").build();
		channel.basicPublish(WORK_EXCHANGE_NAME, "", properties, message.getBytes());
	}
}
