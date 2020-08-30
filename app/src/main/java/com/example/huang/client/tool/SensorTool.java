package com.example.huang.client.tool;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.example.huang.client.entity.Orientation;

import static android.content.Context.SENSOR_SERVICE;

/*
获取传感器姿态参数
 */
public class SensorTool {
    //方向传感器
    private static SensorManager orientationSensorManager;
    private static SensorEventListener orientationListener;
    private static Orientation orientationData;

    public static void initSensor(Context context) {
        orientationSensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        assert orientationSensorManager != null;
        Sensor orientationSensor = orientationSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        orientationListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                orientationData = new Orientation(event.values[0], event.values[1], event.values[2]);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
        orientationSensorManager.registerListener(orientationListener,
                orientationSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public static void stopSensor() {
        orientationSensorManager.unregisterListener(orientationListener);
    }

    public static Orientation getOrientation() {
        return orientationData;
    }
}




