package com.example.huang.client.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.huang.client.activity.MonitorEditActivity;
import com.example.huang.client.activity.MonitorLoginActivity;
import com.example.huang.client.R;
import com.example.huang.client.activity.MonitorRegisterActivity;

import com.example.huang.client.config.App2;
import com.example.huang.client.tool.JSONTool;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MonitorInfoFragment extends Fragment {

    private ImageView imageViewUserIcon;
    private TextView textViewTel;
    private TextView textViewName;
    private TextView textViewIdNumber;
    private TextView textViewGender;
    private TextView textViewNation;
    private TextView textViewOfficePlace;
    private TextView textViewOfficeDuty;

    private ScrollView scrollViewBeforeLogin;
    private ScrollView scrollViewAfterLogin;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.fragment_monitor, null);
        initView(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        refresh();
    }

    private void initView(View view) {
        imageViewUserIcon = (ImageView) view.findViewById(R.id.imageView_userIcon);
        textViewName = (TextView) view.findViewById(R.id.textView_userName);
        textViewIdNumber = (TextView) view.findViewById(R.id.textView_monitor_id_number);
        textViewTel = (TextView) view.findViewById(R.id.textView_monitor_tel);
        textViewGender = (TextView) view.findViewById(R.id.textView_monitor_gender);
        textViewNation = (TextView) view.findViewById(R.id.textView_monitor_nation);
        textViewOfficePlace = (TextView) view.findViewById(R.id.textView_monitor_office_place);
        textViewOfficeDuty = (TextView) view.findViewById(R.id.textView_monitor_office_duty);

        scrollViewBeforeLogin = (ScrollView) view.findViewById(R.id.layout_beforeLogin);
        scrollViewAfterLogin = (ScrollView) view.findViewById(R.id.layout_afterLogin);
        scrollViewAfterLogin.setVisibility(View.INVISIBLE);
        scrollViewBeforeLogin.setVisibility(View.VISIBLE);
        clearUserInfo();

        Button buttonRegister = (Button) view.findViewById(R.id.btn_register);
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MonitorRegisterActivity.class);
                MonitorInfoFragment.this.startActivityForResult(intent, 0);
            }
        });
        Button buttonLogin = (Button) view.findViewById(R.id.btn_login);
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MonitorLoginActivity.class);
                MonitorInfoFragment.this.startActivityForResult(intent, 1);
            }
        });
        Button buttonLogout = (Button) view.findViewById(R.id.btn_logout);
        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (App2.monitor == null)
                    return;
                OkHttpClient client = new OkHttpClient();
                FormBody formBody = new FormBody.Builder()
                        .add("id", App2.monitor.getId().toString())
                        .build();
                Request request = new Request.Builder()
                        .url(App2.serverURL + "/monitor/logout")
                        .post(formBody)
                        .build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        //什么都不做
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        Boolean res = Boolean.parseBoolean(JSONTool.filterJSONString(response.body().string()));
                        if(res){
                            App2.monitor = null;
                            App2.targetList = new ArrayList<>();
                            handler.sendEmptyMessage(0);
                        }
                    }
                });
            }
        });

        Button buttonEdit = (Button) view.findViewById(R.id.btn_editUserInfo);
        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MonitorEditActivity.class);
                MonitorInfoFragment.this.startActivityForResult(intent, 3);
            }
        });
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message message){
            super.handleMessage(message);
            refresh();
        }
    };

    private void loadUserInfo() {
        imageViewUserIcon.setImageResource(R.drawable.icon_police_green_128);
        textViewName.setText(App2.monitor.getName());
        textViewIdNumber.setText(App2.monitor.getIdNumber());
        textViewTel.setText(App2.monitor.getTel());
        textViewGender.setText(App2.monitor.getGender());
        textViewNation.setText(App2.monitor.getNation());
        textViewOfficePlace.setText(App2.monitor.getOfficePlace());
        textViewOfficeDuty.setText(App2.monitor.getOfficeDuty());
    }

    private void clearUserInfo() {
        imageViewUserIcon.setImageResource(R.drawable.icon_police_gray_128);
        textViewName.setText("未登录");
        textViewIdNumber.setText("xxxxxxxxxxxxxxxxxx");
        textViewTel.setText("未知");
        textViewGender.setText("未知");
        textViewNation.setText("未知");
        textViewOfficePlace.setText("未知");
        textViewOfficeDuty.setText("未知");
    }

    private void refresh() {
        if (App2.monitor != null) {
            scrollViewBeforeLogin.setVisibility(View.INVISIBLE);
            scrollViewAfterLogin.setVisibility(View.VISIBLE);
            loadUserInfo();
        } else {
            scrollViewAfterLogin.setVisibility(View.INVISIBLE);
            scrollViewBeforeLogin.setVisibility(View.VISIBLE);
            clearUserInfo();
        }
    }
}
