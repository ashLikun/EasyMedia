package com.ashlikun.media.video.controller

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.ashlikun.media.R
import com.ashlikun.media.video.VideoData
import com.ashlikun.media.video.VideoUtils.mainHander
import com.ashlikun.media.video.status.VideoStatus
import com.ashlikun.media.video.view.other.EasyLoaddingView

/**
 * 作者　　: 李坤
 * 创建时间: 2017/12/5　16:46
 * 邮箱　　：496546144@qq.com
 *
 *
 * 功能介绍：控制器的holder
 */
class EasyControllerViewHolder(val videoController: EasyVideoController, val clickListener: View.OnClickListener?, val onEventListener: VideoControllerBottom.OnEventListener?) {
    var iconPlay = R.drawable.easy_video_click_play_selector
    var iconReplay = R.drawable.easy_video_click_replay_selector
    var iconPause = R.drawable.easy_video_click_pause_selector

    //开始按钮
    var startButton: ImageView

    //顶部控制器和底部控制器
    var topContainer: VideoControllerTop
    var bottomContainer: VideoControllerBottom

    //重新加载view
    var mRetryLayout: LinearLayout

    //进度条
    var easyLoaddingView: View

    //是否是全屏播放
    var isFull = false
        set(value) {
            field = value
            bottomContainer.isFull = value
            topContainer.setFull(value)
            if (value) {
                changeStartButtonSize(videoController.resources.getDimension(R.dimen.easy_video_start_button_w_h_fullscreen).toInt())
            } else {
                changeStartButtonSize(videoController.resources.getDimension(R.dimen.easy_video_start_size).toInt())
            }
        }

    //是否只在全屏的时候显示标题和顶部
    var isOnlyFullShowTitle = false
        set(value) {
            field = value
            if (value && !isFull) {
                topContainer.visibility = View.GONE
            }
        }

    var bottomProgressBar: ProgressBar

    //未播放时候占位图
    var thumbImageView: ImageView

    //从新播放
    var replayTextView: TextView

    //当前播放状态
    var currentState = VideoStatus.NORMAL
    var animatorSet = AnimatorSet()
    var isCurrentAnimHint = true

    //之前是否是准备状态
    private var isBeforeStatePreparing = false

    init {
        initAnimator()
        startButton = videoController.findViewById(R.id.start)
        bottomContainer = videoController.findViewById(R.id.controllerBottom)
        topContainer = videoController.findViewById(R.id.controllerTop)
        mRetryLayout = videoController.findViewById(R.id.retry_layout)
        easyLoaddingView = videoController.findViewById(R.id.loading)
        thumbImageView = videoController.findViewById(R.id.thumb)
        replayTextView = videoController.findViewById(R.id.replay_text)
        bottomProgressBar = videoController.findViewById(R.id.bottom_progress)
        thumbImageView.setOnClickListener(clickListener)
        startButton.setOnClickListener(clickListener)
        bottomContainer.onEventListener = onEventListener
        bottomContainer.videoController = videoController
        videoController.findViewById<View>(R.id.retry_btn).setOnClickListener(clickListener)
        changeUiToNormal()
        updateStartImage(currentState)
        bottomContainer.stopProgressSchedule()
    }

    fun setControllFullEnable(fullEnable: Boolean) {
        bottomContainer.fullEnable = fullEnable
    }

    /**
     * 根据状态改变ui
     */
    fun changUi(currentState: VideoStatus) {
        if (this.currentState == currentState) {
            return
        }
        this.currentState = currentState
        if (currentState == VideoStatus.PREPARING) {
            isBeforeStatePreparing = true
        }
        //默认
        when (currentState) {
            VideoStatus.NORMAL -> {
                changeUiToNormal()
                bottomContainer.stopProgressSchedule()
            }

            VideoStatus.PREPARING -> {
                changeUiToPreparing()
            }

            VideoStatus.PLAYING -> {
                changeUiToPlaying()
                startProgressSchedule()
                //开始倒计时消失控制器
                videoController.startDismissControlViewSchedule(false)
            }

            VideoStatus.PAUSE -> {
                changeUiToPause()
                stopProgressSchedule()
            }

            VideoStatus.AUTO_COMPLETE -> {
                changeUiToComplete()
                bottomContainer.stopProgressSchedule()
            }

            VideoStatus.BUFFERING_START -> {
                changeUiToBufferStart()
            }

            VideoStatus.ERROR -> {
                changeUiToError()
                bottomContainer.stopProgressSchedule()
            }
        }
        updateStartImage(currentState)
    }

    fun setDataSource(mediaDatan: VideoData?) {
        topContainer.setInitData(mediaDatan)
    }

    /**
     * 改变播放按钮的大小
     */
    fun changeStartButtonSize(size: Int) {
        startButton.minimumWidth = size
        startButton.minimumHeight = size
    }

    /**
     * 准备中
     */
    fun changeUiToPreparing() {
        bottomContainer.setTime(0, 0)
        hintContainer(false)
        setMinControlsVisiblity(false, true, true, false, false)
        if (easyLoaddingView is EasyLoaddingView) {
            val view = easyLoaddingView as EasyLoaddingView
            if (view.currentState == EasyLoaddingView.STATE_PRE) {
                view.start()
            }
        }
    }

    /**
     * 开始缓冲
     */
    fun changeUiToBufferStart() {
        setMinControlsVisiblity(false, true, false, true, false)
        if (easyLoaddingView is EasyLoaddingView) {
            val view = easyLoaddingView as EasyLoaddingView
            if (view.currentState == EasyLoaddingView.STATE_PRE) {
                view.start()
            }
        }
    }

    /**
     * 改变ui到默认状态
     */
    protected fun changeUiToNormal() {
        //这边加动画，在低版本手机会卡顿，主要是列表里每次都会走这个方法
        hintContainer(false)
        setMinControlsVisiblity(true, false, true, false, false)
        if (easyLoaddingView is EasyLoaddingView) {
            (easyLoaddingView as EasyLoaddingView).reset()
        }
    }

    /**
     * 改变ui成完成
     */
    protected fun changeUiToComplete() {
        if (isOnlyFullShowTitle && !isFull) {
            topContainer.visibility = View.GONE
        } else {
            topContainer.visibility = View.VISIBLE
        }
        bottomContainer.visibility = View.GONE
        setMinControlsVisiblity(true, false, false, false, false)
        if (easyLoaddingView is EasyLoaddingView) {
            (easyLoaddingView as EasyLoaddingView).reset()
        }
    }

    /**
     * 改变ui错误
     */
    protected fun changeUiToError() {
        if (isFull) {
            topContainer.visibility = View.VISIBLE
            bottomContainer.visibility = View.GONE
        } else {
            hintContainer(false)
        }
        setMinControlsVisiblity(true, false, false, false, true)
    }

    /**
     * 播放
     */
    protected fun changeUiToPlaying() {
//        hintContainer(!isBeforeStatePreparing && containerIsShow())
        setMinControlsVisiblity(false, false, false, true, false)
        if (easyLoaddingView is EasyLoaddingView) {
            (easyLoaddingView as EasyLoaddingView).reset()
        }
        isBeforeStatePreparing = false
    }

    /**
     * 暂停状态
     */
    protected fun changeUiToPause() {
        showContainer(!containerIsShow())
        setMinControlsVisiblity(true, false, false, false, false)
        if (easyLoaddingView is EasyLoaddingView) {
            (easyLoaddingView as EasyLoaddingView).reset()
        }
    }

    /**
     * 设置其他小控件
     */
    protected fun setMinControlsVisiblity(
        startBtn: Boolean, loadingPro: Boolean,
        thumbImg: Boolean, bottomPrp: Boolean, retryLayout: Boolean
    ) {
        startButton.visibility = if (startBtn) View.VISIBLE else View.GONE
        easyLoaddingView.visibility = if (loadingPro) View.VISIBLE else View.GONE
        thumbImageView.visibility = if (thumbImg) View.VISIBLE else View.GONE
        mRetryLayout.visibility = if (retryLayout) View.VISIBLE else View.GONE
        bottomProgressBar.visibility = if (bottomPrp) View.VISIBLE else View.GONE
    }

    /**
     * 更新开始的按钮图片
     */
    fun updateStartImage(currentState: VideoStatus) {
        when (currentState) {
            VideoStatus.PLAYING -> {
                startButton.setImageResource(iconPause)
                replayTextView.visibility = View.GONE
            }

            VideoStatus.ERROR -> {
                startButton.visibility = View.INVISIBLE
                replayTextView.visibility = View.GONE
            }

            VideoStatus.AUTO_COMPLETE -> {
                startButton.setImageResource(iconReplay)
                replayTextView.visibility = View.VISIBLE
            }

            else -> {
                startButton.setImageResource(iconPlay)
                replayTextView.visibility = View.GONE
            }
        }
    }

    /**
     * 清空全部ui展示
     */
    fun changeUiToClean() {
        if (!animatorSet.isRunning) {
            hintContainer(true)
        }
        setMinControlsVisiblity(false, false, false, false, false)
    }

    fun containerIsShow(): Boolean {
        return bottomContainer.visibility == View.VISIBLE || topContainer.visibility == View.VISIBLE
    }

    /**
     * 开始进度定时器
     */
    fun startProgressSchedule() {
        bottomContainer.startProgressSchedule()
    }

    /**
     * 取消进度定时器
     */
    fun stopProgressSchedule() {
        bottomContainer.stopProgressSchedule()
    }

    /**
     * 设置进度  如果2个值都是100，就会设置最大值，如果某个值<0 就不设置
     *
     * @param progress          主进度
     * @param secondaryProgress 缓存进度
     */
    fun setProgress(progress: Int, secondaryProgress: Int) {
        if (progress >= 0) {
            bottomProgressBar.progress = progress
        }
        if (secondaryProgress >= 0) {
            bottomProgressBar.secondaryProgress = secondaryProgress
        }
        bottomContainer.setProgress(progress, secondaryProgress)
    }

    val bufferProgress: Int
        get() = bottomContainer.bufferProgress

    fun setTime(position: Long, duration: Long) {
        bottomContainer.setTime(position, duration)
    }

    /**
     * 显示或者隐藏顶部和底部控制器
     */
    fun showControllerViewAnim(currentState: VideoStatus, isShow: Boolean) {
        if (currentState != VideoStatus.NORMAL && currentState != VideoStatus.ERROR && currentState != VideoStatus.AUTO_COMPLETE) {
            if (isShow) {
                showContainer(true)
                startButton.visibility = if (this.currentState == VideoStatus.BUFFERING_START) View.GONE else View.VISIBLE
                bottomProgressBar.visibility = View.GONE
            } else {
                hintContainer(true)
                startButton.visibility = View.GONE
                bottomProgressBar.visibility = View.VISIBLE
            }
        }
    }

    private fun initAnimator() {
        animatorSet.duration = 300
        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                if (isCurrentAnimHint) {
                    hintContainer(false)
                }
            }

            override fun onAnimationStart(animation: Animator) {
                super.onAnimationStart(animation)
                showContainer(false)
            }
        })
    }

    private fun hintContainer(isAnim: Boolean) {
        if (!isAnim) {
            topContainer.visibility = View.GONE
            bottomContainer.visibility = View.GONE
        } else {
            if (topContainer.visibility == View.VISIBLE || bottomContainer.visibility == View.VISIBLE) {
                animatorSet.cancel()
                isCurrentAnimHint = true
                val animatorTop = ObjectAnimator.ofFloat(
                    topContainer, "translationY",
                    0f, -topContainer.height.toFloat()
                )
                val animatoBottom = ObjectAnimator.ofFloat(
                    bottomContainer, "translationY",
                    0f, bottomContainer.height.toFloat()
                )
                animatorSet.play(animatorTop).with(animatoBottom)
                animatorSet.start()
            }
        }
    }

    private fun showContainer(isAnim: Boolean) {
        if (!isAnim) {
            if (isOnlyFullShowTitle && !isFull) {
                topContainer.visibility = View.GONE
            } else {
                topContainer.visibility = View.VISIBLE
            }
            bottomContainer.visibility = View.VISIBLE
        } else {
            if (topContainer.visibility == View.GONE || bottomContainer.visibility == View.GONE) {
                animatorSet.cancel()
                isCurrentAnimHint = false
                val animatorTop = ObjectAnimator.ofFloat(
                    topContainer, "translationY",
                    -topContainer.height.toFloat(), 0f
                )
                val animatoBottom = ObjectAnimator.ofFloat(
                    bottomContainer, "translationY",
                    bottomContainer.height.toFloat(), 0f
                )
                animatorSet.play(animatorTop).with(animatoBottom)
                animatorSet.start()
            }
        }
    }

    /**
     * 非全屏时候返回键隐藏的时候预留左边空间
     */
    fun setBackGoneLeftSize(backGoneLeftSize: Int) {
        topContainer.setBackGoneLeftSize(backGoneLeftSize)
    }
}