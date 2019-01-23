package com.jesper.seckill.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenpeng on 2019/1/22.
 */
public class RedisKeyUtil {
    public static String combineSeckillKey(String seckillFieldId,String goodsId,String siteNo) {
        List<String> keyElement = new ArrayList<>(3);
        keyElement.add(seckillFieldId);
        keyElement.add(goodsId);
        keyElement.add(siteNo);
        return String.join(":", keyElement);
    }
}
