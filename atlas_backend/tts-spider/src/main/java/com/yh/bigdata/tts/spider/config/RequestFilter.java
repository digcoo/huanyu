package com.yh.bigdata.tts.spider.config;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;


@Order(0)
@WebFilter(filterName = "RequestResponseLogFilter", urlPatterns = "/*")
public class RequestFilter extends OncePerRequestFilter  {

	@Override
	public void destroy() {
	}

	@Override
	protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain)
			throws ServletException, IOException {
		filterChain.doFilter(httpServletRequest, httpServletResponse);
	}

}
