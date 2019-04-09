package com.ashlikun.media;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import com.ashlikun.media.status.MediaStatus;
import com.ashlikun.media.status.MediaViewType;
import com.ashlikun.media.view.BaseEasyVideoPlay;
import com.ashlikun.media.view.EasyVideoPlayTiny;
import com.ashlikun.media.view.EasyVideoPlayer;

import java.util.ArrayList;
import java.util.List;


/**
 * 作者　　: 李坤
 * 创建时间: 2018/8/20　14:41
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：播放器全屏，或者小窗口，还有一些屏幕相关的工具
 */
public class MediaScreenUtils {
    /**
     * 返回键暴力时间
     */
    public static final int FULL_SCREEN_NORMAL_DELAY = 300;
    /**
     * 1:返回键，第一次按返回的时间,
     * 2:还有就是释放视频渲染器时候释放可以释放，比如当小视频点击全屏按钮就不能释放,直接全屏就得释放
     */
    public static long CLICK_QUIT_FULLSCREEN_TIME = 0;
    /**
     * Activity是否存在 FLAG_FULLSCREEN
     */
    public static boolean FLAG_FULLSCREEN_EXIST = true;

    /**
     * 直接开始全屏播放
     *
     * @param easyVideoPlayer 请实例化一个播放器
     * @param url             地址  或者 AssetFileDescriptor
     * @param title           标题
     */
    public static void startFullscreen(EasyVideoPlayer easyVideoPlayer, String url, String title) {
        List<MediaData> mediaData = new ArrayList<>();
        mediaData.add(new MediaData.Builder()
                .url(url)
                .title(title)
                .builder());
        startFullscreen(easyVideoPlayer, mediaData, 0);
    }

    /**
     * 设置数据源
     *
     * @param data 视频ur
     */
    public static void startFullscreen(EasyVideoPlayer easyVideoPlayer, MediaData data) {
        List<MediaData> mediaData = new ArrayList<>();
        mediaData.add(data);
        startFullscreen(easyVideoPlayer, mediaData, 0);
    }

    /**
     * 直接开始全屏播放
     *
     * @param easyVideoPlayer 请实例化一个播放器
     * @param mediaData       地址  或者 AssetFileDescriptor
     * @param defaultIndex    第几个
     */
    public static void startFullscreen(EasyVideoPlayer easyVideoPlayer, List<MediaData> mediaData, int defaultIndex) {
        MediaUtils.releaseAllVideos();
        setActivityFullscreen(easyVideoPlayer.getContext(), true);
        MediaUtils.setRequestedOrientation(easyVideoPlayer.getContext(),
                easyVideoPlayer.isFullscreenPortrait() ? EasyVideoPlayer.ORIENTATION_FULLSCREEN_SENSOR : EasyVideoPlayer.ORIENTATION_FULLSCREEN_LANDSCAPE);
        ViewGroup vp = MediaUtils.getDecorView(easyVideoPlayer.getContext());
        View old = vp.findViewById(R.id.easy_media_fullscreen_id);
        if (old != null) {
            vp.removeView(old);
        }
        easyVideoPlayer.setId(R.id.easy_media_fullscreen_id);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        vp.addView(easyVideoPlayer, lp);
        Animation ra = AnimationUtils.loadAnimation(easyVideoPlayer.getContext(), R.anim.start_fullscreen);
        easyVideoPlayer.setAnimation(ra);
        easyVideoPlayer.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN);
        easyVideoPlayer.setCurrentMediaType(MediaViewType.FULLSCREEN);
        int status = easyVideoPlayer.getCurrentState();
        easyVideoPlayer.setDataSource(mediaData, defaultIndex);
        easyVideoPlayer.setStatus(status);
        if (easyVideoPlayer.getCurrentState() == MediaStatus.NORMAL) {
            easyVideoPlayer.onPlayStartClick();
        }
        CLICK_QUIT_FULLSCREEN_TIME = System.currentTimeMillis();
        EasyVideoPlayerManager.setVideoFullscreen(easyVideoPlayer);
    }

    /**
     * 直接退出全屏和小窗
     */
    public static void quitFullscreenOrTinyWindow() {
        if (EasyVideoPlayerManager.getVideoFullscreen() != null) {
            if (EasyVideoPlayerManager.getVideoFullscreen() instanceof View) {
                clearFloatScreen(EasyVideoPlayerManager.getVideoFullscreen().getContext());
            }
        }
        if (EasyVideoPlayerManager.getVideoTiny() != null) {
            EasyVideoPlayerManager.getVideoTiny().cleanTiny();
        }
        EasyMediaManager.instance().releaseMediaPlayer();
        EasyVideoPlayerManager.completeAll();
    }

    /**
     * 清空全屏
     */
    public static void clearFloatScreen(Context context) {
        MediaUtils.setRequestedOrientation(context, EasyVideoPlayer.ORIENTATION_NORMAL);
        setActivityFullscreen(context, false);
        BaseEasyVideoPlay currentPlay = EasyVideoPlayerManager.getVideoFullscreen();
        if (currentPlay != null) {
            currentPlay.removeTextureView();
            if (currentPlay instanceof View) {
                MediaUtils.getDecorView(context).removeView(currentPlay);
            }
            EasyVideoPlayerManager.setVideoFullscreen(null);
        }
    }

    /**
     * 设置activity全屏
     *
     * @param context
     * @param isFullscreen
     */
    public static void setActivityFullscreen(Context context, boolean isFullscreen) {
        if (isFullscreen) {
            int flag = MediaUtils.getWindow(context).getAttributes().flags;
            if ((flag & WindowManager.LayoutParams.FLAG_FULLSCREEN)
                    == WindowManager.LayoutParams.FLAG_FULLSCREEN) {
                FLAG_FULLSCREEN_EXIST = true;
            } else {
                FLAG_FULLSCREEN_EXIST = false;
            }
            if (!FLAG_FULLSCREEN_EXIST) {
                MediaUtils.getWindow(context).setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
        } else {
            if (!FLAG_FULLSCREEN_EXIST) {
                MediaUtils.getWindow(context).clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
        }
    }

    /**
     * 从当前Activity里面清除全屏View
     */
    public static void clearFullscreenLayout(Context context) {
        ViewGroup vp = MediaUtils.getDecorView(context);
        View oldF = vp.findViewById(R.id.easy_media_fullscreen_id);
        if (oldF != null) {
            vp.removeView(oldF);
        }
        setActivityFullscreen(context, false);
    }

    /**
     * 暴力点击是否满足
     *
     * @return
     */
    public static boolean isBackOk() {
        return System.currentTimeMillis() - CLICK_QUIT_FULLSCREEN_TIME > FULL_SCREEN_NORMAL_DELAY;
    }

    /**
     * 返回键点击,一般用于退出全屏或小窗口
     *
     * @return
     */
    public static boolean backPress() {
        if (!isBackOk()) {
            return false;
        }
        CLICK_QUIT_FULLSCREEN_TIME = System.currentTimeMillis();
        if (EasyVideoPlayerManager.getVideoFullscreen() != null || EasyVideoPlayerManager.getVideoTiny() != null) {
            if (EasyVideoPlayerManager.getVideoDefault() != null) {
                if (MediaUtils.isContainsUri(EasyVideoPlayerManager.getVideoDefault().getMediaData(), EasyMediaManager.getCurrentDataSource())) {
                    //如果默认的Video播放过视频,就直接在这个默认的上面播放
                    EasyVideoPlayerManager.getVideoDefault().onEvent(EasyMediaAction.ON_QUIT_FULLSCREEN);
                } else {
                    //直接退出全屏或者小窗口
                    MediaScreenUtils.quitFullscreenOrTinyWindow();
                }
                return true;
            } else {
                MediaScreenUtils.quitFullscreenOrTinyWindow();
                return true;
            }
        }
        return false;
    }

    /**
     * 开始小窗口播放
     */
    public static boolean startWindowTiny(EasyVideoPlayTiny tiny, List<MediaData> mediaData, int defaultIndex) {
        BaseEasyVideoPlay videoPlayerDefault = EasyVideoPlayerManager.getCurrentVideoPlayerNoTiny();
        if (videoPlayerDefault != null && (videoPlayerDefault.getCurrentState() == MediaStatus.NORMAL ||
                videoPlayerDefault.getCurrentState() == MediaStatus.ERROR ||
                videoPlayerDefault.getCurrentState() == MediaStatus.AUTO_COMPLETE)) {
            return false;
        }

        if (videoPlayerDefault != null) {
            videoPlayerDefault.removeTextureView();
        }
        tiny.setId(R.id.easy_media_tiny_id);

        tiny.setDataSource(mediaData, defaultIndex);
        tiny.addTextureView();

        tiny.showWindow();
        if (videoPlayerDefault == null && !MediaUtils.isContainsUri(mediaData, EasyMediaManager.getCurrentDataSource())) {
            tiny.play();
        } else {
            tiny.setCurrentState(videoPlayerDefault.getCurrentState());
        }
        return true;

    }
}