package com.ashlikun.media.video.controller;

import android.widget.ImageView;

import com.ashlikun.media.video.VideoData;
import com.ashlikun.media.video.status.VideoStatus;

/**
 * 作者　　: 李坤
 * 创建时间: 2017/12/18　11:11
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：控制器viewHolder的抽离
 */

public interface IControllerViewHolder {
    /**
     * 是否可以全屏
     *
     * @param fullEnable
     */
    void setControllFullEnable(boolean fullEnable);

    /**
     * 根据状态改变ui
     *
     * @param currentState
     */
    void changUi(@VideoStatus.Code int currentState);

    void setDataSource(VideoData mediaData);

    /**
     * 开始进度定时器
     */
    void startProgressSchedule();

    /**
     * 取消进度定时器
     */
    void stopProgressSchedule();

    /**
     * 控制器是否显示
     *
     * @return
     */
    boolean containerIsShow();

    /**
     * 显示或者隐藏顶部和底部控制器
     *
     * @param currentState
     * @param isShow
     */
    void showControllerViewAnim(@VideoStatus.Code final int currentState, final boolean isShow);


    /**
     * 清空全部ui展示
     */
    void changeUiToClean();

    /**
     * 设置时间
     *
     * @param position 0：重置
     * @param duration 0：重置
     */
    void setTime(long position, long duration);

    /**
     * 设置进度  如果2个值都是100，就会设置最大值，如果某个值<0 就不设置
     *
     * @param progress          主进度
     * @param secondaryProgress 缓存进度
     */
    void setProgress(int progress, int secondaryProgress);

    /**
     * 获取占位图
     *
     * @return
     */
    ImageView getThumbImageView();

    /**
     * 获取进度缓存
     *
     * @return
     */
    int getBufferProgress();

    void setFull(boolean full);

    /**
     * 是否只在全屏的时候显示标题和顶部
     */
    public void setOnlyFullShowTitle(boolean onlyFullShowTitle);
}
