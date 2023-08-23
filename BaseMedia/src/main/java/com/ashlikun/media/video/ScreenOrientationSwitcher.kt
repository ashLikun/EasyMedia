package com.ashlikun.media.video

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.hardware.SensorManager
import android.util.Log
import android.view.OrientationEventListener
import com.ashlikun.media.video.view.BaseEasyMediaPlay
import java.lang.ref.WeakReference


/**
 * 作者　　: 李坤
 * 创建时间: 2023/8/23　11:15
 * 邮箱　　：496546144@qq.com
 *
 * 功能介绍：
 */

class ScreenOrientationSwitcher @JvmOverloads
constructor(context: Context, rate: Int = SensorManager.SENSOR_DELAY_NORMAL) :
    OrientationEventListener(context, rate) {

    companion object {
        private const val MAX_CHECK_INTERVAL: Long = 3000

    }

    private val contextRef: WeakReference<Context> by lazy {
        WeakReference(context)
    }
    private var isSupportGravity = false
    var currOrientation = ORIENTATION_UNKNOWN
        private set

    var lastCheckTimestamp: Long = 0

    //是否与系统旋转匹配
    var isConfigSystem = true

    var changeListener: ((requestedOrientation: Int) -> Unit)? = null

    init {
        VideoUtils.getActivity(context)?.requestedOrientation?.also {
            currOrientation = it
        }
    }

    override fun onOrientationChanged(orientation: Int) {
        val context = contextRef.get()
        if (context == null || context !is Activity) {
            return
        }
        if (isConfigSystem) {
            val currTimestamp = System.currentTimeMillis()
            if (currTimestamp - lastCheckTimestamp > MAX_CHECK_INTERVAL) {
                isSupportGravity = VideoUtils.isScreenAutoRotate(context)
                lastCheckTimestamp = currTimestamp
            }
            if (!isSupportGravity) {
                return
            }
            if (orientation == ORIENTATION_UNKNOWN) {
                return
            }
        }
        var requestOrientation: Int = if (orientation > 350 || orientation < 10) {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else if (orientation in 81..99) {
            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
        } else if (orientation in 261..279) {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            return
        }
        if (requestOrientation == currOrientation) {
            return
        }
        val needNotify = currOrientation != ORIENTATION_UNKNOWN
        currOrientation = requestOrientation
        if (needNotify) {
            if (changeListener != null) {
                changeListener!!.invoke(requestOrientation)
            } else {
                context.requestedOrientation = requestOrientation
            }
        }
    }
}