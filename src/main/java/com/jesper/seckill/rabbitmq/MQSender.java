package com.jesper.seckill.rabbitmq;

import com.jesper.seckill.activemq.ActiveMqName;
import com.jesper.seckill.activemq.QueueSender;
import com.jesper.seckill.redis.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by jiangyunxiong on 2018/5/29.
 */
@Service
public class MQSender {

    private static Logger log = LoggerFactory.getLogger(MQSender.class);

    @Autowired
    private QueueSender queueSender;


	public void sendSeckillMessage(SeckillMessage message){
        String msg = RedisService.beanToString(message);
        queueSender.send(ActiveMqName.QUEUE_ONE, msg);

    }
}
