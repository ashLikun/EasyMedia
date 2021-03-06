package com.ashlikun.media.video.controller;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ashlikun.media.R;
import com.ashlikun.media.video.VideoData;
import com.ashlikun.media.video.VideoUtils;
import com.ashlikun.media.video.status.VideoStatus;
import com.ashlikun.media.video.view.EasyLoaddingView;


/**
 * 作者　　: 李坤
 * 创建时间: 2017/12/5　16:46
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：控制器的holder
 */

public class EasyControllerViewHolder {
    public int iconPlay = R.drawable.easy_video_click_play_selector;
    public int iconReplay = R.drawable.easy_video_click_replay_selector;
    public int iconPause = R.drawable.easy_video_click_pause_selector;
    ViewGroup viewGroup;
    //开始按钮
    public ImageView startButton;
    //顶部控制器和底部控制器
    public VideoControllerTop topContainer;
    public VideoControllerBottom bottomContainer;
    //重新加载view
    public LinearLayout mRetryLayout;
    //进度条
    public View easyLoaddingView;
    //是否是全屏播放
    public boolean isFull = false;
    //是否只在全屏的时候显示标题和顶部
    public boolean isOnlyFullShowTitle = false;
    //最下面的进度条
    ProgressBar bottomProgressBar;
    //未播放时候占位图
    public ImageView thumbImageView;
    //从新播放
    public TextView replayTextView;
    //当前播放状态
    @VideoStatus.Code
    public int currentState = VideoStatus.NORMAL;
    AnimatorSet animatorSet = new AnimatorSet();
    boolean isCurrentAnimHint = true;
    //之前是否是准备状态
    private boolean isBeforeStatePreparing = false;

    public EasyControllerViewHolder(ViewGroup viewGroup, View.OnClickListener clickListener, VideoControllerBottom.OnEventListener onEventListener) {
        initAnimator();
        this.viewGroup = viewGroup;
        startButton = viewGroup.findViewById(R.id.start);
        bottomContainer = viewGroup.findViewById(R.id.controllerBottom);
        topContainer = viewGroup.findViewById(R.id.controllerTop);
        mRetryLayout = viewGroup.findViewById(R.id.retry_layout);
        easyLoaddingView = viewGroup.findViewById(R.id.loading);
        thumbImageView = viewGroup.findViewById(R.id.thumb);
        replayTextView = viewGroup.findViewById(R.id.replay_text);
        bottomProgressBar = viewGroup.findViewById(R.id.bottom_progress);
        thumbImageView.setOnClickListener(clickListener);
        startButton.setOnClickListener(clickListener);
        bottomContainer.setOnEventListener(onEventListener);
        viewGroup.findViewById(R.id.retry_btn).setOnClickListener(clickListener);
        changeUiToNormal();
        updateStartImage(currentState);
        bottomContainer.stopProgressSchedule();
    }


    public void setControllFullEnable(boolean fullEnable) {
        bottomContainer.setFullEnable(fullEnable);
    }

    /**
     * 根据状态改变ui
     *
     * @param currentState
     */
    public void changUi(@VideoStatus.Code int currentState) {
        if (this.currentState == currentState) {
            return;
        }
        this.currentState = currentState;
        if (currentState == VideoStatus.PREPARING) {
            isBeforeStatePreparing = true;
        }
        //默认
        if (currentState == VideoStatus.NORMAL) {
            changeUiToNormal();
            bottomContainer.stopProgressSchedule();
        }
        //准备
        else if (currentState == VideoStatus.PREPARING) {
            changeUiToPreparing();
        }
        //播放中
        else if (currentState == VideoStatus.PLAYING) {
            changeUiToPlaying();
            startProgressSchedule();
        }
        //暂停
        else if (currentState == VideoStatus.PAUSE) {
            changeUiToPause();
            stopProgressSchedule();
        }
        //自动完成了
        else if (currentState == VideoStatus.AUTO_COMPLETE) {
            changeUiToComplete();
            bottomContainer.stopProgressSchedule();
        }
        //开始缓冲
        else if (currentState == VideoStatus.BUFFERING_START) {
            changeUiToBufferStart();
        }
        //错误
        else if (currentState == VideoStatus.ERROR) {
            changeUiToError();
            bottomContainer.stopProgressSchedule();
        }
        updateStartImage(currentState);
    }

    public void setFull(boolean full) {
        isFull = full;
        bottomContainer.setFull(isFull);
        topContainer.setFull(isFull);
        if (isFull) {
            changeStartButtonSize((int) viewGroup.getResources().getDimension(R.dimen.easy_video_start_button_w_h_fullscreen));
        } else {
            changeStartButtonSize((int) viewGroup.getResources().getDimension(R.dimen.easy_video_start_button_w_h_normal));
        }
    }

    public void setOnlyFullShowTitle(boolean onlyFullShowTitle) {
        isOnlyFullShowTitle = onlyFullShowTitle;
        if (isOnlyFullShowTitle && !isFull) {
            topContainer.setVisibility(View.GONE);
        }
    }

    public void setDataSource(VideoData mediaDatan) {
        topContainer.setInitData(mediaDatan);
    }


    /**
     * 改变播放按钮的大小
     */
    public void changeStartButtonSize(int size) {
        startButton.setMinimumWidth(size);
        startButton.setMinimumHeight(size);
    }


    /**
     * 准备中
     */
    public void changeUiToPreparing() {
        bottomContainer.setTime(0, 0);
        hintContainer(false);
        setMinControlsVisiblity(false, true, true, false, false);
        if (easyLoaddingView instanceof EasyLoaddingView) {
            EasyLoaddingView view = (EasyLoaddingView) easyLoaddingView;
            if (view.getCurrentState() == EasyLoaddingView.STATE_PRE) {
                view.start();
            }
        }
    }

    /**
     * 开始缓冲
     */
    public void changeUiToBufferStart() {
        setMinControlsVisiblity(false, true, false, true, false);
        if (easyLoaddingView instanceof EasyLoaddingView) {
            EasyLoaddingView view = (EasyLoaddingView) easyLoaddingView;
            if (view.getCurrentState() == EasyLoaddingView.STATE_PRE) {
                view.start();
            }
        }
    }

    /**
     * 改变ui到默认状态
     */
    protected void changeUiToNormal() {
        //这边加动画，在低版本手机会卡顿，主要是列表里每次都会走这个方法
        hintContainer(false);
        setMinControlsVisiblity(true, false, true, false, false);
        if (easyLoaddingView instanceof EasyLoaddingView) {
            ((EasyLoaddingView) easyLoaddingView).reset();
        }
    }

    /**
     * 改变ui成完成
     */
    protected void changeUiToComplete() {
        if (isOnlyFullShowTitle && !isFull) {
            topContainer.setVisibility(View.GONE);
        } else {
            topContainer.setVisibility(View.VISIBLE);
        }
        bottomContainer.setVisibility(View.GONE);
        setMinControlsVisiblity(true, false, false, false, false);
        if (easyLoaddingView instanceof EasyLoaddingView) {
            ((EasyLoaddingView) easyLoaddingView).reset();
        }
    }

    /**
     * 改变ui错误
     */
    protected void changeUiToError() {
        if (isFull) {
            topContainer.setVisibility(View.VISIBLE);
            bottomContainer.setVisibility(View.GONE);
        } else {
            hintContainer(false);
        }
        setMinControlsVisiblity(true, false, false, false, true);
    }

    /**
     * 播放
     */
    protected void changeUiToPlaying() {
        hintContainer(!isBeforeStatePreparing && containerIsShow());
        isBeforeStatePreparing = false;
        setMinControlsVisiblity(false, false, false, true, false);
        if (easyLoaddingView instanceof EasyLoaddingView) {
            ((EasyLoaddingView) easyLoaddingView).reset();
        }
    }

    /**
     * 暂停状态
     */
    protected void changeUiToPause() {
        showContainer(!containerIsShow());
        setMinControlsVisiblity(true, false, false, false, false);
        if (easyLoaddingView instanceof EasyLoaddingView) {
            ((EasyLoaddingView) easyLoaddingView).reset();
        }
    }


    /**
     * 设置其他小控件
     *
     * @param startBtn
     * @param loadingPro
     * @param thumbImg
     * @param bottomPrp
     * @param retryLayout
     */
    protected void setMinControlsVisiblity(boolean startBtn, boolean loadingPro,
                                           boolean thumbImg, boolean bottomPrp, boolean retryLayout) {
        startButton.setVisibility(startBtn ? View.VISIBLE : View.GONE);
        easyLoaddingView.setVisibility(loadingPro ? View.VISIBLE : View.GONE);
        thumbImageView.setVisibility(thumbImg ? View.VISIBLE : View.GONE);
        mRetryLayout.setVisibility(retryLayout ? View.VISIBLE : View.GONE);
        bottomProgressBar.setVisibility(bottomPrp ? View.VISIBLE : View.GONE);
    }

    /**
     * 更新开始的按钮图片
     *
     * @param currentState
     */
    public void updateStartImage(@VideoStatus.Code int currentState) {
        if (currentState == VideoStatus.PLAYING) {
            startButton.setImageResource(iconPause);
            replayTextView.setVisibility(View.GONE);
        } else if (currentState == VideoStatus.ERROR) {
            startButton.setVisibility(View.INVISIBLE);
            replayTextView.setVisibility(View.GONE);
        } else if (currentState == VideoStatus.AUTO_COMPLETE) {
            startButton.setImageResource(iconReplay);
            replayTextView.setVisibility(View.VISIBLE);
        } else {
            startButton.setImageResource(iconPlay);
            replayTextView.setVisibility(View.GONE);
        }
    }

    /**
     * 清空全部ui展示
     */
    public void changeUiToClean() {
        if (!animatorSet.isRunning()) {
            hintContainer(true);
        }
        setMinControlsVisiblity(false, false, false, false, false);
    }


    public boolean containerIsShow() {
        return bottomContainer.getVisibility() == View.VISIBLE || topContainer.getVisibility() == View.VISIBLE;
    }

    /**
     * 开始进度定时器
     */
    public void startProgressSchedule() {
        bottomContainer.startProgressSchedule();
    }

    /**
     * 取消进度定时器
     */
    public void stopProgressSchedule() {
        bottomContainer.stopProgressSchedule();
    }

    /**
     * 设置进度  如果2个值都是100，就会设置最大值，如果某个值<0 就不设置
     *
     * @param progress          主进度
     * @param secondaryProgress 缓存进度
     */
    public void setProgress(int progress, int secondaryProgress) {
        if (progress >= 0) {
            bottomProgressBar.setProgress(progress);
        }
        if (secondaryProgress >= 0) {
            bottomProgressBar.setSecondaryProgress(secondaryProgress);
        }
        bottomContainer.setProgress(progress, secondaryProgress);
    }

    public ImageView getThumbImageView() {
        return thumbImageView;
    }

    public int getBufferProgress() {
        return bottomContainer.getBufferProgress();
    }

    public void setTime(long position, long duration) {
        bottomContainer.setTime(position, duration);
    }

    /**
     * 显示或者隐藏顶部和底部控制器
     */
    public void showControllerViewAnim(@VideoStatus.Code final int currentState, final boolean isShow) {
        if (currentState != VideoStatus.NORMAL
                && currentState != VideoStatus.ERROR
                && currentState != VideoStatus.AUTO_COMPLETE) {

            VideoUtils.getMainHander().post(new Runnable() {
                @Override
                public void run() {
                    if (isShow) {
                        showContainer(true);
                        startButton.setVisibility(View.VISIBLE);
                        bottomProgressBar.setVisibility(View.GONE);
                    } else {
                        hintContainer(true);
                        startButton.setVisibility(View.GONE);
                        bottomProgressBar.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
    }

    private void initAnimator() {
        animatorSet.setDuration(300);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (isCurrentAnimHint) {
                    hintContainer(false);
                }
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                showContainer(false);
            }
        });
    }

    private void hintContainer(boolean isAnim) {
        if (!isAnim) {
            topContainer.setVisibility(View.GONE);
            bottomContainer.setVisibility(View.GONE);
        } else {
            if (topContainer.getVisibility() == View.VISIBLE || bottomContainer.getVisibility() == View.VISIBLE) {
                animatorSet.cancel();
                isCurrentAnimHint = true;
                ObjectAnimator animatorTop = ObjectAnimator.ofFloat(topContainer, "translationY",
                        0, -topContainer.getHeight());
                ObjectAnimator animatoBottom = ObjectAnimator.ofFloat(bottomContainer, "translationY",
                        0, bottomContainer.getHeight());
                animatorSet.play(animatorTop).with(animatoBottom);
                animatorSet.start();
            }
        }
    }

    private void showContainer(boolean isAnim) {
        if (!isAnim) {
            if (isOnlyFullShowTitle && !isFull) {
                topContainer.setVisibility(View.GONE);
            } else {
                topContainer.setVisibility(View.VISIBLE);
            }
            bottomContainer.setVisibility(View.VISIBLE);
        } else {
            if (topContainer.getVisibility() == View.GONE || bottomContainer.getVisibility() == View.GONE) {
                animatorSet.cancel();
                isCurrentAnimHint = false;
                ObjectAnimator animatorTop = ObjectAnimator.ofFloat(topContainer, "translationY",
                        -topContainer.getHeight(), 0);
                ObjectAnimator animatoBottom = ObjectAnimator.ofFloat(bottomContainer, "translationY",
                        bottomContainer.getHeight(), 0);
                animatorSet.play(animatorTop).with(animatoBottom);
                animatorSet.start();
            }
        }
    }

    /**
     * 非全屏时候返回键隐藏的时候预留左边空间
     *
     * @param backGoneLeftSize
     */
    public void setBackGoneLeftSize(int backGoneLeftSize) {
        topContainer.setBackGoneLeftSize(backGoneLeftSize);
    }


}
