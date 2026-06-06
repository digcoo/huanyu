package com.yh.bigdata.tts.spider.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yh.bigdata.tts.common.param.base.Response;
import com.yh.bigdata.tts.common.param.base.ResponseUtil;

/**
 * @author duyp
 * 
 * @date 2019/01/29
 * 
 * @comment
 */

@ControllerAdvice
public class SystemExceptionHandler {

	private static Logger logger = LoggerFactory.getLogger(SystemExceptionHandler.class);

	@SuppressWarnings("rawtypes")
	@ResponseBody
	@ExceptionHandler(value = Exception.class)
    public Response errorHandler(HttpServletRequest request, HttpServletResponse response, Exception e) throws Exception{
		logger.error("interserver exception...", e);
        return ResponseUtil.fail();
    }
}
