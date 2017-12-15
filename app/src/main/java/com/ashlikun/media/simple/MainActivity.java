package com.ashlikun.media.simple;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.ashlikun.glideutils.GlideUtils;
import com.ashlikun.media.EasyVideoPlayer;
import com.ashlikun.media.MediaUtils;
import com.ashlikun.media.play.EasyMediaIjkplayer;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    EasyVideoPlayer mediaPlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MediaUtils.setMediaInterface(new EasyMediaIjkplayer());
        GlideUtils.init(new GlideUtils.OnNeedListener() {
            @Override
            public Application getApplication() {
                return MainActivity.this.getApplication();
            }

            @Override
            public boolean isDebug() {
                return BuildConfig.DEBUG;
            }

            @Override
            public String getBaseUrl() {
                return "www.baidu.com";
            }
        });
        setContentView(R.layout.activity_main);
        mediaPlay = (EasyVideoPlayer) findViewById(R.id.mediaPlay);
        mediaPlay.setDataSource("http://ic.snssdk.com/neihaasdadn/video/playbacasdasdk/1513147137.42/?video_id=57bd415a24904fcf9e740d6025bb5b9b&quality=360p&line=1&is_gif=0&device_platform=android", "标题");
        //  MediaUtils.startFullscreen(new EasyVideoPlayer(this), VideoUrl.videoUrls[0][0], "李坤李坤李坤");
        findViewById(R.id.listButton).setOnClickListener(this);
        findViewById(R.id.fullScreenButton).setOnClickListener(this);
        findViewById(R.id.fullScreenButton2).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.listButton) {
            Intent intent = new Intent(this, ActivityRecyclerView.class);
            startActivity(intent);
        } else if (v.getId() == R.id.fullScreenButton) {
            MediaUtils.startFullscreen(new EasyVideoPlayer(this), "http://ic.snssdk.com/neihasn/video/playback/1513ss147137.42/?video_id=57bd415a24904fcf9e740d6025bb5b9b&quality=360p&line=1&is_gif=0&device_platform=android", "标题");
        } else if (v.getId() == R.id.fullScreenButton2) {
            EasyVideoPlayer easyVideoPlayer = new EasyVideoPlayer(this);
            easyVideoPlayer.setFullscreenPortrait(false);
            MediaUtils.startFullscreen(easyVideoPlayer, "http://ic.snssdk.com/neishan/video/playback/1513147137.42/?video_id=57bd4ss15a24904fcf9e740d6025bb5b9b&quality=360p&line=1&is_gif=0&device_platform=android", "标题");
        }
    }
}
