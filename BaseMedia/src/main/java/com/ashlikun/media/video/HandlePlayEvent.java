package com.ashlikun.media.video;

/**
 * 作者　　: 李坤
 * 创建时间: 2020/11/18　17:40
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：
 */
public interface HandlePlayEvent {


    public void onPrepared();

    public void setBufferProgress(int percent);

    public void onError(int what, int extra);

    public void onInfo(int what, int extra);

    public void onVideoSizeChanged(int width, int height);

    public void onSeekComplete();

    public void onCompletion(EasyMediaInterface easyMediaInterface);
}
