package com.ashlikun.media.video;

import android.content.Context;
import android.view.Surface;

/**
 * 作者　　: 李坤
 * 创建时间: 2017/11/24 17:10
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：播放引擎
 * 抽象出来的公共接口，自定义播放器要实现这个接口
 */
public abstract class EasyMediaInterface {
    public Context context;
    public EasyMediaManager easyMediaManager;
    /**
     * 视频数据
     */
    private VideoData currentDataSource;

    /**
     * 开始播放
     */
    public abstract void start();

    /**
     * 停止
     */
    public abstract void stop();

    /**
     * 准备
     */
    public abstract void prepare();

    /**
     * 暂停播放
     */
    public abstract void pause();

    /**
     * 是否正在播放
     */
    public abstract boolean isPlaying();

    /**
     * 快进到指定的时间
     */
    public abstract void seekTo(long time);

    /**
     * 释放播放器
     */
    public abstract void release();

    /**
     * 当前的播放进度
     */
    public abstract long getCurrentPosition();

    /**
     * 获取缓存进度  比例
     *
     * @return
     */
    public abstract int getBufferedPercentage();

    /**
     * 获取播放时长
     */
    public abstract long getDuration();

    /**
     * 设置渲染器
     *
     * @param surface
     */
    public abstract void setSurface(Surface surface);

    public VideoData getCurrentDataSource() {
        return currentDataSource;
    }

    public void setCurrentDataSource(VideoData currentDataSource) {
        this.currentDataSource = currentDataSource;
    }

    public abstract void setPreparedPause(boolean isPreparedPause);

    public void setContext(Context appContext) {
        context = appContext;
    }

    public void setEasyMediaManager(EasyMediaManager easyMediaManager) {
        this.easyMediaManager = easyMediaManager;
    }
}
