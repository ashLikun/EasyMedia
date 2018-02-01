package com.ashlikun.media;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.ashlikun.media.view.EasyVideoPlayer;

/**
 * 作者　　: 李坤
 * 创建时间: 2017/12/7　9:54
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：全屏的重力传感器监听实现
 */

public class MediaAutoFullSensorListener implements SensorEventListener {
    @Override
    public void onSensorChanged(SensorEvent event) {//可以得到传感器实时测量出来的变化值
        final float x = event.values[SensorManager.DATA_X];
        float y = event.values[SensorManager.DATA_Y];
        float z = event.values[SensorManager.DATA_Z];
        //过滤掉用力过猛会有一个反向的大数值
        if (((x > -15 && x < -10) || (x < 15 && x > 10)) && Math.abs(y) < 1.5) {
            if ((System.currentTimeMillis() - EasyVideoPlayer.lastAutoFullscreenTime) > 2000) {
                if (EasyVideoPlayerManager.getCurrentVideoPlayerNoTiny() != null) {
                    EasyVideoPlayerManager.getCurrentVideoPlayerNoTiny().autoFullscreen(x);
                }
                EasyVideoPlayer.lastAutoFullscreenTime = System.currentTimeMillis();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
