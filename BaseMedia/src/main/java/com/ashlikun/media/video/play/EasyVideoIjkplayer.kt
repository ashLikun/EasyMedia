package com.ashlikun.media.video.play

import android.media.AudioManager
import android.text.TextUtils
import android.util.Log
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
 * RTSP 直播流低延迟
 */
inline fun IjkMediaPlayer.liveConfig() {
    val player = this
//    player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "overlay-format", IjkMediaPlayer.SDL_FCC_RV32.toLong())
    // 设置播放前的最大探测时间 （100未测试是否是最佳值）
    player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzemaxduration", 100L)
    //每处理一个packet之后刷新io上下文
    player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "flush_packets", 1L)
    //丢帧阈值 视频帧处理不过来的时候丢弃一些帧达到同步的效果
    player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1L)
    //视频帧率
    player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "fps", 30)
    //环路滤波 是否开启预缓冲，一般直播项目会开启，达到秒开的效果，不过带来了播放丢帧卡顿的体验, 0开启，画面质量高，解码开销大，48关闭，画面质量差点，解码开销小
    player.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48)
    //设置无packet缓存
    player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0)
    player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "fflags", "nobuffer")
    //不限制拉流缓存大小
    player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "infbuf", 1)
    //设置最大缓存数量
    player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "max-buffer-size", 1024)
    //设置最小解码帧数
    player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "min-frames", 10)
    //启动预加载
    player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 1)
    //设置探测包Size，默认是1M, 改小一点会出画面更快
    player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 1024L)
    player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "http-detect-range-support", 0)

    //设置播放前的探测时间 1,达到首屏秒开效果
    player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzeduration", 1)
    //ijkPlayer默认使用udp拉流，因为速度比较快。如果需要可靠且减少丢包，可以改为tcp协议：
//    player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "rtsp_transport", "tcp")

    //硬解码，如果打开硬解码失败，再自动切换到软解码
    player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1)
    player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1)
    player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 1)
}

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
        if (width > 0 && height > 0) {
            if (manager.mediaPlay === this) manager.handlePlayEvent.onVideoSizeChanged(mp.videoWidth, mp.videoHeight)
        } else {
            Log.d("EasyVideo", "width || height is 0   height = $height   width = $width")
        }
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
