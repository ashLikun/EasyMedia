package com.ashlikun.media.video.controller

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import com.ashlikun.media.R
import com.ashlikun.media.video.EasyMediaEvent
import com.ashlikun.media.video.EasyMediaManager
import com.ashlikun.media.video.VideoUtils
import com.ashlikun.media.video.VideoUtils.mainHander
import com.ashlikun.media.video.VideoUtils.stringForTime
import com.ashlikun.media.video.status.VideoStatus
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/**
 * 作者　　: 李坤
 * 创建时间: 2017/12/7　15:25
 * 邮箱　　：496546144@qq.com
 *
 *
 * 功能介绍：控制器底部
 */
class VideoControllerBottom @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    LinearLayout(context, attrs, defStyleAttr), OnSeekBarChangeListener {
    //外部调用设置
    lateinit var videoController: EasyVideoController

    //外部调用设置
    val mediaManager: EasyMediaManager
        get() = videoController.mediaManager

    protected val progressRunnable by lazy {
        ProgressRunnable()
    }

    //下面可触摸进度条
    var progressBar: SeekBar? = null

    //全屏按钮
    var fullscreenButton: ImageView? = null

    //进度文本
    var currentTimeTextView: TextView? = null
    var totalTimeTextView: TextView? = null
    var onEventListener: OnEventListener? = null
    var fullEnable = true
        set(value) {
            field = value
            fullscreenButton?.visibility = if (value) VISIBLE else GONE
        }

    //是否是全屏状态
    var isFull = false
        set(value) {
            if (!fullEnable) return
            field = value
            if (value) {
                fullscreenButton!!.setImageResource(R.drawable.easy_video_shrink)
            } else {
                fullscreenButton!!.setImageResource(R.drawable.easy_video_enlarge)
            }
        }

    init {
        initView()
    }

    private fun initView() {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        LayoutInflater.from(context).inflate(R.layout.easy_video_layout_controller_bottom, this)
        setBackgroundResource(R.drawable.easy_video_bottom_bg)
        progressBar = findViewById(R.id.bottom_seek_progress)
        fullscreenButton = findViewById(R.id.fullscreen)
        currentTimeTextView = findViewById(R.id.current)
        totalTimeTextView = findViewById(R.id.total)
        progressBar?.setOnSeekBarChangeListener(this)
        fullscreenButton?.setOnClickListener {
            onEventListener?.onFullscreenClick()
        }
    }

    /**
     * 设置进度  如果2个值都是100，就会设置最大值，如果某个值<0 就不设置
     *
     * @param progress          主进度
     * @param secondaryProgress 缓存进度
     */
    fun setProgress(progress: Int, secondaryProgress: Int) {
        if (progress >= 0) {
            progressBar!!.progress = progress
        }
        if (secondaryProgress >= 0) {
            progressBar!!.secondaryProgress = secondaryProgress
        }
        if (progress >= progressBar!!.max && secondaryProgress >= progressBar!!.max) {
            currentTimeTextView!!.text = totalTimeTextView!!.text
        }
    }

    val bufferProgress: Int
        get() = progressBar!!.secondaryProgress

    /**
     * 设置时间
     *
     * @param position 0：重置
     * @param duration 0：重置
     */
    fun setTime(position: Long, duration: Long) {
        currentTimeTextView!!.text = stringForTime(position)
        totalTimeTextView!!.text = stringForTime(duration)
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}
    override fun onStartTrackingTouch(seekBar: SeekBar) {
        var vpdown = parent
        while (vpdown != null) {
            vpdown.requestDisallowInterceptTouchEvent(true)
            vpdown = vpdown.parent
        }
        stopProgressSchedule()
        if (onEventListener != null) {
            onEventListener!!.onStartTrackingTouch(seekBar)
        }
    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {
        mediaManager.viewManager.currentVideoPlayerNoTiny?.onEvent(EasyMediaEvent.ON_SEEK_POSITION)
        var vpup = parent
        while (vpup != null) {
            vpup.requestDisallowInterceptTouchEvent(false)
            vpup = vpup.parent
        }
        if (mediaManager.viewManager.currentVideoPlayerNoTiny?.currentState !== VideoStatus.PLAYING
            && mediaManager.viewManager.currentVideoPlayerNoTiny?.currentState !== VideoStatus.PAUSE
        ) {
            return
        }
        val time = (seekBar.progress * this.duration / 100.0).toInt()
        mediaManager.seekTo(time.toLong())
        startProgressSchedule()
        if (onEventListener != null) {
            onEventListener!!.onStopTrackingTouch(seekBar)
        }
    }

    private val duration: Long
        get() = runCatching { mediaManager.duration }.getOrElse { 0 }

    /**
     * 开始进度定时器
     */
    fun startProgressSchedule() {
        stopProgressSchedule()
        postDelayed(progressRunnable, 300)
    }

    /**
     * 取消进度定时器
     */
    fun stopProgressSchedule() {
        removeCallbacks(progressRunnable)
    }

    inner class ProgressRunnable : Runnable {
        override fun run() {
            if (mediaManager.viewManager.currentVideoPlayerNoTiny?.currentState === VideoStatus.PLAYING || mediaManager.viewManager.currentVideoPlayerNoTiny?.currentState === VideoStatus.PAUSE) {
                var position: Long = 0
                var bufferedPercentage = 0
                runCatching {
                    position = mediaManager.currentPosition
                    bufferedPercentage = mediaManager.bufferedPercentage
                }
                val duration = duration
                val progress = (position * 100f / if (duration == 0L) 1 else duration).toInt()
                if (progress >= 0) {
                    if (onEventListener != null) {
                        onEventListener!!.onProgressChang(progress, bufferedPercentage)
                    } else {
                        setProgress(progress, -1)
                    }
                }
                setTime(position, duration)
            }
            postDelayed(this, 300)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopProgressSchedule()
    }

    interface OnEventListener {
        fun onStartTrackingTouch(seekBar: SeekBar)
        fun onStopTrackingTouch(seekBar: SeekBar)
        fun onFullscreenClick()
        fun onProgressChang(progress: Int, secondaryProgress: Int)
    }
}