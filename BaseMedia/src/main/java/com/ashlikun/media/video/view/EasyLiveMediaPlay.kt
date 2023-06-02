package com.ashlikun.media.video.view

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import com.ashlikun.media.R
import com.ashlikun.media.video.EasyMediaEvent
import com.ashlikun.media.video.EasyVideoViewManager
import com.ashlikun.media.video.VideoScreenUtils
import com.ashlikun.media.video.VideoUtils
import com.ashlikun.media.video.controller.EasyVideoController
import com.ashlikun.media.video.status.VideoStatus

/**
 * 作者　　: 李坤
 * 创建时间: 2018/8/21　15:07
 * 邮箱　　：496546144@qq.com
 *
 *
 * 功能介绍：直播使用
 *
 */
open class EasyLiveMediaPlay @JvmOverloads constructor(context: Context, override val attrs: AttributeSet? = null, override val defStyleAttr: Int = 0) :
    EasyMediaPlayer(context, attrs, defStyleAttr) {
    //倒计时消失的Callback
    protected open val showControllerRunnable by lazy {
        Runnable {
            fullImageView.visibility = GONE
            backImageView.visibility = GONE
        }
    }
    open val fullImageView by lazy {
        ImageView(context).also {
            it.setImageResource(R.drawable.easy_video_enlarge)
            val dp10 = VideoUtils.dip2px(context, 10f)
            setPadding(dp10, dp10, dp10, dp10)
            addView(it, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).also { p ->
                p.gravity = Gravity.RIGHT or Gravity.BOTTOM
                p.rightMargin = dp10
                p.bottomMargin = dp10
            })
        }
    }
    open val backImageView by lazy {
        ImageView(context).also {
            it.setImageResource(R.drawable.easy_video_back_normal)
            val dp10 = VideoUtils.dip2px(context, 10f)
            setPadding(dp10, dp10, dp10, dp10)
            addView(it, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).also { p ->
                p.gravity = Gravity.LEFT or Gravity.TOP
                p.leftMargin = dp10
                p.topMargin = dp10
            })
        }
    }

    /**
     * 是否全屏播放
     */
    override var isFull = false
        set(value) {
            field = value
            if (value) {
                backImageView.visibility = VISIBLE
                fullImageView.visibility = GONE
            } else {
                backImageView.visibility = GONE
                fullImageView.setImageResource(R.drawable.easy_video_enlarge)
            }
        }

    init {
        fullImageView.setOnClickListener {
            onFullscreenClick()
        }
        backImageView.visibility = GONE
        backImageView.setOnClickListener {
            onFullscreenClick()
        }
        setOnClickListener {
            onControllerClick()
            if (!isFull) {
                fullImageView.visibility = VISIBLE
            } else {
                backImageView.visibility = VISIBLE
            }
            removeCallbacks(showControllerRunnable)
            postDelayed(showControllerRunnable, 5000)
        }
    }

    override fun createController() = null

    override fun setStatus(state: VideoStatus): Boolean {
        if (state == VideoStatus.NORMAL || state == VideoStatus.ERROR || state == VideoStatus.AUTO_COMPLETE || state == VideoStatus.FORCE_COMPLETE) {
            fullImageView.visibility = GONE
            backImageView.visibility = GONE
            removeCallbacks(showControllerRunnable)
        } else {
            if (!isFull) {
                fullImageView.visibility = VISIBLE
            } else {
                backImageView.visibility = VISIBLE
            }
            removeCallbacks(showControllerRunnable)
            postDelayed(showControllerRunnable, 5000)
        }
        return super.setStatus(state)
    }

    /**
     * 控制器全屏点击
     */
    override fun onFullscreenClick() {
        if (currentState == VideoStatus.NORMAL || currentState == VideoStatus.ERROR || currentState == VideoStatus.AUTO_COMPLETE || currentState == VideoStatus.FORCE_COMPLETE) {
            return
        }
        super.onFullscreenClick()
    }

    override fun newFullscreenView() = EasyLiveMediaPlay(context, attrs, defStyleAttr)
}