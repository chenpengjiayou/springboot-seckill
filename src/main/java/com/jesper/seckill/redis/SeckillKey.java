package com.jesper.seckill.redis;

/**
 *
 */
public class SeckillKey extends BasePrefix {
    private SeckillKey(String prefix) {
        super(prefix);
    }

    public static SeckillKey isGoodsOver = new SeckillKey("go");

    public static SeckillKey seckillStock = new SeckillKey("stock");

    public static SeckillKey seckillStockDetail = new SeckillKey("stockDetail");

    public static SeckillKey seckillRecord = new SeckillKey("record");
}
