package com.ashlikun.media.video

import android.net.Uri
import android.text.TextUtils
import tv.danmaku.ijk.media.player.misc.IMediaDataSource
import java.io.FileDescriptor
import java.io.Serializable

/**
 * 作者　　: 李坤
 * 创建时间: 2017/12/18　15:33
 * 邮箱　　：496546144@qq.com
 *
 *
 * 功能介绍：视频播放数据源
 */
data class VideoData(
    var url: String = "",
    var title: String = "",
    var uri: Uri? = null,

    //是否循环
    var isLooping: Boolean = VideoUtils.isLooping,

    //是否使用缓存
    var isCache: Boolean = VideoUtils.isCache,

    //缓存目录
    var cacheDir: String = VideoUtils.cacheDir,

    //类型覆盖 exo用的
    var overrideExtension: String? = null,

    var iMediaDataSource: IMediaDataSource? = null,
    var fileDescriptor: FileDescriptor? = null,
    var headers: MutableMap<String, String>? = null,

    //其他数据
    val otherParams: MutableMap<String, Any> = mutableMapOf()
) : Serializable {

    /**
     * 是否是本地文件
     *
     * @return
     */
    val isLocal: Boolean
        get() = url.startsWith("file") || uri.toString().startsWith("file") || fileDescriptor != null

    override fun equals(obj: Any?): Boolean {
        return if (obj == null) {
            false
        } else super.equals(obj) || (obj is VideoData && equalsUrl(obj))
    }

    fun equalsUrl(obj: VideoData?): Boolean {
        return if (obj == null) {
            false
        } else TextUtils.equals(
            if (isEmpty(uri)) "noCurr" else uri.toString(),
            if (isEmpty(obj.uri)) "noObj" else obj.uri.toString()
        ) ||
                TextUtils.equals(if (isEmpty(url)) "noCurr" else url, if (isEmpty(obj.url)) "noObj" else obj.url) ||
                TextUtils.equals(
                    if (isEmpty(fileDescriptor)) "noCurr" else fileDescriptor.toString(),
                    if (isEmpty(obj.fileDescriptor)) "noObj" else obj.fileDescriptor.toString()
                )
    }

    private fun isEmpty(text: Any?): Boolean {
        return if (text == null) {
            true
        } else TextUtils.isEmpty(text.toString())
    }

    fun addHead(key: String?, value: String) {
        if (key == null) {
            return
        }
        if (headers == null) {
            headers = HashMap()
        }
        headers!![key] = value
    }
}