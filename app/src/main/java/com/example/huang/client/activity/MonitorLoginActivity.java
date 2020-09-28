package com.example.huang.client.activity;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSONObject;
import com.example.huang.client.config.App2;
import com.example.huang.client.config.RegisterInfoIO;

import com.example.huang.client.R;
import com.example.huang.client.entity.Monitor;
import com.example.huang.client.tool.JSONTool;
import com.example.huang.client.tool.ValidateTool;
import com.example.huang.client.config.RegisterInfo;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MonitorLoginActivity extends AppCompatActivity {

    private EditText editTextIdNumber;
    private EditText editTextPassword;
    private ImageButton imageButtonViewPwd;
    private CheckBox checkBoxRememberId;
    private CheckBox checkBoxRememberPwd;

    private boolean isPwdVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
        loadUserInfo();
    }

    private void initView(){
        editTextIdNumber = (EditText)findViewById(R.id.editText_loginPhoneNumber);
        editTextPassword = (EditText)findViewById(R.id.editText_loginPassword);
        imageButtonViewPwd = (ImageButton)findViewById(R.id.imagebutton_loginViewPwd);
        imageButtonViewPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPwdVisible) {
                    isPwdVisible = false;
                    imageButtonViewPwd.setBackgroundResource(R.drawable.icon_invisible_gray_64);
                    editTextPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);
                } else {
                    isPwdVisible = true;
                    imageButtonViewPwd.setBackgroundResource(R.drawable.icon_visible_gray_64);
                    editTextPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                }
            }
        });
        checkBoxRememberId = (CheckBox)findViewById(R.id.checkBox_loginRememberId);
        checkBoxRememberPwd = (CheckBox)findViewById(R.id.checkBox_loginRememberPwd);
        Button buttonOkay = (Button) findViewById(R.id.btn_loginOkay);
        buttonOkay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    String idNumber = editTextIdNumber.getText().toString();
                    String password = editTextPassword.getText().toString();

                    //验证手机号
                    if(!ValidateTool.IDNumberValidate.validate(idNumber)){
                        Toast.makeText(MonitorLoginActivity.this, "请输入正确的身份证号码", Toast.LENGTH_LONG).show();
                        return;
                    }
                    //验证密码
                    if(ValidateTool.passwordValidate(password)){
                        Toast.makeText(MonitorLoginActivity.this, "密码不能为空，且只能包含数字和大小写英文字母", Toast.LENGTH_LONG).show();
                        return;
                    }

                    OkHttpClient client = new OkHttpClient();
                    FormBody formBody = new FormBody.Builder()
                            .add("idNumber", idNumber)
                            .add("password", password)
                            .build();
                    Request request = new Request.Builder()
                            .url(App2.serverURL + "/login")
                            .post(formBody)
                            .build();
                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(@NotNull Call call, @NotNull IOException e) {
                            Toast.makeText(MonitorLoginActivity.this, "登录失败！请检查用户名或密码", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                            if(response.body() == null)
                                Toast.makeText(MonitorLoginActivity.this, "注册失败！请稍后重试", Toast.LENGTH_LONG).show();

                            App2.monitor = JSONObject.parseObject(JSONTool.filterJSONString(response.body().string()), Monitor.class);

                            //存储登录信息
                            storeUserInfo();
                            finish();
                        }
                    });
                }
                catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(MonitorLoginActivity.this,"登录失败！",Toast.LENGTH_LONG).show();
                }
            }
        });
        Button buttonCancel = (Button) findViewById(R.id.btn_loginCancel);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void loadUserInfo(){
        RegisterInfo registerInfo = RegisterInfoIO.readRegisterInfo();
        if(registerInfo == null)
            return;

        if(registerInfo.isRememberId){
            checkBoxRememberId.setChecked(true);
            editTextIdNumber.setText(registerInfo.id);
        }
        else
            checkBoxRememberId.setChecked(false);

        if(registerInfo.isRememberPwd){
            checkBoxRememberPwd.setChecked(true);
            editTextPassword.setText(registerInfo.pwd);
        }
        else
            checkBoxRememberPwd.setChecked(false);

    }

    private void storeUserInfo(){
        RegisterInfo registerInfo = new RegisterInfo();
        if(checkBoxRememberId.isChecked()){
            registerInfo.isRememberId = true;
            registerInfo.id = editTextIdNumber.getText().toString();
        }
        else{
            registerInfo.isRememberId = false;
            registerInfo.id = "null";
        }
        if(checkBoxRememberPwd.isChecked()){
            registerInfo.isRememberPwd = true;
            registerInfo.pwd = editTextPassword.getText().toString();
        }
        else{
            registerInfo.isRememberPwd = false;
            registerInfo.pwd = "null";
        }
        RegisterInfoIO.writeRegisterInfo(registerInfo);
    }
}
