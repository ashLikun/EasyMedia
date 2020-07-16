package com.ashlikun.media.video.view;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ashlikun.media.R;
import com.ashlikun.media.video.EasyMediaManager;
import com.ashlikun.media.video.EasyVideoAction;
import com.ashlikun.media.video.EasyVideoPlayerManager;
import com.ashlikun.media.video.NetworkUtils;
import com.ashlikun.media.video.VideoData;
import com.ashlikun.media.video.VideoScreenUtils;
import com.ashlikun.media.video.VideoUtils;
import com.ashlikun.media.video.status.VideoDisplayType;
import com.ashlikun.media.video.status.VideoStatus;

import java.util.ArrayList;
import java.util.List;

import static com.ashlikun.media.video.status.VideoStatus.AUTO_COMPLETE;
import static com.ashlikun.media.video.status.VideoStatus.NORMAL;
import static com.ashlikun.media.video.status.VideoStatus.PAUSE;
import static com.ashlikun.media.video.status.VideoStatus.PLAYING;


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
     * Activity 全屏Flag，重力感应(2个横屏，一个竖屏)
     */
    public static int ORIENTATION_FULLSCREEN_SENSOR = ActivityInfo.SCREEN_ORIENTATION_SENSOR;
    /**
     * Activity 全屏Flag，重力感应(2个横屏)
     */
    public static int ORIENTATION_FULLSCREEN_SENSOR_LANDSCAPE = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
    /**
     * 默认的activty的方向 Flag(竖屏)
     */
    public static int ORIENTATION_NORMAL = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    /**
     * Activity 竖屏Flag(1个横屏)
     */
    public static int ORIENTATION_FULLSCREEN_LANDSCAPE = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;

    /**
     * 当前状态
     */
    protected int currentState = VideoStatus.NORMAL;
    /**
     * 数据源，列表
     */
    protected List<VideoData> mediaData;
    /**
     * 当前播放到的列表数据源位置
     */
    protected int currentUrlIndex = 0;

    /**
     * 视频大小缩放类型
     */
    @VideoDisplayType.Code
    private int displayType = VideoDisplayType.ADAPTER;
    /**
     * 播放视频的渲染控件，一般为TextureView
     */
    public ViewGroup textureViewContainer;
    /**
     * 播放事件的回掉
     */
    private ArrayList<EasyVideoAction> videoActions;
    /**
     * 当onResume的时候是否去播放
     */
    private boolean ONRESUME_TO_PLAY = true;
    //从哪个开始播放
    protected long mSeekOnStart = -1;


    public BaseEasyVideoPlay(@NonNull Context context) {
        this(context, null);
    }

    public BaseEasyVideoPlay(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseEasyVideoPlay(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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
        List<VideoData> mediaData = new ArrayList<>();
        mediaData.add(new VideoData.Builder()
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
    public void setDataSource(VideoData data) {
        List<VideoData> mediaData = new ArrayList<>();
        mediaData.add(data);
        setDataSource(mediaData, 0);
    }

    /**
     * 设置数据源
     *
     * @param mediaData    视频数据，数组
     * @param defaultIndex 播放的url 位置 0 开始
     */
    public boolean setDataSource(List<VideoData> mediaData, int defaultIndex) {
        this.mediaData = mediaData;
        //如果这个已经在播放就不管
        if (getMediaData() != null && defaultIndex >= 0 && getMediaData().size() > defaultIndex &&
                VideoUtils.isContainsUri(getMediaData(),
                        EasyMediaManager.getCurrentDataSource())) {
            saveVideoPlayView();
            if (currentState == VideoStatus.NORMAL) {
                setStatus(VideoStatus.NORMAL);
            }
            return false;
        }
        if (isCurrentVideoPlay() && VideoUtils.isContainsUri(mediaData, EasyMediaManager.getCurrentDataSource())) {
            //当前View正在播放视频  保存进度
            int position = 0;
            try {
                position = EasyMediaManager.getCurrentPosition();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            if (position != 0) {
                VideoUtils.saveProgress(getContext(), EasyMediaManager.getCurrentDataSource(), position);
            }
            EasyMediaManager.getInstance().releaseMediaPlayer();
        } else if (!isCurrentVideoPlay() && VideoUtils.isContainsUri(mediaData, EasyMediaManager.getCurrentDataSource())) {
            if (EasyVideoPlayerManager.getVideoTiny() != null) {
                //需要退出小窗退到我这里，我这里是第一层级
                EasyVideoPlayerManager.getVideoTiny().cleanTiny();
            }
        }
        this.currentUrlIndex = defaultIndex;
        setStatus(VideoStatus.NORMAL);
        return true;
    }

    /**
     * 保存播放器 用于全局管理
     * {@link EasyVideoPlayerManager#setVideoDefault)}
     * {@link EasyVideoPlayerManager#setVideoDefault)}
     * {@link EasyVideoPlayerManager#setVideoTiny}
     * 可能会多次调用
     */
    public abstract void saveVideoPlayView();

    /**
     * 开始播放
     * 必须在设置完数据源后
     */
    public void startVideo() {
        if (VideoUtils.showWifiDialog(getContext(), getCurrentData(), this) && currentState == VideoStatus.NORMAL) {
            return;
        }
        //销毁其他播放的视频
        VideoUtils.releaseAllVideos();
        EasyMediaManager.getInstance().removeTextureView();
        addTextureView();
        //在这里添加网络状态监听

        VideoUtils.setAudioFocus(getContext(), true);
        VideoUtils.getActivity(getContext()).getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        EasyMediaManager.setCurrentDataSource(getCurrentData());
        saveVideoPlayView();
        setStatus(VideoStatus.PREPARING);

    }


    /**
     * 设置当前播放器状态
     */
    public void setStatus(int state) {
        if (currentState == state) {
            return;
        }
        VideoUtils.d(String.valueOf(state));
        switch (state) {
            case VideoStatus.NORMAL:
                onStateNormal();
                onEvent(EasyVideoAction.ON_STATUS_NORMAL);
                break;
            case VideoStatus.PREPARING:
                onStatePreparing();
                onEvent(EasyVideoAction.ON_STATUS_PREPARING);
                break;
            case VideoStatus.PLAYING:
                onStatePlaying();
                onEvent(EasyVideoAction.ON_STATUS_PLAYING);
                break;
            case VideoStatus.PAUSE:
                onStatePause();
                onEvent(EasyVideoAction.ON_STATUS_PAUSE);
                break;
            case VideoStatus.ERROR:
                onStateError();
                onEvent(EasyVideoAction.ON_STATUS_ERROR);
                break;
            case VideoStatus.AUTO_COMPLETE:
                onStateAutoComplete();
                onEvent(EasyVideoAction.ON_STATUS_AUTO_COMPLETE);
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
        if (EasyMediaManager.getTextureView() != null && textureViewContainer.getChildAt(0) == EasyMediaManager.getTextureView()) {
            EasyMediaManager.getTextureView().setDisplayType(displayType);
        }

    }
    /********************************************************************************************
     *                                       设置播放器状态后的回调
     ********************************************************************************************/
    /**
     * 设置当前初始状态
     */
    protected void onStateNormal() {
        currentState = VideoStatus.NORMAL;
    }

    /**
     * 当准备时候
     */
    protected void onStatePreparing() {
        currentState = VideoStatus.PREPARING;
    }


    protected void onStatePrepared() {
        //因为这个紧接着就会进入播放状态，所以不设置state
    }

    /**
     * 开始播放回掉
     */
    protected void onStatePlaying() {
        EasyMediaManager.getInstance().getMediaPlay().setPreparedPause(false);
        currentState = VideoStatus.PLAYING;
        EasyMediaManager.start();
    }

    /**
     * 暂停
     */
    protected void onStatePause() {
        currentState = VideoStatus.PAUSE;
        EasyMediaManager.pause();
    }

    /**
     * 错误
     */
    protected void onStateError() {
        EasyMediaManager.getInstance().getMediaPlay().setPreparedPause(false);
        currentState = VideoStatus.ERROR;
    }

    /**
     * 自动完成
     */
    protected void onStateAutoComplete() {
        currentState = VideoStatus.AUTO_COMPLETE;
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
        if (EasyMediaManager.getTextureView() == null || EasyMediaManager.getTextureView().getContext() != getContext()) {
            //根据新的newVideoPlay 创建新的EasyTextureView View 防止内存泄漏
            EasyMediaManager.getInstance().initTextureView(getContext(), getDisplayType());
        }
        if (EasyMediaManager.getTextureView().getParent() != null) {
            ((ViewGroup) (EasyMediaManager.getTextureView().getParent())).removeView(EasyMediaManager.getTextureView());
        }
        textureViewContainer.addView(EasyMediaManager.getTextureView(), layoutParams);
    }

    /**
     * 移除当前的渲染器
     */
    public void removeTextureView() {
        textureViewContainer.removeView(EasyMediaManager.getTextureView());
    }


    /**
     * 释放播放器,全屏下不能释放,先退出全屏再释放
     */
    public void release() {
        if (getCurrentData().equals(EasyMediaManager.getCurrentDataSource())) {
            //在非全屏的情况下只能backPress()
            if (isScreenFull()) {
                VideoScreenUtils.backPress();
            } else {
                VideoUtils.releaseAllVideos();
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
        setStatus(VideoStatus.PLAYING);
        if (mSeekOnStart > 0) {
            EasyMediaManager.seekTo(mSeekOnStart);
            mSeekOnStart = 0;
        }
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
        String netSate;
        //切换网络引起的
        if (what == -10000 && NetworkUtils.isConnected(getContext()) &&
                !TextUtils.equals(EasyMediaManager.getInstance().mNetSate, netSate = NetworkUtils.getNetWorkTypeName(getContext()))) {
            EasyMediaManager.getInstance().mNetSate = netSate;
            int position = getCurrentPositionWhenPlaying();
            EasyMediaManager.getCurrentPosition();
            EasyMediaManager.getInstance().releaseMediaPlayer();
            setSeekOnStart(position);
            //重新播放
            startVideo();
            return;
        }
        if (what != 38 && what != -38 && extra != -38) {
            setStatus(VideoStatus.ERROR);
            if (isCurrentPlay()) {
                EasyMediaManager.getInstance().releaseMediaPlayer();
            }
        }
    }

    /**
     * 自动播放完成，播放器回调的
     */
    @Override
    public void onAutoCompletion() {
        onEvent(EasyVideoAction.ON_AUTO_COMPLETE);
        EasyMediaManager.getInstance().releaseMediaPlayer();
        Runtime.getRuntime().gc();
        VideoUtils.saveProgress(getContext(), getCurrentData(), 0);
    }

    /**
     * 播放器生命周期,自己主动调用的,还原状态
     */
    @Override
    public void onForceCompletionTo() {
        if (currentState == VideoStatus.PLAYING || currentState == VideoStatus.PAUSE) {
            int position = 0;
            try {
                position = EasyMediaManager.getCurrentPosition();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            VideoUtils.saveProgress(getContext(), getCurrentData(), position);
        }
        //还原默认状态
        setStatus(VideoStatus.NORMAL);
        removeTextureView();
        EasyMediaManager.getInstance().currentVideoWidth = 0;
        EasyMediaManager.getInstance().currentVideoHeight = 0;
        //取消音频焦点
        VideoUtils.setAudioFocus(getContext(), false);
        //取消休眠
        VideoUtils.getActivity(getContext()).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //如果是全屏播放就清楚全屏的view
        if (isScreenFull()) {
            VideoScreenUtils.clearFullscreenLayout(getContext());
            VideoUtils.setRequestedOrientation(getContext(), ORIENTATION_NORMAL);
        }
        //释放渲染器和保存的SurfaceTexture，textureView
        EasyMediaManager.getInstance().releaseAllSufaceView();
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
        if (EasyMediaManager.getTextureView() != null) {
            EasyMediaManager.getTextureView().setVideoSize(EasyMediaManager.getInstance().currentVideoWidth, EasyMediaManager.getInstance().currentVideoHeight);
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

    /**
     * 播放事件的回掉
     *
     * @param type {@link EasyVideoAction}
     */
    @Override
    public void onEvent(int type) {
        VideoUtils.d(String.valueOf(type));
        if (isCurrentPlay()) {
            //本实例的回调
            if (videoActions != null) {
                for (EasyVideoAction action : videoActions) {
                    action.onEvent(type);
                }
            }
            //全局的地方回调
            if (EasyMediaManager.getInstance().MEDIA_EVENT != null) {
                EasyMediaManager.getInstance().MEDIA_EVENT.onEvent(type);
            }
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
                && VideoUtils.isContainsUri(mediaData, EasyMediaManager.getCurrentDataSource());
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
    public List<VideoData> getMediaData() {
        return mediaData;
    }

    /**
     * 获取当前播放uil
     *
     * @return
     */
    public VideoData getCurrentData() {
        return VideoUtils.getCurrentMediaData(mediaData, currentUrlIndex);
    }

    /**
     * 获取当前播放器状态
     *
     * @return {@link VideoStatus.Code}
     */
    @VideoStatus.Code
    public int getCurrentState() {
        return currentState;
    }

    /**
     * 直接设置播放状态
     *
     * @param currentState
     */
    public void setCurrentState(@VideoStatus.Code int currentState) {
        this.currentState = currentState;
    }

    /**
     * 从哪里开始播放
     * 目前有时候前几秒有跳动问题，毫秒
     * 需要在startPlayLogic之前，即播放开始之前
     */
    public void setSeekOnStart(long seekOnStart) {
        this.mSeekOnStart = seekOnStart;
    }

    /**
     * 移除播放事件的回掉
     *
     * @param action
     */
    public boolean removeVideoAction(EasyVideoAction action) {
        if (videoActions != null && action != null) {
            return videoActions.remove(action);
        }
        return false;
    }

    /**
     * 添加播放事件的回掉
     *
     * @param action
     */
    public void addVideoAction(EasyVideoAction action) {
        if (action != null) {
            if (videoActions == null) {
                videoActions = new ArrayList<>();
            }
            if (!videoActions.contains(action)) {
                videoActions.add(action);
            }
        }

    }

    /**
     * 获取当前播放位置
     *
     * @return
     */
    public int getCurrentPositionWhenPlaying() {
        int position = 0;
        if (currentState == PLAYING ||
                currentState == PAUSE) {
            try {
                position = EasyMediaManager.getCurrentPosition();
            } catch (IllegalStateException e) {
                e.printStackTrace();
                return position;
            }
        }
        return position;
    }

    /**
     * 对应activity得生命周期
     */
    public void onPause() {
        if (getCurrentState() == AUTO_COMPLETE ||
                getCurrentState() == NORMAL) {
            VideoUtils.releaseAllVideos();
        } else {
            if (getCurrentState() == VideoStatus.PLAYING) {
                ONRESUME_TO_PLAY = true;
            } else {
                ONRESUME_TO_PLAY = false;
            }
            if (getCurrentState() == VideoStatus.PLAYING) {
                setStatus(VideoStatus.PAUSE);
            } else if (currentState == VideoStatus.PREPARING) {
                EasyMediaManager.getInstance().getMediaPlay().setPreparedPause(true);
            }
        }
    }


    /**
     * 对应activity得生命周期
     */
    public void onResume() {
        if (getCurrentState() == PAUSE && ONRESUME_TO_PLAY) {
            setStatus(VideoStatus.PLAYING);
        }
    }


    /**
     * 对应activity得生命周期
     */
    public void onDestroy() {
        VideoUtils.onDestroy();
    }
}
