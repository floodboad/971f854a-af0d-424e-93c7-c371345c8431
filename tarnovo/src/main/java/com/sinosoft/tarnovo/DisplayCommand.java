package com.sinosoft.tarnovo;

import com.sinosoft.message.MessageProcessorCommand;

public class DisplayCommand implements MessageProcessorCommand {

	public boolean execute(String messageBody) {
		System.out.println("received message: " + messageBody);	
		
		return false;
	}

}
