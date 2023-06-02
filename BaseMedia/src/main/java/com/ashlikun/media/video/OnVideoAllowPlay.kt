package com.ashlikun.media.video

import android.app.AlertDialog
import com.ashlikun.media.R
import com.ashlikun.media.video.VideoScreenUtils.clearFullscreenLayout
import com.ashlikun.media.video.view.BaseEasyMediaPlay

/**
 * 作者　　: 李坤
 * 创建时间: 2020/8/21　14:02
 * 邮箱　　：496546144@qq.com
 *
 *
 * 功能介绍：是否允许播放
 */
class OnVideoAllowPlay {
    /**
     * 是否允许播放
     */
    fun onIsAllow(play: BaseEasyMediaPlay?): Boolean {
        return (play?.currentData == null || play.currentData?.isLocal == false) && !NetworkUtils.isWifiConnected(play!!.context) && !VideoUtils.wifiAllowPlay
    }

    /**
     * 不允许播放显示的对话框
     */
    fun showWifiDialog(play: BaseEasyMediaPlay?) {
        if (play == null) return
        val builder = AlertDialog.Builder(play.context)
        builder.setMessage(play.context.resources.getString(R.string.easy_video_tips_not_wifi))
        builder.setPositiveButton(play.context.resources.getString(R.string.easy_video_tips_not_wifi_confirm)) { dialog, which ->
            dialog.dismiss()
            onAllow(play)
        }
        builder.setNegativeButton(play.context.resources.getString(R.string.easy_video_tips_not_wifi_cancel)) { dialog, which ->
            dialog.dismiss()
            onNoAllow(play)
        }
        builder.setOnCancelListener { dialog ->
            dialog.dismiss()
            onNoAllow(play)
        }
        builder.create().show()
    }

    /**
     * 当允许播放点击的时候
     */
    fun onAllow(play: BaseEasyMediaPlay) {
        VideoUtils.wifiAllowPlay = true
        play.onEvent(EasyMediaEvent.ON_CLICK_START_NO_WIFI_GOON)
        play.startVideo()
    }

    /**
     * 当不允许播放点击的时候
     */
    fun onNoAllow(play: BaseEasyMediaPlay) {
        if (play.isFull) {
            clearFullscreenLayout(play.context)
        }
    }
}