package com.yh.bigdata.tts.common.param.base;

/**
 * @author duyp
 * 
 * @date 2019/01/15
 * 
 * @comment
 */

public class ResponseUtil {
	
	public static final ResponseCode SUCCESS = new ResponseCode("200", "success");
	public static final ResponseCode SERVER_ERROR = new ResponseCode("500", "服务器错误!");
	public static final ResponseCode OPERATE_FAILED = new ResponseCode("400", "操作失败!");
	public static final ResponseCode PARAM_ILLEGAL = new ResponseCode("300", "参数不合法!");
	public static final ResponseCode NO_DATA = new ResponseCode("401", "数据不存在!");

	public static Response<String> success() {
		return new Response<String>(SUCCESS.statusCode, SUCCESS.errorMessage);
	}
	
	public static <T> Response<T> success(T data) {
		return new Response<T>(SUCCESS.statusCode, SUCCESS.errorMessage, data);
	}
	
	public static Response<String> fail() {
		return new Response<String>(SERVER_ERROR.statusCode, SERVER_ERROR.errorMessage);
	}
	
	public static Response<String> custom(ResponseCode responseCode) {
		return new Response<String>(responseCode.statusCode, responseCode.errorMessage);
	}

	
	public static class ResponseCode {
		
		private String statusCode;
		private String errorMessage;
		
		public ResponseCode(String statusCode, String errorMessage) {
			this.statusCode = statusCode;
			this.errorMessage = errorMessage;
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
	}
}
