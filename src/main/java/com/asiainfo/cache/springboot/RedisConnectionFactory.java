package com.asiainfo.cache.springboot;

import com.asiainfo.cache.redis.Redis35HttpSessionConfig;
import org.springframework.session.data.redis.RedisFlushMode;

/**
 * Created by Administrator on 2016-07-13.
 */
//@Component
public class RedisConnectionFactory {

    // @Value("${spring.redis.host}")
    private String hostName;
    //@Value("${spring.redis.port}")
    private int port;

    public void afterPropertySet() {
        Redis35HttpSessionConfig config = new Redis35HttpSessionConfig();
        config.setRedisFlushMode(RedisFlushMode.IMMEDIATE);
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

}
