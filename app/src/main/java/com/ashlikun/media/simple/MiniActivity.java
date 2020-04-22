package com.ashlikun.media.simple;

import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ashlikun.media.video.VideoData;
import com.ashlikun.media.video.VideoUtils;
import com.ashlikun.media.video.status.VideoDisplayType;
import com.ashlikun.media.video.view.MiniVideoPlay;

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
        videoPlay.setDisplayType(VideoDisplayType.MATCH_CROP);
        videoPlay.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
        setContentView(videoPlay);
        videoPlay.setDataSource(new VideoData.Builder()
                .title("标题")
                .url(VideoUrl.meinv2)
                .builder());
        videoPlay.startVideo();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        VideoUtils.setVideoImageDisplayType(VideoDisplayType.MATCH_CROP);
        VideoUtils.releaseAllVideos();
    }
}
