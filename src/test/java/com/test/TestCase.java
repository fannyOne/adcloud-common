package com.test;

import javax.annotation.Resource;

import org.junit.Test;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.asiainfo.cache.CacheTemplate;

public class TestCase {
	
	@Test
	public void test01(){
		ClassPathXmlApplicationContext appCtx = new ClassPathXmlApplicationContext("spring-redis.xml");
        final RedisTemplate<String, Object> redisTemplate = appCtx.getBean("redisTemplate",RedisTemplate.class);
        //添加一个 key 
        ValueOperations<String, Object> value = redisTemplate.opsForValue();
        value.set("lp", "hello word");
        //获取 这个 key 的值
        System.out.println(value.get("lp"));
        value.set("用户名", "fanny");
        System.out.println(value.get("用户名"));
		
	}
}
