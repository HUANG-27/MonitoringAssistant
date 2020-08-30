package com.example.huang.client.entity;

import java.time.LocalDateTime;

public class TextData extends Data {

    private String title;
    private String content;
    private LocalDateTime time;
    private Coordinate location;

    public TextData() {
        super();
        setType(DataType.TEXT_DATA);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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

}
