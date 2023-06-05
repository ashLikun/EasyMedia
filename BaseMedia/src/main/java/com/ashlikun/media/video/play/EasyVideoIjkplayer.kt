package com.ashlikun.media.video.play

import android.media.AudioManager
import android.text.TextUtils
import android.view.Surface
import android.widget.Toast
import com.ashlikun.media.R
import com.ashlikun.media.video.EasyMediaInterface
import com.ashlikun.media.video.EasyMediaManager
import com.ashlikun.media.video.EasyMediaManager.Companion.pauseOther
import com.ashlikun.media.video.VideoData
import com.ashlikun.media.video.VideoUtils
import tv.danmaku.ijk.media.player.IMediaPlayer
import tv.danmaku.ijk.media.player.IjkMediaPlayer
import tv.danmaku.ijk.media.player.IjkTimedText
import java.io.IOException

/**
 * 作者　　: 李坤
 * 创建时间: 2017/12/15 17:02
 * 邮箱　　：496546144@qq.com
 *
 *
 * 功能介绍：实现Ijkplayer的播放引擎
 */
/**
 * 对IjkPlayer的一些其他配置
 */
class EasyVideoIjkplayer(override val manager: EasyMediaManager) : EasyMediaInterface(manager), IMediaPlayer.OnPreparedListener,
    IMediaPlayer.OnVideoSizeChangedListener,
    IMediaPlayer.OnCompletionListener, IMediaPlayer.OnErrorListener, IMediaPlayer.OnInfoListener, IMediaPlayer.OnBufferingUpdateListener,
    IMediaPlayer.OnSeekCompleteListener, IMediaPlayer.OnTimedTextListener {
    var ijkMediaPlayer: IjkMediaPlayer? = null

    /**
     * 是否调用过暂定，调用后在准备好的时候不能直接播放
     */
    private var isPreparedPause = false
    override fun setPreparedPause(preparedPause: Boolean) {
        isPreparedPause = preparedPause
    }

    override fun prepare() {
        if (isPlaying) {
            stop()
        }
        release()
        ijkMediaPlayer = IjkMediaPlayer()
        val ijkMediaPlayer = ijkMediaPlayer!!
        if (currentDataSource == null) {
            Toast.makeText(VideoUtils.context, VideoUtils.context.getText(R.string.easy_video_no_url), Toast.LENGTH_SHORT).show()
            onError(ijkMediaPlayer, -2, -2)
            return
        }

        //开启硬解码
        if (VideoUtils.isMediaCodec) {
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1)
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1)
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 1)
        }
        ijkMediaPlayer.setOnPreparedListener(this@EasyVideoIjkplayer)
        ijkMediaPlayer.setOnVideoSizeChangedListener(this@EasyVideoIjkplayer)
        ijkMediaPlayer.setOnCompletionListener(this@EasyVideoIjkplayer)
        ijkMediaPlayer.setOnErrorListener(this@EasyVideoIjkplayer)
        ijkMediaPlayer.setOnInfoListener(this@EasyVideoIjkplayer)
        ijkMediaPlayer.setOnBufferingUpdateListener(this@EasyVideoIjkplayer)
        ijkMediaPlayer.setOnSeekCompleteListener(this@EasyVideoIjkplayer)
        ijkMediaPlayer.setOnTimedTextListener(this@EasyVideoIjkplayer)
        ijkMediaPlayer.setLogEnabled(VideoUtils.isDebug)
        ijkMediaPlayer.isLooping = currentDataSource!!.isLooping

        //全局配置
        VideoUtils.onPlayerCreate?.invoke(this, currentDataSource!!, ijkMediaPlayer!!)
        //回调出去
        manager.viewManager?.currentVideoPlay?.onPlayerCreate?.invoke(this, currentDataSource!!, ijkMediaPlayer!!)
        try {
            if (!TextUtils.isEmpty(currentDataSource!!.url)) {
                if (currentDataSource!!.headers != null) {
                    ijkMediaPlayer.setDataSource(currentDataSource!!.url, currentDataSource!!.headers)
                } else {
                    ijkMediaPlayer.dataSource = currentDataSource!!.url
                }
            } else if (currentDataSource!!.uri != null && !TextUtils.isEmpty(currentDataSource!!.uri.toString())) {
                if (currentDataSource!!.headers != null) {
                    ijkMediaPlayer.setDataSource(VideoUtils.context, currentDataSource!!.uri, currentDataSource!!.headers)
                } else {
                    ijkMediaPlayer.setDataSource(VideoUtils.context, currentDataSource!!.uri)
                }
            } else if (currentDataSource!!.iMediaDataSource != null) {
                ijkMediaPlayer.setDataSource(currentDataSource!!.iMediaDataSource)
            } else if (currentDataSource!!.fileDescriptor != null) {
                ijkMediaPlayer.setDataSource(currentDataSource!!.fileDescriptor)
            } else {
                Toast.makeText(VideoUtils.context, VideoUtils.context.getText(R.string.easy_video_no_url), Toast.LENGTH_SHORT).show()
                onError(ijkMediaPlayer, -2, -2)
                return
            }
            ijkMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
            ijkMediaPlayer.setScreenOnWhilePlaying(true)
            ijkMediaPlayer.prepareAsync()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(VideoUtils.context, VideoUtils.context.getText(R.string.easy_video_no_url), Toast.LENGTH_SHORT).show()
            onError(ijkMediaPlayer, -2, -2)
        }
    }

    override fun start() {
        //暂停不在同一个管理器的播放器
        pauseOther(manager)
        ijkMediaPlayer?.start()
    }

    override fun pause() {
        ijkMediaPlayer?.pause()
    }

    override val isPlaying: Boolean
        get() = ijkMediaPlayer?.isPlaying ?: false

    override fun seekTo(time: Long) {
        ijkMediaPlayer?.seekTo(time)
    }

    override fun release() {
        ijkMediaPlayer?.release()
        ijkMediaPlayer = null
    }

    override val currentPosition: Long
        get() = ijkMediaPlayer?.currentPosition ?: 0
    override val bufferedPercentage: Int
        get() = -1
    override val duration: Long
        get() = ijkMediaPlayer?.duration ?: 0

    override fun setSurface(surface: Surface) {
        ijkMediaPlayer?.setSurface(surface)
    }

    override fun stop() {
        ijkMediaPlayer?.stop()
    }

    override fun onPrepared(iMediaPlayer: IMediaPlayer) {
        if (ijkMediaPlayer == null) return
        if (!isPreparedPause) {
            ijkMediaPlayer!!.start()
            if (manager.mediaPlay === this) manager.handlePlayEvent.onPrepared()
        } else {
            isPreparedPause = false
        }
    }

    override fun onVideoSizeChanged(mp: IMediaPlayer, width: Int, height: Int, sar_num: Int, sar_den: Int) {
        if (manager.mediaPlay === this) manager.handlePlayEvent.onVideoSizeChanged(mp.videoWidth, mp.videoHeight)
    }

    override fun onCompletion(iMediaPlayer: IMediaPlayer) {
        if (manager.mediaPlay === this) manager.handlePlayEvent.onCompletion(this)
    }

    override fun onError(iMediaPlayer: IMediaPlayer, what: Int, extra: Int): Boolean {
        if (manager.mediaPlay === this) manager.handlePlayEvent.onError(what, extra)
        return true
    }

    override fun onInfo(iMediaPlayer: IMediaPlayer, what: Int, extra: Int): Boolean {
        if (manager.mediaPlay === this) manager.handlePlayEvent.onInfo(what, extra)
        return false
    }

    override fun onBufferingUpdate(iMediaPlayer: IMediaPlayer, percent: Int) {
        if (manager.mediaPlay === this) manager.handlePlayEvent.setBufferProgress(percent)
    }

    override fun onSeekComplete(iMediaPlayer: IMediaPlayer) {
        if (manager.mediaPlay === this) manager.handlePlayEvent.onSeekComplete()
    }

    override fun onTimedText(iMediaPlayer: IMediaPlayer, ijkTimedText: IjkTimedText) {}

}
