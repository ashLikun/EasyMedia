package com.ashlikun.media.status;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 作者　　: 李坤
 * 创建时间: 2017/11/28　10:17
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：播放器类型
 */

public interface MediaViewType {
    /**
     * 默认的
     */
    int NORMAL = 0;
    /**
     * 列表的
     */
    int LIST = 1;
    /**
     * 全屏的
     */
    int FULLSCREEN = 2;
    /**
     * 小窗口
     */
    int TINY = 3;

    @IntDef(value = {NORMAL, LIST
            , FULLSCREEN, TINY})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Code {

    }

}
