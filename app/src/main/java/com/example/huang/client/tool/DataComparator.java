package com.example.huang.client.tool;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.huang.client.entity.Data;

import java.time.LocalDateTime;
import java.util.Comparator;

public class DataComparator implements Comparator<Data> {
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int compare(Data o1, Data o2) {
        LocalDateTime time1 = o1.getStartTime();
        LocalDateTime time2 = o2.getStartTime();

        if(time1 == null || time2 == null)
            return 0;

        if(time1.isBefore(time2))
            return 1;
        else if(time1.isAfter(time2))
            return -1;
        else
            return 0;
    }
}
