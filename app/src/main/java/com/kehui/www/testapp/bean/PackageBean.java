package com.kehui.www.testapp.bean;

import java.util.Arrays;

/**
 * @author 29062
 * @date 2016/11/22
 */
public class PackageBean {

    private int SM;
    private int Mark;
    private int Index;
    private int[] Predsample;
    private int[] Date;
    private int[] Crc;

    public PackageBean() {

    }

    @Override
    public String toString() {
        return "PackageBean{" +
                "SM=" + SM +
                ", Mark=" + Mark +
                ", Index=" + Index +
                ", Predsample=" + Arrays.toString(Predsample) +
                ", Date=" + Arrays.toString(Date) +
                ", Crc=" + Arrays.toString(Crc) +
                '}';
    }

    public int getSM() {
        return SM;
    }

    public void setSM(int SM) {
        this.SM = SM;
    }

    public int getMark() {
        return Mark;
    }

    public void setMark(int mark) {
        Mark = mark;
    }

    public int getIndex() {
        return Index;
    }

    public void setIndex(int index) {
        Index = index;
    }

    public int[] getPredsample() {
        return Predsample;
    }

    public void setPredsample(int[] predsample) {
        Predsample = predsample;
    }

    public int[] getDate() {
        return Date;
    }

    public void setDate(int[] date) {
        Date = date;
    }

    public int[] getCrc() {
        return Crc;
    }

    public void setCrc(int[] crc) {
        Crc = crc;
    }

}
