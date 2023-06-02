package com.ashlikun.media.video.view

import android.content.Context
import android.util.AttributeSet
import com.ashlikun.media.video.EasyVideoViewManager

/**
 * 作者　　: 李坤
 * 创建时间: 2018/8/21　15:07
 * 邮箱　　：496546144@qq.com
 *
 *
 * 功能介绍：简单的播放器，只会播放视频没有其他任何控制器
 * 适合抖音样式的短视频
 */
open class MiniMediaPlay @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    BaseEasyMediaPlay(context, attrs, defStyleAttr) {

    /**
     * 保存播放器 用于全局管理
     * [)][EasyVideoViewManager.setVideoDefault]
     * [)][EasyVideoViewManager.setVideoDefault]
     * [EasyVideoViewManager.setVideoTiny]
     * 可能会多次调用
     */
    override fun saveVideoPlayView() {
        mediaManager.viewManager.videoDefault = this
    }
}