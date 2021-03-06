package com.ashlikun.media.video.view;

import android.content.Context;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ashlikun.media.R;
import com.ashlikun.media.video.EasyMediaManager;
import com.ashlikun.media.video.EasyVideoAction;
import com.ashlikun.media.video.EasyVideoPlayerManager;
import com.ashlikun.media.video.VideoData;
import com.ashlikun.media.video.VideoScreenUtils;
import com.ashlikun.media.video.VideoUtils;
import com.ashlikun.media.video.status.VideoStatus;

import java.util.List;

import static com.ashlikun.media.video.status.VideoStatus.AUTO_COMPLETE;
import static com.ashlikun.media.video.status.VideoStatus.NORMAL;
import static com.ashlikun.media.video.status.VideoStatus.PAUSE;
import static com.ashlikun.media.video.status.VideoStatus.PLAYING;

/**
 * 作者　　: 李坤
 * 创建时间: 2018/1/30　9:17
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：小窗口播放控件
 * <p>
 * <ImageView
 * android:id="@+id/back_tiny"
 * android:layout_width="24dp"
 * android:layout_height="24dp"
 * android:layout_marginLeft="6dp"
 * android:layout_marginTop="6dp"
 * android:src="@drawable/easy_video_click_back_tiny_selector"
 * android:padding="5dp"
 * android:visibility="gone" />
 */

public class EasyVideoPlayTiny extends BaseEasyVideoPlay implements IEasyVideoPlayListener {
    WindowManager mWindowManager = (WindowManager) getContext().getApplicationContext()
            .getSystemService(Context.WINDOW_SERVICE);
    WindowManager.LayoutParams mLayoutParams = new WindowManager.LayoutParams();
    protected float touchStartX;
    protected float touchStartY;
    /**
     * 状态栏高度
     */
    protected int statusHeight;

    public EasyVideoPlayTiny(@NonNull Context context) {
        this(context, null);
    }

    public EasyVideoPlayTiny(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EasyVideoPlayTiny(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs);
        initView(context, attrs);
    }

    protected void initView(Context context, AttributeSet attrs) {
        statusHeight = getStatusBarHeight();
        ImageView imageView = new ImageView(getContext());
        imageView.setImageResource(R.drawable.easy_video_click_back_tiny_selector);
        LayoutParams containerBack = new LayoutParams(VideoUtils.dip2px(context, 20), VideoUtils.dip2px(context, 20));
        containerBack.gravity = Gravity.TOP | Gravity.RIGHT;
        addView(imageView, containerBack);
        imageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                VideoScreenUtils.backPress();
            }
        });
    }


    /**
     * 播放器生命周期,自己主动调用的,还原状态
     */
    @Override
    public void onForceCompletionTo() {
        super.onForceCompletionTo();
        cleanTiny();
    }

    @Override
    public boolean isScreenFull() {
        return false;
    }

    @Override
    public void onStatePrepared() {
        super.onStatePrepared();
        //因为这个紧接着就会进入播放状态，所以不设置state
        long position = VideoUtils.getSavedProgress(getContext(), getCurrentData());
        if (position != 0) {
            EasyMediaManager.seekTo(position);
        }
    }

    public void play() {
        if (mediaData == null || getCurrentData() == null) {
            Toast.makeText(getContext(), getResources().getString(R.string.easy_video_no_url), Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentState == NORMAL) {
            if (VideoUtils.videoAllowPlay(this)) {
                return;
            }
            startVideo();
        } else if (currentState == PLAYING) {
            onEvent(EasyVideoAction.ON_CLICK_PAUSE);
            EasyMediaManager.pause();
            setStatus(VideoStatus.PAUSE);
        } else if (currentState == PAUSE) {
            onEvent(EasyVideoAction.ON_CLICK_RESUME);
            EasyMediaManager.start();
            setStatus(PLAYING);
        } else if (currentState == AUTO_COMPLETE) {
            onEvent(EasyVideoAction.ON_CLICK_START_AUTO_COMPLETE);
            startVideo();
        }
    }

    @Override
    public void onError(int what, int extra) {
        super.onError(what, extra);
        cleanTiny();
    }

    @Override
    public boolean onAutoCompletion() {
        boolean res = super.onAutoCompletion();
        if (!res) {
            cleanTiny();
        }
        return res;
    }


    @Override
    public List<VideoData> getMediaData() {
        return mediaData;
    }

    @Override
    public void removeTextureView() {
        if (EasyMediaManager.getTextureView() != null) {
            textureViewContainer.removeView(EasyMediaManager.getTextureView());
        }
    }

    /**
     * 保存播放器 用于全局管理
     * {@link EasyVideoPlayerManager#setVideoDefault)}
     * {@link EasyVideoPlayerManager#setVideoDefault)}
     * {@link EasyVideoPlayerManager#setVideoTiny}
     * 可能会多次调用
     */
    @Override
    public void saveVideoPlayView() {
        EasyVideoPlayerManager.setVideoTiny(this);
    }

    /**
     * 清空小窗口
     */
    public void cleanTiny() {
        if (mWindowManager != null) {
            try {
                mWindowManager.removeView(this);
            } catch (Exception e) {
            }
        }
        removeTextureView();
        EasyVideoPlayerManager.setVideoTiny(null);
    }

    public void showWindow() {
        int width = getContext().getResources().getDisplayMetrics().widthPixels / 3 * 2;
        // 窗体的布局样式
        mLayoutParams = new WindowManager.LayoutParams();
        // 设置窗体显示类型——TYPE_SYSTEM_ALERT(系统提示)
        mLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        // 设置窗体焦点及触摸：
        // FLAG_NOT_FOCUSABLE(不能获得按键输入焦点)
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        // 设置显示的模式
        mLayoutParams.format = PixelFormat.RGBA_8888;
        // 设置对齐的方法
        mLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        // 设置窗体宽度和高度
        mLayoutParams.width = width;
        mLayoutParams.height = (int) (width / 16.0 * 9);
        //将指定View解析后添加到窗口管理器里面
        mWindowManager.addView(this, mLayoutParams);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float rawX = event.getRawX();
        float rawY = event.getRawY() - statusHeight;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStartX = event.getX();
                touchStartY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                //原始坐标减去移动坐标
                mLayoutParams.x = (int) (rawX - touchStartX);
                mLayoutParams.y = (int) (rawY - touchStartY);
                mWindowManager.updateViewLayout(this, mLayoutParams);
                break;
        }
        return super.onTouchEvent(event);
    }


    /**
     * 获取状态栏高度
     *
     * @return
     */
    public int getStatusBarHeight() {
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            //根据资源ID获取响应的尺寸值
            return getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }
}
