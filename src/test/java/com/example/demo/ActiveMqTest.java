package com.example.demo;

import com.jesper.seckill.MainApplication;
import com.jesper.seckill.activemq.ActiveMqName;
import com.jesper.seckill.activemq.QueueSender;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created by chenpeng on 2019/1/25.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MainApplication.class)
public class ActiveMqTest {
    @Autowired
    private QueueSender queueSender;
    @Test
    public void testSend(){
        for(int i=0;i<2;i++) {
            if(i==0) {
                queueSender.send(ActiveMqName.QUEUE_ONE, "success1",10000);
            } else {
                queueSender.send(ActiveMqName.QUEUE_ONE, "success2",5000);
            }
        }
    }
}
