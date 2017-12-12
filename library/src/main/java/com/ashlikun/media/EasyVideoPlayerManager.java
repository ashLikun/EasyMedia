package com.ashlikun.media;

/**
 * 播放器控件的管理器，可全局调用EasyVideoPlayer
 */
public class EasyVideoPlayerManager {

    //第一个VideoPlay
    public static EasyVideoPlayer FIRST_FLOOR_VIDEO_PLAY;
    //第二个VideoPlay,全屏或者小窗口播放的
    public static EasyVideoPlayer SECOND_FLOOR_VIDEO_PLAY;

    public static EasyVideoPlayer getFirstFloor() {
        return FIRST_FLOOR_VIDEO_PLAY;
    }

    public static void setFirstFloor(EasyVideoPlayer jzVideoPlayer) {
        FIRST_FLOOR_VIDEO_PLAY = jzVideoPlayer;
    }

    public static EasyVideoPlayer getSecondFloor() {
        return SECOND_FLOOR_VIDEO_PLAY;
    }

    public static void setSecondFloor(EasyVideoPlayer jzVideoPlayer) {
        SECOND_FLOOR_VIDEO_PLAY = jzVideoPlayer;
    }

    //获取当前播放器
    public static EasyVideoPlayer getCurrentVideoPlayer() {
        if (getSecondFloor() != null) {
            return getSecondFloor();
        }
        return getFirstFloor();
    }

    public static void completeAll() {
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
