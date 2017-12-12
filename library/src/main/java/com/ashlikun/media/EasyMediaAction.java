package com.ashlikun.media;

import com.ashlikun.media.status.MediaScreenStatus;

/**
 * 播放器事件
 */
public interface EasyMediaAction {
    //播放按钮点击
    int ON_CLICK_START_ICON = 0;
    int ON_CLICK_START_ERROR = 1;
    int ON_CLICK_START_AUTO_COMPLETE = 2;

    int ON_CLICK_PAUSE = 3;
    int ON_CLICK_RESUME = 4;
    int ON_SEEK_POSITION = 5;
    int ON_AUTO_COMPLETE = 6;

    int ON_ENTER_FULLSCREEN = 7;
    int ON_QUIT_FULLSCREEN = 8;
    int ON_ENTER_TINYSCREEN = 9;
    int ON_QUIT_TINYSCREEN = 10;


    int ON_TOUCH_SCREEN_SEEK_VOLUME = 11;
    int ON_TOUCH_SCREEN_SEEK_POSITION = 12;
    //继续播放
    int ON_CLICK_START_NO_WIFI_GOON = 101;

    void onEvent(int type, Object url, @MediaScreenStatus.Code int screen, Object... objects);

}
