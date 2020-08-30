package com.example.huang.client.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

public class ImageData extends Data implements Serializable {

    private LocalDateTime time;
    private Coordinate location;
    private Orientation orientation;
    private String uri;

    public ImageData() {
        super();
        setType(DataType.IMAGE_DATA);
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public Coordinate getLocation() {
        return location;
    }

    public void setLocation(Coordinate location) {
        this.location = location;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

}
