package com.ashlikun.media.video.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.ashlikun.media.R
import com.ashlikun.media.video.EasyMediaEvent
import com.ashlikun.media.video.EasyVideoViewManager
import com.ashlikun.media.video.VideoData
import com.ashlikun.media.video.VideoScreenUtils
import com.ashlikun.media.video.VideoUtils
import com.ashlikun.media.video.controller.EasyVideoController
import com.ashlikun.media.video.status.VideoStatus

/**
 * 作者　　: 李坤
 * 创建时间: 2017/11/24 17:28
 * 邮箱　　：496546144@qq.com
 *
 *
 * 功能介绍：播放器view，负责视频的播放
 * 基于[BaseEasyMediaPlay] 实现带有控制器的播放器
 * 可以重写 [.createController] 实现不同的控制器
 * 可以重写 [.createMiddleView] 添加中间控件，比如弹幕
 */
open class EasyMediaPlayer @JvmOverloads constructor(context: Context, open val attrs: AttributeSet? = null, open val defStyleAttr: Int = 0) :
    BaseEasyMediaPlay(context, attrs, defStyleAttr),
    EasyOnControllEvent, IEasyMediaPlayListener {
    var seekToInAdvance = 0
    var ratio = 0f
    var videoRotation = 0

    /**
     * 是否可以全屏
     */
    protected var fullscreenEnable = true
        set(value) {
            field = value
            mediaController?.setControllFullEnable(value)
        }

    /**
     * 是否全屏播放
     */
    override var isFull = false
        set(value) {
            field = value
            mediaController?.isFull = value
        }

    /**
     * 播放器控制器
     */
    var mediaController: EasyVideoController? = null
        protected set

    //未播放时候占位图
    val thumbImageView
        get() = mediaController?.thumbImageView

    init {
        initView(context, attrs)
    }

    fun initView(context: Context, attrs: AttributeSet?) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.EasyVideoPlayer)
        //是否可以全屏
        fullscreenEnable = a.getBoolean(R.styleable.EasyVideoPlayer_video_full_screen_enable, fullscreenEnable)
        fullscreenPortrait = a.getInt(R.styleable.EasyVideoPlayer_video_full_screen_portrait, fullscreenPortrait)
        a.recycle()
        createMiddleView()
        initController(createController())
        if (isCurrentPlay) {
            VideoUtils.getActivity(context)?.requestedOrientation?.also {
                ORIENTATION_NORMAL = it
            }
        }
    }

    /**
     * 这里提供给继承者的添加中间控件
     * 在控制器与播放器中间的控件
     * 比如弹幕
     */
    protected fun createMiddleView() {}

    /**
     * 子类可以实现从写
     *
     * @return
     */
    protected open fun createController(): EasyVideoController? {
        return EasyVideoController(context, attrs, defStyleAttr).also {
            it.mediaManager = mediaManager
        }
    }

    /**
     * 是否显示控制器
     */
    fun setControllerVisiable(isShow: Boolean) {
        if (!isShow) {
            if (mediaController != null) {
                removeView(mediaController as View)
            }
        } else {
            if (mediaController == null) {
                initController(createController())
            }
        }
    }

    /**
     * 初始化控制器
     *
     * @param controller
     */
    protected fun initController(controller: EasyVideoController?) {
        if (mediaController != null) {
            removeView(mediaController as View)
        }
        mediaController = controller
        if (mediaController != null) {
            addView(mediaController as View)
            mediaController!!.onControllEvent = this
            mediaController!!.setControllFullEnable(fullscreenEnable)
        }
    }

    override fun setDataSource(mediaData: List<VideoData>, defaultIndex: Int): Boolean {
        val res = super.setDataSource(mediaData, defaultIndex)
        if (mediaController != null && currentData != null) {
            mediaController!!.setDataSource(currentData)
        }
        return res
    }

    override fun switchData(position: Int): Boolean {
        if (super.switchData(position)) {
            if (mediaController != null && currentData != null) {
                mediaController!!.setDataSource(currentData)
            }
            return true
        }
        return false
    }

    override fun setStatus(state: VideoStatus): Boolean {
        val result = super.setStatus(state)
        if (result) {
            mediaController?.currentState = currentState
        }
        return result
    }

    /**
     * 保存播放器 用于全局管理
     * [)][EasyVideoViewManager.setVideoDefault]
     * [)][EasyVideoViewManager.setVideoDefault]
     * [EasyVideoViewManager.setVideoTiny]
     * 可能会多次调用
     */
    override fun saveVideoPlayView() {
        if (isFull) {
            mediaManager.viewManager.setVideoFullscreen(this)
        } else {
            mediaManager.viewManager.videoDefault = this
        }
    }

    /**
     * 当控制器从新播放点击
     */
    override fun onRetryClick() {
        if (VideoUtils.videoAllowPlay(this)) {
            onEvent(EasyMediaEvent.ON_CLICK_START_ICON)
            return
        }
        startVideo()
        onEvent(EasyMediaEvent.ON_CLICK_START_ERROR)
    }

    /**
     * 控制器全屏点击
     */
    override fun onFullscreenClick() {
        if (currentState === VideoStatus.AUTO_COMPLETE || !fullscreenEnable) {
            return
        }
        if (isFull) {
            //退出全屏
            VideoScreenUtils.onBackPressed()
        } else {
            onEvent(EasyMediaEvent.ON_ENTER_FULLSCREEN)
            startWindowFullscreen()
        }
    }

    /**
     * 当控制器点击的时候
     */
    override fun onControllerClick() {
        if (currentState === VideoStatus.ERROR) {
            startVideo()
        } else {
            mediaController?.startDismissControlViewSchedule()
        }
    }

    override fun onStateNormal() {
        super.onStateNormal()
    }

    override fun onStatePreparing() {
        super.onStatePreparing()
    }

    override fun onStatePrepared() { //因为这个紧接着就会进入播放状态，所以不设置state
        super.onStatePrepared()
        if (seekToInAdvance != 0) {
            mediaManager.seekTo(seekToInAdvance.toLong())
            seekToInAdvance = 0
        } else {
            val position = getSavedProgress()
            if (position > 0L) {
                mediaManager.seekTo(position)
            }
        }
    }

    /**
     * 开始播放回掉
     */
    override fun onStatePlaying() {
        super.onStatePlaying()
    }

    /**
     * 暂停
     */
    override fun onStatePause() {
        super.onStatePause()
    }

    /**
     * 错误
     */
    override fun onStateError() {
        super.onStateError()
    }

    /**
     * 开始缓冲
     */
    override fun onBufferStart() {
        super.onBufferStart()
    }

    /**
     * 自动完成
     */
    override fun onStateAutoComplete() {
        super.onStateAutoComplete()
        if (mediaController != null) {
            mediaController!!.setMaxProgressAndTime()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (isFull) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }
        if (ratio != 0f) {
            val specWidth = MeasureSpec.getSize(widthMeasureSpec)
            val specHeight = (specWidth / ratio).toInt()
            val childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(specWidth, MeasureSpec.EXACTLY)
            val childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(specHeight, MeasureSpec.EXACTLY)
            super.onMeasure(childWidthMeasureSpec, childHeightMeasureSpec)
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    /**
     * 准备播放
     */
    override fun onPrepared() {
        super.onPrepared()
    }

    /**
     * 缓存进度更新
     *
     * @param bufferProgress
     */
    override fun setBufferProgress(bufferProgress: Int) {
        super.setBufferProgress(bufferProgress)
        if (mediaController != null) {
            mediaController!!.bufferProgress = bufferProgress
        }
    }

    /**
     * 播放器生命周期
     */
    override fun onAutoCompletion(): Boolean {
        val res = super.onAutoCompletion()
        if (!res) {
            if (mediaController != null) {
                mediaController!!.onAutoCompletion()
            }
            if (isFull) {
                VideoScreenUtils.onBackPressed()
            }
        }
        return res
    }

    /**
     * 播放器生命周期
     */
    override fun onVideoSizeChanged(width: Int, height: Int) {
        super.onVideoSizeChanged(width, height)
        if (videoRotation != 0) {
            mediaManager.textureView?.rotation = videoRotation.toFloat()
        }
    }

    override fun onInfo(what: Int, extra: Int) {
        super.onInfo(what, extra)
    }

    /**
     * 播放器生命周期,自己主动调用的,还原状态
     */
    override fun onForceCompletionTo() {
        super.onForceCompletionTo()
    }

    /**
     * 开始全屏播放
     * 在当前activity的跟布局加一个新的最大化的EasyVideoPlayer
     * 再把activity设置成全屏，
     */
    open fun startWindowFullscreen() {
        //这里对应的不能释放当前视频
        VideoScreenUtils.CLICK_QUIT_FULLSCREEN_TIME = System.currentTimeMillis()
        removeTextureView()
        val fullPlay = newFullscreenView()
        fullPlay.copyStatus(this)
        fullPlay.addTextureView()
        if (mediaController != null && fullPlay.mediaController != null) {
            fullPlay.mediaController!!.bufferProgress = mediaController!!.bufferProgress
        }
        //还原默认的view
        setStatus(VideoStatus.NORMAL)
        //取消定时器
        mediaController?.cancelDismissControlViewSchedule()
        VideoScreenUtils.startFullscreen(fullPlay, false, mediaData, currentUrlIndex)
    }

    /**
     * 创建一个全屏的View
     */
    open fun newFullscreenView() = EasyMediaPlayer(context, attrs, defStyleAttr)

    /**
     * 退出全屏和小窗口后调用这个方法 继续播放
     */
    open fun playOnThisVideo() {
        addTextureView()
        //1.清空全屏和小窗的播放器
        if (mediaManager.viewManager.videoTiny != null) {
            copyStatus(mediaManager.viewManager.videoTiny!!)
            mediaManager.viewManager.videoTiny!!.cleanTiny()
        } else if (mediaManager.viewManager.videoFullscreen != null) {
            copyStatus(mediaManager.viewManager.videoFullscreen!!)
            VideoScreenUtils.clearFloatScreen(context, mediaManager)
        }
    }


    /**
     * 实现播放事件的回掉
     *
     * @param type 事件类型
     */
    override fun onEvent(type: Int) {
        super.onEvent(type)
        if (type == EasyMediaEvent.ON_QUIT_FULLSCREEN || type == EasyMediaEvent.ON_QUIT_TINYSCREEN) {
            //如果默认的Video播放过视频,就直接在这个默认的上面播放
            playOnThisVideo()
        }
    }

    override fun copyStatus(oldVideo: BaseEasyMediaPlay) {
        if (oldVideo is EasyMediaPlayer) {
            fullscreenPortrait = oldVideo.fullscreenPortrait
        }
        super.copyStatus(oldVideo)
    }

    /**
     * 转到另外一个View播放
     */
    override fun copyPlay(oldVideo: BaseEasyMediaPlay) {
        super.copyPlay(oldVideo)

        //复制缓存进度
        if (oldVideo is EasyMediaPlayer) {
            if (oldVideo.mediaController != null && mediaController != null) {
                mediaController!!.bufferProgress = oldVideo.mediaController!!.bufferProgress
            }
            fullscreenPortrait = oldVideo.fullscreenPortrait
        }
        //取消定时器
        if (oldVideo is EasyMediaPlayer && oldVideo.mediaController != null) {
            oldVideo.mediaController!!.cancelDismissControlViewSchedule()
        }
    }

    /**
     * 设置宽高比例
     *
     * @param ratio 比例  width/height
     */
    open fun setVideoRatio(ratio: Float) {
        this.ratio = ratio
    }


    override fun release() {
        super.release()
        if (mediaController != null) {
            mediaController!!.cancelDismissControlViewSchedule()
        }
    }

    companion object {
        /**
         * 是否保存进度
         */
        var SAVE_PROGRESS = true
    }
}