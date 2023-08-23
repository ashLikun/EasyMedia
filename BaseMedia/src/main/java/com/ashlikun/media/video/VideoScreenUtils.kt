package com.ashlikun.media.video

import android.content.Context
import android.content.pm.ActivityInfo
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import com.ashlikun.media.R
import com.ashlikun.media.video.status.VideoStatus
import com.ashlikun.media.video.view.BaseEasyMediaPlay
import com.ashlikun.media.video.view.EasyMediaPlayTiny
import com.ashlikun.media.video.view.EasyMediaPlayer

/**
 * 作者　　: 李坤
 * 创建时间: 2018/8/20　14:41
 * 邮箱　　：496546144@qq.com
 *
 *
 * 功能介绍：播放器全屏，或者小窗口，还有一些屏幕相关的工具
 */
object VideoScreenUtils {
    /**
     * 返回键暴力时间
     */
    const val FULL_SCREEN_NORMAL_DELAY = 300

    /**
     * 1:返回键，第一次按返回的时间,
     * 2:还有就是释放视频渲染器时候释放可以释放，比如当小视频点击全屏按钮就不能释放,直接全屏就得释放
     */

    var CLICK_QUIT_FULLSCREEN_TIME: Long = 0

    /**
     * Activity是否存在 FLAG_FULLSCREEN
     */
    var FLAG_FULLSCREEN_EXIST = true

    /**
     * 直接开始全屏播放
     */
    fun startFullscreen(easyVideoPlayer: EasyMediaPlayer, url: String, title: String = "") {
        val mediaData: MutableList<VideoData> = ArrayList()
        mediaData.add(VideoData(url, title))
        startFullscreen(easyVideoPlayer, true, mediaData, 0)
    }

    /**
     * 设置数据源
     */
    fun startFullscreen(easyVideoPlayer: EasyMediaPlayer, data: VideoData) {
        val mediaData: MutableList<VideoData> = ArrayList()
        mediaData.add(data)
        startFullscreen(easyVideoPlayer, true, mediaData, 0)
    }

    /**
     * 开启自动横竖屏
     */
    fun startAutoScreenOrientation(context: Context) = ScreenOrientationSwitcher(context).also { it.enable() }

    /**
     * 直接开始全屏播放
     *
     * @param easyVideoPlayer 请实例化一个播放器
     * @param isFirst         是否第一次播放这个链接
     * @param mediaData       地址  或者 AssetFileDescriptor
     * @param defaultIndex    第几个
     */
    fun startFullscreen(easyVideoPlayer: BaseEasyMediaPlay, isFirst: Boolean, mediaData: List<VideoData>, defaultIndex: Int) {
        if (isFirst) {
            VideoUtils.releaseAll(easyVideoPlayer.mediaManageTag)
        }
        setActivityFullscreen(easyVideoPlayer.context, true)
        startFullscreenOrientation(easyVideoPlayer)

        val vp = VideoUtils.getDecorView(easyVideoPlayer.context)
        val old = vp.findViewById<View>(R.id.easy_video_fullscreen_id)
        if (old != null) {
            vp.removeView(old)
        }
        easyVideoPlayer.id = R.id.easy_video_fullscreen_id
        val lp = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        )
        vp.addView(easyVideoPlayer, lp)
        val ra = AnimationUtils.loadAnimation(easyVideoPlayer.context, R.anim.easy_video_start_fullscreen)
        easyVideoPlayer.animation = ra
        easyVideoPlayer.systemUiVisibility = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_FULLSCREEN)
        easyVideoPlayer.isFull = true
        val status = easyVideoPlayer.currentState
        easyVideoPlayer.setDataSource(mediaData, defaultIndex)
        easyVideoPlayer.setStatus(status)
        if (easyVideoPlayer.currentState == VideoStatus.NORMAL) {
            easyVideoPlayer.onPlayStartClick()
        }
        CLICK_QUIT_FULLSCREEN_TIME = System.currentTimeMillis()
        easyVideoPlayer.mediaManager.viewManager.setVideoFullscreen(easyVideoPlayer)
    }

    /**
     * 直接退出全屏和小窗
     */
    fun quitFullscreenOrTinyWindow(mediaManager: EasyMediaManager) {
        if (mediaManager.viewManager.videoFullscreen != null) {
            if (mediaManager.viewManager.videoFullscreen is View) {
                clearFloatScreen(mediaManager.viewManager.videoFullscreen!!.context, mediaManager)
            }
        }
        if (mediaManager.viewManager.videoTiny != null) {
            mediaManager.viewManager.videoTiny?.cleanTiny()
        }
        mediaManager.releaseMediaPlayer(true)
    }

    /**
     * 清空全屏
     */
    fun clearFloatScreen(context: Context?, mediaManager: EasyMediaManager) {
        val currentPlay = mediaManager.viewManager.videoFullscreen
        if (currentPlay != null) {
            exitFullscreenOrientation(currentPlay)
        }
        setActivityFullscreen(context, false)
        if (currentPlay != null) {
            currentPlay.removeTextureView()
            if (currentPlay is View) {
                VideoUtils.getDecorView(context).removeView(currentPlay)
            }
            mediaManager.viewManager.setVideoFullscreen(null)
        }
    }

    /**
     * 设置activity全屏
     */
    fun setActivityFullscreen(context: Context?, isFullscreen: Boolean) {
        if (isFullscreen) {
            val flag = VideoUtils.getWindow(context).attributes.flags
            FLAG_FULLSCREEN_EXIST = (flag and WindowManager.LayoutParams.FLAG_FULLSCREEN
                    == WindowManager.LayoutParams.FLAG_FULLSCREEN)
            if (!FLAG_FULLSCREEN_EXIST) {
                VideoUtils.getWindow(context).setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
                )
            }
        } else {
            if (!FLAG_FULLSCREEN_EXIST) {
                VideoUtils.getWindow(context).clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            }
        }
    }

    /**
     * 从当前Activity里面清除全屏View
     */

    fun clearFullscreenLayout(context: Context) {
        val vp = VideoUtils.getDecorView(context)
        val oldF = vp.findViewById<View>(R.id.easy_video_fullscreen_id)
        if (oldF != null) {
            vp.removeView(oldF)
        }
        setActivityFullscreen(context, false)
    }

    /**
     * 暴力点击是否满足
     *
     * @return
     */
    val isBackOk: Boolean
        get() = System.currentTimeMillis() - CLICK_QUIT_FULLSCREEN_TIME > FULL_SCREEN_NORMAL_DELAY

    /**
     * 返回键点击,一般用于退出全屏或小窗口
     *
     * @return
     */
    fun onBackPressed(mediaManager: EasyMediaManager = EasyMediaManager.getVideoDefault()): Boolean {
        if (!isBackOk) {
            return false
        }
        CLICK_QUIT_FULLSCREEN_TIME = System.currentTimeMillis()
        if (mediaManager.viewManager.videoFullscreen != null || mediaManager.viewManager.videoTiny != null) {
            if (mediaManager.viewManager.videoDefault != null) {
                if (VideoUtils.isContainsUri(mediaManager.viewManager.videoDefault!!.mediaData, mediaManager.currentDataSource)) {
                    //如果默认的Video播放过视频,就直接在这个默认的上面播放
                    mediaManager.viewManager.videoDefault?.onEvent(EasyMediaEvent.ON_QUIT_FULLSCREEN)
                } else {
                    //直接退出全屏或者小窗口
                    quitFullscreenOrTinyWindow(mediaManager)
                }
            } else {
                quitFullscreenOrTinyWindow(mediaManager)
            }
            return true
        }
        return false
    }

    /**
     * 开始小窗口播放
     */

    fun startWindowTiny(tiny: EasyMediaPlayTiny, mediaData: List<VideoData>, defaultIndex: Int): Boolean {
        val videoPlayerDefault = tiny.mediaManager.viewManager.currentVideoPlayerNoTiny
        if (videoPlayerDefault != null && (videoPlayerDefault.currentState == VideoStatus.NORMAL || videoPlayerDefault.currentState == VideoStatus.ERROR || videoPlayerDefault.currentState == VideoStatus.AUTO_COMPLETE)) {
            return false
        }
        videoPlayerDefault?.removeTextureView()
        tiny.id = R.id.easy_video_tiny_id
        tiny.setDataSource(mediaData, defaultIndex)
        tiny.addTextureView()
        tiny.showWindow()
        if (videoPlayerDefault == null && !VideoUtils.isContainsUri(mediaData, tiny.mediaManager.currentDataSource)) {
            tiny.onPlayStartClick()
        } else {
            tiny.currentState = videoPlayerDefault!!.currentState
        }
        return true
    }

    /**
     * 从一个视频替换到另一个视频
     * 前提两个视频播放的是同一个
     * 一般用于页面跳转
     */
    fun startCacheVideo(newVideoPlay: BaseEasyMediaPlay?): Boolean {
        return if (newVideoPlay == null) false else startCacheVideo(newVideoPlay, newVideoPlay.currentData)
    }

    fun startCacheVideo(newVideoPlay: BaseEasyMediaPlay, newPlayData: VideoData?): Boolean {
        val oldVideo = newVideoPlay.mediaManager.viewManager.currentVideoPlay
        if (oldVideo != null && newVideoPlay !== oldVideo && newPlayData != null &&
            newPlayData.equalsUrl(oldVideo.currentData)
        ) {
            oldVideo.removeTextureView()
            newVideoPlay.addTextureView()
            newVideoPlay.copyPlay(oldVideo)
            newVideoPlay.saveVideoPlayView()
            return true
        }
        return false
    }


    /**
     * 退出全屏
     */
    fun exitFullscreenOrientation(videoPlayer: BaseEasyMediaPlay) {
        VideoUtils.setRequestedOrientation(videoPlayer.context, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
    }

    /**
     * 进入全屏
     */
    fun startFullscreenOrientation(videoPlayer: BaseEasyMediaPlay) {
        VideoUtils.setRequestedOrientation(
            videoPlayer.context, calculateOrientation(videoPlayer.mediaManager, videoPlayer.fullscreenPortrait)
        )
    }

    /**
     * 计算全屏方向
     * 0:自动判断(宽高比是否可以竖屏)
     * 1:可以竖屏(2个横屏，一个竖屏)
     * 2:不可以竖屏(2个横屏)
     */
    fun calculateOrientation(mediaManager: EasyMediaManager, fullscreenPortrait: Int): Int {
        var orientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        if (fullscreenPortrait == 0) {
            if (mediaManager.textureView != null && mediaManager.textureView?.isSizeOk == true) {
                orientation =
                    if (mediaManager.textureView?.isPortrait == true) ActivityInfo.SCREEN_ORIENTATION_SENSOR else ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            }
        } else if (fullscreenPortrait == 1) {
            orientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        } else if (fullscreenPortrait == 2) {
            orientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        } else if (fullscreenPortrait == 3) {
            orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
        return orientation
    }
}