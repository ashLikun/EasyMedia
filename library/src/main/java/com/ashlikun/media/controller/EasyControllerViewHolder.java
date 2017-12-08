package com.ashlikun.media.controller;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ashlikun.media.MediaUtils;
import com.ashlikun.media.R;
import com.ashlikun.media.status.EasyMediaStatus;
import com.ashlikun.media.status.EasyScreenStatus;

import static com.ashlikun.media.status.EasyMediaStatus.CURRENT_STATE_AUTO_COMPLETE;
import static com.ashlikun.media.status.EasyMediaStatus.CURRENT_STATE_ERROR;
import static com.ashlikun.media.status.EasyMediaStatus.CURRENT_STATE_NORMAL;
import static com.ashlikun.media.status.EasyMediaStatus.CURRENT_STATE_PAUSE;
import static com.ashlikun.media.status.EasyMediaStatus.CURRENT_STATE_PLAYING;
import static com.ashlikun.media.status.EasyMediaStatus.CURRENT_STATE_PREPARING;
import static com.ashlikun.media.status.EasyMediaStatus.CURRENT_STATE_PREPARING_CHANGING_URL;
import static com.ashlikun.media.status.EasyScreenStatus.SCREEN_WINDOW_FULLSCREEN;
import static com.ashlikun.media.status.EasyScreenStatus.SCREEN_WINDOW_LIST;
import static com.ashlikun.media.status.EasyScreenStatus.SCREEN_WINDOW_NORMAL;
import static com.ashlikun.media.status.EasyScreenStatus.SCREEN_WINDOW_TINY;

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
    //2个进度条
    public ProgressBar loadingProgressBar;
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

        thumbImageView.setOnClickListener(clickListener);
        startButton.setOnClickListener(clickListener);
        bottomContainer.setOnEventListener(onEventListener);
    }

    //根据状态改变ui
    public void changUi(int currentState, int currentScreen) {
        //默认
        if (currentState == CURRENT_STATE_NORMAL) {
            changeUiToNormal(currentState, currentState);
            bottomContainer.stopProgressSchedule();
        }
        //准备好了
        else if (currentState == CURRENT_STATE_PREPARING) {
            changeUiToPreparing(currentState);
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
            changeUiToPauseClear(currentState);
            bottomContainer.startProgressSchedule();
        }
        //自动完成了
        else if (currentState == CURRENT_STATE_AUTO_COMPLETE) {
            changeUiToComplete(currentState, currentScreen);
            bottomContainer.stopProgressSchedule();
        }
        //错误
        else if (currentState == CURRENT_STATE_ERROR) {
            changeUiToError(currentState, currentScreen);
            bottomContainer.stopProgressSchedule();
        }
        updateStartImage(currentState);
    }

    public void setDataSource(Object[] dataSourceObjects, int screen, Object... objects) {
        bottomContainer.setInitData(dataSourceObjects, screen);
        topContainer.setInitData(screen, objects);
        if (screen == SCREEN_WINDOW_FULLSCREEN) {
            changeStartButtonSize((int) viewGroup.getResources().getDimension(R.dimen.jz_start_button_w_h_fullscreen));
        } else if (screen == SCREEN_WINDOW_NORMAL || screen == SCREEN_WINDOW_LIST) {
            changeStartButtonSize((int) viewGroup.getResources().getDimension(R.dimen.jz_start_button_w_h_normal));
        } else if (screen == SCREEN_WINDOW_TINY) {
            changeStartButtonSize((int) viewGroup.getResources().getDimension(R.dimen.jz_start_button_w_h_normal));
        }
    }


    //改变播放按钮的大小
    public void changeStartButtonSize(int size) {
        ViewGroup.LayoutParams lp = startButton.getLayoutParams();
        lp.height = size;
        lp.width = size;
        lp = loadingProgressBar.getLayoutParams();
        lp.height = size;
        lp.width = size;
    }

    public void onClickUiToggleToClear(int currentState, int currentScreen) {
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
                changeUiToComplete(currentState, currentScreen);
            }
        }
    }


    public void changeUiToPreparing(int currentScreen) {
        bottomContainer.resetProgressAndTime();
        switch (currentScreen) {
            case SCREEN_WINDOW_NORMAL:
            case SCREEN_WINDOW_LIST:
                setAllControlsVisiblity(View.INVISIBLE, View.INVISIBLE, View.INVISIBLE,
                        View.VISIBLE, View.VISIBLE, View.INVISIBLE);
                break;
            case SCREEN_WINDOW_FULLSCREEN:
                setAllControlsVisiblity(View.INVISIBLE, View.INVISIBLE, View.INVISIBLE,
                        View.VISIBLE, View.VISIBLE, View.INVISIBLE);
                break;
            case SCREEN_WINDOW_TINY:
                break;
        }
    }

    //清空播放状态
    public void changeUiToPlayingClean(int currentScreen) {
        switch (currentScreen) {
            case SCREEN_WINDOW_NORMAL:
            case SCREEN_WINDOW_LIST:
                setAllControlsVisiblity(View.INVISIBLE, View.INVISIBLE, View.INVISIBLE,
                        View.GONE, View.INVISIBLE, View.INVISIBLE);
                break;
            case SCREEN_WINDOW_FULLSCREEN:
                setAllControlsVisiblity(View.INVISIBLE, View.INVISIBLE, View.INVISIBLE,
                        View.GONE, View.INVISIBLE, View.INVISIBLE);
                break;
            case SCREEN_WINDOW_TINY:
                break;
        }
    }

    //清空暂停状态
    public void changeUiToPauseClear(int currentScreen) {
        switch (currentScreen) {
            case SCREEN_WINDOW_NORMAL:
            case SCREEN_WINDOW_LIST:
                setAllControlsVisiblity(View.INVISIBLE, View.INVISIBLE, View.INVISIBLE,
                        View.INVISIBLE, View.INVISIBLE, View.INVISIBLE);
                break;
            case SCREEN_WINDOW_FULLSCREEN:
                setAllControlsVisiblity(View.INVISIBLE, View.INVISIBLE, View.INVISIBLE,
                        View.INVISIBLE, View.INVISIBLE, View.INVISIBLE);
                break;
            case SCREEN_WINDOW_TINY:
                break;
        }
    }

    //改变ui到默认状态
    public void changeUiToNormal(int currentState, int currentScreen) {
        switch (currentScreen) {
            case EasyScreenStatus.SCREEN_WINDOW_NORMAL:
            case EasyScreenStatus.SCREEN_WINDOW_LIST:
                setAllControlsVisiblity(View.VISIBLE, View.INVISIBLE, View.VISIBLE,
                        View.INVISIBLE, View.VISIBLE, View.INVISIBLE);
                updateStartImage(currentState);
                break;
            case SCREEN_WINDOW_FULLSCREEN:
                setAllControlsVisiblity(View.VISIBLE, View.INVISIBLE, View.VISIBLE,
                        View.INVISIBLE, View.VISIBLE, View.INVISIBLE);
                updateStartImage(currentState);
                break;
            case SCREEN_WINDOW_TINY:
                break;
        }
    }

    //改变ui成完成
    public void changeUiToComplete(int currentState, int currentScreen) {
        switch (currentScreen) {
            case SCREEN_WINDOW_NORMAL:
            case SCREEN_WINDOW_LIST:
                setAllControlsVisiblity(View.VISIBLE, View.INVISIBLE, View.VISIBLE,
                        View.INVISIBLE, View.VISIBLE, View.INVISIBLE);
                updateStartImage(currentState);
                break;
            case SCREEN_WINDOW_FULLSCREEN:
                setAllControlsVisiblity(View.VISIBLE, View.INVISIBLE, View.VISIBLE,
                        View.INVISIBLE, View.VISIBLE, View.INVISIBLE);
                updateStartImage(currentState);
                break;
            case SCREEN_WINDOW_TINY:
                break;
        }
    }

    public void changeUiToError(int currentState, int currentScreen) {
        switch (currentScreen) {
            case SCREEN_WINDOW_NORMAL:
            case SCREEN_WINDOW_LIST:
                setAllControlsVisiblity(View.INVISIBLE, View.INVISIBLE, View.VISIBLE,
                        View.INVISIBLE, View.INVISIBLE, View.VISIBLE);
                updateStartImage(currentState);
                break;
            case SCREEN_WINDOW_FULLSCREEN:
                setAllControlsVisiblity(View.INVISIBLE, View.INVISIBLE, View.VISIBLE,
                        View.INVISIBLE, View.INVISIBLE, View.VISIBLE);
                updateStartImage(currentState);
                break;
            case SCREEN_WINDOW_TINY:
                break;
        }

    }

    public void setAllControlsVisiblity(int topCon, int bottomCon, int startBtn, int loadingPro,
                                        int thumbImg, int retryLayout) {
        topContainer.setVisibility(topCon);
        bottomContainer.setVisibility(bottomCon);
        startButton.setVisibility(startBtn);
        loadingProgressBar.setVisibility(loadingPro);
        thumbImageView.setVisibility(thumbImg);
        mRetryLayout.setVisibility(retryLayout);
    }

    //跟新开始的按钮图片
    public void updateStartImage(int currentState) {
        if (currentState == CURRENT_STATE_PLAYING) {
            startButton.setImageResource(R.drawable.easy_media_click_pause_selector);
            replayTextView.setVisibility(View.INVISIBLE);
        } else if (currentState == EasyMediaStatus.CURRENT_STATE_ERROR) {
            startButton.setVisibility(View.INVISIBLE);
            replayTextView.setVisibility(View.INVISIBLE);
        } else if (currentState == CURRENT_STATE_AUTO_COMPLETE) {
            startButton.setImageResource(R.drawable.easy_media_click_replay_selector);
            replayTextView.setVisibility(View.VISIBLE);
        } else {
            startButton.setImageResource(R.drawable.easy_media_click_play_selector);
            replayTextView.setVisibility(View.INVISIBLE);
        }
    }


    public void showControllerViewAnim(final int currentState, final boolean isShow) {
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
                    } else {
                        bottomContainer.setVisibility(View.GONE);
                        topContainer.setVisibility(View.GONE);
                        startButton.setVisibility(View.GONE);
                    }
                }
            });
        }

    }

}
