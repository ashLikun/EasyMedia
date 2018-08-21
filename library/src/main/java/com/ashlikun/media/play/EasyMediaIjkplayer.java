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

import java.io.IOException;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.player.IjkTimedText;

/**
 * 作者　　: 李坤
 * 创建时间: 2017/12/15 17:02
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：实现Ijkplayer的播放引擎
 */

public class EasyMediaIjkplayer extends EasyMediaInterface implements IMediaPlayer.OnPreparedListener, IMediaPlayer.OnVideoSizeChangedListener, IMediaPlayer.OnCompletionListener, IMediaPlayer.OnErrorListener, IMediaPlayer.OnInfoListener, IMediaPlayer.OnBufferingUpdateListener, IMediaPlayer.OnSeekCompleteListener, IMediaPlayer.OnTimedTextListener {
    IjkMediaPlayer ijkMediaPlayer;

    @Override
    public void start() {
        ijkMediaPlayer.start();
    }


    @Override
    public void prepare() {

        ijkMediaPlayer = new IjkMediaPlayer();
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 0);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", 0);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "overlay-format", IjkMediaPlayer.SDL_FCC_RV32);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 0);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "http-detect-range-support", 0);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48);
        ijkMediaPlayer.setOnPreparedListener(EasyMediaIjkplayer.this);
        ijkMediaPlayer.setOnVideoSizeChangedListener(EasyMediaIjkplayer.this);
        ijkMediaPlayer.setOnCompletionListener(EasyMediaIjkplayer.this);
        ijkMediaPlayer.setOnErrorListener(EasyMediaIjkplayer.this);
        ijkMediaPlayer.setOnInfoListener(EasyMediaIjkplayer.this);
        ijkMediaPlayer.setOnBufferingUpdateListener(EasyMediaIjkplayer.this);
        ijkMediaPlayer.setOnSeekCompleteListener(EasyMediaIjkplayer.this);
        ijkMediaPlayer.setOnTimedTextListener(EasyMediaIjkplayer.this);

        try {
            if (currentDataSource == null) {
                Toast.makeText(MediaUtils.mContext, MediaUtils.mContext.getText(R.string.no_url), Toast.LENGTH_SHORT).show();
            } else if (!TextUtils.isEmpty(currentDataSource.getUrl())) {
                if (currentDataSource.getHeaders() != null) {
                    ijkMediaPlayer.setDataSource(currentDataSource.getUrl(), currentDataSource.getHeaders());
                } else {
                    ijkMediaPlayer.setDataSource(currentDataSource.getUrl());
                }
            } else if (currentDataSource.getUri() != null && !TextUtils.isEmpty(currentDataSource.getUri().toString())) {
                if (currentDataSource.getHeaders() != null) {
                    ijkMediaPlayer.setDataSource(MediaUtils.mContext, currentDataSource.getUri(), currentDataSource.getHeaders());
                } else {
                    ijkMediaPlayer.setDataSource(MediaUtils.mContext, currentDataSource.getUri());
                }
            } else if (currentDataSource.getFileDescriptor() != null) {
                ijkMediaPlayer.setDataSource(currentDataSource.getFileDescriptor().getFileDescriptor());
            } else {
                Toast.makeText(MediaUtils.mContext, MediaUtils.mContext.getText(R.string.no_url), Toast.LENGTH_SHORT).show();
            }
            ijkMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            ijkMediaPlayer.setScreenOnWhilePlaying(true);
            ijkMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(MediaUtils.mContext, MediaUtils.mContext.getText(R.string.no_url), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void pause() {
        ijkMediaPlayer.pause();
    }

    @Override
    public boolean isPlaying() {
        if (ijkMediaPlayer != null) {
            return ijkMediaPlayer.isPlaying();
        }
        return false;
    }

    @Override
    public void seekTo(int time) {
        ijkMediaPlayer.seekTo(time);
    }

    @Override
    public void release() {
        if (ijkMediaPlayer != null) {
            ijkMediaPlayer.release();
        }
    }

    @Override
    public int getCurrentPosition() {
        if (ijkMediaPlayer == null) {
            return 0;
        }
        return (int) ijkMediaPlayer.getCurrentPosition();
    }

    @Override
    public int getDuration() {
        if (ijkMediaPlayer == null) {
            return 0;
        }
        return (int) ijkMediaPlayer.getDuration();
    }

    @Override
    public void setSurface(Surface surface) {
        if (ijkMediaPlayer == null) {
            return;
        }
        ijkMediaPlayer.setSurface(surface);
    }

    @Override
    public void stop() {
        if (ijkMediaPlayer == null) {
            return;
        }
        ijkMediaPlayer.stop();
    }

    @Override
    public void onPrepared(IMediaPlayer iMediaPlayer) {
        if (ijkMediaPlayer == null) {
            return;
        }
        ijkMediaPlayer.start();
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
    public void onVideoSizeChanged(IMediaPlayer iMediaPlayer, int i, int i1, int i2, int i3) {
        EasyMediaManager.instance().currentVideoWidth = iMediaPlayer.getVideoWidth();
        EasyMediaManager.instance().currentVideoHeight = iMediaPlayer.getVideoHeight();
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
    public void onCompletion(IMediaPlayer iMediaPlayer) {
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
    public boolean onError(IMediaPlayer iMediaPlayer, final int what, final int extra) {
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
    public boolean onInfo(IMediaPlayer iMediaPlayer, final int what, final int extra) {
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
    public void onBufferingUpdate(IMediaPlayer iMediaPlayer, final int percent) {
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
    public void onSeekComplete(IMediaPlayer iMediaPlayer) {
        EasyMediaManager.instance().mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (EasyVideoPlayerManager.getCurrentVideoPlay() != null) {
                    EasyVideoPlayerManager.getCurrentVideoPlay().onSeekComplete();
                }
            }
        });
    }

    @Override
    public void onTimedText(IMediaPlayer iMediaPlayer, IjkTimedText ijkTimedText) {

    }
}
