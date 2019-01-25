package com.jesper.seckill.service;

import com.jesper.seckill.rabbitmq.MQSender;
import com.jesper.seckill.rabbitmq.SeckillMessage;
import com.jesper.seckill.redis.RedisService;
import com.jesper.seckill.redis.SeckillKey;
import com.jesper.seckill.redis.dto.SeckillStockDetail;
import com.jesper.seckill.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;

import java.util.List;
import java.util.UUID;

/**
 * Created by jiangyunxiong on 2018/5/23.
 */
@Service
public class SeckillService {

    @Autowired
    RedisService redisService;
    @Autowired
    MQSender sender;
    @Autowired
    JedisPool jedisPool;
    public void prepare(String fieldId,String goodsId,String siteNo){



            int seckillTsStock = 10;
            int seckillWmsStock = 50;

            String seckillKey = RedisKeyUtil.combineSeckillKey(fieldId,goodsId,siteNo);
            redisService.set(SeckillKey.seckillStock, seckillKey, seckillTsStock + seckillWmsStock);

            SeckillStockDetail seckillStockDetail = new SeckillStockDetail();
            seckillStockDetail.setTsStock(seckillTsStock);
            seckillStockDetail.setWmsStock(seckillWmsStock);
            redisService.set(SeckillKey.seckillStockDetail, seckillKey, seckillStockDetail);
            redisService.delete(SeckillKey.seckillRecord,seckillKey);
            redisService.delete(SeckillKey.isGoodsOver,seckillKey);
        }


    public boolean seckill(String fieldId,long goodsId,String siteNo,Long userId,String uuid){
        String seckillKey = RedisKeyUtil.combineSeckillKey(fieldId,String.valueOf(goodsId),siteNo);


        //预减库存
        long stock = redisService.decr(SeckillKey.seckillStock, seckillKey);//10
        if (stock < 0) {
            return false;
        }
        //判断重复秒杀
        /*SeckillOrder order = orderService.getOrderByUserIdGoodsId(user.getId(), goodsId);
        if (order != null) {
            return Result.error(CodeMsg.REPEATE_SECKILL);
        }*/
        //入队
        SeckillMessage message = new SeckillMessage();
        message.setUserId(userId);
        message.setGoodsId(goodsId);
        message.setFieldId(fieldId);
        message.setNumber(1);
        message.setSiteNo(siteNo);
        message.setId(uuid);
        sender.sendSeckillMessage(message);
        return true;
    }
    public  boolean seckillWatch(){
        Jedis jedis = null;
        try {

            jedis = jedisPool.getResource();

            String key_s = "user_name";// 抢到的用户
            String key = "test_count";// 商品数量
            String clientName = UUID.randomUUID().toString().replace("-", "");// 用户名字

            while (true) {
                try {
                    jedis.watch(key);// key加上乐观锁
                    System.out.println("用户:" + clientName + "开始抢商品");
                    System.out.println("当前商品的个数：" + jedis.get(key));
                    int prdNum = Integer.parseInt(jedis.get(key));// 当前商品个数
                    int seckillNum = 5;
                    if (prdNum > 0 && (prdNum-seckillNum)>=0) {

                        Transaction transaction = jedis.multi();// 标记一个事务块的开始
                        transaction.set(key, String.valueOf(prdNum - seckillNum));
                        List<Object> result = transaction.exec();// 原子性提交事物
                        if (result == null || result.isEmpty()) {
                            System.out.println("用户:" + clientName + "没有抢到商品");// 可能是watch-key被外部修改，或者是数据操作被驳回
                        } else {
                            jedis.sadd(key_s, clientName);// 将抢到的用户存起来
                            System.out.println("用户:" + clientName + "抢到商品");
                           return true;
                        }
                    } else {
                        System.out.println("库存为0，用户:" + clientName + "没有抢到商品");
                        return false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    jedis.unwatch();// exec，discard，unwatch命令都会清除连接中的所有监视

                }
            } // while
        } catch (Exception e) {
            System.out.println("redis bug:" + e.getMessage());
        } finally {
            // 释放jedis连接
            if (jedis != null) {
                jedis.close();//不是关闭，只是返回连接池
            }
        }
        return false;
    }

    private void setGoodsOver(Long goodsId) {
        redisService.set(SeckillKey.isGoodsOver, ""+goodsId, true);
    }

    private boolean getGoodsOver(long goodsId) {
        return redisService.exists(SeckillKey.isGoodsOver, ""+goodsId);
    }
}
