package com.ashlikun.media.controller;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ashlikun.media.MediaUtils;
import com.ashlikun.media.R;
import com.ashlikun.media.status.MediaStatus;
import com.ashlikun.media.status.MediaScreenStatus;

import static com.ashlikun.media.status.MediaStatus.CURRENT_STATE_AUTO_COMPLETE;
import static com.ashlikun.media.status.MediaStatus.CURRENT_STATE_ERROR;
import static com.ashlikun.media.status.MediaStatus.CURRENT_STATE_NORMAL;
import static com.ashlikun.media.status.MediaStatus.CURRENT_STATE_PAUSE;
import static com.ashlikun.media.status.MediaStatus.CURRENT_STATE_PLAYING;
import static com.ashlikun.media.status.MediaStatus.CURRENT_STATE_PREPARING;
import static com.ashlikun.media.status.MediaStatus.CURRENT_STATE_PREPARING_CHANGING_URL;
import static com.ashlikun.media.status.MediaScreenStatus.SCREEN_WINDOW_FULLSCREEN;
import static com.ashlikun.media.status.MediaScreenStatus.SCREEN_WINDOW_LIST;
import static com.ashlikun.media.status.MediaScreenStatus.SCREEN_WINDOW_NORMAL;
import static com.ashlikun.media.status.MediaScreenStatus.SCREEN_WINDOW_TINY;

/**
 * 作者　　: 李坤
 * 创建时间: 2017/12/5　16:46
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：控制器的holder
 */

public class EasyControllerViewHolder {
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
    //从新播放
    public TextView replayTextView;


    public EasyControllerViewHolder(ViewGroup viewGroup, View.OnClickListener clickListener, MediaControllerBottom.OnEventListener onEventListener) {
        this.viewGroup = viewGroup;
        startButton = viewGroup.findViewById(R.id.start);

        bottomContainer = viewGroup.findViewById(R.id.controllerBottom);
        topContainer = viewGroup.findViewById(R.id.controllerTop);
        mRetryLayout = viewGroup.findViewById(R.id.retry_layout);

        loadingProgressBar = viewGroup.findViewById(R.id.loading);
        thumbImageView = viewGroup.findViewById(R.id.thumb);
        replayTextView = viewGroup.findViewById(R.id.replay_text);
        bottomProgressBar = viewGroup.findViewById(R.id.bottom_progress);

        thumbImageView.setOnClickListener(clickListener);
        startButton.setOnClickListener(clickListener);
        bottomContainer.setOnEventListener(onEventListener);
        viewGroup.findViewById(R.id.retry_btn).setOnClickListener(clickListener);
    }

    //根据状态改变ui
    public void changUi(@MediaStatus.Code int currentState, @MediaScreenStatus.Code int currentScreen) {
        //默认
        if (currentState == CURRENT_STATE_NORMAL) {
            changeUiToNormal(currentScreen);
            bottomContainer.stopProgressSchedule();
        }
        //准备好了
        else if (currentState == CURRENT_STATE_PREPARING) {
            changeUiToPreparing(currentScreen);
        }
        //准备改变播放的url
        else if (currentState == CURRENT_STATE_PREPARING_CHANGING_URL) {
            loadingProgressBar.setVisibility(View.VISIBLE);
            startButton.setVisibility(View.GONE);
        }
        //播放中
        else if (currentState == CURRENT_STATE_PLAYING) {
            changeUiToPlayingClean(currentScreen);
            bottomContainer.startProgressSchedule();
        }
        //暂停
        else if (currentState == CURRENT_STATE_PAUSE) {
            changeUiToPauseClear(currentScreen);
            bottomContainer.startProgressSchedule();
        }
        //自动完成了
        else if (currentState == CURRENT_STATE_AUTO_COMPLETE) {
            changeUiToComplete(currentScreen);
            bottomContainer.stopProgressSchedule();
        }
        //错误
        else if (currentState == CURRENT_STATE_ERROR) {
            changeUiToError(currentScreen);
            bottomContainer.stopProgressSchedule();
        }
        updateStartImage(currentState);
    }

    public void setDataSource(Object[] dataSourceObjects, int screen, Object... objects) {
        bottomContainer.setInitData(dataSourceObjects, screen);
        topContainer.setInitData(screen, objects);
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

    //显示对话框的时候清空控制器状态
    public void onShowDialogToClear(@MediaStatus.Code int currentState, @MediaScreenStatus.Code int currentScreen) {
        if (currentState == CURRENT_STATE_PREPARING) {
            if (bottomContainer.getVisibility() == View.VISIBLE) {
                changeUiToPreparing(currentScreen);
            }
        } else if (currentState == CURRENT_STATE_PLAYING) {
            if (bottomContainer.getVisibility() == View.VISIBLE) {
                changeUiToPlayingClean(currentScreen);
            }
        } else if (currentState == CURRENT_STATE_PAUSE) {
            if (bottomContainer.getVisibility() == View.VISIBLE) {
                changeUiToPauseClear(currentScreen);
            }
        } else if (currentState == CURRENT_STATE_AUTO_COMPLETE) {
            if (bottomContainer.getVisibility() == View.VISIBLE) {
                changeUiToComplete(currentScreen);
            }
        }
    }

    //准备中
    public void changeUiToPreparing(@MediaScreenStatus.Code int currentScreen) {
        bottomContainer.resetProgressAndTime();
        switch (currentScreen) {
            case SCREEN_WINDOW_NORMAL:
            case SCREEN_WINDOW_LIST:
            case SCREEN_WINDOW_FULLSCREEN:
                setTopAndBottomControlsVisiblity(false, false);
                setMinControlsVisiblity(false, true, true, false, false);
                break;
            case SCREEN_WINDOW_TINY:
                break;
        }
    }


    //改变ui到默认状态
    public void changeUiToNormal(@MediaScreenStatus.Code int currentScreen) {
        switch (currentScreen) {
            case MediaScreenStatus.SCREEN_WINDOW_NORMAL:
            case MediaScreenStatus.SCREEN_WINDOW_LIST:
            case SCREEN_WINDOW_FULLSCREEN:
                setTopAndBottomControlsVisiblity(false, false);
                setMinControlsVisiblity(true, false, true, false, false);
                break;
            case SCREEN_WINDOW_TINY:
                break;
        }
    }

    //改变ui成完成
    public void changeUiToComplete(@MediaScreenStatus.Code int currentScreen) {
        switch (currentScreen) {
            case SCREEN_WINDOW_NORMAL:
            case SCREEN_WINDOW_LIST:
            case SCREEN_WINDOW_FULLSCREEN:
                setTopAndBottomControlsVisiblity(true, false);
                setMinControlsVisiblity(true, false, true, false, false);
                break;
            case SCREEN_WINDOW_TINY:
                break;
        }
    }

    //改变ui错误
    public void changeUiToError(@MediaScreenStatus.Code int currentScreen) {
        switch (currentScreen) {
            case SCREEN_WINDOW_NORMAL:
            case SCREEN_WINDOW_LIST:
            case SCREEN_WINDOW_FULLSCREEN:
                setTopAndBottomControlsVisiblity(false, false);
                setMinControlsVisiblity(true, false, false, false, true);
                break;
            case SCREEN_WINDOW_TINY:
                break;
        }

    }


    //清空播放状态,只播放
    public void changeUiToPlayingClean(@MediaScreenStatus.Code int currentScreen) {
        switch (currentScreen) {
            case SCREEN_WINDOW_NORMAL:
            case SCREEN_WINDOW_LIST:
            case SCREEN_WINDOW_FULLSCREEN:
                setTopAndBottomControlsVisiblity(false, false);
                setMinControlsVisiblity(false, false, false, true, false);
                break;
            case SCREEN_WINDOW_TINY:
                break;
        }
    }

    //清空暂停状态
    public void changeUiToPauseClear(@MediaScreenStatus.Code int currentScreen) {
        switch (currentScreen) {
            case SCREEN_WINDOW_NORMAL:
            case SCREEN_WINDOW_LIST:
            case SCREEN_WINDOW_FULLSCREEN:
                setTopAndBottomControlsVisiblity(false, false);
                setMinControlsVisiblity(false, false, false, true, false);
                break;
            case SCREEN_WINDOW_TINY:
                break;
        }
    }

    //设置顶部和底部控制器显示或者隐藏
    public void setTopAndBottomControlsVisiblity(boolean topConIsShow, boolean bottomConIsShow) {
        topContainer.setVisibility(topConIsShow ? View.VISIBLE : View.GONE);
        bottomContainer.setVisibility(bottomConIsShow ? View.VISIBLE : View.GONE);
    }

    //设置其他小控件
    public void setMinControlsVisiblity(boolean startBtn, boolean loadingPro,
                                        boolean thumbImg, boolean bottomPrp, boolean retryLayout) {
        startButton.setVisibility(startBtn ? View.VISIBLE : View.GONE);
        loadingProgressBar.setVisibility(loadingPro ? View.VISIBLE : View.GONE);
        thumbImageView.setVisibility(thumbImg ? View.VISIBLE : View.GONE);
        mRetryLayout.setVisibility(retryLayout ? View.VISIBLE : View.GONE);
        bottomProgressBar.setVisibility(bottomPrp ? View.VISIBLE : View.GONE);
    }

    //跟新开始的按钮图片
    public void updateStartImage(@MediaStatus.Code int currentState) {
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

    public boolean bottomContainerIsShow() {
        return bottomContainer.getVisibility() == View.VISIBLE;
    }

    //显示或者隐藏顶部和底部控制器
    public void showControllerViewAnim(@MediaStatus.Code final int currentState, final boolean isShow) {
        if (currentState != CURRENT_STATE_NORMAL
                && currentState != CURRENT_STATE_ERROR
                && currentState != CURRENT_STATE_AUTO_COMPLETE) {

            MediaUtils.getMainHander().post(new Runnable() {
                @Override
                public void run() {
                    if (isShow) {
                        bottomContainer.setVisibility(View.VISIBLE);
                        topContainer.setVisibility(View.VISIBLE);
                        startButton.setVisibility(View.VISIBLE);
                        bottomProgressBar.setVisibility(View.GONE);
                    } else {
                        bottomContainer.setVisibility(View.GONE);
                        topContainer.setVisibility(View.GONE);
                        startButton.setVisibility(View.GONE);
                        bottomProgressBar.setVisibility(View.VISIBLE);
                    }
                }
            });
        }

    }

}
