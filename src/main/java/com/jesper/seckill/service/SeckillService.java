package com.jesper.seckill.service;

import com.jesper.seckill.activemq.QueueSender;
import com.jesper.seckill.activemq.SeckillMessage;
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

/**
 *
 */
@Service
public class SeckillService {

    @Autowired
    RedisService redisService;
    @Autowired
    QueueSender queueSender;
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


    public boolean seckill(String fieldId,long goodsId,String siteNo,Long userId,String uuid,Integer seckillNum){
        String seckillKey = RedisKeyUtil.combineSeckillKey(fieldId,String.valueOf(goodsId),siteNo);

        seckillNum = seckillNum==null?1:seckillNum;
        //decr扣减库存（每次只能秒杀一个）
        /*long stock = redisService.decr(SeckillKey.seckillStock, seckillKey);//10
        if (stock < 0) {
            return false;
        }*/
        //watch扣减库存（支持设置秒杀数量）
        seckillKey = SeckillKey.seckillStock.getPrefix()+seckillKey;
        boolean result = seckillWatch(seckillKey,seckillNum);
        if(!result) {
            return result;
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
        message.setNumber(seckillNum);
        message.setSiteNo(siteNo);
        message.setId(uuid);
        queueSender.sendSeckillMessage(message);
        return true;
    }
    public  boolean seckillWatch(String key,Integer seckillNum){
        Jedis jedis = null;
        try {

            jedis = redisService.getJedisPool().getResource();

            while (true) {
                try {
                    jedis.watch(key);// key加上乐观锁
                    System.out.println("当前商品的个数：" + jedis.get(key));
                    int prdNum = Integer.parseInt(jedis.get(key));// 当前商品个数
                    if (prdNum > 0 && (prdNum-seckillNum)>=0) {

                        Transaction transaction = jedis.multi();// 标记一个事务块的开始
                        transaction.set(key, String.valueOf(prdNum - seckillNum));
                        List<Object> result = transaction.exec();// 原子性提交事物
                        if (result != null && !result.isEmpty()) {
                            System.out.println("用户:抢到商品");
                            return true;
                        }
                    } else {
                        System.out.println("库存为0，用户没有抢到商品");
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
