package com.sinoparasoft.common;

import java.util.List;

public class BatchActionResult {
	private boolean success;
	private String message;
	private List<String> finishedList;
	private List<String> failedList;
	
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public List<String> getFinishedList() {
		return finishedList;
	}
	public void setFinishedList(List<String> finishedList) {
		this.finishedList = finishedList;
	}
	public List<String> getFailedList() {
		return failedList;
	}
	public void setFailedList(List<String> failedList) {
		this.failedList = failedList;
	}
}
