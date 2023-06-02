package com.ashlikun.media.video.controller

import android.widget.ImageView
import com.ashlikun.media.video.VideoData
import com.ashlikun.media.video.status.VideoStatus

/**
 * 作者　　: 李坤
 * 创建时间: 2017/12/18　11:11
 * 邮箱　　：496546144@qq.com
 *
 *
 * 功能介绍：控制器viewHolder的抽离
 */
interface IControllerViewHolder {
    /**
     * 是否可以全屏
     */
    fun setControllFullEnable(fullEnable: Boolean)

    /**
     * 根据状态改变ui
     */
    fun changUi(currentState: VideoStatus)
    fun setDataSource(mediaData: VideoData?)

    /**
     * 开始进度定时器
     */
    fun startProgressSchedule()

    /**
     * 取消进度定时器
     */
    fun stopProgressSchedule()

    /**
     * 控制器是否显示
     */
    fun containerIsShow(): Boolean

    /**
     * 显示或者隐藏顶部和底部控制器
     */
    fun showControllerViewAnim(currentState: VideoStatus?, isShow: Boolean)

    /**
     * 清空全部ui展示
     */
    fun changeUiToClean()

    /**
     * 设置时间
     *
     * @param position 0：重置
     * @param duration 0：重置
     */
    fun setTime(position: Long, duration: Long)

    /**
     * 设置进度  如果2个值都是100，就会设置最大值，如果某个值<0 就不设置
     *
     * @param progress          主进度
     * @param secondaryProgress 缓存进度
     */
    fun setProgress(progress: Int, secondaryProgress: Int)

    /**
     * 获取占位图
     *
     * @return
     */
    val thumbImageView: ImageView?

    /**
     * 获取进度缓存
     *
     * @return
     */
    val bufferProgress: Int
    fun setFull(full: Boolean)

    /**
     * 是否只在全屏的时候显示标题和顶部
     */
    fun setOnlyFullShowTitle(onlyFullShowTitle: Boolean)
}