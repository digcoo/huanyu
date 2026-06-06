package com.yh.bigdata.tts.common.param.base;

/**
 * @author duyp
 * 
 * @date 2019/01/15
 * 
 * @comment
 */

public class Response<T> {


	private String statusCode;
	
	private String errorMessage;
	
	private T data;

	public Response(){}
	
	public Response(String statusCode, String errorMessage) {
		this.statusCode = statusCode;
		this.errorMessage = errorMessage;
	}
	
	public Response(String statusCode, String errorMessage, T data) {
		this.statusCode = statusCode;
		this.errorMessage = errorMessage;
		this.data = data;
	}



	public String getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}
}
