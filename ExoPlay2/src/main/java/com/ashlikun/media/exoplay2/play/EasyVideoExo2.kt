package com.ashlikun.media.exoplay2.play

import android.media.AudioManager
import android.net.Uri
import android.view.Surface
import android.widget.Toast
import com.ashlikun.media.R
import com.ashlikun.media.exoplay2.IjkExo2MediaPlayer
import com.ashlikun.media.video.EasyMediaInterface
import com.ashlikun.media.video.EasyMediaManager
import com.ashlikun.media.video.VideoUtils
import tv.danmaku.ijk.media.player.IMediaPlayer
import java.io.File

/**
 * @author　　: 李坤
 * 创建时间: 2023/6/1 17:39
 * 邮箱　　：496546144@qq.com
 *
 * 功能介绍：实现Exo3的播放引擎
 */
class EasyVideoExo2(override val manager: EasyMediaManager) : EasyMediaInterface(manager), IMediaPlayer.OnPreparedListener,
    IMediaPlayer.OnCompletionListener,
    IMediaPlayer.OnBufferingUpdateListener, IMediaPlayer.OnErrorListener, IMediaPlayer.OnInfoListener, IMediaPlayer.OnSeekCompleteListener,
    IMediaPlayer.OnVideoSizeChangedListener {
    /**
     * 是否调用过暂定，调用后在准备好的时候不能直接播放
     */
    private var isPreparedPause = false
    var mediaPlayer: IjkExo2MediaPlayer? = null
    override fun setPreparedPause(isPreparedPause: Boolean) {
        this.isPreparedPause = isPreparedPause
    }

    override fun start() {
        //暂停不在同一个管理器的播放器
        EasyMediaManager.pauseOther(manager)
        mediaPlayer?.start()
    }

    override fun stop() {
        mediaPlayer?.stop()
    }

    override fun prepare() {
        runCatching {
            if (isPlaying) stop()
            release()
        }
        mediaPlayer = IjkExo2MediaPlayer(VideoUtils.context)
        if (currentDataSource == null) {
            Toast.makeText(VideoUtils.context, VideoUtils.context.getText(R.string.easy_video_no_url), Toast.LENGTH_SHORT).show()
            onError(mediaPlayer!!, -2, -2)
            return
        }
        mediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
        mediaPlayer!!.setOnPreparedListener(this@EasyVideoExo2)
        mediaPlayer!!.setOnCompletionListener(this@EasyVideoExo2)
        mediaPlayer!!.setOnBufferingUpdateListener(this@EasyVideoExo2)
        mediaPlayer!!.setScreenOnWhilePlaying(true)
        mediaPlayer!!.setOnErrorListener(this@EasyVideoExo2)
        mediaPlayer!!.setOnInfoListener(this@EasyVideoExo2)
        mediaPlayer!!.setOnVideoSizeChangedListener(this@EasyVideoExo2)
        mediaPlayer!!.setOnSeekCompleteListener(this)
        mediaPlayer!!.isLooping = currentDataSource!!.isLooping
        //通过自己的内部缓存机制
        mediaPlayer!!.isCache = currentDataSource!!.isCache
        if (currentDataSource!!.cacheDir.isNotEmpty()) {
            mediaPlayer!!.cacheDir = File(currentDataSource!!.cacheDir)
        }
        mediaPlayer!!.overrideExtension = currentDataSource!!.overrideExtension
        try {
            if (currentDataSource!!.url.isNotEmpty()) {
                if (currentDataSource!!.headers != null) {
                    mediaPlayer!!.setDataSource(VideoUtils.context, Uri.parse(currentDataSource!!.url), currentDataSource!!.headers)
                } else {
                    mediaPlayer!!.dataSource = currentDataSource!!.url
                }
            } else if (currentDataSource!!.uri != null && currentDataSource!!.uri.toString().isNotEmpty()) {
                if (currentDataSource!!.headers != null) {
                    mediaPlayer!!.setDataSource(VideoUtils.context, currentDataSource!!.uri, currentDataSource!!.headers)
                } else {
                    mediaPlayer!!.setDataSource(VideoUtils.context, currentDataSource!!.uri)
                }
            } else if (currentDataSource!!.iMediaDataSource != null) {
                mediaPlayer!!.setDataSource(currentDataSource!!.iMediaDataSource)
            } else if (currentDataSource!!.fileDescriptor != null) {
                mediaPlayer!!.setDataSource(currentDataSource!!.fileDescriptor)
            } else {
                Toast.makeText(VideoUtils.context, VideoUtils.context.getText(R.string.easy_video_no_url), Toast.LENGTH_SHORT).show()
                onError(mediaPlayer!!, -2, -2)
                return
            }
            mediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
            mediaPlayer!!.setScreenOnWhilePlaying(true)
            mediaPlayer!!.prepareAsync()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(VideoUtils.context, VideoUtils.context.getText(R.string.easy_video_no_url), Toast.LENGTH_SHORT).show()
            onError(mediaPlayer!!, -2, -2)
        }
    }

    override fun pause() {
        mediaPlayer?.pause()
    }

    override val isPlaying: Boolean
        get() = mediaPlayer?.isPlaying ?: false

    override fun seekTo(time: Long) {
        mediaPlayer?.seekTo(time.toInt().toLong())
    }

    override fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override val currentPosition: Long
        get() = mediaPlayer?.currentPosition ?: 0

    override val bufferedPercentage: Int
        get() = mediaPlayer?.bufferedPercentage ?: 0

    override val duration: Long
        get() = mediaPlayer?.duration ?: 0

    override fun setSurface(surface: Surface) {
        mediaPlayer?.setSurface(surface)
    }

    override fun onPrepared(mediaPlayer: IMediaPlayer) {
        if (!isPreparedPause) {
            mediaPlayer.start()
            if (manager.mediaPlay === this) manager.handlePlayEvent.onPrepared()
        } else {
            isPreparedPause = false
        }
    }

    override fun onCompletion(mediaPlayer: IMediaPlayer) {
        if (manager.mediaPlay === this) manager.handlePlayEvent.onCompletion(this)
    }

    override fun onBufferingUpdate(mediaPlayer: IMediaPlayer, percent: Int) {
        if (manager.mediaPlay === this) manager.handlePlayEvent.setBufferProgress(percent)
    }

    override fun onError(mediaPlayer: IMediaPlayer, what: Int, extra: Int): Boolean {
        if (manager.mediaPlay === this) manager.handlePlayEvent.onError(what, extra)
        return true
    }

    override fun onInfo(mediaPlayer: IMediaPlayer, what: Int, extra: Int): Boolean {
        if (manager.mediaPlay === this) manager.handlePlayEvent.onInfo(what, extra)
        return false
    }

    override fun onVideoSizeChanged(mediaPlayer: IMediaPlayer, width: Int, height: Int, sar_num: Int, sar_den: Int) {
        if (manager.mediaPlay === this) manager.handlePlayEvent.onVideoSizeChanged(width, height)
    }

    override fun onSeekComplete(mp: IMediaPlayer) {
        if (manager.mediaPlay === this) manager.handlePlayEvent.onSeekComplete()
    }
}