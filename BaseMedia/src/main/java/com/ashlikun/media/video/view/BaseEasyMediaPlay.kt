package com.ashlikun.media.video.view

import android.content.Context
import android.content.pm.ActivityInfo
import android.media.MediaPlayer
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.annotation.CallSuper
import com.ashlikun.media.R
import com.ashlikun.media.video.EasyMediaEvent
import com.ashlikun.media.video.EasyMediaManager
import com.ashlikun.media.video.EasyVideoViewManager
import com.ashlikun.media.video.NetworkUtils
import com.ashlikun.media.video.OnPlayerCreate
import com.ashlikun.media.video.VideoData
import com.ashlikun.media.video.VideoScreenUtils.backPress
import com.ashlikun.media.video.VideoScreenUtils.clearFullscreenLayout
import com.ashlikun.media.video.VideoUtils
import com.ashlikun.media.video.listener.MediaBufferProgressCall
import com.ashlikun.media.video.listener.MediaErrorCall
import com.ashlikun.media.video.listener.MediaEventCall
import com.ashlikun.media.video.listener.MediaInfoCall
import com.ashlikun.media.video.listener.MediaSeekCompleteCall
import com.ashlikun.media.video.listener.MediaSizeChangeCall
import com.ashlikun.media.video.status.VideoDisplayType
import com.ashlikun.media.video.status.VideoStatus

/**
 * 作者　　: 李坤
 * 创建时间: 2018/8/20　13:13
 * 邮箱　　：496546144@qq.com
 *
 *
 * 功能介绍：播放器基础类
 * [.setDataSource] 去设置播放的数据源
 */
abstract class BaseEasyMediaPlay @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    FrameLayout(context, attrs, defStyleAttr), IEasyMediaPlayListener {
    /**
     * 使用管理器的标识，需要在初始化时候定义
     */
    open var mediaManageTag = EasyMediaManager.TAG_VIDEO
        set(value) {
            field = value
            mediaManager = EasyMediaManager.getInstance(value)
        }
    var mediaManager = EasyMediaManager.getInstance(mediaManageTag)
        private set

    /**
     * 当内部的播放器创建的时候
     */
    open var onPlayerCreate: OnPlayerCreate? = null

    /**
     * 当前状态
     */
    open var currentState = VideoStatus.NORMAL

    /**
     * 备份缓存前的播放状态
     */
    open protected var backUpPlayingBufferState: VideoStatus? = null

    /**
     * 是否播放过
     */
    open protected var mHadPlay = false

    /***
     * 是否保存进度
     */
    open var isSaveProgress = true

    /**
     * 数据源，列表
     */
    open var mediaData: List<VideoData> = emptyList()
        protected set

    /**
     * 当前播放到的列表数据源位置
     */
    open var currentUrlIndex = 0
        protected set

    /**
     * 播放视频的渲染控件，一般为TextureView
     */

    open val textureViewContainer by lazy {
        FrameLayout(getContext())
    }
    open var onSizeChange: MediaSizeChangeCall? = null
    open var onBufferProgressCall: MediaBufferProgressCall? = null
    open var onSeekCompleteCall: MediaSeekCompleteCall? = null
    open var onInfoCall: MediaInfoCall? = null
    open var onErrorCall: MediaErrorCall? = null

    /**
     * 视频大小缩放类型
     */
    open var displayType = VideoDisplayType.ADAPTER
        set(value) {
            field = value
            if (mediaManager.textureView != null && textureViewContainer.getChildAt(0) === mediaManager.textureView) {
                mediaManager.textureView?.displayType = displayType
            }
        }

    /**
     * 播放事件的回掉
     */
    private val videoEvents by lazy {
        mutableListOf<MediaEventCall>()
    }

    /**
     * 当onResume的时候是否去播放
     */
    private var ONRESUME_TO_PLAY = true

    /**
     * 从哪个开始播放
     */
    open protected var mSeekOnStart: Long = -1

    /**
     * 是否全屏播放
     */
    open var isFull = false

    /**
     * 全屏后是否可以竖屏，默认动态计算，当视频是竖屏的时候  可以竖屏
     * 0:自动判断(宽高比是否可以竖屏)
     * 1:可以竖屏(2个横屏，一个竖屏)
     * 2:不可以竖屏(2个横屏)
     * 3:只能单一横屏
     */
    var fullscreenPortrait = 0

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.BaseEasyVideoPlay)
        if (!a.hasValue(0)) {
            setBackgroundColor(-0x1000000)
        }
        if (a.hasValue(R.styleable.BaseEasyVideoPlay_video_display_type)) {
            displayType = VideoDisplayType.get(a.getInt(R.styleable.BaseEasyVideoPlay_video_display_type, displayType.ordinal))
        }
        if (a.hasValue(R.styleable.BaseEasyVideoPlay_video_manage_tag)) {
            mediaManageTag = a.getString(R.styleable.BaseEasyVideoPlay_video_manage_tag) ?: mediaManageTag
        }
        if (a.hasValue(R.styleable.BaseEasyVideoPlay_video_is_save_progress)) {
            isSaveProgress = a.getBoolean(R.styleable.BaseEasyVideoPlay_video_is_save_progress, isSaveProgress)
        }
        addView(textureViewContainer, LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
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
        if (mediaData.getOrNull(defaultIndex) != null && VideoUtils.isContainsUri(this.mediaData, mediaManager.currentDataSource)) {
            saveVideoPlayView()
            if (currentState == VideoStatus.NORMAL) {
                setStatus(VideoStatus.NORMAL)
            }
            return false
        }
        if (isCurrentVideoPlay && VideoUtils.isContainsUri(mediaData, mediaManager.currentDataSource)) {
            //当前View正在播放视频  保存进度
            if (isSaveProgress) {
                var position = runCatching { mediaManager.currentPosition }.getOrNull() ?: 0L
                if (position != 0L) {
                    mediaManager.currentDataSource?.let { VideoUtils.saveProgress(context, it, position) }
                }
            }
            mediaManager.releaseMediaPlayer()
        } else if (!isCurrentVideoPlay && VideoUtils.isContainsUri(mediaData, mediaManager.currentDataSource)) {
            //需要退出小窗退到我这里，我这里是第一层级
            mediaManager.viewManager.videoTiny?.cleanTiny()
        }
        currentUrlIndex = defaultIndex
        setStatus(VideoStatus.NORMAL)
        return true
    }

    /**
     * 保存播放器 用于全局管理
     * [)][EasyVideoViewManager.setVideoDefault]
     * [)][EasyVideoViewManager.setVideoDefault]
     * [EasyVideoViewManager.setVideoTiny]
     * 可能会多次调用
     */
    abstract fun saveVideoPlayView()

    /**
     * 当控制器播放按钮点击后
     */
    open fun onPlayStartClick() {
        if (mediaData == null || currentData == null) {
            Toast.makeText(context, resources.getString(R.string.easy_video_no_url), Toast.LENGTH_SHORT).show()
            return
        }
        if (currentState === VideoStatus.NORMAL) {
            onEvent(EasyMediaEvent.ON_CLICK_START_ICON)
            startVideo()
        } else if (currentState === VideoStatus.PLAYING) {
            onEvent(EasyMediaEvent.ON_CLICK_PAUSE)
            setStatus(VideoStatus.PAUSE)
        } else if (currentState === VideoStatus.PAUSE) {
            onEvent(EasyMediaEvent.ON_CLICK_RESUME)
            setStatus(VideoStatus.PLAYING)
        } else if (currentState === VideoStatus.AUTO_COMPLETE) {
            onEvent(EasyMediaEvent.ON_CLICK_START_AUTO_COMPLETE)
            startVideo()
        }
    }

    /**
     * 开始播放 必须在设置完数据源后
     */
    open fun startVideo() {
        if (VideoUtils.videoAllowPlay(this) && currentState == VideoStatus.NORMAL) return
        //销毁其他播放的视频
        VideoUtils.releaseAll(mediaManageTag)
        mediaManager.removeTextureView()
        //添加到当前
        addTextureView()

//        VideoUtils.setAudioFocus(getContext(), true);
//        VideoUtils.getActivity(getContext()).getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mediaManager.currentDataSource = currentData
        saveVideoPlayView()
        setStatus(VideoStatus.PREPARING)
    }

    /**
     * 切换数据源
     */
    open fun switchData(position: Int): Boolean {
        val old = currentUrlIndex
        currentUrlIndex = position
        return if (currentData != null) {
            startVideo()
            true
        } else {
            currentUrlIndex = old
            false
        }
    }

    /**
     * 设置当前播放器状态
     */
    open fun setStatus(state: VideoStatus): Boolean {
        if (currentState == state) {
            return false
        }
        VideoUtils.d(state.toString())
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
    protected open fun onStateNormal() {
        currentState = VideoStatus.NORMAL
    }

    /**
     * 当准备时候
     */
    protected open fun onStatePreparing() {
        currentState = VideoStatus.PREPARING
    }

    protected open fun onStatePrepared() {
        //因为这个紧接着就会进入播放状态，所以不设置state
    }

    /**
     * 开始播放回掉
     */
    protected open fun onStatePlaying() {
        mediaManager.mediaPlay.setPreparedPause(false)
        currentState = VideoStatus.PLAYING
        mediaManager.start()
    }

    /**
     * 暂停
     */
    protected open fun onStatePause() {
        currentState = VideoStatus.PAUSE
        mediaManager.pause()
    }

    /**
     * 开始缓冲
     */
    protected open fun onBufferStart() {
        currentState = VideoStatus.BUFFERING_START
    }

    /**
     * 错误
     */
    protected open fun onStateError() {
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
     * 添加TextureView
     */
    fun addTextureView() {
        val layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER)
        if (mediaManager.textureView == null || mediaManager.textureView?.context !== context) {
            //根据新的newVideoPlay 创建新的EasyTextureView View 防止内存泄漏
            mediaManager.initTextureView(context, displayType, mediaManager.textureView)
        }
        if (mediaManager.textureView?.parent != null) {
            (mediaManager.textureView?.parent as ViewGroup).removeView(mediaManager.textureView)
        }
        textureViewContainer.addView(mediaManager.textureView, layoutParams)
    }

    /**
     * 移除当前的渲染器
     */
    open fun removeTextureView() {
        textureViewContainer.removeView(mediaManager.textureView)
    }

    /**
     * 释放播放器,全屏下不能释放,先退出全屏再释放
     */
    open fun release() {
        if (currentData != null && currentData == mediaManager.currentDataSource) {
            //在非全屏的情况下只能backPress()
            if (isFull) {
                backPress(mediaManager)
            } else {
                VideoUtils.releaseAll(mediaManageTag)
            }
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
            mediaManager.seekTo(mSeekOnStart)
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
    override fun onInfo(what: Int, extra: Int) {
        onInfoCall?.invoke(what, extra)
        if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
            backUpPlayingBufferState = currentState
            //避免在onPrepared之前就进入了buffering，导致一只loading
            if (mHadPlay && currentState != VideoStatus.PREPARING && currentState != VideoStatus.NORMAL) {
                setStatus(VideoStatus.BUFFERING_START)
            }
        } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
            if (backUpPlayingBufferState != null) {
                if (backUpPlayingBufferState == VideoStatus.BUFFERING_START) {
                    backUpPlayingBufferState = VideoStatus.PLAYING
                }
                if (mHadPlay && currentState != VideoStatus.PREPARING && currentState != VideoStatus.NORMAL) {
                    setStatus(backUpPlayingBufferState!!)
                }
                backUpPlayingBufferState = null
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
            mediaManager.currentPosition
            mediaManager.releaseMediaPlayer()
            setSeekOnStart(position)
            //重新播放
            startVideo()
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
        if (isSaveProgress) {
            currentData?.let { VideoUtils.saveProgress(context, it, 0) }
        }
        setStatus(VideoStatus.AUTO_COMPLETE)
        //播放下一个
        return switchData(currentUrlIndex + 1)
    }

    /**
     * 播放器生命周期,自己主动调用的,还原状态
     */
    override fun onForceCompletionTo() {
        if (currentState == VideoStatus.PLAYING || currentState == VideoStatus.PAUSE) {
            if (isSaveProgress) {
                var position: Long = 0
                try {
                    position = mediaManager.currentPosition
                } catch (e: IllegalStateException) {
                    e.printStackTrace()
                }
                currentData?.let { VideoUtils.saveProgress(context, it, position) }
            }
        }
        //还原默认状态
        setStatus(VideoStatus.FORCE_COMPLETE)
        removeTextureView()
        mediaManager.currentVideoWidth = 0
        mediaManager.currentVideoHeight = 0
        //取消音频焦点
        VideoUtils.setAudioFocus(context, false)
        //取消休眠
        VideoUtils.getActivity(context)?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        //如果是全屏播放就清楚全屏的view
        if (isFull) {
            clearFullscreenLayout(context)
            VideoUtils.setRequestedOrientation(context, ORIENTATION_NORMAL)
        }
        //释放渲染器和保存的SurfaceTexture，textureView
        mediaManager.releaseAllSufaceView()
    }


    /**
     * 播放器大小改变
     */
    @CallSuper
    override fun onVideoSizeChanged(width: Int, height: Int) {
        mediaManager.textureView?.setVideoSize(width, height)
        onSizeChange?.invoke(width, height)
    }

    /**
     * 缓存进度更新
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
    override fun onEvent(type: Int) {
        VideoUtils.d(type.toString())
        //本实例的回调
        videoEvents.forEach { it.invoke(type) }
        //全局的地方回调
        mediaManager.onEventCall?.invoke(type)
    }
    /********************************************************************************************
     * 下面这些都是获取属性和设置属性
     */
    /**
     * 当前EasyVideoPlay  是否正在播放
     * 不仅正在播放的url不能一样，并且各个清晰度也不能一样
     */
    val isCurrentPlay: Boolean
        get() = (isCurrentVideoPlay && VideoUtils.isContainsUri(mediaData, mediaManager.currentDataSource))

    /**
     * 是否是当前EasyVideoPlay在播放视频
     */
    val isCurrentVideoPlay: Boolean
        get() = (mediaManager.viewManager.currentVideoPlayerNoTiny != null && mediaManager.viewManager.currentVideoPlayerNoTiny === this)

    /**
     * 获取当前播放uil
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
            videoEvents.remove(action)
        } else false
    }

    /**
     * 添加播放事件的回掉
     *
     * @param action
     */
    fun addEvent(action: MediaEventCall) {
        if (!videoEvents.contains(action)) {
            videoEvents.add(action)
        }
    }

    /**
     * 获取当前播放位置
     */
    val currentPositionWhenPlaying: Long
        get() {
            if (currentState == VideoStatus.PLAYING || currentState == VideoStatus.PAUSE) {
                return runCatching { mediaManager.currentPosition }.getOrNull() ?: 0
            }
            return 0
        }

    open fun copyStatus(oldVideo: BaseEasyMediaPlay) {
        //复制一些标志位
        backUpPlayingBufferState = oldVideo.backUpPlayingBufferState
        mHadPlay = oldVideo.mHadPlay
        setStatus(oldVideo.currentState)
        currentUrlIndex = oldVideo.currentUrlIndex
    }

    /**
     * 转到另外一个View播放
     */
    open fun copyPlay(oldVideo: BaseEasyMediaPlay) {
        if (mediaData == null && oldVideo.mediaData != null) {
            setDataSource(oldVideo.mediaData, oldVideo.currentUrlIndex)
        }
        copyStatus(oldVideo)
        //还原默认的view
        oldVideo.setStatus(VideoStatus.NORMAL)
    }

    /**
     * 对应activity得生命周期
     */
    override fun onPause() {
        if (currentState == VideoStatus.AUTO_COMPLETE || currentState == VideoStatus.NORMAL) {
            VideoUtils.releaseAll(mediaManageTag)
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

    /**
     * 对应activity得生命周期
     */
    fun onDestroy() {
        VideoUtils.onDestroy()
    }

    fun getSavedProgress() = if (isSaveProgress) VideoUtils.getSavedProgress(context, currentData!!) else 0

    companion object {
        /**
         * Activity 全屏Flag，重力感应(2个横屏，一个竖屏)
         */
        const val ORIENTATION_FULLSCREEN_SENSOR = ActivityInfo.SCREEN_ORIENTATION_SENSOR

        /**
         * Activity 全屏Flag，重力感应(2个横屏)
         */
        const val ORIENTATION_FULLSCREEN_SENSOR_LANDSCAPE = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

        /**
         * 默认的activty的方向 Flag(竖屏)
         */
        var ORIENTATION_NORMAL = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        /**
         * Activity 竖屏Flag(1个横屏)
         */
        const val ORIENTATION_FULLSCREEN_LANDSCAPE = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }
}