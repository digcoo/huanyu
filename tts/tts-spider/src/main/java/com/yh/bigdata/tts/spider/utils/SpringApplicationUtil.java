package com.yh.bigdata.tts.spider.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @author duyp
 * 
 * @date 2019/01/21
 * 
 * @comment
 */

@Component
public class SpringApplicationUtil implements ApplicationContextAware {

	private static ApplicationContext applicationContext;
	
//    private final static ThreadLocal<Map<String, Object>> HOLDER = new ThreadLocal<>();


	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		if (SpringApplicationUtil.applicationContext == null) {
			SpringApplicationUtil.applicationContext = applicationContext;
		}
	}

	// 获取applicationContext
	public static ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	// 通过name获取 Bean.
	public static Object getBean(String name) {
		return getApplicationContext().getBean(name);
	}

	// 通过class获取Bean.
	public static <T> T getBean(Class<T> clazz) {
		return getApplicationContext().getBean(clazz);
	}

	// 通过name,以及Clazz返回指定的Bean
	public static <T> T getBean(String name, Class<T> clazz) {
		return getApplicationContext().getBean(name, clazz);
	}

	// 通过name,以及Clazz返回指定的Bean
	public static String getProperty(String key) {
		return getApplicationContext().getEnvironment().getProperty(key);
	}
	
	public static Object getRequestParameter(String key){
		ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
		return servletRequestAttributes.getRequest().getAttribute(key);
	}

//    public static void set(String key, Object value) {
//        getContext().put(key, value);
//    }
//
//    public static Object get(String key) {
//        return getContext().get(key);
//    }
//
//    static private Map<String, Object> getContext() {
//        Map<String, Object> map = HOLDER.get();
//        if (map == null) {
//            map = new HashMap<>(8);
//            HOLDER.set(map);
//        }
//        return map;
//    }
}
