package com.ashlikun.media.status;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 作者　　: 李坤
 * 创建时间: 2017/11/28　10:17
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：屏幕状态
 */

public interface MediaScreenStatus {
    /**
     * 默认的
     */
    int SCREEN_WINDOW_NORMAL = 0;
    /**
     * 列表的
     */
    int SCREEN_WINDOW_LIST = 1;
    /**
     * 全屏的
     */
    int SCREEN_WINDOW_FULLSCREEN = 2;
    /**
     * 小窗口
     */
    int SCREEN_WINDOW_TINY = 3;

    @IntDef(value = {SCREEN_WINDOW_NORMAL, SCREEN_WINDOW_LIST
            , SCREEN_WINDOW_FULLSCREEN, SCREEN_WINDOW_TINY})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Code {

    }

}
