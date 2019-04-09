package com.ashlikun.media.view;

import android.content.Context;
import android.graphics.PixelFormat;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.ashlikun.media.EasyMediaAction;
import com.ashlikun.media.EasyMediaManager;
import com.ashlikun.media.EasyVideoPlayerManager;
import com.ashlikun.media.MediaData;
import com.ashlikun.media.MediaScreenUtils;
import com.ashlikun.media.MediaUtils;
import com.ashlikun.media.R;

import java.util.List;

import static com.ashlikun.media.status.MediaStatus.AUTO_COMPLETE;
import static com.ashlikun.media.status.MediaStatus.NORMAL;
import static com.ashlikun.media.status.MediaStatus.PAUSE;
import static com.ashlikun.media.status.MediaStatus.PLAYING;

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
 * android:src="@drawable/easy_media_click_back_tiny_selector"
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
        super(context, attrs);
    }

    @Override
    protected void initView(Context context, AttributeSet attrs) {
        super.initView(context, attrs);
        statusHeight = getStatusBarHeight();
        ImageView imageView = new ImageView(getContext());
        imageView.setImageResource(R.drawable.easy_media_click_back_tiny_selector);
        LayoutParams containerBack = new LayoutParams(dip2px(20), dip2px(20));
        containerBack.gravity = Gravity.TOP | Gravity.RIGHT;
        addView(imageView, containerBack);
        imageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaScreenUtils.backPress();
            }
        });
    }


    /**
     * 播放器生命周期,自己主动调用的,还原状态
     */
    @Override
    public void onForceCompletionTo() {
        super.onForceCompletionTo();
    }


    @Override
    public void onStatePrepared() {
        super.onStatePrepared();
        //因为这个紧接着就会进入播放状态，所以不设置state
        int position = MediaUtils.getSavedProgress(getContext(), getCurrentData());
        if (position != 0) {
            EasyMediaManager.seekTo(position);
        }
    }

    public void play() {
        if (mediaData == null || getCurrentData() == null) {
            Toast.makeText(getContext(), getResources().getString(R.string.no_url), Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentState == NORMAL) {
            if (MediaUtils.showWifiDialog(getContext(), getCurrentData(), this)) {
                return;
            }
            startVideo();
        } else if (currentState == PLAYING) {
            onEvent(EasyMediaAction.ON_CLICK_PAUSE);
            EasyMediaManager.pause();
            onStatePause();
        } else if (currentState == PAUSE) {
            onEvent(EasyMediaAction.ON_CLICK_RESUME);
            EasyMediaManager.start();
            onStatePlaying();
        } else if (currentState == AUTO_COMPLETE) {
            onEvent(EasyMediaAction.ON_CLICK_START_AUTO_COMPLETE);
            startVideo();
        }
    }

    @Override
    public void onError(int what, int extra) {
        super.onError(what, extra);
        cleanTiny();
    }

    @Override
    public void onAutoCompletion() {
        super.onAutoCompletion();
        cleanTiny();
    }


    @Override
    public List<MediaData> getMediaData() {
        return mediaData;
    }

    @Override
    public void removeTextureView() {
        if (EasyMediaManager.textureView != null) {
            textureViewContainer.removeView(EasyMediaManager.textureView);
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
    protected void saveVideoPlayView() {
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

    public int dip2px(float dipValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
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
