package com.ashlikun.media.controller;

import android.widget.ImageView;

import com.ashlikun.media.MediaData;
import com.ashlikun.media.status.MediaScreenStatus;
import com.ashlikun.media.status.MediaStatus;

/**
 * 作者　　: 李坤
 * 创建时间: 2017/12/18　11:11
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：控制器viewHolder的抽离
 */

public interface IControllerViewHolder {
    //是否可以全屏
    public void setControllFullEnable(boolean fullEnable);

    //根据状态改变ui
    public void changUi(@MediaStatus.Code int currentState, @MediaScreenStatus.Code int currentScreen);

    public void setDataSource(MediaData mediaData, int screen);

    //开始进度定时器
    public void startProgressSchedule();

    //取消进度定时器
    public void stopProgressSchedule();

    //控制器是否显示
    public boolean containerIsShow();

    //显示或者隐藏顶部和底部控制器

    public void showControllerViewAnim(@MediaStatus.Code final int currentState, @MediaScreenStatus.Code final int currentScreen, final boolean isShow);


    //清空全部ui展示
    public void changeUiToClean();

    /**
     * 设置时间
     *
     * @param position 0：重置
     * @param duration 0：重置
     */
    public void setTime(int position, int duration);

    /**
     * 设置进度  如果2个值都是100，就会设置最大值，如果某个值<0 就不设置
     *
     * @param progress          主进度
     * @param secondaryProgress 缓存进度
     */
    public void setProgress(int progress, int secondaryProgress);

    //获取占位图
    public ImageView getThumbImageView();

    //获取进度缓存
    public int getBufferProgress();
}
