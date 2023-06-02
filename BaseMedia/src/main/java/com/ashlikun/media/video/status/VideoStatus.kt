package com.ashlikun.media.video.status

/**
 * 作者　　: 李坤
 * 创建时间: 2017/11/28　10:17
 * 邮箱　　：496546144@qq.com
 *
 *
 * 功能介绍：播放器状态
 */
enum class VideoStatus {
    /**
     * 默认状态
     */
    NORMAL,

    /**
     * 准备中
     */
    PREPARING,

    /**
     * 播放中
     */
    PLAYING,

    /**
     * 暂停
     */
    PAUSE,

    /**
     * 自动完成
     */
    AUTO_COMPLETE,

    /**
     * 强制完成
     */
    FORCE_COMPLETE,

    /**
     * 错误
     */
    ERROR,

    /**
     * 开始缓冲
     */
    BUFFERING_START,
}