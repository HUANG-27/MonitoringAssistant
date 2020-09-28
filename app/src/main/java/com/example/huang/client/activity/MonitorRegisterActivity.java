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
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MonitorRegisterActivity extends AppCompatActivity {

    private EditText editTextIdNumber;
    private EditText editTextName;
    private EditText editTextPhoneNum;
    private EditText editTextPassword;
    private ImageButton imageButtonViewPwd;
    private CheckBox checkBoxRememberId;
    private CheckBox checkBoxRememberPwd;

    private boolean isPwdVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initView();
    }

    private void initView() {
        editTextIdNumber = findViewById(R.id.editText_registerIdNum);
        editTextName = findViewById(R.id.editText_registerName);
        editTextPhoneNum = findViewById(R.id.editText_registerPhoneNum);
        editTextPassword = findViewById(R.id.editText_registerPassword);
        imageButtonViewPwd = findViewById(R.id.imageButton_registerViewPwd);
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

        checkBoxRememberId = (CheckBox) findViewById(R.id.checkBox_registerRememberId);
        checkBoxRememberPwd = (CheckBox) findViewById(R.id.checkBox_registerRememberPwd);

        Button buttonOkay = findViewById(R.id.btn_registerOkay);
        buttonOkay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Monitor tempMonitor = new Monitor();
                    tempMonitor.setIdNumber(editTextIdNumber.getText().toString());
                    tempMonitor.setName(editTextName.getText().toString());
                    tempMonitor.setTel(editTextPhoneNum.getText().toString());
                    tempMonitor.setPassword(editTextPassword.getText().toString());

                    //验证身份证号
                    if(!ValidateTool.IDNumberValidate.validate(tempMonitor.getIdNumber())){
                        Toast.makeText(MonitorRegisterActivity.this, "请输入正确的身份证号", Toast.LENGTH_LONG).show();
                        return;
                    }
                    //验证手机号
                    if(ValidateTool.phoneNumberValidate(tempMonitor.getTel())){
                        Toast.makeText(MonitorRegisterActivity.this, "请输入正确的手机号码", Toast.LENGTH_LONG).show();
                        return;
                    }
                    //验证密码
                    if(ValidateTool.passwordValidate(tempMonitor.getPassword())){
                        Toast.makeText(MonitorRegisterActivity.this, "密码不能为空，且只能包含数字和大小写英文字母", Toast.LENGTH_LONG).show();
                        return;
                    }

                    OkHttpClient client = new OkHttpClient();
                    MediaType type = MediaType.parse("application/json");
                    RequestBody requestBody = RequestBody.create(JSONObject.toJSONString(tempMonitor), type);
                    Request request = new Request.Builder()
                            .url(App2.serverURL + "/register")
                            .post(requestBody)
                            .build();

                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(@NotNull Call call, @NotNull IOException e) {
                            Toast.makeText(MonitorRegisterActivity.this, "注册失败！请稍后重试", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                            if(response.body() == null)
                                Toast.makeText(MonitorRegisterActivity.this, "注册失败！请稍后重试", Toast.LENGTH_LONG).show();
                            App2.monitor = JSONObject.parseObject(JSONTool.filterJSONString(response.body().string()),Monitor.class);
                            storeUserInfo();
                            finish();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(MonitorRegisterActivity.this, "注册失败！请稍后重试", Toast.LENGTH_LONG).show();
                }
            }
        });
        Button buttonCancel = findViewById(R.id.btn_registerCancel);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    private void storeUserInfo() {
        RegisterInfo registerInfo = new RegisterInfo();
        if (checkBoxRememberId.isChecked()) {
            registerInfo.isRememberId = true;
            registerInfo.id = editTextPhoneNum.getText().toString();
        } else {
            registerInfo.isRememberId = false;
            registerInfo.id = "null";
        }
        if (checkBoxRememberPwd.isChecked()) {
            registerInfo.isRememberPwd = true;
            registerInfo.pwd = editTextPassword.getText().toString();
        } else {
            registerInfo.isRememberPwd = false;
            registerInfo.pwd = "null";
        }
        RegisterInfoIO.writeRegisterInfo(registerInfo);
    }
}
