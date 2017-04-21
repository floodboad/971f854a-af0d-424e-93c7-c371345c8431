package com.sinosoft.message;

import java.io.IOException;
import java.net.ConnectException;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public abstract class RetryMessageQueueClient {
	private static Logger logger = LoggerFactory.getLogger(RetryMessageQueueClient.class);

	protected static final String WORK_EXCHANGE_NAME = "work_exchange";
	protected static final String WORK_QUEUE_NAME = "work_queue";
	protected static final String RETRY_EXCHANGE_NAME = "retry_exchange";
	protected static final String RETRY_QUEUE_NAME = "retry_queue";

	/*
	 * ttl in ms
	 */
	protected static final int RETRY_QUEUE_MESSAGE_TTL = 30000;
	protected static final int PER_MESSAGE_TTL = 60000;
	protected static final float PER_MESSAGE_TTL_EXPONENTIAL_FACTOR = 1.2f;
	protected static final int MAX_PER_MESSAGE_TTL = 86400000; // one day

	private static final int RECONNECT_INTERVAL = 30000;

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
	public void connect(String host, String virtualHost, String username, String password) throws IOException {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(host);
		factory.setVirtualHost(virtualHost);
		factory.setUsername(username);
		factory.setPassword(password);

		/*
		 * explicitly enable automatic recovery for Java client prior 4.0.0. the purpose is after the broker comes back
		 * online, the connection will be reestablished, and messages will be published thereafter.
		 */
		factory.setAutomaticRecoveryEnabled(true);
		factory.setNetworkRecoveryInterval(RECONNECT_INTERVAL);

		/*
		 * connection is a socket abstraction
		 */
		connection = retryConnect(factory);

		/*
		 * channel is API calling from
		 */
		channel = connection.createChannel();

		/*
		 * declare a direct exchange.
		 */
		channel.exchangeDeclare(WORK_EXCHANGE_NAME, "direct");

		/*
		 * set queue as durable, so the message will not lost after rabbitmq server dies.
		 */
		boolean durable = true;
		channel.queueDeclare(WORK_QUEUE_NAME, durable, false, false, null);

		/*
		 * binding.
		 */
		channel.queueBind(WORK_QUEUE_NAME, WORK_EXCHANGE_NAME, "");
	}

	private Connection retryConnect(ConnectionFactory factory) throws IOException {
		Connection connection = null;
		boolean retry = true;

		while (true == retry) {
			try {
				connection = factory.newConnection();

				retry = false;
			} catch (ConnectException e) {
				try {
					// System.out.println("建立消息队列连接失败，将于" + RECONNECT_INTERVAL + "毫秒后重试，" + e.getClass() + "，"
					// + e.getMessage());
					logger.warn("建立消息队列连接失败，将于" + RECONNECT_INTERVAL + "毫秒后重试，" + e.getClass() + "，" + e.getMessage());
					Thread.sleep(RECONNECT_INTERVAL);
				} catch (InterruptedException e1) {
					retry = false;
				}
			} catch (TimeoutException e) {
				try {
					// System.out.println("建立消息队列连接失败，将于" + RECONNECT_INTERVAL + "毫秒后重试，" + e.getClass() + "，"
					// + e.getMessage());
					logger.warn("建立消息队列连接失败，将于" + RECONNECT_INTERVAL + "毫秒后重试，" + e.getClass() + "，" + e.getMessage());
					Thread.sleep(RECONNECT_INTERVAL);
				} catch (InterruptedException e1) {
					retry = false;
				}
			}
		}

		return connection;
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

	/**
	 * check if client is connected. Use of the isOpen() method of channel and connection objects is not recommended for
	 * production code. https://www.rabbitmq.com/api-guide.html
	 * 
	 * @return
	 * @author xiangqian
	 */
	public boolean isOpen() {
		return (connection.isOpen() && channel.isOpen());
	}
}
