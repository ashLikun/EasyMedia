package com.ashlikun.media.video.view.other

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import com.ashlikun.media.R
import com.ashlikun.media.video.VideoUtils.dip2px

/**
 * Created by codeest on 2016/11/9.
 */
class EasyLoaddingView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    View(context, attrs, defStyleAttr) {
    private var onDownloadStateListener: OnDownloadStateListener? = null
    var currentState = 0
        private set
    private var mCurrentRippleX = 0f
    private var mCurrentSize = 0.0
    private var mTotalSize = 0.0
    private var mTextSize = 0
    private var mDownloadTime = 0
    private var mUnit: DownloadUnit? = null
    private var mPaint: Paint? = null
    private var mBgPaint: Paint? = null
    private var mTextPaint: Paint? = null
    private var mPath: Path? = null
    private var mRectF: RectF? = null
    private var mClipRectF: RectF? = null
    var mValueAnimator: ValueAnimator? = null
    private var mFraction = 0f
    private var mWidth = 0f
    private var mHeight = 0f
    private var mCenterX = 0f
    private var mCenterY = 0f
    private var mBaseLength = 0f
    private var mCircleRadius = 0f
    private var mBaseRippleLength = 0f

    enum class DownloadUnit {
        GB, MB, KB, B, NONE
    }

    init {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.EasyLoaddingView)
        val lineColor = ta.getColor(R.styleable.EasyLoaddingView_loadding_line_color, DEFAULT_LINE_COLOR)
        val bgLineColor = ta.getColor(R.styleable.EasyLoaddingView_loadding_bg_line_color, DEFAULT_BG_LINE_COLOR)
        val textColor = ta.getColor(R.styleable.EasyLoaddingView_loadding_text_color, DEFAULT_TEXT_COLOR)
        val lineWidth = ta.getDimensionPixelSize(R.styleable.EasyLoaddingView_loadding_line_width, dip2px(getContext(), DEFAULT_LINE_WIDTH.toFloat()))
        val bgLineWidth =
            ta.getDimensionPixelSize(R.styleable.EasyLoaddingView_loadding_bg_line_width, dip2px(getContext(), DEFAULT_BG_LINE_WIDTH.toFloat()))
        val textSize = ta.getDimensionPixelSize(R.styleable.EasyLoaddingView_loadding_text_size, dip2px(getContext(), DEFAULT_TEXT_SIZE.toFloat()))
        ta.recycle()
        mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaint!!.style = Paint.Style.STROKE
        mPaint!!.strokeCap = Paint.Cap.ROUND
        mPaint!!.strokeWidth = lineWidth.toFloat()
        mPaint!!.color = lineColor
        mBgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mBgPaint!!.style = Paint.Style.STROKE
        mBgPaint!!.strokeCap = Paint.Cap.ROUND
        mBgPaint!!.strokeWidth = bgLineWidth.toFloat()
        mBgPaint!!.color = bgLineColor
        mTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mTextPaint!!.color = textColor
        mTextPaint!!.textSize = textSize.toFloat()
        mTextPaint!!.textAlign = Paint.Align.CENTER
        mPath = Path()
        mTextSize = textSize
        currentState = DEFAULT_STATE
        mUnit = DEFAULT_DOWNLOAD_UNIT
        mDownloadTime = DEFAULT_DOWNLOAD_TIME
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = w.toFloat()
        mHeight = h.toFloat()
        mCenterX = mWidth / 2
        mCenterY = mHeight / 2
        mCircleRadius = mWidth * 5 / 12
        mBaseLength = mCircleRadius / 3
        mBaseRippleLength = 4.4f * mBaseLength / 12
        mCurrentRippleX = mCenterX - mBaseRippleLength * 10
        mRectF = RectF(mCenterX - mCircleRadius, mCenterY - mCircleRadius, mCenterX + mCircleRadius, mCenterY + mCircleRadius)
        mClipRectF = RectF(mCenterX - 6 * mBaseRippleLength, 0f, mCenterX + 6 * mBaseRippleLength, mHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        when (currentState) {
            STATE_PRE -> if (mFraction <= 0.4) {
                canvas.drawCircle(mCenterX, mCenterY, mCircleRadius, mBgPaint!!)
                canvas.drawLine(mCenterX - mBaseLength, mCenterY, mCenterX, mCenterY + mBaseLength, mPaint!!)
                canvas.drawLine(mCenterX, mCenterY + mBaseLength, mCenterX + mBaseLength, mCenterY, mPaint!!)
                canvas.drawLine(
                    mCenterX, mCenterY + mBaseLength - 1.3f * mBaseLength / 0.4f * mFraction,
                    mCenterX, mCenterY - 1.6f * mBaseLength + 1.3f * mBaseLength / 0.4f * mFraction, mPaint!!
                )
            } else if (mFraction <= 0.6) {
                canvas.drawCircle(mCenterX, mCenterY, mCircleRadius, mBgPaint!!)
                canvas.drawCircle(mCenterX, mCenterY - 0.3f * mBaseLength, 2f, mPaint!!)
                canvas.drawLine(
                    mCenterX - mBaseLength - mBaseLength * 1.2f / 0.2f * (mFraction - 0.4f),
                    mCenterY,
                    mCenterX,
                    mCenterY + mBaseLength - mBaseLength / 0.2f * (mFraction - 0.4f),
                    mPaint!!
                )
                canvas.drawLine(
                    mCenterX,
                    mCenterY + mBaseLength - mBaseLength / 0.2f * (mFraction - 0.4f),
                    mCenterX + mBaseLength + mBaseLength * 1.2f / 0.2f * (mFraction - 0.4f),
                    mCenterY,
                    mPaint!!
                )
            } else if (mFraction <= 1) {
                canvas.drawCircle(mCenterX, mCenterY, mCircleRadius, mBgPaint!!)
                canvas.drawCircle(
                    mCenterX,
                    mCenterY - 0.3f * mBaseLength - (mCircleRadius - 0.3f * mBaseLength) / 0.4f * (mFraction - 0.6f),
                    2f,
                    mPaint!!
                )
                canvas.drawLine(mCenterX - mBaseLength * 2.2f, mCenterY, mCenterX + mBaseLength * 2.2f, mCenterY, mPaint!!)
            } else {
                canvas.drawCircle(mCenterX, mCenterY, mCircleRadius, mBgPaint!!)
                canvas.drawCircle(mCenterX, mCenterY - mCircleRadius - mBaseLength * 3 * (mFraction - 1), 3f, mPaint!!)
                canvas.drawLine(mCenterX - mBaseLength * 2.2f, mCenterY, mCenterX + mBaseLength * 2.2f, mCenterY, mPaint!!)
            }

            STATE_DOWNLOADING -> {
                if (mFraction <= 0.2) {
                    mTextPaint!!.textSize = mTextSize / 0.2f * mFraction
                }
                canvas.drawCircle(mCenterX, mCenterY, mCircleRadius, mBgPaint!!)
                canvas.drawArc(mRectF!!, -90f, 359.99f * mFraction, false, mPaint!!)
                mPath!!.reset()
                mCurrentRippleX += DEFAULT_RIPPLE_SPEED.toFloat()
                if (mCurrentRippleX > mCenterX - mBaseRippleLength * 6) mCurrentRippleX = mCenterX - mBaseRippleLength * 10
                mPath!!.moveTo(mCurrentRippleX, mCenterY)
                var i = 0
                while (i < 4) {
                    mPath!!.rQuadTo(mBaseRippleLength, -(1 - mFraction) * mBaseRippleLength, mBaseRippleLength * 2, 0f)
                    mPath!!.rQuadTo(mBaseRippleLength, (1 - mFraction) * mBaseRippleLength, mBaseRippleLength * 2, 0f)
                    i++
                }
                canvas.save()
                canvas.clipRect(mClipRectF!!)
                canvas.drawPath(mPath!!, mPaint!!)
                canvas.restore()
                if (mUnit != DownloadUnit.NONE && mCurrentSize > 0) {
                    //canvas.drawText(String.format("%.2f", mCurrentSize) + getUnitStr(mUnit), mCenterX , mCenterY + 1.4f * mBaseLength , mTextPaint);
                }
            }

            STATE_END -> {
                canvas.drawCircle(mCenterX, mCenterY, mCircleRadius, mPaint!!)
                if (mFraction <= 0.5) {
                    mTextPaint!!.textSize = mTextSize - mTextSize / 0.2f * mFraction
                } else {
                    mTextPaint!!.textSize = 0f
                }
                if (mUnit != DownloadUnit.NONE && mCurrentSize > 0) {
                    canvas.drawText(String.format("%.2f", mCurrentSize) + getUnitStr(mUnit), mCenterX, mCenterY + 1.4f * mBaseLength, mTextPaint!!)
                }
                canvas.drawLine(
                    mCenterX - mBaseLength * 2.2f + mBaseLength * 1.2f * mFraction, mCenterY,
                    mCenterX - mBaseLength * 0.5f, mCenterY + mBaseLength * 0.5f * mFraction * 1.3f, mPaint!!
                )
                canvas.drawLine(
                    mCenterX - mBaseLength * 0.5f, mCenterY + mBaseLength * 0.5f * mFraction * 1.3f,
                    mCenterX + mBaseLength * 2.2f - mBaseLength * mFraction, mCenterY - mBaseLength * mFraction * 1.3f, mPaint!!
                )
            }

            STATE_RESET -> {
                canvas.drawCircle(mCenterX, mCenterY, mCircleRadius, mBgPaint!!)
                canvas.drawLine(
                    mCenterX - mBaseLength,
                    mCenterY,
                    mCenterX - mBaseLength * 0.5f + mBaseLength * 0.5f * mFraction,
                    mCenterY + mBaseLength * 0.65f + mBaseLength * 0.35f * mFraction,
                    mPaint!!
                )
                canvas.drawLine(
                    mCenterX - mBaseLength * 0.5f + mBaseLength * 0.5f * mFraction,
                    mCenterY + mBaseLength * 0.65f + mBaseLength * 0.35f * mFraction,
                    mCenterX + mBaseLength * 1.2f - mBaseLength * 0.2f * mFraction,
                    mCenterY - 1.3f * mBaseLength + 1.3f * mBaseLength * mFraction,
                    mPaint!!
                )
                canvas.drawLine(
                    mCenterX - mBaseLength * 0.5f + mBaseLength * 0.5f * mFraction, mCenterY + mBaseLength * 0.65f + mBaseLength * 0.35f * mFraction,
                    mCenterX - mBaseLength * 0.5f + mBaseLength * 0.5f * mFraction,
                    mCenterY + mBaseLength * 0.65f - mBaseLength * 2.25f * mFraction, mPaint!!
                )
            }
        }
    }

    fun release() {
        if (mValueAnimator != null) {
            mValueAnimator!!.removeAllListeners()
            mValueAnimator!!.removeAllUpdateListeners()
            if (mValueAnimator!!.isRunning) {
                mValueAnimator!!.cancel()
            }
            mValueAnimator = null
        }
    }

    fun start() {
        if (mValueAnimator != null) {
            mValueAnimator!!.removeAllListeners()
            mValueAnimator!!.removeAllUpdateListeners()
            if (mValueAnimator!!.isRunning) {
                mValueAnimator!!.cancel()
            }
            mValueAnimator = null
        }
        currentState = STATE_DOWNLOADING
        mValueAnimator = ValueAnimator.ofFloat(1f, 100f)
        mValueAnimator!!.setDuration(1500)
        mValueAnimator!!.setInterpolator(OvershootInterpolator())
        mValueAnimator!!.addUpdateListener(AnimatorUpdateListener { valueAnimator ->
            mFraction = valueAnimator.animatedFraction
            invalidate()
        })
        mValueAnimator!!.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                currentState = STATE_DOWNLOADING
                downloadAnim()
            }
        })
        mValueAnimator!!.start()
    }

    private fun downloadAnim() {
        if (mValueAnimator != null) {
            mValueAnimator!!.removeAllListeners()
            mValueAnimator!!.removeAllUpdateListeners()
            if (mValueAnimator!!.isRunning) {
                mValueAnimator!!.cancel()
            }
            mValueAnimator = null
        }
        if (currentState != STATE_DOWNLOADING) {
            return
        }
        mValueAnimator = ValueAnimator.ofFloat(1f, 100f)
        mValueAnimator!!.setDuration(mDownloadTime.toLong())
        mValueAnimator!!.setInterpolator(LinearInterpolator())
        mValueAnimator!!.addUpdateListener(AnimatorUpdateListener { valueAnimator ->
            mFraction = valueAnimator.animatedFraction
            if (mUnit != DownloadUnit.NONE && mTotalSize > 0) {
                mCurrentSize = mFraction * mTotalSize
            }
            invalidate()
        })
        mValueAnimator!!.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                currentState = STATE_DOWNLOADING
                downloadAnim()
            }
        })
        mValueAnimator!!.start()
    }

    private fun endAnim() {
        if (mValueAnimator != null) {
            mValueAnimator!!.removeAllListeners()
            mValueAnimator!!.removeAllUpdateListeners()
            if (mValueAnimator!!.isRunning) {
                mValueAnimator!!.cancel()
            }
            mValueAnimator = null
        }
        mValueAnimator = ValueAnimator.ofFloat(1f, 100f)
        mValueAnimator!!.setDuration(700)
        mValueAnimator!!.setInterpolator(OvershootInterpolator())
        mValueAnimator!!.addUpdateListener(AnimatorUpdateListener { valueAnimator ->
            mFraction = valueAnimator.animatedFraction
            invalidate()
        })
        mValueAnimator!!.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                mFraction = 0f
                currentState = STATE_RESET
                if (onDownloadStateListener != null) {
                    onDownloadStateListener!!.onDownloadFinish()
                }
            }
        })
        mValueAnimator!!.start()
    }

    fun reset() {
        mFraction = 0f
        currentState = STATE_PRE
        if (mValueAnimator != null) {
            mValueAnimator!!.removeAllListeners()
            mValueAnimator!!.removeAllUpdateListeners()
            if (mValueAnimator!!.isRunning) {
                mValueAnimator!!.cancel()
            }
            mValueAnimator = null
        }
    }

    private fun getUnitStr(unit: DownloadUnit?): String {
        when (unit) {
            DownloadUnit.GB -> return " gb"
            DownloadUnit.MB -> return " mb"
            DownloadUnit.KB -> return " kb"
            DownloadUnit.B -> return " b"
        }
        return " b"
    }

    fun setDownloadConfig(downloadTime: Int, downloadFileSize: Double, unit: DownloadUnit?) {
        mDownloadTime = downloadTime
        mTotalSize = downloadFileSize
        mUnit = unit
    }

    interface OnDownloadStateListener {
        fun onDownloadFinish()
        fun onResetFinish()
    }

    fun setOnDownloadStateListener(onDownloadStateListener: OnDownloadStateListener?) {
        this.onDownloadStateListener = onDownloadStateListener
    }

    companion object {
        const val STATE_PRE = 0
        const val STATE_DOWNLOADING = 1
        const val STATE_END = 2
        const val STATE_RESET = 3
        private const val DEFAULT_LINE_COLOR = Color.WHITE
        private const val DEFAULT_BG_LINE_COLOR = -0xc5c0bb
        private const val DEFAULT_TEXT_COLOR = Color.WHITE
        private const val DEFAULT_LINE_WIDTH = 1
        private const val DEFAULT_BG_LINE_WIDTH = 1
        private const val DEFAULT_TEXT_SIZE = 14
        private const val DEFAULT_STATE = STATE_PRE
        private const val DEFAULT_RIPPLE_SPEED = 2
        private const val DEFAULT_DOWNLOAD_TIME = 2000
        private val DEFAULT_DOWNLOAD_UNIT = DownloadUnit.B
    }
}