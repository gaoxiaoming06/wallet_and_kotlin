package com.xlqianbao.and.ext

import android.app.NotificationManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * View的扩展类
 * Created by Makise on 2017/5/23.
 */

/**
 * 设置view的高度
 */
fun View.setHeight(height: Int) {
    val params = layoutParams
    params.height = height
    layoutParams = params
}

/**
 * 设置 view 可见
 */
fun View.visible() {
    visibility = View.VISIBLE
}

/**
 * 设置 view 隐藏
 */
fun View.invisible() {
    visibility = View.INVISIBLE
}

/**
 * 设置 view 不可见
 */
fun View.gone() {
    visibility = View.GONE
}

/**
 * Inflate a layout
 */
fun Context.inflate(res: Int, parent: ViewGroup? = null): View {
    return LayoutInflater.from(this).inflate(res, parent, false)
}

/**
 * 获取系统服务示例
 */
val Context.notificationManager: NotificationManager
    get() = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager