package com.ashlikun.media.view;

import android.content.Context;
import android.support.annotation.NonNull;

import com.ashlikun.media.EasyMediaManager;
import com.ashlikun.media.EasyVideoPlayerManager;
import com.ashlikun.media.MediaData;

import java.util.List;

/**
 * 作者　　: 李坤
 * 创建时间: 2018/8/21　15:07
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：简单的播放器，只会播放视频没有其他任何控制器
 * 设置玩数据源就会播放
 */
public class MiniVideoPlay extends BaseEasyVideoPlay {
    public MiniVideoPlay(@NonNull Context context) {
        super(context);
    }

    @Override
    public boolean setDataSource(List<MediaData> mediaData, int defaultIndex) {
        super.setDataSource(mediaData, defaultIndex);
        startVideo();
        return true;
    }

    @Override
    public void onAutoCompletion() {
        EasyMediaManager.start();
    }

    @Override
    protected void saveVideoPlayView() {
        EasyVideoPlayerManager.setVideoDefault(this);
    }
}
