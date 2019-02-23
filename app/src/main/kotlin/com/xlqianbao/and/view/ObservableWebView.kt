package com.xlqianbao.and.view

import android.content.Context
import android.util.AttributeSet
import android.webkit.WebView

/**
 * desc: 获得webview的滑动偏移量和方向
 * Created by Liz on 2016/9/10.
 * github: https://github.com/lizwangying
 */
class ObservableWebView : WebView {
    var onScrollChangedCallback: OnScrollChangedCallback? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet,
                defStyle: Int) : super(context, attrs, defStyle)

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)

        if (onScrollChangedCallback != null) {
            onScrollChangedCallback!!.onScroll(l, t)
        }
    }

    /**
     * Impliment in the activity/fragment/view that you want to listen to the webview
     */
    interface OnScrollChangedCallback {
        fun onScroll(dx: Int, dy: Int)
    }
}
