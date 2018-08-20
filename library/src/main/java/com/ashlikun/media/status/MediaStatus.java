package com.ashlikun.media.status;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 作者　　: 李坤
 * 创建时间: 2017/11/28　10:17
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：
 */

public interface MediaStatus {
    /**
     * 默认状态
     */
    int CURRENT_STATE_NORMAL = 0;
    /**
     * 准备中
     */
    int CURRENT_STATE_PREPARING = 1;
    /**
     * 准备中uil改变
     */
    int CURRENT_STATE_PREPARING_CHANGING_URL = 2;
    int CURRENT_STATE_PLAYING = 3;
    int CURRENT_STATE_PAUSE = 5;
    int CURRENT_STATE_AUTO_COMPLETE = 6;
    int CURRENT_STATE_ERROR = 7;

    @IntDef(value = {CURRENT_STATE_NORMAL, CURRENT_STATE_PREPARING
            , CURRENT_STATE_PREPARING_CHANGING_URL, CURRENT_STATE_PLAYING,
            CURRENT_STATE_PAUSE, CURRENT_STATE_AUTO_COMPLETE, CURRENT_STATE_ERROR})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Code {

    }
}
