package com.ashlikun.media;

import android.view.View;
import android.view.ViewGroup;

import com.ashlikun.media.status.MediaStatus;
import com.ashlikun.media.view.BaseEasyVideoPlay;
import com.ashlikun.media.view.EasyVideoPlayTiny;
import com.ashlikun.media.view.EasyVideoPlayer;

import static com.ashlikun.media.status.MediaStatus.PAUSE;

/**
 * 作者　　: 李坤
 * 创建时间: 2018/8/20　14:38
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：播放器列表的一些工具方法
 */
public class MediaListUtils {
    /**
     * 当子view附属到窗口时候
     * 这2个方法是给列表使用的
     *
     * @param vidoPlayId 播放器控件的id
     */
    public static void onChildViewAttachedToWindow(View view, int vidoPlayId) {
        if (EasyVideoPlayerManager.getVideoTiny() != null) {
            EasyVideoPlayer videoPlayer = view.findViewById(vidoPlayId);
            if (videoPlayer != null && MediaUtils.getCurrentMediaData(videoPlayer.getMediaData(), videoPlayer.getCurrentUrlIndex()).equals(EasyMediaManager.getCurrentDataSource())) {
                MediaScreenUtils.backPress();
            }
        }
    }

    /**
     * 当子view从窗口分离
     *
     * @param view
     */
    public static void onChildViewDetachedFromWindow(View view) {
        if (EasyVideoPlayerManager.getVideoTiny() == null) {
            BaseEasyVideoPlay videoPlayer = EasyVideoPlayerManager.getCurrentVideoPlayerNoTiny();
            if (videoPlayer instanceof View && ((ViewGroup) view).indexOfChild(videoPlayer) != -1) {
                if (videoPlayer.getCurrentState() == PAUSE) {
                    MediaUtils.releaseAllVideos();
                } else {
                    if (MediaScreenUtils.startWindowTiny(new EasyVideoPlayTiny(videoPlayer.getContext()), videoPlayer.getMediaData(), videoPlayer.getCurrentUrlIndex())) {
                        //还原默认状态
                        videoPlayer.setStatus(MediaStatus.NORMAL);
                    }
                }
            }
        }
    }

    /**
     * ListView列表滑动时候自动小窗口
     * RecyclerView请用recyclerView.addOnChildAttachStateChangeListener
     *
     * @param firstVisibleItem 第一个有效的Item
     * @param visibleItemCount 一共有效的Item
     */
    public static void onScrollAutoTiny(int currentPlayPosition, int firstVisibleItem, int visibleItemCount) {
        int lastVisibleItem = firstVisibleItem + visibleItemCount;
        if (currentPlayPosition >= 0) {
            if ((currentPlayPosition < firstVisibleItem || currentPlayPosition > (lastVisibleItem - 1))) {
                if (EasyVideoPlayerManager.getVideoTiny() == null) {
                    if (EasyVideoPlayerManager.getCurrentVideoPlayerNoTiny().getCurrentState() == PAUSE) {
                        MediaUtils.releaseAllVideos();
                    } else {
                        BaseEasyVideoPlay videoPlayer = EasyVideoPlayerManager.getCurrentVideoPlayerNoTiny();
                        MediaScreenUtils.startWindowTiny(new EasyVideoPlayTiny(videoPlayer.getContext()), videoPlayer.getMediaData(), videoPlayer.getCurrentUrlIndex());
                        videoPlayer.setStatus(MediaStatus.NORMAL);
                    }
                }
            } else {
                if (EasyVideoPlayerManager.getVideoTiny() != null) {
                    MediaScreenUtils.backPress();
                }
            }
        }
    }

    /**
     * 列表滑动时候清空全部播放  ListView用的
     * RecyclerView请用recyclerView.addOnChildAttachStateChangeListener
     *
     * @param currentPlayPosition 当前正在播放的item
     * @param firstVisibleItem    第一个有效的Item
     * @param visibleItemCount    一共有效的Item
     */
    public static void onScrollReleaseAllVideos(int currentPlayPosition, int firstVisibleItem, int visibleItemCount) {
        int lastVisibleItem = firstVisibleItem + visibleItemCount;
        if (currentPlayPosition >= 0) {
            if ((currentPlayPosition < firstVisibleItem || currentPlayPosition > (lastVisibleItem - 1))) {
                MediaUtils.releaseAllVideos();
            }
        }
    }

    /**
     * RecyclerView列表滑动时候自动小窗口
     *
     * @param isChileViewDetached 子view是否分离 true:分离，false:添加
     */
    public static void onRecyclerAutoTiny(EasyVideoPlayer videoPlayer, boolean isChileViewDetached) {

        if (videoPlayer == null || !MediaUtils.isContainsUri(videoPlayer.getMediaData(),
                EasyMediaManager.getCurrentDataSource())) {
            //如果当前的view播放的视频地址不是正在播放的视频地址就过滤掉这次
            return;
        }
        //这里一定是在播放的
        if (isChileViewDetached) {
            if (EasyVideoPlayerManager.getVideoTiny() == null) {
                if (EasyVideoPlayerManager.getCurrentVideoPlayerNoTiny() != null &&
                        EasyVideoPlayerManager.getCurrentVideoPlayerNoTiny().getCurrentState() == PAUSE) {
                    MediaUtils.releaseAllVideos();
                } else {
                    if (MediaScreenUtils.startWindowTiny(new EasyVideoPlayTiny(videoPlayer.getContext()),
                            videoPlayer.getMediaData(), videoPlayer.getCurrentUrlIndex())) {
                        videoPlayer.setStatus(MediaStatus.NORMAL);
                        //列表的时候如果进入小窗口，那么久把列表的view设为null,可能回收后就不对了
                        EasyVideoPlayerManager.setVideoDefault(null);
                    }
                }
            }
        } else if (EasyVideoPlayerManager.getVideoTiny() != null) {
            //回来的时候再把这个保存,对应于上面的设为null
            EasyVideoPlayerManager.setVideoDefault(videoPlayer);
            MediaScreenUtils.backPress();
        }
    }

    /**
     * RecyclerView滑动时候清空全部播放
     * RecyclerView请用recyclerView.addOnChildAttachStateChangeListener
     *
     * @param videoPlayer 当前子view播放器
     */
    public static void onRecyclerRelease(EasyVideoPlayer videoPlayer) {
        //如果当前的view播放的视频地址不是正在播放的视频地址就过滤掉这次
        if (videoPlayer == null || !MediaUtils.isContainsUri(videoPlayer.getMediaData(),
                EasyMediaManager.getCurrentDataSource())) {
            return;
        }
        MediaUtils.releaseAllVideos();
    }
}
