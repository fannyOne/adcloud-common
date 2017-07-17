package com.asiainfo.config;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.config.ServerConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import static com.asiainfo.config.BaseConfig.getServerConfig;

@Configuration
@lombok.extern.slf4j.Slf4j
public class QaConfig {

    @Value("${datasource.url}")
    String url;
    @Value("${datasource.username}")
    String userName;
    @Value("${datasource.password}")
    String password;

    @Value("${sonar.datasource.url}")
    String sonarUrl;
    @Value("${sonar.datasource.username}")
    String sonarUserName;
    @Value("${sonar.datasource.password}")
    String sonarPassword;

    public ServerConfig serverConfigAdcloud_asDefault() {
        return getServerConfig("oracle.jdbc.OracleDriver", userName, password, url, "adcloud", true, 0, 30);
    }

//    public ServerConfig serverConfigRmp() {
//        return getServerConfig("oracle.jdbc.OracleDriver", "slm", "slm", "jdbc:oracle:thin:@20.26.11.30:1521:CSHP12", "rmp", false, 0, 30);
//    }


    public ServerConfig serverConfigSonar() {
        return getServerConfig("oracle.jdbc.OracleDriver", sonarUserName, sonarPassword, sonarUrl, "sonar", false, 0, 30);
    }


    @Bean
    @Profile("qa")
    @Qualifier("adcloud")
    public EbeanServer adcloud() {
        EbeanServer ebeanServer = EbeanServerFactory.create(serverConfigAdcloud_asDefault());
        log.info("Creating Ebean server: " + ebeanServer + ebeanServer.getName());
        return ebeanServer;
    }

//    @Bean
//    @Profile("qa")
//    @Qualifier("rmp")
//    public EbeanServer rmp() {
//        EbeanServer ebeanServer = EbeanServerFactory.create(serverConfigRmp());
//        log.info("Creating Ebean server: " + ebeanServer + ebeanServer.getName());
//        return ebeanServer;
//    }

    @Bean
    @Profile("qa")
    @Qualifier("sonar")
    public EbeanServer sonar() {
        EbeanServer ebeanServer = EbeanServerFactory.create(serverConfigSonar());
        log.info("Creating Ebean server: " + ebeanServer + ebeanServer.getName());
        return ebeanServer;
    }

}
