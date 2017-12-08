package com.ashlikun.media.controller;

import android.content.Context;
import android.media.AudioManager;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.ashlikun.media.EasyMediaAction;
import com.ashlikun.media.EasyMediaManager;
import com.ashlikun.media.MediaUtils;
import com.ashlikun.media.R;
import com.ashlikun.media.status.EasyScreenStatus;
import com.ashlikun.media.view.EasyMediaDialogBright;
import com.ashlikun.media.view.EasyMediaDialogProgress;
import com.ashlikun.media.view.EasyMediaDialogVolume;
import com.ashlikun.media.view.EasyOnControllEvent;

import java.util.TimerTask;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.ashlikun.media.status.EasyMediaStatus.CURRENT_STATE_ERROR;
import static com.ashlikun.media.status.EasyMediaStatus.CURRENT_STATE_PAUSE;
import static com.ashlikun.media.status.EasyMediaStatus.CURRENT_STATE_PLAYING;

/**
 * 作者　　: 李坤
 * 创建时间: 2017/11/28　9:43
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：
 */

public class EasyMediaController extends RelativeLayout implements
        MediaControllerInterface, View.OnClickListener
        , MediaControllerBottom.OnEventListener, View.OnTouchListener {
    public static final int THRESHOLD = 80;

    protected ScheduledFuture showControllerFuture;
    protected EasyOnControllEvent onControllEvent;
    //音频管理器，改变声音大小
    protected AudioManager mAudioManager;
    protected EasyControllerViewHolder viewHolder;

    public int currentScreen = -1;//当前屏幕方向
    public int currentState = -1;//当前播放状态

    protected float mDownX;//按下的X坐标
    protected float mDownY;//按下的Y坐标
    protected int mScreenWidth;//屏幕宽度
    protected int mScreenHeight;//屏幕高度
    protected boolean mChangeVolume;//是否改变音量
    protected boolean mChangePosition;//是否改变进度
    protected boolean mChangeBrightness;//是否改变亮度
    protected int mGestureDownPosition;
    protected int mGestureDownVolume;
    protected float mGestureDownBrightness;

    protected int mSeekTimePosition;//滑动播放进度的位置

    //进度对话框
    protected EasyMediaDialogProgress mProgressDialog;
    //音量对话框
    protected EasyMediaDialogVolume mVolumeDialog;
    //亮度
    protected EasyMediaDialogBright mBrightDialog;


    @Override
    public void setOnControllEvent(EasyOnControllEvent onControllEvent) {
        this.onControllEvent = onControllEvent;
    }

    public EasyMediaController(Context context) {
        this(context, null);
    }

    public EasyMediaController(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EasyMediaController(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    @Override
    public void setDataSource(Object[] dataSourceObjects, int defaultUrlMapIndex, int screen, Object... objects) {
        viewHolder.setDataSource(dataSourceObjects, screen, objects);
    }

    public int getLayoutId() {
        return R.layout.easy_layout_controller;
    }

    private void initView() {
        mScreenWidth = getContext().getResources().getDisplayMetrics().widthPixels;
        mScreenHeight = getContext().getResources().getDisplayMetrics().heightPixels;
        View.inflate(getContext(), getLayoutId(), this);
        viewHolder = new EasyControllerViewHolder(this, this, this);
        mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        setOnTouchListener(this);
        setOnClickListener(this);
    }

    //开始触摸进度条
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        cancelDismissControlViewSchedule();
    }

    //结束触摸进度条
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        onControllEvent.onEvent(EasyMediaAction.ON_SEEK_POSITION);
        viewHolder.bottomContainer.startProgressSchedule();
        ViewParent vpup = getParent();
        while (vpup != null) {
            vpup.requestDisallowInterceptTouchEvent(false);
            vpup = vpup.getParent();
        }
        if (currentState != CURRENT_STATE_PLAYING &&
                currentState != CURRENT_STATE_PAUSE) {
            return;
        }
        int time = seekBar.getProgress() * getDuration() / 100;
        EasyMediaManager.seekTo(time);
        startDismissControlViewSchedule();
    }

    @Override
    public void onFullscreenClick() {
        onControllEvent.onFullscreenClick();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.start) {
            onControllEvent.onPlayStartClick();
        } else if (v == this) {
            onControllEvent.onControllerClick();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = x;
                mDownY = y;
                mChangeVolume = false;
                mChangePosition = false;
                mChangeBrightness = false;
                viewHolder.bottomContainer.stopProgressSchedule();
                break;
            case MotionEvent.ACTION_MOVE:
                float deltaX = x - mDownX;
                float deltaY = y - mDownY;
                float absDeltaX = Math.abs(deltaX);
                float absDeltaY = Math.abs(deltaY);
                if (currentScreen == EasyScreenStatus.SCREEN_WINDOW_FULLSCREEN) {
                    if (!mChangePosition && !mChangeVolume && !mChangeBrightness) {
                        if (absDeltaX > THRESHOLD || absDeltaY > THRESHOLD) {
                            viewHolder.bottomContainer.stopProgressSchedule();
                            if (absDeltaX >= THRESHOLD) {
                                // 全屏模式下的CURRENT_STATE_ERROR状态下,不响应进度拖动事件.
                                // 否则会因为mediaplayer的状态非法导致App Crash
                                if (currentState != CURRENT_STATE_ERROR) {
                                    mChangePosition = true;
                                    mGestureDownPosition = getCurrentPositionWhenPlaying();
                                }
                            } else {
                                //如果y轴滑动距离超过设置的处理范围，那么进行滑动事件处理
                                if (mDownX < mScreenWidth * 0.5f) {//左侧改变亮度
                                    mChangeBrightness = true;
                                    WindowManager.LayoutParams lp = MediaUtils.getWindow(getContext()).getAttributes();
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
                }
                //进度
                if (mChangePosition) {
                    int totalTimeDuration = getDuration();
                    mSeekTimePosition = (int) (mGestureDownPosition + deltaX * totalTimeDuration / mScreenWidth);
                    if (mSeekTimePosition > totalTimeDuration) {
                        mSeekTimePosition = totalTimeDuration;
                    }
                    String seekTime = MediaUtils.stringForTime(mSeekTimePosition);
                    String totalTime = MediaUtils.stringForTime(totalTimeDuration);

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
                    WindowManager.LayoutParams params = MediaUtils.getWindow(getContext()).getAttributes();
                    if (((mGestureDownBrightness + deltaV) / 255) >= 1) {//这和声音有区别，必须自己过滤一下负值
                        params.screenBrightness = 1;
                    } else if (((mGestureDownBrightness + deltaV) / 255) <= 0) {
                        params.screenBrightness = 0.01f;
                    } else {
                        params.screenBrightness = (mGestureDownBrightness + deltaV) / 255;
                    }
                    MediaUtils.getWindow(getContext()).setAttributes(params);
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
                    onControllEvent.onEvent(EasyMediaAction.ON_TOUCH_SCREEN_SEEK_POSITION);
                    EasyMediaManager.seekTo(mSeekTimePosition);
                    setProgress();
                }
                if (mChangeVolume) {
                    onControllEvent.onEvent(EasyMediaAction.ON_TOUCH_SCREEN_SEEK_VOLUME);
                }
                viewHolder.bottomContainer.startProgressSchedule();
                break;
        }
        return false;
    }

    public void setProgress() {
        int duration = getDuration();
        int progress = mSeekTimePosition * 100 / (duration == 0 ? 1 : duration);
        viewHolder.bottomContainer.setProgress(progress);
    }

    /**
     * 显示进度对话框
     */
    public void showProgressDialog(float deltaX, String seekTime, int seekTimePosition, String totalTime, int totalTimeDuration) {
        if (mProgressDialog == null) {
            mProgressDialog = new EasyMediaDialogProgress(getContext());
        }
        mProgressDialog.show();

        mProgressDialog.setTime(seekTime, totalTime);
        mProgressDialog.setProgress(seekTimePosition, totalTimeDuration);
        mProgressDialog.setOrientation(deltaX > 0);
        viewHolder.onClickUiToggleToClear(currentState, currentScreen);
    }

    //显示音量对话框
    public void showVolumeDialog(float deltaY, int volumePercent) {
        if (mVolumeDialog == null) {
            mVolumeDialog = new EasyMediaDialogVolume(getContext());
        }
        mVolumeDialog.show();
        mVolumeDialog.setVolumePercent(volumePercent);

        viewHolder.onClickUiToggleToClear(currentState, currentScreen);
    }

    //显示亮度对话框
    public void showBrightnessDialog(int brightnessPercent) {
        if (mBrightDialog == null) {
            mBrightDialog = new EasyMediaDialogBright(getContext());
        }
        mBrightDialog.show();
        mBrightDialog.setBrightPercent(brightnessPercent);
        viewHolder.onClickUiToggleToClear(currentState, currentScreen);
    }


    /**
     * 销毁进度对话框
     */
    @Override
    public void dismissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    /**
     * 销毁音量对话框
     */
    @Override
    public void dismissVolumeDialog() {
        if (mVolumeDialog != null) {
            mVolumeDialog.dismiss();
        }
    }

    /**
     * 销毁亮度对话框
     */
    @Override
    public void dismissBrightnessDialog() {
        if (mBrightDialog != null) {
            mBrightDialog.dismiss();
        }
    }

    @Override
    public void showWifiDialog(int event) {

    }

    public int getDuration() {
        int duration = 0;
        try {
            duration = EasyMediaManager.getDuration();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            return duration;
        }
        return duration;
    }

    //获取当前播放位置
    @Override
    public int getCurrentPositionWhenPlaying() {
        int position = 0;
        if (currentState == CURRENT_STATE_PLAYING ||
                currentState == CURRENT_STATE_PAUSE) {
            try {
                position = EasyMediaManager.getCurrentPosition();
            } catch (IllegalStateException e) {
                e.printStackTrace();
                return position;
            }
        }
        return position;
    }


    public int getCurrentScreen() {
        return currentScreen;
    }


    //点击时候显示控制器（3秒后消失）
    @Override
    public void startDismissControlViewSchedule() {
        cancelDismissControlViewSchedule();
        viewHolder.showControllerViewAnim(currentState, true);
        showControllerFuture = MediaUtils.POOL_SCHEDULE().schedule(new DismissControlViewRunnable(), 3000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void cancelDismissControlViewSchedule() {
        if (showControllerFuture != null && !showControllerFuture.isCancelled()) {
            showControllerFuture.cancel(true);
            showControllerFuture = null;
        }
    }


    public class DismissControlViewRunnable extends TimerTask {

        @Override
        public void run() {
            viewHolder.showControllerViewAnim(currentState, false);
        }
    }

    @Override
    public void setCurrentScreen(int currentScreen) {
        this.currentScreen = currentScreen;
    }

    @Override
    public void setCurrentState(int currentState) {
        this.currentState = currentState;
        //跟新ui
        viewHolder.changUi(currentState, currentScreen);
    }

    //设置进度最大
    @Override
    public void setMaxProgressAndTime() {
        viewHolder.bottomContainer.setMaxProgressAndTime();
    }

    //设置进度缓存
    @Override
    public void setBufferProgress(int bufferProgress) {
        if (bufferProgress != 0) {
            viewHolder.bottomContainer.setBufferProgress(bufferProgress);
        }
    }

    //获取进度缓存
    @Override
    public int getBufferProgress() {
        return viewHolder.bottomContainer.getBufferProgress();
    }

}
