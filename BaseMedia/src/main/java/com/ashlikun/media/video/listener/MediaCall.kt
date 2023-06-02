package com.ashlikun.media.video.listener

import com.ashlikun.media.video.EasyMediaEvent

/**
 * 作者　　: 李坤
 * 创建时间: 2023/6/2　9:20
 * 邮箱　　：496546144@qq.com
 *
 * 功能介绍：
 */

/**
 * 播放器的发出的事件
 */
typealias MediaEventCall = (type: Int) -> Unit
/**
 * 大小改变
 */
typealias MediaSizeChangeCall = (width: Int, height: Int) -> Unit

/**
 * 缓存进度更新
 */
typealias MediaBufferProgressCall = (bufferProgress: Int) -> Unit

/**
 * 设置进度完成
 */
typealias MediaSeekCompleteCall = () -> Unit

/**
 * 播放信息
 *
 * @param what  错误码
 * @param extra 扩展码
 */
typealias MediaInfoCall = (what: Int, extra: Int) -> Unit

/**
 * 播放错误
 *
 * @param what  错误码
 * @param extra 扩展码
 */
typealias MediaErrorCall = (what: Int, extra: Int) -> Unit