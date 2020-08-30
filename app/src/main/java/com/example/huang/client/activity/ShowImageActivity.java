package com.example.huang.client.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.huang.client.R;

import com.example.huang.client.config.App2;

public class ShowImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);
        initView();
    }

    private void initView(){
        ImageView imageView = (ImageView) findViewById(R.id.imageView_showImage);
        Bitmap image = App2.tempImage;
        if(image != null)
            imageView.setImageBitmap(image);
    }
}
