package com.ashlikun.media;

import com.ashlikun.media.view.BaseEasyVideoPlay;
import com.ashlikun.media.view.EasyVideoPlayTiny;
import com.ashlikun.media.view.EasyVideoPlayer;

/**
 * 作者　　: 李坤
 * 创建时间: 2018/2/1 14:18
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：播放器控件的管理器，可全局调用BaseEasyVideoPlay
 */
public class EasyVideoPlayerManager {

    /**
     * 第一个VideoPlay,默认的，可以自行设置其他的
     */
    private static BaseEasyVideoPlay FIRST_FLOOR_VIDEO_PLAY;
    /**
     * 第二个VideoPlay,全屏
     */
    private static BaseEasyVideoPlay SECOND_FLOOR_VIDEO_PLAY;
    /**
     * 第三个VideoPlay,小窗口的
     */
    private static EasyVideoPlayTiny SECOND_FLOOR_VIDEO_PLAY_TINY;

    public static BaseEasyVideoPlay getVideoDefault() {
        return FIRST_FLOOR_VIDEO_PLAY;
    }

    public static void setVideoDefault(BaseEasyVideoPlay jzVideoPlayer) {
        FIRST_FLOOR_VIDEO_PLAY = jzVideoPlayer;
    }

    public static BaseEasyVideoPlay getVideoFullscreen() {
        return SECOND_FLOOR_VIDEO_PLAY;
    }

    public static void setVideoFullscreen(EasyVideoPlayer jzVideoPlayer) {
        SECOND_FLOOR_VIDEO_PLAY = jzVideoPlayer;
    }

    /**
     * 获取小窗口播放器
     *
     * @return
     */
    public static EasyVideoPlayTiny getVideoTiny() {
        return SECOND_FLOOR_VIDEO_PLAY_TINY;
    }

    public static void setVideoTiny(EasyVideoPlayTiny jzVideoPlayer) {
        SECOND_FLOOR_VIDEO_PLAY_TINY = jzVideoPlayer;
    }

    /**
     * 获取小窗口播放器
     *
     * @return
     */
    public static BaseEasyVideoPlay getCurrentVideoPlayerNoTiny() {
        if (getVideoFullscreen() != null) {
            return getVideoFullscreen();
        }
        return getVideoDefault();
    }

    /**
     * 获取3种情况的播放器
     *
     * @return
     */
    public static BaseEasyVideoPlay getCurrentVideoPlay() {
        BaseEasyVideoPlay videoPlayer = EasyVideoPlayerManager.getVideoTiny();
        if (videoPlayer == null) {
            videoPlayer = EasyVideoPlayerManager.getCurrentVideoPlayerNoTiny();
        }
        return videoPlayer;
    }

    /**
     * 强制释放全部播放器
     */
    public static void completeAll() {
        if (SECOND_FLOOR_VIDEO_PLAY_TINY != null) {
            SECOND_FLOOR_VIDEO_PLAY_TINY.onForceCompletionTo();
            SECOND_FLOOR_VIDEO_PLAY_TINY = null;
        }
        if (SECOND_FLOOR_VIDEO_PLAY != null) {
            SECOND_FLOOR_VIDEO_PLAY.onForceCompletionTo();
            SECOND_FLOOR_VIDEO_PLAY = null;
        }
        if (FIRST_FLOOR_VIDEO_PLAY != null) {
            FIRST_FLOOR_VIDEO_PLAY.onForceCompletionTo();
            FIRST_FLOOR_VIDEO_PLAY = null;
        }
        EasyMediaManager.setCurrentDataSource(null);
    }
}
