package com.jesper.seckill.controller;

import com.google.common.util.concurrent.RateLimiter;
import com.jesper.seckill.bean.User;
import com.jesper.seckill.rabbitmq.SeckillMessage;
import com.jesper.seckill.redis.RedisService;
import com.jesper.seckill.redis.SeckillKey;
import com.jesper.seckill.result.CodeMsg;
import com.jesper.seckill.result.Result;
import com.jesper.seckill.service.SeckillService;
import com.jesper.seckill.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by jiangyunxiong on 2018/5/22.
 */
@Controller
@RequestMapping("/seckill")
public class SeckillController  {

    @Autowired
    SeckillService seckillService;

    @Autowired
    RedisService redisService;



    public final static String siteNo = "ZTD000001";
    public final static String fieldId = "1";
    public final static Long goodsId = 1L;
    //基于令牌桶算法的限流实现类
    RateLimiter rateLimiter = RateLimiter.create(500);

    //做标记，判断该商品是否被处理过了
    private HashMap<String, Boolean> localOverMap = new HashMap<String, Boolean>();
    @RequestMapping(value = "/prepare", method = RequestMethod.GET)
    @ResponseBody
    public Result<Integer> prepare() {
        localOverMap.clear();
        seckillService.prepare(fieldId,goodsId.toString(),siteNo);
        return Result.success(0);
    }




    /**
     * GET POST
     * 1、GET幂等,服务端获取数据，无论调用多少次结果都一样
     * 2、POST，向服务端提交数据，不是幂等
     * <p>
     * 将同步下单改为异步下单
     *
     * @param model
     * @param user
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "/do_seckill", method = RequestMethod.POST)
    @ResponseBody
    public Result<Integer> list(Model model, User user, @RequestParam("goodsId") long goodsId) {

        if (!rateLimiter.tryAcquire(1000, TimeUnit.MILLISECONDS)) {
            return  Result.error(CodeMsg.ACCESS_LIMIT_REACHED);
        }

        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        model.addAttribute("user", user);

        //内存标记，减少redis访问
        String seckillKey = RedisKeyUtil.combineSeckillKey(fieldId,String.valueOf(goodsId),siteNo);
        Boolean over = localOverMap.get(seckillKey);
        if (over!=null && over) {
            System.out.println("本地内存标记库存已售完");
            return Result.error(CodeMsg.SECKILL_OVER);
        }
        String uuid = UUID.randomUUID().toString();
        if(seckillService.seckill(fieldId,goodsId,siteNo,user.getId(),uuid)) {
            Result success = Result.success(0);
            success.setMsg(uuid);
            return success;
        } else {
            localOverMap.put(seckillKey, true);
            return Result.error(CodeMsg.SECKILL_OVER);
        }


    }


    @RequestMapping(value = "/seckill_all_result", method = RequestMethod.GET)
    @ResponseBody
    public Result<List<SeckillMessage>> list(@RequestParam("goodsId") long goodsId) {
        String seckillKey = RedisKeyUtil.combineSeckillKey(fieldId,String.valueOf(goodsId),siteNo);
        List<SeckillMessage> record = redisService.getJsonList(SeckillKey.seckillRecord,seckillKey, SeckillMessage.class);
        return Result.success(record);//排队中
    }


    /**
     * orderId：成功
     * -1：秒杀失败
     * 0： 排队中
     */
    @RequestMapping(value = "/result", method = RequestMethod.GET)
    @ResponseBody
    public Result<Integer> seckillResult(Model model,
                                        @RequestParam("goodsId") String goodsId, @RequestParam("uuid") String uuid) {
        String seckillKey = RedisKeyUtil.combineSeckillKey(fieldId, String.valueOf(goodsId), siteNo);
        List<SeckillMessage> record = redisService.getJsonList(SeckillKey.seckillRecord, seckillKey, SeckillMessage.class);
        if(record==null) {
            return Result.error(CodeMsg.ORDER_NOT_EXIST);
        }
        for (SeckillMessage seckillMessage : record) {
            if (uuid.equals(seckillMessage.getId())) {
                return Result.success(200);
            }
        }

        return Result.success(0);

    }

}
