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

    void setDataSource(MediaData mediaData);

    void setOnControllEvent(EasyOnControllEvent onControllEvent);

    //是否可以全屏
    void setControllFullEnable(boolean fullEnable);

    void setCurrentScreen(@MediaScreenStatus.Code int currentScreen);

    void setCurrentState(@MediaStatus.Code int currentState);


    //开始显示控制器的定时器
    void startDismissControlViewSchedule();

    //取消显示控制器的定时器
    void cancelDismissControlViewSchedule();

    //设置进度最大
    void setMaxProgressAndTime();

    //获取当前播放位置
    int getCurrentPositionWhenPlaying();

    //设置进度缓存
    void setBufferProgress(int bufferProgress);

    //获取进度缓存
    int getBufferProgress();

    //自动播放完成
    public void onAutoCompletion();

    //获取占位图
    public ImageView getThumbImageView();

}
