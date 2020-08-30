package com.example.huang.client.entity;

import java.util.List;

public class Mission {

    private Integer id;
    private String name;
    private MissionType type;
    private List<Monitor> monitors;
    private Target target;
    private UAV uav;

    public Mission(){
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MissionType getType() {
        return type;
    }

    public void setType(MissionType type) {
        this.type = type;
    }

    public List<Monitor> getMonitors() {
        return monitors;
    }

    public void setMonitors(List<Monitor> monitors) {
        this.monitors = monitors;
    }

    public Target getTarget() {
        return target;
    }

    public void setTarget(Target target) {
        this.target = target;
    }

    public UAV getUav() {
        return uav;
    }

    public void setUav(UAV uav) {
        this.uav = uav;
    }
}
