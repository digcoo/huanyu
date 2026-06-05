//package com.yh.bigdata.tts.spider.datasource;
//
//import com.alibaba.druid.pool.DruidDataSource;
//import org.springframework.boot.bind.RelaxedPropertyResolver;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.env.Environment;
//
//import javax.sql.DataSource;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.Map;
//import java.util.Map.Entry;
//
//@Configuration
//public class CmsDataSource2 extends DynamicDataSource {
//
//	private static CmsDataSource2 cmsDataSource;
//	@SuppressWarnings("rawtypes")
//	private static Map dataSources = new HashMap();
//
//	public CmsDataSource2(Environment env) throws Exception {
//		cmsDataSource = this;
//		initCustomDataSources(env);
//	}
//
//	@SuppressWarnings({ "rawtypes", "unchecked" })
//	public static DataSource initDataSource(Map dsMap) throws Exception {
//		DruidDataSource dataSource = new DruidDataSource();
//		dataSource.setDriverClassName((String) dsMap.get("driver-class-name"));
//		dataSource.setUsername((String) dsMap.get("username"));
//		dataSource.setPassword((String) dsMap.get("password"));
//		dataSource.setUrl((String) dsMap.get("url"));
//		dataSource.setInitialSize(
//				dsMap.get("initialSize") != null ? Integer.parseInt(dsMap.get("initialSize").toString()) : 5);
//		dataSource.setMinIdle(dsMap.get("minIdle") != null ? Integer.parseInt(dsMap.get("minIdle").toString()) : 1);
//		dataSource.setMaxActive(
//				dsMap.get("maxActive") != null ? Integer.parseInt(dsMap.get("maxActive").toString()) : 50);
//		dataSource.setDefaultAutoCommit(false);
//		if (dsMap.get("dsname") != null && !dsMap.get("dsname").equals("")) {
//			dataSources.put(dsMap.get("dsname"), dataSource);
//		}
//
//		dataSource.setFilters("stat");
//		dataSource.addFilters("wall");
//		return dataSource;
//	}
//
//	@SuppressWarnings({ "unchecked", "rawtypes" })
//	private static void initCustomDataSources(Environment env) throws Exception {
//		RelaxedPropertyResolver propertyResolver = new RelaxedPropertyResolver(env, "custom.datasource.");
//		String dsPrefixs = propertyResolver.getProperty("names");
//		String[] var6;
//		int var5 = (var6 = dsPrefixs.split(",")).length;
//
//		for (int i = 0; i < var5; ++i) {
//			String dsPrefix = var6[i];
//			Map dsMap = propertyResolver.getSubProperties(dsPrefix + ".");
//			DataSource ds = initDataSource(dsMap);
//			getDataSources().put(dsPrefix, ds);
//		}
//
//		cmsDataSource.setTargetDataSources(getDataSources(), (DataSource) getDataSources().get("default"));
//		cmsDataSource.init();
//	}
//
//	public void afterPropertiesSet() {
//	}
//
//	public void init() {
//		super.afterPropertiesSet();
//	}
//
//	@SuppressWarnings("unchecked")
//	public void put(Object name, Object dataSource) {
//		dataSources.put(name, dataSource);
//	}
//
//	public void remove(Object name) {
//		dataSources.remove(name);
//	}
//
//	@SuppressWarnings("rawtypes")
//	public static Map getDataSources() {
//		return dataSources;
//	}
//
//	@SuppressWarnings("rawtypes")
//	public static DruidDataSource getDataSources(String dsname) {
//		Iterator var2 = dataSources.entrySet().iterator();
//
//		while (var2.hasNext()) {
//			Entry entry = (Entry) var2.next();
//			DruidDataSource ds = (DruidDataSource) entry.getValue();
//			if (entry.getKey().equals(dsname)) {
//				return ds;
//			}
//		}
//
//		return null;
//	}
//
//	public static void reflash() {
//		cmsDataSource.init();
//	}
//}
