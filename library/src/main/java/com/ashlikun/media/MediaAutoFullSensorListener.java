package com.ashlikun.media;

import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.ashlikun.media.view.BaseEasyVideoPlay;
import com.ashlikun.media.view.EasyVideoPlayer;

import static com.ashlikun.media.status.MediaStatus.PLAYING;

/**
 * 作者　　: 李坤
 * 创建时间: 2017/12/7　9:54
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：全屏的重力传感器监听实现,可以自定义重写onSensorChanged
 */

public class MediaAutoFullSensorListener implements SensorEventListener {
    public static long lastAutoFullscreenTime = 0;

    @Override
    public void onSensorChanged(SensorEvent event) {
        //可以得到传感器实时测量出来的变化值
        final float x = event.values[SensorManager.DATA_X];
        float y = event.values[SensorManager.DATA_Y];
        float z = event.values[SensorManager.DATA_Z];
        //过滤掉用力过猛会有一个反向的大数值
        if ((System.currentTimeMillis() - lastAutoFullscreenTime) > 2000) {
            lastAutoFullscreenTime = System.currentTimeMillis();
            if (((x > -15 && x < -10) || (x < 15 && x > 10)) && Math.abs(y) < 1.5) {
                autoFullscreen(x);
            } else {
                autoQuitFullscreen();
            }
        }
    }

    /**
     * 自动退出全屏
     */
    public void autoQuitFullscreen() {
        BaseEasyVideoPlay play = EasyVideoPlayerManager.getCurrentVideoPlayerNoTiny();
        if (play != null) {
            if ((System.currentTimeMillis() - lastAutoFullscreenTime) > 2000
                    && play.isCurrentPlay()
                    && play.getCurrentState() == PLAYING
                    && play.isScreenFull()) {
                lastAutoFullscreenTime = System.currentTimeMillis();
                MediaScreenUtils.backPress();
            }
        }
    }

    /**
     * 重力感应的时候调用的函数
     *
     * @param x
     */
    public void autoFullscreen(float x) {
        BaseEasyVideoPlay playppp = EasyVideoPlayerManager.getCurrentVideoPlayerNoTiny();
        if (playppp != null && playppp instanceof EasyVideoPlayer) {
            EasyVideoPlayer play = (EasyVideoPlayer) playppp;
            if (play.isCurrentPlay()
                    && play.getCurrentState() == PLAYING
                    && !play.isFullscreenPortrait()) {
                if (x > 0) {
                    MediaUtils.setRequestedOrientation(play.getContext(), ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                } else {
                    MediaUtils.setRequestedOrientation(play.getContext(), ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                }
                play.onEvent(EasyMediaAction.ON_ENTER_FULLSCREEN);
                play.startWindowFullscreen();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
