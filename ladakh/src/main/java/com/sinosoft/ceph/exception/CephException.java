package com.sinosoft.ceph.exception;

@SuppressWarnings("serial")
public class CephException extends RuntimeException {
	public CephException(String message, Throwable cause) {
		super(message, cause);
	}
}
