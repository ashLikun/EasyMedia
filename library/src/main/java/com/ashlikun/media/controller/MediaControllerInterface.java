package com.ashlikun.media.controller;

import android.widget.ImageView;

import com.ashlikun.media.MediaData;
import com.ashlikun.media.status.MediaScreenStatus;
import com.ashlikun.media.status.MediaStatus;
import com.ashlikun.media.view.EasyOnControllEvent;

/**
 * 作者　　: 李坤
 * 创建时间: 2017/12/7　9:28
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：抽离出控制器的接口
 */

public interface MediaControllerInterface {

    public void setDataSource(MediaData mediaData);

    public void setOnControllEvent(EasyOnControllEvent onControllEvent);
    //是否可以全屏
    public void setControllFullEnable(boolean fullEnable);

    public void setCurrentScreen(@MediaScreenStatus.Code int currentScreen);

    public void setCurrentState(@MediaStatus.Code int currentState);


    //开始显示控制器的定时器
    public void startDismissControlViewSchedule();

    //取消显示控制器的定时器
    public void cancelDismissControlViewSchedule();

    //设置进度最大
    public void setMaxProgressAndTime();

    //获取当前播放位置
    public int getCurrentPositionWhenPlaying();

    //设置进度缓存
    public void setBufferProgress(int bufferProgress);

    //获取进度缓存
    public int getBufferProgress();

    //自动播放完成
    public void onAutoCompletion();

    //获取占位图
    public ImageView getThumbImageView();

}
