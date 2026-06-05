package com.yh.bigdata.tts.spider.datasource;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.sql.DataSource;

import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.alibaba.druid.pool.DruidDataSource;

@Configuration
public class CmsDataSource extends DynamicDataSource {

    private static CmsDataSource cmsDataSource;
    @SuppressWarnings("rawtypes")
    private static Map dataSources = new HashMap();

    public CmsDataSource(Environment env) throws Exception {
        cmsDataSource = this;
        initCustomDataSources(env);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static DataSource initDataSource(Map dsMap) throws Exception {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName((String) dsMap.get("driver-class-name"));
        dataSource.setUsername((String) dsMap.get("username"));
        dataSource.setPassword((String) dsMap.get("password"));
        dataSource.setUrl((String) dsMap.get("url"));
        dataSource.setInitialSize(
                dsMap.get("initialSize") != null ? Integer.parseInt(dsMap.get("initialSize").toString()) : 5);
        dataSource.setMinIdle(dsMap.get("minIdle") != null ? Integer.parseInt(dsMap.get("minIdle").toString()) : 1);
                dataSource.setMaxActive(
                        dsMap.get("maxActive") != null ? Integer.parseInt(dsMap.get("maxActive").toString()) : 50);
        dataSource.setDefaultAutoCommit(false);
        if (dsMap.get("dsname") != null && !dsMap.get("dsname").equals("")) {
            dataSources.put(dsMap.get("dsname"), dataSource);
        }

        dataSource.setFilters("stat");
        dataSource.addFilters("wall");
        return dataSource;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static void initCustomDataSources(Environment env) throws Exception {
        // 使用 Binder 替代 RelaxedPropertyResolver
        Binder binder = Binder.get(env);

        // 获取数据源名称列表
        String dsPrefixs = binder.bind("custom.datasource.names", String.class).orElse("");
        String[] dsPrefixArray = dsPrefixs.split(",");

        for (String dsPrefix : dsPrefixArray) {
            // 绑定子属性
            Map<String, Object> dsMap = binder.bind("custom.datasource." + dsPrefix, Map.class).orElse(new HashMap<>());
            DataSource ds = initDataSource(dsMap);
            getDataSources().put(dsPrefix, ds);
        }

        cmsDataSource.setTargetDataSources(getDataSources(), (DataSource) getDataSources().get("default"));
        cmsDataSource.init();
    }

    public void afterPropertiesSet() {
    }

    public void init() {
        super.afterPropertiesSet();
    }

    @SuppressWarnings("unchecked")
    public void put(Object name, Object dataSource) {
        dataSources.put(name, dataSource);
    }

    public void remove(Object name) {
        dataSources.remove(name);
    }

    @SuppressWarnings("rawtypes")
    public static Map getDataSources() {
        return dataSources;
    }

    @SuppressWarnings("rawtypes")
    public static DruidDataSource getDataSources(String dsname) {
        Iterator var2 = dataSources.entrySet().iterator();

        while (var2.hasNext()) {
            Entry entry = (Entry) var2.next();
            DruidDataSource ds = (DruidDataSource) entry.getValue();
            if (entry.getKey().equals(dsname)) {
                return ds;
            }
        }

        return null;
    }

    public static void reflash() {
        cmsDataSource.init();
    }
}
