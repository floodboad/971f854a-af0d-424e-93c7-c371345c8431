package com.sinoparasoft.nagios.exception;

@SuppressWarnings("serial")
public class NagiosException extends RuntimeException {
	public NagiosException(String message, Throwable cause) {
		super(message, cause);
	}
}
