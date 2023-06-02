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
 * 功能介绍：
 */
class EasyVideoVolumeDialog(context: Context) : Dialog(context, R.style.easy_video_style_dialog_progress) {
    protected var mDialogVolumeProgressBar: ProgressBar? = null
    protected var mDialogVolumeTextView: TextView? = null
    protected var mDialogVolumeImageView: ImageView? = null

    init {
        init()
    }

    private fun init() {
        setContentView(R.layout.easy_video_dialog_volume)
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
        mDialogVolumeImageView = findViewById(R.id.volume_image_tip)
        mDialogVolumeTextView = findViewById(R.id.tv_volume)
        mDialogVolumeProgressBar = findViewById(R.id.volume_progressbar)
    }

    fun setVolumePercent(volumePercent: Int) {
        var volumePercent = volumePercent
        if (volumePercent <= 0) {
            mDialogVolumeImageView!!.setBackgroundResource(R.mipmap.easy_video_close_volume)
        } else {
            mDialogVolumeImageView!!.setBackgroundResource(R.mipmap.easy_video_add_volume)
        }
        if (volumePercent > 100) {
            volumePercent = 100
        } else if (volumePercent < 0) {
            volumePercent = 0
        }
        mDialogVolumeTextView!!.text = "$volumePercent%"
        mDialogVolumeProgressBar!!.progress = volumePercent
    }
}