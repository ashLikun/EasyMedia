package com.ashlikun.media.video

import android.content.pm.ActivityInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.ashlikun.media.video.VideoScreenUtils.backPress
import com.ashlikun.media.video.VideoUtils.setRequestedOrientation
import com.ashlikun.media.video.status.VideoStatus
import com.ashlikun.media.video.view.EasyMediaPlayer
import kotlin.math.abs

/**
 * 作者　　: 李坤
 * 创建时间: 2017/12/7　9:54
 * 邮箱　　：496546144@qq.com
 *
 *
 * 功能介绍：全屏的重力传感器监听实现,可以自定义重写onSensorChanged
 */
class MediaAutoFullSensorListener(var mediaManager: EasyMediaManager) : SensorEventListener {
    override fun onSensorChanged(event: SensorEvent) {
        //可以得到传感器实时测量出来的变化值
        val x = event.values[SensorManager.DATA_X]
        val y = event.values[SensorManager.DATA_Y]
        val z = event.values[SensorManager.DATA_Z]
        //过滤掉用力过猛会有一个反向的大数值
        if (System.currentTimeMillis() - lastAutoFullscreenTime > 2000) {
            lastAutoFullscreenTime = System.currentTimeMillis()
            if (x > -15 && x < -10 || x < 15 && x > 10 && abs(y) < 1.5) {
                autoFullscreen(x)
            } else {
                autoQuitFullscreen()
            }
        }
    }

    /**
     * 自动退出全屏
     */
    fun autoQuitFullscreen() {
        val play = mediaManager.viewManager.currentVideoPlayerNoTiny
        if (play != null) {
            if (System.currentTimeMillis() - lastAutoFullscreenTime > 2000 && play.isCurrentPlay && play.currentState === VideoStatus.PLAYING && play.isFull) {
                lastAutoFullscreenTime = System.currentTimeMillis()
                backPress(mediaManager)
            }
        }
    }

    /**
     * 重力感应的时候调用的函数
     *
     * @param x
     */
    fun autoFullscreen(x: Float) {
        val playppp = mediaManager.viewManager.currentVideoPlayerNoTiny
        if (playppp != null && playppp is EasyMediaPlayer) {
            val play = playppp
            if (play.isCurrentPlay && play.currentState === VideoStatus.PLAYING) {
                if (x > 0) {
                    setRequestedOrientation(play.context, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
                } else {
                    setRequestedOrientation(play.context, ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE)
                }
                play.onEvent(EasyMediaEvent.ON_ENTER_FULLSCREEN)
                play.startWindowFullscreen()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    companion object {
        var lastAutoFullscreenTime: Long = 0
    }
}