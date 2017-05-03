package com.sinoparasoft.tarnovo;

import com.sinoparasoft.message.MessageProcessorCommand;

public class DisplayCommand implements MessageProcessorCommand {

	public boolean execute(String messageBody) {
		System.out.println("received message: " + messageBody);	
		
		return false;
	}

}
