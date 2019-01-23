package com.jesper.seckill.vo;

import java.util.Date;
import java.util.List;

/**
 * Created by chenpeng on 2019/1/22.
 */
public class SeckillField {
    private String id;
    private String name;
    private Date startTime;
    private Date endTime;

    public SeckillField(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

}
