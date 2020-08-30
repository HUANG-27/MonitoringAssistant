package com.example.huang.client.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.huang.client.R;
import com.example.huang.client.config.App2;

public class TargetInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_target);
        initView();
        loadTarget();
    }

    private TextView textViewTargetName;
    private TextView textViewTargetIdNumber;
    private TextView textViewTargetTel;
    private TextView textViewTargetNation;
    private TextView textViewTargetGender;
    private TextView textViewTargetAge;
    private TextView textViewTargetHeight;
    private TextView textViewTargetWeight;
    private TextView textViewTargetIdAddress;
    private TextView textViewTargetAddress;
    private TextView textViewTargetDescription;
    private ImageView imageViewTargetIcon;

    private void initView(){
        textViewTargetName = (TextView) findViewById(R.id.textView_targetIdNumber);
        textViewTargetIdNumber = (TextView) findViewById(R.id.textView_targetName);
        textViewTargetTel = (TextView) findViewById(R.id.textView_target_tel);
        textViewTargetNation = (TextView) findViewById(R.id.textView_target_nation);
        textViewTargetGender = (TextView) findViewById(R.id.textView_target_gender);
        textViewTargetAge = (TextView) findViewById(R.id.textView_target_age);
        textViewTargetHeight = (TextView) findViewById(R.id.textView_target_height);
        textViewTargetWeight = (TextView) findViewById(R.id.textView_target_weight);
        textViewTargetIdAddress = (TextView) findViewById(R.id.textView_target_id_address);
        textViewTargetAddress = (TextView) findViewById(R.id.textView_target_address);
        textViewTargetDescription = (TextView) findViewById(R.id.textView_target_description);
        imageViewTargetIcon = (ImageView)findViewById(R.id.imageView_targetIcon);
    }

    @SuppressLint("SetTextI18n")
    private void loadTarget(){
        if(App2.focusTarget == null)
            return;
        if(App2.focusTarget.getName() != null)
            textViewTargetName.setText(App2.focusTarget.getName());
        if(App2.focusTarget.getIdNumber() != null)
            textViewTargetIdNumber.setText(App2.focusTarget.getIdNumber());
        if(App2.focusTarget.getTel() != null)
            textViewTargetTel.setText(App2.focusTarget.getTel());
        if(App2.focusTarget.getName() != null)
            textViewTargetNation.setText(App2.focusTarget.getNation());
        if(App2.focusTarget.getGender() != null)
            textViewTargetGender.setText(App2.focusTarget.getGender());
        if(App2.focusTarget.getAge() != null)
            textViewTargetAge.setText(App2.focusTarget.getAge().toString());
        if(App2.focusTarget.getHeight() != null)
            textViewTargetHeight.setText(App2.focusTarget.getHeight().toString());
        if(App2.focusTarget.getWeight() != null)
            textViewTargetWeight.setText(App2.focusTarget.getWeight().toString());
        if(App2.focusTarget.getIdAddress() != null)
            textViewTargetIdAddress.setText(App2.focusTarget.getIdAddress());
        if(App2.focusTarget.getAddress() != null)
            textViewTargetAddress.setText(App2.focusTarget.getAddress());
        if(App2.focusTarget.getDescription() != null)
            textViewTargetDescription.setText(App2.focusTarget.getDescription());
        if(App2.focusTarget.getIconUri() != null)
            imageViewTargetIcon.setImageURI(Uri.parse(App2.focusTarget.getIconUri()));



    }
}
