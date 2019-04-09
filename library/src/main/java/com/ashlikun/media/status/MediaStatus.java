package com.ashlikun.media.status;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 作者　　: 李坤
 * 创建时间: 2017/11/28　10:17
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：播放器状态
 */

public interface MediaStatus {
    /**
     * 默认状态
     */
    int NORMAL = 0;
    /**
     * 准备中
     */
    int PREPARING = 1;
    /**
     * 准备中uil改变
     */
    int PREPARING_CHANGING_URL = 2;
    /**
     * 播放中
     */
    int PLAYING = 3;
    /**
     * 暂停
     */
    int PAUSE = 5;
    /**
     * 自动完成
     */
    int AUTO_COMPLETE = 6;
    /**
     * 错误
     */
    int ERROR = 7;

    @IntDef(value = {NORMAL, PREPARING
            , PREPARING_CHANGING_URL, PLAYING,
            PAUSE, AUTO_COMPLETE, ERROR})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Code {

    }
}
