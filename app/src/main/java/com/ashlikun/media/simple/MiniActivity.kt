package com.ashlikun.media.simple

import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.ashlikun.media.video.VideoUtils
import com.ashlikun.media.video.status.VideoDisplayType
import com.ashlikun.media.video.view.MiniMediaPlay

/**
 * 作者　　: 李坤
 * 创建时间: 2018/8/21　15:11
 * 邮箱　　：496546144@qq.com
 *
 *
 * 功能介绍：
 */
class MiniActivity : AppCompatActivity() {
    var videoPlay: MiniMediaPlay? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        videoPlay = MiniMediaPlay(this)
        videoPlay!!.displayType = VideoDisplayType.MATCH_CROP
        videoPlay!!.layoutParams = FrameLayout.LayoutParams(-1, -1)
        setContentView(videoPlay)
        videoPlay!!.setDataSource(VideoUrl.meinv2, "标题")
        videoPlay!!.startVideo()
    }

    override fun onPause() {
        super.onPause()
        videoPlay!!.onPause()
    }

    override fun onDestroy() {
        VideoUtils.releaseAll(videoPlay!!.mediaManageTag)
        videoPlay!!.onDestroy()
        super.onDestroy()

    }
}