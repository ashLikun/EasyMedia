package com.ashlikun.media.video.view.other

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.ashlikun.media.R

/**
 * 作者　　: 李坤
 * 创建时间: 2017/11/28　10:42
 * 邮箱　　：496546144@qq.com
 *
 *
 * 功能介绍：进度对话框
 */
class EasyVideoProgressDialog(context: Context) : Dialog(context, R.style.easy_video_style_dialog_progress) {
    protected var mDialogProgressBar: ProgressBar? = null
    protected var mDialogSeekTime: TextView? = null
    protected var mDialogTotalTime: TextView? = null
    protected var mDialogIcon: ImageView? = null

    init {
        init()
    }

    private fun init() {
        setContentView(R.layout.easy_video_dialog_progress)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val window = window
        window!!.addFlags(Window.FEATURE_ACTION_BAR)
        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        window.setLayout(-2, -2)
        val localLayoutParams = window.attributes
        localLayoutParams.gravity = Gravity.CENTER
        window.attributes = localLayoutParams
        mDialogProgressBar = findViewById(R.id.duration_progressbar)
        mDialogSeekTime = findViewById(R.id.tv_current)
        mDialogTotalTime = findViewById(R.id.tv_duration)
        mDialogIcon = findViewById(R.id.duration_image_tip)
    }

    fun setTime(seekTime: String?, totalTime: String) {
        mDialogSeekTime!!.text = seekTime
        mDialogTotalTime!!.text = " / $totalTime"
    }

    fun setProgress(seekTimePosition: Long, totalTimeDuration: Long) {
        mDialogProgressBar!!.progress = if (totalTimeDuration <= 0) 0 else (seekTimePosition * 100 / totalTimeDuration).toInt()
    }

    /**
     * 设置箭头方向
     *
     * @param isOrientationToRight 是否向右
     */
    fun setOrientation(isOrientationToRight: Boolean) {
        if (isOrientationToRight) {
            mDialogIcon!!.setImageResource(R.drawable.easy_video_forward_icon)
        } else {
            mDialogIcon!!.setImageResource(R.drawable.easy_video_backward_icon)
        }
    }
}