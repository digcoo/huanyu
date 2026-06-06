package com.yh.bigdata.tts.spider.datasource;

import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class DynamicDataSource extends AbstractRoutingDataSource {

	protected Object determineCurrentLookupKey() {
		return DynamicDataSourceContextHolder.getDataSourceType();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void setTargetDataSources(Map targetDataSources, DataSource defaultDatabase) {
		this.setTargetDataSources(targetDataSources);
		if (defaultDatabase != null) {
			this.setDefaultTargetDataSource(defaultDatabase);
		}

	}
}
