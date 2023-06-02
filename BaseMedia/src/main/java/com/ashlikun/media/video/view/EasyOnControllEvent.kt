package com.ashlikun.media.video.view

import com.ashlikun.media.video.EasyMediaEvent

/**
 * 作者　　: 李坤
 * 创建时间: 2017/12/5　17:40
 * 邮箱　　：496546144@qq.com
 *
 *
 * 功能介绍：控制器的事件
 */
interface EasyOnControllEvent : EasyMediaEvent {
    /**
     * 开始播放按钮点击
     */
    fun onPlayStartClick()

    /**
     * 播放失败，从新播放
     */
    fun onRetryClick()

    /**
     * 全屏点击
     */
    fun onFullscreenClick()

    /**
     * 当控制器点击的时候
     */
    fun onControllerClick()
}