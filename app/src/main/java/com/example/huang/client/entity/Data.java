package com.example.huang.client.entity;

import java.io.Serializable;

public class Data implements Serializable {

    private Integer id;
    private String fileName;
    private DataType type;
    private Target target;   //数据表述的目标对象
    private Monitor monitor; //数据采集人
    private String description;

    public Data() {
        this.setType(DataType.DEFAULT);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public DataType getType() {
        return type;
    }

    public void setType(DataType type) {
        this.type = type;
    }

    public Target getTarget() {
        return target;
    }

    public void setTarget(Target target) {
        this.target = target;
    }

    public Monitor getMonitor() {
        return monitor;
    }

    public void setMonitor(Monitor monitor) {
        this.monitor = monitor;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
