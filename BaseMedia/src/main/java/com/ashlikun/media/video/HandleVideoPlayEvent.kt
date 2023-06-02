package com.ashlikun.media.video

import android.media.MediaPlayer

/**
 * 作者　　: 李坤
 * 创建时间: 2020/11/18　17:40
 * 邮箱　　：496546144@qq.com
 *
 *
 * 功能介绍：处理全局的视频播放事件
 */
class HandleVideoPlayEvent(val mediaManager: EasyMediaManager) : HandlePlayEvent {
    override fun onPause() {
        mediaManager.mediaHandler.post {
            mediaManager.viewManager.currentVideoPlay?.onPause()
        }
    }

    override fun onPrepared() {
        mediaManager.mediaHandler.post {
            mediaManager.viewManager.currentVideoPlay?.onPrepared()
        }
    }

    override fun setBufferProgress(percent: Int) {
        mediaManager.mediaHandler.post {
            mediaManager.viewManager.currentVideoPlay?.setBufferProgress(percent)
        }
    }

    override fun onError(what: Int, extra: Int) {
        mediaManager.mediaHandler.post {
            mediaManager.viewManager.currentVideoPlay?.onError(what, extra)
        }
    }

    override fun onInfo(what: Int, extra: Int) {
        mediaManager.mediaHandler.post {
            if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                mediaManager.viewManager.currentVideoPlay?.onPrepared()
            } else {
                mediaManager.viewManager.currentVideoPlay?.onInfo(what, extra)
            }
        }
    }

    override fun onVideoSizeChanged(width: Int, height: Int) {
        mediaManager.currentVideoWidth = width
        mediaManager.currentVideoHeight = height
        mediaManager.mediaHandler.post {
            mediaManager.viewManager.currentVideoPlay?.onVideoSizeChanged(width, height)
        }
    }

    override fun onSeekComplete() {
        mediaManager.mediaHandler.post {
            mediaManager.viewManager.currentVideoPlay?.onSeekComplete()
        }
    }

    override fun onCompletion(easyMediaInterface: EasyMediaInterface) {
        mediaManager.mediaHandler.post {
            if (mediaManager.viewManager.currentVideoPlay?.onAutoCompletion() == false) {
                easyMediaInterface!!.currentDataSource = null
            }
        }
    }
}