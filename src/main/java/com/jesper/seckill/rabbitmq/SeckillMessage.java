package com.jesper.seckill.rabbitmq;

/**
 * Created by jiangyunxiong on 2018/5/29.
 *
 * 消息体
 */
public class SeckillMessage {


    private Long userId;
    private long goodsId;
    private String fieldId;
    private String siteNo;
    private Integer number;
    private Integer tsStock = 0;
    private Integer wmsStock = 0;
    private String id;


    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public long getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(long goodsId) {
        this.goodsId = goodsId;
    }

    public String getFieldId() {
        return fieldId;
    }

    public void setFieldId(String fieldId) {
        this.fieldId = fieldId;
    }

    public String getSiteNo() {
        return siteNo;
    }

    public void setSiteNo(String siteNo) {
        this.siteNo = siteNo;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
