package com.ashlikun.media.video.view

import android.content.Context
import android.graphics.PixelFormat
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import com.ashlikun.media.R
import com.ashlikun.media.video.EasyVideoViewManager
import com.ashlikun.media.video.VideoScreenUtils.backPress
import com.ashlikun.media.video.VideoUtils
import com.ashlikun.media.video.status.VideoStatus

/**
 * 作者　　: 李坤
 * 创建时间: 2018/1/30　9:17
 * 邮箱　　：496546144@qq.com
 *
 *
 * 功能介绍：小窗口播放控件
 *
 *
 * <ImageView android:id="@+id/back_tiny" android:layout_width="24dp" android:layout_height="24dp" android:layout_marginLeft="6dp" android:layout_marginTop="6dp" android:src="@drawable/easy_video_click_back_tiny_selector" android:padding="5dp" android:visibility="gone"></ImageView>
 */
open class EasyMediaPlayTiny @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    BaseEasyMediaPlay(context, attrs, defStyleAttr), IEasyMediaPlayListener {
    var mWindowManager: WindowManager? = getContext().applicationContext
        .getSystemService(Context.WINDOW_SERVICE) as WindowManager
    var mLayoutParams = WindowManager.LayoutParams()
    protected var touchStartX = 0f
    protected var touchStartY = 0f

    /**
     * 状态栏高度
     */
    protected var statusHeight = 0

    init {
        initView(context, attrs)
    }

    protected fun initView(context: Context?, attrs: AttributeSet?) {
        statusHeight = statusBarHeight
        val imageView = ImageView(getContext())
        imageView.setImageResource(R.drawable.easy_video_click_back_tiny_selector)
        val containerBack = LayoutParams(VideoUtils.dip2px(context!!, 20f), VideoUtils.dip2px(context, 20f))
        containerBack.gravity = Gravity.TOP or Gravity.RIGHT
        addView(imageView, containerBack)
        imageView.setOnClickListener { backPress() }
    }

    /**
     * 播放器生命周期,自己主动调用的,还原状态
     */
    override fun onForceCompletionTo() {
        super.onForceCompletionTo()
        cleanTiny()
    }

    public override fun onStatePrepared() {
        super.onStatePrepared()
        //因为这个紧接着就会进入播放状态，所以不设置state
        val position = getSavedProgress()
        if (position > 0L) {
            mediaManager.seekTo(position)
        }
    }

    override fun onPlayStartClick() {
        if (mediaData == null || currentData == null) {
            Toast.makeText(context, resources.getString(R.string.easy_video_no_url), Toast.LENGTH_SHORT).show()
            return
        }
        if (currentState === VideoStatus.NORMAL) {
            if (VideoUtils.videoAllowPlay(this)) {
                return
            }
        }
        super.onPlayStartClick()

    }

    override fun onError(what: Int, extra: Int) {
        super.onError(what, extra)
        cleanTiny()
    }

    override fun onAutoCompletion(): Boolean {
        val res = super.onAutoCompletion()
        if (!res) {
            cleanTiny()
        }
        return res
    }


    override fun removeTextureView() {
        if (mediaManager.textureView != null) {
            textureViewContainer.removeView(mediaManager.textureView)
        }
    }

    /**
     * 保存播放器 用于全局管理
     * [)][EasyVideoViewManager.setVideoDefault]
     * [)][EasyVideoViewManager.setVideoDefault]
     * [EasyVideoViewManager.setVideoTiny]
     * 可能会多次调用
     */
    override fun saveVideoPlayView() {
        mediaManager.viewManager.videoTiny = this
    }

    /**
     * 清空小窗口
     */
    fun cleanTiny() {
        runCatching {
            mWindowManager?.removeView(this)
        }
        removeTextureView()
        mediaManager.viewManager.videoTiny = null
    }

    fun showWindow() {
        val width = context.resources.displayMetrics.widthPixels / 3 * 2
        // 窗体的布局样式
        mLayoutParams = WindowManager.LayoutParams()
        // 设置窗体显示类型——TYPE_SYSTEM_ALERT(系统提示)
        mLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
        // 设置窗体焦点及触摸：
        // FLAG_NOT_FOCUSABLE(不能获得按键输入焦点)
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        // 设置显示的模式
        mLayoutParams.format = PixelFormat.RGBA_8888
        // 设置对齐的方法
        mLayoutParams.gravity = Gravity.TOP or Gravity.LEFT
        // 设置窗体宽度和高度
        mLayoutParams.width = width
        mLayoutParams.height = (width / 16.0 * 9).toInt()
        //将指定View解析后添加到窗口管理器里面
        mWindowManager!!.addView(this, mLayoutParams)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val rawX = event.rawX
        val rawY = event.rawY - statusHeight
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchStartX = event.x
                touchStartY = event.y
            }

            MotionEvent.ACTION_MOVE -> {
                //原始坐标减去移动坐标
                mLayoutParams.x = (rawX - touchStartX).toInt()
                mLayoutParams.y = (rawY - touchStartY).toInt()
                mWindowManager!!.updateViewLayout(this, mLayoutParams)
            }
        }
        return super.onTouchEvent(event)
    }

    /**
     * 获取状态栏高度
     */
    val statusBarHeight: Int
        get() {
            val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
            return if (resourceId > 0) {
                //根据资源ID获取响应的尺寸值
                resources.getDimensionPixelSize(resourceId)
            } else 0
        }
}