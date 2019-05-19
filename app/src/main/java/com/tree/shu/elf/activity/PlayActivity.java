package com.tree.shu.elf.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tree.shu.elf.Music;
import com.tree.shu.elf.R;
import com.tree.shu.elf.ViewControlContainer;
import com.tree.shu.elf.tools.OtherTools;

public class PlayActivity extends AppCompatActivity implements IActivity, View.OnClickListener {


    TextView tiltle;
    TextView singer_name;
    ImageView album_icon;

    ImageView play;
    ImageView move_back;
    ImageView move_forward;


    String nowPlayMusic;

    FrameLayout content_lrc;

    boolean first_start = true;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        init();
    }

    private void init() {
        ViewControlContainer.getInstance().onBindPlayActivity(PlayActivity.this);
        Toolbar toolbar = findViewById(R.id.paly_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        OtherTools.setTranslucentStatus(PlayActivity.this);
        OtherTools.setAndroidNativeLightStatusBar(PlayActivity.this, true);
        tiltle = toolbar.findViewById(R.id.toolbar_title);
        singer_name = findViewById(R.id.play_singer_name);
        album_icon = findViewById(R.id.album_icon);
        play = findViewById(R.id.play);
        move_back = findViewById(R.id.move_back);
        move_forward = findViewById(R.id.move_forward);
        move_back.setOnClickListener(this);
        play.setOnClickListener(this);
        move_forward.setOnClickListener(this);
        content_lrc = findViewById(R.id.content_lrc);
        content_lrc.addView(ViewControlContainer.getInstance().getPlayLrcView());
        songChange();
    }

    @Override
    public void songChange() {
        Music.getInstance().setActivityView(PlayActivity.this, tiltle, singer_name, album_icon);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!first_start&&!nowPlayMusic.equals(Music.getInstance().getNowPalyName())) {
            songChange();
        }
        if (first_start) {
            first_start = false;
        }
    }

    @Override
    public void showShortTaost(String content) {
        Toast.makeText(this,content, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ViewControlContainer.getInstance().unBindPlayActivity();
        content_lrc.removeAllViews();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setNowPlayMusic(String nowPlayMusic) {
        this.nowPlayMusic = nowPlayMusic;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.move_back:
                Music.getInstance().setActivityView(PlayActivity.this,tiltle,singer_name,album_icon,true);
                break;
            case R.id.play:
                break;
            case R.id.move_forward:
                Music.getInstance().setActivityView(true,PlayActivity.this,tiltle,singer_name,album_icon);
                break;

        }
    }
}
