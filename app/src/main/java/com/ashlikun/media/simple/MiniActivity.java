package com.ashlikun.media.simple;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;

import com.ashlikun.media.MediaData;
import com.ashlikun.media.MediaUtils;
import com.ashlikun.media.status.MediaDisplayType;
import com.ashlikun.media.view.MiniVideoPlay;

/**
 * 作者　　: 李坤
 * 创建时间: 2018/8/21　15:11
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：
 */
public class MiniActivity extends AppCompatActivity {
    MiniVideoPlay videoPlay;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        videoPlay = new MiniVideoPlay(this);
        videoPlay.setDisplayType(MediaDisplayType.MATCH_CROP);
        videoPlay.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
        setContentView(videoPlay);
        videoPlay.setDataSource(new MediaData.Builder()
                .title("标题")
                .url(VideoUrl.meinv2)
                .builder());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MediaUtils.setVideoImageDisplayType(MediaDisplayType.MATCH_CROP);
        MediaUtils.releaseAllVideos();
    }
}
