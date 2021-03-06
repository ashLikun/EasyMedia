package com.ashlikun.media.video.play;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.text.TextUtils;
import android.view.Surface;
import android.widget.Toast;

import com.ashlikun.media.R;
import com.ashlikun.media.video.EasyMediaInterface;
import com.ashlikun.media.video.EasyMediaManager;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * 作者　　: 李坤
 * 创建时间: 2017/12/01 16:25
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：实现系统的播放引擎
 */

public class EasyVideoSystem extends EasyMediaInterface
        implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnInfoListener,
        MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnVideoSizeChangedListener {
    /**
     * 是否调用过暂定，调用后在准备好的时候不能直接播放
     */
    private boolean isPreparedPause = false;
    public MediaPlayer mediaPlayer;

    @Override
    public void setPreparedPause(boolean preparedPause) {
        isPreparedPause = preparedPause;
    }

    @Override
    public void start() {
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
            mediaPlayer = new MediaPlayer();
            if (getCurrentDataSource() == null) {
                Toast.makeText(context, context.getText(R.string.easy_video_no_url), Toast.LENGTH_SHORT).show();
                onError(mediaPlayer, -2, -2);
                return;
            }
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            //  mediaPlayer.setLooping((boolean) dataSource[1]);
            mediaPlayer.setOnPreparedListener(EasyVideoSystem.this);
            mediaPlayer.setOnCompletionListener(EasyVideoSystem.this);
            mediaPlayer.setOnBufferingUpdateListener(EasyVideoSystem.this);
            mediaPlayer.setScreenOnWhilePlaying(true);
            mediaPlayer.setOnErrorListener(EasyVideoSystem.this);
            mediaPlayer.setOnInfoListener(EasyVideoSystem.this);
            mediaPlayer.setOnVideoSizeChangedListener(EasyVideoSystem.this);
            mediaPlayer.setOnSeekCompleteListener(this);
            mediaPlayer.setLooping(getCurrentDataSource().isLooping());
            if (!TextUtils.isEmpty(getCurrentDataSource().getUrl())) {
                if (getCurrentDataSource().getHeaders() != null) {
                    Class<MediaPlayer> clazz = MediaPlayer.class;
                    Method method = clazz.getDeclaredMethod("setDataSource", String.class, Map.class);
                    method.invoke(mediaPlayer, getCurrentDataSource().getUrl(), getCurrentDataSource().getHeaders());
                } else {
                    mediaPlayer.setDataSource(getCurrentDataSource().getUrl());
                }
            } else if (getCurrentDataSource().getUri() != null && !TextUtils.isEmpty(getCurrentDataSource().getUri().toString())) {
                if (getCurrentDataSource().getHeaders() != null) {
                    mediaPlayer.setDataSource(context, getCurrentDataSource().getUri(), getCurrentDataSource().getHeaders());
                } else {
                    mediaPlayer.setDataSource(context, getCurrentDataSource().getUri());
                }
            } else if (getCurrentDataSource().getFileDescriptor() != null) {
                mediaPlayer.setDataSource(getCurrentDataSource().getFileDescriptor());
            } else {
                Toast.makeText(context, context.getText(R.string.easy_video_no_url), Toast.LENGTH_SHORT).show();
                onError(mediaPlayer, -2, -2);
                return;
            }
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
        return -1;
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
    public void onPrepared(MediaPlayer mediaPlayer) {
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
    public void onCompletion(MediaPlayer mediaPlayer) {
        if (easyMediaManager.getMediaPlay() == this)
            easyMediaManager.getHandlePlayEvent().onCompletion(this);
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, final int percent) {
        if (easyMediaManager.getMediaPlay() == this)
            easyMediaManager.getHandlePlayEvent().setBufferProgress(percent);
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, final int what, final int extra) {
        if (easyMediaManager.getMediaPlay() == this)
            easyMediaManager.getHandlePlayEvent().onError(what, extra);
        return true;
    }

    @Override
    public boolean onInfo(MediaPlayer mediaPlayer, final int what, final int extra) {
        if (easyMediaManager.getMediaPlay() == this)
            easyMediaManager.getHandlePlayEvent().onInfo(what, extra);
        return false;
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mediaPlayer, int width, int height) {
        if (easyMediaManager.getMediaPlay() == this)
            easyMediaManager.getHandlePlayEvent().onVideoSizeChanged(width, height);
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        if (easyMediaManager.getMediaPlay() == this)
            easyMediaManager.getHandlePlayEvent().onSeekComplete();
    }
}
