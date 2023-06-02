package com.ashlikun.media.music

import com.ashlikun.media.video.EasyMediaManager
import com.ashlikun.media.video.view.IEasyMediaPlayListener

/**
 * 作者　　: 李坤
 * 创建时间: 2018/2/1 14:18
 * 邮箱　　：496546144@qq.com
 *
 *
 * 功能介绍：音频播放器的管理器
 */
class EasyMusicViewManager internal constructor(var mediaManager: EasyMediaManager) {
    var musicDefault: IEasyMediaPlayListener? = null
        set(value) {
            if (field !== value) {
                if (value != null) {
                    //把之前的设置到完成状态
                    completeAll()
                }
                field = value
            }
        }

    /**
     * 获取当前正在播放
     */
    val currentMusicPlay: IEasyMediaPlayListener?
        get() = musicDefault

    /**
     * 强制释放全部播放器
     */
    fun completeAll() {
        musicDefault?.onForceCompletionTo()
        musicDefault = null
        mediaManager.currentDataSource = null
    }
}