package com.example.huang.client.entity;

import java.util.ArrayList;
import java.util.List;

public enum MissionType {
    围捕,
    搜索;
    public static List<String> toList(){
        MissionType[] missionTypes = MissionType.values();
        List<String> strMissions = new ArrayList<>();
        for(MissionType missionType : missionTypes){
            strMissions.add(missionType.name());
        }
        return strMissions;
    }
}
