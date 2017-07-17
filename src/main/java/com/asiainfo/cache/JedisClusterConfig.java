package com.asiainfo.cache;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.stereotype.Component;

import redis.clients.jedis.HostAndPort;



import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by guojian on 01/12/2016.
 */

@Component(value = "jedisClusterConfig")
public class JedisClusterConfig {

    @Value("${spring.redis.hosts}")
    private String hosts;

    public String getHosts() {
        return hosts;
    }

    public boolean isCluster() {
        return hosts.contains(";");
    }

    public Set<RedisNode> getClusterNodes() {
        // 添加redis集群的节点
        String[] serverArray = hosts.split(";");//获取服务器数组(这里要相信配置正确，所以没有考虑空指针问题)
        Set<RedisNode> nodes = new HashSet<RedisNode>();
        for (String ipPort : serverArray) {
            String[] ipPortPair = ipPort.split(":");
            RedisNode node = new RedisNode(ipPortPair[0].trim(), Integer.valueOf(ipPortPair[1].trim()));
            nodes.add(node);
        }
        return nodes;
    }

    /**
     * JedisPoolConfig 配置
     * <p>
     * 配置JedisPoolConfig的各项属性
     *
     * @return
     */
    public JedisPoolConfig jedisPoolConfig() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        //连接耗尽时是否阻塞, false报异常,ture阻塞直到超时, 默认true
        jedisPoolConfig.setBlockWhenExhausted(true);

        //是否启用pool的jmx管理功能, 默认true
        jedisPoolConfig.setJmxEnabled(true);

        //默认就好
        //jedisPoolConfig.setJmxNamePrefix("pool");

        //jedis调用returnObject方法时，是否进行有效检查
        jedisPoolConfig.setTestOnReturn(true);

        //是否启用后进先出, 默认true
        jedisPoolConfig.setLifo(true);

        //最大空闲连接数, 默认8个
        jedisPoolConfig.setMaxIdle(8);

        //最大连接数, 默认8个
        jedisPoolConfig.setMaxTotal(8);

        //获取连接时的最大等待毫秒数(如果设置为阻塞时BlockWhenExhausted),如果超时就抛异常, 小于零:阻塞不确定的时间,  默认-1
        jedisPoolConfig.setMaxWaitMillis(-1);

        //逐出连接的最小空闲时间 默认1800000毫秒(30分钟)
        jedisPoolConfig.setMinEvictableIdleTimeMillis(1800000);

        //最小空闲连接数, 默认0
        jedisPoolConfig.setMinIdle(0);

        //每次逐出检查时 逐出的最大数目 如果为负数就是 : 1/abs(n), 默认3
        jedisPoolConfig.setNumTestsPerEvictionRun(3);

        //对象空闲多久后逐出, 当空闲时间>该值 且 空闲连接>最大空闲数 时直接逐出,不再根据MinEvictableIdleTimeMillis判断  (默认逐出策略)
        jedisPoolConfig.setSoftMinEvictableIdleTimeMillis(1800000);

        //在获取连接的时候检查有效性, 默认false
        jedisPoolConfig.setTestOnBorrow(false);

        //在空闲时检查有效性, 默认false
        jedisPoolConfig.setTestWhileIdle(false);

        //逐出扫描的时间间隔(毫秒) 如果为负数,则不运行逐出线程, 默认-1
        jedisPoolConfig.setTimeBetweenEvictionRunsMillis(-1);
        return jedisPoolConfig;
    }

    public JedisCluster getJedisCluster() {
        String[] serverArray = hosts.split(";");//获取服务器数组(这里要相信配置正确，所以没有考虑空指针问题)
        Set<HostAndPort> nodes = new HashSet<HostAndPort>();
        for (String ipPort : serverArray) {
            String[] ipPortPair = ipPort.split(":");
            nodes.add(new HostAndPort(ipPortPair[0].trim(), Integer.valueOf(ipPortPair[1].trim())));
        }
        return new JedisCluster(nodes, 15);
    }
}
