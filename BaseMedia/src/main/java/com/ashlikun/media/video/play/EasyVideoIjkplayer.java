package com.ashlikun.media.video.play;

import android.media.AudioManager;
import android.text.TextUtils;
import android.util.Log;
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
        //跳帧处理,放CPU处理较慢时，进行跳帧处理，保证播放流程，画面和声音同步
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 0);
        //设置是否开启环路过滤: 0开启，画面质量高，解码开销大，48关闭，画面质量差点，解码开销小
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48);
        //设置播放前的探测时间 1,达到首屏秒开效果
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzeduration", 1);
        //播放前的探测Size，默认是1M, 改小一点会出画面更快
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 10240L);
        //每处理一个packet之后刷新io上下文
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "flush_packets", 1L);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "reconnect", 1);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-hevc", 1);//支持高清
//        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0);//是否开启预缓冲
//        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT,"http_proxy", "http://192.168.1.6:8888");
//        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "min-frames", 100);
//        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "http-detect-range-support", 0);
        //是否开启预缓冲，一般直播项目会开启，达到秒开的效果，不过带来了播放丢帧卡顿的体验
//        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0L);
//        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "dns_cache_clear", 1);
//        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "timeout", 20000);
//        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "buffer_size", 1316);
//        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "infbuf", 1);  // 无限读
//        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzemaxduration", 100L);
//        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "dns_cache_timeout", -1);

        ijkMediaPlayer.setOnPreparedListener(EasyVideoIjkplayer.this);
        ijkMediaPlayer.setOnVideoSizeChangedListener(EasyVideoIjkplayer.this);
        ijkMediaPlayer.setOnCompletionListener(EasyVideoIjkplayer.this);
        ijkMediaPlayer.setOnErrorListener(EasyVideoIjkplayer.this);
        ijkMediaPlayer.setOnInfoListener(EasyVideoIjkplayer.this);
        ijkMediaPlayer.setOnBufferingUpdateListener(EasyVideoIjkplayer.this);
        ijkMediaPlayer.setOnSeekCompleteListener(EasyVideoIjkplayer.this);
        ijkMediaPlayer.setOnTimedTextListener(EasyVideoIjkplayer.this);
//        ijkMediaPlayer.setLogEnabled(true);
        if (VideoUtils.getOnCreateIjkplay() != null) {
            VideoUtils.getOnCreateIjkplay().onCreate(ijkMediaPlayer);
        }
        ijkMediaPlayer.setLooping(getCurrentDataSource().isLooping());
        try {
            if (getCurrentDataSource() == null) {
                Toast.makeText(context, context.getText(R.string.easy_video_no_url), Toast.LENGTH_SHORT).show();
                return;
            } else if (!TextUtils.isEmpty(getCurrentDataSource().getUrl())) {
                if (getCurrentDataSource().getHeaders() != null) {
                    ijkMediaPlayer.setDataSource(getCurrentDataSource().getUrl(), getCurrentDataSource().getHeaders());
                } else {
                    ijkMediaPlayer.setDataSource(getCurrentDataSource().getUrl());
                }
            } else if (getCurrentDataSource().getUri() != null && !TextUtils.isEmpty(getCurrentDataSource().getUri().toString())) {
                if (getCurrentDataSource().getHeaders() != null) {
                    ijkMediaPlayer.setDataSource(context, getCurrentDataSource().getUri(), getCurrentDataSource().getHeaders());
                } else {
                    ijkMediaPlayer.setDataSource(context, getCurrentDataSource().getUri());
                }
            } else if (getCurrentDataSource().getIMediaDataSource() != null) {
                ijkMediaPlayer.setDataSource(getCurrentDataSource().getIMediaDataSource());
            } else if (getCurrentDataSource().getFileDescriptor() != null) {
                ijkMediaPlayer.setDataSource(getCurrentDataSource().getFileDescriptor());
            } else {
                Toast.makeText(context, context.getText(R.string.easy_video_no_url), Toast.LENGTH_SHORT).show();
                return;
            }
            ijkMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            ijkMediaPlayer.setScreenOnWhilePlaying(true);
            ijkMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, context.getText(R.string.easy_video_no_url), Toast.LENGTH_SHORT).show();
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
    public void seekTo(long time) {
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
    public long getCurrentPosition() {
        if (ijkMediaPlayer == null) {
            return 0;
        }
        return ijkMediaPlayer.getCurrentPosition();
    }

    @Override
    public int getBufferedPercentage() {
        return -1;
    }

    @Override
    public long getDuration() {
        if (ijkMediaPlayer == null) {
            return 0;
        }
        return ijkMediaPlayer.getDuration();
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
            EasyMediaManager.getInstance().getMediaHandler().post(new Runnable() {
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
        EasyMediaManager.getInstance().getMediaHandler().post(new Runnable() {
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
        EasyMediaManager.getInstance().getMediaHandler().post(new Runnable() {
            @Override
            public void run() {
                if (EasyVideoPlayerManager.getCurrentVideoPlay() != null) {
                    if (!EasyVideoPlayerManager.getCurrentVideoPlay().onAutoCompletion()) {
                        setCurrentDataSource(null);
                    }
                }
            }
        });
    }

    @Override
    public boolean onError(IMediaPlayer iMediaPlayer, final int what, final int extra) {
        Log.e("onError", "what = " + what + "    extra = " + extra);
        EasyMediaManager.getInstance().getMediaHandler().post(new Runnable() {
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
        EasyMediaManager.getInstance().getMediaHandler().post(new Runnable() {
            @Override
            public void run() {
                if (EasyVideoPlayerManager.getCurrentVideoPlay() != null) {
                    if (what == IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
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
        Log.e("onBufferingUpdate", percent + "");
        EasyMediaManager.getInstance().getMediaHandler().post(new Runnable() {
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
        EasyMediaManager.getInstance().getMediaHandler().post(new Runnable() {
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


    public interface OnCreateIjkplay {
        /**
         * 对IjkPlayer的一些其他配置
         *
         * @param ijkMediaPlayer
         */
        public void onCreate(IjkMediaPlayer ijkMediaPlayer);
    }
}
