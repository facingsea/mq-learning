package com.facingsea.rabbitmq.rpc;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

/**
 * 客户端，来自：https://www.rabbitmq.com/tutorials/tutorial-six-java.html
 * @author wangzhf
 *
 */
public class RPCClient {
	
	private Connection conn = null;
	private Channel channel = null;
	private String requestQueueName = "rpc_name";
	private String replyQueueName;
	private QueueingConsumer consumer;

	public RPCClient() throws IOException, TimeoutException {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		
		conn = factory.newConnection();
		channel = conn.createChannel();
		
		replyQueueName = channel.queueDeclare().getQueue();
		consumer = new QueueingConsumer(channel);
		channel.basicConsume(replyQueueName, true, consumer);
	}
	
	public String call(String message) throws Exception{
		String response = null;
		
		String corrId = UUID.randomUUID().toString().replaceAll("-", "");
		
		BasicProperties props = new BasicProperties()
										.builder()
										.correlationId(corrId)
										.replyTo(replyQueueName)
										.build();
		
		channel.basicPublish("", requestQueueName, props, message.getBytes());
		
		while(true){
			QueueingConsumer.Delivery delivery = consumer.nextDelivery();
			if(delivery.getProperties().getCorrelationId().equals(corrId)){
				response = new String(delivery.getBody());
				break;
			}
		}
		
		return response;
	}
	
	public void close() throws IOException{
		if(conn != null ){
			conn.close();
		}
	}
	
	
	public static void main(String[] args) {
		RPCClient client = null;
		try {
			client = new RPCClient();
			System.out.println("[x] Requesting fib(30)");
			String response = client.call("3");
			System.out.println("[.] Got '" + response + "'");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				client.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
}
