package com.ashlikun.media.video

import com.ashlikun.media.video.VideoScreenUtils.backPress
import com.ashlikun.media.video.view.BaseEasyMediaPlay
import com.ashlikun.media.video.view.EasyMediaPlayTiny
import com.ashlikun.media.video.view.EasyMediaPlayer

/**
 * 作者　　: 李坤
 * 创建时间: 2018/2/1 14:18
 * 邮箱　　：496546144@qq.com
 *
 *
 * 功能介绍：播放器控件的管理器，可全局调用BaseEasyVideoPlay
 */
class EasyVideoViewManager internal constructor(var mediaManager: EasyMediaManager) {
    /**
     * 第一个VideoPlay,默认的，可以自行设置其他的
     */
    private var firstView: BaseEasyMediaPlay? = null

    /**
     * 第二个VideoPlay,全屏
     */
    var videoFullscreen: BaseEasyMediaPlay? = null
        private set

    /**
     * 第三个VideoPlay,小窗口的
     */
    private var secondView: EasyMediaPlayTiny? = null

    var videoDefault: BaseEasyMediaPlay?
        get() = firstView
        set(videoPlayer) {
            if (videoPlayer != null && (videoFullscreen != null || secondView != null)) {
                backPress(videoPlayer.mediaManager)
            }
            firstView = videoPlayer
        }


    fun setVideoFullscreen(videoPlayer: BaseEasyMediaPlay?) {
        if (videoPlayer != null && videoFullscreen != null) {
            backPress(videoPlayer.mediaManager)
        }
        videoFullscreen = videoPlayer
    }

    /**
     * 获取小窗口播放器
     */
    var videoTiny: EasyMediaPlayTiny?
        get() = secondView
        set(videoPlayer) {
            if (videoPlayer != null && secondView != null) {
                backPress(videoPlayer.mediaManager)
            }
            secondView = videoPlayer
        }

    /**
     * 获取非小窗口播放器
     */
    val currentVideoPlayerNoTiny: BaseEasyMediaPlay?
        get() = if (videoFullscreen != null) {
            videoFullscreen
        } else videoDefault

    /**
     * 获取当前正在播放
     *
     * @return
     */

    val currentVideoPlay: BaseEasyMediaPlay?
        get() {
            var videoPlayer: BaseEasyMediaPlay? = videoTiny
            if (videoPlayer == null) {
                videoPlayer = currentVideoPlayerNoTiny
            }
            return videoPlayer
        }

    /**
     * 强制释放全部播放器
     */
    fun completeAll() {
        if (secondView != null) {
            secondView!!.onForceCompletionTo()
            secondView = null
        }
        if (videoFullscreen != null) {
            videoFullscreen!!.onForceCompletionTo()
            videoFullscreen = null
        }
        if (firstView != null) {
            firstView!!.onForceCompletionTo()
            firstView = null
        }
        mediaManager.currentDataSource = null
    }
}