package com.example.huang.client.tool;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;

import com.alibaba.fastjson.JSONObject;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.Point;
import com.example.huang.client.config.App2;
import com.example.huang.client.entity.Coordinate;
import com.example.huang.client.fragment.MapFragment;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class LocateTool {
    private static Location gpsLocation;
    public static Coordinate myLocation;
//    public static GeoPoint tiandituLocation;
//    private static TGeoDecode geoDecode;
//    private static TGeoAddress geoAddress;
    private static boolean isFirstLocate = true;
    private static Context context;

    public static void initLocationClient(final Context context) {

        LocateTool.context = context;

        //检查设备和权限
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        //逆地理编码——高德地图API
        //TODO

        //获取android定位控制
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        //为获取地理位置信息时设置查询条件
        assert locationManager != null;
        String bestProvider = locationManager.getBestProvider(getCriteria(), true);

        //获取位置，每1s更新一次，变化量超过1m更新一次
        assert bestProvider != null;
        locationManager.requestLocationUpdates(bestProvider, 1000, 1.0f,
                new LocationListener() {
            @Override
            public void onLocationChanged(Location currentLocation) {
                try {
                    //记录位置信息
                    gpsLocation = currentLocation;
                    myLocation = new Coordinate(gpsLocation.getLatitude(),
                            gpsLocation.getLongitude(), gpsLocation.getAltitude());

                    //TODO-逆地理编码

                    //
                    if(isFirstLocate){
                        isFirstLocate = false;
                        Point point = new Point(LocateTool.myLocation.getLon(), LocateTool.myLocation.getLat(), MapFragment.spatialReferenceLL);
                        Point proPoint = (Point) GeometryEngine.project(point, MapFragment.spatialReferencePro);
                        MapFragment.mapView.setViewpointCenterAsync(proPoint);
                        MapFragment.mapView.setViewpointScaleAsync(300000);
                    }

                    //上传位置数据
                    if(App2.monitor != null){
                        App2.monitor.setLocation(myLocation);
                        OkHttpClient client = new OkHttpClient();
                        FormBody formBody = new FormBody.Builder()
                                .add("id", App2.monitor.getId().toString())
                                .add("location", JSONObject.toJSONString(App2.monitor.getLocation()))
                                .build();
                        Request request = new Request.Builder()
                                .url(App2.serverURL + "/update-location")
                                .post(formBody)
                                .build();
                        client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                //什么都不做
                            }

                            @Override
                            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                                //什么都不做
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            //GPS状态变化时触发
            public void onStatusChanged(String provider, int status, Bundle extras) {
                switch (status) {
                    // GPS状态为可见时
                    case LocationProvider.AVAILABLE:
                        break;
                    // GPS状态为服务区外时
                    case LocationProvider.OUT_OF_SERVICE:
                        break;
                    // GPS状态为暂停服务时
                    case LocationProvider.TEMPORARILY_UNAVAILABLE:
                        break;
                }
            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        });
    }

    //GPS定位配置
    private static Criteria getCriteria() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE); // 高精度

        criteria.setAltitudeRequired(true);
        criteria.setHorizontalAccuracy(Criteria.ACCURACY_FINE);
        criteria.setVerticalAccuracy(Criteria.ACCURACY_FINE);

        criteria.setBearingRequired(true);
        criteria.setBearingAccuracy(Criteria.ACCURACY_FINE);

        criteria.setSpeedRequired(true);
        criteria.setSpeedAccuracy(Criteria.ACCURACY_FINE);

        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);
        return criteria;
    }
}
