package com.xlqianbao.and.ext

import android.content.Intent
import android.support.v4.app.Fragment

/**
 * Created by Makise on 2017/8/31.
 */
fun Fragment.startNewActivity(intent: Intent, isFinish: Boolean) {
    activity.startNewActivity(intent, isFinish)
}

fun Fragment.startNewActivity(intent: Intent, enterAnim: Int, exitAnim: Int, isFinish: Boolean) {
    activity.startNewActivity(intent, enterAnim, exitAnim, isFinish)
}

fun Fragment.toast(message: String, duration: Int) {
    activity.toast(message, duration)
}