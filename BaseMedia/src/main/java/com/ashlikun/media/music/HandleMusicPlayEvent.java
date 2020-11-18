package com.ashlikun.media.music;

import android.media.MediaPlayer;

import com.ashlikun.media.video.EasyMediaInterface;
import com.ashlikun.media.video.EasyMediaManager;
import com.ashlikun.media.video.HandlePlayEvent;

/**
 * 作者　　: 李坤
 * 创建时间: 2020/11/18　17:40
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：
 */
public class HandleMusicPlayEvent implements HandlePlayEvent {
    String tag;

    public HandleMusicPlayEvent(String tag) {
        this.tag = tag;
    }

    @Override
    public void onPause() {
        EasyMediaManager.getInstance(tag).getMediaHandler().post(new Runnable() {
            @Override
            public void run() {
                if (EasyMusicPlayerManager.getCurrentMusicPlay() != null) {
                    EasyMusicPlayerManager.getCurrentMusicPlay().onPause();
                }
            }
        });
    }

    @Override
    public void onPrepared() {
        EasyMediaManager.getInstance(tag).getMediaHandler().post(new Runnable() {
            @Override
            public void run() {
                if (EasyMusicPlayerManager.getCurrentMusicPlay() != null) {
                    EasyMusicPlayerManager.getCurrentMusicPlay().onPrepared();
                }
            }
        });
    }

    @Override
    public void setBufferProgress(int percent) {
        EasyMediaManager.getInstance(tag).getMediaHandler().post(new Runnable() {
            @Override
            public void run() {
                if (EasyMusicPlayerManager.getCurrentMusicPlay() != null) {
                    EasyMusicPlayerManager.getCurrentMusicPlay().setBufferProgress(percent);
                }
            }
        });
    }

    @Override
    public void onError(int what, int extra) {
        EasyMediaManager.getInstance(tag).getMediaHandler().post(new Runnable() {
            @Override
            public void run() {
                if (EasyMusicPlayerManager.getCurrentMusicPlay() != null) {
                    EasyMusicPlayerManager.getCurrentMusicPlay().onError(what, extra);
                }
            }
        });
    }

    @Override
    public void onInfo(int what, int extra) {
        EasyMediaManager.getInstance(tag).getMediaHandler().post(new Runnable() {
            @Override
            public void run() {
                if (EasyMusicPlayerManager.getCurrentMusicPlay() != null) {
                    if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                        EasyMusicPlayerManager.getCurrentMusicPlay().onPrepared();
                    } else {
                        EasyMusicPlayerManager.getCurrentMusicPlay().onInfo(what, extra);
                    }
                }
            }
        });
    }

    @Override
    public void onVideoSizeChanged(int width, int height) {
        EasyMediaManager.getInstance(tag).currentVideoWidth = width;
        EasyMediaManager.getInstance(tag).currentVideoHeight = height;
        EasyMediaManager.getInstance(tag).getMediaHandler().post(new Runnable() {
            @Override
            public void run() {
                if (EasyMusicPlayerManager.getCurrentMusicPlay() != null) {
                    EasyMusicPlayerManager.getCurrentMusicPlay().onVideoSizeChanged();
                }
            }
        });
    }

    @Override
    public void onSeekComplete() {
        EasyMediaManager.getInstance(tag).getMediaHandler().post(new Runnable() {
            @Override
            public void run() {
                if (EasyMusicPlayerManager.getCurrentMusicPlay() != null) {
                    EasyMusicPlayerManager.getCurrentMusicPlay().onSeekComplete();
                }
            }
        });
    }

    @Override
    public void onCompletion(EasyMediaInterface easyMediaInterface) {
        EasyMediaManager.getInstance(tag).getMediaHandler().post(new Runnable() {
            @Override
            public void run() {
                if (EasyMusicPlayerManager.getCurrentMusicPlay() != null) {
                    if (!EasyMusicPlayerManager.getCurrentMusicPlay().onAutoCompletion()) {
                        easyMediaInterface.setCurrentDataSource(null);
                    }
                }
            }
        });
    }


}
