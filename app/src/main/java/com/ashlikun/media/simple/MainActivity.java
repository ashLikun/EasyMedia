package com.ashlikun.media.simple;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.ashlikun.media.EasyVideoPlayer;
import com.ashlikun.media.status.EasyScreenStatus;

public class MainActivity extends AppCompatActivity {

    EasyVideoPlayer mediaPlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mediaPlay = (EasyVideoPlayer) findViewById(R.id.mediaPlay);
        mediaPlay.setDataSource(VideoUrl.videoUrls[0][0], EasyScreenStatus.SCREEN_WINDOW_NORMAL);
    }
}
