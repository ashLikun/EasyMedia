package com.ashlikun.media.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ashlikun.media.R;

/**
 * 作者　　: 李坤
 * 创建时间: 2017/11/28　10:42
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：
 */

public class EasyMediaDialogVolume extends Dialog {
    protected ProgressBar mDialogVolumeProgressBar;
    protected TextView mDialogVolumeTextView;
    protected ImageView mDialogVolumeImageView;

    public EasyMediaDialogVolume(@NonNull Context context) {
        super(context, R.style.jz_style_dialog_progress);
        init();
    }

    private void init() {
        setContentView(R.layout.easy_dialog_volume);

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

        mDialogVolumeImageView = findViewById(R.id.volume_image_tip);
        mDialogVolumeTextView = findViewById(R.id.tv_volume);
        mDialogVolumeProgressBar = findViewById(R.id.volume_progressbar);
    }

    public void setVolumePercent(int volumePercent) {
        if (volumePercent <= 0) {
            mDialogVolumeImageView.setBackgroundResource(R.drawable.easy_media_close_volume);
        } else {
            mDialogVolumeImageView.setBackgroundResource(R.drawable.easy_media_add_volume);
        }
        if (volumePercent > 100) {
            volumePercent = 100;
        } else if (volumePercent < 0) {
            volumePercent = 0;
        }
        mDialogVolumeTextView.setText(volumePercent + "%");
        mDialogVolumeProgressBar.setProgress(volumePercent);

    }
}
