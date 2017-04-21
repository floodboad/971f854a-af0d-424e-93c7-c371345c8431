package com.sinosoft.message;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.LongString;

public class RetryMessageConsumer extends RetryMessageQueueClient {
	private static Logger logger = LoggerFactory.getLogger(RetryMessageConsumer.class);

	public void connect(String host, String virtualHost, String username, String password) throws IOException {
		super.connect(host, virtualHost, username, password);

		/*
		 * allow server to send one message each time to worker, to prevent messages from pile up at worker side,
		 * otherwise the work load maybe unbalanced.
		 */
		int prefetchCount = 1;
		channel.basicQos(prefetchCount);

		/*
		 * declare a direct exchange.
		 */
		channel.exchangeDeclare(RETRY_EXCHANGE_NAME, "direct");

		/*
		 * set queue as durable, so the message will not lost after rabbitmq server dies.
		 */
		boolean durable = true;
		/*
		 * set the dead letter exchange.
		 */
		Map<String, Object> queueArguments = new HashMap<String, Object>();
		queueArguments.put("x-dead-letter-exchange", WORK_EXCHANGE_NAME);
		/*
		 * set message TTL for the queue. the TTL declared here is Per-Queue Message TTL, and it affects all messages in
		 * the queue. you can define Per-Message TTL, by setting the expiration field in the basic AMQP class when
		 * sending a basic.publish. when both a per-queue and a per-message TTL are specified, the lower value between
		 * the two will be chosen.
		 */
		// queueArguments.put("x-message-ttl", RETRY_QUEUE_MESSAGE_TTL);
		channel.queueDeclare(RETRY_QUEUE_NAME, durable, false, false, queueArguments);

		/*
		 * binding.
		 */
		channel.queueBind(RETRY_QUEUE_NAME, RETRY_EXCHANGE_NAME, "");
	}

	/**
	 * handle message. retry if message handler exception.
	 * 
	 * @param command
	 *            - message handler
	 * @throws IOException
	 * @author xiangqian
	 */
	public void consume(final MessageProcessorCommand command) throws IOException {
		Consumer consumer = new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
					throws IOException {
				String messageBody = new String(body, "UTF-8");
				try {
					boolean retry = command.execute(messageBody);
					if (true == retry) {
						/*
						 * republish the message with an increasing Per-Message TTL.
						 */
						int expiration = PER_MESSAGE_TTL;
						Map<String, Object> headers = properties.getHeaders();
						if (null != headers) {
							@SuppressWarnings("unchecked")
							List<Map<String, Object>> xDeathHeader = (List<Map<String, Object>>) headers.get("x-death");
							if (null != xDeathHeader) {
								LongString expirationString = (LongString) xDeathHeader.get(0).get(
										"original-expiration");
								if (null != expirationString) {
									// logger.debug("original-expiration: " + expirationString.toString());
									expiration = Integer.parseInt(expirationString.toString());
								}

								Long count = (Long) xDeathHeader.get(0).get("count");
								if (null != count) {
									// logger.debug("count: " + count);
									expiration = (int) (expiration * Math
											.pow(PER_MESSAGE_TTL_EXPONENTIAL_FACTOR, count));
								}

								if (expiration > MAX_PER_MESSAGE_TTL) {
									expiration = MAX_PER_MESSAGE_TTL;
								}
							}
						}

						properties = properties.builder().expiration(Integer.toString(expiration)).build();

						channel.basicPublish(RETRY_EXCHANGE_NAME, "", properties, body);
					}
				} catch (Exception e) {
					logger.error("处理消息发生错误，尝试进行重试", e);
					/*
					 * requeue message to the original queue, this maybe lead to an infinite consume loop.
					 */
					// channel.basicNack(envelope.getDeliveryTag(), false, true);

					/*
					 * republish the message with a fixed Per-Message TTL.
					 */
					properties = properties.builder().expiration(Integer.toString(PER_MESSAGE_TTL)).build();
					channel.basicPublish(RETRY_EXCHANGE_NAME, "", properties, body);
				} finally {
					/*
					 * ack messsage, so the message will be removed from work queue.
					 */
					channel.basicAck(envelope.getDeliveryTag(), false);
				}
			}
		};

		/*
		 * send ack back to server by myself, the server will not remove the message before it gets the ack. so if the
		 * worker dies, the message will send to other worker.
		 */
		boolean autoAck = false;
		channel.basicConsume(WORK_QUEUE_NAME, autoAck, consumer);
	}
}
