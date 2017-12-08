package com.ashlikun.media.controller;

import com.ashlikun.media.view.EasyOnControllEvent;

/**
 * 作者　　: 李坤
 * 创建时间: 2017/12/7　9:28
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：抽离出控制器的接口
 */

public interface MediaControllerInterface {

    public void setDataSource(Object[] dataSourceObjects, int defaultUrlMapIndex, int screen, Object... objects);

    public void setOnControllEvent(EasyOnControllEvent onControllEvent);

    public void setCurrentScreen(int currentScreen);

    public void setCurrentState(int currentState);


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

    //销毁进度对话框
    public void dismissProgressDialog();

    //销毁声音对话框
    public void dismissVolumeDialog();

    //销毁亮度对话框
    public void dismissBrightnessDialog();

    //显示非WiFi提示
    public void showWifiDialog(int event);
}
