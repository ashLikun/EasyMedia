package com.ashlikun.media.video

/**
 * 作者　　: 李坤
 * 创建时间: 2020/11/18　17:40
 * 邮箱　　：496546144@qq.com
 *
 *
 * 功能介绍：
 */
interface HandlePlayEvent {
    fun onPrepared()
    fun setBufferProgress(percent: Int)
    fun onError(what: Int, extra: Int)
    fun onInfo(what: Int, extra: Int)
    fun onVideoSizeChanged(width: Int, height: Int)
    fun onSeekComplete()
    fun onCompletion(easyMediaInterface: EasyMediaInterface)
    fun onPause()
}