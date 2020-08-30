package com.example.huang.client;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.List;

import com.example.huang.client.dialog.PermissionDialog;
import com.example.huang.client.fragment.MapFragment;
import com.example.huang.client.fragment.TargetListFragment;
import com.example.huang.client.fragment.MonitorInfoFragment;
import com.example.huang.client.tool.LocateTool;
import com.example.huang.client.tool.PreferencesUtils;
import com.example.huang.client.tool.SensorTool;
import com.example.huang.client.config.App2;
import com.google.android.material.bottomnavigation.BottomNavigationView;


public class MainActivity extends AppCompatActivity {

    private FrameLayout frameLayout;
    private List<Fragment> fragments;

    private int lastIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //获取App所需权限
        requestPermissionTip();
        setContentView(R.layout.activity_main);

        initFragments();
        initMainActivity();

        LocateTool.initLocationClient(getApplicationContext());
        SensorTool.initSensor(getApplicationContext());

        App2.createAppFolder();
        App2.autoLogin();
        App2.cycleUpdate();
    }

    @Override
    protected void onDestroy() {
        SensorTool.stopSensor();
        App2.autoLogout();
        super.onDestroy();
    }

    private void initFragments(){
        fragments = new ArrayList<>();
        Fragment mapFragment = new MapFragment();
        fragments.add(mapFragment);
        Fragment dataFragment = new TargetListFragment();
        fragments.add(dataFragment);
        Fragment userFragment = new MonitorInfoFragment();
        fragments.add(userFragment);
        setFragmentIndex(0);
    }

    private void setFragmentIndex(int index){
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment currentFragment = fragments.get(index);
        Fragment lastFragment = fragments.get(lastIndex);
        lastIndex = index;
        ft.hide(lastFragment);
        if (!currentFragment.isAdded()) {
            getSupportFragmentManager().beginTransaction().remove(currentFragment).commit();
            ft.add(R.id.frame_layout, currentFragment);
        }
        ft.show(currentFragment);
        ft.commitAllowingStateLoss();
    }

    private void initMainActivity() {

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.navigation_map:
                        setFragmentIndex(0);
                        return true;
                    case R.id.navigation_data:
                        setFragmentIndex(1);
                        return true;
                    case R.id.navigation_user:
                        setFragmentIndex(2);
                        return true;
                    default:
                        return false;
                }
            }
        });
    }

    private void requestPermission(){
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                //定位
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                //相机、麦克
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                //网络
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                //存储
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
        }, 1);
    }

    private void requestPermissionTip() {
        boolean hasShow = PreferencesUtils.getBoolean(MainActivity.this,PreferencesUtils.HAS_SHOW_PREMISSION_DIALOG,false);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ! hasShow){
            PermissionDialog permissionDialog = new PermissionDialog(MainActivity.this);
            permissionDialog.setOnCertainButtonClickListener(new PermissionDialog.OnCertainButtonClickListener() {
                @Override
                public void onCertainButtonClick() {
                    //搭配SharedPreferences 将状态值记录下来，实现APP首次打开的时候才会弹出这个对话框
                    PreferencesUtils.putBoolean(MainActivity.this,PreferencesUtils.HAS_SHOW_PREMISSION_DIALOG,true);
                    //调用运行时权限申请框架
                    requestPermission();
                }
            });
            permissionDialog.show();
        }else{
            //调用运行时权限申请框架
            requestPermission();
        }

    }




}

