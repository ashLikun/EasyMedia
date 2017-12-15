package com.ashlikun.media.controller;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.ashlikun.media.EasyMediaAction;
import com.ashlikun.media.EasyMediaManager;
import com.ashlikun.media.EasyVideoPlayerManager;
import com.ashlikun.media.MediaUtils;
import com.ashlikun.media.R;
import com.ashlikun.media.status.MediaStatus;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.ashlikun.media.status.MediaScreenStatus.SCREEN_WINDOW_FULLSCREEN;
import static com.ashlikun.media.status.MediaScreenStatus.SCREEN_WINDOW_LIST;
import static com.ashlikun.media.status.MediaScreenStatus.SCREEN_WINDOW_NORMAL;
import static com.ashlikun.media.status.MediaScreenStatus.SCREEN_WINDOW_TINY;
import static com.ashlikun.media.status.MediaStatus.CURRENT_STATE_PAUSE;
import static com.ashlikun.media.status.MediaStatus.CURRENT_STATE_PLAYING;

/**
 * 作者　　: 李坤
 * 创建时间: 2017/12/7　15:25
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：控制器底部
 */

public class MediaControllerBottom extends LinearLayout implements SeekBar.OnSeekBarChangeListener {
    protected ScheduledFuture progressFuture;
    //下面可触摸进度条
    public SeekBar progressBar;
    //全屏按钮
    public ImageView fullscreenButton;
    //进度文本
    public TextView currentTimeTextView, totalTimeTextView;

    public OnEventListener onEventListener;

    public void setOnEventListener(OnEventListener onEventListener) {
        this.onEventListener = onEventListener;
    }

    public MediaControllerBottom(Context context) {
        this(context, null);
    }

    public MediaControllerBottom(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MediaControllerBottom(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        LayoutInflater.from(getContext()).inflate(R.layout.easy_layout_controller_bottom, this);
        setBackgroundResource(R.drawable.easy_media_bottom_bg);
        progressBar = findViewById(R.id.bottom_seek_progress);
        fullscreenButton = findViewById(R.id.fullscreen);
        currentTimeTextView = findViewById(R.id.current);
        totalTimeTextView = findViewById(R.id.total);
        progressBar.setOnSeekBarChangeListener(this);
        fullscreenButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onEventListener != null) {
                    onEventListener.onFullscreenClick();
                }
            }
        });
    }

    public void setInitData(Object[] dataSource, int screen) {
        if (screen == SCREEN_WINDOW_FULLSCREEN) {
            setIsFull(true);
        } else if (screen == SCREEN_WINDOW_NORMAL || screen == SCREEN_WINDOW_LIST) {
            setIsFull(false);
        } else if (screen == SCREEN_WINDOW_TINY) {
            setIsFull(false);
        }
    }

    public void setIsFull(boolean isFull) {
        if (isFull) {
            fullscreenButton.setImageResource(R.drawable.easy_media_shrink);
        } else {
            fullscreenButton.setImageResource(R.drawable.easy_media_enlarge);
        }
    }

    //重置进度条
    public void resetProgressAndTime() {
        progressBar.setProgress(0);
        progressBar.setSecondaryProgress(0);
        currentTimeTextView.setText(MediaUtils.stringForTime(0));
        totalTimeTextView.setText(MediaUtils.stringForTime(0));
    }

    //设置最大进度
    public void setMaxProgressAndTime() {
        progressBar.setProgress(100);
        progressBar.setSecondaryProgress(100);
        currentTimeTextView.setText(totalTimeTextView.getText());
    }

    public void setProgress(int progress) {
        progressBar.setProgress(progress);
    }

    public void setBufferProgress(int progress) {
        progressBar.setSecondaryProgress(progress);
    }

    public int getBufferProgress() {
        return progressBar.getSecondaryProgress();
    }

    public void setTime(int position, int duration) {
        if (position != 0) {
            currentTimeTextView.setText(MediaUtils.stringForTime(position));
        }
        totalTimeTextView.setText(MediaUtils.stringForTime(duration));
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        ViewParent vpdown = getParent();
        while (vpdown != null) {
            vpdown.requestDisallowInterceptTouchEvent(true);
            vpdown = vpdown.getParent();
        }
        stopProgressSchedule();
        if (onEventListener != null) {
            onEventListener.onStartTrackingTouch(seekBar);
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        EasyVideoPlayerManager.getCurrentVideoPlayer().onEvent(EasyMediaAction.ON_SEEK_POSITION);
        ViewParent vpup = getParent();
        while (vpup != null) {
            vpup.requestDisallowInterceptTouchEvent(false);
            vpup = vpup.getParent();
        }
        if (EasyVideoPlayerManager.getCurrentVideoPlayer().currentState != MediaStatus.CURRENT_STATE_PLAYING &&
                EasyVideoPlayerManager.getCurrentVideoPlayer().currentState != MediaStatus.CURRENT_STATE_PAUSE) {
            return;
        }
        int time = (int) (seekBar.getProgress() * getDuration() / 100.0);
        Log.e("aaaa", "当前播放时间 = " + time + "    播放百分比" + seekBar.getProgress());
        EasyMediaManager.seekTo(time);
        startProgressSchedule();
        if (onEventListener != null) {
            onEventListener.onStopTrackingTouch(seekBar);
        }
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

    //开始进度定时器
    public void startProgressSchedule() {
        stopProgressSchedule();
        progressFuture = MediaUtils.POOL_SCHEDULE().scheduleWithFixedDelay(new ProgressRunnable(), 0, 300, TimeUnit.MILLISECONDS);
    }

    //取消进度定时器
    public void stopProgressSchedule() {
        if (progressFuture != null && !progressFuture.isCancelled()) {
            progressFuture.cancel(true);
            progressFuture = null;
        }
    }

    public class ProgressRunnable implements Runnable {
        @Override
        public void run() {
            if (EasyVideoPlayerManager.getCurrentVideoPlayer().currentState == CURRENT_STATE_PLAYING || EasyVideoPlayerManager.getCurrentVideoPlayer().currentState == CURRENT_STATE_PAUSE) {
                MediaUtils.getMainHander().post(new Runnable() {
                    @Override
                    public void run() {
                        int position = 0;
                        try {
                            position = EasyMediaManager.getCurrentPosition();
                        } catch (IllegalStateException e) {
                            e.printStackTrace();
                        }
                        int duration = getDuration();
                        int progress = (int) (position * 100f / (duration == 0 ? 1 : duration));
                        if (progress != 0) {
                            setProgress(progress);
                            if (onEventListener != null) {
                                onEventListener.onProgressChang(progress);
                            }
                        }
                        setTime(position, duration);
                    }
                });
            }
        }
    }

    public interface OnEventListener {
        void onStartTrackingTouch(SeekBar seekBar);

        void onStopTrackingTouch(SeekBar seekBar);

        void onFullscreenClick();

        void onProgressChang(int progress);
    }
}
