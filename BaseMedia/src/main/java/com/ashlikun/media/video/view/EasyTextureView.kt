package com.ashlikun.media.video.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.TextureView
import android.view.View
import com.ashlikun.media.video.status.VideoDisplayType

/**
 * 播放视频的渲染载体
 * 1：参照Android系统的VideoView的onMeasure方法
 * 2：注意!relativelayout中无法全屏，要嵌套一个linearlayout
 *
 *
 * 因为SurfaceView的内容不在应用窗口上，所以不能使用变换（平移、缩放、旋转等）。
 * 也难以放在ListView或者ScrollView中，不能使用UI控件的一些特性比如View.setAlpha()。
 * 为了解决这个问题 Android 4.0中引入了TextureView。只能在具有硬件加速的设备中，就是gup
 */
class EasyTextureView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : TextureView(context, attrs) {

    companion object {
        protected const val TAG = "EasyTextureView"
    }

    var currentVideoWidth = 0
        private set
    var currentVideoHeight = 0
        private set

    /**
     * 视频大小缩放类型
     */
    var displayType = VideoDisplayType.ADAPTER


    fun setVideoSize(currentVideoWidth: Int, currentVideoHeight: Int) {
        if (this.currentVideoWidth != currentVideoWidth || this.currentVideoHeight != currentVideoHeight) {
            this.currentVideoWidth = currentVideoWidth
            this.currentVideoHeight = currentVideoHeight
            requestLayout()
        }
    }

    /**
     * 视频是否高度大于宽度,可能获取不到
     *
     * @return
     */
    val isPortrait: Boolean
        get() = currentVideoHeight > currentVideoWidth
    val isSizeOk: Boolean
        get() = currentVideoHeight != 0 && currentVideoWidth != 0

    override fun setRotation(rotation: Float) {
        if (rotation != getRotation()) {
            super.setRotation(rotation)
            requestLayout()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var widthMeasureSpec = widthMeasureSpec
        var heightMeasureSpec = heightMeasureSpec
        val viewRotation = rotation.toInt()
        val videoWidth = currentVideoWidth.toFloat()
        var videoHeight = currentVideoHeight.toFloat()
        var parentHeight = (parent as View).measuredHeight.toFloat()
        var parentWidth = (parent as View).measuredWidth.toFloat()
        if (parentWidth != 0f && parentHeight != 0f && videoWidth != 0f && videoHeight != 0f) {
            if (displayType === VideoDisplayType.MATCH_PARENT) {
                if (viewRotation == 90 || viewRotation == 270) {
                    val tempSize = parentWidth
                    parentWidth = parentHeight
                    parentHeight = tempSize
                }
                /**强制充满 */
                videoHeight = videoWidth * parentHeight / parentWidth
            }
        }

        // 如果判断成立，则说明显示的TextureView和本身的位置是有90度的旋转的，所以需要交换宽高参数。
        if (viewRotation == 90 || viewRotation == 270) {
            val tempMeasureSpec = widthMeasureSpec
            widthMeasureSpec = heightMeasureSpec
            heightMeasureSpec = tempMeasureSpec
        }
        var width = getDefaultSize(videoWidth.toInt(), widthMeasureSpec).toFloat()
        var height = getDefaultSize(videoHeight.toInt(), heightMeasureSpec).toFloat()
        if (videoWidth > 0 && videoHeight > 0) {
            val widthSpecMode = MeasureSpec.getMode(widthMeasureSpec)
            val widthSpecSize = MeasureSpec.getSize(widthMeasureSpec)
            val heightSpecMode = MeasureSpec.getMode(heightMeasureSpec)
            val heightSpecSize = MeasureSpec.getSize(heightMeasureSpec)
            Log.i(TAG, "widthMeasureSpec  [" + MeasureSpec.toString(widthMeasureSpec) + "]")
            Log.i(TAG, "heightMeasureSpec [" + MeasureSpec.toString(heightMeasureSpec) + "]")
            if (widthSpecMode == MeasureSpec.EXACTLY && heightSpecMode == MeasureSpec.EXACTLY) {
                // the size is fixed
                width = widthSpecSize.toFloat()
                height = heightSpecSize.toFloat()
                // for compatibility, we adjust size based on aspect ratio
                if (videoWidth * height < width * videoHeight) {
                    width = height * videoWidth / videoHeight
                } else if (videoWidth * height > width * videoHeight) {
                    height = width * videoHeight / videoWidth
                }
            } else if (widthSpecMode == MeasureSpec.EXACTLY) {
                // only the width is fixed, adjust the height to match aspect ratio if possible
                width = widthSpecSize.toFloat()
                height = width * videoHeight / videoWidth
                if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    height = heightSpecSize.toFloat()
                    width = height * videoWidth / videoHeight
                }
            } else if (heightSpecMode == MeasureSpec.EXACTLY) {
                // only the height is fixed, adjust the width to match aspect ratio if possible
                height = heightSpecSize.toFloat()
                width = height * videoWidth / videoHeight
                if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    width = widthSpecSize.toFloat()
                    height = width * videoHeight / videoWidth
                }
            } else {
                // neither the width nor the height are fixed, try to use actual video size
                width = videoWidth
                height = videoHeight
                if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
                    // too tall, decrease both width and height
                    height = heightSpecSize.toFloat()
                    width = height * videoWidth / videoHeight
                }
                if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
                    // too wide, decrease both width and height
                    width = widthSpecSize.toFloat()
                    height = width * videoHeight / videoWidth
                }
            }
        } else {
            // no size yet, just adopt the given spec sizes
        }
        if (parentWidth != 0f && parentHeight != 0f && videoWidth != 0f && videoHeight != 0f) {
            if (displayType === VideoDisplayType.ORIGINAL) {
                /**原图 */
                height = videoHeight
                width = videoWidth
            } else if (displayType === VideoDisplayType.MATCH_CROP) {
                if (viewRotation == 90 || viewRotation == 270) {
                    val tempSize = parentWidth
                    parentWidth = parentHeight
                    parentHeight = tempSize
                }
                /**充满剪切 */
                if (videoHeight / (videoWidth * 1.0f) > parentHeight / parentWidth) {
                    height = parentWidth / width * height
                    width = parentWidth
                } else if (videoHeight / videoWidth < parentHeight / parentWidth) {
                    width = parentHeight / height * width
                    height = parentHeight
                }
            }
        }
        setMeasuredDimension(width.toInt(), height.toInt())
    }

}