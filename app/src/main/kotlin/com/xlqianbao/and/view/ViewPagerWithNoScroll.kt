package com.xlqianbao.and.view

import android.content.Context
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent
import com.xlqianbao.and.utils.rx.RxTask

import java.util.concurrent.TimeUnit

/**
 * 禁止滑动的viewpager
 * Created by Makise on 2016/8/10.
 */
class ViewPagerWithNoScroll @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : ViewPager(context, attrs) {

    init {
        //自定义滑动速度
        ViewPagerScroller(getContext()).initViewPagerScroll(this)
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return false
        //        return super.onTouchEvent(ev);
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return false
        //        return super.onInterceptTouchEvent(ev);
    }

    /**
     * 下一页
     */
    operator fun next() {
        RxTask.doInUIThreadDelay(
                object : RxTask.UITask() {
                    override fun doInUIThread() {
                        currentItem += 1
                    }
                }, 200, TimeUnit.MILLISECONDS)
    }

    /**
     * 上一页
     */
    fun previous() {
        RxTask.doInUIThreadDelay(object : RxTask.UITask() {
            override fun doInUIThread() {
                currentItem -= 1
            }
        }, 200, TimeUnit.MILLISECONDS)
    }
}
