package com.asiainfo.config;

import com.asiainfo.interceptors.LoginInterceptor;
import com.asiainfo.interceptors.ProjectAuthorInterceptor;
import com.asiainfo.interceptors.UserRoleAuthorInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Created by YangRY
 * 2016/7/25 0025.
 */
@Component
@Configuration
public class WebInterceptConfig extends WebMvcConfigurerAdapter {
    @Autowired
    UserRoleAuthorInterceptor userRoleAuthorInterceptor;

    @Autowired
    LoginInterceptor loginInterceptor;
    @Autowired
    ProjectAuthorInterceptor projectAuthorInterceptor;

    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
            .addPathPatterns("/**");
        registry.addInterceptor(userRoleAuthorInterceptor)
            .addPathPatterns("/**/role/**").addPathPatterns("/**/groupAdminUser/**");
        registry.addInterceptor(projectAuthorInterceptor)
            .addPathPatterns("/**/project/**").addPathPatterns("/**/stage/**")
            .addPathPatterns("/**/branch/**").addPathPatterns("/**/pipeline/**");
    }
}
