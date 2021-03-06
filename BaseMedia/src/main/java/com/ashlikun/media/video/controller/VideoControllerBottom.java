package com.ashlikun.media.video.controller;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.ashlikun.media.R;
import com.ashlikun.media.video.EasyMediaManager;
import com.ashlikun.media.video.EasyVideoAction;
import com.ashlikun.media.video.EasyVideoPlayerManager;
import com.ashlikun.media.video.VideoUtils;
import com.ashlikun.media.video.status.VideoStatus;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 作者　　: 李坤
 * 创建时间: 2017/12/7　15:25
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：控制器底部
 */

public class VideoControllerBottom extends LinearLayout implements SeekBar.OnSeekBarChangeListener {
    protected ScheduledFuture progressFuture;
    //下面可触摸进度条
    public SeekBar progressBar;
    //全屏按钮
    public ImageView fullscreenButton;
    //进度文本
    public TextView currentTimeTextView, totalTimeTextView;

    public OnEventListener onEventListener;
    boolean fullEnable = true;
    //是否是全屏状态
    boolean isFull = false;

    public void setOnEventListener(OnEventListener onEventListener) {
        this.onEventListener = onEventListener;
    }

    public VideoControllerBottom(Context context) {
        this(context, null);
    }

    public VideoControllerBottom(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoControllerBottom(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        LayoutInflater.from(getContext()).inflate(R.layout.easy_video_layout_controller_bottom, this);
        setBackgroundResource(R.drawable.easy_video_bottom_bg);
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

    public void setFull(boolean isFull) {
        if (!fullEnable) {
            return;
        }
        this.isFull = isFull;
        if (isFull) {
            fullscreenButton.setImageResource(R.drawable.easy_video_shrink);
        } else {
            fullscreenButton.setImageResource(R.drawable.easy_video_enlarge);
        }
    }

    public void setFullEnable(boolean enable) {
        fullEnable = enable;
        fullscreenButton.setVisibility(enable ? VISIBLE : GONE);
    }

    /**
     * 设置进度  如果2个值都是100，就会设置最大值，如果某个值<0 就不设置
     *
     * @param progress          主进度
     * @param secondaryProgress 缓存进度
     */
    public void setProgress(int progress, int secondaryProgress) {
        if (progress >= 0) {
            progressBar.setProgress(progress);
        }
        if (secondaryProgress >= 0) {
            progressBar.setSecondaryProgress(secondaryProgress);
        }
        if (progress >= progressBar.getMax() && secondaryProgress >= progressBar.getMax()) {
            currentTimeTextView.setText(totalTimeTextView.getText());
        }
    }

    public int getBufferProgress() {
        return progressBar.getSecondaryProgress();
    }

    /**
     * 设置时间
     *
     * @param position 0：重置
     * @param duration 0：重置
     */
    public void setTime(long position, long duration) {
        currentTimeTextView.setText(VideoUtils.stringForTime(position));
        totalTimeTextView.setText(VideoUtils.stringForTime(duration));
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
        EasyVideoPlayerManager.getCurrentVideoPlayerNoTiny().onEvent(EasyVideoAction.ON_SEEK_POSITION);
        ViewParent vpup = getParent();
        while (vpup != null) {
            vpup.requestDisallowInterceptTouchEvent(false);
            vpup = vpup.getParent();
        }
        if (EasyVideoPlayerManager.getCurrentVideoPlayerNoTiny().getCurrentState() != VideoStatus.PLAYING &&
                EasyVideoPlayerManager.getCurrentVideoPlayerNoTiny().getCurrentState() != VideoStatus.PAUSE) {
            return;
        }
        int time = (int) (seekBar.getProgress() * getDuration() / 100.0);
        EasyMediaManager.seekTo(time);
        startProgressSchedule();
        if (onEventListener != null) {
            onEventListener.onStopTrackingTouch(seekBar);
        }
    }

    private long getDuration() {
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
     * 开始进度定时器
     */
    public void startProgressSchedule() {
        stopProgressSchedule();
        progressFuture = VideoUtils.POOL_SCHEDULE().scheduleWithFixedDelay(new ProgressRunnable(), 0, 300, TimeUnit.MILLISECONDS);
    }

    public boolean isFull() {
        return isFull;
    }

    /**
     * 取消进度定时器
     */
    public void stopProgressSchedule() {
        if (progressFuture != null && !progressFuture.isCancelled()) {
            progressFuture.cancel(true);
            progressFuture = null;
        }
    }

    private class ProgressRunnable implements Runnable {
        @Override
        public void run() {
            if (EasyVideoPlayerManager.getCurrentVideoPlayerNoTiny().getCurrentState() == VideoStatus.PLAYING
                    || EasyVideoPlayerManager.getCurrentVideoPlayerNoTiny().getCurrentState() == VideoStatus.PAUSE) {
                VideoUtils.getMainHander().post(new Runnable() {
                    @Override
                    public void run() {
                        long position = 0;
                        int bufferedPercentage = 0;
                        try {
                            position = EasyMediaManager.getCurrentPosition();
                            bufferedPercentage = EasyMediaManager.getBufferedPercentage();
                        } catch (IllegalStateException e) {
                            e.printStackTrace();
                        }
                        long duration = getDuration();
                        int progress = (int) (position * 100f / (duration == 0 ? 1 : duration));
                        if (progress >= 0) {
                            if (onEventListener != null) {
                                onEventListener.onProgressChang(progress, bufferedPercentage);
                            } else {
                                setProgress(progress, -1);
                            }
                        }
                        setTime(position, duration);
                    }
                });
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopProgressSchedule();
    }

    public interface OnEventListener {
        void onStartTrackingTouch(SeekBar seekBar);

        void onStopTrackingTouch(SeekBar seekBar);

        void onFullscreenClick();

        void onProgressChang(int progress, int secondaryProgress);
    }
}
