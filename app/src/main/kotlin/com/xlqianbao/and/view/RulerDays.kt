package com.xlqianbao.and.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.Layout
import android.text.TextPaint
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import com.xlqianbao.and.R
import java.util.*

/**
 * Created by gao on 2017/8/30.
 */
class RulerDays constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    // 字体大小
    private var mTextSize = 36
    // 分隔线(大号)
    private var mLineHeighMax: Int = 0
    private val mLineColorMax: Int
    // 分隔线(中号)
    private var mLineHeighMid: Int = 0
    private val mLineColorMid: Int
    // 分隔线(小号)
    private var mLineHeighMin: Int = 0
    private val mLineColorMin: Int
    // 当前值
    private var mCurrValue: Int = 0
    // 显示最大值
    private var mMaxValue: Int = 0
    // 分隔模式
    private var mModType = MOD_TYPE_SCALE
    // 分隔线之间间隔
    private var mLineDivder: Int = 0
    // 滚动器
    private var scroller: WheelHorizontalScroller? = null
    // 是否执行滚动
    private var isScrollingPerformed: Boolean = false
    // 滚动偏移量
    private var scrollingOffset: Int = 0
    // 中间标线
    private val midBitmap: Bitmap? = null
    // 显示刻度值
    private val isScaleValue: Boolean
    private var rulerData: ArrayList<String>? = null
    private var canDraw: Boolean = false //保证数据过来之后在进行绘制
    private val dm: DisplayMetrics

    private val dotPoint: Paint

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val lineSpecialPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val markPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private val textSpecialPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)

    private val mTpDesiredWidth: Float
    private var mDownFocusX: Float = 0.toFloat()
    private var mDownFocusY: Float = 0.toFloat()
    private var isDisallowIntercept: Boolean = false
    private var onWheelListener: OnWheelScrollListener? = null
    private var isMove: Boolean = false
    // Scrolling listener
    private var scrollingListener: WheelHorizontalScroller.ScrollingListener = object : WheelHorizontalScroller.ScrollingListener {
        override fun onStarted() {
            isScrollingPerformed = true
            notifyScrollingListenersAboutStart()
        }

        override fun onScroll(distance: Int) {
            doScroll(distance)
        }

        override fun onFinished() {
            if (thatExceed()) {
                return
            }
            if (isScrollingPerformed) {
                notifyScrollingListenersAboutEnd()
                isScrollingPerformed = false
            }
            scrollingOffset = 0
            invalidate()
        }

        override fun onJustify() {
            if (thatExceed()) {
                return
            }
            if (!isDisallowIntercept && !isMove) return
            if (scrollingOffset < 0) {
                if (0 <= mCurrValue % 5 && mCurrValue % 5 <= 2.5) {
                    scroller?.scroll(-(mCurrValue - mCurrValue / 5 * 5 + 1) * mLineDivder, 500)
                } else {
                    scroller?.scroll(((mCurrValue / 5 + 1) * 5 - mCurrValue) * mLineDivder, 500)
                }
            } else {
                if (0 <= mCurrValue % 5 && mCurrValue % 5 <= 2.5) {
                    scroller?.scroll(-(mCurrValue - mCurrValue / 5 * 5) * mLineDivder, 500)
                } else {
                    scroller?.scroll(((mCurrValue / 5 + 1) * 5 - mCurrValue + 1) * mLineDivder, 500)
                }
            }
        }
    }


    init {

        dm = DisplayMetrics()
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.defaultDisplay.getMetrics(dm)

        scroller = WheelHorizontalScroller(context, scrollingListener)
        // 获取自定义属性和默认值
        val mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.RulerWheel)

        dotPoint = Paint()
        dotPoint.isAntiAlias = true                       //设置画笔为无锯齿
        dotPoint.strokeWidth = 1.5.toFloat()              //线宽
        dotPoint.style = Paint.Style.STROKE                   //空心效果

        // 刻度宽度
        val scaleWidth = mTypedArray.getDimensionPixelSize(R.styleable.RulerWheel_scaleWidth, 4)
        linePaint.strokeWidth = scaleWidth.toFloat()

        lineSpecialPaint.color = Color.parseColor("#80d36d")
        lineSpecialPaint.strokeWidth = 5f

        // 刻度颜色
        mLineColorMax = mTypedArray.getColor(R.styleable.RulerWheel_lineColorMax, Color.BLACK)
        mLineColorMid = mTypedArray.getColor(R.styleable.RulerWheel_lineColorMid, Color.BLACK)
        mLineColorMin = mTypedArray.getColor(R.styleable.RulerWheel_lineColorMin, Color.BLACK)

        mTextSize = mTypedArray.getInteger(R.styleable.RulerWheel_text_Size, 36)
        mCurrValue = mTypedArray.getInteger(R.styleable.RulerWheel_def_value, 0)
        mMaxValue = mTypedArray.getInteger(R.styleable.RulerWheel_max_value, 100)
        // 刻度模式
        mModType = obtainMode(mTypedArray.getInteger(R.styleable.RulerWheel_mode, 0))
        // 线条间距
        mLineDivder = obtainLineDivder(mTypedArray.getDimensionPixelSize(R.styleable.RulerWheel_line_divider, 0))
        // 显示刻度值
        isScaleValue = mTypedArray.getBoolean(R.styleable.RulerWheel_showScaleValue, false)
        textPaint.textSize = dip2px(12f).toFloat()
        textPaint.textAlign = Paint.Align.CENTER//居中
        textPaint.color = mLineColorMax//刻度字体颜色设置为和尺子大刻度一样的颜色。
        mTpDesiredWidth = Layout.getDesiredWidth("0", textPaint)

        textSpecialPaint.textSize = dip2px(18f).toFloat()
        textSpecialPaint.textAlign = Paint.Align.CENTER
        textSpecialPaint.color = Color.parseColor("#50afff")

        mTypedArray.recycle()
    }

    private fun obtainMode(mode: Int): Int {
        return if (mode == 1) {
            MOD_TYPE_HALF
        } else MOD_TYPE_SCALE
    }

    private fun obtainLineDivder(lineDivder: Int): Int {
        if (0 == lineDivder) {
            if (mModType == MOD_TYPE_HALF) {
                mLineDivder = dip2px(27f)
            } else {
                mLineDivder = dip2px(12f)
            }
            return mLineDivder
        }

        return lineDivder
    }

    /**
     * 越界回滚
     *
     * @return
     */
    private fun thatExceed(): Boolean {
        var outRange = 0
        if (mCurrValue < 0) {
            outRange = mCurrValue * mLineDivder
        } else if (mCurrValue > mMaxValue) {
            outRange = (mCurrValue - mMaxValue) * mLineDivder
        }
        if (0 != outRange) {
            scrollingOffset = 0
            scroller?.scroll(-outRange, 100)
            return true
        }
        return false
    }

    fun setValue(current: Int, maxValue: Int) {
        var current = current
        var maxValue = maxValue
        if (current < 0) {
            current = 0
        }
        if (maxValue < 0) {
            maxValue = 100
        }
        this.mCurrValue = current
        this.mMaxValue = maxValue
        invalidate()
    }

    /**
     * 获取当前值
     *
     * @return
     */
    val value: Int
        get() = Math.min(Math.max(0, mCurrValue), mMaxValue)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = View.getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
        //        int heightSize = midBitmap.getHeight() + getPaddingTop() + getPaddingBottom();
        val heightSize = dip2px(48f) + paddingTop + paddingBottom
        setMeasuredDimension(widthSize, heightSize)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w == 0 || h == 0)
            return
        val rHeight = h - paddingTop - paddingBottom
        mLineHeighMax = rHeight / 4
        mLineHeighMid = rHeight / 4
        mLineHeighMin = rHeight / 8
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!canDraw) {
            return
        }
        if (mLineHeighMin == 0) {
            return
        }
        val rWidth = width - paddingLeft - paddingRight
        val rHeight = height - paddingTop - paddingBottom
        val ry = 0
        //        int ry = getPaddingTop() + rHeight;
        drawScaleLine(canvas, rWidth, rHeight, ry)
        //        drawMiddleLine(canvas, rWidth, rHeight);
    }

    private fun doScroll(delta: Int) {
        var delta = delta
        if (mCurrValue < -5 || mCurrValue > mMaxValue + 5) {
            if (isDisallowIntercept)
                delta /= 3
        }
        scrollingOffset += delta
        val offsetCount = scrollingOffset / mLineDivder
        if (0 != offsetCount) {
            // 显示在范围内
            val oldValue = Math.min(Math.max(0, mCurrValue), mMaxValue)
            mCurrValue -= offsetCount
            scrollingOffset -= offsetCount * mLineDivder
            if (null != onWheelListener) {
                onWheelListener!!.onChanged(this, oldValue, Math.min(Math.max(0, mCurrValue), mMaxValue))
            }
        }
        invalidate()
    }

    /**
     * @param canvas
     * @param rWidth  显示宽度
     * @param rHeight 显示高度
     * @param ry      线y坐标
     */
    private fun drawScaleLine(canvas: Canvas, rWidth: Int, rHeight: Int, ry: Int) {
        // 根据间隔计算当前一半宽度的个数+偏移2个
        val halfCount = Math.ceil((rWidth.toFloat() / 2f / mLineDivder.toFloat()).toDouble()).toInt() + 2
        val distanceX = scrollingOffset
        val currValue = mCurrValue
        var value: Int
        var xPosition: Float
        for (i in 0..halfCount - 1) {
            //  right
            xPosition = rWidth / 2f + (i * mLineDivder).toFloat() + distanceX.toFloat()
            value = currValue + i
            if (xPosition <= rWidth && value >= 0 && value <= mMaxValue) {
                if (value % mModType == 0) {
                    if (mModType == MOD_TYPE_HALF) {
                        linePaint.color = Color.parseColor("#7fa2f5")
                        if (isScaleValue) {
                        }
                    } else if (mModType == MOD_TYPE_SCALE) {
                        if (value % MOD_TYPE_SCALE == 0) {
                            //                        linePaint.setColor(mLineColorMax);
                            if (currValue != value) {
                                linePaint.strokeWidth = 2f
                                linePaint.color = Color.parseColor("#7fa2f5")
                            }
                            if (isScaleValue) {
                                val text = rulerData!![value / 5]
                                if (currValue == value) {
                                } else {
                                    //未选中的
                                    textPaint.textSize = dip2px(16f).toFloat()
                                    textPaint.color = Color.parseColor("#aaaaaa")
                                    textPaint.alpha = getAlpha(halfCount, i)
                                    canvas.drawText(text, xPosition, (rHeight - mLineHeighMax - dip2px(7f)).toFloat(), textPaint)
                                    //绘制圆形
                                    dotPoint.color = Color.parseColor("#aaaaaa")
                                    dotPoint.alpha = getAlpha(halfCount, i)
                                    canvas.drawCircle(xPosition, (rHeight - mLineHeighMax - dip2px(12f)).toFloat(), dip2px(14f).toFloat(), dotPoint)
                                }
                            }
                        } else {//画中间的刻度
                        }
                    }
                } else {//小刻度
                }
            }

            //  left
            xPosition = rWidth / 2f - i * mLineDivder + distanceX
            value = currValue - i
            if (xPosition > paddingLeft && value >= 0 && value <= mMaxValue) {
                if (value % mModType == 0) {
                    if (mModType == MOD_TYPE_HALF) {
                        //                        linePaint.setColor(mLineColorMax);
                        linePaint.color = Color.parseColor("#ffffff")
                        //                        canvas.drawLine(xPosition, ry, xPosition, ry + mLineHeighMax, linePaint);
                        if (isScaleValue) {
                            //                            canvas.drawText(String.valueOf(value / 2), xPosition, rHeight - mTpDesiredWidth, textPaint);
                        }
                    } else if (mModType == MOD_TYPE_SCALE) {
                        if (value % MOD_TYPE_SCALE == 0) {
                            //                        linePaint.setColor(mLineColorMax);
                            if (currValue != value) {
                                linePaint.strokeWidth = 2f
                                linePaint.color = Color.parseColor("#ffffff")
                                //                                canvas.drawLine(xPosition, rHeight, xPosition, rHeight - mLineHeighMax, linePaint);
                            }
                            if (isScaleValue) {
                                val text = rulerData!![value / 5]
                                if (currValue == value) {
                                    //选中
                                    textSpecialPaint.textSize = dip2px(24f).toFloat()
                                    textSpecialPaint.color = Color.parseColor("#5f8bf3")
                                    canvas.drawText(text, xPosition, (rHeight - mLineHeighMax - dip2px(3f)).toFloat(), textSpecialPaint)
                                    //绘制圆形
                                    dotPoint.color = Color.parseColor("#5f8bf3")
                                    canvas.drawCircle(xPosition, (rHeight - mLineHeighMax - dip2px(12f)).toFloat(), dip2px(22f).toFloat(), dotPoint)
                                } else {
                                    //未选中的
                                    textPaint.textSize = dip2px(16f).toFloat()
                                    textPaint.color = Color.parseColor("#aaaaaa")
                                    textPaint.alpha = getAlpha(halfCount, i)
                                    canvas.drawText(text, xPosition, (rHeight - mLineHeighMax - dip2px(7f)).toFloat(), textPaint)
                                    //绘制圆形
                                    dotPoint.color = Color.parseColor("#aaaaaa")
                                    dotPoint.alpha = getAlpha(halfCount, i)
                                    canvas.drawCircle(xPosition, (rHeight - mLineHeighMax - dip2px(12f)).toFloat(), dip2px(14f).toFloat(), dotPoint)
                                }
                            }
                        } else { //中间刻度
                        }
                    }
                } else {//小刻度
                }
            }
        }
    }

    private fun getAlpha(halfCount: Int, index: Int): Int {
        val MAX_ALPHA_VALUE = 255
        return MAX_ALPHA_VALUE / halfCount * (halfCount - index)
    }

    private fun drawMiddl3eLine(canvas: Canvas, rWidth: Int, rHeight: Int) {
        canvas.drawBitmap(midBitmap!!, ((rWidth - midBitmap.width) / 2).toFloat(), ((rHeight - midBitmap.height) / 2).toFloat(), markPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return true
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mDownFocusX = event.x
                mDownFocusY = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                isMove = event.x > mDownFocusX + 7 || event.x < mDownFocusX - 7
                if (!isDisallowIntercept && Math.abs(event.y - mDownFocusY) < Math.abs(event.x - mDownFocusX)) {
                    isDisallowIntercept = true
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true)
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                val x = event.x
                if (x < width / 9 * 4 && mCurrValue > 0) {
                    //左侧
                    scroller?.scroll(-(5 * mLineDivder), 500)
                } else if (x > width / 9 * 5 && mCurrValue < mMaxValue) {
                    //右侧
                    scroller?.scroll(5 * mLineDivder, 500)
                }
                if (parent != null) {
                    parent.requestDisallowInterceptTouchEvent(false)
                }
                isDisallowIntercept = false
            }
        }
        return scroller?.onTouchEvent(event)!!
    }

    /**
     * Adds wheel changing listener
     *
     * @param listener the listener
     */
    fun setScrollingListener(listener: OnWheelScrollListener) {
        onWheelListener = listener
    }

    /**
     * Removes wheel changing listener
     */
    fun removeScrollingListener() {
        onWheelListener = null
    }

    /**
     * Notifies changing listeners
     *
     * @param oldValue the old wheel value
     * @param newValue the new wheel value
     */
    protected fun notifyScrollingListeners(oldValue: Int, newValue: Int) {
        onWheelListener!!.onChanged(this, oldValue, newValue)
    }

    private fun notifyScrollingListenersAboutStart() {
        if (null != onWheelListener) {
            onWheelListener!!.onScrollingStarted(this)
        }
    }

    private fun notifyScrollingListenersAboutEnd() {
        if (null != onWheelListener) {
            onWheelListener!!.onScrollingFinished(this)
        }
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    private fun dip2px(dpValue: Float): Int {
        return (dpValue * dm.density + 0.5f).toInt()
    }

    /**
     * 获取尺子的刻度值
     *
     * @param rulerData
     */
    fun setData(rulerData: ArrayList<String>) {
        this.rulerData = rulerData
        mMaxValue = rulerData.size * 5 - 5
        mCurrValue = 0
        //setData前先设置监听 即可回调默认值
        if (onWheelListener != null)
            onWheelListener!!.onChanged(this, 0, mCurrValue)
        canDraw = true
        invalidate()
    }

    interface OnWheelScrollListener {
        /**
         * Callback method to be invoked when current item changed
         *
         * @param wheel    the wheel view whose state has changed
         * @param oldValue the old value of current item
         * @param newValue the new value of current item
         */
        fun onChanged(wheel: RulerDays, oldValue: Int, newValue: Int)

        /**
         * Callback method to be invoked when scrolling started.
         *
         * @param wheel the wheel view whose state has changed.
         */
        fun onScrollingStarted(wheel: RulerDays)

        /**
         * Callback method to be invoked when scrolling ended.
         *
         * @param wheel the wheel view whose state has changed.
         */
        fun onScrollingFinished(wheel: RulerDays)
    }

    companion object {
        // 默认刻度模式
        val MOD_TYPE_SCALE = 5
        // 1/2模式
        val MOD_TYPE_HALF = 2
    }
}