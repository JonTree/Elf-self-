package com.tree.shu.elf.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.support.v4.view.GravityCompat;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tree.shu.elf.Bean.SongListInformationBean;
import com.tree.shu.elf.Music;
import com.tree.shu.elf.R;
import com.tree.shu.elf.tools.OtherTools;
import com.tree.shu.elf.ViewControlContainer;

public class MainActivity extends AppCompatActivity implements IActivity{

    DrawerLayout drawer;
    RelativeLayout content_fra_evaluation;

    TextView song_name;
    TextView singer_name;
    ImageView song_icon;

    String nowPlayMusic;

    int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 1;

    boolean first_start = true;

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        OtherTools.setAndroidNativeLightStatusBar(MainActivity.this, true);
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout);
        OtherTools.getPersimmion(MainActivity.this, WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
        init();
    }


    private void init() {
        ViewControlContainer.getInstance().onBindMainActivity(MainActivity.this);
        song_icon = findViewById(R.id.song_icon);
        song_name = findViewById(R.id.song_name);
        singer_name = findViewById(R.id.singer_name);
        content_fra_evaluation = findViewById(R.id.content_fra_evaluation);
        ViewControlContainer.getInstance().initForThis(content_fra_evaluation, null);
        songChange();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (!first_start&&nowPlayMusic!=null&&!nowPlayMusic.equals(Music.getInstance().getNowPalyName())) {
            songChange();
        }
        if (first_start) {
            first_start = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ViewControlContainer.getInstance().unBindMainActivity();
        super.onStop();
        // 释放mediaPlayer
        if (Music.getInstance().getMediaPlayer() != null) {
            Music.getInstance().getMediaPlayer().stop();
            Music.getInstance().getMediaPlayer().release();
            Music.getInstance().setMediaPlayer(null);
        }
    }


    @Override
    public void setNowPlayMusic(String nowPlayMusic) {
        this.nowPlayMusic = nowPlayMusic;
    }

    @Override
    public void songChange() {
//        String s = ViewControlContainer.getInstance().getPresentMood();
//        SongListInformationBean songListInformationBean = Music.getInstance().getMoodDataMap().get(ViewControlContainer.getInstance().getPresentMood());
//        song_name.setText("" + songListInformationBean.getPlaylist().getTracks().get(Music.getInstance().getNowPaly()).getName());
//        singer_name.setText(""+songListInformationBean.getPlaylist().getTracks().get(Music.getInstance().getNowPaly()).getAr().get(0).getName());
        Music.getInstance().setActivityView(MainActivity.this, song_name, singer_name, song_icon);
    }

    @Override
    public void showShortTaost(String content) {
        Toast.makeText(this,content, Toast.LENGTH_SHORT).show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            if (drawer.isDrawerOpen(GravityCompat.END)) {
                drawer.closeDrawers();
            } else {
                drawer.openDrawer(Gravity.END);
            }
        }

        return super.onOptionsItemSelected(item);
    }


}
