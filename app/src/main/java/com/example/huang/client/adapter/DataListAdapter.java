package com.example.huang.client.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import com.example.huang.client.R;

import java.io.IOException;

import com.example.huang.client.entity.AudioData;
import com.example.huang.client.entity.Data;
import com.example.huang.client.entity.ImageData;
import com.example.huang.client.entity.TextData;
import com.example.huang.client.entity.VideoData;
import com.example.huang.client.config.App2;
import com.example.huang.client.tool.TimeTool;

public class DataListAdapter extends BaseAdapter {
    private int textResourceId;
    private int audioResourceId;
    private int imageResourceId;
    private int videoResourceId;
    private LayoutInflater layoutInflater;
    private Context context;
    private Data data;

    //播放audio
    private MediaPlayer mediaPlayer;
    private Integer playingAudioId;
    private boolean isPlaying = false;

    public DataListAdapter(Context context) {
        super();
        this.textResourceId = R.layout.layout_text_data_item;
        this.audioResourceId = R.layout.layout_audio_data_item;
        this.imageResourceId = R.layout.layout_image_data_item;
        this.videoResourceId = R.layout.layout_video_data_item;
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount(){
        return App2.dataList.size();
    }

    @Override
    public Object getItem(int position){
        return App2.dataList.get(position);
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        data = (Data)getItem(position);
        View view = null;
        assert data != null;
        switch (data.getType()) {
            //文本数据
            case TEXT_DATA:
                TextData textData = (TextData) data;
                View viewTextData = layoutInflater.inflate(textResourceId, null);
                //采集人头像
                ImageButton imageButtonTextDataUserIcon = (ImageButton) viewTextData.findViewById(R.id.imageButton_textDataUserIcon);
                imageButtonTextDataUserIcon.setBackgroundResource(R.drawable.icon_police_green_64);
                //采集人
                TextView textViewTextDataUserName = (TextView)viewTextData.findViewById(R.id.textView_textDataUserName);
                textViewTextDataUserName.setText(textData.getMonitor().getName());
                //采集时间
                TextView textViewTextDataTime = (TextView) viewTextData.findViewById(R.id.textView_textDataTime);
                textViewTextDataTime.setText(TimeTool.formatDateTime2(textData.getTime()));
                //数据
                TextView textViewTextData = (TextView) viewTextData.findViewById(R.id.textView_textData);
                textViewTextData.setText(textData.getContent());
                //采集地点
                TextView textViewTextDataPlace = (TextView) viewTextData.findViewById(R.id.textView_textDataPlace);
                //textViewTextDataPlace.setText(textData.getLocation().toString());
                //返回view
                view = viewTextData;
                break;
            //音频数据
            case AUDIO_DATA:
                AudioData audioData = (AudioData) data;
                View viewAudioData = layoutInflater.inflate(audioResourceId, null);
                //采集人头像
                ImageButton imageButtonAudioDataUserIcon = (ImageButton) viewAudioData.findViewById(R.id.imageButton_audioDataUserIcon);
                imageButtonAudioDataUserIcon.setBackgroundResource(R.drawable.icon_police_green_64);
                //采集人
                TextView textViewAudioDataUserName = (TextView)viewAudioData.findViewById(R.id.textView_audioDataMonitorName);
                textViewAudioDataUserName.setText(audioData.getMonitor().getName());
                //采集时间
                TextView textViewAudioDataTime = (TextView) viewAudioData.findViewById(R.id.textView_audioDataTime);
                textViewAudioDataTime.setText(audioData.getStartTime().toString());
                //数据播放按钮
                ImageButton imageButtonPlayAudio = (ImageButton) viewAudioData.findViewById(R.id.imageButton_playAudioData);
                imageButtonPlayAudio.setBackgroundResource(R.drawable.icon_audio_green_64);
                imageButtonPlayAudio.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {//播放
                        try {
                            if(isPlaying){
                                //同一个音频，则停止播放
                                if(audioData.getId().equals(playingAudioId)){
                                    mediaPlayer.stop();
                                    mediaPlayer.release();
                                    mediaPlayer = null;

                                    isPlaying = false;
                                    playingAudioId = -1;
                                }
                                //不同音频，停止正在播放的音频，并播放新音频
                                else{
                                    //停止正在播放的音频
                                    mediaPlayer.stop();
                                    mediaPlayer.release();
                                    mediaPlayer = null;
                                    //播放新音频
                                    mediaPlayer = new MediaPlayer();
                                    mediaPlayer.setDataSource(context, Uri.parse(audioData.getUri()));
                                    mediaPlayer.prepare();
                                    mediaPlayer.start();

                                    isPlaying = true;
                                    playingAudioId = audioData.getId();
                                }
                            }
                            else{
                                mediaPlayer = new MediaPlayer();
                                mediaPlayer.setDataSource(context, Uri.parse(audioData.getUri()));
                                mediaPlayer.prepare();
                                mediaPlayer.start();

                                isPlaying = true;
                                playingAudioId = audioData.getId();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                //采集地点
                TextView textViewAudioDataPlace = (TextView) viewAudioData.findViewById(R.id.textView_audioDataPlace);
                //textViewAudioDataPlace.setText(audioData.place);
                //返回view
                view = viewAudioData;
                break;
            //图片数据
            case IMAGE_DATA:
                ImageData imageData = (ImageData)data;
                View viewImageData = layoutInflater.inflate(imageResourceId, null);
                //采集人头像
                ImageButton imageButtonImageDataUserIcon = (ImageButton) viewImageData.findViewById(R.id.imageButton_imageDataUserIcon);
                imageButtonImageDataUserIcon.setBackgroundResource(R.drawable.icon_police_green_64);
                //采集人
                TextView textViewImageDataUserName = (TextView)viewImageData.findViewById(R.id.textView_imageDataUserName);
                textViewImageDataUserName.setText(imageData.getMonitor().getName());
                //采集时间
                TextView textViewImageDataTime = (TextView) viewImageData.findViewById(R.id.textView_imageDataTime);
                textViewImageDataTime.setText(imageData.getTime().toString());
                //数据
                ImageView imageViewImageData = (ImageView) viewImageData.findViewById(R.id.imageView_imageData);
                imageViewImageData.setImageURI(Uri.parse(imageData.getUri()));
                //采集地点
                TextView textViewImageDataPlace = (TextView) viewImageData.findViewById(R.id.textView_imageDataPlace);
                //textViewImageDataPlace.setText(((ImageData)data).place);
                //返回view
                view = viewImageData;
                break;
            //视频数据
            case VIDEO_DATA:
                VideoData videoData = (VideoData)data;
                View viewVideoData = layoutInflater.inflate(videoResourceId, null);
                //采集人头像
                ImageButton imageButtonVideoDataUserIcon = (ImageButton) viewVideoData.findViewById(R.id.imageButton_videoDataUserIcon);
                imageButtonVideoDataUserIcon.setBackgroundResource(R.drawable.icon_police_green_64);
                //采集人
                TextView textViewVideoDataUserName = (TextView)viewVideoData.findViewById(R.id.textView_videoDataUserName);
                textViewVideoDataUserName.setText(((VideoData)data).getMonitor().toString());
                //采集时间
                TextView textDataVideoDataTime = (TextView) viewVideoData.findViewById(R.id.textData_videoDataTime);
                textDataVideoDataTime.setText(((VideoData)data).getStartTime().toString());
                //数据
                VideoView videoViewVideoData = (VideoView) viewVideoData.findViewById(R.id.videoView_videoData);
                videoViewVideoData.setVideoURI(Uri.parse(videoData.getUri()));
                //采集地点
                TextView textDataVideoDataPlace = (TextView) viewVideoData.findViewById(R.id.textData_videoDataPlace);
                //textDataVideoDataPlace.setText(videoData.place);
                //返回view
                view = viewVideoData;
                break;
            default:
                break;
        }
        return view;
    }
}
