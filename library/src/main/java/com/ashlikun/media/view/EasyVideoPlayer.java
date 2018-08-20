package com.ashlikun.media.view;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.TypedArray;
import android.util.AttributeSet;
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
import com.ashlikun.media.controller.EasyMediaController;
import com.ashlikun.media.controller.MediaControllerInterface;
import com.ashlikun.media.status.MediaDisplayType;
import com.ashlikun.media.status.MediaScreenStatus;

import java.util.List;

import static com.ashlikun.media.status.MediaStatus.CURRENT_STATE_AUTO_COMPLETE;
import static com.ashlikun.media.status.MediaStatus.CURRENT_STATE_ERROR;
import static com.ashlikun.media.status.MediaStatus.CURRENT_STATE_NORMAL;
import static com.ashlikun.media.status.MediaStatus.CURRENT_STATE_PAUSE;
import static com.ashlikun.media.status.MediaStatus.CURRENT_STATE_PLAYING;

/**
 * 作者　　: 李坤
 * 创建时间: 2017/11/24 17:28
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：播放器view，负责视频的播放
 */
public class EasyVideoPlayer extends BaseEasyVideoPlay
        implements EasyOnControllEvent, IEasyVideoPlayListener {
    public static int ORIENTATION_FULLSCREEN_SENSOR = ActivityInfo.SCREEN_ORIENTATION_SENSOR;
    public static int ORIENTATION_NORMAL = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    public static int ORIENTATION_FULLSCREEN_LANDSCAPE = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
    /**
     * 是否保存进度
     */
    public static boolean SAVE_PROGRESS = true;
    @MediaDisplayType.Code
    public static int VIDEO_IMAGE_DISPLAY_TYPE = 0;

    public int seekToInAdvance = 0;
    public int widthRatio = 0;
    public int heightRatio = 0;


    public int videoRotation = 0;
    /**
     * 全屏后是否可以竖屏
     */
    protected boolean mFullscreenPortrait = true;
    /**
     * 是否可以全屏
     */
    protected boolean mFullscreenEnable = true;
    /**
     * 播放器控制器
     */
    MediaControllerInterface mediaController;

    public EasyVideoPlayer(Context context) {
        this(context, null);
    }

    public EasyVideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void initView(Context context, AttributeSet attrs) {
        super.initView(context, attrs);
        currentScreen = MediaScreenStatus.SCREEN_WINDOW_NORMAL;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.EasyVideoPlayer);
        if (!a.hasValue(0)) {
            setBackgroundColor(0xff000000);
        }
        mFullscreenEnable = a.getBoolean(R.styleable.EasyVideoPlayer_evp_full_screen_enable, true);
        a.recycle();
        initController(getController());
        try {
            if (isCurrentPlay()) {
                ORIENTATION_NORMAL = MediaUtils.getActivity(context).getRequestedOrientation();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 子类可以实现从写
     *
     * @return
     */
    public MediaControllerInterface getController() {
        return new EasyMediaController(getContext());
    }

    /**
     * 初始化控制器
     *
     * @param controller
     */
    public final void initController(MediaControllerInterface controller) {
        if (this.mediaController != null) {
            removeView((View) mediaController);
        }
        this.mediaController = controller;
        addView((View) mediaController);
        mediaController.setOnControllEvent(this);
        mediaController.setControllFullEnable(mFullscreenEnable);
    }


    @Override
    public void setDataSource(List<MediaData> mediaData, int defaultIndex) {
        super.setDataSource(mediaData, defaultIndex);
        mediaController.setCurrentScreen(getCurrentScreen());
        mediaController.setDataSource(mediaData.get(currentUrlIndex));
    }

    /**
     * 开始播放
     */
    @Override
    public void startVideo() {
        super.startVideo();
        if (isScreenFull()) {
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
            onEvent(EasyMediaAction.ON_CLICK_START_ICON);
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
        if (isScreenFull()) {
            //退出全屏
            MediaScreenUtils.backPress();
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


    @Override
    protected void onStateNormal() {
        super.onStateNormal();
        mediaController.setCurrentState(currentState);
    }


    @Override
    protected void onStatePreparing() {
        super.onStatePreparing();
        mediaController.setCurrentState(currentState);
    }

    @Override
    protected void onStatePreparingChangingUrl(int currentUrlIndex, int seekToInAdvance) {
        super.onStatePreparingChangingUrl(currentUrlIndex, seekToInAdvance);
        mediaController.setCurrentState(currentState);
    }

    @Override
    protected void onStatePrepared() {//因为这个紧接着就会进入播放状态，所以不设置state
        super.onStatePrepared();
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

    /**
     * 开始播放回掉
     */
    @Override
    protected void onStatePlaying() {
        super.onStatePlaying();
        mediaController.setCurrentState(currentState);
    }

    /**
     * 暂停
     */
    @Override
    protected void onStatePause() {
        super.onStatePause();
        mediaController.setCurrentState(currentState);
    }

    /**
     * 错误
     */
    @Override
    protected void onStateError() {
        super.onStateError();
        mediaController.setCurrentState(currentState);
    }

    /**
     * 自动完成
     */
    @Override
    protected void onStateAutoComplete() {
        super.onStateAutoComplete();
        mediaController.setCurrentState(currentState);
        mediaController.setMaxProgressAndTime();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (isScreenFull()) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        if (widthRatio != 0 && heightRatio != 0) {
            int specWidth = MeasureSpec.getSize(widthMeasureSpec);
            int specHeight = (int) ((specWidth * (float) heightRatio) / widthRatio);
            int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(specWidth, MeasureSpec.EXACTLY);
            int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(specHeight, MeasureSpec.EXACTLY);
            super.onMeasure(childWidthMeasureSpec, childHeightMeasureSpec);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    /**
     * 播放器生命周期
     */
    @Override
    public void onPrepared() {
        onStatePrepared();
        onStatePlaying();
    }

    /**
     * 播放器生命周期
     *
     * @param what
     * @param extra
     */
    @Override
    public void onInfo(int what, int extra) {

    }

    /**
     * 播放器生命周期
     */
    @Override
    public void onSeekComplete() {

    }

    /**
     * 播放器生命周期
     *
     * @param what
     * @param extra
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
     * 播放器生命周期
     *
     * @param bufferProgress
     */
    @Override
    public void setBufferProgress(int bufferProgress) {
        mediaController.setBufferProgress(bufferProgress);
    }

    /**
     * 播放器生命周期
     */
    @Override
    public void onAutoCompletion() {
        Runtime.getRuntime().gc();
        onEvent(EasyMediaAction.ON_AUTO_COMPLETE);
        mediaController.onAutoCompletion();
        onStateAutoComplete();
        if (isScreenFull()) {
            MediaScreenUtils.backPress();
        }
        EasyMediaManager.instance().releaseMediaPlayer();
        MediaUtils.saveProgress(getContext(), getCurrentData(), 0);
    }

    /**
     * 播放器生命周期
     */
    @Override
    public void onVideoSizeChanged() {
        if (EasyMediaManager.textureView != null) {
            if (videoRotation != 0) {
                EasyMediaManager.textureView.setRotation(videoRotation);
            }
            EasyMediaManager.textureView.setVideoSize(EasyMediaManager.instance().currentVideoWidth, EasyMediaManager.instance().currentVideoHeight);
        }
    }


    /**
     * 播放器生命周期,自己主动调用的,还原状态
     */
    @Override
    public void onForceCompletionTo() {
        if (currentState == CURRENT_STATE_PLAYING || currentState == CURRENT_STATE_PAUSE) {
            int position = mediaController.getCurrentPositionWhenPlaying();
            MediaUtils.saveProgress(getContext(), getCurrentData(), position);
        }
        onStateNormal();
        removeTextureView();
        EasyMediaManager.instance().currentVideoWidth = 0;
        EasyMediaManager.instance().currentVideoHeight = 0;
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


    @Override
    public int getCurrentState() {
        return currentState;
    }

    @Override
    public List<MediaData> getMediaData() {
        return mediaData;
    }

    /**
     * 开始全屏播放
     * 在当前activity的跟布局加一个新的最大化的EasyVideoPlayer
     * 再把activity设置成全屏，
     */
    public void startWindowFullscreen() {
        MediaScreenUtils.CLICK_QUIT_FULLSCREEN_TIME = System.currentTimeMillis();
        removeTextureView();
        EasyVideoPlayer fullPlay = new EasyVideoPlayer(getContext());
        fullPlay.setFullscreenPortrait(mFullscreenPortrait);
        fullPlay.setStatus(currentState);
        fullPlay.addTextureView();
        fullPlay.mediaController.setBufferProgress(mediaController.getBufferProgress());
        //还原默认的view
        onStateNormal();
        //取消定时器
        mediaController.cancelDismissControlViewSchedule();
        MediaScreenUtils.startFullscreen(fullPlay, mediaData, currentUrlIndex);
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
        setStatus(currentState);
    }

    /**
     * 清空全屏
     */
    public void clearFloatScreen() {
        MediaUtils.setRequestedOrientation(getContext(), ORIENTATION_NORMAL);
        MediaScreenUtils.setActivityFullscreen(getContext(), false);
        BaseEasyVideoPlay currentPlay = EasyVideoPlayerManager.getVideoFullscreen();
        if (currentPlay != null) {
            currentPlay.removeTextureView();
            if (currentPlay instanceof View) {
                MediaUtils.getDecorView(getContext()).removeView(currentPlay);
            }
            EasyVideoPlayerManager.setVideoFullscreen(null);
        }
    }


    /**
     * 实现播放事件的回掉
     *
     * @param type 事件类型
     */
    @Override
    public void onEvent(int type) {
        super.onEvent(type);
        if (type == EasyMediaAction.ON_QUIT_FULLSCREEN || type == EasyMediaAction.ON_QUIT_TINYSCREEN) {
            //如果默认的Video播放过视频,就直接在这个默认的上面播放
            playOnThisVideo();
        }
    }

    /**
     * 设置是列表播放的
     * 请在setData之前设置
     */
    public void setCurrentPlayList() {
        setCurrentScreen(MediaScreenStatus.SCREEN_WINDOW_LIST);
    }

    /**
     * 全屏后是否可以竖屏
     */
    public void setFullscreenPortrait(boolean mFullscreenPortrait) {
        this.mFullscreenPortrait = mFullscreenPortrait;
    }

    /**
     * 是否可以全屏
     *
     * @param mFullscreenEnable
     */
    public void setFullscreenEnable(boolean mFullscreenEnable) {
        this.mFullscreenEnable = mFullscreenEnable;
        mediaController.setControllFullEnable(mFullscreenEnable);
    }


    /**
     * 是否可以竖屏
     *
     * @return
     */
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


}
