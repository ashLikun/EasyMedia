package com.ashlikun.media.video.view

import com.ashlikun.media.video.EasyMediaEvent

/**
 * @author　　: 李坤
 * 创建时间: 2018/8/20 11:01
 * 邮箱　　：496546144@qq.com
 *
 *
 * 功能介绍：播放器回掉的生命周期
 */
interface IEasyMediaPlayListener : EasyMediaEvent {
    /**
     * 准备播放
     */
    fun onPrepared()

    /**
     * 对应activity得生命周期
     */
    fun onPause()

    /**
     * 对应activity得生命周期
     */
    fun onBackPressed() = false

    /**
     * 对应activity得生命周期
     */
    fun onResume()

    /**
     * 对应activity得生命周期
     */
    fun onDestroy()

    /**
     * 播放信息
     *
     * @param what  错误码
     * @param extra 扩展码
     */
    fun onInfo(what: Int, extra: Int)

    /**
     * 设置进度完成
     */
    fun onSeekComplete()

    /**
     * 播放错误
     *
     * @param what  错误码
     * @param extra 扩展码
     */
    fun onError(what: Int, extra: Int)

    /**
     * 自动播放完成，播放器回调的
     *
     * @return 是否结束
     */
    fun onAutoCompletion(): Boolean

    /**
     * 自己主动调用完成
     */
    fun onForceCompletionTo()

    /**
     * 播放器大小改变
     */
    fun onVideoSizeChanged(width: Int, height: Int)

    /**
     * 缓存进度更新
     *
     * @param bufferProgress
     */
    fun setBufferProgress(bufferProgress: Int)
}