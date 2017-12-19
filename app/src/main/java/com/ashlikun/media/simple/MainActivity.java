package com.ashlikun.media.simple;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.ashlikun.glideutils.GlideUtils;
import com.ashlikun.media.EasyVideoPlayer;
import com.ashlikun.media.MediaData;
import com.ashlikun.media.MediaUtils;
import com.ashlikun.media.play.EasyMediaIjkplayer;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    EasyVideoPlayer mediaPlay;
    String videoUrl = "http://ic.snssdk.com/neihan/video/playback/1513563522.2/?video_id=90ffcaa3a2a642bb8e6f02b73a5b27de&quality=origin&line=1&is_gif=0&device_platform=android";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MediaUtils.init(getApplicationContext());
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
        mediaPlay.setDataSource(new MediaData.Builder()
                .title("标题")
                .url(videoUrl)
                .builder());
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
            MediaUtils.startFullscreen(new EasyVideoPlayer(this), videoUrl, "标题");
        } else if (v.getId() == R.id.fullScreenButton2) {
            EasyVideoPlayer easyVideoPlayer = new EasyVideoPlayer(this);
            easyVideoPlayer.setFullscreenPortrait(false);
            MediaUtils.startFullscreen(easyVideoPlayer, videoUrl, "标题");
        }
    }
}
