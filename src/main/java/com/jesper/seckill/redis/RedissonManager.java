package com.jesper.seckill.redis;

import org.redisson.Redisson;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by chenpeng on 2019/1/22.
 */
@Component
public class RedissonManager implements InitializingBean {
    @Autowired
    public RedisConfig redisConfig;

    public RedisConfig getRedisConfig() {
        return redisConfig;
    }

    public void setRedisConfig(RedisConfig redisConfig) {
        this.redisConfig = redisConfig;
    }

    private static Config config = new Config();
    private static RedissonClient redisson = null;


    public static RedissonClient getRedisson() {
        return redisson;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            config.useSingleServer()
                    .setAddress("redis://127.0.0.1:6380")
                    .setPassword(redisConfig.getPassword()).setConnectionPoolSize(500)//设置对于master节点的连接池中连接数最大为500
                    .setIdleConnectionTimeout(10000)//如果当前连接池里的连接数量超过了最小空闲连接数，而同时有连接空闲时间超过了该数值，那么这些连接将会自动被关闭，并从连接池里去掉。时间单位是毫秒。
                    .setConnectTimeout(30000)//同任何节点建立连接时的等待超时。时间单位是毫秒。
                    .setTimeout(3000)//等待节点回复命令的时间。该时间从命令发送成功时开始计时。
                    .setPingTimeout(30000)
                    .setReconnectionTimeout(3000); //当与某个节点的连接断开时，等待与其重新建立连接的时间间隔。时间单位是毫秒。
            redisson = Redisson.create(config);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
