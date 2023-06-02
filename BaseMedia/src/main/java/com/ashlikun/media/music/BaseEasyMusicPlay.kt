package com.ashlikun.media.music

import android.content.Context
import android.media.MediaPlayer
import android.text.TextUtils
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.CallSuper
import com.ashlikun.media.R
import com.ashlikun.media.music.MusicUtils.releaseAll
import com.ashlikun.media.music.MusicUtils.setAudioFocus
import com.ashlikun.media.video.EasyMediaEvent
import com.ashlikun.media.video.EasyMediaManager
import com.ashlikun.media.video.NetworkUtils
import com.ashlikun.media.video.VideoData
import com.ashlikun.media.video.VideoUtils.isContainsUri
import com.ashlikun.media.video.listener.MediaBufferProgressCall
import com.ashlikun.media.video.listener.MediaErrorCall
import com.ashlikun.media.video.listener.MediaEventCall
import com.ashlikun.media.video.listener.MediaInfoCall
import com.ashlikun.media.video.listener.MediaSeekCompleteCall
import com.ashlikun.media.video.status.VideoStatus
import com.ashlikun.media.video.view.IEasyMediaPlayListener

/**
 * @author　　: 李坤
 * 创建时间: 2020/11/18 19:46
 * 邮箱　　：496546144@qq.com
 *
 *
 * 功能介绍：音频播放器基础类
 * * [.setDataSource] 去设置播放的数据源
 */
open class BaseEasyMusicPlay @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    FrameLayout(context, attrs, defStyleAttr), IEasyMediaPlayListener {
    /**
     * 使用管理器的标识，需要在初始化时候定义
     */
    open var mediaManageTag = EasyMediaManager.TAG_MUSIC
        set(value) {
            field = EasyMediaManager.TAG_MUSIC + value
            mediaManager = EasyMediaManager.getInstance(field)
        }
    var mediaManager = EasyMediaManager.getInstance(mediaManageTag)
        private set

    /**
     * 当前状态
     */
    var currentState = VideoStatus.NORMAL

    /**
     * 备份缓存前的播放状态
     */
    protected var mBackUpPlayingBufferState: VideoStatus? = null

    /**
     * 是否播放过
     */
    protected var mHadPlay = false
    /**
     * 获取播放器数据
     *
     * @return
     */
    /**
     * 数据源，列表
     */
    open var mediaData: List<VideoData> = emptyList()
        protected set

    /**
     * 当前播放到的列表数据源位置
     */
    var currentUrlIndex = 0
        protected set

    /**
     * 播放事件的回掉
     */
    private val musicEvents by lazy {
        mutableListOf<MediaEventCall>()
    }

    /**
     * 当onResume的时候是否去播放
     */
    private var ONRESUME_TO_PLAY = true

    /**
     * 从哪个开始播放
     */
    protected var mSeekOnStart: Long = -1
    open var onBufferProgressCall: MediaBufferProgressCall? = null
    open var onSeekCompleteCall: MediaSeekCompleteCall? = null
    open var onInfoCall: MediaInfoCall? = null
    open var onErrorCall: MediaErrorCall? = null

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.BaseEasyMusicPlay)
        if (a.hasValue(R.styleable.BaseEasyMusicPlay_music_manage_tag)) {
            mediaManageTag = a.getString(R.styleable.BaseEasyMusicPlay_music_manage_tag) ?: mediaManageTag
        }
    }

    /**
     * 设置数据源
     * @param url   视频ur
     * @param title 标题
     */
    fun setDataSource(url: String, title: String = "") {
        setDataSource(listOf(VideoData(url, title)), 0)
    }

    /**
     * 设置数据源
     *
     * @param data 视频ur
     */
    fun setDataSource(data: VideoData) {
        setDataSource(listOf(data), 0)
    }

    /**
     * 设置数据源
     */
    fun setDataSource(mediaData: List<VideoData>) {
        setDataSource(mediaData, 0)
    }

    /**
     * 设置数据源
     *
     * @param mediaData    视频数据，数组
     * @param defaultIndex 播放的url 位置 0 开始
     */
    open fun setDataSource(mediaData: List<VideoData>, defaultIndex: Int): Boolean {
        this.mediaData = mediaData
        //如果这个已经在播放就不管
        if (mediaData.getOrNull(defaultIndex) != null && isContainsUri(this.mediaData, mediaManager.mediaPlay.currentDataSource)) {
            saveMusicPlayView()
            if (currentState == VideoStatus.NORMAL) {
                setStatus(VideoStatus.NORMAL)
            }
            return false
        }
        if (isCurrentMusicPlay && isContainsUri(mediaData, mediaManager.mediaPlay.currentDataSource)) {
            //当前View正在播放视频  保存进度
            var position = runCatching { mediaManager.mediaPlay.currentPosition }.getOrNull() ?: 0
            mediaManager.releaseMediaPlayer()
        }
        currentUrlIndex = defaultIndex
        setStatus(VideoStatus.NORMAL)
        return true
    }

    /**
     * 保存播放器 用于全局管理
     * 可能会多次调用
     */
    fun saveMusicPlayView() {
        mediaManager.musicViewManager.musicDefault = this
    }

    /**
     * 开始播放
     * 必须在设置完数据源后
     */
    fun startMusic() {
        //释放播放器
        mediaManager.releaseMediaPlayer()
        mediaManager.mediaPlay.currentDataSource = currentData
        //获取音频焦点
        setAudioFocus(context, true)
        saveMusicPlayView()
        mediaManager.prepare()
        setStatus(VideoStatus.PREPARING)
    }

    /**
     * 切换数据源
     *
     * @param position
     * @return
     */
    fun switchData(position: Int): Boolean {
        val old = currentUrlIndex
        currentUrlIndex = position
        return if (currentData != null) {
            startMusic()
            true
        } else {
            currentUrlIndex = old
            false
        }
    }

    /**
     * 设置当前播放器状态
     */
    fun setStatus(state: VideoStatus): Boolean {
        if (currentState == state) {
            return false
        }
        when (state) {
            VideoStatus.NORMAL -> {
                onStateNormal()
                onEvent(EasyMediaEvent.ON_STATUS_NORMAL)
            }

            VideoStatus.PREPARING -> {
                onStatePreparing()
                onEvent(EasyMediaEvent.ON_STATUS_PREPARING)
            }

            VideoStatus.PLAYING -> {
                onStatePlaying()
                onEvent(EasyMediaEvent.ON_STATUS_PLAYING)
            }

            VideoStatus.PAUSE -> {
                onStatePause()
                onEvent(EasyMediaEvent.ON_STATUS_PAUSE)
            }

            VideoStatus.ERROR -> {
                onStateError()
                onEvent(EasyMediaEvent.ON_STATUS_ERROR)
            }

            VideoStatus.AUTO_COMPLETE -> {
                onStateAutoComplete()
                onEvent(EasyMediaEvent.ON_STATUS_AUTO_COMPLETE)
                onEvent(EasyMediaEvent.ON_STATUS_COMPLETE)
            }

            VideoStatus.FORCE_COMPLETE -> {
                onStateNormal()
                onEvent(EasyMediaEvent.ON_STATUS_FORCE_COMPLETE)
                onEvent(EasyMediaEvent.ON_STATUS_COMPLETE)
            }

            VideoStatus.BUFFERING_START -> {
                onBufferStart()
                onEvent(EasyMediaEvent.ON_STATUS_BUFFERING_START)
            }
        }
        return true
    }
    /********************************************************************************************
     * 设置播放器状态后的回调
     */
    /**
     * 设置当前初始状态
     */
    protected fun onStateNormal() {
        currentState = VideoStatus.NORMAL
    }

    /**
     * 当准备时候
     */
    protected fun onStatePreparing() {
        currentState = VideoStatus.PREPARING
    }

    protected fun onStatePrepared() {
        //因为这个紧接着就会进入播放状态，所以不设置state
    }

    /**
     * 开始播放回掉
     */
    protected open fun onStatePlaying() {
        mediaManager.mediaPlay.setPreparedPause(false)
        currentState = VideoStatus.PLAYING
        mediaManager.mediaPlay.start()
    }

    /**
     * 暂停
     */
    protected fun onStatePause() {
        currentState = VideoStatus.PAUSE
        mediaManager.mediaPlay.pause()
    }

    /**
     * 开始缓冲
     */
    protected fun onBufferStart() {
        currentState = VideoStatus.BUFFERING_START
    }

    /**
     * 错误
     */
    protected fun onStateError() {
        mediaManager.mediaPlay.setPreparedPause(false)
        currentState = VideoStatus.ERROR
    }

    /**
     * 自动完成
     */
    protected open fun onStateAutoComplete() {
        currentState = VideoStatus.AUTO_COMPLETE
    }

    /**
     * 释放播放器,全屏下不能释放,先退出全屏再释放
     */
    fun release() {
        if (currentData!!.equals(mediaManager.mediaPlay.currentDataSource)) {
            //把之前的设置到完成状态
            mediaManager.musicViewManager.completeAll()
            //释放播放器
            mediaManager.releaseMediaPlayer()
        }
    }
    /********************************************************************************************
     * 播放器的生命周期，可以重写
     */
    /**
     * 准备播放
     */
    override fun onPrepared() {
        onStatePrepared()
        setStatus(VideoStatus.PLAYING)
        if (mSeekOnStart > 0) {
            mediaManager.mediaPlay.seekTo(mSeekOnStart)
            mSeekOnStart = 0
        }
        mHadPlay = true
    }

    /**
     * 播放信息
     *
     * @param what  错误码
     * @param extra 扩展码
     */
    @CallSuper
    override fun onInfo(what: Int, extra: Int) {
        onInfoCall?.invoke(what, extra)
        if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
            mBackUpPlayingBufferState = currentState
            //避免在onPrepared之前就进入了buffering，导致一只loading
            if (mHadPlay && currentState != VideoStatus.PREPARING && currentState != VideoStatus.NORMAL) {
                setStatus(VideoStatus.BUFFERING_START)
            }
        } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
            if (mBackUpPlayingBufferState != null) {
                if (mBackUpPlayingBufferState == VideoStatus.BUFFERING_START) {
                    mBackUpPlayingBufferState = VideoStatus.PLAYING
                }
                if (mHadPlay && currentState != VideoStatus.PREPARING && currentState != VideoStatus.NORMAL) {
                    setStatus(mBackUpPlayingBufferState!!)
                }
                mBackUpPlayingBufferState = null
            }
        }
    }

    /**
     * 设置进度完成
     */
    @CallSuper
    override fun onSeekComplete() {
        onSeekCompleteCall?.invoke()
    }

    /**
     * 播放错误
     *
     * @param what  错误码
     * @param extra 扩展码
     */
    @CallSuper
    override fun onError(what: Int, extra: Int) {
        onErrorCall?.invoke(what, extra)
        var netSate = ""
        //切换网络引起的
        if (what == -10000 && NetworkUtils.isConnected(context) &&
            !TextUtils.equals(mediaManager.netSate, NetworkUtils.getNetWorkTypeName().also { netSate = it })
        ) {
            mediaManager.netSate = netSate
            val position = currentPositionWhenPlaying
            mediaManager.releaseMediaPlayer()
            setSeekOnStart(position)
            //重新播放
            startMusic()
            return
        }
        if (what != 38 && what != -38 && extra != -38) {
            setStatus(VideoStatus.ERROR)
            if (isCurrentPlay) {
                mediaManager.releaseMediaPlayer()
            }
        }
    }

    /**
     * 自动播放完成，播放器回调的
     */
    override fun onAutoCompletion(): Boolean {
        onEvent(EasyMediaEvent.ON_AUTO_COMPLETE)
        mediaManager.releaseMediaPlayer()
        Runtime.getRuntime().gc()
        setStatus(VideoStatus.AUTO_COMPLETE)
        //播放下一个
        return switchData(currentUrlIndex + 1)
    }

    /**
     * 播放器生命周期,自己主动调用的,还原状态
     */
    override fun onForceCompletionTo() {
        //还原默认状态
        setStatus(VideoStatus.FORCE_COMPLETE)
        mediaManager.currentVideoWidth = 0
        mediaManager.currentVideoHeight = 0
        //取消音频焦点
        setAudioFocus(context, false)
    }

    override fun onVideoSizeChanged(width: Int, height: Int) {

    }

    /**
     * 缓存进度更新
     *
     * @param bufferProgress
     */
    @CallSuper
    override fun setBufferProgress(bufferProgress: Int) {
        onBufferProgressCall?.invoke(bufferProgress)
    }

    /**
     * 播放事件的回掉
     *
     * @param type [EasyMediaEvent]
     */
    @CallSuper
    override fun onEvent(type: Int) {
        //本实例的回调
        musicEvents?.forEach {
            it.invoke(type)
        }
        //全局的地方回调
        mediaManager.onEventCall?.invoke(type)
    }
    /********************************************************************************************
     * 下面这些都是获取属性和设置属性
     **/
    /**
     * 当前EasyVideoPlay  是否正在播放
     */
    val isCurrentPlay: Boolean
        get() = (isCurrentMusicPlay && isContainsUri(mediaData, mediaManager.mediaPlay.currentDataSource))

    /**
     * 是否是当前在播放音频
     */
    val isCurrentMusicPlay: Boolean
        get() = (mediaManager.musicViewManager.musicDefault != null && mediaManager.musicViewManager.musicDefault === this)

    /**
     * 获取当前播放uil
     *
     * @return
     */
    val currentData: VideoData?
        get() = mediaData.getOrNull(currentUrlIndex)

    /**
     * 从哪里开始播放
     * 目前有时候前几秒有跳动问题，毫秒
     * 需要在startPlayLogic之前，即播放开始之前
     */
    fun setSeekOnStart(seekOnStart: Long) {
        mSeekOnStart = seekOnStart
    }

    /**
     * 移除播放事件的回掉
     */
    fun removeEvent(action: MediaEventCall?): Boolean {
        return if (action != null) {
            musicEvents.remove(action)
        } else false
    }

    /**
     * 添加播放事件的回掉
     */
    fun addEvent(action: MediaEventCall) {
        if (!musicEvents.contains(action)) {
            musicEvents.add(action)
        }
    }

    /**
     * 获取当前播放位置
     */
    val currentPositionWhenPlaying: Long
        get() {
            var position: Long = 0
            if (currentState == VideoStatus.PLAYING || currentState == VideoStatus.PAUSE) {
                position = runCatching { mediaManager.mediaPlay.currentPosition }.getOrNull() ?: 0
            }
            return position
        }

    fun copyStatus(oldVideo: BaseEasyMusicPlay) {
        //复制一些标志位
        mBackUpPlayingBufferState = oldVideo.mBackUpPlayingBufferState
        mHadPlay = oldVideo.mHadPlay
        setStatus(oldVideo.currentState)
        currentUrlIndex = oldVideo.currentUrlIndex
    }

    /**
     * 转到另外一个View播放
     */
    fun copyPlay(oldMusic: BaseEasyMusicPlay) {
        if (mediaData == null && oldMusic.mediaData != null) {
            setDataSource(oldMusic.mediaData, oldMusic.currentUrlIndex)
        }
        copyStatus(oldMusic)
        //还原默认的view
        oldMusic.setStatus(VideoStatus.NORMAL)
    }

    /**
     * 对应activity得生命周期
     */
    override fun onPause() {
        if (currentState == VideoStatus.AUTO_COMPLETE || currentState == VideoStatus.NORMAL) {
            releaseAll()
        } else {
            ONRESUME_TO_PLAY = currentState == VideoStatus.PLAYING
            if (currentState == VideoStatus.PLAYING) {
                setStatus(VideoStatus.PAUSE)
            } else if (currentState == VideoStatus.PREPARING) {
                mediaManager.mediaPlay.setPreparedPause(true)
            }
        }
    }

    /**
     * 对应activity得生命周期
     */
    fun onResume() {
        if (currentState == VideoStatus.PAUSE && ONRESUME_TO_PLAY) {
            setStatus(VideoStatus.PLAYING)
        }
    }

    fun play() {
        if (currentState != VideoStatus.PLAYING) {
            if (currentState == VideoStatus.PAUSE) {
                setStatus(VideoStatus.PLAYING)
            } else {
                startMusic()
            }
        }
    }

    /**
     * 对应activity得生命周期
     */
    fun onDestroy() {
        MusicUtils.onDestroy()
    }
}