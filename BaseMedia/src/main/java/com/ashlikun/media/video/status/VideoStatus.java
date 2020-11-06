package com.ashlikun.media.video.status;


import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 作者　　: 李坤
 * 创建时间: 2017/11/28　10:17
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：播放器状态
 */

public interface VideoStatus {
    /**
     * 默认状态
     */
    int NORMAL = 0;
    /**
     * 准备中
     */
    int PREPARING = 1;
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
     * 强制完成
     */
    int FORCE_COMPLETE = 9;
    /**
     * 错误
     */
    int ERROR = 7;
    /**
     * 开始缓冲
     */
    int BUFFERING_START = 8;

    @IntDef(value = {NORMAL, PREPARING, PLAYING,
            PAUSE, AUTO_COMPLETE, ERROR, BUFFERING_START})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Code {

    }
}
