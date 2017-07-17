package com.asiainfo.config;

import com.avaje.ebean.Model;
import com.avaje.ebean.config.DataSourceConfig;
import com.avaje.ebean.config.ServerConfig;
import org.reflections.Reflections;

import java.util.Set;

/**
 * Created by zhenghp on 2016/12/6.
 */
public class BaseConfig {
    private final static String prefix = "com.asiainfo.comm.module.models";

    public static void addEbeanClasses(ServerConfig config) {
        Reflections reflections = new Reflections(prefix);
        Set<Class<? extends Model>> classes = reflections.getSubTypesOf(Model.class);
        for (Class<?> clazz : classes) {
            config.addClass(clazz);
        }
    }

   
    public static ServerConfig getServerConfig(String driver, String username, String password, String url, String configName, boolean defaultServer, int minConnections, int maxConnections) {
        DataSourceConfig dataSourceConfig = new DataSourceConfig();
        dataSourceConfig.setDriver(driver);
        dataSourceConfig.setUsername(username);
        dataSourceConfig.setPassword(password);
        dataSourceConfig.setUrl(url);
        if (minConnections > 0) {
            dataSourceConfig.setMinConnections(minConnections);
        }
        if (maxConnections > 0) {
            dataSourceConfig.setMaxConnections(maxConnections);
        }

        ServerConfig config = new ServerConfig();
        config.setDefaultServer(defaultServer);
        config.setName(configName);
        config.setDatabaseSequenceBatchSize(1);
        config.setDataSourceConfig(dataSourceConfig);
        config.setDdlGenerate(false);
        config.setDdlRun(false); //注意,为true会先删表
        config.setRegister(true);
        BaseConfig.addEbeanClasses(config);
        return config;
    }
}
