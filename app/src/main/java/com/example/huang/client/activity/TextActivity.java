package com.example.huang.client.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.time.LocalDateTime;

import com.example.huang.client.R;
import com.example.huang.client.entity.TextData;
import com.example.huang.client.tool.LocateTool;
import com.example.huang.client.config.App2;

public class TextActivity extends AppCompatActivity {

    private EditText editText;
    private static TextData textData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text);
        initView();
    }

    private void initView() {
        editText = (EditText) findViewById(R.id.editText_textData);
        Button buttonOK = (Button) findViewById(R.id.button_textOk);
        buttonOK.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                textData = new TextData();
                textData.setMonitor(App2.monitor);
                textData.setTarget(App2.focusTarget);
                textData.setTime(LocalDateTime.now());
                textData.setLocation(LocateTool.myLocation);
                textData.setContent(editText.getText().toString());

                App2.uploadData = textData;
                setResult(RESULT_OK);
                finish();
            }
        });
        Button buttonCancel = (Button) findViewById(R.id.button_textCancel);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });
    }
}
