package com.ashlikun.media.view;

import android.content.Context;
import android.graphics.PixelFormat;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.ashlikun.media.EasyMediaAction;
import com.ashlikun.media.EasyMediaManager;
import com.ashlikun.media.EasyVideoPlayerManager;
import com.ashlikun.media.MediaData;
import com.ashlikun.media.MediaUtils;
import com.ashlikun.media.R;
import com.ashlikun.media.play.EasyMediaSystem;
import com.ashlikun.media.status.MediaStatus;

import java.util.ArrayList;
import java.util.List;

import static com.ashlikun.media.status.MediaStatus.CURRENT_STATE_AUTO_COMPLETE;
import static com.ashlikun.media.status.MediaStatus.CURRENT_STATE_ERROR;
import static com.ashlikun.media.status.MediaStatus.CURRENT_STATE_NORMAL;
import static com.ashlikun.media.status.MediaStatus.CURRENT_STATE_PAUSE;
import static com.ashlikun.media.status.MediaStatus.CURRENT_STATE_PLAYING;
import static com.ashlikun.media.status.MediaStatus.CURRENT_STATE_PREPARING;

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

public class EasyVideoPlayTiny extends FrameLayout implements EasyBaseVideoPlay {
    public ViewGroup textureViewContainer;
    //数据源，列表
    public List<MediaData> mediaData;
    public int currentUrlIndex = 0;
    @MediaStatus.Code
    public int currentState = MediaStatus.CURRENT_STATE_NORMAL;//当前状态
    WindowManager mWindowManager = (WindowManager) getContext().getApplicationContext()
            .getSystemService(Context.WINDOW_SERVICE);
    WindowManager.LayoutParams mLayoutParams = new WindowManager.LayoutParams();

    private float mTouchStartX;
    private float mTouchStartY;
    private int mStatusHeight;

    public EasyVideoPlayTiny(@NonNull Context context) {
        this(context, null);
    }

    public EasyVideoPlayTiny(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {
        mStatusHeight = getStatusBarHeight();
        textureViewContainer = new FrameLayout(context);
        textureViewContainer.setBackgroundColor(0xff000000);
        LayoutParams containerLp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        containerLp.rightMargin = dip2px(10);
        containerLp.topMargin = dip2px(10);
        addView(textureViewContainer, containerLp);
        ImageView imageView = new ImageView(getContext());
        imageView.setImageResource(R.drawable.easy_media_click_back_tiny_selector);
        LayoutParams containerBack = new LayoutParams(dip2px(20), dip2px(20));
        containerBack.gravity = Gravity.TOP | Gravity.RIGHT;
        addView(imageView, containerBack);
        imageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaUtils.backPress();
            }
        });
    }

    /**
     * 设置数据源
     *
     * @param data 视频ur
     */
    public void setDataSource(MediaData data) {
        List<MediaData> mediaData = new ArrayList<>();
        mediaData.add(data);
        setDataSource(mediaData, 0);
    }


    /**
     * 设置数据源
     *
     * @param mediaData    视频数据，数组
     * @param defaultIndex 播放的url 位置 0 开始
     */
    public void setDataSource(List<MediaData> mediaData, int defaultIndex) {
        //是否有播放器，没用就用系统的
        if (EasyMediaManager.instance().mMediaPlay == null) {
            EasyMediaManager.instance().mMediaPlay = new EasyMediaSystem();
        }
        //过滤已经在播放的
        if (this.mediaData != null && mediaData.size() > defaultIndex &&
                getCurrentData().equals(MediaUtils.getCurrentMediaData(mediaData, defaultIndex))) {
            return;
        }
        if (isCurrentVideoPlay() && MediaUtils.isContainsUri(mediaData, EasyMediaManager.getCurrentDataSource())) {
            //当前View正在播放视频  保存进度
            int position = 0;
            try {
                position = EasyMediaManager.getCurrentPosition();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            if (position != 0) {
                MediaUtils.saveProgress(getContext(), EasyMediaManager.getCurrentDataSource(), position);
            }
            EasyMediaManager.instance().releaseMediaPlayer();
        }
        this.mediaData = mediaData;
        this.currentUrlIndex = defaultIndex;
        onStateNormal();
    }

    public void onStateNormal() {
        currentState = CURRENT_STATE_NORMAL;
    }

    //播放器生命周期,自己主动调用的,还原状态
    public void onCompletion() {
        //保存进度
        if (currentState == CURRENT_STATE_PLAYING || currentState == CURRENT_STATE_PAUSE) {
            int position = 0;
            try {
                position = EasyMediaManager.getCurrentPosition();
            } catch (IllegalStateException e) {
            }
            MediaUtils.saveProgress(getContext(), getCurrentData(), position);
        }
        cleanTiny();
        //还原默认状态
        onStateNormal();
        EasyMediaManager.instance().currentVideoWidth = 0;
        EasyMediaManager.instance().currentVideoHeight = 0;
        MediaUtils.setAudioFocus(getContext(), false);
        //取消休眠
        MediaUtils.getActivity(getContext()).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //释放渲染器和保存的SurfaceTexture，textureView
        EasyMediaManager.instance().releaseAllSufaceView();

    }

    public void onStatePrepared() {//因为这个紧接着就会进入播放状态，所以不设置state
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
        if (currentState == CURRENT_STATE_NORMAL) {
            if (MediaUtils.showWifiDialog(getContext(), getCurrentData(), this)) {
                return;
            }
            startVideo();
        } else if (currentState == CURRENT_STATE_PLAYING) {
            onEvent(EasyMediaAction.ON_CLICK_PAUSE);
            EasyMediaManager.pause();
            onStatePause();
        } else if (currentState == CURRENT_STATE_PAUSE) {
            onEvent(EasyMediaAction.ON_CLICK_RESUME);
            EasyMediaManager.start();
            onStatePlaying();
        } else if (currentState == CURRENT_STATE_AUTO_COMPLETE) {
            onEvent(EasyMediaAction.ON_CLICK_START_AUTO_COMPLETE);
            startVideo();
        }
    }

    public void onStatePreparing() {
        currentState = CURRENT_STATE_PREPARING;
    }

    //开始播放回掉
    public void onStatePlaying() {
        currentState = CURRENT_STATE_PLAYING;
    }

    //暂停
    public void onStatePause() {
        currentState = CURRENT_STATE_PAUSE;
    }

    //错误
    public void onStateError() {
        currentState = CURRENT_STATE_ERROR;
    }

    //自动完成
    public void onStateAutoComplete() {
        currentState = CURRENT_STATE_AUTO_COMPLETE;
    }

    @Override
    public void onPrepared() {
        onStatePrepared();
        onStatePlaying();
    }

    @Override
    public void onInfo(int what, int extra) {

    }

    @Override
    public void onSeekComplete() {

    }

    @Override
    public void onError(int what, int extra) {
        cleanTiny();
    }

    @Override
    public void onAutoCompletion() {
        cleanTiny();
    }

    @Override
    public void onVideoSizeChanged() {

    }

    @Override
    public void setBufferProgress(int bufferProgress) {

    }


    public List<MediaData> getMediaData() {
        return mediaData;
    }

    /**
     * 开始播放
     */
    public void startVideo() {
        //销毁其他播放的视频
        MediaUtils.releaseAllVideos();
        EasyMediaManager.instance().initTextureView(getContext());
        addTextureView();
        MediaUtils.setAudioFocus(getContext(), true);
        MediaUtils.getActivity(getContext()).getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        EasyMediaManager.setCurrentDataSource(getCurrentData());
        onStatePreparing();
        EasyVideoPlayerManager.setVideoTiny(this);
    }

    public void removeTextureView() {
        if (EasyMediaManager.textureView != null) {
            textureViewContainer.removeView(EasyMediaManager.textureView);
        }
    }

    public int getCurrentState() {
        return currentState;
    }

    public int getCurrentUrlIndex() {
        return currentUrlIndex;
    }

    public void onEvent(int type) {
        if (EasyMediaManager.MEDIA_EVENT != null && isCurrentPlay() && getCurrentData() != null) {
            EasyMediaManager.MEDIA_EVENT.onEvent(type, getCurrentData());
        }
    }

    /**
     * 当前EasyVideoPlay  是否正在播放
     */
    public boolean isCurrentPlay() {
        return isCurrentVideoPlay()
                && MediaUtils.isContainsUri(mediaData, EasyMediaManager.getCurrentDataSource());
    }

    //获取当前播放uil
    public MediaData getCurrentData() {
        return MediaUtils.getCurrentMediaData(mediaData, currentUrlIndex);
    }


    /**
     * 是否是当前EasyVideoPlay在播放视频
     */
    public boolean isCurrentVideoPlay() {
        return EasyVideoPlayerManager.getVideoTiny() != null
                && EasyVideoPlayerManager.getVideoTiny() == this;
    }

    /**
     * 添加TextureView
     */
    public void addTextureView() {
        FrameLayout.LayoutParams layoutParams =
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        Gravity.CENTER);
        textureViewContainer.addView(EasyMediaManager.textureView, layoutParams);
    }

    //清空小窗口
    public void cleanTiny() {
        if (mWindowManager != null) {
            mWindowManager.removeView(this);
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
        float rawY = event.getRawY() - mStatusHeight;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchStartX = event.getX();
                mTouchStartY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                //原始坐标减去移动坐标
                mLayoutParams.x = (int) (rawX - mTouchStartX);
                mLayoutParams.y = (int) (rawY - mTouchStartY);
                mWindowManager.updateViewLayout(this, mLayoutParams);
                break;
        }
        return super.onTouchEvent(event);
    }

    public int dip2px(float dipValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    //获取状态栏高度
    public int getStatusBarHeight() {
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            //根据资源ID获取响应的尺寸值
            return getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }
}
