package com.ashlikun.media.video.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.ashlikun.media.R;
import com.ashlikun.media.video.EasyMediaManager;
import com.ashlikun.media.video.EasyVideoAction;
import com.ashlikun.media.video.EasyVideoPlayerManager;
import com.ashlikun.media.video.VideoData;
import com.ashlikun.media.video.VideoScreenUtils;
import com.ashlikun.media.video.VideoUtils;
import com.ashlikun.media.video.controller.EasyVideoController;
import com.ashlikun.media.video.status.VideoStatus;

import java.util.List;

import static com.ashlikun.media.video.status.VideoStatus.AUTO_COMPLETE;
import static com.ashlikun.media.video.status.VideoStatus.ERROR;
import static com.ashlikun.media.video.status.VideoStatus.NORMAL;
import static com.ashlikun.media.video.status.VideoStatus.PAUSE;
import static com.ashlikun.media.video.status.VideoStatus.PLAYING;

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
     * 全屏后是否可以竖屏，默认动态计算，当视频是竖屏的时候  可以竖屏
     * 0:自动判断(宽高比是否可以竖屏)
     * 1:可以竖屏(2个横屏，一个竖屏)
     * 2:不可以竖屏(2个横屏)
     * 3:只能单一横屏
     */
    protected int mFullscreenPortrait = 0;
    /**
     * 是否可以全屏
     */
    protected boolean mFullscreenEnable = true;
    /**
     * 是否全屏播放
     */
    protected boolean isFull = false;
    /**
     * 播放器控制器
     */
    protected EasyVideoController mediaController;

    public EasyVideoPlayer(Context context) {
        this(context, null);
    }

    public EasyVideoPlayer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EasyVideoPlayer(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    public void initView(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.EasyVideoPlayer);
        //是否可以全屏
        mFullscreenEnable = a.getBoolean(R.styleable.EasyVideoPlayer_video_full_screen_enable, mFullscreenEnable);
        mFullscreenPortrait = a.getInt(R.styleable.EasyVideoPlayer_video_full_screen_portrait, mFullscreenPortrait);
        a.recycle();
        createMiddleView();
        initController(createController());
        try {
            if (isCurrentPlay()) {
                ORIENTATION_NORMAL = VideoUtils.getActivity(context).getRequestedOrientation();
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
    protected EasyVideoController createController() {
        return new EasyVideoController(getContext());
    }

    /**
     * 获取控制器
     *
     * @return
     */
    public EasyVideoController getMediaController() {
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
    protected void initController(EasyVideoController controller) {
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
    public boolean setDataSource(List<VideoData> mediaData, int defaultIndex) {
        boolean res = super.setDataSource(mediaData, defaultIndex);
        if (mediaController != null && getCurrentData() != null) {
            mediaController.setDataSource(getCurrentData());
        }
        return res;
    }

    @Override
    public boolean switchData(int position) {
        if (super.switchData(position)) {
            if (mediaController != null && getCurrentData() != null) {
                mediaController.setDataSource(getCurrentData());
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean setStatus(int state) {
        boolean result = super.setStatus(state);
        if (result) {
            if (mediaController != null) {
                mediaController.setCurrentState(currentState);
            }
        }
        return result;
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
            Toast.makeText(getContext(), getResources().getString(R.string.easy_video_no_url), Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentState == NORMAL) {
            onEvent(EasyVideoAction.ON_CLICK_START_ICON);
            startVideo();
        } else if (currentState == PLAYING) {
            onEvent(EasyVideoAction.ON_CLICK_PAUSE);
            setStatus(VideoStatus.PAUSE);
        } else if (currentState == PAUSE) {
            onEvent(EasyVideoAction.ON_CLICK_RESUME);
            setStatus(PLAYING);
        } else if (currentState == AUTO_COMPLETE) {
            onEvent(EasyVideoAction.ON_CLICK_START_AUTO_COMPLETE);
            startVideo();
        }
    }

    /**
     * 当控制器从新播放点击
     */
    @Override
    public void onRetryClick() {
        if (VideoUtils.showWifiDialog(getContext(), getCurrentData(), this)) {
            onEvent(EasyVideoAction.ON_CLICK_START_ICON);
            return;
        }
        startVideo();
        onEvent(EasyVideoAction.ON_CLICK_START_ERROR);
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
            VideoScreenUtils.backPress();
        } else {
            onEvent(EasyVideoAction.ON_ENTER_FULLSCREEN);
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

    }


    @Override
    protected void onStatePreparing() {
        super.onStatePreparing();

    }


    @Override
    protected void onStatePrepared() {//因为这个紧接着就会进入播放状态，所以不设置state
        super.onStatePrepared();
        if (seekToInAdvance != 0) {
            EasyMediaManager.seekTo(seekToInAdvance);
            seekToInAdvance = 0;
        } else {
            long position = VideoUtils.getSavedProgress(getContext(), getCurrentData());
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

    }

    /**
     * 暂停
     */
    @Override
    protected void onStatePause() {
        super.onStatePause();

    }

    /**
     * 错误
     */
    @Override
    protected void onStateError() {
        super.onStateError();

    }

    /**
     * 开始缓冲
     */
    @Override
    protected void onBufferStart() {
        super.onBufferStart();
    }

    /**
     * 自动完成
     */
    @Override
    protected void onStateAutoComplete() {
        super.onStateAutoComplete();
        if (mediaController != null) {
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
    public boolean onAutoCompletion() {
        boolean res = super.onAutoCompletion();
        if (!res) {
            if (mediaController != null) {
                mediaController.onAutoCompletion();
            }
            setStatus(VideoStatus.AUTO_COMPLETE);
            if (isScreenFull()) {
                VideoScreenUtils.backPress();
            }
        }
        return res;
    }

    /**
     * 播放器生命周期
     */
    @Override
    public void onVideoSizeChanged() {
        super.onVideoSizeChanged();
        if (EasyMediaManager.getTextureView() != null) {
            if (videoRotation != 0) {
                EasyMediaManager.getTextureView().setRotation(videoRotation);
            }
        }
    }

    @Override
    public void onInfo(int what, int extra) {
        super.onInfo(what, extra);
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
        VideoScreenUtils.CLICK_QUIT_FULLSCREEN_TIME = System.currentTimeMillis();
        removeTextureView();
        EasyVideoPlayer fullPlay = new EasyVideoPlayer(getContext());
        fullPlay.setFullscreenPortrait(mFullscreenPortrait);
        fullPlay.setStatus(currentState);
        fullPlay.addTextureView();
        if (mediaController != null && fullPlay.mediaController != null) {
            fullPlay.mediaController.setBufferProgress(mediaController.getBufferProgress());
        }
        //还原默认的view
        setStatus(VideoStatus.NORMAL);
        //取消定时器
        if (mediaController != null) {
            mediaController.cancelDismissControlViewSchedule();
        }
        VideoScreenUtils.startFullscreen(fullPlay, mediaData, currentUrlIndex);
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
            VideoScreenUtils.clearFloatScreen(getContext());
        }
        if (mediaController != null) {
            mediaController.setCurrentState(currentState);
        }
        addTextureView();
        //2.在本Video上播放
        setStatus(currentState);
    }


    public void setFull(boolean full) {
        isFull = full;
        if (mediaController != null) {
            mediaController.setFull(full);
        }
    }

    @Override
    public boolean isScreenFull() {
        return isFull;
    }

    /**
     * 实现播放事件的回掉
     *
     * @param type 事件类型
     */
    @Override
    public void onEvent(int type) {
        super.onEvent(type);
        if (type == EasyVideoAction.ON_QUIT_FULLSCREEN || type == EasyVideoAction.ON_QUIT_TINYSCREEN) {
            //如果默认的Video播放过视频,就直接在这个默认的上面播放
            playOnThisVideo();
        }
    }

    /**
     * 全屏后是否可以竖屏
     * 0:自动判断(宽高比是否可以竖屏)
     * 1:可以竖屏(2个横屏，一个竖屏)
     * 2:不可以竖屏(2个横屏)
     * 3:只能单一横屏
     */
    public void setFullscreenPortrait(int mFullscreenPortrait) {
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
     * @return 0:自动判断(宽高比是否可以竖屏)
     * 1:可以竖屏(2个横屏，一个竖屏)
     * 2:不可以竖屏(2个横屏)
     * 3:只能单一横屏
     */
    public int getFullscreenPortrait() {
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
