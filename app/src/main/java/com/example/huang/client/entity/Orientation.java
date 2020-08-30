package com.example.huang.client.entity;

import java.io.Serializable;

public class Orientation implements Serializable {
    public double heading;     //偏航角，从正北方向开始顺时针旋转，到手机后置相机方向（从手机底部指向顶部）的夹角
    public double roll;    //滚转角，手机左侧抬高为负，右侧抬高为正；
    public double pitch;   //俯仰角，手机顶部抬高为负，底部抬高为正；

    public Orientation() {
        this.heading = 0d;
        this.roll = 0d;
        this.pitch = 0d;
    }

    public Orientation(double heading, double roll, double pitch) {
        this.heading = heading;
        this.roll = roll;
        this.pitch = pitch;
    }

    @Override
    public String toString() {
        return "(" + this.heading + "," + this.roll + "," + this.pitch + ")";
    }

    public void fromString(String dataStr) {
        try {
            String[] dataArray = dataStr.substring(1, dataStr.length() - 2).split(",");
            heading = Double.valueOf(dataArray[0]);
            roll = Double.valueOf(dataArray[1]);
            pitch = Double.valueOf(dataArray[2]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
