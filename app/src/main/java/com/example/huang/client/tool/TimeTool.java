package com.example.huang.client.tool;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;

public class TimeTool {

    public static String formatDateTime(LocalDateTime time){
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return sdf.format(time);
    }

    public static String formatDateTime2(LocalDateTime time) {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(time);
    }
}
