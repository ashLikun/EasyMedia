package com.ashlikun.media.music;

import com.ashlikun.media.video.EasyMediaManager;
import com.ashlikun.media.video.view.IEasyVideoPlayListener;

/**
 * 作者　　: 李坤
 * 创建时间: 2018/2/1 14:18
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：音频播放器的管理器
 */
public class EasyMusicPlayerManager {


    private static IEasyVideoPlayListener FIRST_FLOOR_MUSIC_PLAY;

    public static IEasyVideoPlayListener getMusicDefault() {
        return FIRST_FLOOR_MUSIC_PLAY;
    }

    /**
     * 监听播放事件
     *
     * @param videoPlayer
     */
    public static void setMusicDefault(IEasyVideoPlayListener videoPlayer) {
        if (videoPlayer != FIRST_FLOOR_MUSIC_PLAY) {
            if (FIRST_FLOOR_MUSIC_PLAY != null) {
                //把之前的设置到完成状态
                completeAll();
            }
            FIRST_FLOOR_MUSIC_PLAY = videoPlayer;
        }
    }

    /**
     * 获取当前正在播放
     *
     * @return
     */
    public static IEasyVideoPlayListener getCurrentMusicPlay() {
        return getMusicDefault();
    }

    /**
     * 强制释放全部播放器
     */
    public static void completeAll() {
        if (FIRST_FLOOR_MUSIC_PLAY != null) {
            FIRST_FLOOR_MUSIC_PLAY.onForceCompletionTo();
            FIRST_FLOOR_MUSIC_PLAY = null;
        }
        EasyMediaManager manager = EasyMediaManager.getInstanceMusic();
        if (manager != null) {
            manager.getMediaPlay().setCurrentDataSource(null);
        }

    }
}
