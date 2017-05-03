package com.sinoparasoft.message;

import java.io.IOException;

import com.rabbitmq.client.MessageProperties;

public class SimpleMessageProducer extends SimpleMessageQueueClient {	
	public void publish(String message) throws IOException {
		/*
		 * set message as durable with PERSISTENT_TEXT_PLAIN, so the message will not lost after rabbitmq server dies.
		 */
		channel.basicPublish("", WORK_QUEUE_NAME, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes());
	}
}
