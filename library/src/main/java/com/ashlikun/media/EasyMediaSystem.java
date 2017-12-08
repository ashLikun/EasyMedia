package com.ashlikun.media;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.view.Surface;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Created by Nathen on 2017/11/8.
 * 实现系统的播放引擎
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
    public void prepare() {
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            if (dataSourceObjects.length > 1) {
                mediaPlayer.setLooping((boolean) dataSourceObjects[1]);
            }
            mediaPlayer.setOnPreparedListener(EasyMediaSystem.this);
            mediaPlayer.setOnCompletionListener(EasyMediaSystem.this);
            mediaPlayer.setOnBufferingUpdateListener(EasyMediaSystem.this);
            mediaPlayer.setScreenOnWhilePlaying(true);
            mediaPlayer.setOnErrorListener(EasyMediaSystem.this);
            mediaPlayer.setOnInfoListener(EasyMediaSystem.this);
            mediaPlayer.setOnVideoSizeChangedListener(EasyMediaSystem.this);
            mediaPlayer.setOnSeekCompleteListener(this);
            Class<MediaPlayer> clazz = MediaPlayer.class;
            Method method = clazz.getDeclaredMethod("setDataSource", String.class, Map.class);
            if (dataSourceObjects.length > 2) {
                method.invoke(mediaPlayer, currentDataSource.toString(), dataSourceObjects[2]);
            } else {
                method.invoke(mediaPlayer, currentDataSource.toString(), null);
            }
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
        return mediaPlayer.isPlaying();
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
