package com.example.huang.client.config;

import android.os.Environment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class RegisterInfoIO {

    private static String appUserFolder = App2.appFolder + File.separatorChar + "user";

    public static void writeRegisterInfo(RegisterInfo registerInfo) {
        //创建文件夹
        File dataFolder = new File(appUserFolder);
        if (!dataFolder.exists()) {
            dataFolder.mkdir();
        }
        //写入数据
        try {
            String file = appUserFolder + "/register_info.txt";
            FileWriter fileWriter = new FileWriter(file, false);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write("记住账号 " + registerInfo.isRememberId);
            bufferedWriter.newLine();
            bufferedWriter.write("记住密码 " + registerInfo.isRememberPwd);
            bufferedWriter.newLine();
            bufferedWriter.write("账号 " + registerInfo.id);
            bufferedWriter.newLine();
            bufferedWriter.write("密码 " + registerInfo.pwd);
            bufferedWriter.close();
            fileWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static RegisterInfo readRegisterInfo() {
        try {
            RegisterInfo registerInfo = new RegisterInfo();
            String fileStr = appUserFolder + "/register_info.txt";
            File file = new File(fileStr);
            if(!file.exists())
                return null;
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String isRememberId = bufferedReader.readLine().split(" ")[1];
            String isRememberPwd = bufferedReader.readLine().split(" ")[1];
            String id = bufferedReader.readLine().split(" ")[1];
            String pwd = bufferedReader.readLine().split(" ")[1];
            bufferedReader.close();
            fileReader.close();
            registerInfo.isRememberId = isRememberId.toLowerCase().equals("true");
            registerInfo.id = id;
            registerInfo.isRememberPwd = isRememberPwd.toLowerCase().equals("true");
            registerInfo.pwd = pwd;
            return registerInfo;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
