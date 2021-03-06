package com.ashlikun.media.video.view;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ashlikun.media.video.EasyVideoPlayerManager;

/**
 * 作者　　: 李坤
 * 创建时间: 2018/8/21　15:07
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：简单的播放器，只会播放视频没有其他任何控制器
 * 适合抖音样式的短视频
 */
public class MiniVideoPlay extends BaseEasyVideoPlay {


    public MiniVideoPlay(@NonNull Context context) {
        super(context);
    }

    public MiniVideoPlay(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MiniVideoPlay(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 保存播放器 用于全局管理
     * {@link EasyVideoPlayerManager#setVideoDefault)}
     * {@link EasyVideoPlayerManager#setVideoDefault)}
     * {@link EasyVideoPlayerManager#setVideoTiny}
     * 可能会多次调用
     */
    @Override
    public void saveVideoPlayView() {
        EasyVideoPlayerManager.setVideoDefault(this);
    }


    @Override
    public boolean isScreenFull() {
        return false;
    }
}
