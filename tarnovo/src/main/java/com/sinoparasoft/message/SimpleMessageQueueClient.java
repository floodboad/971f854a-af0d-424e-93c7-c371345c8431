package com.sinoparasoft.message;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public abstract class SimpleMessageQueueClient {
	protected static final String WORK_QUEUE_NAME = "work_queue";
	
	protected Connection connection;
	protected Channel channel;

	/**
	 * connect to message queue server and create queue.
	 * 
	 * @param host
	 *            - server name or ip
	 * @param virtualHost
	 *            - virtual host
	 * @param username
	 *            - message queue user name
	 * @param password
	 *            - password
	 * @throws IOException
	 * @throws TimeoutException
	 * @author xiangqian
	 */
	public void connect(String host, String virtualHost, String username, String password) throws IOException,
			TimeoutException {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(host);
		factory.setVirtualHost(virtualHost);
		factory.setUsername(username);
		factory.setPassword(password);
		/*
		 * connection is a socket abstraction
		 */
		connection = factory.newConnection();
		/*
		 * channel is API calling from
		 */
		channel = connection.createChannel();
		
		/*
		 * set queue as durable, so the message will not lost after rabbitmq server dies.
		 */
		boolean durable = true;
		channel.queueDeclare(WORK_QUEUE_NAME, durable, false, false, null);		
	}

	/**
	 * close message queue connection.
	 * 
	 * @throws IOException
	 * @throws TimeoutException
	 * @author xiangqian
	 */
	public void close() throws IOException, TimeoutException {
		channel.close();
		connection.close();
	}
}
