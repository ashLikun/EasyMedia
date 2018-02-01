package com.ashlikun.media;

import com.ashlikun.media.view.EasyBaseVideoPlay;
import com.ashlikun.media.view.EasyVideoPlayTiny;
import com.ashlikun.media.view.EasyVideoPlayer;

/**
 * 作者　　: 李坤
 * 创建时间: 2018/2/1 14:18
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：播放器控件的管理器，可全局调用EasyVideoPlayer和EasyVideoPlayTiny
 */
public class EasyVideoPlayerManager {

    //第一个VideoPlay
    public static EasyVideoPlayer FIRST_FLOOR_VIDEO_PLAY;
    //第二个VideoPlay,全屏
    public static EasyVideoPlayer SECOND_FLOOR_VIDEO_PLAY;
    //第三个VideoPlay,小窗口的
    public static EasyVideoPlayTiny SECOND_FLOOR_VIDEO_PLAY_TINY;

    public static EasyVideoPlayer getVideoDefault() {
        return FIRST_FLOOR_VIDEO_PLAY;
    }

    public static void setVideoDefault(EasyVideoPlayer jzVideoPlayer) {
        FIRST_FLOOR_VIDEO_PLAY = jzVideoPlayer;
    }

    public static EasyVideoPlayer getVideoFullscreen() {
        return SECOND_FLOOR_VIDEO_PLAY;
    }

    public static void setVideoFullscreen(EasyVideoPlayer jzVideoPlayer) {
        SECOND_FLOOR_VIDEO_PLAY = jzVideoPlayer;
    }

    //获取小窗口播放器
    public static EasyVideoPlayTiny getVideoTiny() {
        return SECOND_FLOOR_VIDEO_PLAY_TINY;
    }

    public static void setVideoTiny(EasyVideoPlayTiny jzVideoPlayer) {
        SECOND_FLOOR_VIDEO_PLAY_TINY = jzVideoPlayer;
    }

    //获取当前播放器，默认，或者全屏的
    public static EasyVideoPlayer getCurrentVideoPlayerNoTiny() {
        if (getVideoFullscreen() != null) {
            return getVideoFullscreen();
        }
        return getVideoDefault();
    }

    //获取3种情况的播放器
    public static EasyBaseVideoPlay getCurrentVideoPlay() {
        EasyBaseVideoPlay videoPlayer = EasyVideoPlayerManager.getVideoTiny();
        if (videoPlayer == null) {
            videoPlayer = EasyVideoPlayerManager.getCurrentVideoPlayerNoTiny();
        }
        return videoPlayer;
    }

    public static void completeAll() {
        if (SECOND_FLOOR_VIDEO_PLAY_TINY != null) {
            SECOND_FLOOR_VIDEO_PLAY_TINY.onCompletion();
            SECOND_FLOOR_VIDEO_PLAY_TINY = null;
        }
        if (SECOND_FLOOR_VIDEO_PLAY != null) {
            SECOND_FLOOR_VIDEO_PLAY.onCompletion();
            SECOND_FLOOR_VIDEO_PLAY = null;
        }
        if (FIRST_FLOOR_VIDEO_PLAY != null) {
            FIRST_FLOOR_VIDEO_PLAY.onCompletion();
            FIRST_FLOOR_VIDEO_PLAY = null;
        }

    }
}
