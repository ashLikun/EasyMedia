package com.ashlikun.media.video

import android.content.Context
import android.graphics.SurfaceTexture
import android.os.Handler
import android.os.Looper
import android.view.Surface
import android.view.TextureView
import android.view.TextureView.SurfaceTextureListener
import android.view.ViewGroup
import com.ashlikun.media.music.EasyMusicViewManager
import com.ashlikun.media.music.HandleMusicPlayEvent
import com.ashlikun.media.video.listener.MediaEventCall
import com.ashlikun.media.video.play.EasyVideoSystem
import com.ashlikun.media.video.status.VideoDisplayType
import com.ashlikun.media.video.view.EasyTextureView
import java.util.concurrent.ConcurrentHashMap

/**
 * @author　　: 李坤
 * 创建时间: 2018/11/10 16:01
 * 邮箱　　：496546144@qq.com
 *
 *
 * 功能介绍：整个播放器的管理器
 * 保证只有一个播放器
 * 保证渲染器也是只有一个(这样实现无缝切换)
 */
class EasyMediaManager(val tag: String) : SurfaceTextureListener {
    /**
     * 播放器
     * 使用面向接口编程
     */
    val mediaPlay: EasyMediaInterface by lazy {
        if (VideoUtils.mediaPlayClass != null) {
            runCatching {
                val it = VideoUtils.mediaPlayClass.getDeclaredConstructor(EasyMediaManager::class.java)
                it.isAccessible = true
                it.newInstance(this)
            }.getOrNull() ?: runCatching {
                VideoUtils.mediaPlayClass.newInstance()
            }.getOrNull() ?: EasyVideoSystem(this)
        } else {
            EasyVideoSystem(this)
        }
    }

    /**
     * 播放器控件的管理器，可全局调用BaseEasyVideoPlay
     */
    val viewManager by lazy {
        EasyVideoViewManager(this)
    }

    /**
     * 音乐播放器控件的管理器，可全局调用
     */
    val musicViewManager by lazy {
        EasyMusicViewManager(this)
    }

    /**
     * 播放器控件
     */
    var textureView: EasyTextureView? = null
        internal set

    /**
     * 用来捕获视频流中的图像帧的
     */
    var savedSurfaceTexture: SurfaceTexture? = null
        internal set

    /**
     * 设置给MediaPlay的渲染器(就是内存中的一段绘图缓冲区),里面有savedSurfaceTexture
     * 这个是保证无缝切换的重点
     */
    var surface: Surface? = null
        internal set

    /**
     * 当前播放的视频的大小
     */

    var currentVideoWidth = 0
        internal set


    var currentVideoHeight = 0
        internal set

    /**
     * 主线程的handler
     */
    val mediaHandler by lazy { Handler(Looper.getMainLooper()) }

    /**
     * 处理播放器事件
     */
    val handlePlayEvent by lazy {
        if (tag.startsWith(TAG_VIDEO)) HandleVideoPlayEvent(this) else HandleMusicPlayEvent(this)
    }

    /**
     * 播放事件的回掉
     */

    var onEventCall: MediaEventCall? = null

    /**
     * 网络状态 准备的时候
     */

    var netSate = "NORMAL"
        internal set

    /**
     * 正在播放的视频数据
     */
    var currentDataSource: VideoData?
        get() = mediaPlay.currentDataSource
        set(currentDataSource) {
            mediaPlay.currentDataSource = currentDataSource
        }

    val currentPosition: Long
        get() = mediaPlay.currentPosition

    /**
     * 初始化TextureView
     */
    fun initTextureView(context: Context, displayType: VideoDisplayType, old: EasyTextureView? = null) {
        textureView = EasyTextureView(context)
        if (old != null) {
            textureView!!.setVideoSize(old.currentVideoWidth, old.currentVideoHeight)
        }
        textureView!!.displayType = displayType
        textureView!!.surfaceTextureListener = this
        //用之前已经存在的savedSurfaceTexture，实现无差别播放
        if (savedSurfaceTexture != null && savedSurfaceTexture !== textureView!!.surfaceTexture) {
            textureView!!.setSurfaceTexture(savedSurfaceTexture!!)
        }
    }

    /**
     * 释放播放器
     */
    fun releaseMediaPlayer(isCompleteAll: Boolean = false) {
        //把之前的设置到完成状态
        if (isCompleteAll) {
            viewManager.completeAll()
        }
        if (isPlayingNei) {
            mediaPlay.stop()
        }
        mediaPlay.release()
    }


    private val isPlayingNei: Boolean
        get() = runCatching { mediaPlay.isPlaying }.getOrNull() ?: false

    /**
     * 准备播放
     */
    fun prepare() {
        releaseMediaPlayer()
        netSate = NetworkUtils.getNetWorkTypeName()
        currentVideoWidth = 0
        currentVideoHeight = 0
        mediaPlay.prepare()
        if (surface != null) {
            surface!!.release()
        }
        if (savedSurfaceTexture != null) {
            surface = Surface(savedSurfaceTexture)
            mediaPlay.setSurface(surface!!)
        }
    }

    /**
     * 释放渲染器和保存的SurfaceTexture，textureView
     */
    fun releaseAllSufaceView() {
        surface?.release()
        savedSurfaceTexture?.release()
        textureView = null
        savedSurfaceTexture = null
    }

    override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, i: Int, i1: Int) {
        if (savedSurfaceTexture == null) {
            savedSurfaceTexture = surfaceTexture
            prepare()
        } else {
            //说明之前正在某个SurfaceTexture上渲染播放，这里直接拿来播放
            textureView!!.setSurfaceTexture(savedSurfaceTexture!!)
        }
    }

    override fun onSurfaceTextureSizeChanged(surfaceTexture: SurfaceTexture, i: Int, i1: Int) {}
    override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
        return savedSurfaceTexture == null
    }

    override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {}

    /**
     * 移除TextureView
     */
    fun removeTextureView() {
        if (textureView != null && textureView!!.parent != null) {
            textureView!!.surfaceTextureListener = null
            (textureView!!.parent as ViewGroup).removeView(textureView)
        }
        savedSurfaceTexture = null
        textureView = null
    }

    val bufferedPercentage: Int
        get() = mediaPlay.bufferedPercentage


    val duration: Long
        get() = mediaPlay.duration


    fun seekTo(time: Long) {
        mediaPlay.seekTo(time)
    }


    fun pause() {
        mediaPlay.pause()
    }


    fun start() {
        mediaPlay.start()
    }

    val isPlaying: Boolean
        get() = runCatching { mediaPlay.isPlaying }.getOrNull() ?: false

    /**
     * 对应activity得生命周期
     */
    fun onPause() {
        viewManager.currentVideoPlayerNoTiny?.onPause()
    }

    /**
     * 对应activity得生命周期
     */
    fun onResume() {
        viewManager.currentVideoPlayerNoTiny?.onResume()
    }

    /**
     * 对应activity得生命周期
     */
    fun onDestroy() {
        //把之前的设置到完成状态
        viewManager.completeAll()
        //释放播放器
        releaseMediaPlayer()
        //释放渲染器
        releaseAllSufaceView()
    }

    companion object {
        /**
         * 单利模式,存放多个管理器
         */
        @Volatile
        var instance = ConcurrentHashMap<String, EasyMediaManager>()
        const val TAG_VIDEO = "VIDEO_"
        const val TAG_MUSIC = "MUSIC_"

        /**
         * 获取对应TAG的实例
         */
        fun getInstance(tag: String): EasyMediaManager {
            return if (instance.containsKey(tag)) {
                instance[tag]!!
            } else {
                synchronized(EasyMediaManager::class.java) {
                    if (!instance.containsKey(tag)) instance[tag] = EasyMediaManager(tag)
                    return instance[tag]!!
                }
            }
        }

        fun getVideoDefault(): EasyMediaManager {
            return getInstance(TAG_VIDEO)
        }

        fun getMusicDefault(): EasyMediaManager {
            return getInstance(TAG_MUSIC)
        }

        /**
         * 获取对应TAG的全部实例
         */
        fun getTag(vararg tag: String): Map<String, EasyMediaManager> {
            return instance.filter { tag.contains(it.key) }
        }

        /**
         * 获取对应TAG的全部实例
         */
        fun getTagAll(tag: String): Map<String, EasyMediaManager> {
            return instance.filter { it.key.startsWith(tag) }
        }

        fun getMusicAll(): Map<String, EasyMediaManager> {
            getInstance(TAG_MUSIC)
            return instance.filter { it.key.startsWith(TAG_MUSIC) }
        }

        fun getVideoAll(): Map<String, EasyMediaManager> {
            getInstance(TAG_VIDEO)
            return instance.filter { it.key.startsWith(TAG_VIDEO) }
        }


        fun pauseOther(easyMediaManager: EasyMediaManager) {
            //暂停其他的
            instance.forEach {
                if (it.value !== easyMediaManager) {
                    it.value.handlePlayEvent.onPause()
                }
            }
        }
    }
}