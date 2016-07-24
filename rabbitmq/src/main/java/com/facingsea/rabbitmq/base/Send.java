package com.facingsea.rabbitmq.base;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class Send {
	
	private static final String QUEUE_NAME = "hello";
	
	
	public static void main(String[] args) throws IOException, TimeoutException {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		Connection conn = factory.newConnection();
		Channel channel = conn.createChannel();
		channel.queueDeclare(QUEUE_NAME, false, false, false, null);
		String msg = "Hello Rabbit MQ!";
		channel.basicPublish("", QUEUE_NAME, null, msg.getBytes());
		System.out.println("[x] sent '" + msg + "'");
		
		channel.close();
		conn.close();
	}

}
