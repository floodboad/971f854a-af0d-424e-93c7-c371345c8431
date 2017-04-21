package com.sinosoft.message;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class SimpleMessageConsumer extends SimpleMessageQueueClient {	
	public void connect(String host, String virtualHost, String username, String password) throws IOException, TimeoutException {
		super.connect(host, virtualHost, username, password);
		
		/*
		 * allow server to send one message each time to worker, to prevent messages from pile up at worker side,
		 * otherwise the work load maybe unbalanced.
		 */
		int prefetchCount = 1;
		channel.basicQos(prefetchCount);		
	}

	public void consume() throws IOException {
		Consumer consumer = new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
					throws IOException {
				String requestBody = new String(body, "UTF-8");
				try {
					System.out.println(requestBody);
					
					// exception simulation
					// int i = 1 / 0;
					
					// ack messsage
					channel.basicAck(envelope.getDeliveryTag(), false);					
				} catch (Exception e) {
					// requeue message
					channel.basicNack(envelope.getDeliveryTag(), false, true);
				}
			}
		};

		/*
		 * send ack back to server by myself, the server will not remove the message before it gets the ack. so if the
		 * worker dies, the message will send to other worker.
		 */
		boolean autoAck = false;
		channel.basicConsume(WORK_QUEUE_NAME, autoAck, consumer);	}
}
