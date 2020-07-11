package com.ashlikun.media.video.play;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.text.TextUtils;
import android.view.Surface;
import android.widget.Toast;

import com.ashlikun.media.R;
import com.ashlikun.media.video.EasyMediaInterface;
import com.ashlikun.media.video.EasyMediaManager;
import com.ashlikun.media.video.EasyVideoPlayerManager;
import com.ashlikun.media.video.VideoUtils;

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

public class EasyVideoIjkplayer extends EasyMediaInterface implements IMediaPlayer.OnPreparedListener, IMediaPlayer.OnVideoSizeChangedListener, IMediaPlayer.OnCompletionListener, IMediaPlayer.OnErrorListener, IMediaPlayer.OnInfoListener, IMediaPlayer.OnBufferingUpdateListener, IMediaPlayer.OnSeekCompleteListener, IMediaPlayer.OnTimedTextListener {
    IjkMediaPlayer ijkMediaPlayer;
    /**
     * 是否调用过暂定，调用后在准备好的时候不能直接播放
     */
    private boolean isPreparedPause = false;

    public IjkMediaPlayer getIjkMediaPlayer() {
        return ijkMediaPlayer;
    }

    @Override
    public void setPreparedPause(boolean preparedPause) {
        isPreparedPause = preparedPause;
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


        ijkMediaPlayer.setOnPreparedListener(EasyVideoIjkplayer.this);
        ijkMediaPlayer.setOnVideoSizeChangedListener(EasyVideoIjkplayer.this);
        ijkMediaPlayer.setOnCompletionListener(EasyVideoIjkplayer.this);
        ijkMediaPlayer.setOnErrorListener(EasyVideoIjkplayer.this);
        ijkMediaPlayer.setOnInfoListener(EasyVideoIjkplayer.this);
        ijkMediaPlayer.setOnBufferingUpdateListener(EasyVideoIjkplayer.this);
        ijkMediaPlayer.setOnSeekCompleteListener(EasyVideoIjkplayer.this);
        ijkMediaPlayer.setOnTimedTextListener(EasyVideoIjkplayer.this);

        try {
            if (getCurrentDataSource() == null) {
                Toast.makeText(VideoUtils.mContext, VideoUtils.mContext.getText(R.string.easy_video_no_url), Toast.LENGTH_SHORT).show();
            } else if (!TextUtils.isEmpty(getCurrentDataSource().getUrl())) {
                if (getCurrentDataSource().getHeaders() != null) {
                    ijkMediaPlayer.setDataSource(getCurrentDataSource().getUrl(), getCurrentDataSource().getHeaders());
                } else {
                    ijkMediaPlayer.setDataSource(getCurrentDataSource().getUrl());
                }
            } else if (getCurrentDataSource().getUri() != null && !TextUtils.isEmpty(getCurrentDataSource().getUri().toString())) {
                if (getCurrentDataSource().getHeaders() != null) {
                    ijkMediaPlayer.setDataSource(VideoUtils.mContext, getCurrentDataSource().getUri(), getCurrentDataSource().getHeaders());
                } else {
                    ijkMediaPlayer.setDataSource(VideoUtils.mContext, getCurrentDataSource().getUri());
                }
            } else if (getCurrentDataSource().getFileDescriptor() != null) {
                ijkMediaPlayer.setDataSource(getCurrentDataSource().getFileDescriptor().getFileDescriptor());
            } else {
                Toast.makeText(VideoUtils.mContext, VideoUtils.mContext.getText(R.string.easy_video_no_url), Toast.LENGTH_SHORT).show();
            }
            ijkMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            ijkMediaPlayer.setScreenOnWhilePlaying(true);
            ijkMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(VideoUtils.mContext, VideoUtils.mContext.getText(R.string.easy_video_no_url), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void start() {
        if (ijkMediaPlayer != null) {
            ijkMediaPlayer.start();
        }
    }

    @Override
    public void pause() {
        if (ijkMediaPlayer != null) {
            ijkMediaPlayer.pause();
        }
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
            ijkMediaPlayer = null;
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
        if (!isPreparedPause) {
            ijkMediaPlayer.start();
            EasyMediaManager.getInstance().mainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (EasyVideoPlayerManager.getCurrentVideoPlay() != null) {
                        EasyVideoPlayerManager.getCurrentVideoPlay().onPrepared();
                    }
                }
            });
        } else {
            isPreparedPause = false;
        }
    }

    @Override
    public void onVideoSizeChanged(IMediaPlayer iMediaPlayer, int i, int i1, int i2, int i3) {
        EasyMediaManager.getInstance().currentVideoWidth = iMediaPlayer.getVideoWidth();
        EasyMediaManager.getInstance().currentVideoHeight = iMediaPlayer.getVideoHeight();
        EasyMediaManager.getInstance().mainThreadHandler.post(new Runnable() {
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
        EasyMediaManager.getInstance().mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (EasyVideoPlayerManager.getCurrentVideoPlay() != null) {
                    EasyVideoPlayerManager.getCurrentVideoPlay().onAutoCompletion();
                    setCurrentDataSource(null);
                }
            }
        });
    }

    @Override
    public boolean onError(IMediaPlayer iMediaPlayer, final int what, final int extra) {
        EasyMediaManager.getInstance().mainThreadHandler.post(new Runnable() {
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
        EasyMediaManager.getInstance().mainThreadHandler.post(new Runnable() {
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
        EasyMediaManager.getInstance().mainThreadHandler.post(new Runnable() {
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
        EasyMediaManager.getInstance().mainThreadHandler.post(new Runnable() {
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
