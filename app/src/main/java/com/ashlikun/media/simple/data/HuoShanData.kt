package com.ashlikun.media.simple.data

import com.ashlikun.okhttputils.http.response.HttpResponse
import com.google.gson.annotations.SerializedName

/**
 * 作者　　: 李坤
 * 创建时间: 2018/8/20　15:48
 * 邮箱　　：496546144@qq.com
 *
 *
 * 功能介绍：
 */
class HuoShanData : HttpResponse() {
    var result = 0
    var pcursor: String? = null
    var new_notify = 0
    var llsid: String? = null

    @SerializedName("host-name")
    var hostname: String? = null
    var feeds: List<FeedsData>? = null

    class FeedsData {
        var ext_params: ExtParamsData? = null
        var headurls: List<HeadurlsData>? = null
        var text: String? = null

        @SerializedName(value = "cover_urls", alternate = ["main_mv_urls"])
        var main_mv_urls: List<AudioUrlsData>? = null
        var cover_thumbnail_urls: List<AudioUrlsData>? = null
        var main_mv_urls_h265: List<AudioUrlsData>? = null

        class ExtParamsData {
            var mtype = 0
            var color: String? = null
            var w = 0
            var sound = 0
            var h = 0
            var interval = 0
            var video = 0
        }

        val width: Int
            get() = if (ext_params != null) ext_params!!.w else -2
        val imageUrl: String?
            get() = if (cover_thumbnail_urls != null && !cover_thumbnail_urls!!.isEmpty()) cover_thumbnail_urls!![0].url else ""
        val url: String?
            get() = if (main_mv_urls != null) main_mv_urls!![0].url else ""
        val height: Float
            get() = if (ext_params != null) ext_params!!.h.toFloat() else -2f
    }

    class HeadurlsData {
        var cdn: String? = null
        var url: String? = null
        var urlPattern: String? = null
    }

    class AudioUrlsData {
        var cdn: String? = null
        var url: String? = null
    }
}