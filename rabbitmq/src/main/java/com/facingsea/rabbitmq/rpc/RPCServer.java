package com.facingsea.rabbitmq.rpc;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

/**
 * 服务端，来自：https://www.rabbitmq.com/tutorials/tutorial-six-java.html
 * @author wangzhf
 *
 */
public class RPCServer {
	
	private static final String QUEUE_NAME = "rpc_name";
	
	/**
	 * 斐波那契数列
	 * @param i
	 * @return
	 */
	private static int fib(int i) {
		if(i == 0 ) return 0;
		if(i == 1) return 1;
		return fib(i - 1) + fib(i - 2);
	}

	public static void main(String[] args) {
		Connection conn = null;
		Channel channel = null;
		try {
			ConnectionFactory factory = new ConnectionFactory();
			factory.setHost("localhost");
			conn = factory.newConnection();
			channel = conn.createChannel();
			channel.queueDeclare(QUEUE_NAME, false, false, false, null);
			channel.basicQos(1);
			QueueingConsumer consumer = new QueueingConsumer(channel);
			channel.basicConsume(QUEUE_NAME, false, consumer);
			System.out.println("[x] Awaiting RPC requests.");
			
			while(true){
				String response = null;
				
				QueueingConsumer.Delivery delivery = consumer.nextDelivery();
				
				BasicProperties props = delivery.getProperties();
				BasicProperties replyProps = new BasicProperties()
													.builder()
													.correlationId(props.getCorrelationId())
													.build();
				
				try {
					String message = new String(delivery.getBody(), "UTF-8");
					int n = Integer.parseInt(message);
					System.out.println("[.] fib(" +n + ")");
					response = "" + fib(n);
				} catch (NumberFormatException e) {
					e.printStackTrace();
					System.out.println("[.] " + e.toString());
					response = "";
				} finally {
					channel.basicPublish("", props.getReplyTo(), replyProps, response.getBytes("UTF-8"));
					channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		} catch (ShutdownSignalException e) {
			e.printStackTrace();
		} catch (ConsumerCancelledException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			if(conn != null ){
				try {
					conn.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
}
