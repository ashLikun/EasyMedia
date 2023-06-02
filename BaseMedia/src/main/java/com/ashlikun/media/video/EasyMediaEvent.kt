package com.ashlikun.media.video

/**
 * @author　　: 李坤
 * 创建时间: 2018/8/20 11:01
 * 邮箱　　：496546144@qq.com
 *
 *
 * 功能介绍：播放器事件,当播放器整个生命周期中的一些事件，一般用于全局监听
 */

interface EasyMediaEvent {
    fun onEvent(type: Int)

    companion object {
        /**
         * 播放按钮点击
         */
        const val ON_CLICK_START_ICON = 0
        const val ON_CLICK_START_ERROR = 1
        const val ON_CLICK_START_AUTO_COMPLETE = 2
        const val ON_CLICK_PAUSE = 3
        const val ON_CLICK_RESUME = 4
        const val ON_SEEK_POSITION = 5
        const val ON_AUTO_COMPLETE = 6

        /**
         *
         */
        const val ON_ENTER_FULLSCREEN = 7

        /**
         * 退出全屏
         */
        const val ON_QUIT_FULLSCREEN = 8

        /**
         * 退出小窗口
         */
        const val ON_QUIT_TINYSCREEN = 10

        /**
         * 在播放器上手势改变音量
         */
        const val ON_TOUCH_SCREEN_SEEK_VOLUME = 11

        /**
         * 在播放器上手势改变进度
         */
        const val ON_TOUCH_SCREEN_SEEK_POSITION = 12

        /**
         * 播放状态 startVideo 调用的时候,准备过后调用的
         */
        const val ON_STATUS_PREPARING = 13

        /**
         * 默认状态
         */
        const val ON_STATUS_NORMAL = 14

        /**
         * 播放状态
         */
        const val ON_STATUS_PLAYING = 15

        /**
         * 暂停状态
         */
        const val ON_STATUS_PAUSE = 16

        /**
         * 错误状态
         */
        const val ON_STATUS_ERROR = 17

        /**
         * 自动完成状态
         */
        const val ON_STATUS_AUTO_COMPLETE = 18

        /**
         * 其他地方播放视频，当前播放器被强制完成
         */
        const val ON_STATUS_FORCE_COMPLETE = 20

        /**
         * 完成状态(自动和被动)
         */
        const val ON_STATUS_COMPLETE = 21

        /**
         * 开始缓冲
         */
        const val ON_STATUS_BUFFERING_START = 19

        /**
         * 没有无线网络时候点击继续播放
         */
        const val ON_CLICK_START_NO_WIFI_GOON = 101
    }
}