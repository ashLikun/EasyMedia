package com.ashlikun.media.view;

/**
 * 作者　　: 李坤
 * 创建时间: 2018/1/30　10:13
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：播放器回掉的生命周期
 */

public interface EasyBaseVideoPlay {
    //播放器生命周期
    void onPrepared();

    void onInfo(int what, int extra);

    void onSeekComplete();

    void onError(int what, int extra);

    void onAutoCompletion();

    void onVideoSizeChanged();

    void setBufferProgress(int bufferProgress);
    //播放器生命周期 END

}
