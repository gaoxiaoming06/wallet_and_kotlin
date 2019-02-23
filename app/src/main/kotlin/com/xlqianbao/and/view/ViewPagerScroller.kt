package com.xlqianbao.and.view

import android.content.Context
import android.support.v4.view.ViewPager
import android.view.animation.Interpolator
import android.widget.Scroller

/**
 * 自定义viewpager滑动速度
 * Created by Makise on 2016/12/22.
 */

class ViewPagerScroller : Scroller {
    /**
     * 获取当前设置的速度
     */
    /**
     * 设置速度速度

     * @param duration
     */
    var scrollDuration = 800 // 滑动速度

    constructor(context: Context) : super(context) {}

    constructor(context: Context, interpolator: Interpolator) : super(context, interpolator) {}

    constructor(context: Context, interpolator: Interpolator,
                flywheel: Boolean) : super(context, interpolator, flywheel) {
    }

    override fun startScroll(startX: Int, startY: Int, dx: Int, dy: Int, duration: Int) {
        super.startScroll(startX, startY, dx, dy, scrollDuration)
    }

    override fun startScroll(startX: Int, startY: Int, dx: Int, dy: Int) {
        super.startScroll(startX, startY, dx, dy, scrollDuration)
    }

    fun initViewPagerScroll(viewPager: ViewPager) {
        try {
            val mScroller = ViewPager::class.java.getDeclaredField("mScroller")
            mScroller.setAccessible(true)
            mScroller.set(viewPager, this)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}