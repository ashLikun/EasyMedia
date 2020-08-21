package com.ashlikun.media.video;

import android.app.AlertDialog;
import android.content.DialogInterface;

import com.ashlikun.media.R;
import com.ashlikun.media.video.view.BaseEasyVideoPlay;

/**
 * 作者　　: 李坤
 * 创建时间: 2020/8/21　14:02
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：是否允许播放
 */
public class OnVideoAllowPlay {
    /**
     * 是否允许播放
     *
     * @param play
     * @return
     */
    public boolean onIsAllow(final BaseEasyVideoPlay play) {
        return (play.getCurrentData() == null || !play.getCurrentData().isLocal()) &&
                !NetworkUtils.isWifiConnected(play.getContext()) && !VideoUtils.WIFI_ALLOW_PLAY;
    }

    /**
     * 不允许播放显示的对话框
     *
     * @param play
     * @return
     */
    public void showWifiDialog(final BaseEasyVideoPlay play) {
        AlertDialog.Builder builder = new AlertDialog.Builder(play.getContext());
        builder.setMessage(play.getContext().getResources().getString(R.string.easy_video_tips_not_wifi));
        builder.setPositiveButton(play.getContext().getResources().getString(R.string.easy_video_tips_not_wifi_confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                onAllow(play);
            }
        });
        builder.setNegativeButton(play.getContext().getResources().getString(R.string.easy_video_tips_not_wifi_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                onNoAllow(play);
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
                onNoAllow(play);
            }
        });
        builder.create().show();
    }

    /**
     * 当允许播放点击的时候
     */
    public void onAllow(final BaseEasyVideoPlay play) {
        VideoUtils.WIFI_ALLOW_PLAY = true;
        play.onEvent(EasyVideoAction.ON_CLICK_START_NO_WIFI_GOON);
        play.startVideo();
    }

    /**
     * 当不允许播放点击的时候
     */
    public void onNoAllow(final BaseEasyVideoPlay play) {
        if (play.isScreenFull()) {
            VideoScreenUtils.clearFullscreenLayout(play.getContext());
        }
    }
}
