package com.ashlikun.media.view;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.TypedArray;
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
import com.ashlikun.media.controller.EasyMediaController;
import com.ashlikun.media.controller.MediaControllerInterface;
import com.ashlikun.media.play.EasyMediaSystem;
import com.ashlikun.media.status.MediaDisplayType;
import com.ashlikun.media.status.MediaScreenStatus;
import com.ashlikun.media.status.MediaStatus;

import java.util.ArrayList;
import java.util.List;

import static com.ashlikun.media.status.MediaScreenStatus.SCREEN_WINDOW_FULLSCREEN;
import static com.ashlikun.media.status.MediaStatus.CURRENT_STATE_AUTO_COMPLETE;
import static com.ashlikun.media.status.MediaStatus.CURRENT_STATE_ERROR;
import static com.ashlikun.media.status.MediaStatus.CURRENT_STATE_NORMAL;
import static com.ashlikun.media.status.MediaStatus.CURRENT_STATE_PAUSE;
import static com.ashlikun.media.status.MediaStatus.CURRENT_STATE_PLAYING;
import static com.ashlikun.media.status.MediaStatus.CURRENT_STATE_PREPARING;
import static com.ashlikun.media.status.MediaStatus.CURRENT_STATE_PREPARING_CHANGING_URL;

/**
 * 作者　　: 李坤
 * 创建时间: 2017/11/24 17:28
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：播放器view，负责视频的播放
 */
public class EasyVideoPlayer extends FrameLayout
        implements EasyOnControllEvent, EasyBaseVideoPlay {
    public static int ORIENTATION_FULLSCREEN_SENSOR = ActivityInfo.SCREEN_ORIENTATION_SENSOR;
    public static int ORIENTATION_NORMAL = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    public static int ORIENTATION_FULLSCREEN_LANDSCAPE = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
    //是否保存进度
    public static boolean SAVE_PROGRESS = true;

    @MediaDisplayType.Code
    public static int VIDEO_IMAGE_DISPLAY_TYPE = 0;
    public static long lastAutoFullscreenTime = 0;

    @MediaScreenStatus.Code
    public int currentScreen = MediaScreenStatus.SCREEN_WINDOW_NORMAL;//当前屏幕方向
    @MediaStatus.Code
    public int currentState = MediaStatus.CURRENT_STATE_NORMAL;//当前状态
    public int seekToInAdvance = 0;
    public int widthRatio = 0;
    public int heightRatio = 0;
    //数据源，列表
    public List<MediaData> mediaData;
    public int currentUrlIndex = 0;
    public int videoRotation = 0;
    public ViewGroup textureViewContainer;
    //全屏后是否可以竖屏
    protected boolean mFullscreenPortrait = true;
    //是否可以全屏
    protected boolean mFullscreenEnable = true;

    //播放器控制器
    MediaControllerInterface mediaController;

    public EasyVideoPlayer(Context context) {
        this(context, null);
    }

    public EasyVideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public void init(Context context, AttributeSet attrs) {

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.EasyVideoPlayer);
        if (!a.hasValue(0)) {
            setBackgroundColor(0xff000000);
        }
        mFullscreenEnable = a.getBoolean(R.styleable.EasyVideoPlayer_evp_full_screen_enable, mFullscreenEnable);
        a.recycle();
        textureViewContainer = new FrameLayout(getContext());
        addView(textureViewContainer, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        initController(getController());
        try {
            if (isCurrentPlay()) {
                ORIENTATION_NORMAL = MediaUtils.getActivity(context).getRequestedOrientation();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //子类可以实现从写
    public MediaControllerInterface getController() {
        return new EasyMediaController(getContext());
    }

    //初始化控制器
    public final void initController(MediaControllerInterface controller) {
        if (this.mediaController != null) {
            removeView((View) mediaController);
        }
        this.mediaController = controller;
        addView((View) mediaController);
        mediaController.setOnControllEvent(this);
        mediaController.setControllFullEnable(mFullscreenEnable);
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
        } else if (!isCurrentVideoPlay() && MediaUtils.isContainsUri(mediaData, EasyMediaManager.getCurrentDataSource())) {
            if (EasyVideoPlayerManager.getVideoTiny() != null) {
                //需要退出小窗退到我这里，我这里是第一层级
                EasyVideoPlayerManager.getVideoTiny().cleanTiny();
            }
        }
        this.mediaData = mediaData;
        this.currentUrlIndex = defaultIndex;
        mediaController.setCurrentScreen(currentScreen);
        mediaController.setDataSource(mediaData.get(currentUrlIndex));
        onStateNormal();
    }

    /**
     * 开始播放
     */
    public void startVideo() {
        //销毁其他播放的视频
        MediaUtils.releaseAllVideos();
        initTextureView();
        addTextureView();
        MediaUtils.setAudioFocus(getContext(), true);
        MediaUtils.getActivity(getContext()).getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        EasyMediaManager.setCurrentDataSource(getCurrentData());
        onStatePreparing();
        if (currentScreen == MediaScreenStatus.SCREEN_WINDOW_FULLSCREEN) {
            EasyVideoPlayerManager.setVideoFullscreen(this);
        } else {
            EasyVideoPlayerManager.setVideoDefault(this);
        }

    }

    /**
     * 当控制器播放按钮点击后
     */
    @Override
    public void onPlayStartClick() {
        if (mediaData == null || getCurrentData() == null) {
            Toast.makeText(getContext(), getResources().getString(R.string.no_url), Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentState == CURRENT_STATE_NORMAL) {
            if (MediaUtils.showWifiDialog(getContext(), getCurrentData(), this)) {
                return;
            }
            startVideo();
            onEvent(EasyMediaAction.ON_CLICK_START_ICON);
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

    /**
     * 当控制器从新播放点击
     */
    @Override
    public void onRetryClick() {
        if (MediaUtils.showWifiDialog(getContext(), getCurrentData(), this)) {
            onEvent(EasyMediaAction.ON_CLICK_START_ICON);
            return;
        }
        startVideo();
        onEvent(EasyMediaAction.ON_CLICK_START_ERROR);
    }

    /**
     * 控制器全屏点击
     */
    @Override
    public void onFullscreenClick() {
        if (currentState == CURRENT_STATE_AUTO_COMPLETE) {
            return;
        }
        if (currentScreen == SCREEN_WINDOW_FULLSCREEN) {
            //退出全屏
            MediaUtils.backPress();
        } else {
            onEvent(EasyMediaAction.ON_ENTER_FULLSCREEN);
            startWindowFullscreen();
        }
    }

    /**
     * 当控制器点击的时候
     */
    @Override
    public void onControllerClick() {
        if (currentState == CURRENT_STATE_ERROR) {
            startVideo();
        } else {
            mediaController.startDismissControlViewSchedule();
        }
    }


    public void setState(int state) {
        setState(state, 0, 0);
    }

    public void setState(int state, int urlMapIndex, int seekToInAdvance) {
        switch (state) {
            case CURRENT_STATE_NORMAL:
                onStateNormal();
                break;
            case CURRENT_STATE_PREPARING:
                onStatePreparing();
                break;
            case CURRENT_STATE_PREPARING_CHANGING_URL:
                onStatePreparingChangingUrl(urlMapIndex, seekToInAdvance);
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

    public void onStateNormal() {
        currentState = CURRENT_STATE_NORMAL;
        mediaController.setCurrentState(currentState);
    }


    public void onStatePreparing() {
        currentState = CURRENT_STATE_PREPARING;
        mediaController.setCurrentState(currentState);
    }

    public void onStatePreparingChangingUrl(int urlMapIndex, int seekToInAdvance) {
        currentState = CURRENT_STATE_PREPARING_CHANGING_URL;
        mediaController.setCurrentState(currentState);
        this.currentUrlIndex = urlMapIndex;
        this.seekToInAdvance = seekToInAdvance;
        EasyMediaManager.setCurrentDataSource(getCurrentData());
        EasyMediaManager.instance().prepare();
    }

    public void onStatePrepared() {//因为这个紧接着就会进入播放状态，所以不设置state
        if (seekToInAdvance != 0) {
            EasyMediaManager.seekTo(seekToInAdvance);
            seekToInAdvance = 0;
        } else {
            int position = MediaUtils.getSavedProgress(getContext(), getCurrentData());
            if (position != 0) {
                EasyMediaManager.seekTo(position);
            }
        }
    }

    //开始播放回掉
    public void onStatePlaying() {
        currentState = CURRENT_STATE_PLAYING;
        mediaController.setCurrentState(currentState);
    }

    //暂停
    public void onStatePause() {
        currentState = CURRENT_STATE_PAUSE;
        mediaController.setCurrentState(currentState);
    }

    //错误
    public void onStateError() {
        currentState = CURRENT_STATE_ERROR;
        mediaController.setCurrentState(currentState);
    }

    //自动完成
    public void onStateAutoComplete() {
        currentState = CURRENT_STATE_AUTO_COMPLETE;
        mediaController.setCurrentState(currentState);
        mediaController.setMaxProgressAndTime();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (currentScreen == SCREEN_WINDOW_FULLSCREEN) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        if (widthRatio != 0 && heightRatio != 0) {
            int specWidth = MeasureSpec.getSize(widthMeasureSpec);
            int specHeight = (int) ((specWidth * (float) heightRatio) / widthRatio);
            if (specHeight > specWidth / 5 * 4) {
                //高度最大值限制
                specHeight = specWidth / 5 * 4;
            }
            setMeasuredDimension(specWidth, specHeight);

            int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(specWidth, MeasureSpec.EXACTLY);
            int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(specHeight, MeasureSpec.EXACTLY);
            textureViewContainer.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            ((View) mediaController).measure(childWidthMeasureSpec, childHeightMeasureSpec);
            if (getThumbImageView() != null) {
                getThumbImageView().measure(childWidthMeasureSpec, childHeightMeasureSpec);
            }
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }

    }

    //播放器生命周期
    @Override
    public void onPrepared() {
        onStatePrepared();
        onStatePlaying();
    }

    //播放器生命周期
    @Override
    public void onInfo(int what, int extra) {

    }

    //播放器生命周期
    @Override
    public void onSeekComplete() {

    }

    //播放器生命周期
    @Override
    public void onError(int what, int extra) {
        if (what != 38 && what != -38 && extra != -38) {
            onStateError();
            if (isCurrentPlay()) {
                EasyMediaManager.instance().releaseMediaPlayer();
            }
        }
    }

    //播放器生命周期
    @Override
    public void setBufferProgress(int bufferProgress) {
        mediaController.setBufferProgress(bufferProgress);
    }

    //播放器生命周期
    @Override
    public void onAutoCompletion() {
        Runtime.getRuntime().gc();
        onEvent(EasyMediaAction.ON_AUTO_COMPLETE);
        mediaController.onAutoCompletion();
        onStateAutoComplete();
        if (currentScreen == SCREEN_WINDOW_FULLSCREEN) {
            MediaUtils.backPress();
        }
        EasyMediaManager.instance().releaseMediaPlayer();
        MediaUtils.saveProgress(getContext(), getCurrentData(), 0);
    }

    //播放器生命周期
    @Override
    public void onVideoSizeChanged() {
        if (EasyMediaManager.textureView != null) {
            if (videoRotation != 0) {
                EasyMediaManager.textureView.setRotation(videoRotation);
            }
            EasyMediaManager.textureView.setVideoSize(EasyMediaManager.instance().currentVideoWidth, EasyMediaManager.instance().currentVideoHeight);
        }
    }

    public List<MediaData> getMediaData() {
        return mediaData;
    }

    //播放器生命周期,自己主动调用的,还原状态
    public void onCompletion() {
        if (currentState == CURRENT_STATE_PLAYING || currentState == CURRENT_STATE_PAUSE) {
            int position = mediaController.getCurrentPositionWhenPlaying();
            MediaUtils.saveProgress(getContext(), getCurrentData(), position);
        }
        onStateNormal();
        textureViewContainer.removeView(EasyMediaManager.textureView);
        EasyMediaManager.instance().currentVideoWidth = 0;
        EasyMediaManager.instance().currentVideoHeight = 0;
        MediaUtils.setAudioFocus(getContext(), false);
        //取消休眠
        MediaUtils.getActivity(getContext()).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //如果是全屏播放就清楚全屏的view
        if (currentScreen == SCREEN_WINDOW_FULLSCREEN) {
            clearFullscreenLayout();
            MediaUtils.setRequestedOrientation(getContext(), ORIENTATION_NORMAL);
        }
        //释放渲染器和保存的SurfaceTexture，textureView
        EasyMediaManager.instance().releaseAllSufaceView();
    }


    //是否是全屏
    public boolean isFullscreen() {
        return currentScreen == SCREEN_WINDOW_FULLSCREEN;
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

    /**
     * 释放播放器,全屏下不能释放,先退出全屏再释放
     */
    public void release() {
        if (getCurrentData().equals(EasyMediaManager.getCurrentDataSource()) &&
                (System.currentTimeMillis() - MediaUtils.CLICK_QUIT_FULLSCREEN_TIME) > MediaUtils.FULL_SCREEN_NORMAL_DELAY) {
            //在非全屏的情况下只能backPress()
            if (isFullscreen()) {
                MediaUtils.backPress();
            } else {
                MediaUtils.releaseAllVideos();
            }
        }
    }

    /**
     * 初始化TextureView
     */
    public void initTextureView() {
        EasyMediaManager.instance().removeTextureView();
        EasyMediaManager.textureView = new EasyTextureView(getContext());
        EasyMediaManager.textureView.setSurfaceTextureListener(EasyMediaManager.instance());
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
     * 从当前Activity里面清除全屏View
     */
    public void clearFullscreenLayout() {
        ViewGroup vp = MediaUtils.getDecorView(getContext());
        View oldF = vp.findViewById(R.id.easy_media_fullscreen_id);
        if (oldF != null) {
            vp.removeView(oldF);
        }
        MediaUtils.setActivityFullscreen(getContext(), false);
    }


    /**
     * 开始全屏播放
     * 在当前activity的跟布局加一个新的最大化的EasyVideoPlayer
     * 再把activity设置成全屏，
     */
    public void startWindowFullscreen() {
        MediaUtils.CLICK_QUIT_FULLSCREEN_TIME = System.currentTimeMillis();
        removeTextureView();
        EasyVideoPlayer fullPlay = new EasyVideoPlayer(getContext());
        fullPlay.setFullscreenPortrait(mFullscreenPortrait);
        fullPlay.setState(currentState);
        fullPlay.addTextureView();
        fullPlay.mediaController.setBufferProgress(mediaController.getBufferProgress());
        //还原默认的view
        onStateNormal();
        //取消定时器
        mediaController.cancelDismissControlViewSchedule();
        MediaUtils.startFullscreen(fullPlay, mediaData, currentUrlIndex);
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


    //获取当前播放uil
    public MediaData getCurrentData() {
        return MediaUtils.getCurrentMediaData(mediaData, currentUrlIndex);
    }

    //设置TextureView旋转
    public static void setTextureViewRotation(int rotation) {
        if (EasyMediaManager.textureView != null) {
            EasyMediaManager.textureView.setRotation(rotation);
        }
    }

    //设置TextureView显示角度
    public static void setVideoImageDisplayType(@MediaDisplayType.Code int type) {
        EasyVideoPlayer.VIDEO_IMAGE_DISPLAY_TYPE = type;
        if (EasyMediaManager.textureView != null) {
            EasyMediaManager.textureView.requestLayout();
        }
    }

    /**
     * 退出全屏和小窗口后调用这个方法 继续播放
     */
    public void playOnThisVideo() {
        //1.清空全屏和小窗的播放器
        if (EasyVideoPlayerManager.getVideoTiny() != null) {
            currentState = EasyVideoPlayerManager.getVideoTiny().getCurrentState();
            currentUrlIndex = EasyVideoPlayerManager.getVideoTiny().getCurrentUrlIndex();
            EasyVideoPlayerManager.getVideoTiny().cleanTiny();
        } else if (EasyVideoPlayerManager.getVideoFullscreen() != null) {
            currentState = EasyVideoPlayerManager.getVideoFullscreen().getCurrentState();
            currentUrlIndex = EasyVideoPlayerManager.getVideoFullscreen().getCurrentUrlIndex();
            clearFloatScreen();
        }
        mediaController.setCurrentState(currentState);
        addTextureView();
        //2.在本Video上播放
        setState(currentState);
    }

    /**
     * 清空全屏
     */
    public void clearFloatScreen() {
        MediaUtils.setRequestedOrientation(getContext(), ORIENTATION_NORMAL);
        MediaUtils.setActivityFullscreen(getContext(), false);
        EasyVideoPlayer currentPlay = EasyVideoPlayerManager.getVideoFullscreen();
        if (currentPlay != null) {
            currentPlay.removeTextureView();
            if (currentPlay instanceof View) {
                MediaUtils.getDecorView(getContext()).removeView((View) currentPlay);
            }
            EasyVideoPlayerManager.setVideoFullscreen(null);
        }
    }

    /**
     * 重力感应的时候调用的函数
     *
     * @param x
     */
    public void autoFullscreen(float x) {
        if (isCurrentPlay()
                && currentState == CURRENT_STATE_PLAYING
                && currentScreen != SCREEN_WINDOW_FULLSCREEN) {
            if (x > 0) {
                MediaUtils.setRequestedOrientation(getContext(), ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            } else {
                MediaUtils.setRequestedOrientation(getContext(), ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
            }
            onEvent(EasyMediaAction.ON_ENTER_FULLSCREEN);
            startWindowFullscreen();
        }
    }

    /**
     * 实现播放事件的回掉
     *
     * @param type 事件类型
     */
    @Override
    public void onEvent(int type) {
        if (EasyMediaManager.MEDIA_EVENT != null && isCurrentPlay() && getCurrentData() != null) {
            EasyMediaManager.MEDIA_EVENT.onEvent(type, getCurrentData());
        }
    }


    /**
     * 自动退出全屏
     */
    public void autoQuitFullscreen() {
        if ((System.currentTimeMillis() - lastAutoFullscreenTime) > 2000
                && isCurrentPlay()
                && currentState == CURRENT_STATE_PLAYING
                && currentScreen == SCREEN_WINDOW_FULLSCREEN) {
            lastAutoFullscreenTime = System.currentTimeMillis();
            MediaUtils.backPress();
        }
    }

    /**
     * 设置是列表播放的
     * 请在setData之前设置
     */
    public void setCurrentPlayList() {
        this.currentScreen = MediaScreenStatus.SCREEN_WINDOW_LIST;
    }

    /**
     * 全屏后是否可以竖屏
     */
    public void setFullscreenPortrait(boolean mFullscreenPortrait) {
        this.mFullscreenPortrait = mFullscreenPortrait;
    }

    //是否可以全屏
    public void setFullscreenEnable(boolean mFullscreenEnable) {
        this.mFullscreenEnable = mFullscreenEnable;
        mediaController.setControllFullEnable(mFullscreenEnable);
    }


    public boolean isFullscreenPortrait() {
        return mFullscreenPortrait;
    }

    /**
     * 设置宽高比例
     *
     * @param widthRatio
     * @param heightRatio
     */
    public void setVideoRatio(int widthRatio, int heightRatio) {
        this.heightRatio = heightRatio;
        this.widthRatio = widthRatio;
    }

    public ImageView getThumbImageView() {
        return mediaController.getThumbImageView();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);

    }
}
