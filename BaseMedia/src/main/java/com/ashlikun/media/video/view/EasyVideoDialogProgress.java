package com.ashlikun.media.video.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.ashlikun.media.R;

/**
 * 作者　　: 李坤
 * 创建时间: 2017/11/28　10:42
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：
 */

public class EasyVideoDialogProgress extends Dialog {
    protected ProgressBar mDialogProgressBar;
    protected TextView mDialogSeekTime;
    protected TextView mDialogTotalTime;
    protected ImageView mDialogIcon;

    public EasyVideoDialogProgress(@NonNull Context context) {
        super(context, R.style.easy_video_style_dialog_progress);
        init();
    }

    private void init() {
        setContentView(R.layout.easy_video_dialog_progress);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        window.addFlags(Window.FEATURE_ACTION_BAR);
        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        window.setLayout(-2, -2);
        WindowManager.LayoutParams localLayoutParams = window.getAttributes();
        localLayoutParams.gravity = Gravity.CENTER;
        window.setAttributes(localLayoutParams);

        mDialogProgressBar = findViewById(R.id.duration_progressbar);
        mDialogSeekTime = findViewById(R.id.tv_current);
        mDialogTotalTime = findViewById(R.id.tv_duration);
        mDialogIcon = findViewById(R.id.duration_image_tip);
    }

    public void setTime(String seekTime, String totalTime) {
        mDialogSeekTime.setText(seekTime);
        mDialogTotalTime.setText(" / " + totalTime);
    }

    public void setProgress(long seekTimePosition, long totalTimeDuration) {
        mDialogProgressBar.setProgress(totalTimeDuration <= 0 ? 0 : (int) (seekTimePosition * 100 / totalTimeDuration));
    }

    /**
     * 设置箭头方向
     *
     * @param isOrientationToRight 是否向右
     */
    public void setOrientation(boolean isOrientationToRight) {
        if (isOrientationToRight) {
            mDialogIcon.setImageResource(R.drawable.easy_video_forward_icon);
        } else {
            mDialogIcon.setImageResource(R.drawable.easy_video_backward_icon);
        }
    }
}
