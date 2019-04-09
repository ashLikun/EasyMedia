package com.ashlikun.media.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
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
import com.ashlikun.media.status.MediaViewType;

import java.util.List;

import static com.ashlikun.media.status.MediaStatus.AUTO_COMPLETE;
import static com.ashlikun.media.status.MediaStatus.ERROR;
import static com.ashlikun.media.status.MediaStatus.NORMAL;
import static com.ashlikun.media.status.MediaStatus.PAUSE;
import static com.ashlikun.media.status.MediaStatus.PLAYING;

/**
 * 作者　　: 李坤
 * 创建时间: 2017/11/24 17:28
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：播放器view，负责视频的播放
 * 基于{@link BaseEasyVideoPlay} 实现带有控制器的播放器
 * 可以重写 {@link #createController} 实现不同的控制器
 * 可以重写 {@link #createMiddleView} 添加中间控件，比如弹幕
 */
public class EasyVideoPlayer extends BaseEasyVideoPlay
        implements EasyOnControllEvent, IEasyVideoPlayListener {

    /**
     * 是否保存进度
     */
    public static boolean SAVE_PROGRESS = true;


    public int seekToInAdvance = 0;
    public float ratio = 0;


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
    protected MediaControllerInterface mediaController;

    public EasyVideoPlayer(Context context) {
        this(context, null);
    }

    public EasyVideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void initView(Context context, AttributeSet attrs) {
        super.initView(context, attrs);
        currentMediaType = MediaViewType.NORMAL;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.EasyVideoPlayer);
        //是否可以全屏
        mFullscreenEnable = a.getBoolean(R.styleable.EasyVideoPlayer_video_full_screen_enable, true);
        a.recycle();
        createMiddleView();
        initController(createController());
        try {
            if (isCurrentPlay()) {
                ORIENTATION_NORMAL = MediaUtils.getActivity(context).getRequestedOrientation();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 这里提供给继承者的添加中间控件
     * 在控制器与播放器中间的控件
     * 比如弹幕
     */
    protected void createMiddleView() {
    }

    /**
     * 子类可以实现从写
     *
     * @return
     */
    protected MediaControllerInterface createController() {
        return new EasyMediaController(getContext());
    }

    /**
     * 获取控制器
     *
     * @return
     */
    public MediaControllerInterface getMediaController() {
        return mediaController;
    }

    /**
     * 是否显示控制器
     *
     * @param isShow 是否显示
     */
    public void setControllerVisiable(boolean isShow) {
        if (!isShow) {
            if (mediaController != null) {
                removeView((View) mediaController);
            }
        } else {
            if (mediaController == null) {
                initController(createController());
            }
        }
    }

    /**
     * 初始化控制器
     *
     * @param controller
     */
    protected void initController(MediaControllerInterface controller) {
        if (mediaController != null) {
            removeView((View) mediaController);
        }
        mediaController = controller;
        if (mediaController != null) {
            addView((View) mediaController);
            mediaController.setOnControllEvent(this);
            mediaController.setControllFullEnable(mFullscreenEnable);
        }
    }


    @Override
    public boolean setDataSource(List<MediaData> mediaData, int defaultIndex) {
        if (super.setDataSource(mediaData, defaultIndex) && mediaController != null) {
            mediaController.setCurrentScreen(getCurrentMediaType());
            mediaController.setDataSource(mediaData.get(currentUrlIndex));
        }
        return true;
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
        if (currentState == NORMAL) {
            if (MediaUtils.showWifiDialog(getContext(), getCurrentData(), this)) {
                return;
            }
            onEvent(EasyMediaAction.ON_CLICK_START_ICON);
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
        if (currentState == AUTO_COMPLETE) {
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
        if (currentState == ERROR) {
            startVideo();
        } else {
            if (mediaController != null) {
                mediaController.startDismissControlViewSchedule();
            }
        }
    }


    @Override
    protected void onStateNormal() {
        super.onStateNormal();
        if (mediaController != null) {
            mediaController.setCurrentState(currentState);
        }
    }


    @Override
    protected void onStatePreparing() {
        super.onStatePreparing();
        if (mediaController != null) {
            mediaController.setCurrentState(currentState);
        }
    }

    @Override
    protected void onStatePreparingChangingUrl(int currentUrlIndex, int seekToInAdvance) {
        super.onStatePreparingChangingUrl(currentUrlIndex, seekToInAdvance);
        if (mediaController != null) {
            mediaController.setCurrentState(currentState);
        }
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
        if (mediaController != null) {
            mediaController.setCurrentState(currentState);
        }
    }

    /**
     * 暂停
     */
    @Override
    protected void onStatePause() {
        super.onStatePause();
        if (mediaController != null) {
            mediaController.setCurrentState(currentState);
        }
    }

    /**
     * 错误
     */
    @Override
    protected void onStateError() {
        super.onStateError();
        if (mediaController != null) {
            mediaController.setCurrentState(currentState);
        }
    }

    /**
     * 自动完成
     */
    @Override
    protected void onStateAutoComplete() {
        super.onStateAutoComplete();
        if (mediaController != null) {
            mediaController.setCurrentState(currentState);
            mediaController.setMaxProgressAndTime();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (isScreenFull()) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        if (ratio != 0) {
            int specWidth = MeasureSpec.getSize(widthMeasureSpec);
            int specHeight = (int) (specWidth / ratio);
            int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(specWidth, MeasureSpec.EXACTLY);
            int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(specHeight, MeasureSpec.EXACTLY);
            super.onMeasure(childWidthMeasureSpec, childHeightMeasureSpec);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    /**
     * 准备播放
     */
    @Override
    public void onPrepared() {
        super.onPrepared();
    }

    /**
     * 缓存进度更新
     *
     * @param bufferProgress
     */
    @Override
    public void setBufferProgress(int bufferProgress) {
        super.setBufferProgress(bufferProgress);
        if (mediaController != null) {
            mediaController.setBufferProgress(bufferProgress);
        }
    }

    /**
     * 播放器生命周期
     */
    @Override
    public void onAutoCompletion() {
        super.onAutoCompletion();
        if (mediaController != null) {
            mediaController.onAutoCompletion();
        }
        onStateAutoComplete();
        if (isScreenFull()) {
            MediaScreenUtils.backPress();
        }
    }

    /**
     * 播放器生命周期
     */
    @Override
    public void onVideoSizeChanged() {
        super.onVideoSizeChanged();
        if (EasyMediaManager.textureView != null) {
            if (videoRotation != 0) {
                EasyMediaManager.textureView.setRotation(videoRotation);
            }
        }
    }


    /**
     * 播放器生命周期,自己主动调用的,还原状态
     */
    @Override
    public void onForceCompletionTo() {
        super.onForceCompletionTo();
    }

    /**
     * 开始全屏播放
     * 在当前activity的跟布局加一个新的最大化的EasyVideoPlayer
     * 再把activity设置成全屏，
     */
    public void startWindowFullscreen() {
        //这里对应的不能释放当前视频
        MediaScreenUtils.CLICK_QUIT_FULLSCREEN_TIME = System.currentTimeMillis();
        removeTextureView();
        EasyVideoPlayer fullPlay = new EasyVideoPlayer(getContext());
        fullPlay.setFullscreenPortrait(mFullscreenPortrait);
        fullPlay.setStatus(currentState);
        fullPlay.addTextureView();
        if (mediaController != null && fullPlay.mediaController != null) {
            fullPlay.mediaController.setBufferProgress(mediaController.getBufferProgress());
        }
        //还原默认的view
        onStateNormal();
        //取消定时器
        if (mediaController != null) {
            mediaController.cancelDismissControlViewSchedule();
        }
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
        if (mediaController != null) {
            mediaController.setCurrentState(currentState);
        }
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
        setCurrentMediaType(MediaViewType.LIST);
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
        if (mediaController != null) {
            mediaController.setControllFullEnable(mFullscreenEnable);
        }
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
     * @param ratio 比例  width/height
     */
    public void setVideoRatio(float ratio) {
        this.ratio = ratio;
    }

    public ImageView getThumbImageView() {
        if (mediaController != null) {
            return mediaController.getThumbImageView();
        }
        return null;
    }

    @Override
    public void release() {
        super.release();
        if (mediaController != null) {
            mediaController.cancelDismissControlViewSchedule();
        }
    }
}
