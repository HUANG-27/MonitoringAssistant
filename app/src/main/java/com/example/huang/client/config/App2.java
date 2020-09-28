package com.example.huang.client.config;

import android.graphics.Bitmap;
import android.os.Environment;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.huang.client.entity.Data;
import com.example.huang.client.entity.Monitor;
import com.example.huang.client.entity.Target;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.example.huang.client.tool.JSONTool;

import org.jetbrains.annotations.NotNull;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class App2 {

    //App文件夹

    public static String appFolder = Environment.getExternalStorageDirectory().getAbsolutePath()
            + File.separatorChar + "MonitoringAssistant";

    public static String appDataFolder = appFolder + File.separatorChar + "data";

    //Server URL
    public static String serverURL = "http://192.168.1.106:8082";

    //用户
    public static Monitor monitor;

    //目标列表
    public static List<Target> targetList = new ArrayList<>();
    //选中目标
    public static Target focusTarget = null;
    //数据列表
    public static List<Data> dataList = new ArrayList<>();
    //待上传数据
    public static Data uploadData = null;

    public static Bitmap tempImage = null;

    public static void addUnknownTarget(){
        Target unknownTarget = new Target();
        unknownTarget.setId(-1);
        unknownTarget.setName("Unknown");
        unknownTarget.setIdNumber("000000000000000000");
        targetList.add(unknownTarget);
    }


    //创建文件夹
    public static void createAppFolder() {
        try {
            File folder = new File(appFolder);
            if (!folder.exists()) {
                folder.mkdir();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void autoLogin() {
        try {
            RegisterInfo registerInfo = RegisterInfoIO.readRegisterInfo();
            if(registerInfo == null)
                return;

            if(registerInfo.isRememberId && registerInfo.isRememberPwd){
                //登录
                OkHttpClient client = new OkHttpClient();
                FormBody formBody = new FormBody.Builder()
                        .add("idNumber", registerInfo.id)
                        .add("password", registerInfo.pwd)
                        .build();
                Request request = new Request.Builder()
                        .url(App2.serverURL + "/login")
                        .post(formBody)
                        .build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {

                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        if(response.body() != null)
                            App2.monitor = JSONObject.parseObject(response.body().string(), Monitor.class);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void autoLogout(){
        try{
            if(App2.monitor == null)
                return;
            OkHttpClient client = new OkHttpClient();
            FormBody formBody = new FormBody.Builder()
                    .add("id", App2.monitor.getId().toString())
                    .build();
            Request request = new Request.Builder()
                    .url(App2.serverURL + "/logout")
                    .post(formBody)
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    //什么都不做
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    Boolean res = Boolean.valueOf(response.body().string());
                    if(res){
                        App2.monitor = null;
                    }
                }
            });
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public static void cycleUpdate(){
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateMonitor();
                updateTarget();
            }
        }, 0, 1000);
    }

    private static void updateMonitor(){
        //monitor
        if(App2.monitor == null)
            return;
        OkHttpClient client = new OkHttpClient();
        FormBody formBody = new FormBody.Builder()
                .add("id", App2.monitor.getId().toString())
                .build();
        Request request = new Request.Builder()
                .url(App2.serverURL + "/get")
                .post(formBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                //什么都不做
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if(response.body() == null)
                    return;
                App2.monitor = JSONObject.parseObject(JSONTool.filterJSONString(response.body().string()), Monitor.class);
//                if(App2.monitor.getMission() != null){
//                    App2.targetList = new ArrayList<>();
//                    App2.targetList.add(App2.monitor.getMission().getTarget());
//                }
            }
        });

        //target

    }

    private static void updateTarget(){
        //target
        if(App2.monitor == null)
            return;
        if(App2.monitor.getMission()!=null){
            App2.targetList = new ArrayList<>();
            App2.targetList.addAll(App2.monitor.getMission().getTargets());
        }
        //下载
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(App2.serverURL + "/target/list")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                //什么都不做
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if(response.body() == null)
                    return;
                App2.targetList = JSONArray.parseArray(JSONTool.filterJSONString(response.body().string()), Target.class);
            }
        });
    }
}
