package com.ashlikun.media.video;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import com.ashlikun.media.R;
import com.ashlikun.media.video.status.VideoStatus;
import com.ashlikun.media.video.view.BaseEasyVideoPlay;
import com.ashlikun.media.video.view.EasyVideoPlayTiny;
import com.ashlikun.media.video.view.EasyVideoPlayer;

import java.util.ArrayList;
import java.util.List;


/**
 * 作者　　: 李坤
 * 创建时间: 2018/8/20　14:41
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：播放器全屏，或者小窗口，还有一些屏幕相关的工具
 */
public class VideoScreenUtils {
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
        List<VideoData> mediaData = new ArrayList<>();
        mediaData.add(new VideoData.Builder()
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
    public static void startFullscreen(EasyVideoPlayer easyVideoPlayer, VideoData data) {
        List<VideoData> mediaData = new ArrayList<>();
        mediaData.add(data);
        startFullscreen(easyVideoPlayer, mediaData, 0);
    }

    /**
     * 计算全屏方向
     * 0:自动判断(宽高比是否可以竖屏)
     * 1:可以竖屏(2个横屏，一个竖屏)
     * 2:不可以竖屏(2个横屏)
     *
     * @return
     */
    public static int calculateOrientation(int fullscreenPortrait) {
        int orientation = EasyVideoPlayer.ORIENTATION_FULLSCREEN_SENSOR;
        if (fullscreenPortrait == 0) {
            if (EasyMediaManager.textureView != null && EasyMediaManager.textureView.isSizeOk()) {
                orientation = EasyMediaManager.textureView.isPortrait() ? EasyVideoPlayer.ORIENTATION_FULLSCREEN_SENSOR :
                        EasyVideoPlayer.ORIENTATION_FULLSCREEN_SENSOR_LANDSCAPE;
            }
        } else if (fullscreenPortrait == 1) {
            orientation = EasyVideoPlayer.ORIENTATION_FULLSCREEN_SENSOR;
        } else if (fullscreenPortrait == 2) {
            orientation = EasyVideoPlayer.ORIENTATION_FULLSCREEN_SENSOR_LANDSCAPE;
        } else if (fullscreenPortrait == 3) {
            orientation = EasyVideoPlayer.ORIENTATION_FULLSCREEN_LANDSCAPE;
        }
        return orientation;
    }

    /**
     * 直接开始全屏播放
     *
     * @param easyVideoPlayer 请实例化一个播放器
     * @param mediaData       地址  或者 AssetFileDescriptor
     * @param defaultIndex    第几个
     */
    public static void startFullscreen(EasyVideoPlayer easyVideoPlayer, List<VideoData> mediaData, int defaultIndex) {
        VideoUtils.releaseAllVideos();
        setActivityFullscreen(easyVideoPlayer.getContext(), true);

        VideoUtils.setRequestedOrientation(easyVideoPlayer.getContext(), calculateOrientation(easyVideoPlayer.getFullscreenPortrait()));
        ViewGroup vp = VideoUtils.getDecorView(easyVideoPlayer.getContext());
        View old = vp.findViewById(R.id.easy_video_fullscreen_id);
        if (old != null) {
            vp.removeView(old);
        }
        easyVideoPlayer.setId(R.id.easy_video_fullscreen_id);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        vp.addView(easyVideoPlayer, lp);
        Animation ra = AnimationUtils.loadAnimation(easyVideoPlayer.getContext(), R.anim.easy_video_start_fullscreen);
        easyVideoPlayer.setAnimation(ra);
        easyVideoPlayer.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN);
        easyVideoPlayer.setFull(true);
        int status = easyVideoPlayer.getCurrentState();
        easyVideoPlayer.setDataSource(mediaData, defaultIndex);
        easyVideoPlayer.setStatus(status);
        if (easyVideoPlayer.getCurrentState() == VideoStatus.NORMAL) {
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
        EasyMediaManager.getInstance().releaseMediaPlayer();
        EasyVideoPlayerManager.completeAll();
    }

    /**
     * 清空全屏
     */
    public static void clearFloatScreen(Context context) {
        VideoUtils.setRequestedOrientation(context, EasyVideoPlayer.ORIENTATION_NORMAL);
        setActivityFullscreen(context, false);
        BaseEasyVideoPlay currentPlay = EasyVideoPlayerManager.getVideoFullscreen();
        if (currentPlay != null) {
            currentPlay.removeTextureView();
            if (currentPlay instanceof View) {
                VideoUtils.getDecorView(context).removeView(currentPlay);
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
            int flag = VideoUtils.getWindow(context).getAttributes().flags;
            if ((flag & WindowManager.LayoutParams.FLAG_FULLSCREEN)
                    == WindowManager.LayoutParams.FLAG_FULLSCREEN) {
                FLAG_FULLSCREEN_EXIST = true;
            } else {
                FLAG_FULLSCREEN_EXIST = false;
            }
            if (!FLAG_FULLSCREEN_EXIST) {
                VideoUtils.getWindow(context).setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
        } else {
            if (!FLAG_FULLSCREEN_EXIST) {
                VideoUtils.getWindow(context).clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
        }
    }

    /**
     * 从当前Activity里面清除全屏View
     */
    public static void clearFullscreenLayout(Context context) {
        ViewGroup vp = VideoUtils.getDecorView(context);
        View oldF = vp.findViewById(R.id.easy_video_fullscreen_id);
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
                if (VideoUtils.isContainsUri(EasyVideoPlayerManager.getVideoDefault().getMediaData(), EasyMediaManager.getCurrentDataSource())) {
                    //如果默认的Video播放过视频,就直接在这个默认的上面播放
                    EasyVideoPlayerManager.getVideoDefault().onEvent(EasyVideoAction.ON_QUIT_FULLSCREEN);
                } else {
                    //直接退出全屏或者小窗口
                    VideoScreenUtils.quitFullscreenOrTinyWindow();
                }
                return false;
            } else {
                VideoScreenUtils.quitFullscreenOrTinyWindow();
                return true;
            }
        }
        return false;
    }

    /**
     * 开始小窗口播放
     */
    public static boolean startWindowTiny(EasyVideoPlayTiny tiny, List<VideoData> mediaData, int defaultIndex) {
        BaseEasyVideoPlay videoPlayerDefault = EasyVideoPlayerManager.getCurrentVideoPlayerNoTiny();
        if (videoPlayerDefault != null && (videoPlayerDefault.getCurrentState() == VideoStatus.NORMAL ||
                videoPlayerDefault.getCurrentState() == VideoStatus.ERROR ||
                videoPlayerDefault.getCurrentState() == VideoStatus.AUTO_COMPLETE)) {
            return false;
        }

        if (videoPlayerDefault != null) {
            videoPlayerDefault.removeTextureView();
        }
        tiny.setId(R.id.easy_video_tiny_id);

        tiny.setDataSource(mediaData, defaultIndex);
        tiny.addTextureView();

        tiny.showWindow();
        if (videoPlayerDefault == null && !VideoUtils.isContainsUri(mediaData, EasyMediaManager.getCurrentDataSource())) {
            tiny.play();
        } else {
            tiny.setCurrentState(videoPlayerDefault.getCurrentState());
        }
        return true;

    }

    /**
     * 从一个视频替换到另一个视频
     * 前提两个视频播放的是同一个
     * 一般用于页面跳转
     */
    public static void startCacheVideo(BaseEasyVideoPlay newVideoPlay) {
        BaseEasyVideoPlay oldVideo = EasyVideoPlayerManager.getCurrentVideoPlay();
        if (oldVideo != null && (newVideoPlay.getCurrentData() == null ||
                newVideoPlay.getCurrentData().equalsUrl(oldVideo.getCurrentData()))) {

            oldVideo.removeTextureView();
            newVideoPlay.setStatus(oldVideo.getCurrentState());
            newVideoPlay.addTextureView();
            if (oldVideo instanceof EasyVideoPlayer && newVideoPlay instanceof EasyVideoPlayer) {
                if (((EasyVideoPlayer) oldVideo).getMediaController() != null && ((EasyVideoPlayer) newVideoPlay).getMediaController() != null) {
                    ((EasyVideoPlayer) newVideoPlay).getMediaController().setBufferProgress(((EasyVideoPlayer) oldVideo).getMediaController().getBufferProgress());
                }
            }
            //还原默认的view
            oldVideo.setStatus(VideoStatus.NORMAL);
            //取消定时器
            if (oldVideo instanceof EasyVideoPlayer && ((EasyVideoPlayer) oldVideo).getMediaController() != null) {
                ((EasyVideoPlayer) oldVideo).getMediaController().cancelDismissControlViewSchedule();
            }
            if (newVideoPlay.getMediaData() == null && oldVideo.getMediaData() != null) {
                newVideoPlay.setDataSource(oldVideo.getMediaData(), oldVideo.getCurrentUrlIndex());
            }
            EasyVideoPlayerManager.setVideoDefault(newVideoPlay);
        }
    }
}
