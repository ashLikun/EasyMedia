package com.ashlikun.media.simple.data

import android.text.TextUtils
import com.google.gson.annotations.SerializedName

/**
 * 作者　　: 李坤
 * 创建时间: 2017/12/13　17:23
 * 邮箱　　：496546144@qq.com
 *
 *
 * 功能介绍：
 */
class NeiHanData {
    var group: Group? = null
    var type = 0
    val videoUrl: String?
        get() {
            if (group != null) {
                if (!TextUtils.isEmpty(group!!.video360!!.uri)) {
                    return group!!.video360!!.uri
                } else if (!TextUtils.isEmpty(group!!.video480!!.uri)) {
                    return group!!.video480!!.uri
                } else if (!TextUtils.isEmpty(group!!.video720!!.uri)) {
                    return group!!.video720!!.uri
                } else if (!TextUtils.isEmpty(group!!.mp4_url)) {
                    return group!!.mp4_url
                }
            }
            return ""
        }
    val text: String?
        get() = if (group != null) {
            group!!.text
        } else ""
    val heigth: Int
        get() {
            if (group != null) {
                if (group!!.video360!!.height > 0) {
                    return group!!.video360!!.height
                } else if (group!!.video480!!.height > 0) {
                    return group!!.video480!!.height
                } else if (group!!.video720!!.height > 0) {
                    return group!!.video720!!.height
                } else if (group!!.video_height > 0) {
                    return group!!.video_height
                }
            }
            return 0
        }
    val width: Int
        get() {
            if (group != null) {
                if (group!!.video360!!.width > 0) {
                    return group!!.video360!!.width
                } else if (group!!.video480!!.width > 0) {
                    return group!!.video480!!.width
                } else if (group!!.video720!!.width > 0) {
                    return group!!.video720!!.width
                } else if (group!!.video_wh_ratio > 0) {
                    return group!!.video_wh_ratio
                }
            }
            return 0
        }
    val imageUrl: String?
        get() = if (group != null && group!!.large_cover != null) {
            group!!.large_cover!!.uri
        } else ""

    class Group {
        val video_id: String? = null

        @SerializedName("360p_video")
        val video360: Video360PBean? = null
        val mp4_url: String? = null
        val text: String? = null

        @SerializedName("720p_video")
        val video720: Video720PBean? = null
        val duration = 0.0

        @SerializedName("480p_video")
        val video480: Video480PBean? = null
        val create_time = 0
        val id: Long = 0
        val large_cover: LargeCoverBean? = null
        val video_wh_ratio = 0
        val download_url: String? = null
        val video_height = 0
        val video_width = 0

        class Video360PBean {
            /**
             * width : 480
             * url_list : [{"url":"http://v3-nh.ixigua.com/8f64cfaf6e4c7f74c5c1adc2740117b3/5a30efd7/video/m/22011118f6f2192441e8c0aebbe98484adc1152c8880000aa739de4f296/","expires":1513157167},{"url":"http://ic.snssdk.com/neihan/video/playback/1513156567.83/?video_id=d4a7221567104b2db919a78cc8631f23&quality=360p&line=1&is_gif=0&device_platform=android"}]
             * uri : 360p/d4a7221567104b2db919a78cc8631f23
             * height : 854
             */
            var width = 0
            var height = 0
            var url_list: List<UrlListBean>? = null
            val uri: String?
                get() = if (url_list == null || url_list!!.size == 0) {
                    ""
                } else url_list!![0].url

            class UrlListBean {
                /**
                 * url : http://v3-nh.ixigua.com/8f64cfaf6e4c7f74c5c1adc2740117b3/5a30efd7/video/m/22011118f6f2192441e8c0aebbe98484adc1152c8880000aa739de4f296/
                 * expires : 1513157167
                 */
                var url: String? = null
                var expires = 0
            }
        }

        class Video720PBean {
            /**
             * width : 480
             * url_list : [{"url":"http://v3-nh.ixigua.com/8f64cfaf6e4c7f74c5c1adc2740117b3/5a30efd7/video/m/22011118f6f2192441e8c0aebbe98484adc1152c8880000aa739de4f296/","expires":1513157167},{"url":"http://ic.snssdk.com/neihan/video/playback/1513156567.83/?video_id=d4a7221567104b2db919a78cc8631f23&quality=720p&line=1&is_gif=0&device_platform=android"}]
             * uri : 720p/d4a7221567104b2db919a78cc8631f23
             * height : 854
             */
            var width = 0
            var uri: String? = null
            var height = 0
            var url_list: List<UrlListBeanX>? = null

            class UrlListBeanX {
                /**
                 * url : http://v3-nh.ixigua.com/8f64cfaf6e4c7f74c5c1adc2740117b3/5a30efd7/video/m/22011118f6f2192441e8c0aebbe98484adc1152c8880000aa739de4f296/
                 * expires : 1513157167
                 */
                var url: String? = null
                var expires = 0
            }
        }

        class Video480PBean {
            /**
             * width : 480
             * url_list : [{"url":"http://v3-nh.ixigua.com/8f64cfaf6e4c7f74c5c1adc2740117b3/5a30efd7/video/m/22011118f6f2192441e8c0aebbe98484adc1152c8880000aa739de4f296/","expires":1513157167},{"url":"http://ic.snssdk.com/neihan/video/playback/1513156567.83/?video_id=d4a7221567104b2db919a78cc8631f23&quality=480p&line=1&is_gif=0&device_platform=android"}]
             * uri : 480p/d4a7221567104b2db919a78cc8631f23
             * height : 854
             */
            var width = 0
            var uri: String? = null
            var height = 0
            var url_list: List<UrlListBeanXX>? = null

            class UrlListBeanXX {
                /**
                 * url : http://v3-nh.ixigua.com/8f64cfaf6e4c7f74c5c1adc2740117b3/5a30efd7/video/m/22011118f6f2192441e8c0aebbe98484adc1152c8880000aa739de4f296/
                 * expires : 1513157167
                 */
                var url: String? = null
                var expires = 0
            }
        }

        class LargeCoverBean {
            /**
             * url_list : [{"url":"http://p3.pstatp.com/large/4c2100134d006e2a0172.webp"},{"url":"http://pb9.pstatp.com/large/4c2100134d006e2a0172.webp"},{"url":"http://pb1.pstatp.com/large/4c2100134d006e2a0172.webp"}]
             * uri : large/4c2100134d006e2a0172
             */
            var uri: String? = null
            var url_list: List<UrlListBeanXXX>? = null

            class UrlListBeanXXX {
                /**
                 * url : http://p3.pstatp.com/large/4c2100134d006e2a0172.webp
                 */
                var url: String? = null
            }
        }

        fun `get_$360p_video`(): Video360PBean? {
            return video360
        }

        fun `get_$720p_video`(): Video720PBean? {
            return video720
        }

        fun `get_$480p_video`(): Video480PBean? {
            return video480
        }
    }
}