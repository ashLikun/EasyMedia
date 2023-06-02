package com.ashlikun.media.simple.music

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import com.ashlikun.media.music.BaseEasyMusicPlay
import com.ashlikun.media.video.EasyMediaManager
import com.ashlikun.media.video.VideoData

/**
 * 作者　　: 李坤
 * 创建时间: 2020/11/18　16:57
 * 邮箱　　：496546144@qq.com
 *
 *
 * 功能介绍：
 */
class MusicView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    BaseEasyMusicPlay(context, attrs, defStyleAttr) {

    override fun setDataSource(mediaData: List<VideoData>, defaultIndex: Int): Boolean {
//        setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (getCurrentState() == VideoStatus.PLAYING) {
//                    onPause();
//                } else {
//                    play();
//                }
//            }
//        });
        return super.setDataSource(mediaData, defaultIndex)
    }

    override fun onStatePlaying() {
        super.onStatePlaying()
        mediaManager.seekTo(70)
    }

    override fun onStateAutoComplete() {
        super.onStateAutoComplete()
        Log.e("aaaaaaa", "cccccccc")
    }

    override fun onAutoCompletion(): Boolean {
        Log.e("aaaaaaa", "bbbbbbbbbbbb")
        return super.onAutoCompletion()
    }
}