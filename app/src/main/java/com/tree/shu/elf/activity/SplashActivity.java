package com.tree.shu.elf.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.tree.shu.elf.Music;
import com.tree.shu.elf.R;
import com.tree.shu.elf.ViewControlContainer;

public class SplashActivity extends AppCompatActivity {


    int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 1;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        new Thread(() -> {
            ViewControlContainer.getInstance().init();
//            Music.getInstance().init();
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }).start();
    }

    @TargetApi(23)
    private void getPersimmion() {
        // 如果应用没有获得对应权限
        //申请WRITE_EXTERNAL_STORAGE权限
        //第一个字符串列是预申请的权限，第三個int是本次请求的辨认编号
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            new AlertDialog.Builder(SplashActivity.this).setMessage("为了保证程序的正常运行，请允许获得必要的权限").setPositiveButton("确定", (dialog, which) -> {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
                dialog.dismiss();
            }).create().show();
    }
}
