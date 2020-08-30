package com.example.huang.client.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSONArray;
import com.example.huang.client.adapter.DataListAdapter;

import com.example.huang.client.R;
import com.example.huang.client.entity.AudioData;
import com.example.huang.client.entity.DataType;
import com.example.huang.client.entity.ImageData;
import com.example.huang.client.entity.TextData;
import com.example.huang.client.entity.VideoData;
import com.example.huang.client.config.App2;
import com.example.huang.client.tool.DataComparator;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TargetDataActivity extends AppCompatActivity {

    ListView listViewDataList;
    private DataListAdapter dataListAdapter;
    private Timer timer;


    private final int REQUEST_TEXT = 0;
    private final int REQUEST_AUDIO = 1;
    private final int REQUEST_IMAGE = 2;
    private final int REQUEST_VIDEO = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_target_data);
        initView();
        cycleDownloadTargetData();
    }

    @Override
    protected void onDestroy(){
        timer.cancel();
        timer = null;
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_TEXT:
                    try {
                        if(App2.uploadData.getType() != DataType.TEXT_DATA)
                            return;
                        TextData textData = (TextData) App2.uploadData;
                        upload(textData);
                        dataListAdapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(TargetDataActivity.this, "数据上传失败！", Toast.LENGTH_LONG).show();
                    }
                    break;
                case REQUEST_IMAGE:
                    try {
                        if(App2.uploadData.getType() != DataType.IMAGE_DATA)
                            return;
                        ImageData imageData = (ImageData) App2.uploadData;
                        upload(imageData);
                        dataListAdapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(TargetDataActivity.this, "数据上传失败！", Toast.LENGTH_LONG).show();
                    }
                    break;
                case REQUEST_AUDIO:
                    try {
                        if(App2.uploadData.getType() != DataType.AUDIO_DATA)
                            return;
                        AudioData audioData = (AudioData) App2.uploadData;
                        //TODO
                        dataListAdapter.notifyDataSetChanged();

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(TargetDataActivity.this, "数据上传失败！", Toast.LENGTH_LONG).show();
                    }
                    break;
                case REQUEST_VIDEO:
                    try {
                        if(App2.uploadData.getType() != DataType.VIDEO_DATA)
                            return;
                        VideoData videoData = (VideoData) App2.uploadData;
                        //TODO
                        dataListAdapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(TargetDataActivity.this, "数据上传失败！", Toast.LENGTH_LONG).show();
                    }
                    break;
            }
        }
    }

    private void initView() {
        ImageButton imageButtonCancel = (ImageButton) findViewById(R.id.imageButton_getDataCancel);
        imageButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        TextView textViewTargetName = (TextView) findViewById(R.id.textView_getDataTargetName);
        textViewTargetName.setText(App2.focusTarget.getName());

        ImageButton imageButtonLoadTarget = (ImageButton) findViewById(R.id.button_load_target_info);
        imageButtonLoadTarget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TargetDataActivity.this, TargetInfoActivity.class);
                startActivity(intent);
            }
        });
        listViewDataList = (ListView) findViewById(R.id.listView_dataList);
        dataListAdapter = new DataListAdapter(TargetDataActivity.this);
        listViewDataList.setAdapter(dataListAdapter);

        LinearLayout layoutUploadText = (LinearLayout) findViewById(R.id.layout_uploadText);
        layoutUploadText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TargetDataActivity.this, TextActivity.class);
                startActivityForResult(intent, REQUEST_TEXT);
            }
        });
        LinearLayout layoutUploadAudio = (LinearLayout) findViewById(R.id.layout_uploadAudio);
        layoutUploadAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TargetDataActivity.this, AudioActivity.class);
                startActivityForResult(intent, REQUEST_AUDIO);
            }
        });
        LinearLayout layoutUploadImage = (LinearLayout) findViewById(R.id.layout_uploadImage);
        layoutUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TargetDataActivity.this, ImageActivity.class);
                startActivityForResult(intent, REQUEST_IMAGE);
            }
        });
        LinearLayout layoutUploadVideo = (LinearLayout) findViewById(R.id.layout_uploadVideo);
        layoutUploadVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TargetDataActivity.this, VideoActivity.class);
                startActivityForResult(intent, REQUEST_VIDEO);
            }
        });
    }

    private void cycleDownloadTargetData(){
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void run() {
                int size = App2.dataList.size();
                App2.dataList.clear();;
                if(App2.focusTarget != null){
                    getTextData();
                    getImageData();
                    getAudioData();
                    getVideoData();
                }
                if(App2.dataList.size() > size){
                    dataListAdapter.notifyDataSetChanged();
                    listViewDataList.smoothScrollToPosition(listViewDataList.getLastVisiblePosition());
                }
            }
        }, 0, 5000);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void getTextData() {
        try{
            OkHttpClient client = new OkHttpClient();
            FormBody formBody = new FormBody.Builder()
                    .add("targetId", App2.focusTarget.getId().toString())
                    .build();
            Request request = new Request.Builder()
                    .url(App2.serverURL + "/text/list")
                    .post(formBody)
                    .build();
            Response response = client.newCall(request).execute();
            if(response.body() != null){
                List<TextData> textDataList = JSONArray.parseArray(response.body().string(), TextData.class);
                App2.dataList.addAll(textDataList);
                App2.dataList.sort(new DataComparator());
            }
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void getImageData(){
        try{
            OkHttpClient client = new OkHttpClient();
            FormBody formBody = new FormBody.Builder()
                    .add("targetId", App2.focusTarget.getId().toString())
                    .build();
            Request request = new Request.Builder()
                    .url(App2.serverURL + "/image/list")
                    .post(formBody)
                    .build();
            Response response = client.newCall(request).execute();
            if(response.body() != null){
                List<ImageData> imageDataList = JSONArray.parseArray(response.body().string(), ImageData.class);
                App2.dataList.addAll(imageDataList);
                App2.dataList.sort(new DataComparator());
            }
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void getAudioData(){
        try{
            OkHttpClient client = new OkHttpClient();
            FormBody formBody = new FormBody.Builder()
                    .add("targetId", App2.focusTarget.getId().toString())
                    .build();
            Request request = new Request.Builder()
                    .url(App2.serverURL + "/audio/list")
                    .post(formBody)
                    .build();
            Response response = client.newCall(request).execute();
            if(response.body() != null){
                List<AudioData> audioDataList = JSONArray.parseArray(response.body().string(), AudioData.class);
                App2.dataList.addAll(audioDataList);
                App2.dataList.sort(new DataComparator());
            }
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void getVideoData(){
        try{
            OkHttpClient client = new OkHttpClient();
            FormBody formBody = new FormBody.Builder()
                    .add("targetId", App2.focusTarget.getId().toString())
                    .build();
            Request request = new Request.Builder()
                    .url(App2.serverURL + "/video/list")
                    .post(formBody)
                    .build();
            Response response = client.newCall(request).execute();
            if(response.body() != null){
                List<VideoData> videoDataList = JSONArray.parseArray(response.body().string(), VideoData.class);
                App2.dataList.addAll(videoDataList);
                App2.dataList.sort(new DataComparator());
            }
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }

    private void upload(TextData textData){

    }

    private void upload(ImageData imageData){

    }
}

