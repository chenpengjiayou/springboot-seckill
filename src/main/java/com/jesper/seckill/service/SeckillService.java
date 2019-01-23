package com.jesper.seckill.service;

import com.jesper.seckill.bean.OrderInfo;
import com.jesper.seckill.bean.SeckillOrder;
import com.jesper.seckill.bean.User;
import com.jesper.seckill.rabbitmq.MQSender;
import com.jesper.seckill.rabbitmq.SeckillMessage;
import com.jesper.seckill.redis.RedisService;
import com.jesper.seckill.redis.SeckillKey;
import com.jesper.seckill.redis.dto.SeckillStockDetail;
import com.jesper.seckill.result.CodeMsg;
import com.jesper.seckill.result.Result;
import com.jesper.seckill.util.RedisKeyUtil;
import com.jesper.seckill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by jiangyunxiong on 2018/5/23.
 */
@Service
public class SeckillService {

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    RedisService redisService;
    @Autowired
    MQSender sender;

    public void prepare(String fieldId,String siteNo){
        List<GoodsVo> goods = goodsService.listGoodsVo();

        for(GoodsVo goodsVo:goods) {
            int seckillTsStock = 10;
            int seckillWmsStock = 50;

            String seckillKey = RedisKeyUtil.combineSeckillKey(fieldId,goodsVo.getId().toString(),siteNo);
            redisService.set(SeckillKey.seckillStock, seckillKey, seckillTsStock + seckillWmsStock);

            SeckillStockDetail seckillStockDetail = new SeckillStockDetail();
            seckillStockDetail.setTsStock(seckillTsStock);
            seckillStockDetail.setWmsStock(seckillWmsStock);
            redisService.set(SeckillKey.seckillStockDetail, seckillKey, seckillStockDetail);
            redisService.delete(SeckillKey.seckillRecord,seckillKey);
            redisService.delete(SeckillKey.isGoodsOver,seckillKey);
        }
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

    public long getSeckillResult(long userId, long goodsId){
        SeckillOrder order = orderService.getOrderByUserIdGoodsId(userId, goodsId);
        if (order != null){
            return order.getOrderId();
        }else{
            boolean isOver = getGoodsOver(goodsId);
            if(isOver) {
                return -1;
            }else {
                return 0;
            }
        }
    }

    private void setGoodsOver(Long goodsId) {
        redisService.set(SeckillKey.isGoodsOver, ""+goodsId, true);
    }

    private boolean getGoodsOver(long goodsId) {
        return redisService.exists(SeckillKey.isGoodsOver, ""+goodsId);
    }
}
