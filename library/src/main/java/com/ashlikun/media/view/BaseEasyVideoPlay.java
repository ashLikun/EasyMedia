package com.ashlikun.media.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.ashlikun.media.EasyMediaManager;
import com.ashlikun.media.EasyVideoPlayerManager;
import com.ashlikun.media.MediaData;
import com.ashlikun.media.MediaScreenUtils;
import com.ashlikun.media.MediaUtils;
import com.ashlikun.media.play.EasyMediaSystem;
import com.ashlikun.media.status.MediaScreenStatus;
import com.ashlikun.media.status.MediaStatus;

import java.util.ArrayList;
import java.util.List;

import static com.ashlikun.media.status.MediaStatus.CURRENT_STATE_AUTO_COMPLETE;
import static com.ashlikun.media.status.MediaStatus.CURRENT_STATE_ERROR;
import static com.ashlikun.media.status.MediaStatus.CURRENT_STATE_NORMAL;
import static com.ashlikun.media.status.MediaStatus.CURRENT_STATE_PAUSE;
import static com.ashlikun.media.status.MediaStatus.CURRENT_STATE_PLAYING;
import static com.ashlikun.media.status.MediaStatus.CURRENT_STATE_PREPARING;
import static com.ashlikun.media.status.MediaStatus.CURRENT_STATE_PREPARING_CHANGING_URL;

/**
 * 作者　　: 李坤
 * 创建时间: 2018/8/20　13:13
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：播放器基础类
 */
public abstract class BaseEasyVideoPlay extends FrameLayout implements IEasyVideoPlayListener {
    /**
     * 当前状态
     */
    protected int currentState = MediaStatus.CURRENT_STATE_NORMAL;
    /**
     * 数据源，列表
     */
    protected List<MediaData> mediaData;
    /**
     * 当前播放到的列表数据源位置
     */
    protected int currentUrlIndex = 0;

    /**
     * 当前屏幕方向
     */
    @MediaScreenStatus.Code
    protected int currentScreen;

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
    public void setDataSource(List<MediaData> mediaData, int defaultIndex) {
        //是否有播放器，没用就用系统的
        if (EasyMediaManager.instance().mMediaPlay == null) {
            EasyMediaManager.instance().mMediaPlay = new EasyMediaSystem();
        }
        this.mediaData = mediaData;
        this.currentUrlIndex = defaultIndex;
        //过滤已经在播放的
        if (this.mediaData != null && mediaData.size() > defaultIndex &&
                MediaUtils.isContainsUri(getMediaData(),
                        EasyMediaManager.getCurrentDataSource())) {
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
        } else if (!isCurrentVideoPlay() && MediaUtils.isContainsUri(mediaData, EasyMediaManager.getCurrentDataSource())) {
            if (EasyVideoPlayerManager.getVideoTiny() != null) {
                //需要退出小窗退到我这里，我这里是第一层级
                EasyVideoPlayerManager.getVideoTiny().cleanTiny();
            }
        }
        onStateNormal();
    }

    /**
     * 开始播放
     * 必须在设置完数据源后
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
            case CURRENT_STATE_NORMAL:
                onStateNormal();
                break;
            case CURRENT_STATE_PREPARING:
                onStatePreparing();
                break;
            case CURRENT_STATE_PREPARING_CHANGING_URL:
                onStatePreparingChangingUrl(currentUrlIndex, seekToInAdvance);
                break;
            case CURRENT_STATE_PLAYING:
                onStatePlaying();
                break;
            case CURRENT_STATE_PAUSE:
                onStatePause();
                break;
            case CURRENT_STATE_ERROR:
                onStateError();
                break;
            case CURRENT_STATE_AUTO_COMPLETE:
                onStateAutoComplete();
                break;
        }
    }

    /**
     * 设置当前初始状态
     */
    protected void onStateNormal() {
        currentState = CURRENT_STATE_NORMAL;
    }

    /**
     * 当准备好了时候
     */
    protected void onStatePreparing() {
        currentState = CURRENT_STATE_PREPARING;
    }

    protected void onStatePreparingChangingUrl(int currentUrlIndex, int seekToInAdvance) {
        currentState = CURRENT_STATE_PREPARING_CHANGING_URL;
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
        currentState = CURRENT_STATE_PLAYING;
    }

    /**
     * 暂停
     */
    protected void onStatePause() {
        currentState = CURRENT_STATE_PAUSE;
    }

    /**
     * 错误
     */
    protected void onStateError() {
        currentState = CURRENT_STATE_ERROR;
    }

    /**
     * 自动完成
     */
    protected void onStateAutoComplete() {
        currentState = CURRENT_STATE_AUTO_COMPLETE;
    }

    /**
     * 添加TextureView
     */
    protected void addTextureView() {
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
     * 获取当前播放uil
     *
     * @return
     */
    public MediaData getCurrentData() {
        return MediaUtils.getCurrentMediaData(mediaData, currentUrlIndex);
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
     * 释放播放器,全屏下不能释放,先退出全屏再释放
     */
    public void release() {
        if (getCurrentData().equals(EasyMediaManager.getCurrentDataSource()) && MediaScreenUtils.isBackOk()) {
            //在非全屏的情况下只能backPress()
            if (isScreenFull()) {
                MediaScreenUtils.backPress();
            } else {
                MediaUtils.releaseAllVideos();
            }
        }
    }

    @Override
    public void onPrepared() {

    }

    @Override
    public void onInfo(int what, int extra) {

    }

    @Override
    public void onSeekComplete() {

    }

    @Override
    public void onError(int what, int extra) {

    }

    @Override
    public void onAutoCompletion() {

    }

    @Override
    public void onForceCompletionTo() {

    }

    @Override
    public void onVideoSizeChanged() {

    }

    @Override
    public void setBufferProgress(int bufferProgress) {

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

    @Override
    public void onEvent(int type) {
        if (EasyMediaManager.MEDIA_EVENT != null && isCurrentPlay() && getCurrentData() != null) {
            EasyMediaManager.MEDIA_EVENT.onEvent(type);
        }
    }

    /**
     * 获取播放器类型
     *
     * @return 1:默认的，2全屏的，3：小窗口
     */
    @MediaScreenStatus.Code
    public int getCurrentScreen() {
        return currentScreen;
    }

    /**
     * 是否是全屏的
     *
     * @return
     */
    public boolean isScreenFull() {
        return currentScreen == MediaScreenStatus.SCREEN_WINDOW_FULLSCREEN;
    }

    /**
     * 是否是默认的
     *
     * @return
     */
    public boolean isScreenNormal() {
        return currentScreen == MediaScreenStatus.SCREEN_WINDOW_NORMAL;
    }

    /**
     * 是否是列表的
     *
     * @return
     */
    public boolean isScreenList() {
        return currentScreen == MediaScreenStatus.SCREEN_WINDOW_LIST;
    }

    /**
     * 是否是小窗口的
     *
     * @return
     */
    public boolean isScreenTiny() {
        return currentScreen == MediaScreenStatus.SCREEN_WINDOW_TINY;
    }

    /**
     * 设置当前屏幕，默认的和列表或者小窗口
     * 请在setDataSource之前设置
     *
     * @param currentScreen
     */
    public void setCurrentScreen(int currentScreen) {
        this.currentScreen = currentScreen;
    }
}
