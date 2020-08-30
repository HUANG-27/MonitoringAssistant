package com.example.huang.client.entity;

import java.time.LocalDateTime;
import java.util.List;

public class AudioData extends Data {

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String uri;
    private List<Coordinate> locations; //每5秒采集一次位置信息

    public AudioData() {
        super();
        setType(DataType.AUDIO_DATA);
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public List<Coordinate> getLocations() {
        return locations;
    }

    public void setLocations(List<Coordinate> locations) {
        this.locations = locations;
    }

}
