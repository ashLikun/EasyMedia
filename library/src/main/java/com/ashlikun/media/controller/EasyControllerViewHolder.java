package com.ashlikun.media.controller;

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

import com.ashlikun.media.EasyVideoPlayerManager;
import com.ashlikun.media.MediaData;
import com.ashlikun.media.MediaUtils;
import com.ashlikun.media.R;
import com.ashlikun.media.status.MediaScreenStatus;
import com.ashlikun.media.status.MediaStatus;

import static com.ashlikun.media.status.MediaScreenStatus.SCREEN_WINDOW_FULLSCREEN;
import static com.ashlikun.media.status.MediaScreenStatus.SCREEN_WINDOW_LIST;
import static com.ashlikun.media.status.MediaScreenStatus.SCREEN_WINDOW_NORMAL;
import static com.ashlikun.media.status.MediaScreenStatus.SCREEN_WINDOW_TINY;
import static com.ashlikun.media.status.MediaStatus.CURRENT_STATE_AUTO_COMPLETE;
import static com.ashlikun.media.status.MediaStatus.CURRENT_STATE_ERROR;
import static com.ashlikun.media.status.MediaStatus.CURRENT_STATE_NORMAL;
import static com.ashlikun.media.status.MediaStatus.CURRENT_STATE_PAUSE;
import static com.ashlikun.media.status.MediaStatus.CURRENT_STATE_PLAYING;
import static com.ashlikun.media.status.MediaStatus.CURRENT_STATE_PREPARING;
import static com.ashlikun.media.status.MediaStatus.CURRENT_STATE_PREPARING_CHANGING_URL;

/**
 * 作者　　: 李坤
 * 创建时间: 2017/12/5　16:46
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：控制器的holder
 */

public class EasyControllerViewHolder implements IControllerViewHolder {
    ViewGroup viewGroup;
    //开始按钮
    public ImageView startButton;
    //顶部控制器和底部控制器
    public MediaControllerTop topContainer;
    public MediaControllerBottom bottomContainer;
    //重新加载view
    public LinearLayout mRetryLayout;
    //进度条
    public ProgressBar loadingProgressBar;
    //最下面的进度条
    ProgressBar bottomProgressBar;
    //未播放时候占位图
    public ImageView thumbImageView;
    //小窗口的退出按钮
    public ImageView backTiny;
    //从新播放
    public TextView replayTextView;
    //当前屏幕方向
    @MediaScreenStatus.Code
    public int currentScreen = MediaScreenStatus.SCREEN_WINDOW_NORMAL;
    //当前播放状态
    @MediaStatus.Code
    public int currentState = MediaStatus.CURRENT_STATE_NORMAL;
    AnimatorSet animatorSet = new AnimatorSet();
    boolean isCurrentAnimHint;
    //之前是否是准备状态
    private boolean isBeforeStatePreparing = false;

    public EasyControllerViewHolder(ViewGroup viewGroup, View.OnClickListener clickListener, MediaControllerBottom.OnEventListener onEventListener) {
        initAnimator();
        this.viewGroup = viewGroup;
        startButton = viewGroup.findViewById(R.id.start);
        bottomContainer = viewGroup.findViewById(R.id.controllerBottom);
        topContainer = viewGroup.findViewById(R.id.controllerTop);
        mRetryLayout = viewGroup.findViewById(R.id.retry_layout);
        loadingProgressBar = viewGroup.findViewById(R.id.loading);
        thumbImageView = viewGroup.findViewById(R.id.thumb);
        replayTextView = viewGroup.findViewById(R.id.replay_text);
        bottomProgressBar = viewGroup.findViewById(R.id.bottom_progress);
        backTiny = viewGroup.findViewById(R.id.back_tiny);
        thumbImageView.setOnClickListener(clickListener);
        startButton.setOnClickListener(clickListener);
        bottomContainer.setOnEventListener(onEventListener);
        viewGroup.findViewById(R.id.retry_btn).setOnClickListener(clickListener);
        changeUiToNormal();
        bottomContainer.stopProgressSchedule();
        backTiny.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (EasyVideoPlayerManager.getFirstFloor().currentScreen == SCREEN_WINDOW_LIST) {
                    //只清空小窗口
                    MediaUtils.quitFullscreenOrTinyWindow();
                } else {
                    //退出小窗口并且之前的继续播放
                    MediaUtils.backPress();
                }
            }
        });
    }


    @Override
    public void setControllFullEnable(boolean fullEnable) {
        bottomContainer.setFullEnable(fullEnable);
    }

    //根据状态改变ui
    @Override
    public void changUi(@MediaStatus.Code int currentState, @MediaScreenStatus.Code int currentScreen) {
        if (this.currentState == currentState && this.currentScreen == currentScreen) {
            return;
        }
        this.currentState = currentState;
        this.currentScreen = currentScreen;
        if (currentState == CURRENT_STATE_PREPARING || currentState == CURRENT_STATE_PREPARING_CHANGING_URL) {
            isBeforeStatePreparing = true;
        }
        backTiny.setVisibility(currentScreen == SCREEN_WINDOW_TINY ? View.VISIBLE : View.GONE);
        //小窗口隐藏顶部和顶部控制器
        if (currentScreen == SCREEN_WINDOW_TINY) {
            hintContainer(false);
        }
        //默认
        if (currentState == CURRENT_STATE_NORMAL) {
            changeUiToNormal();
            bottomContainer.stopProgressSchedule();
        }
        //准备
        else if (currentState == CURRENT_STATE_PREPARING) {
            changeUiToPreparing();
        }
        //准备改变播放的url
        else if (currentState == CURRENT_STATE_PREPARING_CHANGING_URL) {
            loadingProgressBar.setVisibility(View.VISIBLE);
            startButton.setVisibility(View.GONE);
        }
        //播放中
        else if (currentState == CURRENT_STATE_PLAYING) {
            changeUiToPlaying();
            startProgressSchedule();
        }
        //暂停
        else if (currentState == CURRENT_STATE_PAUSE) {
            changeUiToPause();
            stopProgressSchedule();
        }
        //自动完成了
        else if (currentState == CURRENT_STATE_AUTO_COMPLETE) {
            changeUiToComplete();
            bottomContainer.stopProgressSchedule();
        }
        //错误
        else if (currentState == CURRENT_STATE_ERROR) {
            changeUiToError();
            bottomContainer.stopProgressSchedule();
        }
        updateStartImage(currentState);
    }

    @Override
    public void setDataSource(MediaData mediaData, int screen) {
        bottomContainer.setInitData(screen);
        topContainer.setInitData(mediaData, screen);
        if (screen == SCREEN_WINDOW_FULLSCREEN) {
            changeStartButtonSize((int) viewGroup.getResources().getDimension(R.dimen.media_start_button_w_h_fullscreen));
        } else if (screen == SCREEN_WINDOW_NORMAL || screen == SCREEN_WINDOW_LIST) {
            changeStartButtonSize((int) viewGroup.getResources().getDimension(R.dimen.media_start_button_w_h_normal));
        } else if (screen == SCREEN_WINDOW_TINY) {
            changeStartButtonSize((int) viewGroup.getResources().getDimension(R.dimen.media_start_button_w_h_normal));
        }
    }


    //改变播放按钮的大小
    public void changeStartButtonSize(int size) {
        startButton.setMinimumWidth(size);
        startButton.setMinimumHeight(size);
    }


    //准备中
    public void changeUiToPreparing() {
        bottomContainer.setTime(0, 0);
        switch (currentScreen) {
            case SCREEN_WINDOW_NORMAL:
            case SCREEN_WINDOW_LIST:
            case SCREEN_WINDOW_FULLSCREEN:
                hintContainer(false);
                setMinControlsVisiblity(false, true, true, false, false);
                break;
            case SCREEN_WINDOW_TINY:
                setMinControlsVisiblity(false, true, true, false, false);
                break;
        }
    }


    //改变ui到默认状态
    private void changeUiToNormal() {
        switch (currentScreen) {
            case MediaScreenStatus.SCREEN_WINDOW_NORMAL:
            case MediaScreenStatus.SCREEN_WINDOW_LIST:
            case SCREEN_WINDOW_FULLSCREEN:
                //这边加动画，在低版本手机会卡顿，主要是列表里每次都会走这个方法
                hintContainer(false);
                setMinControlsVisiblity(true, false, true, false, false);
                break;
            case SCREEN_WINDOW_TINY:
                setMinControlsVisiblity(true, false, false, false, false);
                break;
        }
    }

    //改变ui成完成
    private void changeUiToComplete() {
        switch (currentScreen) {
            case SCREEN_WINDOW_NORMAL:
            case SCREEN_WINDOW_LIST:
            case SCREEN_WINDOW_FULLSCREEN:
                topContainer.setVisibility(View.VISIBLE);
                bottomContainer.setVisibility(View.GONE);
                setMinControlsVisiblity(true, false, true, false, false);
                break;
            case SCREEN_WINDOW_TINY:
                break;
        }
    }

    //改变ui错误
    private void changeUiToError() {
        switch (currentScreen) {
            case SCREEN_WINDOW_NORMAL:
            case SCREEN_WINDOW_LIST:
                hintContainer(false);
                setMinControlsVisiblity(true, false, false, false, true);
                break;
            case SCREEN_WINDOW_FULLSCREEN:
                topContainer.setVisibility(View.VISIBLE);
                bottomContainer.setVisibility(View.GONE);
                setMinControlsVisiblity(true, false, false, false, true);
                break;
            case SCREEN_WINDOW_TINY:
                break;
        }
    }

    //播放
    private void changeUiToPlaying() {
        switch (currentScreen) {
            case SCREEN_WINDOW_NORMAL:
            case SCREEN_WINDOW_LIST:
            case SCREEN_WINDOW_FULLSCREEN:
                hintContainer(!isBeforeStatePreparing && containerIsShow());
                isBeforeStatePreparing = false;
                setMinControlsVisiblity(false, false, false, true, false);
                break;
            case SCREEN_WINDOW_TINY:
                setMinControlsVisiblity(false, false, false, true, false);
                break;
        }
    }

    //暂停状态
    private void changeUiToPause() {
        switch (currentScreen) {
            case SCREEN_WINDOW_NORMAL:
            case SCREEN_WINDOW_LIST:
            case SCREEN_WINDOW_FULLSCREEN:
                showContainer(!containerIsShow());
                setMinControlsVisiblity(true, false, false, false, false);
                break;
            case SCREEN_WINDOW_TINY:
                setMinControlsVisiblity(true, false, false, true, false);
                break;
        }
    }


    //设置其他小控件
    private void setMinControlsVisiblity(boolean startBtn, boolean loadingPro,
                                         boolean thumbImg, boolean bottomPrp, boolean retryLayout) {
        startButton.setVisibility(startBtn ? View.VISIBLE : View.GONE);
        loadingProgressBar.setVisibility(loadingPro ? View.VISIBLE : View.GONE);
        thumbImageView.setVisibility(thumbImg ? View.VISIBLE : View.GONE);
        mRetryLayout.setVisibility(retryLayout ? View.VISIBLE : View.GONE);
        bottomProgressBar.setVisibility(bottomPrp ? View.VISIBLE : View.GONE);
    }

    //跟新开始的按钮图片
    private void updateStartImage(@MediaStatus.Code int currentState) {
        if (currentState == CURRENT_STATE_PLAYING) {
            startButton.setImageResource(R.drawable.easy_media_click_pause_selector);
            replayTextView.setVisibility(View.GONE);
        } else if (currentState == MediaStatus.CURRENT_STATE_ERROR) {
            startButton.setVisibility(View.INVISIBLE);
            replayTextView.setVisibility(View.GONE);
        } else if (currentState == CURRENT_STATE_AUTO_COMPLETE) {
            startButton.setImageResource(R.drawable.easy_media_click_replay_selector);
            replayTextView.setVisibility(View.VISIBLE);
        } else {
            startButton.setImageResource(R.drawable.easy_media_click_play_selector);
            replayTextView.setVisibility(View.GONE);
        }
    }

    //清空全部ui展示
    @Override
    public void changeUiToClean() {
        hintContainer(true);
        setMinControlsVisiblity(false, false, false, false, false);
    }


    @Override
    public boolean containerIsShow() {
        return bottomContainer.getVisibility() == View.VISIBLE || topContainer.getVisibility() == View.VISIBLE;
    }

    //开始进度定时器
    @Override
    public void startProgressSchedule() {
        bottomContainer.startProgressSchedule();
    }

    //取消进度定时器
    @Override
    public void stopProgressSchedule() {
        bottomContainer.stopProgressSchedule();
    }

    /**
     * 设置进度  如果2个值都是100，就会设置最大值，如果某个值<0 就不设置
     *
     * @param progress          主进度
     * @param secondaryProgress 缓存进度
     */
    @Override
    public void setProgress(int progress, int secondaryProgress) {
        if (progress >= 0) {
            bottomProgressBar.setProgress(progress);
        }
        if (secondaryProgress >= 0) {
            bottomProgressBar.setSecondaryProgress(secondaryProgress);
        }
        bottomContainer.setProgress(progress, secondaryProgress);
    }

    @Override
    public ImageView getThumbImageView() {
        return thumbImageView;
    }

    @Override
    public int getBufferProgress() {
        return bottomContainer.getBufferProgress();
    }

    @Override
    public void setTime(int position, int duration) {
        bottomContainer.setTime(position, duration);
    }

    //显示或者隐藏顶部和底部控制器
    @Override
    public void showControllerViewAnim(@MediaStatus.Code final int currentState, @MediaScreenStatus.Code final int currentScreen, final boolean isShow) {
        if (currentState != CURRENT_STATE_NORMAL
                && currentState != CURRENT_STATE_ERROR
                && currentState != CURRENT_STATE_AUTO_COMPLETE) {

            MediaUtils.getMainHander().post(new Runnable() {
                @Override
                public void run() {
                    if (currentScreen == SCREEN_WINDOW_TINY) {
                        startButton.setVisibility(isShow ? View.VISIBLE : View.GONE);
                    } else {
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

    private void showContainer(boolean isAnim) {
        if (!isAnim) {
            topContainer.setVisibility(View.VISIBLE);
            bottomContainer.setVisibility(View.VISIBLE);
        } else {
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
