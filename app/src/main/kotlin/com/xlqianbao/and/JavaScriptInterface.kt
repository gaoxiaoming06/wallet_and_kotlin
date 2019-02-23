package com.xlqianbao.and

import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebView

/**
 * 被js调用的方法
 * Created by Makise on 2017/2/4.
 */

class JavaScriptInterface(private val mwebView: WebView, private val mContext: Context) {

    @JavascriptInterface
    fun test(params: String) {
        //实现
    }

    companion object {

        fun name(): String {
            return "_XB"
        }
    }
}
