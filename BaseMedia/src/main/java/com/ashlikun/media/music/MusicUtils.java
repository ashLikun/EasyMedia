package com.ashlikun.media.music;

import android.content.Context;
import android.media.AudioManager;

import com.ashlikun.media.video.EasyMediaManager;

/**
 * 作者　　: 李坤
 * 创建时间: 2020/11/18　19:02
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：
 */
public class MusicUtils {

    public static void setAudioFocus(Context context, boolean isFocus) {
        AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (isFocus) {
            mAudioManager.requestAudioFocus(onAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        } else {
            mAudioManager.abandonAudioFocus(onAudioFocusChangeListener);
        }
    }

    public static AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {//是否新建个class，代码更规矩，并且变量的位置也很尴尬
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    releaseAllVideos();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    if (EasyMediaManager.getMusicMediaPlay().isPlaying()) {
                        EasyMediaManager.getMusicMediaPlay().pause();
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    break;
            }
        }
    };

    /**
     * 释放音频
     */
    public static void releaseAllVideos() {
        //把之前的设置到完成状态
        EasyMusicPlayerManager.completeAll();
        //释放播放器
        EasyMediaManager.getInstanceMusic().releaseMediaPlayer();
    }

    /**
     * 对应activity得生命周期
     */
    public static void onDestroy() {
        releaseAllVideos();
    }
}
