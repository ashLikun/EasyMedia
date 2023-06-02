package com.ashlikun.media.music

import android.content.Context
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener
import com.ashlikun.media.video.EasyMediaManager

/**
 * 作者　　: 李坤
 * 创建时间: 2020/11/18　19:02
 * 邮箱　　：496546144@qq.com
 *
 *
 * 功能介绍：
 */
object MusicUtils {

    fun setAudioFocus(context: Context, isFocus: Boolean) {
        val mAudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (isFocus) {
            mAudioManager.requestAudioFocus(onAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
        } else {
            mAudioManager.abandonAudioFocus(onAudioFocusChangeListener)
        }
    }

    /**
     * 是否新建个class，代码更规矩，并且变量的位置也很尴尬
     */
    var onAudioFocusChangeListener = OnAudioFocusChangeListener { focusChange: Int ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {}
            AudioManager.AUDIOFOCUS_LOSS -> releaseAll()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                EasyMediaManager.getMusicAll().forEach {
                    if (it.value.mediaPlay.isPlaying) {
                        it.value.mediaPlay.pause()
                    }
                }
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {}
        }
    }

    /**
     * 释放音频
     */

    fun releaseAll(vararg tag: String) {
        //释放播放器
        if (tag.isEmpty()) {
            EasyMediaManager.getInstance(EasyMediaManager.TAG_MUSIC).releaseMediaPlayer(true)
        } else {
            EasyMediaManager.getTag(*tag).forEach {
                it.value.releaseMediaPlayer(true)
            }
        }
    }

    /**
     * 对应activity得生命周期
     */

    fun onDestroy() {
        releaseAll()
    }
}