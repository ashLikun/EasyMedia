package com.ashlikun.media.view;

import com.ashlikun.media.EasyMediaAction;

/**
 * 作者　　: 李坤
 * 创建时间: 2017/12/5　17:40
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：控制器的事件
 */

public interface EasyOnControllEvent extends EasyMediaAction {
    /**
     * 开始播放按钮点击
     */
    void onPlayStartClick();

    /**
     * 播放失败，从新播放
     */
    void onRetryClick();

    /**
     * 全屏点击
     */
    void onFullscreenClick();

    /**
     * 当控制器点击的时候
     */
    void onControllerClick();
}
