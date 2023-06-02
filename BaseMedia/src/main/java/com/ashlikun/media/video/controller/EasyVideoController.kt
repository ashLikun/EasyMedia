package com.ashlikun.media.video.controller

import android.content.Context
import android.media.AudioManager
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.SeekBar
import com.ashlikun.media.R
import com.ashlikun.media.video.EasyMediaEvent
import com.ashlikun.media.video.EasyMediaManager
import com.ashlikun.media.video.VideoData
import com.ashlikun.media.video.VideoUtils
import com.ashlikun.media.video.VideoUtils.getWindow
import com.ashlikun.media.video.VideoUtils.stringForTime
import com.ashlikun.media.video.status.VideoStatus
import com.ashlikun.media.video.view.EasyOnControllEvent
import com.ashlikun.media.video.view.other.EasyVideoBrightDialog
import com.ashlikun.media.video.view.other.EasyVideoProgressDialog
import com.ashlikun.media.video.view.other.EasyVideoVolumeDialog
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/**
 * 作者　　: 李坤
 * 创建时间: 2017/11/28　9:43
 * 邮箱　　：496546144@qq.com
 *
 *
 * 功能介绍：视频控制器
 */
open class EasyVideoController @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    RelativeLayout(context, attrs, defStyleAttr), View.OnClickListener, VideoControllerBottom.OnEventListener {
    //倒计时消失的Callback
    protected open val showControllerRunnable by lazy {
        Runnable { viewHolder.showControllerViewAnim(currentState, false) }
    }

    open var onControllEvent: EasyOnControllEvent? = null

    //音频管理器，改变声音大小
    open protected val audioManager by lazy {
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    //外部调用设置
    open lateinit var mediaManager: EasyMediaManager


//        get() {
//            fun find(vi: ViewParent): EasyMediaManager? {
//                return if (vi.parent is BaseEasyMediaPlay) (vi.parent as BaseEasyMediaPlay).mediaManager else if (vi.parent != null) find(vi.parent) else null
//            }
//            return find(parent) ?: throw NullPointerException("not EasyMediaManager")
//        }

    /**
     * 控制页面上的View
     * 可以从写实现
     */
    open val viewHolder by lazy {
        EasyControllerViewHolder(this, this, this)
    }

    //是否运行手势改变亮度
    var isCanChangeBrightn = true

    //是否运行手势改变声音
    var isCanChangeVolume = true

    //是否运行手势改变进度
    var isCanChangeProgress = true

    //是否是全屏播放
    open var isFull = false
        set(value) {
            field = value
            viewHolder.isFull = value
        }

    //当前播放状态
    open var currentState = VideoStatus.NORMAL
        set(value) {
            field = value
            //跟新ui
            viewHolder.changUi(value)
        }

    //按下的X坐标
    open protected var downX = 0f

    //按下的Y坐标
    open protected var downY = 0f

    //屏幕宽度
    open protected var screenWidth = 0

    //屏幕高度
    open protected var screenHeight = 0
    open protected var changeVolume = false //是否改变音量
    open protected var changePosition = false //是否改变进度
    open protected var changeBrightness = false //是否改变亮度
    open protected var gestureDownPosition: Long = 0
    open protected var gestureDownVolume = 0
    open protected var gestureDownBrightness = 0f

    //滑动播放进度的位置
    open protected var seekTimePosition = 0L

    //进度对话框
    open protected val progressDialog by lazy {
        EasyVideoProgressDialog(context)
    }

    //音量对话框
    open protected val volumeDialog: EasyVideoVolumeDialog by lazy {
        EasyVideoVolumeDialog(context)
    }

    //亮度
    open protected val brightDialog by lazy {
        EasyVideoBrightDialog(context)
    }

    //未播放时候占位图
    val thumbImageView
        get() = viewHolder.thumbImageView

    init {
        initView()
    }

    /**
     * 设置是否可以全屏
     */
    fun setControllFullEnable(fullEnable: Boolean) {
        viewHolder.setControllFullEnable(fullEnable)
    }

    /**
     * 设置数据源
     */
    fun setDataSource(mediaData: VideoData?) {
        viewHolder.setDataSource(mediaData)
    }

    /**
     * 可以从写
     */
    val layoutId: Int
        get() = R.layout.easy_video_layout_controller


    private fun initView() {
        descendantFocusability = FOCUS_BLOCK_DESCENDANTS
        screenWidth = context.resources.displayMetrics.widthPixels
        screenHeight = context.resources.displayMetrics.heightPixels
        inflate(context, layoutId, this)
        setOnClickListener(this)
        isFull = isFull
    }

    /**
     * 开始触摸进度条
     */
    override fun onStartTrackingTouch(seekBar: SeekBar) {
        cancelDismissControlViewSchedule()
    }

    /**
     * 结束触摸进度条
     */
    override fun onStopTrackingTouch(seekBar: SeekBar) {
        onControllEvent!!.onEvent(EasyMediaEvent.ON_SEEK_POSITION)
        var vpup = parent
        while (vpup != null) {
            vpup.requestDisallowInterceptTouchEvent(false)
            vpup = vpup.parent
        }
        if (currentState !== VideoStatus.PLAYING && currentState !== VideoStatus.PAUSE) return
        val time = seekBar.progress * this.duration / 100
        mediaManager.seekTo(time)
        startDismissControlViewSchedule(false)
    }

    override fun onFullscreenClick() {
        onControllEvent?.onFullscreenClick()
    }

    //底部进度改变
    override fun onProgressChang(progress: Int, secondaryProgress: Int) {
        viewHolder.setProgress(progress, secondaryProgress)
    }

    override fun onClick(v: View) {
        val i = v.id
        if (i == R.id.start) {
            cancelDismissControlViewSchedule()
            onControllEvent!!.onPlayStartClick()
        } else if (i == R.id.retry_btn) {
            onControllEvent!!.onRetryClick()
        } else if (v === this) {
            //保证滑动事件后不调用点击事件
            if (!changePosition && !changeVolume && !changeBrightness) {
                onControllEvent!!.onControllerClick()
            } else {
                changePosition = false
                changeVolume = false
                changeVolume = false
            }
        }
    }

    override fun dispatchSetPressed(pressed: Boolean) {}
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val res = super.onTouchEvent(event)
        // 全屏模式下的CURRENT_STATE_ERROR状态下,不响应进度拖动事件.
        // 否则会因为mediaplayer的状态非法导致App Crash
        if (currentState === VideoStatus.ERROR) {
            return false
        }
        if (!isFull) {
            return res
        }
        val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = x
                downY = y
                changeVolume = false
                changePosition = false
                changeBrightness = false
                viewHolder.stopProgressSchedule()
            }

            MotionEvent.ACTION_MOVE -> {
                val deltaX = x - downX
                var deltaY = y - downY
                val absDeltaX = Math.abs(deltaX)
                val absDeltaY = Math.abs(deltaY)
                if (!changePosition && !changeVolume && !changeBrightness) {
                    if (absDeltaX > THRESHOLD || absDeltaY > THRESHOLD) {
                        viewHolder.stopProgressSchedule()
                        if (absDeltaX >= THRESHOLD) {
                            if (isCanChangeProgress) {
                                changePosition = true
                                gestureDownPosition = currentPositionWhenPlaying
                            }
                        } else {
                            //如果y轴滑动距离超过设置的处理范围，那么进行滑动事件处理
                            if (downX < screenWidth * 0.5f) {
                                //左侧改变亮度
                                if (isCanChangeBrightn) {
                                    changeBrightness = true
                                    val lp = getWindow(context).attributes
                                    if (lp.screenBrightness < 0) {
                                        try {
                                            gestureDownBrightness =
                                                Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS).toFloat()
                                        } catch (e: SettingNotFoundException) {
                                            e.printStackTrace()
                                        }
                                    } else {
                                        gestureDownBrightness = lp.screenBrightness * 255
                                    }
                                }
                            } else {
                                //右侧改变声音
                                if (isCanChangeVolume) {
                                    changeVolume = true
                                    gestureDownVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                                }
                            }
                        }
                    }
                }
                //进度
                if (changePosition) {
                    val totalTimeDuration = this.duration
                    seekTimePosition = (gestureDownPosition + deltaX * totalTimeDuration / screenWidth).toInt().toLong()
                    if (seekTimePosition > totalTimeDuration) {
                        seekTimePosition = totalTimeDuration
                    }
                    val seekTime = stringForTime(seekTimePosition)
                    val totalTime = stringForTime(totalTimeDuration)
                    showProgressDialog(deltaX, seekTime, seekTimePosition, totalTime, totalTimeDuration)
                }
                //音量
                if (changeVolume) {
                    deltaY = -deltaY
                    val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                    val deltaV = (max * deltaY * 3 / screenHeight).toInt()
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, gestureDownVolume + deltaV, 0)
                    //dialog中显示百分比
                    val volumePercent = (gestureDownVolume * 100 / max + deltaY * 3 * 100 / screenHeight).toInt()
                    showVolumeDialog(-deltaY, volumePercent)
                }
                //亮度
                if (changeBrightness) {
                    deltaY = -deltaY
                    val deltaV = (255 * deltaY * 3 / screenHeight).toInt()
                    val params = getWindow(context).attributes
                    if ((gestureDownBrightness + deltaV) / 255 >= 1) { //这和声音有区别，必须自己过滤一下负值
                        params.screenBrightness = 1f
                    } else if ((gestureDownBrightness + deltaV) / 255 <= 0) {
                        params.screenBrightness = 0.01f
                    } else {
                        params.screenBrightness = (gestureDownBrightness + deltaV) / 255
                    }
                    getWindow(context).attributes = params
                    //dialog中显示百分比
                    val brightnessPercent = (gestureDownBrightness * 100 / 255 + deltaY * 3 * 100 / screenHeight).toInt()
                    //显示亮度
                    showBrightnessDialog(brightnessPercent)
                }
            }

            MotionEvent.ACTION_UP -> {
                dismissProgressDialog()
                dismissVolumeDialog()
                dismissBrightnessDialog()
                if (changePosition) {
                    onControllEvent!!.onEvent(EasyMediaEvent.ON_TOUCH_SCREEN_SEEK_POSITION)
                    mediaManager.seekTo(seekTimePosition)
                    setProgress()
                }
                if (changeVolume) {
                    onControllEvent!!.onEvent(EasyMediaEvent.ON_TOUCH_SCREEN_SEEK_VOLUME)
                }
                viewHolder.startProgressSchedule()
            }
        }
        return true
    }

    private fun setProgress() {
        val duration = this.duration
        val progress = (seekTimePosition * 100 / if (duration == 0L) 1 else duration).toInt()
        viewHolder.setProgress(progress, -1)
    }

    /**
     * 显示进度对话框
     */
    fun showProgressDialog(deltaX: Float, seekTime: String?, seekTimePosition: Long, totalTime: String?, totalTimeDuration: Long) {
        progressDialog.show()
        progressDialog.setTime(seekTime, totalTime!!)
        progressDialog.setProgress(seekTimePosition, totalTimeDuration)
        progressDialog.setOrientation(deltaX > 0)
        viewHolder.changeUiToClean()
    }

    /**
     * 显示音量对话框
     */
    fun showVolumeDialog(deltaY: Float, volumePercent: Int) {
        volumeDialog.show()
        volumeDialog.setVolumePercent(volumePercent)
        viewHolder.changeUiToClean()
    }

    /**
     * 显示亮度对话框
     */
    fun showBrightnessDialog(brightnessPercent: Int) {
        brightDialog.show()
        brightDialog.setBrightPercent(brightnessPercent)
        viewHolder.changeUiToClean()
    }

    /**
     * 销毁进度对话框
     */
    fun dismissProgressDialog() {
        progressDialog.dismiss()
    }

    /**
     * 销毁音量对话框
     */
    fun dismissVolumeDialog() {
        volumeDialog.dismiss()
    }

    /**
     * 销毁亮度对话框
     */
    fun dismissBrightnessDialog() {
        brightDialog.dismiss()
    }

    val duration: Long
        get() {
            var duration: Long = 0
            duration = try {
                mediaManager.duration
            } catch (e: IllegalStateException) {
                e.printStackTrace()
                return duration
            }
            return duration
        }

    /**
     * 获取当前播放位置
     */
    val currentPositionWhenPlaying: Long
        get() {
            var position: Long = 0
            if (currentState === VideoStatus.PLAYING || currentState === VideoStatus.PAUSE) {
                position = runCatching { mediaManager.currentPosition }.getOrElse { 0 }
            }
            return position
        }

    /**
     * 点击时候显示控制器（3秒后消失）
     * @param isAuto 是否判断显示
     */
    fun startDismissControlViewSchedule(isAuto: Boolean = true) {
        cancelDismissControlViewSchedule()
        if (viewHolder.containerIsShow() && isAuto) {
            //隐藏
            viewHolder.showControllerViewAnim(currentState, false)
        } else {
            //显示
            viewHolder.showControllerViewAnim(currentState, true)
            postDelayed(showControllerRunnable, 5000)
        }
    }

    /**
     * 取消显示控制器的定时器
     */
    fun cancelDismissControlViewSchedule() {
        removeCallbacks(showControllerRunnable)
    }

    /**
     * 是否只在全屏的时候显示标题和顶部
     */
    fun setOnlyFullShowTitle(onlyFullShowTitle: Boolean) {
        viewHolder.isOnlyFullShowTitle = onlyFullShowTitle
    }

    /**
     * 设置进度最大
     */
    fun setMaxProgressAndTime() {
        viewHolder.setProgress(100, 100)
    }

    /**
     * 获取进度缓存
     */
    var bufferProgress: Int
        get() = viewHolder.bufferProgress
        set(bufferProgress) {
            if (bufferProgress >= 0) {
                viewHolder.setProgress(-1, bufferProgress)
            }
        }

    /**
     * 当自动完成的时候
     */
    fun onAutoCompletion() {
        dismissVolumeDialog()
        dismissProgressDialog()
        dismissBrightnessDialog()
    }


    companion object {
        const val THRESHOLD = 80
    }
}