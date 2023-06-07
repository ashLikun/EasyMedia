package com.ashlikun.media.simple

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ashlikun.media.video.VideoData
import com.ashlikun.media.video.VideoScreenUtils
import com.ashlikun.media.video.VideoUtils
import com.ashlikun.media.video.play.EasyVideoIjkplayer
import com.ashlikun.media.video.view.EasyMediaPlayer

class MainDetailsActivity : AppCompatActivity() {
    val mediaPlay: EasyMediaPlayer by lazy {
        findViewById<View>(R.id.mediaPlay) as EasyMediaPlayer
    }

    /**
     * 上一个列表传递过来的数据
     */
    var oldPlayData: VideoData? = null
    var isOnBackPressed = false
    var isRePlay = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        oldPlayData = intent.getSerializableExtra("data") as VideoData?
        VideoUtils.init(application, EasyVideoIjkplayer::class.java)
        setContentView(R.layout.activity_main_details)
        if (oldPlayData != null) {
            if (!VideoScreenUtils.startCacheVideo(mediaPlay, oldPlayData)) {
                mediaPlay.setDataSource(oldPlayData!!)
                mediaPlay.startVideo()
            } else {
                isRePlay = true
            }
        }
    }

    override fun onBackPressed() {
        if (!VideoScreenUtils.onBackPressed()) {
            isOnBackPressed = true
            super.onBackPressed()
        }
    }

    override fun onPause() {
        super.onPause()
        //不是返回按下或者之前页面没有有播放的时候  停止
        if (!isOnBackPressed || !isRePlay) {
            mediaPlay!!.onPause()
        }
    }

    override fun onResume() {
        super.onResume()
        if (mediaPlay.mediaManager.viewManager.currentVideoPlay != null) {
            val dd = mediaPlay.mediaManager.viewManager.currentVideoPlay?.currentData ?: return
            if (dd.equalsUrl(oldPlayData)) {
                mediaPlay.onResume()
            } else if (mediaPlay.currentData?.equalsUrl(dd) == true) {
                mediaPlay.onResume()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!isOnBackPressed || !isRePlay) {
            mediaPlay!!.onDestroy()
        }
    }
}