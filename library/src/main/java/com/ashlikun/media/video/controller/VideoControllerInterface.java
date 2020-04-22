package com.ashlikun.media.video.controller;

import android.widget.ImageView;

import com.ashlikun.media.video.VideoData;
import com.ashlikun.media.video.status.VideoStatus;
import com.ashlikun.media.video.view.EasyOnControllEvent;

/**
 * 作者　　: 李坤
 * 创建时间: 2017/12/7　9:28
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：抽离出控制器的接口
 */

public interface VideoControllerInterface {

    void setDataSource(VideoData mediaData);

    void setOnControllEvent(EasyOnControllEvent onControllEvent);

    /**
     * 是否可以全屏
     *
     * @param fullEnable
     */
    void setControllFullEnable(boolean fullEnable);

    void setFull(boolean isFull);

    void setCurrentState(@VideoStatus.Code int currentState);


    /**
     * 开始显示控制器的定时器
     */
    void startDismissControlViewSchedule();

    /**
     * 取消显示控制器的定时器
     */
    void cancelDismissControlViewSchedule();

    /**
     * 设置进度最大
     */
    void setMaxProgressAndTime();

    /**
     * 获取当前播放位置
     *
     * @return
     */
    int getCurrentPositionWhenPlaying();

    /**
     * 设置进度缓存
     *
     * @param bufferProgress
     */
    void setBufferProgress(int bufferProgress);

    /**
     * 获取进度缓存
     *
     * @return
     */
    int getBufferProgress();

    /**
     * 自动播放完成
     */
    void onAutoCompletion();

    /**
     * 获取占位图
     *
     * @return
     */
    ImageView getThumbImageView();

}
