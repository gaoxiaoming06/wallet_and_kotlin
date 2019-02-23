package com.xlqianbao.and.view

import android.content.Context
import android.os.Handler
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.animation.Interpolator
import android.widget.Scroller

/**
 * Created by gao on 2017/9/1.
 */
class WheelHorizontalScroller(context: Context, listener: ScrollingListener) {

    private val MESSAGE_SCROLL = 0
    private val MESSAGE_JUSTIFY = 1
    // Listener
    private val listener: ScrollingListener
    private val context: Context
    private var gestureDetector: GestureDetector
    private var scroller: Scroller? = null
    private var lastScrollX: Int = 0
    private var lastTouchedX: Float = 0.toFloat()
    private var isScrollingPerformed: Boolean = false

    private val animationHandler = Handler(Handler.Callback { msg ->
        scroller?.computeScrollOffset()
        val currX = scroller?.getCurrX()
        val delta = lastScrollX - currX!!
        lastScrollX = currX
        if (delta != 0) {
            listener.onScroll(delta)
        }

        if (Math.abs(currX - scroller?.finalX!!) < MIN_DELTA_FOR_SCROLLING) {
            lastScrollX = scroller?.finalX!!
            scroller?.forceFinished(true)
        }
        if (!scroller?.isFinished!!) {
            sendEmptyMessage(msg.what)
        } else if (msg.what == MESSAGE_SCROLL) {
            justify()
        } else {
            finishScrolling()
        }
        true
    })

    private val gestureListener = object : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            return true
        }

        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            lastScrollX = 0
            scroller?.fling(0, lastScrollX, (-velocityX).toInt(), 0, -0x7FFFFFFF, 0x7FFFFFFF, 0, 0)
            setNextMessage(MESSAGE_SCROLL)
            return true
        }
    }

    init {
        gestureDetector = GestureDetector(context, gestureListener)
        gestureDetector.setIsLongpressEnabled(false)
        scroller = Scroller(context)
        scroller?.setFriction(0.05f)

        this.listener = listener
        this.context = context
    }

    fun setInterpolator(interpolator: Interpolator) {
        scroller?.forceFinished(true)
        scroller = Scroller(context, interpolator)
    }

    fun scroll(distance: Int, time: Int) {
        scroller?.forceFinished(true)
        lastScrollX = 0
        scroller?.startScroll(0, 0, distance, 0, if (time != 0) time else SCROLLING_DURATION)
        setNextMessage(MESSAGE_SCROLL)
        startScrolling()
    }

    fun stopScrolling() {
        scroller?.forceFinished(true)
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchedX = event.x
                scroller?.forceFinished(true)//手指按下强行停止scroll
                clearMessages()
            }

            MotionEvent.ACTION_MOVE -> {
                // perform scrolling
                val distanceX = (event.x - lastTouchedX).toInt()
                if (distanceX != 0) {
                    startScrolling()
                    listener.onScroll(distanceX)
                    lastTouchedX = event.x
                }
            }
        }

        if (!gestureDetector.onTouchEvent(event) && event.action == MotionEvent.ACTION_UP) {
            justify()
        }

        return true
    }

    private fun setNextMessage(message: Int) {
        clearMessages()
        animationHandler.sendEmptyMessage(message)
    }

    private fun sendEmptyMessage(what: Int) {
        animationHandler.sendEmptyMessage(what)
    }

    private fun clearMessages() {
        animationHandler.removeMessages(MESSAGE_SCROLL)
        animationHandler.removeMessages(MESSAGE_JUSTIFY)
    }

    private fun justify() {
        listener.onJustify()
        setNextMessage(MESSAGE_JUSTIFY)
    }

    private fun startScrolling() {
        if (!isScrollingPerformed) {
            isScrollingPerformed = true
            listener.onStarted()
        }
    }

    internal fun finishScrolling() {
        if (isScrollingPerformed) {
            listener.onFinished()
            isScrollingPerformed = false
        }
    }

    interface ScrollingListener {
        fun onScroll(distance: Int)

        fun onStarted()

        fun onFinished()

        fun onJustify()
    }

    companion object {
        val SCROLLING_DURATION = 400
        val MIN_DELTA_FOR_SCROLLING = 1
    }
}