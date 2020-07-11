package com.ashlikun.media.video.view;

import com.ashlikun.media.video.EasyVideoAction;

/**
 * @author　　: 李坤
 * 创建时间: 2018/8/20 11:01
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：播放器回掉的生命周期
 */
public interface IEasyVideoPlayListener extends EasyVideoAction {
    /**
     * 准备播放
     */
    void onPrepared();


    /**
     * 播放信息
     *
     * @param what  错误码
     * @param extra 扩展码
     */
    void onInfo(int what, int extra);

    /**
     * 设置进度完成
     */
    void onSeekComplete();

    /**
     * 播放错误
     *
     * @param what  错误码
     * @param extra 扩展码
     */
    void onError(int what, int extra);

    /**
     * 自动播放完成，播放器回调的
     */
    void onAutoCompletion();

    /**
     * 自己主动调用完成
     */
    void onForceCompletionTo();

    /**
     * 播放器大小改变
     */
    void onVideoSizeChanged();

    /**
     * 缓存进度更新
     *
     * @param bufferProgress
     */
    void setBufferProgress(int bufferProgress);


}
