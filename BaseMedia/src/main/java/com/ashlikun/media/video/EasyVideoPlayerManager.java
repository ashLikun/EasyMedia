package com.ashlikun.media.video;

import com.ashlikun.media.video.view.BaseEasyVideoPlay;
import com.ashlikun.media.video.view.EasyVideoPlayTiny;
import com.ashlikun.media.video.view.EasyVideoPlayer;

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

    public static void setVideoDefault(BaseEasyVideoPlay videoPlayer) {
        if (videoPlayer != null && (SECOND_FLOOR_VIDEO_PLAY != null || SECOND_FLOOR_VIDEO_PLAY_TINY != null)) {
            VideoScreenUtils.backPress();
        }
        FIRST_FLOOR_VIDEO_PLAY = videoPlayer;
    }

    public static BaseEasyVideoPlay getVideoFullscreen() {
        return SECOND_FLOOR_VIDEO_PLAY;
    }

    public static void setVideoFullscreen(EasyVideoPlayer videoPlayer) {
        if (videoPlayer != null && SECOND_FLOOR_VIDEO_PLAY != null) {
            VideoScreenUtils.backPress();
        }
        SECOND_FLOOR_VIDEO_PLAY = videoPlayer;
    }

    /**
     * 获取小窗口播放器
     *
     * @return
     */
    public static EasyVideoPlayTiny getVideoTiny() {
        return SECOND_FLOOR_VIDEO_PLAY_TINY;
    }

    public static void setVideoTiny(EasyVideoPlayTiny videoPlayer) {
        if (videoPlayer != null && SECOND_FLOOR_VIDEO_PLAY_TINY != null) {
            VideoScreenUtils.backPress();
        }
        SECOND_FLOOR_VIDEO_PLAY_TINY = videoPlayer;
    }

    /**
     * 获取非小窗口播放器
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
     * 获取当前正在播放
     *
     * @return
     */
    public static BaseEasyVideoPlay getCurrentVideoPlay() {
        BaseEasyVideoPlay videoPlayer = getVideoTiny();
        if (videoPlayer == null) {
            videoPlayer = getCurrentVideoPlayerNoTiny();
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
