package com.sinoparasoft.message;

public interface MessageProcessorCommand {
	/**
	 * message handler method.
	 * 
	 * @param messageBody - message
	 * @return true if retry is needed, return false otherwise
	 * @author xiangqian
	 */
	public boolean execute(String messageBody);
}
