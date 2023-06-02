package com.ashlikun.media.video.controller

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.ashlikun.media.R
import com.ashlikun.media.video.VideoData
import com.ashlikun.media.video.VideoScreenUtils.backPress
import java.text.SimpleDateFormat
import java.util.Date

/**
 * 作者　　: 李坤
 * 创建时间: 2017/12/7　15:25
 * 邮箱　　：496546144@qq.com
 *
 *
 * 功能介绍：控制器顶部
 */
class VideoControllerTop @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    RelativeLayout(context, attrs, defStyleAttr), View.OnClickListener {
    //当前系统时间
    var videoCurrentTime: TextView? = null
    var titleView: TextView? = null

    //电源管理
    var batteryLevel: ImageView? = null
    var backButton: ImageView? = null
    var batteryTimeLayout: ViewGroup? = null

    /**
     * 非全屏时候返回键隐藏的时候预留左边空间
     */
    private var backGoneLeftSize = 0
    private var defaultBackGoneLeftSize = 0
    private fun initView() {
        setBackgroundResource(R.drawable.easy_video_title_bg)
        LayoutInflater.from(context).inflate(R.layout.easy_video_layout_controller_top, this)
        videoCurrentTime = findViewById(R.id.video_current_time)
        batteryLevel = findViewById(R.id.battery_level)
        titleView = findViewById(R.id.title)
        backButton = findViewById(R.id.back)
        batteryTimeLayout = findViewById(R.id.battery_time_layout)
        defaultBackGoneLeftSize = (titleView!!.layoutParams as MarginLayoutParams).leftMargin
        backGoneLeftSize = defaultBackGoneLeftSize
        backButton!!.setOnClickListener(this)
    }

    fun setInitData(mediaData: VideoData?) {
        if (mediaData != null) {
            setTitle(mediaData.title)
        }
    }

    fun setFull(isFull: Boolean) {
        if (isFull) {
            setSystemTimeAndBattery()
            setBackIsShow(true)
            setBatteryIsShow(true)
        } else {
            setBackIsShow(false)
            setBatteryIsShow(false)
        }
    }

    //设置系统的时间和电量
    fun setSystemTimeAndBattery() {
        val dateFormater = SimpleDateFormat("HH:mm")
        val date = Date()
        videoCurrentTime!!.text = dateFormater.format(date)
        if (!battertReceiver.debugUnregister) {
            battertReceiver.debugUnregister = true
            context.registerReceiver(
                battertReceiver,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            )
        }
    }

    fun setTitle(title: String?) {
        if (title != null) {
            titleView!!.text = title
        }
    }

    fun setBackIsShow(isShow: Boolean) {
        if (isShow) {
            backButton!!.visibility = VISIBLE
            (titleView!!.layoutParams as LayoutParams).leftMargin = defaultBackGoneLeftSize
        } else {
            (titleView!!.layoutParams as LayoutParams).leftMargin = backGoneLeftSize
            backButton!!.visibility = GONE
        }
    }

    fun setBatteryIsShow(isShow: Boolean) {
        batteryTimeLayout!!.visibility = if (isShow) VISIBLE else GONE
    }

    //电量
    private val battertReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (Intent.ACTION_BATTERY_CHANGED == action) {
                val level = intent.getIntExtra("level", 0)
                val scale = intent.getIntExtra("scale", 100)
                val percent = level * 100 / scale
                if (percent < 15) {
                    batteryLevel!!.setBackgroundResource(R.mipmap.easy_video_battery_level_10)
                } else if (percent in 15..39) {
                    batteryLevel!!.setBackgroundResource(R.mipmap.easy_video_battery_level_30)
                } else if (percent in 40..59) {
                    batteryLevel!!.setBackgroundResource(R.mipmap.easy_video_battery_level_50)
                } else if (percent in 60..79) {
                    batteryLevel!!.setBackgroundResource(R.mipmap.easy_video_battery_level_70)
                } else if (percent in 80..94) {
                    batteryLevel!!.setBackgroundResource(R.mipmap.easy_video_battery_level_90)
                } else if (percent in 95..100) {
                    batteryLevel!!.setBackgroundResource(R.mipmap.easy_video_battery_level_100)
                }
                getContext().unregisterReceiver(this)
                this.debugUnregister = false
            }
        }
    }

    init {
        initView()
    }

    /**
     * 非全屏时候返回键隐藏的时候预留左边空间
     *
     * @param backGoneLeftSize
     */
    fun setBackGoneLeftSize(backGoneLeftSize: Int) {
        this.backGoneLeftSize = backGoneLeftSize
        setBackIsShow(backButton!!.visibility == VISIBLE)
    }

    override fun onClick(v: View) {
        val i = v.id
        if (i == R.id.back) {
            backPress()
        }
    }
}