package com.ashlikun.media.music

import android.media.MediaPlayer
import com.ashlikun.media.video.EasyMediaInterface
import com.ashlikun.media.video.EasyMediaManager
import com.ashlikun.media.video.HandlePlayEvent

/**
 * 作者　　: 李坤
 * 创建时间: 2020/11/18　17:40
 * 邮箱　　：496546144@qq.com
 *
 *
 * 功能介绍：
 */
class HandleMusicPlayEvent(val mediaManager: EasyMediaManager) : HandlePlayEvent {
    override fun onPause() {
        mediaManager.mediaHandler.post {
            mediaManager.musicViewManager.currentMusicPlay?.onPause()
        }
    }

    override fun onPrepared() {
        mediaManager.mediaHandler.post {
            mediaManager.musicViewManager.currentMusicPlay?.onPrepared()
        }
    }

    override fun setBufferProgress(percent: Int) {
        mediaManager.mediaHandler.post {
            mediaManager.musicViewManager.currentMusicPlay?.setBufferProgress(percent)
        }
    }

    override fun onError(what: Int, extra: Int) {
        mediaManager.mediaHandler.post {
            mediaManager.musicViewManager.currentMusicPlay?.onError(what, extra)
        }
    }

    override fun onInfo(what: Int, extra: Int) {
        mediaManager.mediaHandler.post {
            if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                mediaManager.musicViewManager.currentMusicPlay?.onPrepared()
            } else {
                mediaManager.musicViewManager.currentMusicPlay?.onInfo(what, extra)
            }
        }
    }

    override fun onVideoSizeChanged(width: Int, height: Int) {
        mediaManager.currentVideoWidth = width
        mediaManager.currentVideoHeight = height
        mediaManager.mediaHandler.post {
            mediaManager.musicViewManager.currentMusicPlay?.onVideoSizeChanged(width, height)
        }
    }

    override fun onSeekComplete() {
        mediaManager.mediaHandler.post {
            mediaManager.musicViewManager.currentMusicPlay?.onSeekComplete()
        }
    }

    override fun onCompletion(easyMediaInterface: EasyMediaInterface) {
        mediaManager.mediaHandler.post {
            if (mediaManager.musicViewManager.currentMusicPlay?.onAutoCompletion() == false) {
                easyMediaInterface.currentDataSource = null
            }
        }
    }
}