package com.ashlikun.media.video.controller;

import android.content.Context;
import android.media.AudioManager;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.ashlikun.media.video.EasyVideoAction;
import com.ashlikun.media.video.EasyMediaManager;
import com.ashlikun.media.video.VideoData;
import com.ashlikun.media.video.VideoUtils;
import com.ashlikun.media.R;
import com.ashlikun.media.video.status.VideoStatus;
import com.ashlikun.media.video.view.EasyVideoDialogBright;
import com.ashlikun.media.video.view.EasyVideoDialogProgress;
import com.ashlikun.media.video.view.EasyVideoDialogVolume;
import com.ashlikun.media.video.view.EasyOnControllEvent;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.ashlikun.media.video.status.VideoStatus.ERROR;
import static com.ashlikun.media.video.status.VideoStatus.PAUSE;
import static com.ashlikun.media.video.status.VideoStatus.PLAYING;

/**
 * 作者　　: 李坤
 * 创建时间: 2017/11/28　9:43
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：视频控制器
 */

public class EasyVideoController extends RelativeLayout implements View.OnClickListener
        , VideoControllerBottom.OnEventListener {
    public static final int THRESHOLD = 80;

    protected ScheduledFuture showControllerFuture;
    protected EasyOnControllEvent onControllEvent;
    //音频管理器，改变声音大小
    protected AudioManager mAudioManager;
    protected EasyControllerViewHolder viewHolder;
    //是否是全屏播放
    public boolean isFull = false;
    //当前播放状态
    @VideoStatus.Code
    public int currentState = VideoStatus.NORMAL;
    protected float mDownX;//按下的X坐标
    protected float mDownY;//按下的Y坐标
    protected int mScreenWidth;//屏幕宽度
    protected int mScreenHeight;//屏幕高度
    protected boolean mChangeVolume = false;//是否改变音量
    protected boolean mChangePosition = false;//是否改变进度
    protected boolean mChangeBrightness = false;//是否改变亮度
    protected long mGestureDownPosition;
    protected int mGestureDownVolume;
    protected float mGestureDownBrightness;

    protected long mSeekTimePosition;//滑动播放进度的位置

    //进度对话框
    protected EasyVideoDialogProgress mProgressDialog;
    //音量对话框
    protected EasyVideoDialogVolume mVolumeDialog;
    //亮度
    protected EasyVideoDialogBright mBrightDialog;


    public EasyVideoController(Context context) {
        this(context, null);
    }

    public EasyVideoController(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EasyVideoController(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }


    /**
     * 监听控制器事件
     */
    public void setOnControllEvent(EasyOnControllEvent onControllEvent) {
        this.onControllEvent = onControllEvent;
    }

    /**
     * 设置是否可以全屏
     */
    public void setControllFullEnable(boolean fullEnable) {
        viewHolder.setControllFullEnable(fullEnable);
    }

    /**
     * 设置数据源
     */
    public void setDataSource(VideoData mediaData) {
        viewHolder.setDataSource(mediaData);
    }

    /**
     * 可以从写
     */
    public int getLayoutId() {
        return R.layout.easy_video_layout_controller;
    }

    /**
     * 可以从写实现界面展示
     *
     * @return
     */
    public EasyControllerViewHolder getControllerViewHolder() {
        return new EasyControllerViewHolder(this, this, this);
    }

    private void initView() {
        setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        mScreenWidth = getContext().getResources().getDisplayMetrics().widthPixels;
        mScreenHeight = getContext().getResources().getDisplayMetrics().heightPixels;
        View.inflate(getContext(), getLayoutId(), this);
        viewHolder = getControllerViewHolder();
        mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        setOnClickListener(this);
        setFull(isFull);
    }

    /**
     * 开始触摸进度条
     */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        cancelDismissControlViewSchedule();
    }

    /**
     * 结束触摸进度条
     */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        onControllEvent.onEvent(EasyVideoAction.ON_SEEK_POSITION);
        ViewParent vpup = getParent();
        while (vpup != null) {
            vpup.requestDisallowInterceptTouchEvent(false);
            vpup = vpup.getParent();
        }
        if (currentState != PLAYING &&
                currentState != PAUSE) {
            return;
        }
        long time = seekBar.getProgress() * getDuration() / 100;
        EasyMediaManager.seekTo(time);
        cancelDismissControlViewSchedule();
        if (viewHolder.containerIsShow()) {
            showControllerFuture = VideoUtils.POOL_SCHEDULE().schedule(new DismissControlViewRunnable(), 3500, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void onFullscreenClick() {
        onControllEvent.onFullscreenClick();
    }

    //底部进度改变
    @Override
    public void onProgressChang(int progress, int secondaryProgress) {
        viewHolder.setProgress(progress, secondaryProgress);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.start) {
            cancelDismissControlViewSchedule();
            onControllEvent.onPlayStartClick();
        } else if (i == R.id.retry_btn) {
            onControllEvent.onRetryClick();
        } else if (v == this) {
            //保证滑动事件后不调用点击事件
            if (!mChangePosition && !mChangeVolume && !mChangeBrightness) {
                onControllEvent.onControllerClick();
            } else {
                mChangePosition = false;
                mChangeVolume = false;
                mChangeVolume = false;
            }
        }
    }

    @Override
    protected void dispatchSetPressed(boolean pressed) {

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean res = super.onTouchEvent(event);
        // 全屏模式下的CURRENT_STATE_ERROR状态下,不响应进度拖动事件.
        // 否则会因为mediaplayer的状态非法导致App Crash
        if (currentState == ERROR) {
            return false;
        }
        if (!isFull) {
            return res;
        }
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = x;
                mDownY = y;
                mChangeVolume = false;
                mChangePosition = false;
                mChangeBrightness = false;
                viewHolder.stopProgressSchedule();
                break;
            case MotionEvent.ACTION_MOVE:
                float deltaX = x - mDownX;
                float deltaY = y - mDownY;
                float absDeltaX = Math.abs(deltaX);
                float absDeltaY = Math.abs(deltaY);

                if (!mChangePosition && !mChangeVolume && !mChangeBrightness) {
                    if (absDeltaX > THRESHOLD || absDeltaY > THRESHOLD) {
                        viewHolder.stopProgressSchedule();
                        if (absDeltaX >= THRESHOLD) {
                            mChangePosition = true;
                            mGestureDownPosition = getCurrentPositionWhenPlaying();
                        } else {
                            //如果y轴滑动距离超过设置的处理范围，那么进行滑动事件处理
                            if (mDownX < mScreenWidth * 0.5f) {//左侧改变亮度
                                mChangeBrightness = true;
                                WindowManager.LayoutParams lp = VideoUtils.getWindow(getContext()).getAttributes();
                                if (lp.screenBrightness < 0) {
                                    try {
                                        mGestureDownBrightness = Settings.System.getInt(getContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
                                    } catch (Settings.SettingNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    mGestureDownBrightness = lp.screenBrightness * 255;
                                }
                            } else {//右侧改变声音
                                mChangeVolume = true;
                                mGestureDownVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                            }
                        }
                    }
                }
                //进度
                if (mChangePosition) {
                    long totalTimeDuration = getDuration();
                    mSeekTimePosition = (int) (mGestureDownPosition + deltaX * totalTimeDuration / mScreenWidth);
                    if (mSeekTimePosition > totalTimeDuration) {
                        mSeekTimePosition = totalTimeDuration;
                    }
                    String seekTime = VideoUtils.stringForTime(mSeekTimePosition);
                    String totalTime = VideoUtils.stringForTime(totalTimeDuration);

                    showProgressDialog(deltaX, seekTime, mSeekTimePosition, totalTime, totalTimeDuration);
                }
                //音量
                if (mChangeVolume) {
                    deltaY = -deltaY;
                    int max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                    int deltaV = (int) (max * deltaY * 3 / mScreenHeight);
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mGestureDownVolume + deltaV, 0);
                    //dialog中显示百分比
                    int volumePercent = (int) (mGestureDownVolume * 100 / max + deltaY * 3 * 100 / mScreenHeight);
                    showVolumeDialog(-deltaY, volumePercent);
                }
                //亮度
                if (mChangeBrightness) {
                    deltaY = -deltaY;
                    int deltaV = (int) (255 * deltaY * 3 / mScreenHeight);
                    WindowManager.LayoutParams params = VideoUtils.getWindow(getContext()).getAttributes();
                    if (((mGestureDownBrightness + deltaV) / 255) >= 1) {//这和声音有区别，必须自己过滤一下负值
                        params.screenBrightness = 1;
                    } else if (((mGestureDownBrightness + deltaV) / 255) <= 0) {
                        params.screenBrightness = 0.01f;
                    } else {
                        params.screenBrightness = (mGestureDownBrightness + deltaV) / 255;
                    }
                    VideoUtils.getWindow(getContext()).setAttributes(params);
                    //dialog中显示百分比
                    int brightnessPercent = (int) (mGestureDownBrightness * 100 / 255 + deltaY * 3 * 100 / mScreenHeight);
                    //显示亮度
                    showBrightnessDialog(brightnessPercent);
                }
                break;
            case MotionEvent.ACTION_UP:
                dismissProgressDialog();
                dismissVolumeDialog();
                dismissBrightnessDialog();
                if (mChangePosition) {
                    onControllEvent.onEvent(EasyVideoAction.ON_TOUCH_SCREEN_SEEK_POSITION);
                    EasyMediaManager.seekTo(mSeekTimePosition);
                    setProgress();
                }
                if (mChangeVolume) {
                    onControllEvent.onEvent(EasyVideoAction.ON_TOUCH_SCREEN_SEEK_VOLUME);
                }
                viewHolder.startProgressSchedule();
                break;
        }
        return true;
    }

    private void setProgress() {
        long duration = getDuration();
        int progress = (int) (mSeekTimePosition * 100 / (duration == 0 ? 1 : duration));
        viewHolder.setProgress(progress, -1);
    }

    /**
     * 显示进度对话框
     */
    public void showProgressDialog(float deltaX, String seekTime, long seekTimePosition, String totalTime, long totalTimeDuration) {
        if (mProgressDialog == null) {
            mProgressDialog = new EasyVideoDialogProgress(getContext());
        }
        mProgressDialog.show();

        mProgressDialog.setTime(seekTime, totalTime);
        mProgressDialog.setProgress(seekTimePosition, totalTimeDuration);
        mProgressDialog.setOrientation(deltaX > 0);
        viewHolder.changeUiToClean();
    }

    /**
     * 显示音量对话框
     *
     * @param deltaY
     * @param volumePercent
     */
    public void showVolumeDialog(float deltaY, int volumePercent) {
        if (mVolumeDialog == null) {
            mVolumeDialog = new EasyVideoDialogVolume(getContext());
        }
        mVolumeDialog.show();
        mVolumeDialog.setVolumePercent(volumePercent);
        viewHolder.changeUiToClean();
    }

    /**
     * 显示亮度对话框
     *
     * @param brightnessPercent
     */
    public void showBrightnessDialog(int brightnessPercent) {
        if (mBrightDialog == null) {
            mBrightDialog = new EasyVideoDialogBright(getContext());
        }
        mBrightDialog.show();
        mBrightDialog.setBrightPercent(brightnessPercent);
        viewHolder.changeUiToClean();
    }


    /**
     * 销毁进度对话框
     */
    public void dismissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    /**
     * 销毁音量对话框
     */
    public void dismissVolumeDialog() {
        if (mVolumeDialog != null) {
            mVolumeDialog.dismiss();
        }
    }

    /**
     * 销毁亮度对话框
     */
    public void dismissBrightnessDialog() {
        if (mBrightDialog != null) {
            mBrightDialog.dismiss();
        }
    }


    public long getDuration() {
        long duration = 0;
        try {
            duration = EasyMediaManager.getDuration();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            return duration;
        }
        return duration;
    }

    /**
     * 获取当前播放位置
     */
    public long getCurrentPositionWhenPlaying() {
        long position = 0;
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
     * 点击时候显示控制器（3秒后消失）
     */
    public void startDismissControlViewSchedule() {
        cancelDismissControlViewSchedule();
        if (viewHolder.containerIsShow()) {
            //隐藏
            viewHolder.showControllerViewAnim(currentState, false);
        } else {
            //显示
            viewHolder.showControllerViewAnim(currentState, true);
            showControllerFuture = VideoUtils.POOL_SCHEDULE().schedule(new DismissControlViewRunnable(), 5000, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * 取消显示控制器的定时器
     */
    public void cancelDismissControlViewSchedule() {
        if (showControllerFuture != null) {
            showControllerFuture.cancel(true);
        }
        showControllerFuture = null;
    }


    public class DismissControlViewRunnable implements Runnable {

        @Override
        public void run() {
            viewHolder.showControllerViewAnim(currentState, false);
        }
    }

    public void setFull(boolean full) {
        isFull = full;
        viewHolder.setFull(full);
    }

    /**
     * 是否只在全屏的时候显示标题和顶部
     */
    public void setOnlyFullShowTitle(boolean onlyFullShowTitle) {
        viewHolder.setOnlyFullShowTitle(onlyFullShowTitle);
    }

    /**
     * 设置当前状态
     */
    public void setCurrentState(@VideoStatus.Code int currentState) {
        this.currentState = currentState;
        //跟新ui
        viewHolder.changUi(currentState);
    }

    /**
     * 设置进度最大
     */
    public void setMaxProgressAndTime() {
        viewHolder.setProgress(100, 100);
    }

    /**
     * 设置进度缓存
     */
    public void setBufferProgress(int bufferProgress) {
        if (bufferProgress >= 0) {
            viewHolder.setProgress(-1, bufferProgress);
        }
    }

    /**
     * 获取进度缓存
     */
    public int getBufferProgress() {
        return viewHolder.getBufferProgress();
    }

    /**
     * 当自动完成的时候
     */
    public void onAutoCompletion() {
        dismissVolumeDialog();
        dismissProgressDialog();
        dismissBrightnessDialog();
    }

    /**
     * 获取触摸事件
     */
    public ImageView getThumbImageView() {
        return viewHolder.getThumbImageView();
    }

}
