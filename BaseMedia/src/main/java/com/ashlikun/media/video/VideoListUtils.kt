package com.ashlikun.media.video

import android.view.View
import com.ashlikun.media.video.VideoScreenUtils.onBackPressed
import com.ashlikun.media.video.VideoScreenUtils.startWindowTiny
import com.ashlikun.media.video.VideoUtils.isContainsUri
import com.ashlikun.media.video.VideoUtils.releaseAll
import com.ashlikun.media.video.status.VideoStatus
import com.ashlikun.media.video.view.EasyMediaPlayTiny
import com.ashlikun.media.video.view.EasyMediaPlayer

/**
 * 作者　　: 李坤
 * 创建时间: 2018/8/20　14:38
 * 邮箱　　：496546144@qq.com
 *
 *
 * 功能介绍：播放器列表的一些工具方法
 */
object VideoListUtils {
    /**
     * 当子view附属到窗口时候
     * 这2个方法是给列表使用的
     */
    fun onChildViewAttachedToWindow(view: View, vidoPlayId: Int) {
        val videoPlayer = view.findViewById<EasyMediaPlayer>(vidoPlayId) ?: return
        if (videoPlayer.mediaManager.viewManager.videoTiny != null) {
            if (videoPlayer.mediaData.getOrNull(videoPlayer.currentUrlIndex)?.equals(videoPlayer.mediaManager.currentDataSource) == true) {
                onBackPressed(videoPlayer.mediaManager)
            }
        }
    }

    /**
     * 当子view从窗口分离
     */
    fun onChildViewDetachedFromWindow(view: View, vidoPlayId: Int) {
        val videoPlayer = view.findViewById<EasyMediaPlayer>(vidoPlayId) ?: return
        if (videoPlayer.mediaManager.viewManager.videoTiny != null) {
            if (videoPlayer === videoPlayer.mediaManager.viewManager.currentVideoPlayerNoTiny) {
                if (videoPlayer.currentState === VideoStatus.PAUSE) {
                    releaseAll(videoPlayer.mediaManager.tag)
                } else {
                    if (startWindowTiny(EasyMediaPlayTiny(videoPlayer.context), videoPlayer.mediaData, videoPlayer.currentUrlIndex)) {
                        //还原默认状态
                        videoPlayer.setStatus(VideoStatus.NORMAL)
                    }
                }
            }
        }
    }

    /**
     * ListView列表滑动时候自动小窗口
     * RecyclerView请用recyclerView.addOnChildAttachStateChangeListener
     *
     * @param firstVisibleItem 第一个有效的Item
     * @param visibleItemCount 一共有效的Item
     */
    fun onScrollAutoTiny(currentPlayPosition: Int, firstVisibleItem: Int, visibleItemCount: Int, mediaManager: EasyMediaManager) {
        val lastVisibleItem = firstVisibleItem + visibleItemCount
        if (currentPlayPosition >= 0) {
            if (currentPlayPosition < firstVisibleItem || currentPlayPosition > lastVisibleItem - 1) {
                if (mediaManager.viewManager.videoTiny == null) {
                    if (mediaManager.viewManager.currentVideoPlayerNoTiny?.currentState === VideoStatus.PAUSE) {
                        releaseAll(mediaManager.tag)
                    } else {
                        mediaManager.viewManager.currentVideoPlayerNoTiny?.also {
                            startWindowTiny(EasyMediaPlayTiny(it.context), it.mediaData, it.currentUrlIndex)
                            it.setStatus(VideoStatus.NORMAL)
                        }
                    }
                }
            } else {
                if (mediaManager.viewManager.videoTiny != null) {
                    onBackPressed(mediaManager)
                }
            }
        }
    }

    /**
     * 列表滑动时候清空全部播放  ListView用的
     * RecyclerView请用recyclerView.addOnChildAttachStateChangeListener
     *
     * @param currentPlayPosition 当前正在播放的item
     * @param firstVisibleItem    第一个有效的Item
     * @param visibleItemCount    一共有效的Item
     */
    fun onScrollReleaseAllVideos(currentPlayPosition: Int, firstVisibleItem: Int, visibleItemCount: Int, mediaManager: EasyMediaManager) {
        val lastVisibleItem = firstVisibleItem + visibleItemCount
        if (currentPlayPosition >= 0) {
            if (currentPlayPosition < firstVisibleItem || currentPlayPosition > lastVisibleItem - 1) {
                releaseAll(mediaManager.tag)
            }
        }
    }

    /**
     * RecyclerView列表滑动时候自动小窗口
     *
     * @param isChileViewDetached 子view是否分离 true:分离，false:添加
     */
    fun onRecyclerAutoTiny(videoPlayer: EasyMediaPlayer, isChileViewDetached: Boolean, mediaManager: EasyMediaManager = videoPlayer.mediaManager) {
        if (!isContainsUri(videoPlayer.mediaData, mediaManager.currentDataSource)) {
            //如果当前的view播放的视频地址不是正在播放的视频地址就过滤掉这次
            return
        }
        //这里一定是在播放的
        if (isChileViewDetached) {
            if (mediaManager.viewManager.videoTiny == null) {
                if (mediaManager.viewManager.currentVideoPlayerNoTiny?.currentState === VideoStatus.PAUSE) {
                    releaseAll(mediaManager.tag)
                } else {
                    if (startWindowTiny(EasyMediaPlayTiny(videoPlayer.context), videoPlayer.mediaData, videoPlayer.currentUrlIndex)) {
                        videoPlayer.setStatus(VideoStatus.NORMAL)
                        //列表的时候如果进入小窗口，那么久把列表的view设为null,可能回收后就不对了
                        mediaManager.viewManager.videoDefault = null
                    }
                }
            }
        } else if (mediaManager.viewManager.videoTiny != null) {
            //回来的时候再把这个保存,对应于上面的设为null
            videoPlayer.saveVideoPlayView()
            onBackPressed(mediaManager)
        }
    }

    /**
     * RecyclerView滑动时候清空全部播放
     * RecyclerView请用recyclerView.addOnChildAttachStateChangeListener
     *
     * @param videoPlayer 当前子view播放器
     */
    fun onRecyclerRelease(videoPlayer: EasyMediaPlayer, mediaManager: EasyMediaManager = videoPlayer.mediaManager) {
        //如果当前的view播放的视频地址不是正在播放的视频地址就过滤掉这次
        if (!isContainsUri(videoPlayer.mediaData, mediaManager.currentDataSource)) {
            return
        }
        releaseAll(mediaManager.tag)
    }
}