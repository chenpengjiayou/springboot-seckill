package com.jesper.seckill.activemq;

import com.jesper.seckill.redis.RedisService;
import org.apache.activemq.ScheduledMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

@Service
public class QueueSender {

    @Autowired
    @Qualifier("jmsQueueTemplate")
    private JmsTemplate jmsTemplate;

    public void send(String queueName, final String message) {
        this.send(queueName,message,0);
    }

    public void send(String queueName, final String message,long time) {
        jmsTemplate.send(queueName, new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {

                TextMessage a = session.createTextMessage(message);
                if(time>0) {
                    a.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY, time);
                }
                return a;
            }
        });
    }
    public void sendSeckillMessage(SeckillMessage message){
        String msg = RedisService.beanToString(message);
        send(ActiveMqName.QUEUE_ONE, msg);

    }
}
