package com.ashlikun.media.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TextureView;
import android.view.View;

import com.ashlikun.media.status.MediaDisplayType;

/**
 * 播放视频的渲染载体
 * 1：参照Android系统的VideoView的onMeasure方法
 * 2：注意!relativelayout中无法全屏，要嵌套一个linearlayout</p>
 * <p>
 * 因为SurfaceView的内容不在应用窗口上，所以不能使用变换（平移、缩放、旋转等）。
 * 也难以放在ListView或者ScrollView中，不能使用UI控件的一些特性比如View.setAlpha()。
 * 为了解决这个问题 Android 4.0中引入了TextureView。只能在具有硬件加速的设备中，就是gup
 */
public class EasyTextureView extends TextureView {
    /**
     * 视频大小缩放规则,全局的
     */
    @MediaDisplayType.Code
    public static int VIDEO_IMAGE_DISPLAY_TYPE = 0;
    protected static final String TAG = "JZResizeTextureView";

    public int currentVideoWidth = 0;
    public int currentVideoHeight = 0;

    public EasyTextureView(Context context) {
        super(context);
        currentVideoWidth = 0;
        currentVideoHeight = 0;
    }

    public EasyTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        currentVideoWidth = 0;
        currentVideoHeight = 0;
    }

    public void setVideoSize(int currentVideoWidth, int currentVideoHeight) {
        if (this.currentVideoWidth != currentVideoWidth || this.currentVideoHeight != currentVideoHeight) {
            this.currentVideoWidth = currentVideoWidth;
            this.currentVideoHeight = currentVideoHeight;
            requestLayout();
        }
    }

    @Override
    public void setRotation(float rotation) {
        if (rotation != getRotation()) {
            super.setRotation(rotation);
            requestLayout();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int viewRotation = (int) getRotation();
        float videoWidth = currentVideoWidth;
        float videoHeight = currentVideoHeight;
        float parentHeight = ((View) getParent()).getMeasuredHeight();
        float parentWidth = ((View) getParent()).getMeasuredWidth();
        if (parentWidth != 0 && parentHeight != 0 && videoWidth != 0 && videoHeight != 0) {
            if (VIDEO_IMAGE_DISPLAY_TYPE == MediaDisplayType.VIDEO_IMAGE_DISPLAY_TYPE_FILL_PARENT) {
                if (viewRotation == 90 || viewRotation == 270) {
                    float tempSize = parentWidth;
                    parentWidth = parentHeight;
                    parentHeight = tempSize;
                }
                /**强制充满**/
                videoHeight = videoWidth * parentHeight / parentWidth;
            }
        }

        // 如果判断成立，则说明显示的TextureView和本身的位置是有90度的旋转的，所以需要交换宽高参数。
        if (viewRotation == 90 || viewRotation == 270) {
            int tempMeasureSpec = widthMeasureSpec;
            widthMeasureSpec = heightMeasureSpec;
            heightMeasureSpec = tempMeasureSpec;
        }

        float width = getDefaultSize((int) videoWidth, widthMeasureSpec);
        float height = getDefaultSize((int) videoHeight, heightMeasureSpec);
        if (videoWidth > 0 && videoHeight > 0) {

            int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
            int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
            int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
            int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

            Log.i(TAG, "widthMeasureSpec  [" + MeasureSpec.toString(widthMeasureSpec) + "]");
            Log.i(TAG, "heightMeasureSpec [" + MeasureSpec.toString(heightMeasureSpec) + "]");

            if (widthSpecMode == MeasureSpec.EXACTLY && heightSpecMode == MeasureSpec.EXACTLY) {
                // the size is fixed
                width = widthSpecSize;
                height = heightSpecSize;
                // for compatibility, we adjust size based on aspect ratio
                if (videoWidth * height < width * videoHeight) {
                    width = height * videoWidth / videoHeight;
                } else if (videoWidth * height > width * videoHeight) {
                    height = width * videoHeight / videoWidth;
                }
            } else if (widthSpecMode == MeasureSpec.EXACTLY) {
                // only the width is fixed, adjust the height to match aspect ratio if possible
                width = widthSpecSize;
                height = width * videoHeight / videoWidth;
                if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    height = heightSpecSize;
                    width = height * videoWidth / videoHeight;
                }
            } else if (heightSpecMode == MeasureSpec.EXACTLY) {
                // only the height is fixed, adjust the width to match aspect ratio if possible
                height = heightSpecSize;
                width = height * videoWidth / videoHeight;
                if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    width = widthSpecSize;
                    height = width * videoHeight / videoWidth;
                }
            } else {
                // neither the width nor the height are fixed, try to use actual video size
                width = videoWidth;
                height = videoHeight;
                if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
                    // too tall, decrease both width and height
                    height = heightSpecSize;
                    width = height * videoWidth / videoHeight;
                }
                if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
                    // too wide, decrease both width and height
                    width = widthSpecSize;
                    height = width * videoHeight / videoWidth;
                }
            }
        } else {
            // no size yet, just adopt the given spec sizes
        }
        if (parentWidth != 0 && parentHeight != 0 && videoWidth != 0 && videoHeight != 0) {
            if (VIDEO_IMAGE_DISPLAY_TYPE == MediaDisplayType.VIDEO_IMAGE_DISPLAY_TYPE_ORIGINAL) {
                /**原图**/
                height = videoHeight;
                width = videoWidth;
            } else if (VIDEO_IMAGE_DISPLAY_TYPE == MediaDisplayType.VIDEO_IMAGE_DISPLAY_TYPE_FILL_SCROP) {
                if (viewRotation == 90 || viewRotation == 270) {
                    float tempSize = parentWidth;
                    parentWidth = parentHeight;
                    parentHeight = tempSize;
                }
                /**充满剪切**/
                if (videoHeight / (videoWidth * 1.0f) > parentHeight / parentWidth) {
                    height = parentWidth / width * height;
                    width = parentWidth;
                } else if (videoHeight / videoWidth < parentHeight / parentWidth) {
                    width = parentHeight / height * width;
                    height = parentHeight;
                }
            }
        }
        setMeasuredDimension((int) width, (int) height);
    }
}
