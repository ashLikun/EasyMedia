package com.ashlikun.media.video.play

import android.media.AudioManager
import android.media.MediaPlayer
import android.text.TextUtils
import android.view.Surface
import android.widget.Toast
import com.ashlikun.media.R
import com.ashlikun.media.video.EasyMediaInterface
import com.ashlikun.media.video.EasyMediaManager
import com.ashlikun.media.video.EasyMediaManager.Companion.pauseOther
import com.ashlikun.media.video.VideoUtils

/**
 * 作者　　: 李坤
 * 创建时间: 2017/12/01 16:25
 * 邮箱　　：496546144@qq.com
 *
 *
 * 功能介绍：实现系统的播放引擎
 */
class EasyVideoSystem(override val manager: EasyMediaManager) : EasyMediaInterface(manager), MediaPlayer.OnPreparedListener,
    MediaPlayer.OnCompletionListener, MediaPlayer.OnBufferingUpdateListener,
    MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener, MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnVideoSizeChangedListener {
    /**
     * 是否调用过暂定，调用后在准备好的时候不能直接播放
     */
    private var isPreparedPause = false
    var mediaPlayer: MediaPlayer? = null
    override fun setPreparedPause(preparedPause: Boolean) {
        isPreparedPause = preparedPause
    }

    override fun start() {
        pauseOther(manager)
        if (mediaPlayer != null) {
            mediaPlayer!!.start()
        }
    }

    override fun stop() {
        if (mediaPlayer != null) {
            mediaPlayer!!.stop()
        }
    }

    override fun prepare() {
        try {
            if (isPlaying) {
                stop()
            }
            release()
            mediaPlayer = MediaPlayer()
            if (currentDataSource == null) {
                Toast.makeText(VideoUtils.context, VideoUtils.context.getText(R.string.easy_video_no_url), Toast.LENGTH_SHORT).show()
                onError(mediaPlayer!!, -2, -2)
                return
            }
            mediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
            //  mediaPlayer.setLooping((boolean) dataSource[1]);
            mediaPlayer!!.setOnPreparedListener(this@EasyVideoSystem)
            mediaPlayer!!.setOnCompletionListener(this@EasyVideoSystem)
            mediaPlayer!!.setOnBufferingUpdateListener(this@EasyVideoSystem)
            mediaPlayer!!.setScreenOnWhilePlaying(true)
            mediaPlayer!!.setOnErrorListener(this@EasyVideoSystem)
            mediaPlayer!!.setOnInfoListener(this@EasyVideoSystem)
            mediaPlayer!!.setOnVideoSizeChangedListener(this@EasyVideoSystem)
            mediaPlayer!!.setOnSeekCompleteListener(this)
            mediaPlayer!!.isLooping = currentDataSource!!.isLooping
            if (!TextUtils.isEmpty(currentDataSource!!.url)) {
                if (currentDataSource!!.headers != null) {
                    val clazz = MediaPlayer::class.java
                    val method = clazz.getDeclaredMethod("setDataSource", String::class.java, MutableMap::class.java)
                    method.invoke(mediaPlayer, currentDataSource!!.url, currentDataSource!!.headers)
                } else {
                    mediaPlayer!!.setDataSource(currentDataSource!!.url)
                }
            } else if (currentDataSource!!.uri != null && !TextUtils.isEmpty(currentDataSource!!.uri.toString())) {
                if (currentDataSource!!.headers != null) {
                    mediaPlayer!!.setDataSource(VideoUtils.context, currentDataSource!!.uri!!, currentDataSource!!.headers)
                } else {
                    mediaPlayer!!.setDataSource(VideoUtils.context, currentDataSource!!.uri!!)
                }
            } else if (currentDataSource!!.fileDescriptor != null) {
                mediaPlayer!!.setDataSource(currentDataSource!!.fileDescriptor)
            } else {
                Toast.makeText(VideoUtils.context, VideoUtils.context.getText(R.string.easy_video_no_url), Toast.LENGTH_SHORT).show()
                onError(mediaPlayer!!, -2, -2)
                return
            }
            mediaPlayer!!.prepareAsync()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(VideoUtils.context, VideoUtils.context.getText(R.string.easy_video_no_url), Toast.LENGTH_SHORT).show()
            onError(mediaPlayer!!, -2, -2)
        }
    }

    override fun pause() {
        if (mediaPlayer != null) {
            mediaPlayer!!.pause()
        }
    }

    override val isPlaying: Boolean
        get() {
            if (mediaPlayer == null) {
                return false
            }
            return if (mediaPlayer != null) {
                mediaPlayer!!.isPlaying
            } else false
        }

    override fun seekTo(time: Long) {
        mediaPlayer?.seekTo(time.toInt())
    }

    override fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override val currentPosition: Long
        get() = mediaPlayer?.currentPosition?.toLong() ?: 0
    override val bufferedPercentage: Int
        get() = -1
    override val duration: Long
        get() = mediaPlayer?.duration?.toLong() ?: 0

    override fun setSurface(surface: Surface) {
        mediaPlayer?.setSurface(surface)
    }

    override fun onPrepared(mediaPlayer: MediaPlayer) {
        if (!isPreparedPause) {
            mediaPlayer.start()
            if (manager.mediaPlay === this) manager.handlePlayEvent.onPrepared()
        } else {
            isPreparedPause = false
        }
    }

    override fun onCompletion(mediaPlayer: MediaPlayer) {
        if (manager.mediaPlay === this) manager.handlePlayEvent.onCompletion(this)
    }

    override fun onBufferingUpdate(mediaPlayer: MediaPlayer, percent: Int) {
        if (manager.mediaPlay === this) manager.handlePlayEvent.setBufferProgress(percent)
    }

    override fun onError(mediaPlayer: MediaPlayer, what: Int, extra: Int): Boolean {
        if (manager.mediaPlay === this) manager.handlePlayEvent.onError(what, extra)
        return true
    }

    override fun onInfo(mediaPlayer: MediaPlayer, what: Int, extra: Int): Boolean {
        if (manager.mediaPlay === this) manager.handlePlayEvent.onInfo(what, extra)
        return false
    }

    override fun onVideoSizeChanged(mediaPlayer: MediaPlayer, width: Int, height: Int) {
        if (manager.mediaPlay === this) manager.handlePlayEvent.onVideoSizeChanged(width, height)
    }

    override fun onSeekComplete(mp: MediaPlayer) {
        if (manager.mediaPlay === this) manager.handlePlayEvent.onSeekComplete()
    }
}