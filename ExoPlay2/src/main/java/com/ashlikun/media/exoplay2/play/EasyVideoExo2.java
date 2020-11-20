package com.ashlikun.media.exoplay2.play;

import android.media.AudioManager;
import android.net.Uri;
import android.text.TextUtils;
import android.view.Surface;
import android.widget.Toast;

import com.ashlikun.media.R;
import com.ashlikun.media.exoplay2.IjkExo2MediaPlayer;
import com.ashlikun.media.video.EasyMediaInterface;
import com.ashlikun.media.video.EasyMediaManager;

import java.io.File;

import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * 作者　　: 李坤
 * 创建时间: 2017/12/01 16:25
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：实现Exo2的播放引擎
 */
public class EasyVideoExo2 extends EasyMediaInterface
        implements IjkExo2MediaPlayer.OnPreparedListener,
        IjkExo2MediaPlayer.OnCompletionListener,
        IjkExo2MediaPlayer.OnBufferingUpdateListener,
        IjkExo2MediaPlayer.OnErrorListener,
        IjkExo2MediaPlayer.OnInfoListener,
        IjkExo2MediaPlayer.OnSeekCompleteListener,
        IjkExo2MediaPlayer.OnVideoSizeChangedListener {
    /**
     * 是否调用过暂定，调用后在准备好的时候不能直接播放
     */
    private boolean isPreparedPause = false;
    public IjkExo2MediaPlayer mediaPlayer;

    @Override
    public void setPreparedPause(boolean preparedPause) {
        isPreparedPause = preparedPause;
    }

    @Override
    public void start() {
        //暂停不在同一个管理器的播放器
        EasyMediaManager.pauseOther(easyMediaManager);
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }
    }

    @Override
    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    @Override
    public void prepare() {
        try {
            if (isPlaying()) {
                stop();
            }
            release();
        } catch (Exception e) {
        }
        mediaPlayer = new IjkExo2MediaPlayer(context);

        if (getCurrentDataSource() == null) {
            Toast.makeText(context, context.getText(R.string.easy_video_no_url), Toast.LENGTH_SHORT).show();
            onError(mediaPlayer, -2, -2);
            return;
        }

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(EasyVideoExo2.this);
        mediaPlayer.setOnCompletionListener(EasyVideoExo2.this);
        mediaPlayer.setOnBufferingUpdateListener(EasyVideoExo2.this);
        mediaPlayer.setScreenOnWhilePlaying(true);
        mediaPlayer.setOnErrorListener(EasyVideoExo2.this);
        mediaPlayer.setOnInfoListener(EasyVideoExo2.this);
        mediaPlayer.setOnVideoSizeChangedListener(EasyVideoExo2.this);
        mediaPlayer.setOnSeekCompleteListener(this);

        mediaPlayer.setLooping(getCurrentDataSource().isLooping());

        //通过自己的内部缓存机制
        mediaPlayer.setCache(getCurrentDataSource().isCache());
        if (!TextUtils.isEmpty(getCurrentDataSource().cacheDir())) {
            mediaPlayer.setCacheDir(new File(getCurrentDataSource().cacheDir()));
        }
        mediaPlayer.setOverrideExtension(getCurrentDataSource().overrideExtension());

        try {
            if (!TextUtils.isEmpty(getCurrentDataSource().getUrl())) {
                if (getCurrentDataSource().getHeaders() != null) {
                    mediaPlayer.setDataSource(context, Uri.parse(getCurrentDataSource().getUrl()), getCurrentDataSource().getHeaders());
                } else {
                    mediaPlayer.setDataSource(getCurrentDataSource().getUrl());
                }
            } else if (getCurrentDataSource().getUri() != null && !TextUtils.isEmpty(getCurrentDataSource().getUri().toString())) {
                if (getCurrentDataSource().getHeaders() != null) {
                    mediaPlayer.setDataSource(context, getCurrentDataSource().getUri(), getCurrentDataSource().getHeaders());
                } else {
                    mediaPlayer.setDataSource(context, getCurrentDataSource().getUri());
                }
            } else if (getCurrentDataSource().getIMediaDataSource() != null) {
                mediaPlayer.setDataSource(getCurrentDataSource().getIMediaDataSource());
            } else if (getCurrentDataSource().getFileDescriptor() != null) {
                mediaPlayer.setDataSource(getCurrentDataSource().getFileDescriptor());
            } else {
                Toast.makeText(context, context.getText(R.string.easy_video_no_url), Toast.LENGTH_SHORT).show();
                onError(mediaPlayer, -2, -2);
                return;
            }
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setScreenOnWhilePlaying(true);
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, context.getText(R.string.easy_video_no_url), Toast.LENGTH_SHORT).show();
            onError(mediaPlayer, -2, -2);
        }
    }

    @Override
    public void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
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
    public void seekTo(long time) {
        if (mediaPlayer == null) {
            return;
        }
        mediaPlayer.seekTo((int) time);
    }

    @Override
    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public long getCurrentPosition() {
        if (mediaPlayer == null) {
            return 0;
        }
        return mediaPlayer.getCurrentPosition();
    }

    @Override
    public int getBufferedPercentage() {
        if (mediaPlayer == null) {
            return 0;
        }
        return mediaPlayer.getBufferedPercentage();
    }

    @Override
    public long getDuration() {
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
    public void onPrepared(IMediaPlayer mediaPlayer) {
        if (mediaPlayer == null) {
            return;
        }
        if (!isPreparedPause) {
            mediaPlayer.start();
            if (easyMediaManager.getMediaPlay() == this)
                easyMediaManager.getHandlePlayEvent().onPrepared();
        } else {
            isPreparedPause = false;
        }
    }

    @Override
    public void onCompletion(IMediaPlayer mediaPlayer) {
        if (easyMediaManager.getMediaPlay() == this)
            easyMediaManager.getHandlePlayEvent().onCompletion(this);
    }

    @Override
    public void onBufferingUpdate(IMediaPlayer mediaPlayer, final int percent) {
        if (easyMediaManager.getMediaPlay() == this)
            easyMediaManager.getHandlePlayEvent().setBufferProgress(percent);
    }

    @Override
    public boolean onError(IMediaPlayer mediaPlayer, final int what, final int extra) {
        if (easyMediaManager.getMediaPlay() == this)
            easyMediaManager.getHandlePlayEvent().onError(what, extra);
        return true;
    }

    @Override
    public boolean onInfo(IMediaPlayer mediaPlayer, final int what, final int extra) {
        if (easyMediaManager.getMediaPlay() == this)
            easyMediaManager.getHandlePlayEvent().onInfo(what, extra);
        return false;
    }

    @Override
    public void onVideoSizeChanged(IMediaPlayer mediaPlayer, int width, int height, int sar_num, int sar_den) {
        if (easyMediaManager.getMediaPlay() == this)
            easyMediaManager.getHandlePlayEvent().onVideoSizeChanged(width, height);
    }

    @Override
    public void onSeekComplete(IMediaPlayer mp) {
        if (easyMediaManager.getMediaPlay() == this)
            easyMediaManager.getHandlePlayEvent().onSeekComplete();
    }
}
