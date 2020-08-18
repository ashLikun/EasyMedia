package com.ashlikun.media.simple;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.ashlikun.media.video.EasyVideoPlayerManager;
import com.ashlikun.media.video.VideoData;
import com.ashlikun.media.video.VideoScreenUtils;
import com.ashlikun.media.video.VideoUtils;
import com.ashlikun.media.video.play.EasyVideoIjkplayer;
import com.ashlikun.media.video.view.EasyVideoPlayer;
import com.ashlikun.okhttputils.http.OkHttpUtils;
import com.ashlikun.orm.LiteOrmUtil;

public class MainDetailsActivity extends AppCompatActivity {

    EasyVideoPlayer mediaPlay;
    /**
     * 上一个列表传递过来的数据
     */
    VideoData oldPlayData;
    boolean isOnBackPressed = false;
    boolean isRePlay = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        oldPlayData = (VideoData) getIntent().getSerializableExtra("data");
        //数据库
        LiteOrmUtil.init(getApplication());
        OkHttpUtils.init(null);
        VideoUtils.init(getApplication(), EasyVideoIjkplayer.class);
        setContentView(R.layout.activity_main_details);
        mediaPlay = (EasyVideoPlayer) findViewById(R.id.mediaPlay);
        if (oldPlayData != null) {
            if (!VideoScreenUtils.startCacheVideo(mediaPlay, oldPlayData)) {
                mediaPlay.setDataSource(oldPlayData);
                mediaPlay.startVideo();
            } else {
                isRePlay = true;
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (VideoScreenUtils.backPress()) {
            isOnBackPressed = true;
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //不是返回按下或者之前页面没有有播放的时候  停止
        if (!isOnBackPressed || !isRePlay) {
            mediaPlay.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (EasyVideoPlayerManager.getCurrentVideoPlay() != null) {
            VideoData dd = EasyVideoPlayerManager.getCurrentVideoPlay().getCurrentData();
            if (dd.equalsUrl(oldPlayData)) {
                mediaPlay.onResume();
            } else if (mediaPlay.getCurrentData().equalsUrl(dd)) {
                mediaPlay.onResume();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!isOnBackPressed || !isRePlay) {
            mediaPlay.onDestroy();
        }
    }
}
