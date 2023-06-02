package com.ashlikun.media.video.view.other

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.TextView
import com.ashlikun.media.R

/**
 * 作者　　: 李坤
 * 创建时间: 2017/11/28　10:42
 * 邮箱　　：496546144@qq.com
 *
 *
 * 功能介绍：
 */
class EasyVideoBrightDialog(context: Context) : Dialog(context, R.style.easy_video_style_dialog_progress) {
    protected var mDialogBrightnessProgressBar: ProgressBar? = null
    protected var mDialogBrightnessTextView: TextView? = null

    init {
        init()
    }

    private fun init() {
        setContentView(R.layout.easy_video_dialog_brightness)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val window = window!!
        window.addFlags(Window.FEATURE_ACTION_BAR)
        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        window.setLayout(-2, -2)
        val localLayoutParams = window.attributes
        localLayoutParams.gravity = Gravity.CENTER
        window.attributes = localLayoutParams
        mDialogBrightnessTextView = findViewById(R.id.tv_brightness)
        mDialogBrightnessProgressBar = findViewById(R.id.brightness_progressbar)
    }

    fun setBrightPercent(brightnessPercent: Int) {
        var brightnessPercent = brightnessPercent
        if (brightnessPercent > 100) {
            brightnessPercent = 100
        } else if (brightnessPercent < 0) {
            brightnessPercent = 0
        }
        mDialogBrightnessTextView!!.text = "$brightnessPercent%"
        mDialogBrightnessProgressBar!!.progress = brightnessPercent
    }
}