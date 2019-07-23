package com.kehui.www.testapp.database;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;

/**
 * @author 29062
 * @date 2016/11/09
 */
@Entity
public class ChartInfo {
    private int id;
    private float cichang;
    private float shengyin;
    private float shengyinmemory;
    private float line;
    public float getLine() {
        return this.line;
    }
    public void setLine(float line) {
        this.line = line;
    }
    public float getShengyinmemory() {
        return this.shengyinmemory;
    }
    public void setShengyinmemory(float shengyinmemory) {
        this.shengyinmemory = shengyinmemory;
    }
    public float getShengyin() {
        return this.shengyin;
    }
    public void setShengyin(float shengyin) {
        this.shengyin = shengyin;
    }
    public float getCichang() {
        return this.cichang;
    }
    public void setCichang(float cichang) {
        this.cichang = cichang;
    }
    public int getId() {
        return this.id;
    }
    public void setId(int id) {
        this.id = id;
    }
    @Generated(hash = 1113439654)
    public ChartInfo(int id, float cichang, float shengyin, float shengyinmemory,
            float line) {
        this.id = id;
        this.cichang = cichang;
        this.shengyin = shengyin;
        this.shengyinmemory = shengyinmemory;
        this.line = line;
    }
    @Generated(hash = 617513573)
    public ChartInfo() {
    }
}
