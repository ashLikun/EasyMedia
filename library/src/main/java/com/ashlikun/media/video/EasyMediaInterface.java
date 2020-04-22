package com.ashlikun.media.video;

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
    /**
     * 视频数据
     */
    public VideoData currentDataSource;

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
    public abstract void seekTo(int time);

    /**
     * 释放播放器
     */
    public abstract void release();

    /**
     * 当前的播放进度
     */
    public abstract int getCurrentPosition();

    /**
     * 获取播放时长
     */
    public abstract int getDuration();

    /**
     * 设置渲染器
     *
     * @param surface
     */
    public abstract void setSurface(Surface surface);
}
