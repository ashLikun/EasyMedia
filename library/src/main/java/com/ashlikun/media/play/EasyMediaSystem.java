package com.ashlikun.media.play;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.text.TextUtils;
import android.view.Surface;
import android.widget.Toast;

import com.ashlikun.media.EasyMediaInterface;
import com.ashlikun.media.EasyMediaManager;
import com.ashlikun.media.EasyVideoPlayerManager;
import com.ashlikun.media.MediaUtils;
import com.ashlikun.media.R;

import java.lang.reflect.Method;
import java.util.Map;

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
        if (mediaPlayer == null) {
            return;
        }
        mediaPlayer.start();
    }

    @Override
    public void stop() {
        if (mediaPlayer == null) {
            return;
        }
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
            if (!TextUtils.isEmpty(currentDataSource.getUrl())) {
                if (currentDataSource.getHeaders() != null) {
                    Class<MediaPlayer> clazz = MediaPlayer.class;
                    Method method = clazz.getDeclaredMethod("setDataSource", String.class, Map.class);
                    method.invoke(mediaPlayer, currentDataSource.getUrl(), currentDataSource.getHeaders());
                } else {
                    mediaPlayer.setDataSource(currentDataSource.getUrl());
                }
            } else if (currentDataSource.getUri() != null && !TextUtils.isEmpty(currentDataSource.getUri().toString())) {
                if (currentDataSource.getHeaders() != null) {
                    mediaPlayer.setDataSource(MediaUtils.mContext, currentDataSource.getUri(), currentDataSource.getHeaders());
                } else {
                    mediaPlayer.setDataSource(MediaUtils.mContext, currentDataSource.getUri());
                }
            } else if (currentDataSource.getFileDescriptor() != null) {
                mediaPlayer.setDataSource(currentDataSource.getFileDescriptor().getFileDescriptor(),
                        currentDataSource.getFileDescriptor().getStartOffset(), currentDataSource.getFileDescriptor().getDeclaredLength());
            } else {
                Toast.makeText(MediaUtils.mContext, MediaUtils.mContext.getText(R.string.no_url), Toast.LENGTH_SHORT).show();
            }


            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void pause() {
        if (mediaPlayer == null) {
            return;
        }
        mediaPlayer.pause();
    }

    @Override
    public boolean isPlaying() {
        if (mediaPlayer == null) {
            return false;
        }
        if (mediaPlayer != null) {
            return mediaPlayer.isPlaying();
        }
        return false;
    }

    @Override
    public void seekTo(int time) {
        if (mediaPlayer == null) {
            return;
        }
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
        if (mediaPlayer == null) {
            return 0;
        }
        return mediaPlayer.getCurrentPosition();
    }

    @Override
    public int getDuration() {
        if (mediaPlayer == null) {
            return 0;
        }
        return mediaPlayer.getDuration();
    }

    @Override
    public void setSurface(Surface surface) {
        if (mediaPlayer == null) {
            return;
        }
        mediaPlayer.setSurface(surface);
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        if (mediaPlayer == null) {
            return;
        }
        mediaPlayer.start();
        if (currentDataSource.toString().toLowerCase().contains("mp3")) {
            EasyMediaManager.instance().mainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (EasyVideoPlayerManager.getCurrentVideoPlay() != null) {
                        EasyVideoPlayerManager.getCurrentVideoPlay().onPrepared();
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
                if (EasyVideoPlayerManager.getCurrentVideoPlay() != null) {
                    EasyVideoPlayerManager.getCurrentVideoPlay().onAutoCompletion();
                    currentDataSource = null;
                }
            }
        });
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, final int percent) {
        EasyMediaManager.instance().mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (EasyVideoPlayerManager.getCurrentVideoPlay() != null) {
                    EasyVideoPlayerManager.getCurrentVideoPlay().setBufferProgress(percent);
                }
            }
        });
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, final int what, final int extra) {
        EasyMediaManager.instance().mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (EasyVideoPlayerManager.getCurrentVideoPlay() != null) {
                    EasyVideoPlayerManager.getCurrentVideoPlay().onError(what, extra);
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
                if (EasyVideoPlayerManager.getCurrentVideoPlay() != null) {
                    if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                        EasyVideoPlayerManager.getCurrentVideoPlay().onPrepared();
                    } else {
                        EasyVideoPlayerManager.getCurrentVideoPlay().onInfo(what, extra);
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
                if (EasyVideoPlayerManager.getCurrentVideoPlay() != null) {
                    EasyVideoPlayerManager.getCurrentVideoPlay().onVideoSizeChanged();
                }
            }
        });
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        EasyMediaManager.instance().mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (EasyVideoPlayerManager.getCurrentVideoPlay() != null) {
                    EasyVideoPlayerManager.getCurrentVideoPlay().onSeekComplete();
                }
            }
        });
    }
}
