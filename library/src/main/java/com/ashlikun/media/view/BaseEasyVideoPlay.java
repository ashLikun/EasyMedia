package com.ashlikun.media.view;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.ashlikun.media.EasyMediaAction;
import com.ashlikun.media.EasyMediaManager;
import com.ashlikun.media.EasyVideoPlayerManager;
import com.ashlikun.media.MediaData;
import com.ashlikun.media.MediaScreenUtils;
import com.ashlikun.media.MediaUtils;
import com.ashlikun.media.R;
import com.ashlikun.media.play.EasyMediaSystem;
import com.ashlikun.media.status.MediaDisplayType;
import com.ashlikun.media.status.MediaStatus;

import java.util.ArrayList;
import java.util.List;


/**
 * 作者　　: 李坤
 * 创建时间: 2018/8/20　13:13
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：播放器基础类
 * {@link #setDataSource} 去设置播放的数据源
 */
public abstract class BaseEasyVideoPlay extends FrameLayout implements IEasyVideoPlayListener {
    /**
     * Activity 全屏Flag
     */
    public static int ORIENTATION_FULLSCREEN_SENSOR = ActivityInfo.SCREEN_ORIENTATION_SENSOR;
    /**
     * 默认的activty的方向Flag
     */
    public static int ORIENTATION_NORMAL = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    /**
     * Activity 竖屏Flag
     */
    public static int ORIENTATION_FULLSCREEN_LANDSCAPE = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;

    /**
     * 当前状态
     */
    protected int currentState = MediaStatus.NORMAL;
    /**
     * 数据源，列表
     */
    protected List<MediaData> mediaData;
    /**
     * 当前播放到的列表数据源位置
     */
    protected int currentUrlIndex = 0;

    /**
     * 视频大小缩放类型
     */
    @MediaDisplayType.Code
    private int displayType = MediaDisplayType.ADAPTER;
    /**
     * 播放视频的渲染控件，一般为TextureView
     */
    public ViewGroup textureViewContainer;

    public BaseEasyVideoPlay(@NonNull Context context) {
        this(context, null);
    }

    public BaseEasyVideoPlay(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseEasyVideoPlay(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    protected void initView(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BaseEasyVideoPlay);
        if (!a.hasValue(0)) {
            setBackgroundColor(0xff000000);
        }
        displayType = a.getInt(R.styleable.BaseEasyVideoPlay_video_display_type, displayType);
        textureViewContainer = new FrameLayout(getContext());
        addView(textureViewContainer, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    /**
     * 设置数据源
     *
     * @param url   视频ur
     * @param title 标题
     */
    public void setDataSource(String url, String title) {
        List<MediaData> mediaData = new ArrayList<>();
        mediaData.add(new MediaData.Builder()
                .url(url)
                .title(title)
                .builder());
        setDataSource(mediaData, 0);
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
    public boolean setDataSource(List<MediaData> mediaData, int defaultIndex) {
        //是否有播放器，没用就用系统的
        if (EasyMediaManager.instance().mMediaPlay == null) {
            EasyMediaManager.instance().mMediaPlay = new EasyMediaSystem();
        }
        this.mediaData = mediaData;
        //如果这个已经在播放就不管
        if (getMediaData() != null && getMediaData().size() > defaultIndex &&
                MediaUtils.isContainsUri(getMediaData(),
                        EasyMediaManager.getCurrentDataSource())) {
            saveVideoPlayView();
            if (currentState == MediaStatus.NORMAL) {
                onStateNormal();
            }
            return false;
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
        } else if (!isCurrentVideoPlay() && MediaUtils.isContainsUri(mediaData, EasyMediaManager.getCurrentDataSource())) {
            if (EasyVideoPlayerManager.getVideoTiny() != null) {
                //需要退出小窗退到我这里，我这里是第一层级
                EasyVideoPlayerManager.getVideoTiny().cleanTiny();
            }
        }
        this.currentUrlIndex = defaultIndex;
        onStateNormal();
        return true;
    }

    /**
     * 保存播放器 用于全局管理
     * {@link EasyVideoPlayerManager#setVideoDefault)}
     * {@link EasyVideoPlayerManager#setVideoDefault)}
     * {@link EasyVideoPlayerManager#setVideoTiny}
     * 可能会多次调用
     */
    protected abstract void saveVideoPlayView();

    /**
     * 开始播放
     * 必须在设置完数据源后
     */
    public void startVideo() {
        //销毁其他播放的视频
        MediaUtils.releaseAllVideos();
        EasyMediaManager.instance().initTextureView(getContext(), displayType);
        addTextureView();
        MediaUtils.setAudioFocus(getContext(), true);
        MediaUtils.getActivity(getContext()).getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        EasyMediaManager.setCurrentDataSource(getCurrentData());
        onStatePreparing();
        saveVideoPlayView();
    }


    /**
     * 设置当前播放器状态
     *
     * @param status
     */
    public void setStatus(int status) {
        setStatus(status, 0, 0);
    }

    public void setStatus(int state, int currentUrlIndex, int seekToInAdvance) {
        switch (state) {
            case MediaStatus.NORMAL:
                onStateNormal();
                break;
            case MediaStatus.PREPARING:
                onStatePreparing();
                break;
            case MediaStatus.PREPARING_CHANGING_URL:
                onStatePreparingChangingUrl(currentUrlIndex, seekToInAdvance);
                break;
            case MediaStatus.PLAYING:
                onStatePlaying();
                break;
            case MediaStatus.PAUSE:
                onStatePause();
                break;
            case MediaStatus.ERROR:
                onStateError();
                break;
            case MediaStatus.AUTO_COMPLETE:
                onStateAutoComplete();
                break;
        }
    }

    public int getDisplayType() {
        return displayType;
    }

    /**
     * 设置播放器显示类型
     *
     * @param displayType
     */
    public void setDisplayType(int displayType) {
        this.displayType = displayType;
        if (EasyMediaManager.textureView != null && textureViewContainer.getChildAt(0) == EasyMediaManager.textureView) {
            EasyMediaManager.textureView.setDisplayType(displayType);
        }

    }
    /********************************************************************************************
     *                                       设置播放器状态后的回调
     ********************************************************************************************/
    /**
     * 设置当前初始状态
     */
    protected void onStateNormal() {
        currentState = MediaStatus.NORMAL;
    }

    /**
     * 当准备好了时候
     */
    protected void onStatePreparing() {
        currentState = MediaStatus.PREPARING;
    }

    protected void onStatePreparingChangingUrl(int currentUrlIndex, int seekToInAdvance) {
        currentState = MediaStatus.PREPARING_CHANGING_URL;
        this.currentUrlIndex = currentUrlIndex;
        EasyMediaManager.setCurrentDataSource(getCurrentData());
        EasyMediaManager.instance().prepare();
    }

    protected void onStatePrepared() {
        //因为这个紧接着就会进入播放状态，所以不设置state
    }

    /**
     * 开始播放回掉
     */
    protected void onStatePlaying() {
        currentState = MediaStatus.PLAYING;
    }

    /**
     * 暂停
     */
    protected void onStatePause() {
        currentState = MediaStatus.PAUSE;
    }

    /**
     * 错误
     */
    protected void onStateError() {
        currentState = MediaStatus.ERROR;
    }

    /**
     * 自动完成
     */
    protected void onStateAutoComplete() {
        currentState = MediaStatus.AUTO_COMPLETE;
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

    /**
     * 移除当前的渲染器
     */
    public void removeTextureView() {
        textureViewContainer.removeView(EasyMediaManager.textureView);
    }


    /**
     * 释放播放器,全屏下不能释放,先退出全屏再释放
     */
    public void release() {
        if (getCurrentData().equals(EasyMediaManager.getCurrentDataSource())) {
            //在非全屏的情况下只能backPress()
            if (isScreenFull()) {
                MediaScreenUtils.backPress();
            } else {
                MediaUtils.releaseAllVideos();
            }
        }
    }

    /********************************************************************************************
     *                                           播放器的生命周期，可以重写
     ********************************************************************************************/
    /**
     * 准备播放
     */
    @Override
    public void onPrepared() {
        onStatePrepared();
        onStatePlaying();
    }

    /**
     * 播放信息
     *
     * @param what  错误码
     * @param extra 扩展码
     */
    @Override
    public void onInfo(int what, int extra) {

    }

    /**
     * 设置进度完成
     */
    @Override
    public void onSeekComplete() {

    }

    /**
     * 播放错误
     *
     * @param what  错误码
     * @param extra 扩展码
     */
    @Override
    public void onError(int what, int extra) {
        if (what != 38 && what != -38 && extra != -38) {
            onStateError();
            if (isCurrentPlay()) {
                EasyMediaManager.instance().releaseMediaPlayer();
            }
        }
    }

    /**
     * 自动播放完成，播放器回调的
     */
    @Override
    public void onAutoCompletion() {
        onEvent(EasyMediaAction.ON_AUTO_COMPLETE);
        EasyMediaManager.instance().releaseMediaPlayer();
        Runtime.getRuntime().gc();
        MediaUtils.saveProgress(getContext(), getCurrentData(), 0);
    }

    /**
     * 播放器生命周期,自己主动调用的,还原状态
     */
    @Override
    public void onForceCompletionTo() {
        if (currentState == MediaStatus.PLAYING || currentState == MediaStatus.PAUSE) {
            int position = 0;
            try {
                position = EasyMediaManager.getCurrentPosition();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            MediaUtils.saveProgress(getContext(), getCurrentData(), position);
        }
        //还原默认状态
        onStateNormal();
        removeTextureView();
        EasyMediaManager.instance().currentVideoWidth = 0;
        EasyMediaManager.instance().currentVideoHeight = 0;
        //取消音频焦点
        MediaUtils.setAudioFocus(getContext(), false);
        //取消休眠
        MediaUtils.getActivity(getContext()).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //如果是全屏播放就清楚全屏的view
        if (isScreenFull()) {
            MediaScreenUtils.clearFullscreenLayout(getContext());
            MediaUtils.setRequestedOrientation(getContext(), ORIENTATION_NORMAL);
        }
        //释放渲染器和保存的SurfaceTexture，textureView
        EasyMediaManager.instance().releaseAllSufaceView();
    }

    /**
     * 是否是全屏播放
     *
     * @return
     */
    public abstract boolean isScreenFull();

    /**
     * 播放器大小改变
     */
    @Override
    public void onVideoSizeChanged() {
        if (EasyMediaManager.textureView != null) {
            EasyMediaManager.textureView.setVideoSize(EasyMediaManager.instance().currentVideoWidth, EasyMediaManager.instance().currentVideoHeight);
        }
    }

    /**
     * 缓存进度更新
     *
     * @param bufferProgress
     */
    @Override
    public void setBufferProgress(int bufferProgress) {

    }

    @Override
    public void onEvent(int type) {
        if (EasyMediaManager.MEDIA_EVENT != null && isCurrentPlay()) {
            EasyMediaManager.MEDIA_EVENT.onEvent(type);
        }
    }

    /********************************************************************************************
     *                                           下面这些都是获取属性和设置属性
     ********************************************************************************************/

    /**
     * 当前EasyVideoPlay  是否正在播放
     */
    public boolean isCurrentPlay() {
        //不仅正在播放的url不能一样，并且各个清晰度也不能一样
        return isCurrentVideoPlay()
                && MediaUtils.isContainsUri(mediaData, EasyMediaManager.getCurrentDataSource());
    }

    /**
     * 是否是当前EasyVideoPlay在播放视频
     */
    public boolean isCurrentVideoPlay() {
        return EasyVideoPlayerManager.getCurrentVideoPlayerNoTiny() != null
                && EasyVideoPlayerManager.getCurrentVideoPlayerNoTiny() == this;
    }

    /**
     * 当前播放到第几个视频，用于多视频播放，没有就返回0
     *
     * @return
     */
    public int getCurrentUrlIndex() {
        return currentUrlIndex;
    }


    /**
     * 获取播放器数据
     *
     * @return
     */
    public List<MediaData> getMediaData() {
        return mediaData;
    }

    /**
     * 获取当前播放uil
     *
     * @return
     */
    public MediaData getCurrentData() {
        return MediaUtils.getCurrentMediaData(mediaData, currentUrlIndex);
    }

    /**
     * 获取当前播放器状态
     *
     * @return {@link MediaStatus.Code}
     */
    @MediaStatus.Code
    public int getCurrentState() {
        return currentState;
    }

    /**
     * 直接设置播放状态
     *
     * @param currentState
     */
    public void setCurrentState(@MediaStatus.Code int currentState) {
        this.currentState = currentState;
    }
}
