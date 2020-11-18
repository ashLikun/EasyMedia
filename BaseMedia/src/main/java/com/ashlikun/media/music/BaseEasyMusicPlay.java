package com.ashlikun.media.music;

import android.content.Context;
import android.media.MediaPlayer;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ashlikun.media.video.EasyMediaManager;
import com.ashlikun.media.video.EasyVideoAction;
import com.ashlikun.media.video.NetworkUtils;
import com.ashlikun.media.video.VideoData;
import com.ashlikun.media.video.VideoUtils;
import com.ashlikun.media.video.status.VideoStatus;
import com.ashlikun.media.video.view.IEasyVideoPlayListener;

import java.util.ArrayList;
import java.util.List;

import static com.ashlikun.media.video.status.VideoStatus.AUTO_COMPLETE;
import static com.ashlikun.media.video.status.VideoStatus.NORMAL;
import static com.ashlikun.media.video.status.VideoStatus.PAUSE;
import static com.ashlikun.media.video.status.VideoStatus.PLAYING;


/**
 * @author　　: 李坤
 * 创建时间: 2020/11/18 19:46
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：音频播放器基础类
 * * {@link #setDataSource} 去设置播放的数据源
 */

public class BaseEasyMusicPlay extends FrameLayout implements IEasyVideoPlayListener {
    /**
     * 当前状态
     */
    protected int currentState = VideoStatus.NORMAL;
    /**
     * 备份缓存前的播放状态
     */
    protected int mBackUpPlayingBufferState = -1;
    /**
     * 是否播放过
     */
    protected boolean mHadPlay = false;
    /**
     * 数据源，列表
     */
    protected List<VideoData> mediaData;
    /**
     * 当前播放到的列表数据源位置
     */
    protected int currentUrlIndex = 0;

    /**
     * 播放事件的回掉
     */
    private ArrayList<EasyVideoAction> videoActions;
    /**
     * 当onResume的时候是否去播放
     */
    private boolean ONRESUME_TO_PLAY = true;
    /**
     * 从哪个开始播放
     */
    protected long mSeekOnStart = -1;

    public BaseEasyMusicPlay(@NonNull Context context) {
        this(context, null);
    }

    public BaseEasyMusicPlay(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseEasyMusicPlay(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        EasyMediaManager.getInstanceMusic().setContext(context);
    }

    /**
     * 设置数据源
     *
     * @param url   视频ur
     * @param title 标题
     */
    public void setDataSource(String url, String title) {
        List<VideoData> mediaData = new ArrayList<>();
        mediaData.add(new VideoData.Builder()
                .url(url)
                .title(title)
                .builder());
        setDataSource(mediaData, 0);
    }

    /**
     * 设置数据源
     *
     * @param data 视频ur
     */
    public void setDataSource(VideoData data) {
        List<VideoData> mediaData = new ArrayList<>();
        mediaData.add(data);
        setDataSource(mediaData, 0);
    }

    /**
     * 设置数据源
     */
    public void setDataSource(List<VideoData> mediaData) {
        setDataSource(mediaData, 0);
    }

    /**
     * 设置数据源
     *
     * @param mediaData    视频数据，数组
     * @param defaultIndex 播放的url 位置 0 开始
     */
    public boolean setDataSource(List<VideoData> mediaData, int defaultIndex) {
        this.mediaData = mediaData;
        //如果这个已经在播放就不管
        if (getMediaData() != null && defaultIndex >= 0 && getMediaData().size() > defaultIndex &&
                VideoUtils.isContainsUri(getMediaData(),
                        EasyMediaManager.getMusicMediaPlay().getCurrentDataSource())) {
            saveMusicPlayView();
            if (currentState == VideoStatus.NORMAL) {
                setStatus(VideoStatus.NORMAL);
            }
            return false;
        }
        if (isCurrentMusicPlay() && VideoUtils.isContainsUri(mediaData, EasyMediaManager.getCurrentDataSource())) {
            //当前View正在播放视频  保存进度
            long position = 0;
            try {
                position = EasyMediaManager.getMusicMediaPlay().getCurrentPosition();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            if (position != 0) {
//                VideoUtils.saveProgress(getContext(), EasyMediaManager.getCurrentDataSource(), position);
            }
            EasyMediaManager.getInstanceMusic().releaseMediaPlayer();
        }
        this.currentUrlIndex = defaultIndex;
        setStatus(VideoStatus.NORMAL);
        return true;
    }

    /**
     * 保存播放器 用于全局管理
     * 可能会多次调用
     */
    public void saveMusicPlayView() {
        EasyMusicPlayerManager.setMusicDefault(this);
    }

    /**
     * 开始播放
     * 必须在设置完数据源后
     */
    public void startMusic() {
        //释放播放器
        EasyMediaManager.getInstanceMusic().releaseMediaPlayer();
        EasyMediaManager.getMusicMediaPlay().setCurrentDataSource(getCurrentData());
        //获取音频焦点
        MusicUtils.setAudioFocus(getContext(), true);
        saveMusicPlayView();
        EasyMediaManager.getMusicMediaPlay().prepare();
        setStatus(VideoStatus.PREPARING);
    }

    /**
     * 切换数据源
     *
     * @param position
     * @return
     */
    public boolean switchData(int position) {
        int old = currentUrlIndex;
        this.currentUrlIndex = position;
        if (getCurrentData() != null) {
            startMusic();
            return true;
        } else {
            currentUrlIndex = old;
            return false;
        }

    }

    /**
     * 设置当前播放器状态
     */

    public boolean setStatus(int state) {
        if (currentState == state) {
            return false;
        }
        switch (state) {
            case VideoStatus.NORMAL:
                onStateNormal();
                onEvent(EasyVideoAction.ON_STATUS_NORMAL);
                break;
            case VideoStatus.PREPARING:
                onStatePreparing();
                onEvent(EasyVideoAction.ON_STATUS_PREPARING);
                break;
            case VideoStatus.PLAYING:
                onStatePlaying();
                onEvent(EasyVideoAction.ON_STATUS_PLAYING);
                break;
            case VideoStatus.PAUSE:
                onStatePause();
                onEvent(EasyVideoAction.ON_STATUS_PAUSE);
                break;
            case VideoStatus.ERROR:
                onStateError();
                onEvent(EasyVideoAction.ON_STATUS_ERROR);
                break;
            case VideoStatus.AUTO_COMPLETE:
                onStateAutoComplete();
                onEvent(EasyVideoAction.ON_STATUS_AUTO_COMPLETE);
                onEvent(EasyVideoAction.ON_STATUS_COMPLETE);
                break;
            case VideoStatus.FORCE_COMPLETE:
                onStateNormal();
                onEvent(EasyVideoAction.ON_STATUS_FORCE_COMPLETE);
                onEvent(EasyVideoAction.ON_STATUS_COMPLETE);
                break;
            case VideoStatus.BUFFERING_START:
                onBufferStart();
                onEvent(EasyVideoAction.ON_STATUS_BUFFERING_START);
                break;
        }
        return true;
    }

    /********************************************************************************************
     *                                       设置播放器状态后的回调
     ********************************************************************************************/
    /**
     * 设置当前初始状态
     */
    protected void onStateNormal() {
        currentState = VideoStatus.NORMAL;
    }

    /**
     * 当准备时候
     */
    protected void onStatePreparing() {
        currentState = VideoStatus.PREPARING;
    }


    protected void onStatePrepared() {
        //因为这个紧接着就会进入播放状态，所以不设置state
    }

    /**
     * 开始播放回掉
     */
    protected void onStatePlaying() {
        EasyMediaManager.getMusicMediaPlay().setPreparedPause(false);
        currentState = VideoStatus.PLAYING;
        EasyMediaManager.getMusicMediaPlay().start();
    }

    /**
     * 暂停
     */
    protected void onStatePause() {
        currentState = VideoStatus.PAUSE;
        EasyMediaManager.getMusicMediaPlay().pause();
    }

    /**
     * 开始缓冲
     */
    protected void onBufferStart() {
        currentState = VideoStatus.BUFFERING_START;
    }

    /**
     * 错误
     */
    protected void onStateError() {
        EasyMediaManager.getMusicMediaPlay().setPreparedPause(false);
        currentState = VideoStatus.ERROR;
    }

    /**
     * 自动完成
     */
    protected void onStateAutoComplete() {
        currentState = VideoStatus.AUTO_COMPLETE;
    }


    /**
     * 释放播放器,全屏下不能释放,先退出全屏再释放
     */
    public void release() {
        if (getCurrentData().equals(EasyMediaManager.getMusicMediaPlay().getCurrentDataSource())) {
            //把之前的设置到完成状态
            EasyMusicPlayerManager.completeAll();
            //释放播放器
            EasyMediaManager.getInstanceMusic().releaseMediaPlayer();
        }
    }

    /********************************************************************************************
     *                                           播放器的生命周期，可以重写
     ********************************************************************************************/
    /**
     * 准备播放
     */
    @Override
    public void onPrepared() {
        onStatePrepared();
        setStatus(VideoStatus.PLAYING);
        if (mSeekOnStart > 0) {
            EasyMediaManager.getMusicMediaPlay().seekTo(mSeekOnStart);
            mSeekOnStart = 0;
        }
        mHadPlay = true;
    }

    /**
     * 播放信息
     *
     * @param what  错误码
     * @param extra 扩展码
     */
    @Override
    public void onInfo(int what, int extra) {
        if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
            mBackUpPlayingBufferState = currentState;
            //避免在onPrepared之前就进入了buffering，导致一只loading
            if (mHadPlay && currentState != VideoStatus.PREPARING && currentState > 0) {
                setStatus(VideoStatus.BUFFERING_START);
            }

        } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
            if (mBackUpPlayingBufferState != -1) {
                if (mBackUpPlayingBufferState == VideoStatus.BUFFERING_START) {
                    mBackUpPlayingBufferState = PLAYING;
                }
                if (mHadPlay && currentState != VideoStatus.PREPARING && currentState > 0) {
                    setStatus(mBackUpPlayingBufferState);
                }
                mBackUpPlayingBufferState = -1;
            }
        }
    }

    /**
     * 设置进度完成
     */
    @Override
    public void onSeekComplete() {

    }

    /**
     * 播放错误
     *
     * @param what  错误码
     * @param extra 扩展码
     */
    @Override
    public void onError(int what, int extra) {
        String netSate;
        //切换网络引起的
        if (what == -10000 && NetworkUtils.isConnected(getContext()) &&
                !TextUtils.equals(EasyMediaManager.getInstanceMusic().mNetSate, netSate = NetworkUtils.getNetWorkTypeName(getContext()))) {
            EasyMediaManager.getInstanceMusic().mNetSate = netSate;
            long position = getCurrentPositionWhenPlaying();
            EasyMediaManager.getInstanceMusic().releaseMediaPlayer();
            setSeekOnStart(position);
            //重新播放
            startMusic();
            return;
        }
        if (what != 38 && what != -38 && extra != -38) {
            setStatus(VideoStatus.ERROR);
            if (isCurrentPlay()) {
                EasyMediaManager.getInstanceMusic().releaseMediaPlayer();
            }
        }
    }

    /**
     * 自动播放完成，播放器回调的
     */
    @Override
    public boolean onAutoCompletion() {
        onEvent(EasyVideoAction.ON_AUTO_COMPLETE);
        EasyMediaManager.getInstanceMusic().releaseMediaPlayer();
        Runtime.getRuntime().gc();
        //播放下一个
        return switchData(getCurrentUrlIndex() + 1);
    }

    /**
     * 播放器生命周期,自己主动调用的,还原状态
     */
    @Override
    public void onForceCompletionTo() {
        //还原默认状态
        setStatus(VideoStatus.FORCE_COMPLETE);
        EasyMediaManager.getInstanceMusic().currentVideoWidth = 0;
        EasyMediaManager.getInstanceMusic().currentVideoHeight = 0;
        //取消音频焦点
        MusicUtils.setAudioFocus(getContext(), false);
    }

    /**
     * 播放器大小改变,音频时候无用
     */
    @Override
    public void onVideoSizeChanged() {
    }

    /**
     * 缓存进度更新
     *
     * @param bufferProgress
     */
    @Override
    public void setBufferProgress(int bufferProgress) {

    }

    /**
     * 播放事件的回掉
     *
     * @param type {@link EasyVideoAction}
     */
    @Override
    public void onEvent(int type) {
        if (isCurrentPlay()) {
            //本实例的回调
            if (videoActions != null) {
                for (EasyVideoAction action : videoActions) {
                    action.onEvent(type);
                }
            }
            //全局的地方回调
            if (EasyMediaManager.getInstanceMusic().MEDIA_EVENT != null) {
                EasyMediaManager.getInstanceMusic().MEDIA_EVENT.onEvent(type);
            }
        }

    }

    /********************************************************************************************
     *                                           下面这些都是获取属性和设置属性
     ********************************************************************************************/

    /**
     * 当前EasyVideoPlay  是否正在播放
     */
    public boolean isCurrentPlay() {
        //不仅正在播放的url不能一样，并且各个清晰度也不能一样
        return isCurrentMusicPlay()
                && VideoUtils.isContainsUri(mediaData, EasyMediaManager.getMusicMediaPlay().getCurrentDataSource());
    }

    /**
     * 是否是当前在播放音频
     */
    public boolean isCurrentMusicPlay() {
        return EasyMusicPlayerManager.getMusicDefault() != null
                && EasyMusicPlayerManager.getMusicDefault() == this;
    }

    /**
     * 当前播放到第几个视频，用于多视频播放，没有就返回0
     *
     * @return
     */
    public int getCurrentUrlIndex() {
        return currentUrlIndex;
    }


    /**
     * 获取播放器数据
     *
     * @return
     */
    public List<VideoData> getMediaData() {
        return mediaData;
    }

    /**
     * 获取当前播放uil
     *
     * @return
     */
    public VideoData getCurrentData() {
        return VideoUtils.getCurrentMediaData(mediaData, currentUrlIndex);
    }

    /**
     * 获取当前播放器状态
     *
     * @return {@link VideoStatus.Code}
     */
    @VideoStatus.Code
    public int getCurrentState() {
        return currentState;
    }

    /**
     * 直接设置播放状态
     *
     * @param currentState
     */
    public void setCurrentState(@VideoStatus.Code int currentState) {
        this.currentState = currentState;
    }

    /**
     * 从哪里开始播放
     * 目前有时候前几秒有跳动问题，毫秒
     * 需要在startPlayLogic之前，即播放开始之前
     */
    public void setSeekOnStart(long seekOnStart) {
        this.mSeekOnStart = seekOnStart;
    }

    /**
     * 移除播放事件的回掉
     *
     * @param action
     */
    public boolean removeVideoAction(EasyVideoAction action) {
        if (videoActions != null && action != null) {
            return videoActions.remove(action);
        }
        return false;
    }

    /**
     * 添加播放事件的回掉
     *
     * @param action
     */
    public void addVideoAction(EasyVideoAction action) {
        if (action != null) {
            if (videoActions == null) {
                videoActions = new ArrayList<>();
            }
            if (!videoActions.contains(action)) {
                videoActions.add(action);
            }
        }

    }

    /**
     * 获取当前播放位置
     *
     * @return
     */
    public long getCurrentPositionWhenPlaying() {
        long position = 0;
        if (currentState == PLAYING ||
                currentState == PAUSE) {
            try {
                position = EasyMediaManager.getMusicMediaPlay().getCurrentPosition();
            } catch (IllegalStateException e) {
                e.printStackTrace();
                return position;
            }
        }
        return position;
    }

    public void copyStatus(BaseEasyMusicPlay oldVideo) {
        //复制一些标志位
        mBackUpPlayingBufferState = oldVideo.mBackUpPlayingBufferState;
        mHadPlay = oldVideo.mHadPlay;
        setStatus(oldVideo.getCurrentState());
        currentUrlIndex = oldVideo.getCurrentUrlIndex();
    }

    /**
     * 转到另外一个View播放
     */
    public void copyPlay(BaseEasyMusicPlay oldVideo) {
        if (getMediaData() == null && oldVideo.getMediaData() != null) {
            setDataSource(oldVideo.getMediaData(), oldVideo.getCurrentUrlIndex());
        }
        copyStatus(oldVideo);
        //还原默认的view
        oldVideo.setStatus(VideoStatus.NORMAL);
    }

    /**
     * 对应activity得生命周期
     */
    @Override
    public void onPause() {
        if (getCurrentState() == AUTO_COMPLETE ||
                getCurrentState() == NORMAL) {
            MusicUtils.releaseAllVideos();
        } else {
            if (getCurrentState() == VideoStatus.PLAYING) {
                ONRESUME_TO_PLAY = true;
            } else {
                ONRESUME_TO_PLAY = false;
            }
            if (getCurrentState() == VideoStatus.PLAYING) {
                setStatus(VideoStatus.PAUSE);
            } else if (currentState == VideoStatus.PREPARING) {
                EasyMediaManager.getMusicMediaPlay().setPreparedPause(true);
            }
        }
    }


    /**
     * 对应activity得生命周期
     */
    public void onResume() {
        if (getCurrentState() == PAUSE && ONRESUME_TO_PLAY) {
            setStatus(VideoStatus.PLAYING);
        }
    }

    public void play() {
        if (getCurrentState() != PLAYING) {
            if (getCurrentState() == PAUSE) {
                setStatus(VideoStatus.PLAYING);
            } else {
                startMusic();
            }
        }
    }

    /**
     * 对应activity得生命周期
     */
    public void onDestroy() {
        MusicUtils.onDestroy();
    }
}
