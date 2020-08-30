package com.example.huang.client.tool;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.huang.client.entity.AudioData;
import com.example.huang.client.entity.Data;
import com.example.huang.client.entity.DataType;
import com.example.huang.client.entity.ImageData;
import com.example.huang.client.entity.TextData;
import com.example.huang.client.entity.VideoData;

import org.w3c.dom.Text;

import java.time.LocalDateTime;
import java.util.Comparator;

public class DataComparator implements Comparator<Data> {
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int compare(Data o1, Data o2) {
        LocalDateTime time1 = getDataTime(o1);
        LocalDateTime time2 = getDataTime(o2);

        if(time1 == null || time2 == null)
            return 0;

        if(time1.isBefore(time2))
            return 1;
        else if(time1.isAfter(time2))
            return -1;
        else
            return 0;
    }

    private LocalDateTime getDataTime(Data data){
        switch (data.getType()){
            case TEXT_DATA:
                return ((TextData)data).getTime();
            case IMAGE_DATA:
                return ((ImageData)data).getTime();
            case AUDIO_DATA:
                return ((AudioData)data).getStartTime();
            case VIDEO_DATA:
                return ((VideoData)data).getStartTime();
            default:
                return null;
        }
    }
}
