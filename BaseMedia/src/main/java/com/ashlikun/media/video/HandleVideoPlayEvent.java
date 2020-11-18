package com.ashlikun.media.video;

import android.media.MediaPlayer;

/**
 * 作者　　: 李坤
 * 创建时间: 2020/11/18　17:40
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：
 */
public class HandleVideoPlayEvent implements HandlePlayEvent {
    String tag;

    public HandleVideoPlayEvent(String tag) {
        this.tag = tag;
    }

    @Override
    public void onPause() {
        EasyMediaManager.getInstance(tag).getMediaHandler().post(new Runnable() {
            @Override
            public void run() {
                if (EasyVideoPlayerManager.getCurrentVideoPlay() != null) {
                    EasyVideoPlayerManager.getCurrentVideoPlay().onPause();
                }
            }
        });
    }

    @Override
    public void onPrepared() {
        EasyMediaManager.getInstance(tag).getMediaHandler().post(new Runnable() {
            @Override
            public void run() {
                if (EasyVideoPlayerManager.getCurrentVideoPlay() != null) {
                    EasyVideoPlayerManager.getCurrentVideoPlay().onPrepared();
                }
            }
        });
    }

    @Override
    public void setBufferProgress(int percent) {
        EasyMediaManager.getInstance(tag).getMediaHandler().post(new Runnable() {
            @Override
            public void run() {
                if (EasyVideoPlayerManager.getCurrentVideoPlay() != null) {
                    EasyVideoPlayerManager.getCurrentVideoPlay().setBufferProgress(percent);
                }
            }
        });
    }

    @Override
    public void onError(int what, int extra) {
        EasyMediaManager.getInstance(tag).getMediaHandler().post(new Runnable() {
            @Override
            public void run() {
                if (EasyVideoPlayerManager.getCurrentVideoPlay() != null) {
                    EasyVideoPlayerManager.getCurrentVideoPlay().onError(what, extra);
                }
            }
        });
    }

    @Override
    public void onInfo(int what, int extra) {
        EasyMediaManager.getInstance(tag).getMediaHandler().post(new Runnable() {
            @Override
            public void run() {
                if (EasyVideoPlayerManager.getCurrentVideoPlay() != null) {
                    if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                        EasyVideoPlayerManager.getCurrentVideoPlay().onPrepared();
                    } else {
                        EasyVideoPlayerManager.getCurrentVideoPlay().onInfo(what, extra);
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
                if (EasyVideoPlayerManager.getCurrentVideoPlay() != null) {
                    EasyVideoPlayerManager.getCurrentVideoPlay().onVideoSizeChanged();
                }
            }
        });
    }

    @Override
    public void onSeekComplete() {
        EasyMediaManager.getInstance(tag).getMediaHandler().post(new Runnable() {
            @Override
            public void run() {
                if (EasyVideoPlayerManager.getCurrentVideoPlay() != null) {
                    EasyVideoPlayerManager.getCurrentVideoPlay().onSeekComplete();
                }
            }
        });
    }

    @Override
    public void onCompletion(EasyMediaInterface easyMediaInterface) {
        EasyMediaManager.getInstance(tag).getMediaHandler().post(new Runnable() {
            @Override
            public void run() {
                if (EasyVideoPlayerManager.getCurrentVideoPlay() != null) {
                    if (!EasyVideoPlayerManager.getCurrentVideoPlay().onAutoCompletion()) {
                        easyMediaInterface.setCurrentDataSource(null);
                    }
                }
            }
        });
    }


}
