package com.ashlikun.media.video

import android.content.Context
import android.view.Surface
import tv.danmaku.ijk.media.player.IjkMediaPlayer

/**
 * 作者　　: 李坤
 * 创建时间: 2017/11/24 17:10
 * 邮箱　　：496546144@qq.com
 *
 *
 * 功能介绍：播放引擎
 * 抽象出来的公共接口，自定义播放器要实现这个接口
 */
/**
 * 当内部的播放器创建的时候
 * @param media 当前对象
 * @param data 即将准备的数据
 * @param player 具体播放器 ， ijkplay：[IjkMediaPlayer], exo2：[IjkExo2MediaPlayer], exo3：[IjkExo3MediaPlayer]
 */
typealias OnPlayerCreate = (media: EasyMediaInterface, data: VideoData, player: Any) -> Unit

abstract class EasyMediaInterface(open val manager: EasyMediaManager) {
    /**
     * 视频数据
     */
    var currentDataSource: VideoData? = null

    /**
     * 开始播放
     */
    abstract fun start()

    /**
     * 停止
     */
    abstract fun stop()

    /**
     * 准备
     */
    abstract fun prepare()

    /**
     * 暂停播放
     */
    abstract fun pause()

    /**
     * 是否正在播放
     */
    abstract val isPlaying: Boolean

    /**
     * 快进到指定的时间
     */
    abstract fun seekTo(time: Long)

    /**
     * 释放播放器
     */
    abstract fun release()

    /**
     * 当前的播放进度
     */
    abstract val currentPosition: Long

    /**
     * 获取缓存进度  比例
     *
     * @return
     */
    abstract val bufferedPercentage: Int

    /**
     * 获取播放时长
     */
    abstract val duration: Long

    /**
     * 设置渲染器
     *
     * @param surface
     */
    abstract fun setSurface(surface: Surface)
    abstract fun setPreparedPause(isPreparedPause: Boolean)
}