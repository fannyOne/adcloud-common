package com.asiainfo.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.stereotype.Component;

@Component
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    /**
     * springsecurity 配置，所有的请求都需要经过授权，否则跳转到gitlab进行登录验证。
     *
     * @param http
     * @throws Exception
     * @author:guojian
     * @date : 2016-07-06
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable(); //Disabled CSRF for jenkins call back http post
        //FIXME:关闭权限控制
        http.authorizeRequests().anyRequest().permitAll();
        //FIXME:开启权限控制
//        http.authorizeRequests().antMatchers("/auth/getCookies","/auth/index","/auth/main","/auth/callback","/changeStepByAutoTest").permitAll().anyRequest().authenticated().and().formLogin().loginPage("/auth/index");
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
            .withUser("jacky").password("123").roles("USER");
    }
}
