package com.ashlikun.media.video.cache

import android.content.Context
import tv.danmaku.ijk.media.player.IMediaPlayer
import java.io.File

/**
 * 缓存管理接口
 * Created by guoshuyu on 2018/5/18.
 */
interface ICacheManager {
    /**
     * 开始缓存逻辑
     *
     * @param mediaPlayer 播放内核
     * @param url         播放url
     * @param header      头部信息
     * @param cachePath   缓存路径，可以为空
     */
    fun doCacheLogic(context: Context?, mediaPlayer: IMediaPlayer?, url: String?, header: Map<String?, String?>?, cachePath: File?)

    /**
     * 清除缓存
     *
     * @param cachePath 可以为空，空时用默认
     * @param url       可以为空，空时清除所有
     */
    fun clearCache(context: Context?, cachePath: File?, url: String?)

    /**
     * 是否缓存管理
     */
    fun release()

    /**
     * 播放中判断是否缓存，会频繁调用
     */
    fun hadCached(): Boolean

    /**
     * 播放前判断是否缓存
     */
    fun cachePreview(context: Context?, cacheDir: File?, url: String?): Boolean
    fun setCacheAvailableListener(cacheAvailableListener: ICacheAvailableListener?)

    /**
     * 缓存进度接口
     */
    interface ICacheAvailableListener {
        fun onCacheAvailable(cacheFile: File?, url: String?, percentsAvailable: Int)
    }
}