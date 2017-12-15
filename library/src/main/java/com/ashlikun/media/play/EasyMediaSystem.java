package com.ashlikun.media.play;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.view.Surface;

import com.ashlikun.media.EasyMediaInterface;
import com.ashlikun.media.EasyMediaManager;
import com.ashlikun.media.EasyVideoPlayerManager;

/**
 * 作者　　: 李坤
 * 创建时间: 2017/12/01 16:25
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：实现系统的播放引擎
 */

public class EasyMediaSystem extends EasyMediaInterface
        implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnInfoListener,
        MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnVideoSizeChangedListener {

    public MediaPlayer mediaPlayer;

    @Override
    public void start() {
        mediaPlayer.start();
    }

    @Override
    public void stop() {
        mediaPlayer.stop();
    }

    @Override
    public void prepare() {
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            //  mediaPlayer.setLooping((boolean) dataSource[1]);
            mediaPlayer.setOnPreparedListener(EasyMediaSystem.this);
            mediaPlayer.setOnCompletionListener(EasyMediaSystem.this);
            mediaPlayer.setOnBufferingUpdateListener(EasyMediaSystem.this);
            mediaPlayer.setScreenOnWhilePlaying(true);
            mediaPlayer.setOnErrorListener(EasyMediaSystem.this);
            mediaPlayer.setOnInfoListener(EasyMediaSystem.this);
            mediaPlayer.setOnVideoSizeChangedListener(EasyMediaSystem.this);
            mediaPlayer.setOnSeekCompleteListener(this);
            mediaPlayer.setDataSource(currentDataSource.toString());
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void pause() {
        mediaPlayer.pause();
    }

    @Override
    public boolean isPlaying() {
        if (mediaPlayer != null) {
            return mediaPlayer.isPlaying();
        }
        return false;
    }

    @Override
    public void seekTo(int time) {
        mediaPlayer.seekTo(time);
    }

    @Override
    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }

    @Override
    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    @Override
    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    @Override
    public void setSurface(Surface surface) {
        mediaPlayer.setSurface(surface);
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
        if (currentDataSource.toString().toLowerCase().contains("mp3")) {
            EasyMediaManager.instance().mainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (EasyVideoPlayerManager.getCurrentVideoPlayer() != null) {
                        EasyVideoPlayerManager.getCurrentVideoPlayer().onPrepared();
                    }
                }
            });
        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        EasyMediaManager.instance().mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (EasyVideoPlayerManager.getCurrentVideoPlayer() != null) {
                    EasyVideoPlayerManager.getCurrentVideoPlayer().onAutoCompletion();
                }
            }
        });
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, final int percent) {
        EasyMediaManager.instance().mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (EasyVideoPlayerManager.getCurrentVideoPlayer() != null) {
                    EasyVideoPlayerManager.getCurrentVideoPlayer().setBufferProgress(percent);
                }
            }
        });
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, final int what, final int extra) {
        EasyMediaManager.instance().mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (EasyVideoPlayerManager.getCurrentVideoPlayer() != null) {
                    EasyVideoPlayerManager.getCurrentVideoPlayer().onError(what, extra);
                }
            }
        });
        return true;
    }

    @Override
    public boolean onInfo(MediaPlayer mediaPlayer, final int what, final int extra) {
        EasyMediaManager.instance().mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (EasyVideoPlayerManager.getCurrentVideoPlayer() != null) {
                    if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                        EasyVideoPlayerManager.getCurrentVideoPlayer().onPrepared();
                    } else {
                        EasyVideoPlayerManager.getCurrentVideoPlayer().onInfo(what, extra);
                    }
                }
            }
        });
        return false;
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mediaPlayer, int width, int height) {
        EasyMediaManager.instance().currentVideoWidth = width;
        EasyMediaManager.instance().currentVideoHeight = height;
        EasyMediaManager.instance().mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (EasyVideoPlayerManager.getCurrentVideoPlayer() != null) {
                    EasyVideoPlayerManager.getCurrentVideoPlayer().onVideoSizeChanged();
                }
            }
        });
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        EasyMediaManager.instance().mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (EasyVideoPlayerManager.getCurrentVideoPlayer() != null) {
                    EasyVideoPlayerManager.getCurrentVideoPlayer().onSeekComplete();
                }
            }
        });
    }
}
