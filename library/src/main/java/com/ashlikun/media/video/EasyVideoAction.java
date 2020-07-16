package com.ashlikun.media.video;

/**
 * @author　　: 李坤
 * 创建时间: 2018/8/20 11:01
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：播放器事件,当播放器整个生命周期中的一些事件，一般用于全局监听
 */

public interface EasyVideoAction {
    /**
     * 播放按钮点击
     */
    int ON_CLICK_START_ICON = 0;
    int ON_CLICK_START_ERROR = 1;
    int ON_CLICK_START_AUTO_COMPLETE = 2;

    int ON_CLICK_PAUSE = 3;
    int ON_CLICK_RESUME = 4;
    int ON_SEEK_POSITION = 5;
    int ON_AUTO_COMPLETE = 6;

    /**
     *
     */
    int ON_ENTER_FULLSCREEN = 7;
    /**
     * 退出全屏
     */
    int ON_QUIT_FULLSCREEN = 8;
    /**
     * 退出小窗口
     */
    int ON_QUIT_TINYSCREEN = 10;
    /**
     * 在播放器上手势改变音量
     */
    int ON_TOUCH_SCREEN_SEEK_VOLUME = 11;
    /**
     * 在播放器上手势改变进度
     */
    int ON_TOUCH_SCREEN_SEEK_POSITION = 12;
    /**
     * 播放状态 startVideo 调用的时候,准备过后调用的
     */
    int ON_STATUS_PREPARING = 13;
    /**
     * 默认状态
     */
    int ON_STATUS_NORMAL = 14;
    /**
     * 播放状态
     */
    int ON_STATUS_PLAYING = 15;
    /**
     * 暂停状态
     */
    int ON_STATUS_PAUSE = 16;
    /**
     * 错误状态
     */
    int ON_STATUS_ERROR = 17;
    /**
     * 自动完成状态
     */
    int ON_STATUS_AUTO_COMPLETE = 18;
    /**
     * 没有无线网络时候点击继续播放
     */
    int ON_CLICK_START_NO_WIFI_GOON = 101;

    /**
     * 播放器的发出的事件
     *
     * @param type {@link EasyVideoAction}
     */
    void onEvent(int type);
}
