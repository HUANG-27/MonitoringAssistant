package com.example.huang.client.activity;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.huang.client.R;
import com.example.huang.client.entity.AudioData;
import com.example.huang.client.entity.Coordinate;
import com.example.huang.client.tool.LocateTool;
import com.example.huang.client.config.App2;
import com.example.huang.client.tool.TimeTool;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AudioActivity extends AppCompatActivity {

    private LinearLayout layoutRecordAudio;
    private Chronometer chronometerRecordAudioDuration;
    private ProgressBar progressBarRecordAudioDuration;
    private ImageButton imageButtonRecordAudio;

    private LinearLayout layoutPlayAudio;
    private Chronometer chronometerPlayAudioDuration;
    private ProgressBar progressBarPlayAudioDuration;
    private TextView textViewAudioDuration;
    private ImageButton imageButtonPlayAudio;

    private MediaRecorder mediaRecorder;
    private List<Coordinate> locations;
    private boolean isRecording = false;
    private final int RECORDING = 0;

    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;
    private final int PLAYING = 1;

    private AudioData audioData;
    //最大时常2小时
    private final int MAX_DURATION = 3600 * 1000;    //单位：ms
    private static int AUDIO_DURATION = 0;          //单位：ms

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);
        initView();
    }

    private void initView() {
        layoutRecordAudio = findViewById(R.id.layout_recordAudio);
        layoutRecordAudio.setVisibility(View.VISIBLE);
        chronometerRecordAudioDuration = findViewById(R.id.chronometer_recordAudioDuration);
        chronometerRecordAudioDuration.setBase(SystemClock.elapsedRealtime());
        progressBarRecordAudioDuration = findViewById(R.id.processBar_recordAudioDuration);
        progressBarRecordAudioDuration.setProgress(0);
        imageButtonRecordAudio = findViewById(R.id.imageButton_recordAudio);
        imageButtonRecordAudio.setOnClickListener(new View.OnClickListener() {

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                //正在录制，按下停止录制
                if (isRecording) {
                    //切换图标
                    imageButtonRecordAudio.setBackgroundResource(R.drawable.icon_video_start_64);
                    //结束录制
                    mediaRecorder.stop();
                    isRecording = false;
                    //记录结束时间
                    audioData.setEndTime(LocalDateTime.now());
                    //结束计时
                    chronometerRecordAudioDuration.stop();
                    chronometerRecordAudioDuration.setBase(SystemClock.elapsedRealtime());
                    //进度条设置
                    progressBarRecordAudioDuration.setProgress(0);
                    //切换至播放界面，试听录音
                    layoutRecordAudio.setVisibility(View.INVISIBLE);
                    layoutPlayAudio.setVisibility(View.VISIBLE);
                    //加载音频
                    startMediaPlayer();
                    AUDIO_DURATION = mediaPlayer.getDuration();
                    textViewAudioDuration.setText(formatDuration(AUDIO_DURATION));
                } else {
                    //音频数据
                    audioData = new AudioData();
                    audioData.setMonitor(App2.monitor);
                    audioData.setTarget(App2.focusTarget);
                    audioData.setStartTime(LocalDateTime.now());
                    audioData.setFileName(App2.appDataFolder + File.separatorChar
                            + TimeTool.formatDateTime(audioData.getStartTime()) + ".mp4");
                    ////位置数据，记录位置的线程
                    locations = new ArrayList<>();
                    audioData.setLocations(locations);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if(isRecording){
                                try {
                                    locations.add(LocateTool.myLocation);
                                    Thread.sleep(50);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }).start();

                    //切换图标
                    imageButtonRecordAudio.setBackgroundResource(R.drawable.icon_video_stop_64);
                    //开始录制
                    startMediaRecorder();
                    isRecording = true;
                    //开始计时
                    chronometerRecordAudioDuration.setBase(SystemClock.elapsedRealtime());
                    chronometerRecordAudioDuration.start();
                    //进度条开始
                    progressBarRecordAudioDuration.setProgress(0);
                    progressBarRecordAudioDuration.setMax(MAX_DURATION / 50);
                    //设置录音Handler
                    handlerRecordAudio.sendEmptyMessage(RECORDING);
                }
            }
        });

        layoutPlayAudio = findViewById(R.id.layout_playAudio);
        layoutPlayAudio.setVisibility(View.INVISIBLE);
        chronometerPlayAudioDuration = findViewById(R.id.chronometer_playAudioDuration);
        chronometerPlayAudioDuration.setBase(SystemClock.elapsedRealtime());
        progressBarPlayAudioDuration = findViewById(R.id.processBar_playAudioDuration);
        progressBarPlayAudioDuration.setProgress(0);
        textViewAudioDuration = findViewById(R.id.textView_audioDuration);
        imageButtonPlayAudio = findViewById(R.id.imageButton_playAudio);
        imageButtonPlayAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //正在播放，按下停止播放
                if (isPlaying) {
                    //切换图标
                    imageButtonPlayAudio.setBackgroundResource(R.drawable.icon_play_red_64);
                    //停止播放
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                    mediaPlayer.pause();
                    isPlaying = false;
                    //结束计时
                    chronometerPlayAudioDuration.stop();
                    chronometerPlayAudioDuration.setBase(SystemClock.elapsedRealtime());
                    //进度条设置
                    progressBarPlayAudioDuration.setProgress(0);
                } else {
                    imageButtonPlayAudio.setBackgroundResource(R.drawable.icon_video_stop_64);
                    //开始播放
                    startMediaPlayer();
                    mediaPlayer.start();
                    isPlaying = true;
                    //进度条开始播放
                    progressBarPlayAudioDuration.setProgress(0);
                    progressBarPlayAudioDuration.setMax(AUDIO_DURATION / 50);
                    handlerPlayAudio.sendEmptyMessage(PLAYING);
                    //开始计时
                    chronometerPlayAudioDuration.setBase(SystemClock.elapsedRealtime());
                    chronometerPlayAudioDuration.start();
                }
            }
        });
        Button buttonOk = findViewById(R.id.button_audioOk);
        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                App2.uploadData = audioData;
                setResult(RESULT_OK);
                finish();
            }
        });
        Button buttonCancel = findViewById(R.id.button_audioCancel);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //删除文件
                File file = new File(audioData.getFileName());
                if (file.exists())
                    file.delete();
                audioData = null;
                //返回录音界面
                layoutRecordAudio.setVisibility(View.VISIBLE);
                layoutPlayAudio.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void startMediaRecorder() {
        if (mediaRecorder == null)
            mediaRecorder = new MediaRecorder();

        try {
            //使用麦克风
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            //文件格式
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            //编码格式
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            //最大时长
            mediaRecorder.setMaxDuration(MAX_DURATION);
            //文件存储
            mediaRecorder.setOutputFile(audioData.getFileName());

            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopMediaRecorder() {
        try {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;

        } catch (RuntimeException e) {
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

    private void startMediaPlayer() {
        if (mediaPlayer == null)
            mediaPlayer = new MediaPlayer();
        try {
            File file = new File(audioData.getFileName());
            mediaPlayer.setDataSource(file.getPath());
            mediaPlayer.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler handlerRecordAudio = new Handler() {
        @SuppressLint("SetTextI18n")
        public void handleMessage(@NonNull android.os.Message msg) {
            if (isRecording) {
                int progress = progressBarRecordAudioDuration.getProgress();
                if (progress < progressBarPlayAudioDuration.getMax()) {
                    progress += 1;
                    locations.add(LocateTool.myLocation);
                    handlerRecordAudio.sendEmptyMessageDelayed(RECORDING, 50);
                } else {
                    //到两小时自动结束并跳转
                    //切换图标
                    imageButtonRecordAudio.setBackgroundResource(R.drawable.icon_video_start_64);
                    //结束录制
                    stopMediaRecorder();
                    isRecording = false;
                    //结束计时
                    chronometerRecordAudioDuration.stop();
                    chronometerRecordAudioDuration.setBase(SystemClock.elapsedRealtime());
                    //进度条设置
                    progressBarRecordAudioDuration.setProgress(0);
                    //切换至播放界面
                    layoutRecordAudio.setVisibility(View.INVISIBLE);
                    layoutPlayAudio.setVisibility(View.VISIBLE);
                    //加载音频
                    startMediaPlayer();
                    AUDIO_DURATION = mediaPlayer.getDuration();
                    textViewAudioDuration.setText(formatDuration(AUDIO_DURATION));
                    //清除Handler
                    handlerRecordAudio.removeMessages(RECORDING);
                }
                progressBarRecordAudioDuration.setProgress(progress);
            }
        }
    };

    @SuppressLint("HandlerLeak")
    private Handler handlerPlayAudio = new Handler() {
        @SuppressLint("SetTextI18n")
        public void handleMessage(android.os.Message msg) {
            if (isPlaying) {
                int progress = progressBarRecordAudioDuration.getProgress();
                if (progress < AUDIO_DURATION) {
                    progress += 1;
                    handlerRecordAudio.sendEmptyMessageDelayed(PLAYING, 100);
                } else {
                    //播放结束后自动停止
                    imageButtonPlayAudio.setBackgroundResource(R.drawable.icon_play_green_64);
                    //停止播放
                    mediaPlayer.stop();
                    isPlaying = false;
                    //结束计时
                    chronometerPlayAudioDuration.stop();
                    chronometerPlayAudioDuration.setBase(SystemClock.elapsedRealtime());
                    //进度条设置
                    progressBarPlayAudioDuration.setProgress(0);

                    handlerRecordAudio.removeMessages(PLAYING);
                }
                progressBarRecordAudioDuration.setProgress(progress);
            }
        }
    };

    @SuppressLint("DefaultLocale")
    private String formatDuration(int duration) {
        int min = duration / 60000;
        int s = (int) Math.round(duration % 60000 / 1000.0);
        return String.format("%02d:%02d", min, s);
    }
}
