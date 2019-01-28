package com.jesper.seckill.controller;

import com.alibaba.fastjson.JSON;
import com.jesper.seckill.redis.RedisService;
import com.jesper.seckill.result.Result;
import com.jesper.seckill.vo.GoodsDetailVo;
import com.jesper.seckill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 */
@Controller
@RequestMapping("/goods")
public class GoodsController {


    @Autowired
    RedisService redisService;


    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    ThymeleafViewResolver thymeleafViewResolver;






    /**
     * 商品详情页面
     */
    @RequestMapping(value = "/detail/")
    @ResponseBody
    public Result<GoodsDetailVo> detail(HttpServletRequest request, HttpServletResponse response, Model model) {

        String s = "{\"endDate\":1526984580000,\"goodsDetail\":\"Apple/苹果iPhone X 全网通4G手机苹果X 10\",\"goodsImg\":\"/img/iphonex.png\",\"goodsName\":\"iphoneX\",\"goodsPrice\":7788.0,\"goodsStock\":100,\"goodsTitle\":\"Apple/苹果iPhone X 全网通4G手机苹果X 10\",\"id\":1,\"seckillPrice\":0.01,\"startDate\":1526980972000,\"stockCount\":5999,\"version\":5}";
        System.out.println(s);
        GoodsVo goods = JSON.toJavaObject(JSON.parseObject(s), GoodsVo.class);
        model.addAttribute("goods", goods);



        GoodsDetailVo vo = new GoodsDetailVo();
        vo.setGoods(goods);

        return Result.success(vo);
    }
}
