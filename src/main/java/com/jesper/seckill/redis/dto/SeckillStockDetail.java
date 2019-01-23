package com.jesper.seckill.redis.dto;

/**
 * Created by chenpeng on 2019/1/22.
 */
public class SeckillStockDetail {
    private Integer tsStock;
    private Integer wmsStock;

    public Integer getTsStock() {
        return tsStock;
    }

    public void setTsStock(Integer tsStock) {
        this.tsStock = tsStock;
    }

    public Integer getWmsStock() {
        return wmsStock;
    }

    public void setWmsStock(Integer wmsStock) {
        this.wmsStock = wmsStock;
    }
}
