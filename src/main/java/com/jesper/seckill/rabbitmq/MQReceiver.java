package com.jesper.seckill.rabbitmq;

import com.jesper.seckill.activemq.ActiveMqName;
import com.jesper.seckill.redis.RedisService;
import com.jesper.seckill.redis.RedissonManager;
import com.jesper.seckill.redis.SeckillKey;
import com.jesper.seckill.redis.dto.SeckillStockDetail;
import com.jesper.seckill.service.SeckillService;
import com.jesper.seckill.util.RedisKeyUtil;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by jiangyunxiong on 2018/5/29.
 */
@Service
public class MQReceiver {

    private static Logger log = LoggerFactory.getLogger(MQReceiver.class);


    @Autowired
    RedisService redisService;


    @Autowired
    SeckillService seckillService;

    @JmsListener(destination =  ActiveMqName.QUEUE_ONE, containerFactory = "jmsListenerContainerQueue")
    public void receive(String message){
        SeckillMessage seckillMessage = RedisService.stringToBean(message, SeckillMessage.class);
        String seckillKey = RedisKeyUtil.combineSeckillKey(seckillMessage.getFieldId(),String.valueOf(seckillMessage.getGoodsId()),seckillMessage.getSiteNo());
        RedissonClient redissonClient = RedissonManager.getRedisson();
        RLock rLock = redissonClient.getLock(seckillKey);
        try {
            Long begin = System.currentTimeMillis();
            boolean result = rLock.tryLock(5L,20L, TimeUnit.SECONDS);
            Long end = System.currentTimeMillis();
            System.out.println("获得锁的时间："+(end-begin));
            SeckillStockDetail seckillStockDetail = redisService.get(SeckillKey.seckillStockDetail, seckillKey, SeckillStockDetail.class);
            if(result && (seckillStockDetail.getTsStock()+seckillStockDetail.getWmsStock())>=seckillMessage.getNumber()) {

                if(seckillStockDetail.getTsStock()>=seckillMessage.getNumber()) {
                    seckillMessage.setTsStock(seckillMessage.getNumber());
                    seckillStockDetail.setTsStock(seckillStockDetail.getTsStock()-seckillMessage.getTsStock());
                } else {
                    seckillMessage.setTsStock(seckillStockDetail.getTsStock());
                    seckillMessage.setWmsStock(seckillMessage.getNumber()-seckillMessage.getTsStock());
                    seckillStockDetail.setTsStock(0);
                    seckillStockDetail.setWmsStock(seckillStockDetail.getWmsStock()-seckillMessage.getWmsStock());
                }
                redisService.set(SeckillKey.seckillStockDetail, seckillKey,seckillStockDetail);

                List<SeckillMessage> list = redisService.getJsonList(SeckillKey.seckillRecord, seckillKey, SeckillMessage.class);
                if(list==null) {
                    list = new ArrayList<SeckillMessage>();
                }
                list.add(seckillMessage);
                redisService.setJsonList(SeckillKey.seckillRecord, seckillKey,list);

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            rLock.unlock();
        }

    }
}
