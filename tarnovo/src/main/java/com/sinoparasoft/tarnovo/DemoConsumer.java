package com.sinoparasoft.tarnovo;

import java.io.IOException;

import com.sinoparasoft.message.RetryMessageConsumer;

public class DemoConsumer {
    public static void main( String[] args )
    {
    	String host = "192.168.100.89";
    	String virtualHost = "/";
    	String username = "openstack";
    	String password = "123456";
    	
//    	SimpleMessageConsumer consumer = new SimpleMessageConsumer();
    	RetryMessageConsumer consumer = new RetryMessageConsumer();
    	try {
			consumer.connect(host, virtualHost, username, password);
			
			DisplayCommand command = new DisplayCommand();
			consumer.consume(command);
			
			System.out.println("consumer end");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
