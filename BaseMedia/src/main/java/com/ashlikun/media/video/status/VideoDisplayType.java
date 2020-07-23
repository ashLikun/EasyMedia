package com.ashlikun.media.video.status;


import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 作者　　: 李坤
 * 创建时间: 2017/12/12　13:34
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：视频缩放类型
 */

public interface VideoDisplayType {
    /**
     * Video大小适配视频
     */
    int ADAPTER = 0;
    /**
     * 填充整个父View
     */
    int MATCH_PARENT = 1;
    /**
     * 充满剪切
     */
    int MATCH_CROP = 2;
    /**
     * 原始大小
     */
    int ORIGINAL = 3;

    @IntDef(value = {ADAPTER, MATCH_PARENT
            , MATCH_CROP, ORIGINAL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Code {

    }
}
