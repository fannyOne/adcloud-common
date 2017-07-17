package com.asiainfo.config;

import com.asiainfo.filter.CorpsFilter;
import com.asiainfo.filter.RefererFilter;
import com.asiainfo.filter.XssFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//import com.asiainfo.filter.PortalFilter;

/**
 * Created by weif on 2017/5/2.
 */
@Configuration
public class WebFilterConfig {

    @Autowired
    RefererFilter refererFilter;
    @Autowired
    CorpsFilter corpsFilter;


    @Bean
    public FilterRegistrationBean xssFilterRegistrationBean() {
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setName("XssFilter");
        XssFilter xssFilter = new XssFilter();
        registrationBean.setFilter(xssFilter);
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(3);
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean refererFilterRegistrationBean() {
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setName("RefererFilter");
        registrationBean.setFilter(refererFilter);
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(4);
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean corpsFilterRegistrationBean() {
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setName("CorpsFilter");
        registrationBean.setFilter(corpsFilter);
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(5);
        return registrationBean;
    }

}
