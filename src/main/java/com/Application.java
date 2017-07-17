package com;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.session.SessionAutoConfiguration;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import javax.servlet.MultipartConfigElement;

/**
 * Created by yangry on 2016/6/12 0012.
 */
@SpringBootApplication(exclude = {SessionAutoConfiguration.class})
@ServletComponentScan
public class Application {

    public static void main(String[] args) throws Exception {
        final ApplicationContext applicationContext = SpringApplication.run(Application.class, args);
        SpringApplicationcontextUtil.setApplicationContext(applicationContext);
    }

    @Bean
    MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize("1000MB"); //KB,MB
        factory.setMaxRequestSize("1000MB");
        return factory.createMultipartConfig();
    }

}
