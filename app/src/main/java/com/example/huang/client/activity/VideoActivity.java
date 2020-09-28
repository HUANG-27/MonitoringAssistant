package com.example.huang.client.activity;

import android.annotation.SuppressLint;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Bundle;

import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.VideoView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.huang.client.R;
import com.example.huang.client.entity.Data;
import com.example.huang.client.entity.Coordinate;
import com.example.huang.client.entity.Orientation;
import com.example.huang.client.tool.LocateTool;
import com.example.huang.client.tool.SensorTool;
import com.example.huang.client.config.App2;
import com.example.huang.client.tool.TimeTool;

public class VideoActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener {

    private LinearLayout layoutRecordVideo;
    private TextureView textureView;
    private ProgressBar progressBarRecordVideoDuration;
    private Chronometer chronometerRecordVideoDuration;
    private ImageButton imageButtonRecordVideo;

    private LinearLayout layoutPlayVideo;
    private VideoView videoView;

    private Camera camera;
    private int DEFAULT_CAMERA_ID = 0;

    private MediaRecorder mediaRecorder;
    private List<Coordinate> locations;
    private List<Orientation> orientations;
    private boolean isRecording = false;
    private final int RECORDING = 0;

    private static Data videoData;
    private static int MAX_DURATION = 3600 * 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        initView();
        textureView.setSurfaceTextureListener(this);
    }

    private void initView() {

        layoutRecordVideo = (LinearLayout) findViewById(R.id.layout_recordVideo);
        textureView = (TextureView) findViewById(R.id.texture_viewVideo);
        progressBarRecordVideoDuration = (ProgressBar) findViewById(R.id.processBar_recordVideoDuration);
        progressBarRecordVideoDuration.setProgress(0);
        chronometerRecordVideoDuration = (Chronometer) findViewById(R.id.chronometer_recordVideoDuration);
        layoutRecordVideo.setVisibility(View.VISIBLE);
        imageButtonRecordVideo = (ImageButton) findViewById(R.id.button_recordVideo);
        imageButtonRecordVideo.setBackgroundResource(R.drawable.icon_video_start_64);
        imageButtonRecordVideo.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                //正在录制，按下停止录制
                if (isRecording) {
                    //切换图标
                    imageButtonRecordVideo.setBackgroundResource(R.drawable.icon_video_start_64);
                    //结束录制
                    mediaRecorder.stop();
                    isRecording = false;
                    //记录结束时间
                    videoData.setEndTime(LocalDateTime.now());
                    //结束计时
                    chronometerRecordVideoDuration.stop();
                    chronometerRecordVideoDuration.setBase(SystemClock.elapsedRealtime());
                    //进度条设置
                    progressBarRecordVideoDuration.setProgress(0);
                    //跳转
                    layoutRecordVideo.setVisibility(View.INVISIBLE);
                    layoutPlayVideo.setVisibility(View.VISIBLE);
                    //加载录制的视频
                    videoView.setVideoURI(Uri.fromFile(
                            new File(videoData.getContent())));
                } else {
                    videoData = new Data();
                    videoData.setMonitor(App2.monitor);
                    videoData.setTarget(App2.focusTarget);
                    videoData.setStartTime(LocalDateTime.now());
                    videoData.setContent(App2.appDataFolder + File.separatorChar
                            + TimeTool.formatDateTime(videoData.getStartTime()) + ".mp4");
                    //位置数据
                    locations = new ArrayList<>();
                    videoData.setLocations(locations);
                    //姿态数据
                    orientations = new ArrayList<>();
                    videoData.setOrientations(orientations);
                    //切换图标
                    imageButtonRecordVideo.setBackgroundResource(R.drawable.icon_video_stop_64);
                    //开始录制
                    initMediaRecorder();
                    isRecording = true;
                    //开始计时
                    chronometerRecordVideoDuration.setBase(SystemClock.elapsedRealtime());
                    chronometerRecordVideoDuration.start();
                    //进度条开始
                    progressBarRecordVideoDuration.setProgress(0);
                    progressBarRecordVideoDuration.setMax(MAX_DURATION / 100);
                    handlerRecordVideo.sendEmptyMessage(RECORDING);
                }
            }
        });

        layoutPlayVideo = (LinearLayout) findViewById(R.id.layout_playVideo);
        layoutPlayVideo.setVisibility(View.INVISIBLE);
        videoView = (VideoView) findViewById(R.id.videoView_videoData);
        videoView.setMediaController(new MediaController(this));    //设置视频控制器
        Button buttonOK = (Button) findViewById(R.id.button_videoOk);
        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                App2.uploadData = videoData;
                setResult(RESULT_OK);
                finish();
            }
        });
        Button buttonCancel = (Button) findViewById(R.id.button_videoCancel);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //删除文件
                String filePath = videoData.getContent();
                File file = new File(filePath);
                if(file.exists())
                    file.delete();
                videoData = null;
                videoView.setVideoURI(null);
                layoutPlayVideo.setVisibility(View.INVISIBLE);
                layoutRecordVideo.setVisibility(View.VISIBLE);
            }
        });
    }

    private void initCamera(SurfaceTexture surface) {
        try {
            camera = Camera.open(DEFAULT_CAMERA_ID);
            Camera.Parameters parameters = camera.getParameters();
            //增加对聚焦模式的判断
            List<String> focusModesList = parameters.getSupportedFocusModes();
            if (focusModesList.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO))
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            else if (focusModesList.contains(Camera.Parameters.FOCUS_MODE_AUTO))
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

            int width = textureView.getWidth();
            int height = textureView.getHeight();
            List<Camera.Size> previewSizeList = parameters.getSupportedPreviewSizes();
            Camera.Size optionPreviewSize = getOptimalSize(previewSizeList, height, width);
            parameters.setPreviewSize(optionPreviewSize.width, optionPreviewSize.height);

            camera.setDisplayOrientation(90);
            camera.setPreviewTexture(surface);
            camera.setParameters(parameters);
            camera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void releaseCamera() {
        try {
            if (camera != null) {
                camera.stopPreview();
                camera.release();
                camera = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //计算最佳大小
    private Camera.Size getOptimalSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - h) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - h);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - h) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - h);
                }
            }
        }
        return optimalSize;
    }

    private void initMediaRecorder() {
        try {
            mediaRecorder = new MediaRecorder();
            camera.unlock();
            mediaRecorder.setCamera(camera);
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);//设置音频源
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);//设置视频源
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);//设置视频输出格式
            mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);//设置视频编码格式
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

            mediaRecorder.setVideoSize(1920, 1080);//设置视频分辨率
            mediaRecorder.setVideoFrameRate(20);//设置视频的帧率
            mediaRecorder.setOrientationHint(90);//设置视频的角度
            mediaRecorder.setMaxDuration(3600 * 1000);//设置最大录制时间
            //mediaRecorder.setPreviewDisplay(textureView);
            mediaRecorder.setOutputFile(videoData.getContent());//设置文件保存路径

            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void releaseMediaRecorder() {
        try {
            camera.lock();
            mediaRecorder.stop();
            mediaRecorder.reset();
            mediaRecorder.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler handlerRecordVideo = new Handler() {
        @SuppressLint("SetTextI18n")
        public void handleMessage(android.os.Message msg) {
            if (isRecording) {
                int progress = progressBarRecordVideoDuration.getProgress();
                if (progress < progressBarRecordVideoDuration.getMax()) {
                    progress += 1;
                    //记录每一帧的位置和姿态参数
                    locations.add(LocateTool.myLocation);
                    orientations.add(SensorTool.getOrientation());
                    handlerRecordVideo.sendEmptyMessageDelayed(RECORDING, 50);
                } else {
                    //到1h时自动结束并跳转
                    //切换图标
                    imageButtonRecordVideo.setBackgroundResource(R.drawable.icon_video_start_64);
                    //结束录制
                    releaseMediaRecorder();
                    isRecording = false;
                    //结束计时
                    chronometerRecordVideoDuration.stop();
                    chronometerRecordVideoDuration.setBase(SystemClock.elapsedRealtime());
                    //进度条设置
                    progressBarRecordVideoDuration.setProgress(0);
                    //切换至播放界面
                    layoutRecordVideo.setVisibility(View.INVISIBLE);
                    layoutPlayVideo.setVisibility(View.VISIBLE);
                    //加载录制的视频
                    videoView.setVideoPath(videoData.getContent());

                    handlerRecordVideo.removeMessages(RECORDING);
                }
                progressBarRecordVideoDuration.setProgress(progress);
            }
        }
    };

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        initCamera(surface);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        releaseMediaRecorder();
        releaseCamera();
        return false;
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

}
