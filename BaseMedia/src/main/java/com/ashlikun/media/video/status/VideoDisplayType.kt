package com.ashlikun.media.video.status

import androidx.annotation.IntDef
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

/**
 * 作者　　: 李坤
 * 创建时间: 2017/12/12　13:34
 * 邮箱　　：496546144@qq.com
 *
 *
 * 功能介绍：视频缩放类型
 */
enum class VideoDisplayType {
    /**
     * Video大小适配视频
     */
    ADAPTER,

    /**
     * 填充整个父View
     */
    MATCH_PARENT,

    /**
     * 充满剪切
     */
    MATCH_CROP,

    /**
     * 原始大小
     */
    ORIGINAL;

    companion object {
        fun get(code: Int): VideoDisplayType {
            return VideoDisplayType.values().find { it.ordinal == code } ?: throw NullPointerException()
        }
    }


}