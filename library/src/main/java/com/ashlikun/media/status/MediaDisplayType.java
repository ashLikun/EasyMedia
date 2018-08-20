package com.ashlikun.media.status;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 作者　　: 李坤
 * 创建时间: 2017/12/12　13:34
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：视频显示样式
 */

public interface MediaDisplayType {
    int VIDEO_IMAGE_DISPLAY_TYPE_ADAPTER = 0;
    int VIDEO_IMAGE_DISPLAY_TYPE_FILL_PARENT = 1;
    int VIDEO_IMAGE_DISPLAY_TYPE_FILL_SCROP = 2;
    int VIDEO_IMAGE_DISPLAY_TYPE_ORIGINAL = 3;

    @IntDef(value = {VIDEO_IMAGE_DISPLAY_TYPE_ADAPTER, VIDEO_IMAGE_DISPLAY_TYPE_FILL_PARENT
            , VIDEO_IMAGE_DISPLAY_TYPE_FILL_SCROP, VIDEO_IMAGE_DISPLAY_TYPE_ORIGINAL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Code {

    }
}
