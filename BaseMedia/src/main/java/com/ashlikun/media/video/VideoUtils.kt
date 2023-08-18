package com.ashlikun.media.video

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import com.ashlikun.media.video.VideoScreenUtils.isBackOk
import com.ashlikun.media.video.listener.MediaEventCall
import com.ashlikun.media.video.play.EasyVideoIjkplayer
import com.ashlikun.media.video.view.BaseEasyMediaPlay
import com.ashlikun.media.video.view.EasyMediaPlayer
import java.util.Formatter
import java.util.Locale
import java.util.concurrent.ScheduledThreadPoolExecutor

/**
 * 作者　　: 李坤
 * 创建时间: 2017/12/13 16:03
 * 邮箱　　：496546144@qq.com
 *
 *
 * 功能介绍：播放器工具
 */
object VideoUtils {
    //播放进度常量
    const val EASY_MEDIA_PROGRESS = "EASY_MEDIA_PROGRESS"


    lateinit var context: Context


    var isDebug = false

    lateinit var mediaPlayClass: Class<out EasyMediaInterface>


    /**
     * 当内部的播放器创建的时候
     */
    var onPlayerCreate: OnPlayerCreate? = null

    //是否循环
    var isLooping = false

    //是否使用缓存
    var isCache = true

    //缓存目录
    var cacheDir: String = ""

    /**
     * 是否开启硬解码
     */

    var isMediaCodec = false

    /**
     * 是否允许过非wifi播放视频,生命周期内，默认只提示一次
     */

    var wifiAllowPlay = false

    /**
     * 是否允许播放,播放过滤器
     */
    var onVideoAllowPlay = OnVideoAllowPlay()

    /**
     * 更新进度的定时器
     */
    val poolSchedule by lazy {
        ScheduledThreadPoolExecutor(4)
    }


    val mainHander by lazy {
        Handler(Looper.getMainLooper())
    }

    /**
     * 在Applicable里面初始化
     *
     * @param context
     * @param mediaPlayClass 设置播放器,默认为系统播放器
     */
    fun init(context: Application, mediaPlayClass: Class<out EasyMediaInterface> = EasyVideoIjkplayer::class.java) {
        this.context = context
        this.mediaPlayClass = mediaPlayClass
    }


    fun setIsDebug(isDebug: Boolean) {
        VideoUtils.isDebug = isDebug
    }


    fun stringForTime(timeMs: Long): String {
        if (timeMs <= 0 || timeMs >= 24 * 60 * 60 * 1000) {
            return "00:00"
        }
        val totalSeconds = timeMs / 1000
        val seconds = totalSeconds % 60
        val minutes = totalSeconds / 60 % 60
        val hours = totalSeconds / 3600
        val stringBuilder = StringBuilder()
        val mFormatter = Formatter(stringBuilder, Locale.getDefault())
        return if (hours > 0) {
            mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
        } else {
            mFormatter.format("%02d:%02d", minutes, seconds).toString()
        }
    }

    /**
     * 获取activity
     */

    fun getActivity(ctx: Context?): Activity? {
        if (ctx == null) return null
        when (ctx) {
            is Activity -> return ctx
            is ContextWrapper -> return getActivity(ctx.baseContext)
            is ContextThemeWrapper -> return getActivity(ctx.baseContext)
            else -> return null
        }
    }

    /**
     * 获取Activity的跟布局DecorView
     */
    fun getDecorView(context: Context?): ViewGroup {
        return getActivity(context)!!.window.decorView as ViewGroup
    }

    /**
     * 获取activity
     */
    fun getAppCompActivity(context: Context?): AppCompatActivity? {
        if (context == null) return null
        if (context is AppCompatActivity) {
            return context
        } else if (context is ContextThemeWrapper) {
            return getAppCompActivity(context.baseContext)
        }
        return null
    }


    fun setRequestedOrientation(context: Context?, orientation: Int?) {
        if (orientation != null)
            getActivity(context)?.requestedOrientation = orientation
    }


    fun getWindow(context: Context?): Window {
        return if (getAppCompActivity(context) != null) {
            getAppCompActivity(context)!!.window
        } else {
            getActivity(context)!!.window
        }
    }


    fun dip2px(context: Context, dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    /**
     * 保存播放进度
     *
     * @param context
     * @param url      对应的Url
     * @param progress 进度
     */
    fun saveProgress(context: Context, url: VideoData, progress: Long) {
        var progress = progress
        if (progress < 5000) {
            progress = 0
        }
        val spn = context.getSharedPreferences(EASY_MEDIA_PROGRESS, Context.MODE_PRIVATE)
        val editor = spn.edit()
        editor.putLong(url.toString(), progress)
        editor.apply()
    }

    /**
     * 获取保存的进度
     */

    fun getSavedProgress(context: Context, url: Any): Long {
        val spn = context.getSharedPreferences(EASY_MEDIA_PROGRESS, Context.MODE_PRIVATE)
        return spn.getLong(url.toString(), 0)
    }

    /**
     * 清空进度
     */
    fun clearProgress(context: Context, url: String?) {
        if (TextUtils.isEmpty(url)) {
            val spn = context.getSharedPreferences(EASY_MEDIA_PROGRESS, Context.MODE_PRIVATE)
            spn.edit().clear().apply()
        } else {
            val spn = context.getSharedPreferences(EASY_MEDIA_PROGRESS, Context.MODE_PRIVATE)
            spn.edit().putInt(url, 0).apply()
        }
    }

    /**
     * 当前的播放数组是否包含正在播放的url
     */

    fun isContainsUri(dataSource: List<VideoData>?, obj: VideoData?): Boolean {
        if (dataSource == null || obj == null) return false
        for (o in dataSource) return obj === o || obj.equalsUrl(o)
        return false
    }

    /**
     * 设置播放的事件回掉
     *
     * @param call 会一直持有这个对象，在application里面调用
     */
    fun setEasyMediaAction(vararg tag: String, call: MediaEventCall) {
        if (tag.isEmpty()) {
            EasyMediaManager.getInstance(EasyMediaManager.TAG_VIDEO).onEventCall = call
        } else {
            EasyMediaManager.getTag(*tag).forEach {
                it.value.onEventCall = call
            }
        }
    }

    /**
     * 释放全部video
     */
    fun releaseAll(vararg tag: String) {
        //这里判断，防止播放器点击全屏（这种清空不能释放）
        if (isBackOk) {
            //释放播放器
            if (tag.isEmpty()) {
                EasyMediaManager.getInstance(EasyMediaManager.TAG_VIDEO).releaseMediaPlayer(true)
            } else {
                EasyMediaManager.getTag(*tag).forEach {
                    it.value.releaseMediaPlayer(true)
                }
            }
        }
    }

    /**
     * 对应activity得生命周期
     */
    fun onPause(vararg tag: String) {
        if (tag.isEmpty()) {
            EasyMediaManager.getInstance(EasyMediaManager.TAG_VIDEO).onPause()
        } else {
            EasyMediaManager.getTag(*tag).forEach {
                it.value.onPause()
            }
        }
    }

    /**
     * 对应activity得生命周期
     */
    fun onResume(vararg tag: String) {
        if (tag.isEmpty()) {
            EasyMediaManager.getInstance(EasyMediaManager.TAG_VIDEO).onResume()
        } else {
            EasyMediaManager.getTag(*tag).forEach {
                it.value.onResume()
            }
        }
    }

    /**
     * 对应activity得生命周期
     */
    fun onDestroy(vararg tag: String) {
        if (tag.isEmpty()) {
            EasyMediaManager.getInstance(EasyMediaManager.TAG_VIDEO).also {
                it.onDestroy()
            }
        } else {
            EasyMediaManager.getTag(*tag).forEach { itt ->
                itt.value.onDestroy()
            }
        }
    }


    fun setAudioFocus(context: Context, isFocus: Boolean) {
        val mAudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (isFocus) {
            mAudioManager.requestAudioFocus(onAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
        } else {
            mAudioManager.abandonAudioFocus(onAudioFocusChangeListener)
        }
    }

    /**
     * 显示非wifi播放提示
     */

    fun videoAllowPlay(play: BaseEasyMediaPlay?): Boolean {
        if (onVideoAllowPlay.onIsAllow(play)) {
            onVideoAllowPlay.showWifiDialog(play)
            return true
        }
        return false
    }

    /**
     * 焦点改变的监听
     */
    private var onAudioFocusChangeListener = OnAudioFocusChangeListener { focusChange ->
        //是否新建个class，代码更规矩，并且变量的位置也很尴尬
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {}
            AudioManager.AUDIOFOCUS_LOSS -> releaseAll()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                EasyMediaManager.getVideoAll().forEach {
                    if (it.value.mediaPlay.isPlaying) {
                        it.value.mediaPlay.pause()
                    }
                }
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {}
        }
    }


    fun d(content: String?) {
        if (!isDebug) {
            return
        }
        val tag = generateTag()
        Log.d(tag, content!!)
    }

    /**
     * 得到标签,log标签+类名+方法名+第几行
     *
     * @return
     */
    private fun generateTag(): String {
        val callers = Throwable().stackTrace
        var tag = "%s.%s[L:%d]"
        //去除应用包名,和一些无关的
        val caller = callers.find { !it.className.contains(VideoUtils::class.java.simpleName) }
        var callerClazzName = caller?.className?.split(".")?.lastOrNull()?.split("$")?.firstOrNull() ?: "LogUtils"
        if (callerClazzName.contains(VideoUtils::class.java.simpleName)) {
            //这种情况说明代码是异步执行，找不到调用的地方
            tag = "VideoUtils"
        } else {
            tag = String.format(tag, callerClazzName, caller?.methodName.orEmpty(), caller?.lineNumber ?: 0)
        }
        return tag
    }
}